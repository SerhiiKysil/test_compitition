'use client';

import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="p-8 bg-white rounded-xl shadow-md w-96 text-center">
        <h1 className="text-2xl font-bold mb-2">🚚 LogiUA</h1>
        <p className="text-gray-500 mb-6 text-sm">Система управління логістикою</p>
        <div className="flex flex-col gap-3">
          <button
            onClick={() => router.push('/dashboard/admin')}
            className="w-full bg-blue-600 text-white py-2.5 rounded-lg hover:bg-blue-700 font-semibold"
          >
            Диспетчер / Адміністратор
          </button>
          <button
            onClick={() => router.push('/dashboard/driver')}
            className="w-full bg-emerald-600 text-white py-2.5 rounded-lg hover:bg-emerald-700 font-semibold"
          >
            Водій / Перевізник
          </button>
          <button
            onClick={() => router.push('/dashboard/warehouse')}
            className="w-full bg-amber-600 text-white py-2.5 rounded-lg hover:bg-amber-700 font-semibold"
          >
            Менеджер складу
          </button>
        </div>
      </div>
    </div>
  );
}
