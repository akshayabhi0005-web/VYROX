# VYROX — Free Cloud Deployment Guide

**Team:** TEAM VELTRION  
**Application:** VYROX Smart Commerce Platform (Web + Native Android + Spring Boot 3 Backend)  
**Tagline:** SHOP SMART. COMPARE BETTER. LIVE BETTER.  
**Package:** `com.veltrion.vyrox`  

---

## 1. Free Cloud Architecture Overview

The VYROX platform is architected for zero-cost deployment using industry-standard free tiers:

| Component | Free Cloud Provider | Free Tier Limits | Artifact / Output |
| :--- | :--- | :--- | :--- |
| **Database** | **Supabase PostgreSQL** | 500 MB storage, free managed PostgreSQL | Direct JDBC connection string |
| **Backend API** | **Render (Web Service)** | 750 free instance hours/month | Docker / Java 21 container (`/api/v1`) |
| **Web Frontend** | **Cloudflare Pages** | Unlimited bandwidth, 500 builds/month | Static SPA in `dist/` with `_redirects` |
| **Native Android** | **Direct Release APK / GitHub Releases** | Free unlimited releases / APK downloads | `app-release.apk` |

---

## 2. Step-by-Step Deployment Instructions

### Part A: Free Managed PostgreSQL Database Setup on Supabase

1. Sign up / log in to [Supabase](https://supabase.com).
2. Click **"New Project"**:
   - **Organization:** Select or create your personal free organization.
   - **Project Name:** `vyrox-db`
   - **Database Password:** Enter a strong password (e.g., alphanumeric with special symbols). **Save this password securely.**
   - **Region:** Choose the region closest to your target users (e.g., `ap-south-1` for Mumbai / Bengaluru).
   - **Pricing Plan:** Free ($0/month).
3. **Get Supavisor Session Pooler (IPv4) Connection Information:**
   - In Supabase, click **"Connect"** button (top right of dashboard) or navigate to **Project Settings $\rightarrow$ Database $\rightarrow$ Connection Pooler**.
   - Select **"Session pooler"** (Port **5432**, IPv4 compatible).
     > ⚠️ **CRITICAL FOR RENDER:** Render free tier uses IPv4 outbound routing. Do **NOT** use direct database host `db.xxx.supabase.co` (which uses IPv6). Use the **Session pooler** host (`aws-0-[region].pooler.supabase.com`). Do **NOT** use Transaction pooler port 6543 because Hibernate requires prepared statements.
   - **Session Pooler Host:** `aws-0-[REGION].pooler.supabase.com`
   - **Port:** `5432`
   - **Database Name:** `postgres`
   - **Username:** `postgres.[YOUR-PROJECT-REF]`
   - **JDBC URL:**
     ```
     jdbc:postgresql://aws-0-[REGION].pooler.supabase.com:5432/postgres?sslmode=require
     ```
4. **Automatic Schema & Data Initialization:**
   - The VYROX backend Spring Data JPA engine automatically initializes and updates all relational tables (`users`, `products`, `sellers`, `categories`, `orders`, `cart_items`, `wishlist_items`, `addresses`, `coupons`, `coin_wallets`, `reviews`, `tracking_logs`, etc.) on first boot with `hibernate.ddl-auto: update`.
   - The built-in `DataSeeder` runs automatically when the database is empty, seeding catalog items, admin/customer accounts, coupons, and sample 15-minute quick commerce products.

---

### Part B: Deploy Spring Boot Backend on Render

1. Sign up / log in to [Render](https://render.com).
2. Click **"New +" $\rightarrow$ "Web Service"**:
   - Connect your GitHub repository containing the VYROX project.
   - **Name:** `vyrox-backend`
   - **Root Directory:** `services/vyrox-backend`
   - **Environment:** `Docker`
   - **Plan:** `Free` ($0/month)
3. Under **Advanced / Environment Variables**, configure the database connection values using the Supabase Session Pooler:

   | Key | Value (Example / Placeholder) |
   | :--- | :--- |
   | `PORT` | `10000` *(Render sets this automatically)* |
   | `DEMO_MODE` | `true` *(Enables safe dev mock features when OAuth is unconfigured)* |
   | `DATABASE_URL` | `jdbc:postgresql://aws-0-[REGION].pooler.supabase.com:5432/postgres?sslmode=require` |
   | `DB_USERNAME` | `postgres.[YOUR-PROJECT-REF]` |
   | `DB_PASSWORD` | `YOUR_SUPABASE_DATABASE_PASSWORD` |
   | `CORS_ALLOWED_ORIGINS` | `*` *(or `https://YOUR_CLOUDFLARE_PAGES_URL.pages.dev,http://localhost:3000`)* |
   | `JWT_SECRET` | `YOUR_SECURE_RANDOM_JWT_SECRET_STRING` |
   | `GOOGLE_CLIENT_ID` | `YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com` *(Optional)* |
   | `GOOGLE_CLIENT_SECRET` | `YOUR_GOOGLE_CLIENT_SECRET` *(Optional)* |
   | `FACEBOOK_APP_ID` | `YOUR_FACEBOOK_APP_ID` *(Optional)* |
   | `FACEBOOK_APP_SECRET` | `YOUR_FACEBOOK_APP_SECRET` *(Optional)* |

4. Click **"Deploy Web Service"**.
5. Once deployment completes, your live backend URL will be:
   ```
   https://YOUR_RENDER_BACKEND_URL.onrender.com
   ```
6. Verify deployment by visiting:
   ```
   https://YOUR_RENDER_BACKEND_URL.onrender.com/api/v1/health
   ```
   Expected response:
   ```json
   {
     "status": "UP",
     "service": "vyrox-backend",
     "version": "1.0.0",
     "team": "TEAM VELTRION",
     "demoMode": true,
     "timestamp": "2026-09-03T09:15:00.000Z"
   }
   ```

---

### Part C: Deploy React Web App on Cloudflare Pages

1. Sign up / log in to [Cloudflare Dashboard](https://dash.cloudflare.com) $\rightarrow$ **Workers & Pages** $\rightarrow$ **Create Application** $\rightarrow$ **Pages** $\rightarrow$ **Connect to Git**.
2. Select your VYROX repository and configure build settings:
   - **Project Name:** `vyrox-web`
   - **Framework Preset:** `Vite`
   - **Root Directory:** `apps/vyrox-web`
   - **Build Command:** `npm run build`
   - **Build Output Directory:** `dist`
3. Under **Environment Variables (Production)**, set:

   | Variable Name | Value |
   | :--- | :--- |
   | `VITE_API_BASE_URL` | `https://YOUR_RENDER_BACKEND_URL.onrender.com/api/v1` |
   | `VITE_WS_BASE_URL` | `wss://YOUR_RENDER_BACKEND_URL.onrender.com` |
   | `VITE_GOOGLE_CLIENT_ID` | `YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com` *(Optional)* |
   | `VITE_FACEBOOK_APP_ID` | `YOUR_FACEBOOK_APP_ID` *(Optional)* |

4. Click **"Save and Deploy"**.
5. The included `public/_redirects` file (`/* /index.html 200`) automatically handles SPA client-side routing (`/login`, `/cart`, `/account`, `/wishlist`, `/top-deals`, `/checkout`, `/tracking/:orderNumber`) without 404 reload errors.
6. Your live web store will be online at:
   ```
   https://YOUR_CLOUDFLARE_PAGES_URL.pages.dev
   ```

---

### Part D: Build & Deploy Native Android Release APK

1. In `apps/vyrox-android`, define the production backend endpoint before compilation:
   ```powershell
   $env:RELEASE_API_BASE_URL="https://YOUR_RENDER_BACKEND_URL.onrender.com/api/v1/"
   ```
2. Build the signed release APK:
   ```powershell
   cd apps/vyrox-android
   .\gradlew.bat assembleRelease
   ```
3. Output APK location:
   ```
   apps/vyrox-android/app/build/outputs/apk/release/app-release.apk
   ```
4. Distribute the APK directly to users or attach it to a GitHub Release for free cloud downloading and testing.

---

## 3. Production Verification Matrix

| Check | Command / URL | Target Result |
| :--- | :--- | :--- |
| **Backend Health Check** | `GET /api/v1/health` | `HTTP 200 {"status":"UP"}` |
| **Backend Config Status** | `GET /api/v1/auth/config-status` | `HTTP 200 {"mapProvider":"osm"}` |
| **Web Production Build** | `npm run build` in `apps/vyrox-web` | `dist/` directory generated with 0 errors |
| **Android Debug Build** | `.\gradlew assembleDebug` | `app-debug.apk` built successfully |
| **Android Release Build** | `.\gradlew assembleRelease` | `app-release.apk` built successfully |
| **OpenStreetMap Engine** | Live Tracking / Address Screen | Pure Leaflet & OSM tiles rendering without Google dependencies |
