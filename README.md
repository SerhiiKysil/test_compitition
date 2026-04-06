# 🚀 Run Backend (Spring Boot) + Frontend (Next.js)

## 📦 Backend — Spring Boot

### 1. Вибір головного класу
Знайди клас з анотацією:

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

# 🌐 Run Next.js Frontend

## 📦 1. Встановлення залежностей

```bash
npm install
```

або

```bash
yarn install
```

---

## 🚀 2. Запуск dev сервера (порт 3000)

```bash
npm run dev
```

або явно задати порт:

```bash
PORT=3000 npm run dev
```

---

## 🔍 3. Відкрити в браузері

http://localhost:3000

---

## ⚙️ 4. Налаштування API (опціонально)

Створи файл `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 🏗 5. Production build

```bash
npm run build
npm start
```

---

## 🧠 Поради

* Dev сервер автоматично перезавантажується при зміні файлів
* Використовуй `NEXT_PUBLIC_*` для змінних, доступних у браузері
* Переконайся, що backend (Spring Boot) запущений на `:8080`
