# VYROX — Complete Smart Commerce Platform
> **Tagline:** SHOP SMART. COMPARE BETTER. LIVE BETTER.  
> **Engineering Team:** TEAM VELTRION  
> **Android Application ID:** `com.veltrion.vyrox`

---

## 1. Overview & Architecture

VYROX is a unified, high-performance smart commerce platform built from scratch. It connects a responsive web application and native Jetpack Compose Android client to a centralized Spring Boot 3 Java 21 backend, backed by real-time GPS tracking and semantic AI shopping assistant microservices.

```
                    VYROX PLATFORM
                          |
             +------------+------------+
             |                         |
          WEB APP                 ANDROID APP
     React + TypeScript      Kotlin (Jetpack Compose)
             |                         |
             +------------+------------+
                          |
                     API LAYER (Spring Boot 3 / Java 21)
                          |
        +-----------------+------------------+
        |                 |                  |
    PostgreSQL          Redis              Kafka
        |
   Search / Catalog / Orders / Users / Coins / Coupons
        |
  +-----+---------+----------+
  |               |          |
AI Service    Tracking    Notifications
Node.js/FastAPI WebSocket     Service
```

---

## 2. Technology Stack

- **Frontend Web (`apps/vyrox-web`):** React 18, TypeScript, Vite, Tailwind CSS, Lucide Icons, Leaflet OpenStreetMap, Web Speech API.
- **Mobile Android (`apps/vyrox-android`):** Native Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow, Retrofit2, OkHttp3, Navigation Compose, Coil.
- **Unified Commerce Backend (`services/vyrox-backend`):** Java 21, Spring Boot 3.3.0, Spring Security 6 (Stateless JWT), Spring Data JPA, H2 / PostgreSQL, OpenAPI 3 / Swagger UI.
- **Realtime Live Tracking Service (`services/tracking-realtime-service`):** Node.js, Express, WebSocket (`ws`) telemetry broadcasting.
- **AI Shopping Assistant Service (`services/ai-service`):** Node.js / Python FastAPI semantic catalog matching engine.
- **Infrastructure & Containerization:** Docker, Docker Compose, Kubernetes manifests, Prometheus/Grafana ready.

---

## 3. Project Structure

```
VYROX/
├── apps/
│   ├── vyrox-web/                 # React 18 + TypeScript + Vite web storefront
│   └── vyrox-android/             # Native Android Jetpack Compose app (com.veltrion.vyrox)
│       └── app/src/main/
│           ├── java/com/veltrion/vyrox/
│           │   ├── data/          # Models, ApiClient, Repositories
│           │   ├── ui/            # Compose screens, ViewModels, Theme, 4-tab bottom navigation
│           │   └── MainActivity.kt
│           └── AndroidManifest.xml
├── services/
│   ├── vyrox-backend/             # Spring Boot 3 (Java 21) unified backend
│   │   ├── src/main/java/com/veltrion/vyrox/
│   │   │   ├── config/            # SecurityConfig, OpenApiConfig, JWT
│   │   │   ├── controller/        # Auth, Products, Cart, Orders, Tracking, Coins, etc.
│   │   │   ├── model/             # JPA Entities
│   │   │   ├── repository/        # Spring Data JPA repositories
│   │   │   ├── service/           # Business logic & validations
│   │   │   └── seeder/            # Rich original seed data loader
│   │   └── pom.xml
│   ├── tracking-realtime-service/ # WebSocket live GPS tracking service (Port 8091)
│   └── ai-service/                # AI shopping assistant microservice (Port 8093)
├── infrastructure/
│   ├── docker/                    # Service Dockerfiles
│   ├── kubernetes/                # K8s deployment manifests
│   └── monitoring/                # Prometheus & Grafana configs
├── docs/
│   └── PLAY_STORE_METADATA.md     # Google Play Store listing and AAB guide
├── docker-compose.yml             # Local multi-container orchestration
├── .env.example                   # Template environment variables
└── README.md
```

---

## 4. Local Development Quickstart

### Prerequisites
- Java 21 JDK & Maven
- Node.js 18+ / 20+
- Docker & Docker Compose (Optional for container mode)

### Step 1: Start All Services via Docker Compose (Recommended)
```bash
docker compose up --build
```

### Step 2: Or Start Services Locally (Standalone Dev Mode)

1. **Start Backend (Port 8080):**
```bash
cd services/vyrox-backend
mvn spring-boot:run
```
*Swagger UI will be available at:* `http://localhost:8080/swagger-ui/index.html`

2. **Start Realtime Tracking Service (Port 8091):**
```bash
cd services/tracking-realtime-service
node server.js
```

3. **Start AI Assistant Service (Port 8093):**
```bash
cd services/ai-service
node server.js
```

4. **Start Web Frontend (Port 3000):**
```bash
cd apps/vyrox-web
npm run dev
```

---

## 5. Pre-seeded Demo Credentials

| Role | Email / Identifier | Password | Mobile | Initial Coins |
| :--- | :--- | :--- | :--- | :--- |
| **Customer** | `customer@vyrox.com` | `Customer@123` | `9876543210` | 350 Coins |
| **Admin** | `admin@vyrox.com` | `Admin@123` | `9876543213` | 100 Coins |
| **Seller** | `seller@vyrox.com` | `Seller@123` | `9876543211` | 100 Coins |
| **Delivery Rider** | `rider@vyrox.com` | `Rider@123` | `9876543212` | - |

*Development Mobile OTP:* `123456`

---

## 6. Verified URLs

- **Web Storefront:** `http://localhost:3000`
- **Top Deals Page:** `http://localhost:3000/top-deals`
- **4-Way Compare Matrix:** `http://localhost:3000/compare`
- **Live GPS Tracking Page:** `http://localhost:3000/orders/VYR-2026-90412/track`
- **Spring Boot API & OpenAPI UI:** `http://localhost:8080/swagger-ui/index.html`
- **Realtime WebSocket Server:** `ws://localhost:8091`
- **AI Shopping Assistant API:** `http://localhost:8093`

---

## 7. Android Application & Google Play Store (.aab)

The native Android application is built with **Kotlin + Jetpack Compose** and targets `com.veltrion.vyrox`.

### Bottom Navigation Tabs (Mandated Strict 4 Tabs):
1. **Home**
2. **Top Deals**
3. **Account**
4. **Cart**

### Building Testing APK:
```bash
cd apps/vyrox-android
./gradlew assembleRelease
```

### Generating Production Play Store Bundle (.aab):
```bash
cd apps/vyrox-android
./gradlew bundleRelease
```
*Output:* `apps/vyrox-android/app/build/outputs/bundle/release/app-release.aab`
