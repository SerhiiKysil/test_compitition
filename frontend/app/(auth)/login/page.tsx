export default function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="p-8 bg-white rounded-xl shadow-md w-96 text-center">
        <h1 className="text-2xl font-bold mb-4">вхід у систему</h1>
        <p className="text-gray-500 mb-6">форма авторизації</p>
        <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700">
          войдітє
        </button>
      </div>
    </div>
  );
}