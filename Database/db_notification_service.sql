--
-- PostgreSQL database dump
--

\restrict HU51Upmtn3cwLS2I4cXHhQAAlcWLP8KanAAkVssHAgoYpnGPZu4QDROtJavfYmq

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:09:21

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE db_notification_service;
--
-- TOC entry 2830 (class 1262 OID 19609)
-- Name: db_notification_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_notification_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict HU51Upmtn3cwLS2I4cXHhQAAlcWLP8KanAAkVssHAgoYpnGPZu4QDROtJavfYmq
\connect db_notification_service
\restrict HU51Upmtn3cwLS2I4cXHhQAAlcWLP8KanAAkVssHAgoYpnGPZu4QDROtJavfYmq

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

-- *not* creating schema, since initdb creates it


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 203 (class 1259 OID 19612)
-- Name: notifikasi; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifikasi (
    id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    judul character varying(255) NOT NULL,
    pesan text NOT NULL,
    tipe character varying(50) DEFAULT 'EMAIL'::character varying NOT NULL,
    status character varying(50) DEFAULT 'SENT'::character varying NOT NULL,
    dibaca boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19610)
-- Name: notifikasi_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifikasi_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2831 (class 0 OID 0)
-- Dependencies: 202
-- Name: notifikasi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifikasi_id_seq OWNED BY public.notifikasi.id;


--
-- TOC entry 2688 (class 2604 OID 19615)
-- Name: notifikasi id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifikasi ALTER COLUMN id SET DEFAULT nextval('public.notifikasi_id_seq'::regclass);


--
-- TOC entry 2824 (class 0 OID 19612)
-- Dependencies: 203
-- Data for Name: notifikasi; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.notifikasi VALUES (1, 1, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789177647-4E9C telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-12 08:47:28.41607');
INSERT INTO public.notifikasi VALUES (3, 1, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789202833-F4A2 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-12 15:47:14.601891');
INSERT INTO public.notifikasi VALUES (4, 1, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789202833-F4A2 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-12 15:47:45.850528');
INSERT INTO public.notifikasi VALUES (5, 1, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789202947-7B05 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-12 15:49:08.19166');
INSERT INTO public.notifikasi VALUES (6, 1, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789202947-7B05 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-12 15:49:49.745325');
INSERT INTO public.notifikasi VALUES (7, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789307715-99A0 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-13 20:55:17.157351');
INSERT INTO public.notifikasi VALUES (8, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789307715-99A0 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-13 20:55:17.522667');
INSERT INTO public.notifikasi VALUES (9, 25, 'Permintaan Refund Diterima', 'Permintaan refund untuk pesanan #ORD-1789307715-99A0 sebesar Rp 1350000.00 telah kami terima dan sedang ditinjau oleh Admin (potongan adm 10%).', 'IN_APP', 'SENT', false, '2026-09-13 20:55:17.610286');
INSERT INTO public.notifikasi VALUES (10, 25, 'Refund Disetujui!', 'Permintaan refund untuk pesanan #ORD-1789307715-99A0 sebesar Rp 1350000.00 telah disetujui. Dana akan dikembalikan via metode pembayaran awal dalam 3-7 hari kerja.', 'IN_APP', 'SENT', false, '2026-09-13 20:55:18.215429');
INSERT INTO public.notifikasi VALUES (11, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789348262-9E07 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 08:11:02.910466');
INSERT INTO public.notifikasi VALUES (12, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789348262-9E07 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 08:11:03.143763');
INSERT INTO public.notifikasi VALUES (13, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789348326-C573 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 08:12:06.364776');
INSERT INTO public.notifikasi VALUES (14, 25, 'Pesanan Dibatalkan', 'Pesanan #ORD-1789348326-C573 telah dibatalkan.', 'IN_APP', 'SENT', false, '2026-09-14 08:12:23.175944');
INSERT INTO public.notifikasi VALUES (15, 26, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789350811-A29A telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 08:53:32.113769');
INSERT INTO public.notifikasi VALUES (16, 26, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789350811-A29A telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 08:54:24.079613');
INSERT INTO public.notifikasi VALUES (17, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355181-1BD1 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:23.351608');
INSERT INTO public.notifikasi VALUES (18, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789355181-1BD1 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:23.851601');
INSERT INTO public.notifikasi VALUES (19, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355184-7F66 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:24.36922');
INSERT INTO public.notifikasi VALUES (20, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789355184-7F66 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:24.451449');
INSERT INTO public.notifikasi VALUES (21, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355184-04F0 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:24.697064');
INSERT INTO public.notifikasi VALUES (22, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355188-C71B telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:06:28.263917');
INSERT INTO public.notifikasi VALUES (23, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355492-D292 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:32.654975');
INSERT INTO public.notifikasi VALUES (24, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789355492-D292 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:32.837608');
INSERT INTO public.notifikasi VALUES (25, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355493-4801 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:33.176911');
INSERT INTO public.notifikasi VALUES (26, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789355493-4801 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:33.264029');
INSERT INTO public.notifikasi VALUES (27, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355493-BCEE telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:33.558305');
INSERT INTO public.notifikasi VALUES (28, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789355496-C25B telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:11:36.497397');
INSERT INTO public.notifikasi VALUES (29, 25, 'Transfer Tiket Berhasil', 'Tiket ID #TKT-11 telah berhasil dipindahtangankan kepada Siti Pembeli (siti.test@gmail.com).', 'IN_APP', 'SENT', false, '2026-09-14 10:29:33.85629');
INSERT INTO public.notifikasi VALUES (30, 27, 'Tiket Konser Diterima!', 'Selamat! Anda menerima transfer tiket resmi (ID #TKT-13) dari budi.test@gmail.com. Buka E-Tiket Anda sekarang.', 'IN_APP', 'SENT', false, '2026-09-14 10:29:34.089419');
INSERT INTO public.notifikasi VALUES (31, 25, 'Transfer Tiket Berhasil', 'Tiket ID #TKT-9 telah berhasil dipindahtangankan kepada Siti Pembeli (siti.test@gmail.com).', 'IN_APP', 'SENT', false, '2026-09-14 10:37:41.506116');
INSERT INTO public.notifikasi VALUES (32, 27, 'Tiket Konser Diterima!', 'Selamat! Anda menerima transfer tiket resmi (ID #TKT-14) dari budi.test@gmail.com. Buka E-Tiket Anda sekarang.', 'IN_APP', 'SENT', false, '2026-09-14 10:37:41.521109');
INSERT INTO public.notifikasi VALUES (33, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789357067-F5DF telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:37:47.364699');
INSERT INTO public.notifikasi VALUES (34, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789357067-F5DF telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:37:47.510072');
INSERT INTO public.notifikasi VALUES (35, 25, 'Transfer Tiket Berhasil', 'Tiket ID #TKT-15 telah berhasil dipindahtangankan kepada Siti Pembeli (siti.test@gmail.com).', 'IN_APP', 'SENT', false, '2026-09-14 10:38:59.268003');
INSERT INTO public.notifikasi VALUES (36, 27, 'Tiket Konser Diterima!', 'Selamat! Anda menerima transfer tiket resmi (ID #TKT-16) dari budi.test@gmail.com. Buka E-Tiket Anda sekarang.', 'IN_APP', 'SENT', false, '2026-09-14 10:38:59.282744');
INSERT INTO public.notifikasi VALUES (37, 25, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789357144-E0B1 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 10:39:04.584967');
INSERT INTO public.notifikasi VALUES (38, 25, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789357144-E0B1 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 10:39:04.661338');
INSERT INTO public.notifikasi VALUES (39, 26, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789359818-F9F0 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-14 11:23:40.324411');
INSERT INTO public.notifikasi VALUES (40, 26, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789359818-F9F0 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-14 11:24:34.712423');
INSERT INTO public.notifikasi VALUES (41, 1, 'E-Tiket Resmi: Konser Musik Harmoni Nusantara 2026', 'E-Tiket resmi Anda (#TKT-5) untuk konser Konser Musik Harmoni Nusantara 2026 telah siap. Bawa identitas e-KTP Anda ke pintu gate venue konser.', 'EMAIL', 'SENT', false, '2026-09-15 08:00:28.698518');
INSERT INTO public.notifikasi VALUES (42, 1, 'E-Tiket Resmi: Konser Musik Harmoni Nusantara 2026', 'E-Tiket resmi Anda (#TKT-1) untuk konser Konser Musik Harmoni Nusantara 2026 telah siap. Bawa identitas e-KTP Anda ke pintu gate venue konser.', 'EMAIL', 'SENT', false, '2026-09-15 12:07:53.781598');
INSERT INTO public.notifikasi VALUES (43, 1, 'E-Tiket Resmi: Konser Musik Harmoni Nusantara 2026', 'E-Tiket resmi Anda (#TKT-1) untuk konser Konser Musik Harmoni Nusantara 2026 telah siap. Bawa identitas e-KTP Anda ke pintu gate venue konser.', 'EMAIL', 'SENT', false, '2026-09-15 12:09:30.605233');
INSERT INTO public.notifikasi VALUES (44, 1, 'E-Tiket Resmi: Konser Musik Harmoni Nusantara 2026', 'E-Tiket resmi Anda (#TKT-5) untuk konser Konser Musik Harmoni Nusantara 2026 telah siap. Bawa identitas e-KTP Anda ke pintu gate venue konser.', 'EMAIL', 'SENT', false, '2026-09-15 12:20:45.440225');
INSERT INTO public.notifikasi VALUES (2, 1, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789177647-4E9C telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', true, '2026-09-12 08:47:29.406087');
INSERT INTO public.notifikasi VALUES (45, 1, 'E-Tiket Resmi: Konser Musik Harmoni Nusantara 2026', 'E-Tiket resmi Anda (#TKT-5) untuk konser Konser Musik Harmoni Nusantara 2026 telah siap. Bawa identitas e-KTP Anda ke pintu gate venue konser.', 'EMAIL', 'SENT', false, '2026-09-15 15:04:45.255863');
INSERT INTO public.notifikasi VALUES (46, 32, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789578244-51C1 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-17 00:04:06.765888');
INSERT INTO public.notifikasi VALUES (47, 32, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789578244-51C1 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-17 00:04:18.119759');
INSERT INTO public.notifikasi VALUES (48, 32, 'E-Tiket PDF Terkirim ke Email', 'E-Tiket resmi (#TKT-20) telah berhasil dikirimkan ke alamat email amiiperii@gmail.com beserta lampiran PDF ber-QR Code.', 'IN_APP', 'SENT', true, '2026-09-17 00:04:33.5975');
INSERT INTO public.notifikasi VALUES (49, 32, 'E-Tiket PDF Terkirim ke Email', 'E-Tiket resmi (#TKT-20) telah berhasil dikirimkan ke alamat email amiiperii@gmail.com beserta lampiran PDF ber-QR Code.', 'IN_APP', 'SENT', false, '2026-09-17 00:15:48.628839');
INSERT INTO public.notifikasi VALUES (50, 1, 'E-Tiket PDF Terkirim ke Email', 'E-Tiket resmi (#TKT-5) telah berhasil dikirimkan ke alamat email moondaisy938@gmail.com beserta lampiran PDF ber-QR Code.', 'IN_APP', 'SENT', false, '2026-09-17 00:18:03.731977');
INSERT INTO public.notifikasi VALUES (51, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Senja di Bandung 2026 - Indie Music Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-17 00:52:39.782808');
INSERT INTO public.notifikasi VALUES (52, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Senja di Bandung 2026 - Indie Music Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-17 00:52:54.997487');
INSERT INTO public.notifikasi VALUES (53, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Senja di Bandung 2026 - Indie Music Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-17 00:53:48.528798');
INSERT INTO public.notifikasi VALUES (54, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Senja di Bandung 2026 - Indie Music Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-17 01:05:53.633772');
INSERT INTO public.notifikasi VALUES (55, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Senja di Bandung 2026 - Indie Music Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-17 01:17:54.584579');
INSERT INTO public.notifikasi VALUES (56, 29, 'Pesanan Berhasil Dibuat', 'Pesanan #ORD-1789582918-53F1 telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.', 'IN_APP', 'SENT', false, '2026-09-17 01:21:58.878545');
INSERT INTO public.notifikasi VALUES (57, 29, 'Pembayaran Berhasil! E-Tiket Diterbitkan', 'Pembayaran pesanan #ORD-1789582918-53F1 telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.', 'IN_APP', 'SENT', false, '2026-09-17 01:22:11.691141');
INSERT INTO public.notifikasi VALUES (58, 29, 'E-Tiket PDF Terkirim ke Email', 'E-Tiket resmi (#TKT-24) telah berhasil dikirimkan ke alamat email huljannahmiftah98@gmail.com beserta lampiran PDF ber-QR Code.', 'IN_APP', 'SENT', false, '2026-09-17 01:22:41.814449');
INSERT INTO public.notifikasi VALUES (59, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Midnight Stories: Jakarta Indie Night''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 10:43:12.382675');
INSERT INTO public.notifikasi VALUES (60, 33, 'Event Disetujui!', 'Selamat! Event Anda ''Midnight Stories: Jakarta Indie Night'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 10:45:11.942818');
INSERT INTO public.notifikasi VALUES (61, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Bandung Noise Assembly 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 10:54:33.084977');
INSERT INTO public.notifikasi VALUES (62, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Surabaya After Dark Jazz Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:00:06.598375');
INSERT INTO public.notifikasi VALUES (63, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Jogja Berdendang 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:05:01.190603');
INSERT INTO public.notifikasi VALUES (64, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Sunset Sound Bali 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:09:55.123072');
INSERT INTO public.notifikasi VALUES (65, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Northside Hip-Hop Festival 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:18:50.700227');
INSERT INTO public.notifikasi VALUES (66, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Ruang Pulang: Jakarta Indie Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:24:53.444995');
INSERT INTO public.notifikasi VALUES (67, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Bandung Classical Evening''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 11:57:35.151412');
INSERT INTO public.notifikasi VALUES (68, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Lembayung Bandung: Indie Weekend''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:13:33.127282');
INSERT INTO public.notifikasi VALUES (69, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Jakarta Rock Revival 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:17:36.07631');
INSERT INTO public.notifikasi VALUES (70, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Jogja Moonlight Jazz''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:21:12.041082');
INSERT INTO public.notifikasi VALUES (71, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Electric District Jakarta''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:24:04.71137');
INSERT INTO public.notifikasi VALUES (72, 1, 'Event Baru Menunggu Review', 'Penyelenggara deyaminfo@gmail.com telah mendaftarkan event ''Surabaya Pop Parade 2026''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:26:57.37872');
INSERT INTO public.notifikasi VALUES (73, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Medan Bergoyang Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:30:12.889139');
INSERT INTO public.notifikasi VALUES (74, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Symphony Under The Stars''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:32:51.661385');
INSERT INTO public.notifikasi VALUES (75, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Island Echoes Bali''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:35:46.548484');
INSERT INTO public.notifikasi VALUES (76, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''Southside Hip-Hop Weekend''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:38:23.695642');
INSERT INTO public.notifikasi VALUES (77, 1, 'Event Baru Menunggu Review', 'Penyelenggara deafriliart@gmail.com telah mendaftarkan event ''After Midnight Rock Festival''. Silakan periksa di Hub Moderasi.', 'IN_APP', 'SENT', false, '2026-09-18 17:40:46.168107');
INSERT INTO public.notifikasi VALUES (78, 33, 'Event Disetujui!', 'Selamat! Event Anda ''Medan Bergoyang Festival'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:41:33.899131');
INSERT INTO public.notifikasi VALUES (79, 24, 'Event Disetujui!', 'Selamat! Event Anda ''Jakarta Rock Revival 2026'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:41:40.006711');
INSERT INTO public.notifikasi VALUES (80, 24, 'Event Disetujui!', 'Selamat! Event Anda ''Jogja Moonlight Jazz'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:41:45.496722');
INSERT INTO public.notifikasi VALUES (81, 24, 'Event Disetujui!', 'Selamat! Event Anda ''Electric District Jakarta'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:41:50.829501');
INSERT INTO public.notifikasi VALUES (82, 33, 'Event Disetujui!', 'Selamat! Event Anda ''Symphony Under The Stars'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:41:55.848117');
INSERT INTO public.notifikasi VALUES (83, 33, 'Event Disetujui!', 'Selamat! Event Anda ''Island Echoes Bali'' telah diverifikasi dan kini PUBLISHED di platform TIKARA.', 'IN_APP', 'SENT', false, '2026-09-18 17:42:01.84688');


--
-- TOC entry 2832 (class 0 OID 0)
-- Dependencies: 202
-- Name: notifikasi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notifikasi_id_seq', 83, true);


--
-- TOC entry 2696 (class 2606 OID 19624)
-- Name: notifikasi notifikasi_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifikasi
    ADD CONSTRAINT notifikasi_pkey PRIMARY KEY (id);


--
-- TOC entry 2693 (class 1259 OID 19626)
-- Name: idx_notifikasi_dibaca; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifikasi_dibaca ON public.notifikasi USING btree (dibaca);


--
-- TOC entry 2694 (class 1259 OID 19625)
-- Name: idx_notifikasi_pengguna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notifikasi_pengguna ON public.notifikasi USING btree (pengguna_id);


-- Completed on 2026-09-18 21:09:24

--
-- PostgreSQL database dump complete
--

\unrestrict HU51Upmtn3cwLS2I4cXHhQAAlcWLP8KanAAkVssHAgoYpnGPZu4QDROtJavfYmq

