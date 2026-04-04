"use client";

import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

const ukraineBounds: L.LatLngBoundsExpression = [
  [44.2, 22.0],
  [52.4, 40.3] 
];

export default function Map() {
  return (
    <MapContainer 
      center={[48.3794, 31.1656]} 
      zoom={6} 
      minZoom={6} 
      maxBounds={ukraineBounds} 
      maxBoundsViscosity={1.0} 
      style={{ height: "100%", width: "100%", zIndex: 0 }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={[50.4501, 30.5234]}>
        <Popup>Головний склад<br/> Статус: Нормальний</Popup>
      </Marker>

      <Marker position={[49.8397, 24.0297]}>
        <Popup>Точка видачі<br/> Статус: Критичний</Popup>
      </Marker>
    </MapContainer>
  );
}