import Link from 'next/link';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <nav className="bg-slate-900 text-white p-4 flex gap-4 justify-center">
        <Link href="/dashboard/admin" className="hover:text-blue-400">Диспетчер</Link>
        <Link href="/dashboard/driver" className="hover:text-blue-400">Водій</Link>
        <Link href="/dashboard/warehouse" className="hover:text-blue-400">Склад</Link>
        <Link href="/login" className="hover:text-red-400 ml-8">Вийти</Link>
      </nav>

      <main className="flex-1 p-8">
        {children}
      </main>
    </div>
  );
}