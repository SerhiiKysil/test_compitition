'use client';

import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import type { WarehouseData, FactoryData, ProductData, LocationData } from '@/types';

interface NewOrderForm {
  factoryId: string;
  productId: string;
  quantity: string;
  scheduledPickup: string;
  scheduledDelivery: string;
  notes: string;
}

export default function WarehouseDashboard() {
  const [warehouses,  setWarehouses]  = useState<WarehouseData[]>([]);
  const [factories,   setFactories]   = useState<FactoryData[]>([]);
  const [products,    setProducts]    = useState<ProductData[]>([]);
  const [locations,   setLocations]   = useState<LocationData[]>([]);
  const [loading,     setLoading]     = useState(true);
  const [showForm,    setShowForm]    = useState(false);
  const [selectedWh,  setSelectedWh]  = useState<number | null>(null);
  const [submitting,  setSubmitting]  = useState(false);
  const [successMsg,  setSuccessMsg]  = useState('');

  const [form, setForm] = useState<NewOrderForm>({
    factoryId: '', productId: '', quantity: '',
    scheduledPickup: '', scheduledDelivery: '', notes: '',
  });

  useEffect(() => {
    Promise.allSettled([
      api.warehouses.getAll(),
      api.factories.getAll(),
      api.products.getAll(),
      api.locations.getAll(),
    ]).then(([wh, fa, pr, lo]) => {
      if (wh.status === 'fulfilled') setWarehouses(wh.value);
      if (fa.status === 'fulfilled') setFactories(fa.value);
      if (pr.status === 'fulfilled') setProducts(pr.value);
      if (lo.status === 'fulfilled') setLocations(lo.value);
    }).finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedWh) { alert('Оберіть склад'); return; }
    setSubmitting(true);
    try {
      const warehouse = warehouses.find(w => w.id === selectedWh);
      await api.deliveryOrders.create({
        factoryId:         Number(form.factoryId),
        warehouseId:       selectedWh,
        productId:         Number(form.productId) || undefined,
        quantity:          Number(form.quantity) || undefined,
        scheduledPickup:   form.scheduledPickup || undefined,
        scheduledDelivery: form.scheduledDelivery || undefined,
        notes:             form.notes || undefined,
        consumerName:      warehouse?.name,
      });
      setSuccessMsg('✅ Замовлення створено!');
      setShowForm(false);
      setForm({ factoryId: '', productId: '', quantity: '', scheduledPickup: '', scheduledDelivery: '', notes: '' });
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (ex) {
      alert('Помилка: ' + (ex as Error).message);
    } finally {
      setSubmitting(false);
    }
  };

  const inp = 'w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-400';
  const sel = 'w-full px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-400';

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">📦 Управління складом</h1>
        <p className="text-slate-500 text-sm mt-1">Інвентар та замовлення поповнення</p>
      </div>

      {successMsg && (
        <div className="mb-4 p-3 bg-emerald-50 text-emerald-700 rounded-xl border border-emerald-200 font-semibold text-sm">
          {successMsg}
        </div>
      )}

      {loading ? (
        <div className="text-center text-slate-400 py-12">Завантаження...</div>
      ) : (
        <>
          {/* Warehouses list */}
          <div className="flex flex-col gap-4 mb-6">
            {warehouses.length === 0 && (
              <div className="text-center text-slate-400 py-10 bg-white rounded-xl shadow">
                <div className="text-4xl mb-3">🏗️</div>
                <p>Склади не знайдено</p>
                <p className="text-sm mt-1">Додайте склад через карту диспетчера</p>
              </div>
            )}
            {warehouses.map(wh => {
              const load = wh.currentLoad ?? 0;
              const max  = wh.maxCapacity ?? 100;
              const pct  = Math.min((load / Math.max(max, 1)) * 100, 100);
              const isSelected = selectedWh === wh.id;

              return (
                <div key={wh.id}
                  onClick={() => setSelectedWh(isSelected ? null : wh.id)}
                  className={`bg-white rounded-xl shadow p-5 border-2 cursor-pointer transition-all hover:shadow-md ${isSelected ? 'border-blue-500' : 'border-transparent'}`}>
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h3 className="font-bold text-slate-800">{wh.name}</h3>
                      <p className="text-xs text-slate-400 mt-0.5">{wh.location?.city ?? wh.location?.name ?? '—'}</p>
                    </div>
                    {isSelected && (
                      <span className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full font-semibold">Обрано</span>
                    )}
                  </div>

                  {wh.maxCapacity && (
                    <div className="mt-3">
                      <div className="flex justify-between text-xs text-slate-500 mb-1">
                        <span>Завантаженість</span>
                        <span>{Math.round(pct)}% ({load}/{max} м³)</span>
                      </div>
                      <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                        <div
                          style={{ width: `${pct}%`, background: pct > 85 ? '#ef4444' : pct > 60 ? '#f59e0b' : '#22c55e' }}
                          className="h-full rounded-full transition-all"
                        />
                      </div>
                    </div>
                  )}

                  {wh.contactPhone && (
                    <div className="mt-2 text-xs text-slate-500">📞 {wh.contactPhone}</div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Urgent request button */}
          <div className="bg-white rounded-xl shadow p-5 border border-slate-100">
            <div className="flex justify-between items-center mb-4">
              <div>
                <h2 className="text-lg font-bold text-red-600">🚨 Терміновий запит</h2>
                <p className="text-xs text-slate-500 mt-0.5">Замовити поповнення для обраного складу</p>
              </div>
              <button onClick={() => setShowForm(p => !p)}
                className="px-4 py-2 bg-red-600 text-white rounded-lg font-semibold text-sm hover:bg-red-700 transition-colors">
                {showForm ? 'Згорнути' : '+ Створити'}
              </button>
            </div>

            {showForm && (
              <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                {!selectedWh && (
                  <div className="p-3 bg-amber-50 text-amber-700 rounded-lg text-sm border border-amber-200">
                    ⚠️ Виберіть склад зі списку вище
                  </div>
                )}

                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1">Фабрика-постачальник *</label>
                  <select required className={sel} value={form.factoryId} onChange={e => setForm(p => ({ ...p, factoryId: e.target.value }))}>
                    <option value="">— обрати —</option>
                    {factories.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
                  </select>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">Продукт</label>
                    <select className={sel} value={form.productId} onChange={e => setForm(p => ({ ...p, productId: e.target.value }))}>
                      <option value="">— обрати —</option>
                      {products.map(pr => <option key={pr.id} value={pr.id}>{pr.name}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">Кількість</label>
                    <input type="number" min={1} className={inp} placeholder="100"
                      value={form.quantity} onChange={e => setForm(p => ({ ...p, quantity: e.target.value }))} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">Дата забору</label>
                    <input type="datetime-local" className={inp}
                      value={form.scheduledPickup} onChange={e => setForm(p => ({ ...p, scheduledPickup: e.target.value }))} />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-600 mb-1">Дата доставки</label>
                    <input type="datetime-local" className={inp}
                      value={form.scheduledDelivery} onChange={e => setForm(p => ({ ...p, scheduledDelivery: e.target.value }))} />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-600 mb-1">Примітки</label>
                  <textarea className={`${inp} resize-none`} rows={2} placeholder="Додаткові інструкції..."
                    value={form.notes} onChange={e => setForm(p => ({ ...p, notes: e.target.value }))} />
                </div>

                <button type="submit" disabled={submitting || !selectedWh}
                  className="w-full py-2.5 bg-red-600 text-white rounded-lg font-bold text-sm hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                  {submitting ? 'Збереження...' : '🚨 Надіслати запит'}
                </button>
              </form>
            )}
          </div>

          {/* Stats summary */}
          {warehouses.length > 0 && (
            <div className="grid grid-cols-3 gap-4 mt-6">
              {[
                { label: 'Всього складів', value: warehouses.length, icon: '🏭' },
                { label: 'Постачальників', value: factories.length, icon: '🏗️' },
                { label: 'Продуктів', value: products.length, icon: '📦' },
              ].map(s => (
                <div key={s.label} className="bg-white rounded-xl shadow p-4 text-center">
                  <div className="text-2xl mb-1">{s.icon}</div>
                  <div className="text-2xl font-bold text-slate-800">{s.value}</div>
                  <div className="text-xs text-slate-500">{s.label}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
