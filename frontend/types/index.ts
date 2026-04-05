export interface LocationData {
  id: number;
  name: string;
  address?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  hasPort: boolean;
  hasAirport: boolean;
  hasRailTerminal: boolean;
  hasRoadAccess: boolean;
}

export interface FactoryData {
  id: number;
  name: string;
  location?: LocationData;
  description?: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
  productionCapacity?: number;
}

export interface ConsumerData {
  id: number;
  name: string;
  location?: LocationData;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
  description?: string;
}

export interface WarehouseData {
  id: number;
  name: string;
  location?: LocationData;
  maxCapacity?: number;
  currentLoad?: number;
  description?: string;
  contactPhone?: string;
}

export interface WarehouseProductData {
  id: number;
  warehouse?: WarehouseData;
  product?: ProductData;
  quantity?: number;
}

export interface ServiceStationData {
  id: number;
  name: string;
  location?: LocationData;
  type: 'FUEL_STATION' | 'REPAIR_SHOP' | 'COMBINED';
  fuelTypes?: string;
  pricePerLiter?: number;
  hasHeavyLift?: boolean;
  maxRepairWeightTons?: number;
  description?: string;
  contactPhone?: string;
}

export interface TransportData {
  id: number;
  name: string;
  type: 'CAR' | 'TRAIN' | 'SHIP' | 'AIRPLANE';
  maxCargoWeightTons?: number;
  maxCargoVolumeM3?: number;
  fuelType?: string;
  fuelCapacityLiters?: number;
  rangeKm?: number;
  status: 'AVAILABLE' | 'IN_TRANSIT' | 'MAINTENANCE';
  currentLocation?: LocationData;
  description?: string;
}

export interface ProductData {
  id: number;
  name: string;
  category?: string;
  weightPerUnit?: number;
  unit?: string;
}

export interface DeliveryOrderData {
  id: number;
  orderNumber: string;
  factory?: FactoryData;
  consumer?: ConsumerData;
  warehouse?: WarehouseData;
  transport?: TransportData;
  product?: ProductData;
  quantity?: number;
  status: 'PENDING' | 'CONFIRMED' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  scheduledPickup?: string;
  scheduledDelivery?: string;
  actualPickup?: string;
  actualDelivery?: string;
  notes?: string;
  createdAt?: string;
}

export type EntityType = 'location' | 'factory' | 'consumer' | 'warehouse' | 'serviceStation';
