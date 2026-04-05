'use client';

import { useEffect, useState, useCallback, useRef, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Polyline, useMapEvents, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { api } from '@/lib/api';
import type {
  LocationData, FactoryData, ConsumerData, WarehouseData,
  ServiceStationData, TransportData, DeliveryOrderData, ProductData, EntityType,
} from '@/types';

// ── Config ────────────────────────────────────────────────────────────
const UA_CENTER: [number, number] = [48.5, 31.2];
const UA_BOUNDS: L.LatLngBoundsExpression = [[43.5, 21.5], [53.0, 41.0]];

const ENTITY_CFG = {
  location:       { emoji: '📍', color: '#64748b', label: 'Локація' },
  factory:        { emoji: '🏭', color: '#ea580c', label: 'Фабрика' },
  consumer:       { emoji: '🏢', color: '#2563eb', label: 'Споживач' },
  warehouse:      { emoji: '📦', color: '#16a34a', label: 'Склад' },
  serviceStation: { emoji: '⛽', color: '#d97706', label: 'Сервіс' },
} as const;

const TRANSPORT_CFG = {
  CAR:      { emoji: '🚛', color: '#ef4444' },
  TRAIN:    { emoji: '🚂', color: '#8b5cf6' },
  SHIP:     { emoji: '🚢', color: '#06b6d4' },
  AIRPLANE: { emoji: '✈️', color: '#ec4899' },
} as const;

const ROUTE_COLORS = ['#a855f7','#f97316','#06b6d4','#84cc16','#f43f5e','#fbbf24'];

// ── Types ─────────────────────────────────────────────────────────────
interface CtxMenu {
  x: number; y: number;
  lat: number; lng: number;
  mode: 'map' | EntityType;
  entityId?: number;
  parentLocId?: number;
}

interface Dialog {
  mode: 'create' | 'edit';
  type: EntityType;
  lat?: number; lng?: number;
  parentLocId?: number;
  entityId?: number;
}

interface Timeline {
  playing: boolean; speedDays: number;
  current: Date; start: Date; end: Date;
}

// ── Helpers ───────────────────────────────────────────────────────────
function mkIcon(emoji: string, bg: string, sz = 36) {
  return L.divIcon({
    html: `<div style="width:${sz}px;height:${sz}px;background:${bg};border-radius:50%;border:2.5px solid rgba(255,255,255,.85);box-shadow:0 2px 10px rgba(0,0,0,.55);display:flex;align-items:center;justify-content:center;font-size:${Math.round(sz*.5)}px;cursor:pointer;transition:transform .15s">${emoji}</div>`,
    className: '',
    iconSize: [sz, sz],
    iconAnchor: [sz / 2, sz / 2],
    tooltipAnchor: [sz / 2 + 4, 0],
  });
}

function transportPos(
  t: TransportData, orders: DeliveryOrderData[], now: Date,
): [number, number] | null {
  const order = orders.find(o =>
    o.transport?.id === t.id && ['CONFIRMED', 'IN_TRANSIT'].includes(o.status),
  );
  const fallback = t.currentLocation;
  const fb: [number, number] | null =
    fallback?.latitude ? [fallback.latitude!, fallback.longitude!] : null;

  if (!order) return fb;
  const from = order.factory?.location;
  const to   = order.consumer?.location;
  if (!from?.latitude || !to?.latitude) return fb;

  const pickup   = order.scheduledPickup   ? new Date(order.scheduledPickup)   : null;
  const delivery = order.scheduledDelivery ? new Date(order.scheduledDelivery) : null;
  if (!pickup || !delivery) return [from.latitude, from.longitude!];

  if (now <= pickup)   return [from.latitude, from.longitude!];
  if (now >= delivery) return [to.latitude,   to.longitude!];

  const p = (now.getTime() - pickup.getTime()) / (delivery.getTime() - pickup.getTime());
  return [
    from.latitude + p * (to.latitude   - from.latitude),
    from.longitude! + p * (to.longitude! - from.longitude!),
  ];
}

// ── MapEvents inner component ─────────────────────────────────────────
function MapEvents({
  onRightClick, skipRef,
}: {
  onRightClick: (lat: number, lng: number, x: number, y: number) => void;
  skipRef: React.MutableRefObject<boolean>;
}) {
  useMapEvents({
    contextmenu: (e) => {
      if (skipRef.current) { skipRef.current = false; return; }
      e.originalEvent.preventDefault();
      onRightClick(e.latlng.lat, e.latlng.lng, e.originalEvent.clientX, e.originalEvent.clientY);
    },
    click: () => { /* handled via React onClick */ },
  });
  return null;
}

// ── Styles helpers ────────────────────────────────────────────────────
const panel: React.CSSProperties = {
  background: 'rgba(15,23,42,.95)', border: '1px solid #334155',
  borderRadius: 12, backdropFilter: 'blur(8px)', color: '#e2e8f0',
};
const menuBtn: React.CSSProperties = {
  display: 'block', width: '100%', textAlign: 'left',
  padding: '7px 12px', background: 'none', border: 'none',
  color: '#e2e8f0', fontSize: 13, cursor: 'pointer', borderRadius: 6,
};

// ── Main Map component ─────────────────────────────────────────────────
export default function MapView() {
  const [locations,       setLocations]       = useState<LocationData[]>([]);
  const [factories,       setFactories]       = useState<FactoryData[]>([]);
  const [consumers,       setConsumers]       = useState<ConsumerData[]>([]);
  const [warehouses,      setWarehouses]      = useState<WarehouseData[]>([]);
  const [serviceStations, setServiceStations] = useState<ServiceStationData[]>([]);
  const [transports,      setTransports]      = useState<TransportData[]>([]);
  const [orders,          setOrders]          = useState<DeliveryOrderData[]>([]);
  const [products,        setProducts]        = useState<ProductData[]>([]);

  const [ctx,      setCtx]      = useState<CtxMenu | null>(null);
  const [dialog,   setDialog]   = useState<Dialog | null>(null);
  const [tranPanel, setTranPanel] = useState(false);

  const [timeline, setTimeline] = useState<Timeline>(() => {
    const now = new Date();
    return {
      playing: false, speedDays: 1,
      current: now,
      start: new Date(now.getFullYear(), now.getMonth() - 1, 1),
      end:   new Date(now.getFullYear(), now.getMonth() + 3, 1),
    };
  });

  const skipCtxRef = useRef(false);
  const rafRef     = useRef<number>(0);
  const lastTsRef  = useRef<number | undefined>(undefined);

  // ── Icons (must be browser-only) ─────────────────────────────────
  const icons = useMemo(() => ({
    location:       mkIcon('📍', '#64748b'),
    factory:        mkIcon('🏭', '#ea580c'),
    consumer:       mkIcon('🏢', '#2563eb'),
    warehouse:      mkIcon('📦', '#16a34a'),
    serviceStation: mkIcon('⛽', '#d97706'),
    CAR:            mkIcon('🚛', '#ef4444', 42),
    TRAIN:          mkIcon('🚂', '#8b5cf6', 42),
    SHIP:           mkIcon('🚢', '#06b6d4', 42),
    AIRPLANE:       mkIcon('✈️', '#ec4899', 42),
  }), []);

  // ── Load all data ─────────────────────────────────────────────────
  const loadAll = useCallback(async () => {
    const results = await Promise.allSettled([
      api.locations.getAll(),
      api.factories.getAll(),
      api.consumers.getAll(),
      api.warehouses.getAll(),
      api.serviceStations.getAll(),
      api.transports.getAll(),
      api.deliveryOrders.getAll(),
      api.products.getAll(),
    ]);
    if (results[0].status === 'fulfilled') setLocations(results[0].value);
    if (results[1].status === 'fulfilled') setFactories(results[1].value);
    if (results[2].status === 'fulfilled') setConsumers(results[2].value);
    if (results[3].status === 'fulfilled') setWarehouses(results[3].value);
    if (results[4].status === 'fulfilled') setServiceStations(results[4].value);
    if (results[5].status === 'fulfilled') setTransports(results[5].value);
    if (results[6].status === 'fulfilled') setOrders(results[6].value);
    if (results[7].status === 'fulfilled') setProducts(results[7].value);
  }, []);

  useEffect(() => { loadAll(); }, [loadAll]);

  // ── Timeline animation ────────────────────────────────────────────
  useEffect(() => {
    if (!timeline.playing) {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      lastTsRef.current = undefined as unknown as number;
      return;
    }
    const tick = (ts: number) => {
      if (lastTsRef.current !== undefined) {
        const dtMs = ts - lastTsRef.current;
        const virtMs = dtMs * timeline.speedDays * 86400; // ms per real-second = speedDays virtual days
        setTimeline(prev => {
          const next = prev.current.getTime() + virtMs;
          const capped = Math.min(next, prev.end.getTime());
          return { ...prev, current: new Date(capped), playing: capped < prev.end.getTime() };
        });
      }
      lastTsRef.current = ts;
      rafRef.current = requestAnimationFrame(tick);
    };
    rafRef.current = requestAnimationFrame(tick);
    return () => { if (rafRef.current) cancelAnimationFrame(rafRef.current); };
  }, [timeline.playing, timeline.speedDays]);

  // ── Context menu ──────────────────────────────────────────────────
  const onMarkerCtx = useCallback((
    e: L.LeafletMouseEvent, type: EntityType, id: number, locId?: number,
  ) => {
    skipCtxRef.current = true;
    e.originalEvent.preventDefault();
    setCtx({
      x: e.originalEvent.clientX, y: e.originalEvent.clientY,
      lat: e.latlng.lat, lng: e.latlng.lng,
      mode: type, entityId: id, parentLocId: locId,
    });
  }, []);

  // ── Save entity ───────────────────────────────────────────────────
  const handleSave = useCallback(async (data: Record<string, unknown>) => {
    if (!dialog) return;
    try {
      const apiMap = {
        location:       api.locations,
        factory:        api.factories,
        consumer:       api.consumers,
        warehouse:      api.warehouses,
        serviceStation: api.serviceStations,
      } as Record<string, { create: (d: Record<string, unknown>) => Promise<unknown>; update: (id: number, d: Record<string, unknown>) => Promise<unknown> }>;

      let locationId = dialog.parentLocId;

      if (dialog.type !== 'location' && !locationId && dialog.lat !== undefined) {
        const loc = await api.locations.create({
          name: `${data.name} (точка)`,
          latitude: dialog.lat, longitude: dialog.lng,
          hasPort: false, hasAirport: false, hasRailTerminal: false, hasRoadAccess: true,
        });
        locationId = loc.id;
      }

      const payload = dialog.type !== 'location' ? { ...data, locationId } : data;

      if (dialog.mode === 'create') {
        await apiMap[dialog.type].create(payload);
      } else if (dialog.entityId) {
        await apiMap[dialog.type].update(dialog.entityId, payload);
      }

      await loadAll();
      setDialog(null);
    } catch (e) {
      alert('Помилка: ' + (e as Error).message);
    }
  }, [dialog, loadAll]);

  const handleDelete = useCallback(async (type: EntityType, id: number) => {
    if (!confirm('Видалити?')) return;
    const apiMap = {
      location: api.locations, factory: api.factories, consumer: api.consumers,
      warehouse: api.warehouses, serviceStation: api.serviceStations,
    } as Record<string, { delete: (id: number) => Promise<void> }>;
    try {
      await apiMap[type].delete(id);
      await loadAll();
      setCtx(null);
    } catch (e) { alert('Помилка: ' + (e as Error).message); }
  }, [loadAll]);

  // ── Resolve entity for edit ───────────────────────────────────────
  const editEntity = useMemo(() => {
    if (!dialog?.entityId) return null;
    const id = dialog.entityId;
    switch (dialog.type) {
      case 'location':       return locations.find(l => l.id === id);
      case 'factory':        return factories.find(f => f.id === id);
      case 'consumer':       return consumers.find(c => c.id === id);
      case 'warehouse':      return warehouses.find(w => w.id === id);
      case 'serviceStation': return serviceStations.find(s => s.id === id);
    }
  }, [dialog, locations, factories, consumers, warehouses, serviceStations]);

  const totalMs   = timeline.end.getTime() - timeline.start.getTime();
  const elapsedMs = timeline.current.getTime() - timeline.start.getTime();
  const progress  = totalMs > 0 ? elapsedMs / totalMs : 0;

  const fmtDate = (d: Date) =>
    d.toLocaleDateString('uk-UA', { day: '2-digit', month: 'short', year: 'numeric' });

  return (
    <div style={{ position: 'relative', width: '100%', height: '100%' }} onClick={() => setCtx(null)}>

      {/* ── Leaflet Map ─────────────────────────────────────────── */}
      <MapContainer
        center={UA_CENTER} zoom={6} minZoom={5}
        maxBounds={UA_BOUNDS} maxBoundsViscosity={0.85}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://carto.com">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />

        <MapEvents
          skipRef={skipCtxRef}
          onRightClick={(lat, lng, x, y) =>
            setCtx({ x, y, lat, lng, mode: 'map' })
          }
        />

        {/* Delivery routes */}
        {orders.map((o, i) => {
          const fa = o.factory?.location;
          const co = o.consumer?.location;
          if (!fa?.latitude || !co?.latitude) return null;
          return (
            <Polyline key={`route-${o.id}`}
              positions={[[fa.latitude, fa.longitude!], [co.latitude, co.longitude!]]}
              pathOptions={{ color: ROUTE_COLORS[i % ROUTE_COLORS.length], weight: 2, opacity: 0.55, dashArray: '6 5' }}
            />
          );
        })}

        {/* Location markers */}
        {locations.map(l => l.latitude ? (
          <Marker key={`loc-${l.id}`} position={[l.latitude, l.longitude!]} icon={icons.location}
            eventHandlers={{ contextmenu: e => onMarkerCtx(e, 'location', l.id) }}>
            <Tooltip>{l.name}</Tooltip>
          </Marker>
        ) : null)}

        {/* Factory markers */}
        {factories.map(f => f.location?.latitude ? (
          <Marker key={`fac-${f.id}`} position={[f.location.latitude, f.location.longitude!]} icon={icons.factory}
            eventHandlers={{ contextmenu: e => onMarkerCtx(e, 'factory', f.id, f.location?.id) }}>
            <Tooltip>{f.name}</Tooltip>
          </Marker>
        ) : null)}

        {/* Consumer markers */}
        {consumers.map(c => c.location?.latitude ? (
          <Marker key={`con-${c.id}`} position={[c.location.latitude, c.location.longitude!]} icon={icons.consumer}
            eventHandlers={{ contextmenu: e => onMarkerCtx(e, 'consumer', c.id, c.location?.id) }}>
            <Tooltip>{c.name}</Tooltip>
          </Marker>
        ) : null)}

        {/* Warehouse markers */}
        {warehouses.map(w => w.location?.latitude ? (
          <Marker key={`wh-${w.id}`} position={[w.location.latitude, w.location.longitude!]} icon={icons.warehouse}
            eventHandlers={{ contextmenu: e => onMarkerCtx(e, 'warehouse', w.id, w.location?.id) }}>
            <Tooltip>{w.name}</Tooltip>
          </Marker>
        ) : null)}

        {/* Service station markers */}
        {serviceStations.map(s => s.location?.latitude ? (
          <Marker key={`ss-${s.id}`} position={[s.location.latitude, s.location.longitude!]} icon={icons.serviceStation}
            eventHandlers={{ contextmenu: e => onMarkerCtx(e, 'serviceStation', s.id, s.location?.id) }}>
            <Tooltip>{s.name}</Tooltip>
          </Marker>
        ) : null)}

        {/* Transport markers (animated) */}
        {transports.map(t => {
          const pos = transportPos(t, orders, timeline.current);
          if (!pos) return null;
          const cfg = TRANSPORT_CFG[t.type] || TRANSPORT_CFG.CAR;
          const icon = icons[t.type] || icons.CAR;
          return (
            <Marker key={`tr-${t.id}`} position={pos} icon={icon}>
              <Tooltip permanent={false}>
                {cfg.emoji} {t.name}<br />
                <span style={{ fontSize: 11, color: '#94a3b8' }}>{t.status}</span>
              </Tooltip>
            </Marker>
          );
        })}
      </MapContainer>

      {/* ── Top-right toolbar ──────────────────────────────────── */}
      <div style={{ position: 'absolute', top: 12, right: 12, zIndex: 1000, display: 'flex', flexDirection: 'column', gap: 6 }}>
        {[
          { label: '🚛 Транспорт', onClick: () => setTranPanel(p => !p) },
          { label: '🔄 Оновити',   onClick: loadAll },
        ].map(b => (
          <button key={b.label} onClick={b.onClick}
            style={{ ...panel, padding: '7px 14px', cursor: 'pointer', fontSize: 13, fontWeight: 600, border: '1px solid #475569' }}>
            {b.label}
          </button>
        ))}
      </div>

      {/* ── Legend ─────────────────────────────────────────────── */}
      <div style={{ ...panel, position: 'absolute', top: 8, left: 8, zIndex: 1000, padding: '10px 14px', fontSize: 12 }}>
        <div style={{ fontWeight: 700, fontSize: 10, textTransform: 'uppercase', letterSpacing: '.05em', color: '#94a3b8', marginBottom: 6 }}>Легенда</div>
        {Object.entries(ENTITY_CFG).map(([k, v]) => (
          <div key={k} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 3 }}>
            <span>{v.emoji}</span>
            <span style={{ color: v.color, fontWeight: 600 }}>{v.label}</span>
          </div>
        ))}
        {Object.entries(TRANSPORT_CFG).map(([k, v]) => (
          <div key={k} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 3 }}>
            <span>{v.emoji}</span>
            <span style={{ color: v.color, fontWeight: 600 }}>{k}</span>
          </div>
        ))}
        <div style={{ marginTop: 8, borderTop: '1px solid #334155', paddingTop: 6, color: '#64748b', fontSize: 10 }}>
          ПКМ на карті → додати<br />ПКМ на маркері → редагувати
        </div>
      </div>

      {/* ── Context menu ───────────────────────────────────────── */}
      {ctx && (
        <div onClick={e => e.stopPropagation()}
          style={{ ...panel, position: 'fixed', left: ctx.x, top: ctx.y, zIndex: 10000, minWidth: 190, padding: 6, boxShadow: '0 8px 28px rgba(0,0,0,.7)' }}>
          {ctx.mode === 'map' ? (
            <>
              <div style={{ padding: '4px 10px', color: '#64748b', fontSize: 10, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.05em' }}>Додати</div>
              {Object.entries(ENTITY_CFG).map(([type, cfg]) => (
                <button key={type} style={menuBtn}
                  onMouseEnter={e => (e.currentTarget.style.background = '#1e293b')}
                  onMouseLeave={e => (e.currentTarget.style.background = 'none')}
                  onClick={() => { setDialog({ mode: 'create', type: type as EntityType, lat: ctx.lat, lng: ctx.lng }); setCtx(null); }}>
                  {cfg.emoji} {cfg.label}
                </button>
              ))}
            </>
          ) : (
            <>
              <div style={{ padding: '4px 10px', color: '#64748b', fontSize: 10, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '.05em' }}>
                {ENTITY_CFG[ctx.mode as EntityType]?.label}
              </div>
              <button style={menuBtn}
                onMouseEnter={e => (e.currentTarget.style.background = '#1e293b')}
                onMouseLeave={e => (e.currentTarget.style.background = 'none')}
                onClick={() => { setDialog({ mode: 'edit', type: ctx.mode as EntityType, entityId: ctx.entityId, parentLocId: ctx.parentLocId }); setCtx(null); }}>
                ✏️ Редагувати
              </button>
              {ctx.mode === 'location' && Object.entries(ENTITY_CFG).filter(([k]) => k !== 'location').map(([type, cfg]) => (
                <button key={type} style={menuBtn}
                  onMouseEnter={e => (e.currentTarget.style.background = '#1e293b')}
                  onMouseLeave={e => (e.currentTarget.style.background = 'none')}
                  onClick={() => { setDialog({ mode: 'create', type: type as EntityType, parentLocId: ctx.entityId }); setCtx(null); }}>
                  {cfg.emoji} Додати {cfg.label}
                </button>
              ))}
              <div style={{ borderTop: '1px solid #334155', margin: '4px 0' }} />
              <button style={{ ...menuBtn, color: '#f87171' }}
                onMouseEnter={e => (e.currentTarget.style.background = '#1e293b')}
                onMouseLeave={e => (e.currentTarget.style.background = 'none')}
                onClick={() => handleDelete(ctx.mode as EntityType, ctx.entityId!)}>
                🗑️ Видалити
              </button>
            </>
          )}
        </div>
      )}

      {/* ── Entity dialog ──────────────────────────────────────── */}
      {dialog && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.6)', zIndex: 10001, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          onClick={() => setDialog(null)}>
          <div style={{ ...panel, padding: 24, borderRadius: 16, minWidth: 360, maxWidth: 480, width: '100%', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 60px rgba(0,0,0,.8)' }}
            onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
              <h2 style={{ margin: 0, fontSize: 17, fontWeight: 700 }}>
                {dialog.mode === 'create' ? '➕' : '✏️'} {dialog.mode === 'create' ? 'Додати' : 'Редагувати'} {ENTITY_CFG[dialog.type]?.label}
              </h2>
              <button onClick={() => setDialog(null)} style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: 20, cursor: 'pointer' }}>✕</button>
            </div>
            <EntityForm
              type={dialog.type}
              initial={editEntity as Record<string, unknown> | null}
              showLocationFields={dialog.type === 'location'}
              lat={dialog.lat} lng={dialog.lng}
              onSubmit={handleSave}
              onCancel={() => setDialog(null)}
            />
          </div>
        </div>
      )}

      {/* ── Transport panel ────────────────────────────────────── */}
      {tranPanel && (
        <div style={{ ...panel, position: 'absolute', top: 0, right: 0, bottom: 0, zIndex: 1000, width: 340, overflowY: 'auto', padding: 16, boxShadow: '-4px 0 20px rgba(0,0,0,.5)' }}
          onClick={e => e.stopPropagation()}>
          <TransportPanel
            transports={transports}
            locations={locations}
            factories={factories}
            consumers={consumers}
            products={products}
            onClose={() => setTranPanel(false)}
            onChanged={loadAll}
          />
        </div>
      )}

      {/* ── Timeline ───────────────────────────────────────────── */}
      <div style={{ ...panel, position: 'absolute', bottom: 0, left: 0, right: 0, zIndex: 1000, padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 12, borderRadius: 0, borderTop: '1px solid #334155' }}>
        <button
          onClick={() => setTimeline(p => ({ ...p, playing: !p.playing }))}
          style={{ background: timeline.playing ? '#2563eb' : '#1e40af', color: 'white', border: 'none', borderRadius: 8, width: 36, height: 36, fontSize: 16, cursor: 'pointer', flexShrink: 0 }}>
          {timeline.playing ? '⏸' : '▶'}
        </button>
        <span style={{ fontSize: 12, fontWeight: 600, minWidth: 110, color: '#94a3b8' }}>{fmtDate(timeline.current)}</span>
        <input type="range" min={0} max={1000} value={Math.round(progress * 1000)}
          onChange={e => {
            const p = Number(e.target.value) / 1000;
            setTimeline(prev => ({ ...prev, current: new Date(prev.start.getTime() + p * totalMs), playing: false }));
          }}
          style={{ flex: 1, accentColor: '#3b82f6' }} />
        <div style={{ display: 'flex', gap: 4, flexShrink: 0 }}>
          {[
            { label: '1д/с', val: 1 },
            { label: '7д/с', val: 7 },
            { label: '30д/с', val: 30 },
          ].map(s => (
            <button key={s.val} onClick={() => setTimeline(p => ({ ...p, speedDays: s.val }))}
              style={{ padding: '4px 9px', fontSize: 11, borderRadius: 6, cursor: 'pointer', border: '1px solid #475569', background: timeline.speedDays === s.val ? '#2563eb' : '#1e293b', color: 'white', fontWeight: 600 }}>
              {s.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── EntityForm ─────────────────────────────────────────────────────────
function EntityForm({
  type, initial, showLocationFields, lat, lng, onSubmit, onCancel,
}: {
  type: EntityType;
  initial: Record<string, unknown> | null;
  showLocationFields: boolean;
  lat?: number; lng?: number;
  onSubmit: (d: Record<string, unknown>) => void;
  onCancel: () => void;
}) {
  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const raw = Object.fromEntries(fd.entries()) as Record<string, unknown>;
    const data: Record<string, unknown> = { ...raw };

    // Numeric fields
    ['latitude','longitude','productionCapacity','maxCapacity','pricePerLiter','maxRepairWeightTons'].forEach(k => {
      if (data[k]) data[k] = Number(data[k]);
    });
    // Booleans (checkboxes)
    ['hasPort','hasAirport','hasRailTerminal','hasRoadAccess','hasHeavyLift'].forEach(k => {
      data[k] = fd.get(k) === 'on';
    });
    onSubmit(data);
  };

  const inp: React.CSSProperties = {
    width: '100%', padding: '8px 10px', background: '#0f172a', border: '1px solid #334155',
    borderRadius: 8, color: '#e2e8f0', fontSize: 13, boxSizing: 'border-box',
  };
  const lbl: React.CSSProperties = { fontSize: 12, color: '#94a3b8', marginBottom: 4, display: 'block' };
  const field = (label: string, name: string, type = 'text', defVal?: string) => (
    <div style={{ marginBottom: 12 }}>
      <label style={lbl}>{label}</label>
      <input name={name} type={type} defaultValue={defVal ?? (initial?.[name] as string) ?? ''}
        style={inp} />
    </div>
  );
  const check = (label: string, name: string, defChecked = false) => (
    <label key={name} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, color: '#e2e8f0', cursor: 'pointer' }}>
      <input type="checkbox" name={name} defaultChecked={(initial?.[name] as boolean) ?? defChecked} style={{ accentColor: '#3b82f6' }} />
      {label}
    </label>
  );

  return (
    <form onSubmit={handleSubmit}>
      {field('Назва *', 'name')}

      {type === 'location' && (
        <>
          {field('Адреса', 'address')}
          {field('Місто', 'city')}
          {field('Країна', 'country', 'text', (initial?.country as string) ?? 'Україна')}
          {field('Широта', 'latitude', 'number', String(lat ?? initial?.latitude ?? ''))}
          {field('Довгота', 'longitude', 'number', String(lng ?? initial?.longitude ?? ''))}
          <div style={{ marginBottom: 12 }}>
            <label style={lbl}>Інфраструктура</label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
              {check('Порт', 'hasPort')}
              {check('Аеропорт', 'hasAirport')}
              {check('Залізниця', 'hasRailTerminal')}
              {check('Дорога', 'hasRoadAccess', true)}
            </div>
          </div>
        </>
      )}

      {(type === 'factory' || type === 'consumer') && (
        <>
          {field('Контактна особа', 'contactName')}
          {field('Телефон', 'contactPhone', 'tel')}
          {field('Email', 'contactEmail', 'email')}
          {field('Опис', 'description')}
        </>
      )}

      {type === 'factory' && field('Потужність (одиниць/добу)', 'productionCapacity', 'number')}

      {type === 'warehouse' && (
        <>
          {field('Макс. ємність (м³)', 'maxCapacity', 'number')}
          {field('Телефон', 'contactPhone', 'tel')}
          {field('Опис', 'description')}
        </>
      )}

      {type === 'serviceStation' && (
        <>
          <div style={{ marginBottom: 12 }}>
            <label style={lbl}>Тип</label>
            <select name="type" defaultValue={(initial?.type as string) ?? 'FUEL_STATION'} style={{ ...inp }}>
              <option value="FUEL_STATION">Заправка</option>
              <option value="REPAIR_SHOP">СТО</option>
              <option value="COMBINED">Заправка + СТО</option>
            </select>
          </div>
          {field('Типи палива (через кому)', 'fuelTypes')}
          {field('Ціна/літр (грн)', 'pricePerLiter', 'number')}
          {field('Макс. тоннаж для ремонту', 'maxRepairWeightTons', 'number')}
          {field('Телефон', 'contactPhone', 'tel')}
          <div style={{ marginBottom: 12 }}>
            {check('Є важкий підйомник', 'hasHeavyLift')}
          </div>
        </>
      )}

      <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
        <button type="submit"
          style={{ flex: 1, padding: '9px 0', background: '#2563eb', color: 'white', border: 'none', borderRadius: 8, fontWeight: 700, cursor: 'pointer', fontSize: 14 }}>
          Зберегти
        </button>
        <button type="button" onClick={onCancel}
          style={{ padding: '9px 16px', background: '#1e293b', color: '#94a3b8', border: '1px solid #334155', borderRadius: 8, cursor: 'pointer', fontSize: 14 }}>
          Скасувати
        </button>
      </div>
    </form>
  );
}

// ── TransportPanel ─────────────────────────────────────────────────────
function TransportPanel({
  transports, locations, factories, consumers, products, onClose, onChanged,
}: {
  transports: TransportData[];
  locations:  LocationData[];
  factories:  FactoryData[];
  consumers:  ConsumerData[];
  products:   ProductData[];
  onClose:    () => void;
  onChanged:  () => void;
}) {
  const [adding,        setAdding]        = useState(false);
  const [editId,        setEditId]        = useState<number | null>(null);
  const [dispatchId,    setDispatchId]    = useState<number | null>(null);
  const [addingProduct, setAddingProduct] = useState(false);
  const [newProduct,    setNewProduct]    = useState({ name: '', category: '', unit: '' });

  const ST_COLOR: Record<string, string> = {
    AVAILABLE: '#22c55e', IN_TRANSIT: '#f97316', MAINTENANCE: '#ef4444',
  };
  const ST_LABEL: Record<string, string> = {
    AVAILABLE: 'Доступний', IN_TRANSIT: 'В дорозі', MAINTENANCE: 'Тех. обслуг.',
  };

  const inp: React.CSSProperties = { width: '100%', padding: '7px 9px', background: '#0f172a', border: '1px solid #334155', borderRadius: 7, color: '#e2e8f0', fontSize: 12, boxSizing: 'border-box', marginTop: 4 };
  const lbl: React.CSSProperties = { fontSize: 11, color: '#94a3b8', display: 'block', marginTop: 8 };

  const showEditForm = adding || editId !== null;
  const editTransport = editId ? transports.find(t => t.id === editId) : null;

  const handleSaveTransport = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const data: Record<string, unknown> = Object.fromEntries(fd.entries());
    ['maxCargoWeightTons','maxCargoVolumeM3','fuelCapacityLiters','rangeKm'].forEach(k => {
      if (data[k]) data[k] = Number(data[k]);
    });
    try {
      if (editId) {
        await api.transports.update(editId, data);
      } else {
        await api.transports.create(data);
      }
      await onChanged();
      setAdding(false); setEditId(null);
    } catch (ex) { alert('Помилка: ' + (ex as Error).message); }
  };

  const handleDispatch = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const raw: Record<string, unknown> = Object.fromEntries(fd.entries());

    // numeric fields
    ['factoryId','consumerId','productId','quantity'].forEach(k => {
      if (raw[k]) raw[k] = Number(raw[k]);
    });
    // strip time → LocalDate (backend expects YYYY-MM-DD)
    ['scheduledPickup','scheduledDelivery'].forEach(k => {
      if (raw[k]) raw[k] = (raw[k] as string).slice(0, 10);
    });
    // remove empty optional strings
    Object.keys(raw).forEach(k => { if (raw[k] === '') delete raw[k]; });

    const t = transports.find(x => x.id === dispatchId)!;
    try {
      const orderNumber = `ORD-${new Date().toISOString().slice(0,10).replace(/-/g,'')}-${Math.random().toString(36).slice(2,6).toUpperCase()}`;
      await api.deliveryOrders.create({ ...raw, orderNumber, transportId: dispatchId });
      // PUT requires full transport object
      await api.transports.update(dispatchId!, {
        name: t.name, type: t.type,
        maxCargoWeightTons: t.maxCargoWeightTons,
        maxCargoVolumeM3:   t.maxCargoVolumeM3,
        fuelType:           t.fuelType,
        fuelCapacityLiters: t.fuelCapacityLiters,
        rangeKm:            t.rangeKm,
        currentLocationId:  t.currentLocation?.id,
        description:        t.description,
        status: 'IN_TRANSIT',
      });
      await onChanged();
      setDispatchId(null);
    } catch (ex) { alert('Помилка: ' + (ex as Error).message); }
  };

  // Default datetime helpers
  const nowLocal = () => {
    const d = new Date();
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
  };
  const plusDays = (n: number) => {
    const d = new Date();
    d.setDate(d.getDate() + n);
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
        <h3 style={{ margin: 0, fontSize: 15, fontWeight: 700 }}>🚛 Транспорт</h3>
        <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: 18, cursor: 'pointer' }}>✕</button>
      </div>

      {/* ── Add/Edit transport form ─── */}
      {!showEditForm && !dispatchId && (
        <button onClick={() => setAdding(true)}
          style={{ width: '100%', padding: '8px 0', background: '#1d4ed8', color: 'white', border: 'none', borderRadius: 8, fontWeight: 700, cursor: 'pointer', fontSize: 13, marginBottom: 12 }}>
          ➕ Додати транспорт
        </button>
      )}

      {showEditForm && (
        <form onSubmit={handleSaveTransport} style={{ background: '#0f172a', borderRadius: 10, padding: '12px', marginBottom: 14, border: '1px solid #334155' }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: '#94a3b8', marginBottom: 8 }}>
            {editId ? '✏️ Редагувати' : '➕ Новий транспорт'}
          </div>

          <label style={lbl}>Назва *</label>
          <input name="name" required style={inp} defaultValue={editTransport?.name ?? ''} />

          <label style={lbl}>Тип</label>
          <select name="type" style={inp} defaultValue={editTransport?.type ?? 'CAR'}>
            {[['CAR','🚛 Автомобіль'],['TRAIN','🚂 Потяг'],['SHIP','🚢 Корабель'],['AIRPLANE','✈️ Літак']].map(([v,l]) => (
              <option key={v} value={v}>{l}</option>
            ))}
          </select>

          <label style={lbl}>Вантажопідйомність (тонн)</label>
          <input name="maxCargoWeightTons" type="number" step="0.1" style={inp} defaultValue={editTransport?.maxCargoWeightTons ?? ''} />

          <label style={lbl}>Дальність (км)</label>
          <input name="rangeKm" type="number" style={inp} defaultValue={editTransport?.rangeKm ?? ''} />

          <label style={lbl}>Поточна локація</label>
          <select name="currentLocationId" style={inp} defaultValue={editTransport?.currentLocation?.id ?? ''}>
            <option value="">— не вказано —</option>
            {locations.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>

          <div style={{ display: 'flex', gap: 6, marginTop: 12 }}>
            <button type="submit" style={{ flex: 1, padding: '8px 0', background: '#2563eb', color: 'white', border: 'none', borderRadius: 7, fontWeight: 700, cursor: 'pointer', fontSize: 12 }}>Зберегти</button>
            <button type="button" onClick={() => { setAdding(false); setEditId(null); }}
              style={{ padding: '8px 12px', background: '#1e293b', color: '#94a3b8', border: '1px solid #334155', borderRadius: 7, cursor: 'pointer', fontSize: 12 }}>✕</button>
          </div>
        </form>
      )}

      {/* ── Dispatch form ─── */}
      {dispatchId && (() => {
        const t = transports.find(x => x.id === dispatchId)!;
        return (
          <form onSubmit={handleDispatch} style={{ background: '#0f172a', borderRadius: 10, padding: '12px', marginBottom: 14, border: '2px solid #f97316' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: '#f97316' }}>
                🚀 Відправити: {TRANSPORT_CFG[t.type]?.emoji} {t.name}
              </div>
              <button type="button" onClick={() => setDispatchId(null)} style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: 16, cursor: 'pointer' }}>✕</button>
            </div>

            <label style={lbl}>Фабрика (звідки) *</label>
            <select name="factoryId" required style={inp}>
              <option value="">— обрати —</option>
              {factories.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>

            <label style={lbl}>Споживач (куди) *</label>
            <select name="consumerId" required style={inp}>
              <option value="">— обрати —</option>
              {consumers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
              <label style={{ ...lbl, marginTop: 0 }}>Продукт</label>
              <button type="button"
                onClick={() => setAddingProduct(p => !p)}
                style={{ fontSize: 10, background: addingProduct ? '#334155' : '#1e293b', color: '#94a3b8', border: '1px solid #334155', borderRadius: 5, padding: '2px 7px', cursor: 'pointer' }}>
                {addingProduct ? '✕ скасувати' : '+ новий'}
              </button>
            </div>

            {addingProduct && (
              <div style={{ background: '#1e293b', borderRadius: 8, padding: '10px', marginBottom: 6, border: '1px solid #334155' }}>
                <div style={{ fontSize: 11, color: '#f97316', fontWeight: 700, marginBottom: 6 }}>Новий продукт</div>
                <input
                  type="text" placeholder="Назва *" value={newProduct.name}
                  onChange={e => setNewProduct(p => ({ ...p, name: e.target.value }))}
                  style={{ ...inp, marginTop: 0, marginBottom: 5 }} />
                <input
                  type="text" placeholder="Категорія (напр. Зерно)" value={newProduct.category}
                  onChange={e => setNewProduct(p => ({ ...p, category: e.target.value }))}
                  style={{ ...inp, marginTop: 0, marginBottom: 5 }} />
                <input
                  type="text" placeholder="Одиниця виміру (кг, шт, т)" value={newProduct.unit}
                  onChange={e => setNewProduct(p => ({ ...p, unit: e.target.value }))}
                  style={{ ...inp, marginTop: 0, marginBottom: 8 }} />
                <button type="button"
                  disabled={!newProduct.name.trim()}
                  onClick={async () => {
                    try {
                      await api.products.create({ name: newProduct.name, category: newProduct.category || undefined, unit: newProduct.unit || undefined });
                      setNewProduct({ name: '', category: '', unit: '' });
                      setAddingProduct(false);
                      await onChanged();
                    } catch (ex) { alert('Помилка: ' + (ex as Error).message); }
                  }}
                  style={{ width: '100%', padding: '6px 0', background: '#f97316', color: 'white', border: 'none', borderRadius: 6, fontWeight: 700, cursor: 'pointer', fontSize: 12, opacity: newProduct.name.trim() ? 1 : 0.5 }}>
                  Створити продукт
                </button>
              </div>
            )}

            <select name="productId" required style={inp}>
              <option value="">— обрати —</option>
              {products.map(p => <option key={p.id} value={p.id}>{p.name}{p.unit ? ` (${p.unit})` : ''}</option>)}
            </select>

            <label style={lbl}>Кількість *</label>
            <input name="quantity" type="number" min={1} required style={inp} placeholder="100" />

            <label style={lbl}>Дата забору</label>
            <input name="scheduledPickup" type="datetime-local" style={inp} defaultValue={nowLocal()} />

            <label style={lbl}>Дата доставки</label>
            <input name="scheduledDelivery" type="datetime-local" style={inp} defaultValue={plusDays(3)} />

            <label style={lbl}>Нотатки</label>
            <input name="notes" type="text" style={inp} placeholder="Додаткові інструкції..." />

            <button type="submit"
              style={{ width: '100%', marginTop: 12, padding: '9px 0', background: '#f97316', color: 'white', border: 'none', borderRadius: 7, fontWeight: 700, cursor: 'pointer', fontSize: 13 }}>
              🚀 Відправити
            </button>
          </form>
        );
      })()}

      {/* ── Transport list ─── */}
      {transports.map(t => (
        <div key={t.id} style={{ background: '#0f172a', borderRadius: 10, padding: '10px 12px', marginBottom: 8, border: dispatchId === t.id ? '1px solid #f97316' : '1px solid #1e293b' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div>
                <span style={{ fontSize: 16 }}>{TRANSPORT_CFG[t.type]?.emoji ?? '🚗'}</span>
                <span style={{ fontWeight: 700, marginLeft: 6, fontSize: 13 }}>{t.name}</span>
              </div>
              <span style={{ fontSize: 10, color: ST_COLOR[t.status], fontWeight: 700 }}>
                ● {ST_LABEL[t.status]}
              </span>
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 2 }}>
                {t.maxCargoWeightTons ? `⚖️ ${t.maxCargoWeightTons}т  ` : ''}
                {t.rangeKm ? `📏 ${t.rangeKm}км  ` : ''}
                {t.currentLocation ? `📍 ${t.currentLocation.name}` : ''}
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 3, marginLeft: 6 }}>
              {t.status !== 'IN_TRANSIT' && (
                <button
                  onClick={() => { setDispatchId(t.id); setAdding(false); setEditId(null); }}
                  style={{ background: '#7c2d12', border: '1px solid #f97316', color: '#fed7aa', borderRadius: 6, padding: '3px 7px', cursor: 'pointer', fontSize: 10, fontWeight: 700 }}>
                  🚀 Відправити
                </button>
              )}
              {t.status === 'IN_TRANSIT' && (
                <button
                  onClick={async () => {
                    await api.transports.update(t.id, {
                      name: t.name, type: t.type,
                      maxCargoWeightTons: t.maxCargoWeightTons,
                      maxCargoVolumeM3:   t.maxCargoVolumeM3,
                      fuelType:           t.fuelType,
                      fuelCapacityLiters: t.fuelCapacityLiters,
                      rangeKm:            t.rangeKm,
                      currentLocationId:  t.currentLocation?.id,
                      description:        t.description,
                      status: 'AVAILABLE',
                    });
                    onChanged();
                  }}
                  style={{ background: '#14532d', border: '1px solid #22c55e', color: '#bbf7d0', borderRadius: 6, padding: '3px 7px', cursor: 'pointer', fontSize: 10, fontWeight: 700 }}>
                  ✅ Доставлено
                </button>
              )}
              <div style={{ display: 'flex', gap: 3 }}>
                <button onClick={() => { setEditId(t.id); setAdding(false); setDispatchId(null); }}
                  style={{ background: '#1e293b', border: 'none', color: '#94a3b8', borderRadius: 6, padding: '3px 7px', cursor: 'pointer', fontSize: 11 }}>✏️</button>
                <button onClick={async () => {
                  if (!confirm('Видалити?')) return;
                  await api.transports.delete(t.id);
                  onChanged();
                }} style={{ background: '#1e293b', border: 'none', color: '#f87171', borderRadius: 6, padding: '3px 7px', cursor: 'pointer', fontSize: 11 }}>🗑️</button>
              </div>
            </div>
          </div>
        </div>
      ))}

      {transports.length === 0 && !showEditForm && (
        <div style={{ textAlign: 'center', color: '#475569', fontSize: 13, padding: '20px 0' }}>
          Немає транспортних засобів
        </div>
      )}
    </div>
  );
}
