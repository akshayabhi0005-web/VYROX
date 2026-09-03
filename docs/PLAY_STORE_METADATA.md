# Google Play Store Release & Metadata Documentation

**Application Name:** VYROX  
**Package Name / Application ID:** `com.veltrion.vyrox`  
**Developer Organization:** Team VELTRION  
**Category:** Shopping / E-Commerce  
**Content Rating:** Everyone (3+)

---

## 1. App Store Listing Assets

### Short Description (up to 80 characters)
> Shop smart, compare specs side-by-side, and enjoy 15-minute quick delivery.

### Full Description (up to 4000 characters)
```
Welcome to VYROX — SHOP SMART. COMPARE BETTER. LIVE BETTER.

VYROX is the next-generation intelligent commerce platform developed by Team VELTRION. Designed to give you an uncompromising shopping experience with ultra-fast search, dynamic 4-way product comparisons, guaranteed authentic brand electronics, and 15-minute quick commerce darkstore delivery.

KEY FEATURES:

🛍️ 10,000+ Verified Genuine Products:
Explore top-tier flagship smartphones, pro laptops, wireless noise-cancelling headphones, smart TVs, fashion, and home appliances.

⚡ 15-Minute Quick Commerce:
Need groceries, fresh snacks, or tech accessories right now? VYROX Darkstores dispatch your order within seconds, delivered right to your doorstep.

📊 Dynamic 4-Way Spec Comparison:
Compare up to 4 laptops, smartphones, or gadgets side-by-side. See clear breakdowns of processors, RAM, cameras, displays, and battery life.

🪙 VYROX Coins & Rewards:
Earn 5% cashback coins on every purchase. Play Daily Spin & Win and redeem your coins directly at checkout (1 Coin = ₹1).

🗺️ Live GPS Radar Delivery Tracking:
Track your delivery rider on a live interactive map in real time with precise ETA countdowns and secure 4-digit Doorstep OTP verification.

🤖 VYROX AI Shopping Assistant:
Ask natural-language queries like "Best laptop for coding under ₹1,20,000" and let VYROX AI analyze specifications and budget to give you verified catalog recommendations.

🔒 Safe, Secure & Multiple Payment Options:
Pay with UPI (GPay, PhonePe, Paytm), Credit/Debit Cards, Net Banking, or Cash on Delivery with full buyer protection.

Download VYROX today and experience the future of smart commerce!
```

---

## 2. Release & AAB Generation Commands

To generate the signed Android App Bundle (`.aab`) for Google Play Store upload:

```bash
cd apps/vyrox-android
./gradlew bundleRelease
```

The generated AAB bundle will be located at:
`apps/vyrox-android/app/build/outputs/bundle/release/app-release.aab`

To generate testing APK:
```bash
./gradlew assembleRelease
# Output: apps/vyrox-android/app/build/outputs/apk/release/app-release.apk
```

---

## 3. Data Safety & Permissions Declaration
- **Internet / Network State:** Required for catalog browsing, live tracking, and secure checkout.
- **Location (Fine/Coarse):** Optional for auto-detecting delivery address and darkstore proximity.
- **Audio / Mic:** Optional for Web Speech / Android speech recognition voice search.
- **Data Encryption in Transit:** 100% TLS/HTTPS encryption across all API transactions.
