"use client";

import dynamic from "next/dynamic";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const Map = dynamic(() => import("@/components/ui/Map"), {
  ssr: false,
  loading: () => (
    <div className="h-full w-full flex items-center justify-center bg-slate-100 text-slate-500 animate-pulse">
      Завантаження супутникових даних...
    </div>
  ),
});

export default function AdminDashboard() {
  return (
    <div className="flex flex-col gap-6 h-[calc(100vh-6rem)]">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Панель Диспетчера</h1>
        <p className="text-slate-500">Моніторинг складів, транспорту та критичних запитів</p>
      </div>
      <Card className="flex-1 flex flex-col shadow-sm border-slate-200 overflow-hidden">
        <CardHeader className="pb-3">
          <CardTitle>Інтерактивна карта логістики</CardTitle>
        </CardHeader>
        <CardContent className="flex-1 p-0 m-6 mt-0 border rounded-xl overflow-hidden relative">
          <div className="absolute inset-0">
            <Map />
          </div>
        </CardContent>
      </Card>
    </div>
  );
}