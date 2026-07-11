# 🛡️ DRAGONIC Guard

<div align="center">

![Banner](https://capsule-render.vercel.app/api?type=waving&color=0:050810,50:0D1B3E,100:4FC3F7&height=200&section=header&text=DRAGONIC%20GUARD&fontSize=48&fontColor=4FC3F7&fontAlignY=38&desc=Parental%20Control%20App%20for%20Android&descAlignY=58&descColor=90CAF9)

[![Build APK](https://github.com/leonorenexus/dragonic-guard/actions/workflows/build.yml/badge.svg)](https://github.com/leonorenexus/dragonic-guard/actions/workflows/build.yml)
![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-4FC3F7?style=flat-square&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7C4DFF?style=flat-square&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024-4FC3F7?style=flat-square&logo=jetpackcompose)
![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%2B%20FCM-FFCA28?style=flat-square&logo=firebase)
![License](https://img.shields.io/badge/License-Private-EF5350?style=flat-square)

**Aplikasi parental control Android buatan sendiri — lebih kuat dari Google Family Link.**  
Berbasis Device Admin + Accessibility Service + Firebase Remote Control.

</div>

---

## ✨ Fitur Utama

| Fitur | Teknologi | Keterangan |
|-------|-----------|------------|
| 🔒 **Anti-Uninstall** | Device Admin API | App tidak bisa diuninstall tanpa PIN orang tua |
| 🚫 **App Blocker** | Accessibility Service | Blokir app secara real-time saat dibuka |
| ⏱️ **Screen Time** | UsageStatsManager | Pantau pemakaian setiap aplikasi per hari |
| ⏳ **Daily Limit** | Room DB | Batasi jam pemakaian per app |
| 🔐 **Lock Screen** | Custom Activity | Numpad PIN saat anak buka app yang diblokir |
| 📡 **Remote Control** | Firebase FCM | Kunci layar HP anak dari jarak jauh |
| ☁️ **Cloud Sync** | Firebase Firestore | Aturan tersinkron otomatis ke cloud |
| 🔄 **Auto-Start** | Boot Receiver | Service aktif otomatis setelah HP restart |

---

## 🎨 UI Preview

> Glassmorphism dark theme — deep space aesthetic

```
┌─────────────────────────────────┐
│  DRAGONIC          ● PROTECTED  │
│  Guard                          │
│  ┌─────────────────────────┐    │
│  │      ◯  ◯  ◯           │    │
│  │    ◯  🛡️  ◯           │    │
│  │      ◯  ◯  ◯           │    │
│  │       245m              │    │
│  │   SCREEN TIME HARI INI  │    │
│  └─────────────────────────┘    │
│  [3 Diblokir] [5 Aturan] [12]  │
│                                 │
│  ── Status Izin ──              │
│  ✓ Device Admin    [AKTIF]      │
│  ✓ Accessibility   [AKTIF]      │
│  ✓ Usage Access    [AKTIF]      │
│                                 │
│  [🔒 KUNCI PERANGKAT SEKARANG] │
└─────────────────────────────────┘
```

---

## 🏗️ Arsitektur

```
com.dragonic.guard/
├── GuardApplication.kt         # App entry + notification channels
├── model/Models.kt             # AppRule, AppUsageRecord, RemoteCommand
├── receiver/
│   ├── BootReceiver.kt         # Auto-start saat reboot
│   └── GuardDeviceAdminReceiver.kt  # Device Admin callbacks
├── repository/
│   ├── GuardDatabase.kt        # Room DB (SQLite lokal)
│   └── GuardRepository.kt      # Single source of truth (Room + Firebase)
├── service/
│   ├── GuardAccessibilityService.kt  # Real-time app blocker
│   ├── GuardMonitorService.kt        # Screen time tracker (foreground)
│   └── GuardFCMService.kt            # Remote command receiver
├── ui/
│   ├── components/GlassComponents.kt # Reusable glass UI kit
│   ├── screens/
│   │   ├── MainActivity.kt           # Nav + bottom bar
│   │   ├── DashboardScreen.kt        # Home + stats + 3D ring
│   │   ├── AppControlScreen.kt       # Block/limit per app
│   │   ├── UsageScreen.kt            # Screen time chart
│   │   ├── SettingsScreen.kt         # PIN + Firebase info
│   │   └── LockScreenActivity.kt     # Muncul saat app diblokir
│   └── theme/
│       ├── Theme.kt                  # Dark glassmorphism palette
│       └── Typography.kt
└── viewmodel/MainViewModel.kt        # State + business logic
```

---

## ⚡ Quick Start

### 1. Clone & Setup Firebase

```bash
git clone https://github.com/leonorenexus/dragonic-guard.git
cd dragonic-guard
```

Buka [Firebase Console](https://console.firebase.google.com):
1. Buat project baru
2. Tambah Android app → package: `com.dragonic.guard`
3. Download `google-services.json`
4. Ganti file `app/google-services.json` dengan yang baru

### 2. Build via GitHub Actions (Tanpa PC)

```bash
# Encode google-services.json ke base64
base64 -w 0 app/google-services.json
```

Tambah secret di GitHub:
- **Settings → Secrets and variables → Actions**
- Name: `GOOGLE_SERVICES_JSON`
- Value: hasil base64 di atas

```bash
git add .
git commit -m "feat: initial setup"
git push origin main
# GitHub Actions akan otomatis build APK ✅
```

Download APK dari tab **Actions → dragonic-guard-debug → Artifacts**

### 3. Build Lokal (Optional)

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Setup di HP Anak

Setelah install APK, aktifkan 3 izin berikut (wajib):

| No | Izin | Cara Aktifkan |
|----|------|--------------|
| 1 | **Device Administrator** | Buka app → Dashboard → tombol "Aktifkan" |
| 2 | **Accessibility Service** | Settings → Accessibility → DRAGONIC Guard → ON |
| 3 | **Usage Access** | Settings → Apps → Special App Access → Usage Access → DRAGONIC Guard |

> ⚠️ Ketiga izin harus aktif untuk perlindungan penuh.

---

## 🔐 Default PIN

```
PIN default: 1234
```

Ganti PIN di tab **Settings** setelah install.

---

## 🛰️ Remote Control via Firebase

Struktur Firestore untuk kirim perintah ke HP anak:

```
devices/
└── {deviceId}/
    ├── fcmToken: "..."         # Auto-tersimpan saat install
    ├── lastSeen: timestamp
    ├── todayScreenTimeMinutes: 245
    ├── currentApp: "YouTube"
    ├── rules/
    │   └── {packageName}: AppRule
    └── commands/
        └── {cmdId}: { type: "LOCK_DEVICE" }   # ← tulis ini untuk kunci HP
```

**Perintah yang tersedia:**

| Command | Efek |
|---------|------|
| `LOCK_DEVICE` | Kunci layar HP anak sekarang |
| `SYNC_RULES` | Sinkron ulang aturan dari Firestore |
| `BLOCK_APP` | Blokir app via payload packageName |
| `UNBLOCK_APP` | Buka blokir app |

---

## 🔧 Tech Stack

```kotlin
// Core
Kotlin 1.9.22
Jetpack Compose BOM 2024.02.00
Android minSdk 26 (Android 8.0)

// Architecture
MVVM + StateFlow
Room 2.6.1 (SQLite lokal)
DataStore Preferences

// Firebase
Firebase Auth
Firebase Firestore
Firebase Cloud Messaging (FCM)

// Background
WorkManager 2.9.0
Foreground Service
AccessibilityService
DevicePolicyManager
```

---

## 🛡️ Keamanan

- **Anti-uninstall**: Device Admin aktif → app tidak bisa dihapus tanpa PIN
- **PIN protection**: Semua setting terkunci di balik PIN
- **Boot persistence**: Service restart otomatis setelah HP mati/nyala
- **No root needed**: Semua fitur berjalan tanpa root
- **Offline capable**: Aturan disimpan lokal di Room DB

---

## ⚠️ Keterbatasan

- Accessibility Service bisa dinonaktifkan di Settings (butuh PIN orang tua untuk bypass, tapi tidak 100% immune)
- Factory reset akan menghapus semua proteksi (hanya root/MDM yang bisa cegah ini)
- Beberapa custom ROM mungkin membatasi UsageStats

---

## 📋 Roadmap

- [ ] Lokasi real-time (GPS tracking)
- [ ] Screenshot on demand
- [ ] Report harian via email
- [ ] App parent terpisah (remote dashboard)
- [ ] Bedtime mode (jadwal otomatis kunci HP)
- [ ] Whitelist mode (hanya izinkan app tertentu)

---

## 👨‍💻 Developer

<div align="center">

**Ren Leonore** — Leonore Tech Team

[![GitHub](https://img.shields.io/badge/GitHub-leonorenexus-4FC3F7?style=flat-square&logo=github)](https://github.com/leonorenexus)
[![Website](https://img.shields.io/badge/Web-leonore.web.id-7C4DFF?style=flat-square&logo=googlechrome)](https://leonore.web.id)

*Part of the **DRAGONIC** brand ecosystem*

</div>

---

<div align="center">

![Footer](https://capsule-render.vercel.app/api?type=waving&color=0:4FC3F7,50:7C4DFF,100:050810&height=120&section=footer)

</div>
