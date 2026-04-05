import type {
  LocationData, FactoryData, ConsumerData, WarehouseData,
  ServiceStationData, TransportData, DeliveryOrderData, ProductData,
  WarehouseProductData,
} from '@/types';

const BASE = 'http://localhost:8080/api';

async function req<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null as T;
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || `API error ${res.status}`);
  return json as T;
}

const get  = <T>(p: string) => req<T>(p);
const post = <T>(p: string, b: unknown) => req<T>(p, { method: 'POST', body: JSON.stringify(b) });
const put  = <T>(p: string, b: unknown) => req<T>(p, { method: 'PUT',  body: JSON.stringify(b) });
const del  = (p: string)               => req<void>(p, { method: 'DELETE' });

export const api = {
  locations: {
    getAll:  ()                                      => get<LocationData[]>('/locations'),
    create:  (d: Partial<LocationData>)              => post<LocationData>('/locations', d),
    update:  (id: number, d: Partial<LocationData>)  => put<LocationData>(`/locations/${id}`, d),
    delete:  (id: number)                            => del(`/locations/${id}`),
  },
  factories: {
    getAll:  ()                                      => get<FactoryData[]>('/factories'),
    create:  (d: Record<string, unknown>)            => post<FactoryData>('/factories', d),
    update:  (id: number, d: Record<string, unknown>)=> put<FactoryData>(`/factories/${id}`, d),
    delete:  (id: number)                            => del(`/factories/${id}`),
  },
  consumers: {
    getAll:  ()                                      => get<ConsumerData[]>('/consumers'),
    create:  (d: Record<string, unknown>)            => post<ConsumerData>('/consumers', d),
    update:  (id: number, d: Record<string, unknown>)=> put<ConsumerData>(`/consumers/${id}`, d),
    delete:  (id: number)                            => del(`/consumers/${id}`),
  },
  warehouses: {
    getAll:    ()                                      => get<WarehouseData[]>('/warehouses'),
    create:    (d: Record<string, unknown>)            => post<WarehouseData>('/warehouses', d),
    update:    (id: number, d: Record<string, unknown>)=> put<WarehouseData>(`/warehouses/${id}`, d),
    delete:    (id: number)                            => del(`/warehouses/${id}`),
    getProducts: (id: number)                          => get<WarehouseProductData[]>(`/warehouses/${id}/products`),
  },
  serviceStations: {
    getAll:  ()                                      => get<ServiceStationData[]>('/service-stations'),
    create:  (d: Record<string, unknown>)            => post<ServiceStationData>('/service-stations', d),
    update:  (id: number, d: Record<string, unknown>)=> put<ServiceStationData>(`/service-stations/${id}`, d),
    delete:  (id: number)                            => del(`/service-stations/${id}`),
  },
  transports: {
    getAll:  ()                                      => get<TransportData[]>('/transports'),
    create:  (d: Record<string, unknown>)            => post<TransportData>('/transports', d),
    update:  (id: number, d: Record<string, unknown>)=> put<TransportData>(`/transports/${id}`, d),
    delete:  (id: number)                            => del(`/transports/${id}`),
  },
  deliveryOrders: {
    getAll:  ()                                      => get<DeliveryOrderData[]>('/delivery-orders'),
    create:  (d: Record<string, unknown>)            => post<DeliveryOrderData>('/delivery-orders', d),
    updateStatus: (id: number, status: string)       => req<DeliveryOrderData>(`/delivery-orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
  },
  products: {
    getAll:  () => get<ProductData[]>('/products'),
    create:  (d: Record<string, unknown>) => post<ProductData>('/products', d),
  },
};
