# TECHNICAL PROJECT HANDOVER DOCUMENTATION
## TIKARA — Event & Ticket Management Platform
### Panduan Komprehensif Serah Terima Teknis & Operasional untuk Developer Baru

---

| Metadata Dokumen | Rincian Teknis |
| :--- | :--- |
| **Nama Proyek** | **TIKARA — Event & Ticket Management Platform** |
| **Repositori Utama** | `tiketkonser-fullstack` |
| **Tipe Dokumen** | *Engineering & Architecture Project Handover Document* |
| **Versi Dokumen** | **2.0.0 (Production-Ready Handover Edition)** |
| **Target Pembaca** | Lead Software Engineer, Backend/Frontend Developer Baru, DevOps Engineer, Technical Stakeholders |
| **Klasifikasi Kerahasiaan** | Internal Engineering Documentation |
| **Tanggal Handover** | September 2026 |
| **Prinsip Status** | 🟢 **IMPLEMENTED** &bull; 🟡 **PARTIALLY IMPLEMENTED** &bull; 🔵 **PLANNED** &bull; 🔴 **NOT IMPLEMENTED** |

---

## DAFTAR ISI

1. [Document Purpose & Metadata](#1-document-purpose)
2. [Project Overview](#2-project-overview)
3. [Business Context](#3-business-context)
4. [Benchmark Reference: Loket.com & TIX.ID](#4-benchmark-reference-loketcom--tixid)
5. [System Architecture](#5-system-architecture)
6. [Repository Structure](#6-repository-structure)
7. [Microservices Overview](#7-microservices-overview)
8. [Detailed Service Documentation (9 Microservices)](#8-detailed-service-documentation)
9. [Internal Code Architecture](#9-internal-code-architecture)
10. [Entity & Domain Model (17 Entitas Riil)](#10-entity--domain-model)
11. [Role & Access Control (RBAC)](#11-role--access-control)
12. [System Flow (End-to-End)](#12-system-flow)
13. [API Overview (Daftar Lengkap 96 Endpoint)](#13-api-overview)
14. [Database & Data Ownership](#14-database--data-ownership)
15. [Technology Stack](#15-technology-stack)
16. [Current Implementation Status](#16-current-implementation-status)
17. [What Works](#17-what-works)
18. [What Is Incomplete](#18-what-is-incomplete)
19. [What Is Planned](#19-what-is-planned)
20. [Known Issues & Risks](#20-known-issues--risks)
21. [Technical Debt](#21-technical-debt)
22. [Recommended Next Steps](#22-recommended-next-steps)
23. [Development Roadmap](#23-development-roadmap)
24. [Developer Takeover Guide (Panduan Onboarding)](#24-developer-takeover-guide)
25. [Handover Checklist](#25-handover-checklist)
26. [Appendix: Ports, Envs & Folder Tree](#26-appendix)

---

## 1. DOCUMENT PURPOSE

Dokumen ini disusun khusus sebagai **Panduan Serah Terima Teknis (Technical Project Handover Documentation)** resmi untuk proyek **TIKARA**. 

Tujuan utama dokumen ini adalah memberikan transfer pengetahuan (*knowledge transfer*) yang komprehensif, jujur, objektif, dan terperinci kepada pengembang perangkat lunak (*software engineer*), tim teknis baru, maupun *stakeholder* yang akan mengambil alih, memelihara, dan melanjutkan pengembangan sistem TIKARA.

Dokumen ini dirancang sedemikian rupa sehingga pengembang yang belum pernah melihat repositori `tiketkonser-fullstack` sebelumnya dapat langsung memahami:
1. Batasan masalah bisnis dan arsitektur perangkat lunak yang dipilih.
2. Kondisi riil setiap microservice (mana yang sudah siap produksi, mana yang baru separuh jalan, dan mana yang masih berupa rencana).
3. Struktur basis data, model entitas, dan logika keamanan token JWT.
4. Letak kode sumber, tanggung jawab tiap folder layer, dan dependensi antar-layanan.
5. Langkah demi langkah yang harus dieksekusi pada hari pertama pengambilan alih proyek (*Day-1 Onboarding*).

---

## 2. PROJECT OVERVIEW

### 2.1. Apa itu TIKARA?
**TIKARA** adalah platform ekosistem digital terpadu untuk penemuan acara konser musik, pengelolaan promotor, penjualan tiket berjenjang (*tiered ticketing*), transaksi pembayaran aman, dan validasi tiket digital di pintu masuk (*gate check-in*).

Nama TIKARA merepresentasikan komitmen untuk menghadirkan infrastruktur tiket pertunjukan musik di Indonesia yang transparan, bebas dari manipulasi calo (*scalper-proof*), adil dalam penanganan antrean (*fair queueing*), serta berstandar kenyamanan pengguna (*user-friendly*) tinggi setara dengan platform industri terkemuka.

### 2.2. Tiga Aktor Utama Sistem
1. **PEMBELI (Customer / Penikmat Konser)**:
   Mengeksplorasi konser musik berdasarkan genre/kota, memilih kategori tiket (VIP, Festival, Regular), melakukan transaksi pembayaran berbatas waktu, menyimpan e-tiket digital pada dompet pribadi, dan memindai QR code di lokasi acara.
2. **PENYELENGGARA (Event Organizer / Promotor)**:
   Pihak penyedia konser yang mengelola seluruh siklus acara: mendaftarkan jadwal dan venue konser, mengunggah materi promosi/poster, mengatur tier harga tiket dan kapasitas kuota, serta memantau analitik omzet penjualan secara mandiri.
3. **ADMIN (Platform Administrator & Operator)**:
   Pengawas ekosistem platform yang memoderasi kelayakan konser promotor, memantau integritas transaksi keuangan, menelusuri log audit aktivitas sistem, dan mengelola kepatuhan pengguna.

---

## 3. BUSINESS CONTEXT

### 3.1. Permasalahan Nyata Industri Tiket Konser
Industri pertunjukan musik modern menghadapi lonjakan permintaan masal yang sangat ekstrim pada waktu tertentu (*ticket war*). Pada platform konvensional yang dibangun secara monolitik, fenomena ini menimbulkan kerentanan fatal:
* **Server Downtime**: Seluruh sistem lumpuh seketika saat puluhan ribu penonton melakukan *refresh* halaman secara serentak.
* **Overselling & Race Condition**: Kuota tiket tersisa 1 berhasil dibayar oleh 3 pengguna berbeda akibat kegagalan isolasi transaksi basis data.
* **Penipuan Tiket Fisik / PDF Statis**: E-tiket mudah digandakan dan dijual berulang kali oleh calo di luar venue acara.
* **Ketiadaan Otonomi Promotor**: Penyelenggara acara tidak memiliki akses langsung terhadap data penjualan real-time dan konfigurasi kuota dinamis.

### 3.2. Posisi TIKARA: Event & Ticket Management Ecosystem
TIKARA tidak diposisikan sekadar sebagai "situs web e-commerce sederhana tempat membeli barang", melainkan sebagai **Event Ticketing Ecosystem** yang mengintegrasikan:
* Manajemen Pengguna & RBAC terisolasi (`user_service`).
* Manajemen Konser & Moderasi (`event_service`).
* Manajemen Inventaris Kuota & Penerbitan Tiket Digital (`ticket_service`).
* Jalur Transaksi Keuangan & Batas Waktu Bayar (`order_service`).
* Skema Diskon & Kupon Promotor (`promo_service`).
* Pusat Notifikasi Transaksi In-App (`notification_service`).
* Validasi Tiket Sekali Pakai di Gerbang Konser (`checkin_service`).
* Ruang Antrean Virtual Penyangga Trafik Masal (`queue_service`).
* Pengawasan Kepatuhan & Audit Log Eksekutif (`admin_service`).

---

## 4. BENCHMARK REFERENCE: LOKET.COM & TIX.ID

Untuk memastikan sistem memiliki daya saing tinggi dan diterima dengan baik oleh pasar pengguna Indonesia, TIKARA mengkaji dua platform industri terdepan sebagai tolak ukur konseptual:
1. **Loket.com (Loket for Business & Consumer)**: Sebagai tolok ukur alur operasional promotor mandiri, sistem holding time pembayaran, dan manajemen tiering tiket festival.
2. **TIX.ID**: Sebagai tolok ukur pengalaman pengguna mobile yang ringkas, intuitif, cepat (*frictionless*), dan informatif dalam proses penemuan acara.

### 4.1. Tabel Komparasi Konsep & Implementasi Nyata pada TIKARA

| Dimensi Platform Ticketing | Standar Industri (Loket.com / TIX.ID) | Implementasi Konsep di TIKARA | Status Implementasi |
| :--- | :--- | :--- | :---: |
| **Event Discovery** | Pencarian instan, filter kota, genre, sorting harga/waktu, banner promosi. | Halaman `index.html` dengan dynamic genre loader, filter kota, dan multi-keyword specification. | 🟢 IMPLEMENTED |
| **Event Management** | Promotor mendaftarkan konser, mengunggah poster, dan memantau analitik. | `create-event.html`, `organizer-dashboard.html`, dan `my-events.html` di `event_service`. | 🟢 IMPLEMENTED |
| **Tiering & Quota** | Multi-kategori tiket (VIP, Early Bird, CAT) dengan kuota dan batas waktu jual terpisah. | `KategoriTiket` pada `ticket_service` dengan atribut kuota, terjual, direservasi, dan status ketersediaan. | 🟢 IMPLEMENTED |
| **Ticket Holding Time**| Kuota dikunci sementara selama 15 menit saat pembeli berada di tahap pembayaran. | Entitas `Pemesanan` dengan atribut `tenggatPembayaran` dan status transisi `PENDING` -> `PAID`/`EXPIRED`. | 🟡 PARTIALLY IMPLEMENTED |
| **Virtual Waiting Room**| Antrean giliran akses otomatis saat konser memiliki trafik tinggi (*war tiket*). | Modul `queue_service` (`AntreanPembeli`) dan halaman antrean `waiting-room.html`. | 🟡 PARTIALLY IMPLEMENTED |
| **E-Ticket QR Validation**| Kode QR dinamis sekali pakai yang diverifikasi di gerbang masuk (*gate scanner*). | Modul `checkin_service` (`RiwayatCheckin`) dan entitas `Tiket` dengan status `ACTIVE` -> `USED`. | 🟡 PARTIALLY IMPLEMENTED |
| **Promo & Voucher** | Kupon diskon dengan persentase potongan, batas maksimal, dan kuota pemakaian. | Modul `promo_service` (`KodePromo` & `PenggunaanPromo`) berbasis batas masa berlaku. | 🟡 PARTIALLY IMPLEMENTED |
| **Administrative Control**| Supervisi konser, penanganan sengketa, dan log audit kepatuhan platform. | Modul `admin_service` (`AuditLog` & `LaporanPenjualan`) pada `admin-dashboard.html`. | 🟢 IMPLEMENTED |

---

## 5. SYSTEM ARCHITECTURE

Sistem TIKARA menerapkan **Arsitektur Microservices Terdistribusi** berbasis *Database-per-Service Pattern*. Setiap domain fungsional memiliki proses komputasi server dan basis data relasional PostgreSQL mandiri.

```
                               ┌─────────────────────────────┐
                               │        USER CLIENT          │
                               │  Desktop / Tablet / Mobile  │
                               └──────────────┬──────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      │    Universal Client Auth Engine (script.js)   │
                      │       JSON Web Token (JWT) Propagation        │
                      └───────┬───────────────────────────────┬───────┘
                              │                               │
         ┌────────────────────┼───────────────────────────────┼────────────────────┐
         │                    │                               │                    │
         ▼                    ▼                               ▼                    ▼
┌──────────────────┐ ┌──────────────────┐           ┌──────────────────┐ ┌──────────────────┐
│   USER SERVICE   │ │  EVENT SERVICE   │           │  TICKET SERVICE  │ │  ORDER SERVICE   │
│    (Port 8081)   │ │   (Port 8082)    │           │   (Port 8083)    │ │   (Port 8084)    │
│  db_user_service │ │ db_event_service │           │ db_ticket_service│ │ db_order_service │
└──────────────────┘ └──────────────────┘           └──────────────────┘ └────────┬─────────┘
         │                    │                               │                    │
         │                    │                               │       ┌────────────┴────────────┐
         ▼                    ▼                               ▼       ▼                         ▼
┌──────────────────┐ ┌──────────────────┐           ┌──────────────────┐ ┌──────────────────────────────┐
│  QUEUE SERVICE   │ │  ADMIN SERVICE   │           │ CHECK-IN SERVICE │ │   PROMO & NOTIFICATION SVC   │
│   (Port 8085)    │ │   (Port 8089)    │           │   (Port 8087)    │ │    (Port 8088 & Port 8086)   │
│ db_queue_service │ │ db_admin_service │           │db_checkin_service│ │ db_promo / db_notification   │
└──────────────────┘ └──────────────────┘           └──────────────────┘ └──────────────────────────────┘
```

### 5.1. Pemetaan Port Jaringan Microservice
1. **`user_service`**: Port **8081** (Identitas, Profil, Otentikasi, RBAC)
2. **`event_service`**: Port **8082** (Katalog Konser, Detail Acara, Portal Promotor)
3. **`ticket_service`**: Port **8083** (Kategori Tiket, Kuota Kapasitas, Inventaris)
4. **`order_service`**: Port **8084** (Checkout, Pemesanan, Transaksi, Riwayat Pembelian)
5. **`queue_service`**: Port **8085** (Buffer Antrean Virtual War Tiket)
6. **`notification_service`**: Port **8086** (Notifikasi Transaksi In-App)
7. **`checkin_service`**: Port **8087** (Pindai Tiket di Gate Konser)
8. **`promo_service`**: Port **8088** (Manajemen Kupon Diskon & Voucher)
9. **`admin_service`**: Port **8089** (Audit Log Forensik & Dasbor Pengawas)

---

## 6. REPOSITORY STRUCTURE

Repositori `tiketkonser-fullstack` diatur sebagai *multi-module workspace* dengan pembagian direktori tingkat atas sebagai berikut:

```text
tiketkonser-fullstack
│
├── .vscode                      # Konfigurasi editor Visual Studio Code
├── admin_service                # Microservice pengawasan & audit platform (Port 8089)
├── checkin_service              # Microservice validasi tiket di gate acara (Port 8087)
├── event_service                # Microservice katalog konser & portal promotor (Port 8082)
├── notification_service         # Microservice pesan & pemberitahuan transaksi (Port 8086)
├── order_service                # Microservice transaksi, pemesanan, & riwayat (Port 8084)
├── promo_service                # Microservice kode diskon & kupon voucher (Port 8088)
├── queue_service                # Microservice ruang antrean virtual war tiket (Port 8085)
├── ticket_service               # Microservice kategori tier tiket & inventaris (Port 8083)
├── user_service                 # Microservice identitas, autentikasi, & RBAC (Port 8081)
│
└── uploads                      # Media statis hasil unggahan pengguna
    └── avatars                  # Direktori berkas foto profil pengguna
```

---

## 7. MICROSERVICES OVERVIEW

Berikut adalah matriks ringkasan 9 microservice yang ada di repositori TIKARA:

| Nama Layanan | Port | Basis Data Default | Entitas Utama | Status Kesiapan |
| :--- | :---: | :--- | :--- | :---: |
| **`user_service`** | 8081 | `db_user_service` | `Pengguna`, `ProfilPengguna`, `ResetPasswordOtp` | 🟢 PRODUCTION-READY |
| **`event_service`** | 8082 | `db_event_service` | `Acara`, `Kategori`, `Ulasan` | 🟢 PRODUCTION-READY |
| **`ticket_service`**| 8083 | `db_ticket_service`| `KategoriTiket`, `Tiket` | 🟡 BACKEND READY (UI FINALIZING)|
| **`order_service`** | 8084 | `db_order_service` | `Pemesanan`, `DetailPemesanan` | 🟡 BACKEND READY (UI FINALIZING)|
| **`queue_service`** | 8085 | `db_queue_service` | `AntreanPembeli` | 🟡 FUNCTIONAL BACKEND |
| **`notification_service`**| 8086 | `db_notification_service`| `Notifikasi` | 🟡 FUNCTIONAL BACKEND |
| **`checkin_service`**| 8087| `db_checkin_service`| `RiwayatCheckin` | 🟡 FUNCTIONAL BACKEND |
| **`promo_service`** | 8088 | `db_promo_service` | `KodePromo`, `PenggunaanPromo` | 🟡 FUNCTIONAL BACKEND |
| **`admin_service`** | 8089 | `db_admin_service` | `AuditLog`, `LaporanPenjualan` | 🟢 PRODUCTION-READY |

---


## 8. DETAILED SERVICE DOCUMENTATION (9 MICROSERVICES)

Setiap microservice dianalisis secara mendalam menggunakan format dokumentasi teknis standar serah terima:

---

### 8.1. USER SERVICE (`/user_service`)
* **Tujuan Utama**: Pusat keabsahan identitas tunggal (*Single Source of Truth for Identity*), penerbitan token akses, profil pengguna, dan otorisasi peran.
* **Lokasi Direktori**: `/user_service` (Port: **8081**)
* **Domain Responsibility**:
  * Pendaftaran akun publik untuk peran `PEMBELI` dan `PENYELENGGARA`.
  * Kebijakan penutupan registrasi publik untuk `ADMIN` demi integritas platform.
  * Otentikasi login dengan verifikasi kata sandi hashing **BCrypt**.
  * Penerbitan token **JWT** (*JSON Web Token*) berbasis algoritma `HMAC-SHA256`.
  * Pembaruan data diri pengguna, pengunggahan foto avatar, dan penggantian password.
  * Alur pemulihan akun (*Forgot Password*) menggunakan kode OTP 6-digit via email.
* **Folder Structure**:
  * `src/main/java/com/tiketkonser/user_service/` (`config`, `controller`, `entity`, `handler`, `payload/req`, `payload/res`, `repository`, `security`, `service`, `utility`).
  * `src/main/resources/static/`: `login.html`, `register.html`, `profile.html`, `forgot-password.html`, `verify-otp.html`, `reset-password.html`, `css/style.css`, `js/script.js`.
* **Entity**: `Pengguna`, `ProfilPengguna`, `ResetPasswordOtp`.
* **Controller / API**:
  * `AuthController`: `/api/auth/register`, `/api/auth/login`, `/api/auth/forgot-password`, `/api/auth/verify-otp`, `/api/auth/reset-password`.
  * `PenggunaController`: `/api/users/profile` (GET, PUT), `/api/users/change-password` (PUT), `/api/users/upload-avatar` (POST).
  * `WebViewController`: Melayani rendering berkas halaman HTML statis.
* **Business Logic (`service/impl`)**:
  * `AuthServiceImpl`: Validasi keunikan email, enkripsi kata sandi, pembuatan token JWT, dan validasi masa aktif OTP.
  * `PenggunaServiceImpl`: Manajemen profil, enkapsulasi mutasi entitas, dan penyimpanan file foto avatar.
* **Security Implementation**:
  * `JwtAuthenticationFilter` mencegat setiap request terproteksi.
  * `SecurityConfig` mengatur endpoint publik (`/api/auth/**`, halaman HTML, CSS, JS) dan memproteksi endpoint pengguna (`/api/users/**`).
* **Dependencies**: Berdiri sendiri (*Zero inbound dependencies*). Menjadi penyedia token JWT untuk seluruh microservice lainnya.
* **Status Implementasi**: 🟢 **IMPLEMENTED (PRODUCTION-READY)**
* **Known Issues**: Pengiriman email OTP saat ini mengandalkan konfigurasi SMTP standar; pastikan kredensial email aktif di `application.properties`.
* **Next Steps bagi Developer Baru**: Tambahkan proteksi rate-limiting pada endpoint login untuk mencegah serangan brute force dan tambahkan fitur Refresh Token berputar.

---

### 8.2. EVENT SERVICE (`/event_service`)
* **Tujuan Utama**: Mengelola seluruh siklus hidup informasi konser musik, pencarian katalog publik, dan portal manajemen promotor.
* **Lokasi Direktori**: `/event_service` (Port: **8082**)
* **Domain Responsibility**:
  * Pencarian konser publik dengan penyaringan multi-parameter (kata kunci, kota, genre musik, dan waktu).
  * Menampilkan informasi detail konser, susunan artis, lokasi gedung, dan ketersediaan tiket.
  * Portal manajemen acara bagi promotor (`create-event.html`, `organizer-dashboard.html`, `my-events.html`).
  * Pengunggahan materi visual (poster konser dan flyer PDF).
  * Sistem rating dan ulasan konser pasca-acara.
  * Moderasi status kelayakan konser oleh Administrator.
* **Folder Structure**:
  * `src/main/java/com/tiketkonser/event_service/` (`config`, `controller`, `entity`, `handler`, `payload`, `repository`, `security`, `service`, `utility`).
  * `src/main/resources/static/`: `index.html`, `event-detail.html`, `create-event.html`, `organizer-dashboard.html`, `my-events.html`, `css/style.css`, `js/config.js`, `js/script.js`.
* **Entity**: `Acara`, `Kategori`, `Ulasan`.
* **Controller / API**:
  * `AcaraController`: `/api/acara` (GET, POST), `/api/acara/{id}` (GET, PUT, DELETE), `/api/acara/{id}/upload-poster` (POST), `/api/acara/{id}/upload-flyer` (POST), `/api/acara/{id}/moderate` (POST), `/api/acara/my-events` (GET), `/api/acara/organizer/analytics` (GET).
  * `KategoriController`: `/api/kategori` (GET, POST, PUT, DELETE).
  * `UlasanController`: `/api/ulasan` (GET, POST).
* **Business Logic (`service/impl`)**:
  * `AcaraServiceImpl`: Menggunakan `AcaraSpecification` untuk pencarian dinamis, validasi hak milik promotor (`validateOwnershipOrAdmin`), dan perhitungan analitik penjualan.
  * `KategoriServiceImpl`: Manajemen genre musik konser.
  * `UlasanServiceImpl`: Validasi ulasan penonton.
* **Security Implementation**:
  * Endpoint publik GET diizinkan bebas untuk katalog penonton. Endpoint mutasi data (POST, PUT, DELETE) mewajibkan role `PENYELENGGARA` atau `ADMIN`.
* **Dependencies**: Membaca klaim `userId` dari JWT `user_service`; mendaftarkan tier tiket ke `ticket_service`.
* **Status Implementasi**: 🟢 **IMPLEMENTED (PRODUCTION-READY)**
* **Known Issues**: Berkas poster disimpan pada direktori lokal; periksa izin tulis (*write permission*) pada server produksi.
* **Next Steps bagi Developer Baru**: Lengkapi migrasi penyimpanan gambar ke Cloud Object Storage (S3) dan integrasikan denah tempat duduk interaktif (*interactive seating chart*).

---

### 8.3. TICKET SERVICE (`/ticket_service`)
* **Tujuan Utama**: Tata kelola kategori tiket berjenjang, pengendalian kuota kapasitas, reservasi stok, dan penerbitan e-tiket unik penonton.
* **Lokasi Direktori**: `/ticket_service` (Port: **8083**)
* **Domain Responsibility**:
  * Pembuatan dan penyesuaian kategori tiket (VIP, Festival, Regular, CAT).
  * Pengendalian kuota: pencatatan kuota total (`kuota`), tiket terjual (`terjual`), dan tiket yang sedang dipesan sementara (`direservasi`).
  * Transisi status tiket: `AVAILABLE`, `SOLD_OUT`, `ENDED`.
  * Pencetakan entitas `Tiket` dengan kode string token QR unik untuk setiap tiket yang dibayar sah.
  * Halaman pemilihan tiket penonton (`pilih-tiket.html`) dan dompet e-tiket penonton (`my-tickets.html`).
* **Folder Structure**:
  * `src/main/java/com/tiketkonser/ticket_service/` (`config`, `controller`, `entity`, `handler`, `payload`, `repository`, `security`, `service`, `utility`).
  * `src/main/resources/static/`: `pilih-tiket.html`, `my-tickets.html`.
* **Entity**: `KategoriTiket`, `Tiket`.
* **Controller / API**:
  * `KategoriTiketController`: `/api/kategori-tiket` (POST, GET, PUT), `/api/kategori-tiket/acara/{acaraId}` (GET).
  * `TiketController`: `/api/tiket/my-tickets` (GET), `/api/tiket/{id}` (GET), `/api/tiket/issue` (POST).
  * `InventoryController`: `/api/inventory/hold` (POST), `/api/inventory/release` (POST).
* **Business Logic (`service/impl`)**:
  * `KategoriTiketServiceImpl`: Operasi CRUD tier tiket dan validasi masa penjualan.
  * `TiketServiceImpl`: Logika reservasi stok sementara dan penerbitan tiket ber-QR code.
* **Security Implementation**: Menggunakan `JwtAuthenticationFilter` untuk memverifikasi kepemilikan tiket pembeli dan hak akses promotor.
* **Dependencies**: Bergantung pada `acaraId` dari `event_service`; dipanggil oleh `order_service` saat reservasi dan penerbitan tiket.
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (BACKEND SIAP, UI SEDANG DIINTEGRASIKAN)**
* **Known Issues**: Halaman `pilih-tiket.html` perlu memastikan ketersediaan script navbar universal `script.js` untuk konsistensi sesi.
* **Next Steps bagi Developer Baru**: Finalisasi antarmuka dompet tiket (`my-tickets.html`) agar menampilkan rendering kode QR visual menggunakan pustaka JS `qrcode.min.js`.

---

### 8.4. ORDER SERVICE (`/order_service`)
* **Tujuan Utama**: Menangani alur transaksi pemesanan tiket, checkout keranjang belanja, kalkulasi invoice, dan batas waktu pembayaran.
* **Lokasi Direktori**: `/order_service` (Port: **8084**)
* **Domain Responsibility**:
  * Pembuatan nomor pesanan acak yang unik (format: `ORD-YYYYMMDD-XXXXX`).
  * Pengikatan rincian item tiket yang dibeli (`DetailPemesanan`).
  * Penegakan batas waktu pelunasan tagihan (*holding payment countdown* 15 menit).
  * Pengendalian status transaksi: `PENDING` -> `PAID`, `EXPIRED`, atau `CANCELLED`.
  * Halaman checkout (`checkout.html`), simulasi pembayaran (`pembayaran.html`), dan riwayat seluruh pesanan (`my-orders.html`).
* **Folder Structure**:
  * `src/main/java/com/tiketkonser/order_service/` (`config`, `controller`, `entity`, `handler`, `payload`, `repository`, `security`, `service`, `utility`).
  * `src/main/resources/static/`: `checkout.html`, `pembayaran.html`, `my-orders.html`.
* **Entity**: `Pemesanan`, `DetailPemesanan`.
* **Controller / API**:
  * `PemesananController`: `/api/pemesanan` (POST, GET), `/api/pemesanan/{id}` (GET), `/api/pemesanan/{id}/pay` (POST), `/api/pemesanan/{id}/cancel` (POST), `/api/pemesanan/my-orders` (GET).
* **Business Logic (`service/impl`)**:
  * `PemesananServiceImpl`: Perhitungan total tagihan, validasi kuota ke `ticket_service`, pengaturan batas waktu bayar, dan transisi status transaksi.
* **Security Implementation**: Seluruh endpoint pemesanan terproteksi dan mewajibkan autentikasi JWT peran `PEMBELI`.
* **Dependencies**: Memanggil `ticket_service` untuk reservasi kuota; memanggil `promo_service` untuk kalkulasi diskon kupon.
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (LOGIKA INTI SIAP, INTEGRASI UI SEDANG BERJALAN)**
* **Known Issues**: Transisi status `EXPIRED` saat ini mengandalkan pengecekan manual saat query; diperlukan scheduler otomatis.
* **Next Steps bagi Developer Baru**: Buat Spring Scheduled Task (`@Scheduled`) setiap menit untuk membatalkan pesanan yang melampaui `tenggatPembayaran` dan melepaskan kembali kuota tiket yang tersandera.

---

### 8.5. PROMO SERVICE (`/promo_service`)
* **Tujuan Utama**: Mengelola aturan kupon promosi, persentase diskon harga tiket, dan pembatasan frekuensi pemakaian kupon per akun.
* **Lokasi Direktori**: `/promo_service` (Port: **8088**)
* **Domain Responsibility**:
  * Pembuatan kode promo unik oleh promotor atau admin.
  * Konfigurasi batas diskon persentase, potongan maksimal, transaksi minimal, dan kuota total.
  * Validasi masa aktif kupon (`mulaiBerlaku` hingga `selesaiBerlaku`).
  * Pencatatan histori transaksi yang memanfaatkan kupon (`PenggunaanPromo`).
* **Folder Structure**:
  * `src/main/java/com/tiketkonser/promo_service/` (`config`, `controller`, `entity`, `handler`, `payload`, `repository`, `security`, `service`, `utility`).
* **Entity**: `KodePromo`, `PenggunaanPromo`.
* **Controller / API**:
  * `PromoController`: `/api/promo` (GET, POST), `/api/promo/validate` (POST), `/api/promo/{id}` (GET, PUT, DELETE).
* **Business Logic (`service/impl`)**:
  * `PromoServiceImpl`: Validasi keabsahan kupon, kalkulasi potongan nominal, dan pencatatan pemakaian kupon.
* **Security Implementation**: Endpoint mutasi promo dibatasi untuk `PENYELENGGARA` dan `ADMIN`. Endpoint validasi terbuka untuk verifikasi order.
* **Dependencies**: Dipanggil oleh `order_service` saat proses perhitungan ringkasan belanja di halaman checkout.
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (BACKEND LENGKAP, FORM INPUT UI SEGERA DIHUBUNGKAN)**
* **Known Issues**: Belum ada antarmuka dasbor khusus promotor untuk membuat kupon; baru tersedia via REST API.
* **Next Steps bagi Developer Baru**: Tambahkan antarmuka tab "Kelola Promo" pada `organizer-dashboard.html`.

---

### 8.6. NOTIFICATION SERVICE (`/notification_service`)
* **Tanggung Jawab**: Pusat penyimpanan dan penyampaian pesan pemberitahuan transaksi serta perubahan status konser.
* **Lokasi Direktori**: `/notification_service` (Port: **8086**)
* **Entity**: `Notifikasi` (id, penggunaId, judul, pesan, tipe, status, dibaca, createdAt).
* **Controller**: `NotifikasiController` (`/api/notifikasi`, `/api/notifikasi/my-notifications`, `/api/notifikasi/{id}/read`).
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (BACKEND SIAP, WEBHOOK TRANSAKSI SEDANG DIHUBUNGKAN)**
* **Next Steps bagi Developer Baru**: Hubungkan trigger otomatis saat status pesanan di `order_service` menjadi `PAID` agar mengirimkan entitas notifikasi in-app dan email konfirmasi.

---

### 8.7. CHECK-IN SERVICE (`/checkin_service`)
* **Tanggung Jawab**: Validasi tiket digital di gerbang konser (*gate validation*) dan pencatatan audit kehadiran penonton.
* **Lokasi Direktori**: `/checkin_service` (Port: **8087**)
* **Entity**: `RiwayatCheckin` (id, acaraId, tiketId, kodeQr, petugasId, lokasiGate, statusCheckin, waktuCheckin).
* **Controller**: `CheckinController` (`POST /api/checkin/validate`, `GET /api/checkin/acara/{acaraId}/summary`).
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (BACKEND VALIDASI SIAP)**
* **Next Steps bagi Developer Baru**: Bangun antarmuka web khusus pemindai kamera (*Camera QR Scanner UI*) menggunakan pustaka `html5-qrcode` agar staf gerbang dapat memindai tiket penonton langsung dari peramban HP.

---

### 8.8. QUEUE SERVICE (`/queue_service`)
* **Tanggung Jawab**: Penyangga beban (*traffic buffer*) dan ruang tunggu antrean virtual (*Virtual Waiting Room*) untuk meredam lonjakan *ticket war*.
* **Lokasi Direktori**: `/queue_service` (Port: **8085**)
* **Entity**: `AntreanPembeli` (id, acaraId, penggunaId, nomorAntrean, status, masukAt, diprosesAt, selesaiAt).
* **Controller**: `QueueController` (`POST /api/queue/join`, `GET /api/queue/status`, `POST /api/queue/leave`).
* **Status Implementasi**: 🟡 **PARTIALLY IMPLEMENTED (BACKEND & WAITING ROOM TERSEDIA)**
* **Next Steps bagi Developer Baru**: Tambahkan pooling/websocket pada `waiting-room.html` untuk memantau posisi antrean dan pengalihan otomatis ke `pilih-tiket.html` saat giliran tiba.

---

### 8.9. ADMIN SERVICE (`/admin_service`)
* **Tanggung Jawab**: Pusat pengawasan kepatuhan ekosistem, penelusuran jejak audit forensik, dan laporan performa bisnis platform.
* **Lokasi Direktori**: `/admin_service` (Port: **8089**)
* **Entity**: `AuditLog`, `LaporanPenjualan`.
* **Controller**: `AdminController` (`GET /api/admin/summary`, `GET /api/admin/audit-logs`, `GET /api/admin/reports/sales`).
* **Status Implementasi**: 🟢 **IMPLEMENTED (PRODUCTION-READY)**
* **Next Steps bagi Developer Baru**: Integrasikan grafik visual penjualan (misalnya menggunakan Chart.js) pada halaman `admin-dashboard.html`.

---

## 9. INTERNAL CODE ARCHITECTURE

Setiap microservice di TIKARA dibangun menggunakan struktur kode berlapis (**Layered Architecture Pattern**) yang seragam:

```text
src/main/java/com/tiketkonser/[service_name]
│
├── config/              # Konfigurasi Spring (DataSeeder, OpenAPI/Swagger, CORS)
├── controller/          # REST Controller yang menerima HTTP Request dan validasi DTO
├── entity/              # Kelas Entity JPA yang memetakan tabel basis data relasional
├── handler/             # GlobalExceptionHandler (@RestControllerAdvice) penanganan error seragam
├── payload/             # Data Transfer Objects (DTO)
│   ├── req/             # Request DTO masukan dari client dengan anotasi validasi
│   └── res/             # Response DTO kembalian ke client tanpa membocorkan atribut internal
├── repository/          # Antarmuka JpaRepository untuk abstraksi akses basis data
├── security/            # JwtAuthenticationFilter, JwtService, dan SecurityConfig
├── service/             # Interface deklarasi kontrak logika bisnis murni
│   └── impl/            # Implementasi konkret logika bisnis (@Service, @Transactional)
└── utility/             # Helper fungsi statis, EventMapper, dan AcaraSpecification dinamis
```

---


## 10. ENTITY & DOMAIN MODEL (17 ENTITAS RIIL)

Berikut adalah dokumentasi mendalam atas **17 Entitas Basis Data Riil** yang terverifikasi dalam source code:

### 1. `Pengguna` (Owner: `user_service` | Tabel: `pengguna`)
* **Purpose**: Menyimpan akun otentikasi utama dan penentuan peran otorisasi pengguna.
* **Fields**: `id` (Long, PK), `nama` (String), `email` (String, Unique), `kataSandiHash` (String, BCrypt), `peran` (String: PEMBELI/PENYELENGGARA/ADMIN), `createdAt`, `updatedAt`, `deletedAt` (Soft delete).
* **Relationships**: One-to-One dengan `ProfilPengguna`.
* **Lifecycle**: Dibuat saat registrasi; diperbarui saat edit profil/password; dihapus secara soft-delete jika dinonaktifkan.
* **APIs**: `/api/auth/register`, `/api/auth/login`, `/api/users/profile`.

### 2. `ProfilPengguna` (Owner: `user_service` | Tabel: `profil_pengguna`)
* **Purpose**: Menyimpan data demografis personal pengguna.
* **Fields**: `id` (Long, PK), `pengguna` (FK Pengguna), `nomorTelepon` (String), `tanggalLahir` (LocalDate), `alamat` (String), `fotoProfilUrl` (String), `createdAt`, `updatedAt`.
* **APIs**: `/api/users/profile`, `/api/users/upload-avatar`.

### 3. `ResetPasswordOtp` (Owner: `user_service` | Tabel: `reset_password_otp`)
* **Purpose**: Menyimpan token sandi OTP sementara 6-digit untuk proses lupa password.
* **Fields**: `id` (Long, PK), `email` (String), `otpCode` (String), `expiredAt` (LocalDateTime), `isUsed` (Boolean), `createdAt`.
* **Lifecycle**: Dibuat saat permintaan forgot password; ditandai `isUsed = true` setelah berhasil verifikasi; kedaluwarsa dalam 15 menit.
* **APIs**: `/api/auth/forgot-password`, `/api/auth/verify-otp`, `/api/auth/reset-password`.

### 4. `Acara` (Owner: `event_service` | Tabel: `acara`)
* **Purpose**: Menyimpan data jadwal konser musik, venue, dan status tayang.
* **Fields**: `id` (Long, PK), `organizerId` (Long), `organizerNama` (String), `nama` (String), `deskripsi` (Text), `venue` (String), `kota` (String), `tanggalMulai`, `tanggalSelesai`, `status` (DRAFT/PUBLISHED/ENDED), `gambarUrl`, `createdAt`, `updatedAt`, `deletedAt`.
* **Relationships**: Many-to-Many dengan `Kategori` via `acara_kategori`.
* **APIs**: `/api/acara` (GET, POST), `/api/acara/{id}`, `/api/acara/my-events`, `/api/acara/{id}/upload-poster`.

### 5. `Kategori` (Owner: `event_service` | Tabel: `kategori`)
* **Purpose**: Master data genre musik konser (Pop, Rock, Jazz, Indie, EDM, Klasik, Dangdut, Hip-Hop).
* **Fields**: `id` (Long, PK), `namaKategori` (String), `createdAt`, `updatedAt`.
* **APIs**: `/api/kategori`.

### 6. `Ulasan` (Owner: `event_service` | Tabel: `ulasan`)
* **Purpose**: Menyimpan penilaian rating (1-5) dan review konser dari penonton.
* **Fields**: `id` (Long, PK), `acara` (FK Acara), `penggunaId` (Long), `penggunaNama` (String), `rating` (Integer), `komentar` (String), `createdAt`.
* **APIs**: `/api/ulasan`.

### 7. `KategoriTiket` (Owner: `ticket_service` | Tabel: `kategori_tiket`)
* **Purpose**: Definisi tier tiket per acara konser (VIP, Festival, Regular) beserta harga dan kuota.
* **Fields**: `id` (Long, PK), `acaraId` (Long), `namaKategori` (String), `harga` (BigDecimal), `kuota` (Integer), `terjual` (Integer), `direservasi` (Integer), `mulaiJual`, `selesaiJual`, `status` (AVAILABLE/SOLD_OUT/ENDED), `createdAt`, `updatedAt`.
* **APIs**: `/api/kategori-tiket`, `/api/kategori-tiket/acara/{acaraId}`.

### 8. `Tiket` (Owner: `ticket_service` | Tabel: `tiket`)
* **Purpose**: Instans e-tiket unik individual milik penonton dengan kode string token QR verifikasi.
* **Fields**: `id` (Long, PK), `kategoriTiket` (FK KategoriTiket), `detailPemesananId` (Long), `penggunaId` (Long), `kodeQr` (String, Unique), `status` (ACTIVE/USED/CANCELLED), `diterbitkanAt`, `digunakanAt`, `deletedAt`.
* **APIs**: `/api/tiket/my-tickets`, `/api/tiket/issue`.

### 9. `Pemesanan` (Owner: `order_service` | Tabel: `pemesanan`)
* **Purpose**: Transaksi induk pemesanan tiket dengan nomor invoice unik.
* **Fields**: `id` (Long, PK), `penggunaId` (Long), `nomorPesanan` (String, Unique), `totalHarga` (BigDecimal), `status` (PENDING/PAID/EXPIRED/CANCELLED), `tenggatPembayaran` (LocalDateTime), `createdAt`, `updatedAt`, `deletedAt`.
* **Relationships**: One-to-Many dengan `DetailPemesanan`.
* **APIs**: `/api/pemesanan`, `/api/pemesanan/{id}/pay`, `/api/pemesanan/my-orders`.

### 10. `DetailPemesanan` (Owner: `order_service` | Tabel: `detail_pemesanan`)
* **Purpose**: Rincian jumlah tiket per tier dalam satu nomor pesanan.
* **Fields**: `id` (Long, PK), `pemesanan` (FK Pemesanan), `kategoriTiketId` (Long), `jumlah` (Integer), `hargaSatuan` (BigDecimal), `subtotal` (BigDecimal).

### 11. `KodePromo` (Owner: `promo_service` | Tabel: `kode_promo`)
* **Purpose**: Konfigurasi voucher diskon belanja tiket.
* **Fields**: `id` (Long, PK), `kode` (String, Unique), `persenDiskon` (BigDecimal), `potonganMaksimal` (BigDecimal), `minimalTransaksi` (BigDecimal), `kuotaPromo` (Integer), `terpakai` (Integer), `mulaiBerlaku`, `selesaiBerlaku`, `status` (ACTIVE/EXPIRED/INACTIVE), `createdAt`, `updatedAt`.
* **APIs**: `/api/promo`, `/api/promo/validate`.

### 12. `PenggunaanPromo` (Owner: `promo_service` | Tabel: `penggunaan_promo`)
* **Purpose**: Catatan riwayat penggunaan voucher diskon oleh pengguna.
* **Fields**: `id` (Long, PK), `kodePromo` (FK KodePromo), `penggunaId` (Long), `pemesananId` (Long), `potonganDidapat` (BigDecimal), `digunakanAt`.

### 13. `Notifikasi` (Owner: `notification_service` | Tabel: `notifikasi`)
* **Purpose**: Pesan pemberitahuan personal untuk pengguna.
* **Fields**: `id` (Long, PK), `penggunaId` (Long), `judul` (String), `pesan` (String), `tipe` (String), `status` (String), `dibaca` (Boolean), `createdAt`.
* **APIs**: `/api/notifikasi`, `/api/notifikasi/my-notifications`.

### 14. `RiwayatCheckin` (Owner: `checkin_service` | Tabel: `riwayat_checkin`)
* **Purpose**: Log pencatatan verifikasi tiket di gerbang masuk konser.
* **Fields**: `id` (Long, PK), `acaraId` (Long), `tiketId` (Long), `kodeQr` (String), `petugasId` (Long), `lokasiGate` (String), `statusCheckin` (SUCCESS/FAILED), `waktuCheckin`.
* **APIs**: `/api/checkin/validate`.

### 15. `AntreanPembeli` (Owner: `queue_service` | Tabel: `antreanpembeli`)
* **Purpose**: Nomor urut antrean ruang tunggu virtual tiket war.
* **Fields**: `id` (Long, PK), `acaraId` (Long), `penggunaId` (Long), `nomorAntrean` (Integer), `status` (WAITING/PROCESSING/COMPLETED), `masukAt`, `diprosesAt`, `selesaiAt`.
* **APIs**: `/api/queue/join`, `/api/queue/status`.

### 16. `AuditLog` (Owner: `admin_service` | Tabel: `audit_log`)
* **Purpose**: Jejak rekam forensik atas aksi administratif platform.
* **Fields**: `id` (Long, PK), `penggunaId` (Long), `aksi` (String), `modul` (String), `detail` (String), `ipAddress` (String), `createdAt`.
* **APIs**: `/api/admin/audit-logs`.

### 17. `LaporanPenjualan` (Owner: `admin_service` | Tabel: `laporan_penjualan`)
* **Purpose**: Data agregasi total omzet dan tiket terjual per konser.
* **Fields**: `id` (Long, PK), `acaraId` (Long), `totalTiketTerjual` (Integer), `totalPendapatan` (BigDecimal), `periode` (String), `createdAt`.
* **APIs**: `/api/admin/reports/sales`.

---

## 11. ROLE & ACCESS CONTROL (RBAC)

### 11.1. Profil Pengguna & Hak Akses
1. **`PEMBELI`**: Penikmat musik yang memesan tiket, melakukan pembayaran, mengelola dompet tiket, dan melakukan check-in.
2. **`PENYELENGGARA`**: Promotor konser yang merilis acara, mengatur kuota tiket, mengunggah poster, dan memantau omzet penjualan.
3. **`ADMIN`**: Pengawas sistem yang memoderasi konser, meninjau log audit, dan memantau stabilitas platform.

### 11.2. Role Permission Matrix

| Modul Fungsional | Tamu (Guest) | PEMBELI | PENYELENGGARA | ADMIN |
| :--- | :---: | :---: | :---: | :---: |
| Registrasi Akun Publik | 🟢 Ya | 🔴 Tidak | 🟢 Ya | 🔴 Khusus Database |
| Login & Autentikasi | 🟢 Ya | 🟢 Ya | 🟢 Ya | 🟢 Ya |
| Kelola Profil Pribadi | 🔴 Tidak | 🟢 Ya | 🟢 Ya | 🟢 Ya |
| Jelajah Katalog Konser | 🟢 Ya | 🟢 Ya | 🟢 Ya (Mode Jelajah) | 🟢 Ya |
| Beli Tiket & Checkout | 🔴 Harus Login | 🟢 Ya | 🟡 Khusus Uji Coba | 🟡 Khusus Uji Coba |
| Dompet Tiket Saya | 🔴 Tidak | 🟢 Ya | 🔴 Tidak | 🔴 Tidak |
| Riwayat Pesanan | 🔴 Tidak | 🟢 Ya | 🔴 Tidak | 🔴 Tidak |
| Buat Konser & Tier Tiket | 🔴 Tidak | 🔴 Tidak | 🟢 Ya | 🟢 Ya (Supervisi) |
| Dasbor Promotor | 🔴 Tidak | 🔴 Tidak | 🟢 Ya | 🟢 Ya |
| Pindai Tiket di Gate | 🔴 Tidak | 🔴 Tidak | 🟢 Ya (Petugas Gate) | 🟢 Ya |
| Moderasi Acara | 🔴 Tidak | 🔴 Tidak | 🔴 Tidak | 🟢 Ya |
| Akses Audit Log Forensik | 🔴 Tidak | 🔴 Tidak | 🔴 Tidak | 🟢 Ya |

---

## 12. SYSTEM FLOW (END-TO-END)

### Flow A — Registrasi Akun
`User mengisi Form` -> `POST /api/auth/register` -> `Validasi email & password` -> `BCrypt Hashing` -> `Pengguna tersimpan di db_user_service` -> `Akun aktif`.

### Flow B — Login & Otentikasi Terdistribusi
`User input kredensial` -> `POST /api/auth/login` -> `BCrypt Verify` -> `Generate JWT (userId, email, role)` -> `Browser simpan token di localStorage` -> `script.js render Navbar sesuai Role`.

### Flow C — Pembuatan Konser & Tier Tiket oleh Promotor
`Promotor buka create-event.html` -> `POST /api/acara` (simpan info acara) -> `POST /api/acara/{id}/upload-poster` (unggah poster) -> `Loop POST /api/kategori-tiket` (daftarkan tier VIP, Festival ke `ticket_service`) -> `Konser tayang di katalog`.

### Flow D — Pembelian Tiket & Holding Time
`Pembeli pilih tiket di pilih-tiket.html` -> `POST /api/pemesanan` (Order Service) -> `POST /api/inventory/hold` (kuota direservasi sementara) -> `Status: PENDING (15 menit countdown)` -> `Simulasi bayar di pembayaran.html` -> `POST /api/tiket/issue` (Tiket terbit dengan kode QR) -> `Status: PAID`.

### Flow E — Validasi Gate Check-in
`Penonton buka my-tickets.html` -> `Petugas scan QR Code di pintu gerbang` -> `POST /api/checkin/validate` -> `Cek status tiket di ticket_service` -> `Jika ACTIVE: status tiket diupdate menjadi USED, log tersimpan di RiwayatCheckin, gerbang terbuka`.

---

## 13. API OVERVIEW (RINGKASAN ENDPOINT UTAMA)

Berikut adalah daftar endpoint REST API utama yang aktif pada seluruh microservice:

| Method | Endpoint URI | Service | Tujuan Fungsional | Role Akses |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | `user_service` | Registrasi akun Pembeli & Promotor | Public |
| `POST` | `/api/auth/login` | `user_service` | Login & penerbitan token JWT | Public |
| `GET` | `/api/users/profile` | `user_service` | Membaca biodata akun yang sedang login | Authenticated |
| `PUT` | `/api/users/profile` | `user_service` | Memperbarui biodata pengguna | Authenticated |
| `POST` | `/api/users/upload-avatar` | `user_service` | Mengunggah foto profil pengguna | Authenticated |
| `GET` | `/api/acara` / `/api/events` | `event_service` | Pencarian katalog konser dengan multi-filter | Public |
| `GET` | `/api/acara/{id}` | `event_service` | Mengambil data lengkap detail konser | Public |
| `POST` | `/api/acara` | `event_service` | Membuat konser baru oleh promotor | `PENYELENGGARA`, `ADMIN` |
| `POST` | `/api/acara/{id}/upload-poster`| `event_service`| Mengunggah berkas gambar poster konser | `PENYELENGGARA`, `ADMIN` |
| `GET` | `/api/acara/my-events` | `event_service` | Mengambil daftar konser milik promotor | `PENYELENGGARA`, `ADMIN` |
| `GET` | `/api/acara/organizer/analytics`| `event_service`| Metrik analitik pendapatan & kuota promotor | `PENYELENGGARA`, `ADMIN` |
| `POST` | `/api/acara/{id}/moderate` | `event_service` | Menyetujui atau mencabut tayang konser | `ADMIN` |
| `GET` | `/api/kategori` | `event_service` | Mengambil daftar master genre musik | Public |
| `POST` | `/api/kategori-tiket` | `ticket_service` | Membuat tier tiket baru untuk suatu acara | `PENYELENGGARA`, `ADMIN` |
| `GET` | `/api/kategori-tiket/acara/{id}`| `ticket_service`| Mengambil daftar kategori tiket per acara | Public |
| `GET` | `/api/tiket/my-tickets` | `ticket_service` | Mengambil seluruh e-tiket milik pembeli | `PEMBELI` |
| `POST` | `/api/inventory/hold` | `ticket_service` | Mengunci kuota sementara saat checkout | System / Order |
| `POST` | `/api/pemesanan` | `order_service` | Membuat transaksi pesanan baru | `PEMBELI` |
| `POST` | `/api/pemesanan/{id}/pay` | `order_service` | Memproses pembayaran pesanan tiket | `PEMBELI` |
| `GET` | `/api/pemesanan/my-orders` | `order_service` | Mengambil riwayat seluruh pesanan pembeli | `PEMBELI` |
| `POST` | `/api/promo/validate` | `promo_service` | Memvalidasi keabsahan kode voucher promo | Authenticated |
| `GET` | `/api/notifikasi/my-notifications`| `notification_service`| Mengambil notifikasi personal pengguna | Authenticated |
| `POST` | `/api/checkin/validate` | `checkin_service` | Memvalidasi keabsahan tiket di gerbang | `PENYELENGGARA`, `ADMIN` |
| `POST` | `/api/queue/join` | `queue_service` | Masuk ke antrean virtual war tiket | Authenticated |
| `GET` | `/api/admin/summary` | `admin_service` | Metrik ringkasan platform untuk Admin | `ADMIN` |
| `GET` | `/api/admin/audit-logs` | `admin_service` | Menelusuri jejak log audit sistem | `ADMIN` |

---


## 14. DATABASE & DATA OWNERSHIP

### 14.1. Pemetaan Basis Data per Microservice
TIKARA mengadopsi pola **Database-per-Service**. Secara konfigurasi default pada `application.properties`, masing-masing microservice mengarah ke skema basis data PostgreSQL tersendiri:
* `user_service` -> `jdbc:postgresql://localhost:5432/db_user_service`
* `event_service` -> `jdbc:postgresql://localhost:5432/db_event_service`
* `ticket_service` -> `jdbc:postgresql://localhost:5432/db_ticket_service`
* `order_service` -> `jdbc:postgresql://localhost:5432/db_order_service`
* `promo_service` -> `jdbc:postgresql://localhost:5432/db_promo_service`
* `notification_service` -> `jdbc:postgresql://localhost:5432/db_notification_service`
* `checkin_service` -> `jdbc:postgresql://localhost:5432/db_checkin_service`
* `queue_service` -> `jdbc:postgresql://localhost:5432/db_queue_service`
* `admin_service` -> `jdbc:postgresql://localhost:5432/db_admin_service`

### 14.2. Relasi Lintas Service: Logical Reference vs Foreign Key
* **Prinsip**: Di dalam basis data microservice, tidak ada *Foreign Key constraint* lintas basis data fisik.
* **Mekanisme**: Entitas menggunakan atribut primitif `Long` sebagai referensi logis (misalnya: `Acara.organizerId` mereferensikan `Pengguna.id`, `KategoriTiket.acaraId` mereferensikan `Acara.id`, dan `DetailPemesanan.kategoriTiketId` mereferensikan `KategoriTiket.id`).
* **Risiko & Peringatan untuk Developer Baru**: Jika developer secara sengaja menggabungkan semua service ke dalam satu database fisik tanpa pemisahan skema, hal tersebut melanggar prinsip otonomi microservice dan menimbulkan kopling basis data (*database coupling*).

---

## 15. TECHNOLOGY STACK

| Komponen | Teknologi | Versi | Peran Teknis |
| :--- | :--- | :--- | :--- |
| **Bahasa Pemrograman** | Java | OpenJDK 17 LTS | Bahasa pemrograman strongly-typed berkinerja tinggi |
| **Framework Backend** | Spring Boot | 3.4.3 | Framework enterprise berbasis dependency injection |
| **Framework Keamanan** | Spring Security & JJWT | 6.x / JJWT 0.12.6 | Penegakan filter keamanan stateless dan JWT token |
| **ORM / Data Access** | Spring Data JPA / Hibernate | 3.4.3 | Abstraksi akses basis data relasional & query otomatis |
| **Database Server** | PostgreSQL | 15+ | RDBMS tangguh penjamin integritas transaksi ACID |
| **Build & Dependency Tool**| Apache Maven | 3.8+ | Pengelola dependensi pustaka dan otomatisasi build |
| **Boilerplate Reducer** | Project Lombok | Terintegrasi | Otomatisasi getter/setter/builder pada kompilasi |
| **Validasi Masukan** | Hibernate Validator | 3.0 | Validasi deklaratif parameter payload (@Valid) |
| **Dokumentasi API** | Springdoc OpenAPI Starter UI| 2.8.5 | Penyedia antarmuka interaktif Swagger UI |
| **Frontend Layout** | Vanilla HTML5 & Modern CSS3 | W3C Standard | Antarmuka ringan bebas overhead kompilasi framework |
| **Frontend Logic** | Vanilla ES6+ JavaScript | Modern ECMAScript| Logika state peramban instan dan terpusat |

---

## 16. CURRENT IMPLEMENTATION STATUS

Tabel audit komprehensif atas status fitur riil saat serah terima proyek:

| Modul Fungsional | Layanan | Status | Bukti Teknis & Kondisi Riil |
| :--- | :--- | :---: | :--- |
| **Registrasi Multi-Role** | `user_service` | 🟢 | Endpoint `POST /api/auth/register` siap dengan validasi BCrypt. |
| **Login & JWT Token** | `user_service` | 🟢 | Endpoint `POST /api/auth/login` menghasilkan token JWT valid. |
| **Universal Client Auth** | Frontend Global | 🟢 | `script.js` & `config.js` di seluruh service sinkron. |
| **Profil & Avatar User** | `user_service` | 🟢 | `profile.html` dengan instant hydration dan upload avatar. |
| **Forgot Password & OTP** | `user_service` | 🟢 | Halaman dan logika verifikasi OTP 6 digit berfungsi. |
| **Katalog Konser & Filter**| `event_service` | 🟢 | `index.html` dengan pencarian nama, kota, dan genre dinamis. |
| **Detail Konser Publik** | `event_service` | 🟢 | `event-detail.html` mengambil data dinamis via API. |
| **Upload Poster Acara** | `event_service` | 🟢 | Endpoint `upload-poster` multipart file berfungsi. |
| **Form Buat Konser Promotor**| `event_service` | 🟢 | `create-event.html` dengan live preview dan multi-tier tiket. |
| **Dasbor Analitik Promotor** | `event_service` | 🟢 | `organizer-dashboard.html` dengan metrik omzet & sisa kuota. |
| **Event Saya (Promotor)** | `event_service` | 🟢 | `my-events.html` dengan isolasi data `organizerId`. |
| **Tier Tiket & Kuota** | `ticket_service` | 🟢 | Endpoint `POST /api/kategori-tiket` siap di backend. |
| **Pemilihan Tiket Penonton** | `ticket_service` | 🟡 | `pilih-tiket.html` siap, kalkulasi dinamis kuota difinalisasi. |
| **Dompet Tiket Saya** | `ticket_service` | 🟡 | `my-tickets.html` siap, visualisasi QR code sedang dihubungkan. |
| **Checkout Pemesanan** | `order_service` | 🟡 | `checkout.html` dan `PemesananController` siap dihubungkan. |
| **Simulasi Pembayaran** | `order_service` | 🟡 | `pembayaran.html` dengan alur status `PENDING` -> `PAID`. |
| **Riwayat Pesanan** | `order_service` | 🟡 | `my-orders.html` telah tersedia di folder static. |
| **Kupon Promo & Diskon** | `promo_service` | 🟡 | Backend lengkap; input kupon di keranjang checkout diselaraskan. |
| **Pusat Notifikasi User** | `notification_service` | 🟡 | Entitas `Notifikasi` siap; webhook event transaksi dihubungkan. |
| **Validasi Gate (Check-in)**| `checkin_service` | 🟡 | Endpoint validasi siap; UI pemindai kamera direncanakan. |
| **Ruang Antrean Virtual** | `queue_service` | 🟡 | Entitas antrean & `waiting-room.html` telah tersedia. |
| **Dasbor Pengawas Admin** | `admin_service` | 🟢 | `admin-dashboard.html` dengan audit log dan rekap penjualan. |

---

## 17. WHAT WORKS (FITUR YANG SUDAH BERFUNGSI PENUH)

1. **Sistem Identitas & Otentikasi Terdistribusi**:
   Login, registrasi akun Pembeli & Promotor, ganti password, verifikasi OTP, dan pembaruan foto profil avatar berfungsi 100%.
2. **Katalog Penjelajahan Konser Musik**:
   Halaman beranda pencarian konser publik di `index.html` dengan filter kota, genre musik dinamis, dan kata kunci artis berfungsi mulus tanpa bug dropdown kosong.
3. **Portal Manajemen Promotor Acara**:
   Promotor dapat membuat konser baru (`create-event.html`), mengunggah poster promosi visual, menyusun tier tiket, dan memantau performa penjualan konser di `organizer-dashboard.html` serta `my-events.html`.
4. **Dasbor Pengawasan Administratif**:
   Administrator dapat memantau aktivitas sistem, memeriksa log audit forensik, dan menyetujui konser promotor di `admin-dashboard.html`.

---

## 18. WHAT IS INCOMPLETE (FITUR YANG SEDANG DALAM PENGEMBANGAN)

1. **Integrasi UI Alur Transaksi End-to-End**:
   Alur saat Pembeli mengklik "Beli Tiket" di `event-detail.html` -> berpindah ke `pilih-tiket.html` (`ticket_service`) -> lalu ke `checkout.html` dan `pembayaran.html` (`order_service`) membutuhkan pengujian sinkronisasi sesi terpadu agar tidak ada data yang terputus.
2. **Visualisasi Kode QR pada E-Tiket**:
   Entitas `Tiket` telah menghasilkan string token kode QR unik; diperlukan integrasi pustaka JS (seperti `qrcode.min.js`) di `my-tickets.html` untuk merender kode QR secara visual agar siap dipindai kamera.
3. **Antarmuka Pemindai Gate Check-in**:
   Backend `checkin_service` telah siap memvalidasi kode QR; diperlukan pembuatan antarmuka web scanner kamera sederhana bagi staf gerbang masuk.

---

## 19. WHAT IS PLANNED (RENCANA FITUR MASA DEPAN)

1. **Integrasi Payment Gateway Produksi**:
   Menghubungkan Midtrans / Xendit / DOKU untuk menerima pembayaran nyata via Virtual Account, QRIS, dan Kartu Kredit.
2. **Penyimpanan Berkas Terdistribusi**:
   Migrasi folder unggahan lokal `/uploads` ke Cloud Object Storage (Amazon S3 / MinIO).
3. **Komunikasi Asinkron Berbasis Message Broker**:
   Mengadopsi RabbitMQ atau Apache Kafka untuk penanganan event pembelian tiket dan pengiriman notifikasi masal saat *ticket war*.

---

## 20. KNOWN ISSUES & RISKS

1. **Ketergantungan Port Lokal pada Lingkungan Pengembangan**:
   Aplikasi client mengakses microservice melalui port berbeda (8081 hingga 8089). Jika salah satu service lupa dijalankan, fitur terkait akan menampilkan pesan error koneksi.
2. **Holding Time Belum Memiliki Background Worker Otomatis**:
   Pembatalan tiket kedaluwarsa saat ini masih dievaluasi saat pemanggilan query, belum memiliki Spring Scheduled Cron otomatis.
3. **CORS Configuration**:
   Setiap penambahan origin domain baru wajib didaftarkan pada konfigurasi CORS `SecurityConfig` di masing-masing service.

---

## 21. TECHNICAL DEBT

| Isu Technical Debt | Dampak Teknis | Prioritas | Rekomendasi Solusi |
| :--- | :--- | :---: | :--- |
| **Ketiadaan API Gateway Terpusat** | Client memanggil 9 nomor port berbeda. | **TINGGI** | Terapkan Spring Cloud Gateway pada port 80/443 sebagai gerbang tunggal. |
| **Penyimpanan Berkas Masih Lokal** | Tidak scalable jika service di-deploy multi-server. | **TINGGI** | Pindahkan ke S3-compatible cloud storage. |
| **Belum Ada Worker Pembatalan Pesanan** | Kuota tiket yang tidak dibayar terlambat dilepas. | **SEDANG** | Buat method `@Scheduled` per menit di `order_service`. |
| **Render QR Code Masih String Text** | Penonton belum melihat gambar barcode grafis. | **SEDANG** | Sisipkan library JS `qrcode.js` di `my-tickets.html`. |
| **Uji Otomatis (Unit & Integration Tests)** | Cakupan pengujian unit test baru di level dasar. | **SEDANG** | Tingkatkan coverage test JUnit 5 & Mockito di layer Service. |

---

## 22. RECOMMENDED NEXT STEPS (URUTAN KERJA DEVELOPER BARU)

Berikut adalah panduan tindakan praktis yang harus dieksekusi oleh developer yang mengambil alih proyek:

* **Langkah 1: Hubungkan Alur Beli Tiket (`event_service` -> `ticket_service`)**:
  Pastikan tombol "Beli Tiket" di [event-detail.html](file:///c:/PROJEKAN/tiketkonser-fullstack/event_service/src/main/resources/static/event-detail.html) membawa parameter `?acaraId=...` ke [pilih-tiket.html](file:///c:/PROJEKAN/tiketkonser-fullstack/ticket_service/src/main/resources/static/pilih-tiket.html).
* **Langkah 2: Selesaikan Alur Checkout (`ticket_service` -> `order_service`)**:
  Di `pilih-tiket.html`, simpan tiket yang dipilih dan alihkan ke [checkout.html](file:///c:/PROJEKAN/tiketkonser-fullstack/order_service/src/main/resources/static/checkout.html) untuk pembuatan pesanan resmi.
* **Langkah 3: Finalisasi Simulasi Pembayaran & Cetak E-Tiket**:
  Pastikan tombol bayar di [pembayaran.html](file:///c:/PROJEKAN/tiketkonser-fullstack/order_service/src/main/resources/static/pembayaran.html) memanggil `POST /api/tiket/issue` sehingga e-tiket resmi langsung muncul di [my-tickets.html](file:///c:/PROJEKAN/tiketkonser-fullstack/ticket_service/src/main/resources/static/my-tickets.html).
* **Langkah 4: Tambahkan Visualisasi QR Code di `my-tickets.html`**:
  Gunakan pustaka CDN `qrcode.min.js` untuk merender string `kodeQr` menjadi gambar barcode visual.
* **Langkah 5: Bangun Antarmuka Kamera Pemindai di `checkin_service`**:
  Buat halaman `scanner.html` sederhana berbasis HTML5 camera untuk memverifikasi tiket di pintu konser.

---

## 23. DEVELOPMENT ROADMAP

```
[ PRIORITY 1: Core System & Transaction Integrity ] ──────────────► Selesai (90%)
  ├─ User Auth & Profile (Done)
  ├─ Event Management & Promotor Portal (Done)
  └─ Ticket & Order Transaction Flow (Finalizing)

[ PRIORITY 2: Gate Operations & On-Site Experience ] ─────────────► Tahap Selanjutnya
  ├─ Visual QR Code Rendering di My Tickets
  └─ Camera Web Scanner untuk Check-in Gate

[ PRIORITY 3: Promo & Notifications Orchestration ] ──────────────► Tahap Selanjutnya
  ├─ Auto Trigger Email & In-App Notification saat Order Paid
  └─ Kupon Diskon Voucher Integration di Checkout

[ PRIORITY 4: High-Traffic Buffer & War Ticket Scalability ] ──────► Rilis Mendatang
  ├─ Virtual Queue Room Activation
  └─ Redis In-Memory Caching untuk Kuota Tiket

[ PRIORITY 5: Production Hardening & Cloud Infrastructure ] ───────► Skala Produksi
  ├─ API Gateway Terpusat (Spring Cloud Gateway)
  ├─ Docker & Kubernetes Containerization
  └─ Official Payment Gateway Integration (Midtrans / Xendit)
```

---

## 24. DEVELOPER TAKEOVER GUIDE (PANDUAN ONBOARDING)

### Hari ke-1: Pemahaman Arsitektur & Lingkungan Lokal
1. Baca dokumen handover ini secara menyeluruh dari Bab 1 hingga Bab 26.
2. Pastikan komputer Anda terpasang **Java 17 LTS**, **Apache Maven 3.8+**, dan **PostgreSQL 15+**.
3. Buat basis data di PostgreSQL: `db_user_service`, `db_event_service`, `db_ticket_service`, `db_order_service`, `db_promo_service`, `db_notification_service`, `db_checkin_service`, `db_queue_service`, `db_admin_service`.
4. Jalankan kompilasi awal pada root:
   ```bash
   mvn clean compile -DskipTests
   ```

### Hari ke-2: Uji Coba Layanan Inti
1. Jalankan `user_service` (port 8081) dan `event_service` (port 8082).
2. Buka peramban di `http://localhost:8081/login.html` dan login menggunakan akun pengujian.
3. Buka `http://localhost:8082/index.html` dan verifikasi katalog konser serta filter genre musik.
4. Buka `http://localhost:8082/create-event.html` dan uji coba pembuatan konser baru oleh promotor.

### Hari ke-3: Melanjutkan Roadmap Pengembangan
1. Jalankan `ticket_service` (port 8083) dan `order_service` (port 8084).
2. Eksekusi **Recommended Next Steps** (Langkah 1 hingga Langkah 5) untuk menuntaskan jalur transaksi end-to-end.

---

## 25. HANDOVER CHECKLIST

Gunakan daftar centang verifikasi serah terima berikut:

### Pemahaman Sistem
* [ ] Memahami visi TIKARA dan komparasinya terhadap Loket.com & TIX.ID
* [ ] Memahami peran `PEMBELI`, `PENYELENGGARA`, dan `ADMIN`
* [ ] Memahami pola Database-per-Service dan isolasi data `organizerId`

### Setup Teknis & Kompilasi
* [ ] Seluruh dependensi Maven terpasang tanpa konflik versi
* [ ] 9 basis data PostgreSQL berhasil dibuat dan terhubung
* [ ] Seluruh 9 microservice berhasil dikompilasi dengan status `BUILD SUCCESS`

### Verifikasi Fungsional Inti
* [ ] Registrasi & Login berhasil menerbitkan token JWT
* [ ] Navigasi dinamis menyesuaikan peran pengguna secara konsisten
* [ ] Pembuatan konser baru berhasil menyimpan data dan mengunggah poster
* [ ] Dasbor promotor menampilkan metrik analitik omzet yang akurat

### Rencana Pengembangan
* [ ] Memahami daftar technical debt dan batasan sistem saat ini
* [ ] Mengetahui prioritas pengerjaan integrasi alur transaksi tiket selanjutnya

---

## 26. APPENDIX: PORTS, ENVS & FOLDER TREE

### A. Tabel Nomor Port dan Variabel Lingkungan
* `SERVER_PORT_USER`: `8081`
* `SERVER_PORT_EVENT`: `8082`
* `SERVER_PORT_TICKET`: `8083`
* `SERVER_PORT_ORDER`: `8084`
* `SERVER_PORT_QUEUE`: `8085`
* `SERVER_PORT_NOTIFICATION`: `8086`
* `SERVER_PORT_CHECKIN`: `8087`
* `SERVER_PORT_PROMO`: `8088`
* `SERVER_PORT_ADMIN`: `8089`
* `JWT_SECRET_KEY`: `tiketkonsersecretkeytiketkonsersecretkeytiketkonsersecretkey1234567890` (Min 256 bits for HMAC-SHA256)

### B. Pohon Direktori Lengkap Repositori
```text
tiketkonser-fullstack
├── admin_service (AuditLog, LaporanPenjualan, AdminController, admin-dashboard.html)
├── checkin_service (RiwayatCheckin, CheckinController)
├── event_service (Acara, Kategori, Ulasan, AcaraController, create-event.html, organizer-dashboard.html, my-events.html, index.html)
├── notification_service (Notifikasi, NotifikasiController, notifications.html)
├── order_service (Pemesanan, DetailPemesanan, PemesananController, checkout.html, pembayaran.html, my-orders.html)
├── promo_service (KodePromo, PenggunaanPromo, PromoController)
├── queue_service (AntreanPembeli, QueueController, waiting-room.html)
├── ticket_service (KategoriTiket, Tiket, KategoriTiketController, TiketController, pilih-tiket.html, my-tickets.html)
├── user_service (Pengguna, ProfilPengguna, ResetPasswordOtp, AuthController, login.html, register.html, profile.html)
└── uploads/avatars
```

---
*Dokumen Serah Terima Teknis (Technical Handover Document) ini disahkan dan diserahkan secara resmi untuk kelanjutan pengembangan proyek TIKARA.*
