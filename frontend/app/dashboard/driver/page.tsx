'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import type { DeliveryOrderData } from '@/types';

const STATUS_LABEL: Record<string, { label: string; color: string }> = {
  PENDING:    { label: 'Очікує',   color: '#f59e0b' },
  CONFIRMED:  { label: 'Підтверджено', color: '#3b82f6' },
  IN_TRANSIT: { label: 'В дорозі', color: '#22c55e' },
  DELIVERED:  { label: 'Доставлено', color: '#64748b' },
  CANCELLED:  { label: 'Скасовано', color: '#ef4444' },
};

function mapsUrl(lat?: number, lng?: number) {
  if (!lat || !lng) return null;
  return `https://www.google.com/maps?q=${lat},${lng}`;
}

function appleMapsUrl(lat?: number, lng?: number) {
  if (!lat || !lng) return null;
  return `maps://maps.apple.com/?ll=${lat},${lng}`;
}

function routeUrl(fromLat?: number, fromLng?: number, toLat?: number, toLng?: number) {
  if (!fromLat || !toLat) return null;
  return `https://www.google.com/maps/dir/${fromLat},${fromLng}/${toLat},${toLng}`;
}

export default function DriverDashboard() {
  const [orders, setOrders] = useState<DeliveryOrderData[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<string>('active');

  useEffect(() => {
    api.deliveryOrders.getAll()
      .then(setOrders)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered = orders.filter(o => {
    if (filter === 'active') return ['CONFIRMED', 'IN_TRANSIT', 'PENDING'].includes(o.status);
    if (filter === 'done') return ['DELIVERED', 'CANCELLED'].includes(o.status);
    return true;
  });

  const fmtDate = (s?: string) => s
    ? new Date(s).toLocaleDateString('uk-UA', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    : '—';

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">🗺️ Мої маршрути</h1>
        <p className="text-slate-500 text-sm mt-1">Список замовлень та точок доставки</p>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 mb-4">
        {[
          { key: 'active', label: 'Активні' },
          { key: 'done',   label: 'Завершені' },
          { key: 'all',    label: 'Всі' },
        ].map(f => (
          <button key={f.key} onClick={() => setFilter(f.key)}
            className={`px-4 py-1.5 rounded-full text-sm font-semibold transition-colors ${filter === f.key ? 'bg-blue-600 text-white' : 'bg-white text-slate-600 border hover:bg-slate-50'}`}>
            {f.label}
          </button>
        ))}
      </div>

      {loading && (
        <div className="text-center text-slate-400 py-12">Завантаження...</div>
      )}

      {!loading && filtered.length === 0 && (
        <div className="text-center text-slate-400 py-12 bg-white rounded-xl shadow">
          <div className="text-4xl mb-3">📭</div>
          <p>Немає замовлень у цій категорії</p>
          <p className="text-sm mt-1">Переконайтесь, що бекенд запущено на порту 8080</p>
        </div>
      )}

      <div className="flex flex-col gap-4">
        {filtered.map(order => {
          const st = STATUS_LABEL[order.status] ?? { label: order.status, color: '#64748b' };
          const fromLat = order.factory?.location?.latitude;
          const fromLng = order.factory?.location?.longitude;
          const toLat   = order.consumer?.location?.latitude;
          const toLng   = order.consumer?.location?.longitude;

          return (
            <div key={order.id} className="bg-white rounded-xl shadow p-5 border border-slate-100">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <span className="text-xs font-mono text-slate-400">#{order.orderNumber}</span>
                  <div className="font-bold text-slate-800 mt-0.5">
                    {order.transport ? `${order.transport.name}` : 'Транспорт не призначено'}
                  </div>
                </div>
                <span style={{ color: st.color }} className="text-xs font-bold bg-slate-50 px-2 py-1 rounded-full border">
                  ● {st.label}
                </span>
              </div>

              {/* Route */}
              <div className="flex items-center gap-2 text-sm text-slate-600 mb-3">
                <div className="flex flex-col items-center">
                  <span className="text-lg">🏭</span>
                  <div style={{ width: 2, height: 20, background: '#e2e8f0' }} />
                  <span className="text-lg">🏢</span>
                </div>
                <div className="flex flex-col gap-3">
                  <div>
                    <div className="font-semibold text-slate-700">{order.factory?.name ?? '—'}</div>
                    <div className="text-xs text-slate-400">{order.factory?.location?.city ?? ''} · Забір: {fmtDate(order.scheduledPickup)}</div>
                  </div>
                  <div>
                    <div className="font-semibold text-slate-700">{order.consumer?.name ?? '—'}</div>
                    <div className="text-xs text-slate-400">{order.consumer?.location?.city ?? ''} · Доставка: {fmtDate(order.scheduledDelivery)}</div>
                  </div>
                </div>
              </div>

              {/* Product info */}
              {order.product && (
                <div className="text-xs text-slate-500 mb-3 bg-slate-50 rounded-lg px-3 py-2">
                  📦 {order.product.name} · {order.quantity} {order.product.unit ?? 'од.'}
                </div>
              )}

              {/* Navigation buttons */}
              <div className="flex flex-wrap gap-2">
                {routeUrl(fromLat, fromLng, toLat, toLng) && (
                  <a href={routeUrl(fromLat, fromLng, toLat, toLng)!} target="_blank" rel="noreferrer"
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-700 rounded-lg text-xs font-semibold hover:bg-blue-100 transition-colors">
                    🗺️ Google Maps маршрут
                  </a>
                )}
                {mapsUrl(toLat, toLng) && (
                  <a href={mapsUrl(toLat, toLng)!} target="_blank" rel="noreferrer"
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold hover:bg-slate-200 transition-colors">
                    📍 Точка призначення
                  </a>
                )}
                {appleMapsUrl(toLat, toLng) && (
                  <a href={appleMapsUrl(toLat, toLng)!}
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold hover:bg-slate-200 transition-colors">
                    🍎 Apple Maps
                  </a>
                )}
              </div>

              {order.notes && (
                <div className="mt-3 text-xs text-slate-500 italic border-t pt-2">💬 {order.notes}</div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
