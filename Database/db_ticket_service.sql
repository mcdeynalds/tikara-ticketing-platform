--
-- PostgreSQL database dump
--

\restrict cCZ8qmtK94RMipVU7kQUKVcgCnJUAIfwm8fx9mXKoVXR5tQ3gGLAeUiIrRSx3cy

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:10:50

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

DROP DATABASE db_ticket_service;
--
-- TOC entry 2848 (class 1262 OID 19503)
-- Name: db_ticket_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_ticket_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict cCZ8qmtK94RMipVU7kQUKVcgCnJUAIfwm8fx9mXKoVXR5tQ3gGLAeUiIrRSx3cy
\connect db_ticket_service
\restrict cCZ8qmtK94RMipVU7kQUKVcgCnJUAIfwm8fx9mXKoVXR5tQ3gGLAeUiIrRSx3cy

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
-- TOC entry 203 (class 1259 OID 19506)
-- Name: kategori_tiket; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.kategori_tiket (
    id bigint NOT NULL,
    acara_id bigint NOT NULL,
    nama_kategori character varying(100) NOT NULL,
    harga numeric(12,2) NOT NULL,
    kuota integer NOT NULL,
    terjual integer DEFAULT 0 NOT NULL,
    direservasi integer DEFAULT 0 NOT NULL,
    mulai_jual timestamp without time zone NOT NULL,
    selesai_jual timestamp without time zone NOT NULL,
    status character varying(50) DEFAULT 'AVAILABLE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19504)
-- Name: kategori_tiket_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.kategori_tiket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2849 (class 0 OID 0)
-- Dependencies: 202
-- Name: kategori_tiket_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.kategori_tiket_id_seq OWNED BY public.kategori_tiket.id;


--
-- TOC entry 205 (class 1259 OID 19520)
-- Name: tiket; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tiket (
    id bigint NOT NULL,
    kategori_tiket_id bigint NOT NULL,
    detail_pemesanan_id bigint,
    pengguna_id bigint NOT NULL,
    kode_qr character varying(255) NOT NULL,
    status character varying(50) DEFAULT 'ISSUED'::character varying NOT NULL,
    diterbitkan_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    digunakan_at timestamp without time zone,
    deleted_at timestamp without time zone
);


--
-- TOC entry 204 (class 1259 OID 19518)
-- Name: tiket_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tiket_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2850 (class 0 OID 0)
-- Dependencies: 204
-- Name: tiket_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tiket_id_seq OWNED BY public.tiket.id;


--
-- TOC entry 2693 (class 2604 OID 19509)
-- Name: kategori_tiket id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kategori_tiket ALTER COLUMN id SET DEFAULT nextval('public.kategori_tiket_id_seq'::regclass);


--
-- TOC entry 2699 (class 2604 OID 19523)
-- Name: tiket id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tiket ALTER COLUMN id SET DEFAULT nextval('public.tiket_id_seq'::regclass);


--
-- TOC entry 2840 (class 0 OID 19506)
-- Dependencies: 203
-- Data for Name: kategori_tiket; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.kategori_tiket VALUES (16, 12, 'Front Row', 700000.00, 100, 0, 0, '2026-09-18 11:18:50', '2026-11-28 23:30:00', 'AVAILABLE', '2026-09-18 11:18:50.879556', '2026-09-18 11:18:50.879556');
INSERT INTO public.kategori_tiket VALUES (17, 12, 'General', 350000.00, 800, 0, 0, '2026-09-18 11:18:50', '2026-11-28 23:30:00', 'AVAILABLE', '2026-09-18 11:18:50.93929', '2026-09-18 11:18:50.93929');
INSERT INTO public.kategori_tiket VALUES (18, 13, 'VIP', 950000.00, 200, 0, 0, '2026-09-18 11:24:53', '2026-12-12 23:30:00', 'AVAILABLE', '2026-09-18 11:24:53.57518', '2026-09-18 11:24:53.575556');
INSERT INTO public.kategori_tiket VALUES (19, 13, 'Festival', 500000.00, 1200, 0, 0, '2026-09-18 11:24:53', '2026-12-12 23:30:00', 'AVAILABLE', '2026-09-18 11:24:53.631952', '2026-09-18 11:24:53.631952');
INSERT INTO public.kategori_tiket VALUES (20, 14, 'Premium', 750000.00, 150, 0, 0, '2026-09-18 11:57:35', '2026-11-21 21:30:00', 'AVAILABLE', '2026-09-18 11:57:35.248092', '2026-09-18 11:57:35.248092');
INSERT INTO public.kategori_tiket VALUES (21, 14, 'Reguler', 400000.00, 500, 0, 0, '2026-09-18 11:57:35', '2026-11-21 21:30:00', 'AVAILABLE', '2026-09-18 11:57:35.278054', '2026-09-18 11:57:35.278054');
INSERT INTO public.kategori_tiket VALUES (22, 15, 'VIP', 750000.00, 150, 0, 0, '2026-09-18 17:13:33', '2026-10-09 23:00:00', 'AVAILABLE', '2026-09-18 17:13:34.476825', '2026-09-18 17:13:34.476825');
INSERT INTO public.kategori_tiket VALUES (23, 15, 'Festival', 425000.00, 800, 0, 0, '2026-09-18 17:13:33', '2026-10-09 23:00:00', 'AVAILABLE', '2026-09-18 17:13:34.6993', '2026-09-18 17:13:34.6993');
INSERT INTO public.kategori_tiket VALUES (24, 15, 'Early Bird', 325000.00, 300, 0, 0, '2026-09-18 17:13:33', '2026-10-09 23:00:00', 'AVAILABLE', '2026-09-18 17:13:34.73377', '2026-09-18 17:13:34.73377');
INSERT INTO public.kategori_tiket VALUES (25, 16, 'Rockstar', 900000.00, 150, 0, 0, '2026-09-18 17:17:36', '2026-10-16 23:30:00', 'AVAILABLE', '2026-09-18 17:17:36.297718', '2026-09-18 17:17:36.297718');
INSERT INTO public.kategori_tiket VALUES (26, 16, 'Reguler', 500000.00, 100, 0, 0, '2026-09-18 17:17:36', '2026-10-16 23:30:00', 'AVAILABLE', '2026-09-18 17:17:36.428369', '2026-09-18 17:17:36.428369');
INSERT INTO public.kategori_tiket VALUES (27, 17, 'Premium', 850000.00, 200, 0, 0, '2026-09-18 17:21:12', '2026-10-23 23:00:00', 'AVAILABLE', '2026-09-18 17:21:12.246157', '2026-09-18 17:21:12.246157');
INSERT INTO public.kategori_tiket VALUES (28, 17, 'Reguler', 450000.00, 700, 0, 0, '2026-09-18 17:21:12', '2026-10-23 23:00:00', 'AVAILABLE', '2026-09-18 17:21:12.314692', '2026-09-18 17:21:12.314692');
INSERT INTO public.kategori_tiket VALUES (29, 18, 'VIP', 1250000.00, 250, 0, 0, '2026-09-18 17:24:04', '2026-11-01 01:00:00', 'AVAILABLE', '2026-09-18 17:24:04.817616', '2026-09-18 17:24:04.817616');
INSERT INTO public.kategori_tiket VALUES (30, 18, 'Festival', 650000.00, 1500, 0, 0, '2026-09-18 17:24:04', '2026-11-01 01:00:00', 'AVAILABLE', '2026-09-18 17:24:04.853373', '2026-09-18 17:24:04.853373');
INSERT INTO public.kategori_tiket VALUES (31, 19, 'Diamond', 1100000.00, 150, 0, 0, '2026-09-18 17:26:57', '2026-11-07 23:00:00', 'AVAILABLE', '2026-09-18 17:26:57.547446', '2026-09-18 17:26:57.547446');
INSERT INTO public.kategori_tiket VALUES (32, 19, 'Gold', 700000.00, 400, 0, 0, '2026-09-18 17:26:57', '2026-11-07 23:00:00', 'AVAILABLE', '2026-09-18 17:26:57.611435', '2026-09-18 17:26:57.611435');
INSERT INTO public.kategori_tiket VALUES (33, 19, 'Silver', 400000.00, 900, 0, 0, '2026-09-18 17:26:57', '2026-11-07 23:00:00', 'AVAILABLE', '2026-09-18 17:26:57.669165', '2026-09-18 17:26:57.669165');
INSERT INTO public.kategori_tiket VALUES (34, 20, 'VIP', 650000.00, 150, 0, 0, '2026-09-18 17:30:12', '2026-10-14 23:00:00', 'AVAILABLE', '2026-09-18 17:30:13.051328', '2026-09-18 17:30:13.051328');
INSERT INTO public.kategori_tiket VALUES (35, 20, 'Festival A', 325000.00, 1200, 0, 0, '2026-09-18 17:30:12', '2026-10-14 23:00:00', 'AVAILABLE', '2026-09-18 17:30:13.102395', '2026-09-18 17:30:13.102395');
INSERT INTO public.kategori_tiket VALUES (36, 21, 'Premium', 800000.00, 150, 0, 0, '2026-09-18 17:32:51', '2026-11-21 21:30:00', 'AVAILABLE', '2026-09-18 17:32:51.799268', '2026-09-18 17:32:51.799268');
INSERT INTO public.kategori_tiket VALUES (37, 21, 'Reguler', 450000.00, 600, 0, 0, '2026-09-18 17:32:51', '2026-11-21 21:30:00', 'AVAILABLE', '2026-09-18 17:32:51.836329', '2026-09-18 17:32:51.836329');
INSERT INTO public.kategori_tiket VALUES (38, 22, 'Sunset VIP', 950000.00, 200, 0, 0, '2026-09-18 17:35:46', '2026-11-28 22:30:00', 'AVAILABLE', '2026-09-18 17:35:46.771035', '2026-09-18 17:35:46.771035');
INSERT INTO public.kategori_tiket VALUES (39, 22, 'Festival', 550000.00, 1000, 0, 0, '2026-09-18 17:35:46', '2026-11-28 22:30:00', 'AVAILABLE', '2026-09-18 17:35:46.821259', '2026-09-18 17:35:46.821259');
INSERT INTO public.kategori_tiket VALUES (40, 23, 'Front Row', 700000.00, 150, 0, 0, '2026-09-18 17:38:23', '2026-12-05 23:00:00', 'AVAILABLE', '2026-09-18 17:38:23.813021', '2026-09-18 17:38:23.813021');
INSERT INTO public.kategori_tiket VALUES (41, 23, 'General', 350000.00, 900, 0, 0, '2026-09-18 17:38:23', '2026-12-05 23:00:00', 'AVAILABLE', '2026-09-18 17:38:23.853066', '2026-09-18 17:38:23.853066');
INSERT INTO public.kategori_tiket VALUES (42, 24, 'Rockstar', 850000.00, 200, 0, 0, '2026-09-18 17:40:46', '2026-12-12 23:45:00', 'AVAILABLE', '2026-09-18 17:40:46.269041', '2026-09-18 17:40:46.269041');
INSERT INTO public.kategori_tiket VALUES (43, 24, 'Festival', 475000.00, 1200, 0, 0, '2026-09-18 17:40:46', '2026-12-12 23:45:00', 'AVAILABLE', '2026-09-18 17:40:46.29757', '2026-09-18 17:40:46.29757');
INSERT INTO public.kategori_tiket VALUES (2, 1, 'Festival Regular', 650000.00, 300, 8, 0, '2026-09-01 00:00:00', '2026-10-24 23:59:59', 'AVAILABLE', '2026-09-12 07:58:11.207561', '2026-09-16 22:16:18.129338');
INSERT INTO public.kategori_tiket VALUES (1, 1, 'VIP Front Row', 1500000.00, 100, 8, 0, '2026-09-01 00:00:00', '2026-10-24 23:59:59', 'AVAILABLE', '2026-09-12 07:58:10.867673', '2026-09-17 00:04:17.824568');
INSERT INTO public.kategori_tiket VALUES (4, 6, 'Festival A', 650000.00, 500, 0, 0, '2026-09-17 01:17:54', '2026-12-18 23:00:00', 'AVAILABLE', '2026-09-17 01:17:54.851612', '2026-09-17 01:17:54.851612');
INSERT INTO public.kategori_tiket VALUES (3, 6, 'VIP', 1250000.00, 150, 4, 0, '2026-09-17 01:17:54', '2026-12-18 23:00:00', 'AVAILABLE', '2026-09-17 01:17:54.821256', '2026-09-17 01:22:11.501615');
INSERT INTO public.kategori_tiket VALUES (5, 7, 'VIP', 850000.00, 150, 0, 0, '2026-09-18 10:43:12', '2026-10-10 23:00:00', 'AVAILABLE', '2026-09-18 10:43:13.066607', '2026-09-18 10:43:13.066607');
INSERT INTO public.kategori_tiket VALUES (6, 7, 'Festival', 450000.00, 700, 0, 0, '2026-09-18 10:43:12', '2026-10-10 23:00:00', 'AVAILABLE', '2026-09-18 10:43:13.328849', '2026-09-18 10:43:13.328849');
INSERT INTO public.kategori_tiket VALUES (7, 7, 'Early Bird', 350000.00, 287, 0, 0, '2026-09-18 10:43:12', '2026-10-10 23:00:00', 'AVAILABLE', '2026-09-18 10:43:13.360249', '2026-09-18 10:43:13.360249');
INSERT INTO public.kategori_tiket VALUES (8, 8, 'Rockstar', 750000.00, 100, 0, 0, '2026-09-18 10:54:33', '2026-09-19 23:30:00', 'AVAILABLE', '2026-09-18 10:54:33.154318', '2026-09-18 10:54:33.154318');
INSERT INTO public.kategori_tiket VALUES (9, 8, 'Reguler', 400000.00, 800, 0, 0, '2026-09-18 10:54:33', '2026-09-19 23:30:00', 'AVAILABLE', '2026-09-18 10:54:33.190859', '2026-09-18 10:54:33.190859');
INSERT INTO public.kategori_tiket VALUES (10, 9, 'VIP Lounge', 950000.00, 120, 0, 0, '2026-09-18 11:00:06', '2026-10-31 23:00:00', 'AVAILABLE', '2026-09-18 11:00:06.714935', '2026-09-18 11:00:06.716397');
INSERT INTO public.kategori_tiket VALUES (11, 9, 'Reguler', 500000.00, 600, 0, 0, '2026-09-18 11:00:06', '2026-10-31 23:00:00', 'AVAILABLE', '2026-09-18 11:00:06.766855', '2026-09-18 11:00:06.766855');
INSERT INTO public.kategori_tiket VALUES (12, 10, 'VIP', 600000.00, 150, 0, 0, '2026-09-18 11:05:01', '2026-11-14 23:00:00', 'AVAILABLE', '2026-09-18 11:05:01.320214', '2026-09-18 11:05:01.320214');
INSERT INTO public.kategori_tiket VALUES (13, 10, 'Festival', 300000.00, 1000, 0, 0, '2026-09-18 11:05:01', '2026-11-14 23:00:00', 'AVAILABLE', '2026-09-18 11:05:01.379409', '2026-09-18 11:05:01.379409');
INSERT INTO public.kategori_tiket VALUES (14, 11, 'Sunset VIP', 1000000.00, 200, 0, 0, '2026-09-18 11:09:55', '2026-12-05 22:30:00', 'AVAILABLE', '2026-09-18 11:09:55.268382', '2026-09-18 11:09:55.268382');
INSERT INTO public.kategori_tiket VALUES (15, 11, 'Festival', 550000.00, 83, 0, 0, '2026-09-18 11:09:55', '2026-12-05 22:30:00', 'AVAILABLE', '2026-09-18 11:09:55.310543', '2026-09-18 11:09:55.310543');


--
-- TOC entry 2842 (class 0 OID 19520)
-- Dependencies: 205
-- Data for Name: tiket; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.tiket VALUES (1, 1, 1, 1, 'TK-408D72BD-3401', 'ISSUED', '2026-09-12 07:58:13.404048', NULL, NULL);
INSERT INTO public.tiket VALUES (2, 1, 1, 1, 'TK-C46EDF9B-3458', 'ISSUED', '2026-09-12 07:58:13.458357', NULL, NULL);
INSERT INTO public.tiket VALUES (6, 1, 5, 25, 'TK-3CCEFD72-7464', 'REFUNDED', '2026-09-13 20:55:17.467752', NULL, NULL);
INSERT INTO public.tiket VALUES (7, 1, 6, 25, 'TK-5236FFED-2998', 'USED', '2026-09-14 08:11:02.998979', '2026-09-14 08:13:30.917904', NULL);
INSERT INTO public.tiket VALUES (8, 1, 8, 26, 'TK-9453E9A9-3998', 'USED', '2026-09-14 08:54:24.002192', '2026-09-14 09:25:12.959645', NULL);
INSERT INTO public.tiket VALUES (4, 1, 3, 1, 'TK-DEFDFD42-5629', 'USED', '2026-09-12 15:47:45.648946', '2026-09-14 09:25:29.913896', NULL);
INSERT INTO public.tiket VALUES (3, 1, 2, 1, 'TK-6CC4F9D6-9328', 'USED', '2026-09-12 08:47:29.328725', '2026-09-14 09:25:35.043325', NULL);
INSERT INTO public.tiket VALUES (12, 2, 14, 25, 'TK-AC9FC368-3249', 'USED', '2026-09-14 10:11:33.249773', '2026-09-14 10:29:02.685343', NULL);
INSERT INTO public.tiket VALUES (13, 2, 13, 27, 'TK-45F3153A-3568', 'ISSUED', '2026-09-14 10:29:33.57045', NULL, NULL);
INSERT INTO public.tiket VALUES (11, 2, 13, 25, 'TK-4124F066-2814', 'TRANSFERRED', '2026-09-14 10:11:32.814933', '2026-09-14 10:29:33.568455', NULL);
INSERT INTO public.tiket VALUES (10, 2, 10, 25, 'TK-E770F184-4438', 'USED', '2026-09-14 10:06:24.438316', '2026-09-14 10:29:34.216282', NULL);
INSERT INTO public.tiket VALUES (9, 2, 9, 25, 'TK-964E85D0-3779', 'TRANSFERRED', '2026-09-14 10:06:23.784034', '2026-09-14 10:37:41.49759', NULL);
INSERT INTO public.tiket VALUES (14, 2, 9, 27, 'TK-5DC305DA-1497', 'USED', '2026-09-14 10:37:41.49759', '2026-09-14 10:38:06.071483', NULL);
INSERT INTO public.tiket VALUES (15, 2, 17, 25, 'TK-FE524572-7485', 'TRANSFERRED', '2026-09-14 10:37:47.486202', '2026-09-14 10:38:59.260184', NULL);
INSERT INTO public.tiket VALUES (17, 2, 18, 25, 'TK-605390A5-4645', 'ISSUED', '2026-09-14 10:39:04.645584', NULL, NULL);
INSERT INTO public.tiket VALUES (16, 2, 17, 27, 'TK-0F695409-9260', 'USED', '2026-09-14 10:38:59.260184', '2026-09-14 10:39:22.400442', NULL);
INSERT INTO public.tiket VALUES (18, 2, 19, 26, 'TK-0E4E8DB4-4521', 'ISSUED', '2026-09-14 11:24:34.526513', NULL, NULL);
INSERT INTO public.tiket VALUES (19, 2, 20, 29, 'TK-C3F357D8-8270', 'ISSUED', '2026-09-16 22:16:18.291452', NULL, NULL);
INSERT INTO public.tiket VALUES (20, 1, 21, 32, 'TK-24EF2C24-7889', 'ISSUED', '2026-09-17 00:04:17.895003', NULL, NULL);
INSERT INTO public.tiket VALUES (5, 1, 4, 1, 'TK-6F548496-9448', 'USED', '2026-09-12 15:49:49.448198', '2026-09-17 00:20:37.502332', NULL);
INSERT INTO public.tiket VALUES (21, 3, 22, 29, 'TK-97F05805-1535', 'ISSUED', '2026-09-17 01:22:11.535231', NULL, NULL);
INSERT INTO public.tiket VALUES (22, 3, 22, 29, 'TK-C97114B8-1566', 'ISSUED', '2026-09-17 01:22:11.566224', NULL, NULL);
INSERT INTO public.tiket VALUES (23, 3, 22, 29, 'TK-C3049FDB-1591', 'ISSUED', '2026-09-17 01:22:11.591963', NULL, NULL);
INSERT INTO public.tiket VALUES (24, 3, 22, 29, 'TK-515768F1-1638', 'ISSUED', '2026-09-17 01:22:11.638706', NULL, NULL);


--
-- TOC entry 2851 (class 0 OID 0)
-- Dependencies: 202
-- Name: kategori_tiket_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.kategori_tiket_id_seq', 43, true);


--
-- TOC entry 2852 (class 0 OID 0)
-- Dependencies: 204
-- Name: tiket_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tiket_id_seq', 24, true);


--
-- TOC entry 2704 (class 2606 OID 19516)
-- Name: kategori_tiket kategori_tiket_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kategori_tiket
    ADD CONSTRAINT kategori_tiket_pkey PRIMARY KEY (id);


--
-- TOC entry 2709 (class 2606 OID 19529)
-- Name: tiket tiket_kode_qr_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tiket
    ADD CONSTRAINT tiket_kode_qr_key UNIQUE (kode_qr);


--
-- TOC entry 2711 (class 2606 OID 19527)
-- Name: tiket tiket_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tiket
    ADD CONSTRAINT tiket_pkey PRIMARY KEY (id);


--
-- TOC entry 2702 (class 1259 OID 19517)
-- Name: idx_kategori_tiket_acara; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kategori_tiket_acara ON public.kategori_tiket USING btree (acara_id);


--
-- TOC entry 2705 (class 1259 OID 19809)
-- Name: idx_tiket_detail_pemesanan; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tiket_detail_pemesanan ON public.tiket USING btree (detail_pemesanan_id);


--
-- TOC entry 2706 (class 1259 OID 19536)
-- Name: idx_tiket_kode_qr; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tiket_kode_qr ON public.tiket USING btree (kode_qr);


--
-- TOC entry 2707 (class 1259 OID 19535)
-- Name: idx_tiket_pengguna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tiket_pengguna ON public.tiket USING btree (pengguna_id);


--
-- TOC entry 2712 (class 2606 OID 19530)
-- Name: tiket fk_tiket_kategori_tiket; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tiket
    ADD CONSTRAINT fk_tiket_kategori_tiket FOREIGN KEY (kategori_tiket_id) REFERENCES public.kategori_tiket(id) ON DELETE CASCADE;


-- Completed on 2026-09-18 21:10:53

--
-- PostgreSQL database dump complete
--

\unrestrict cCZ8qmtK94RMipVU7kQUKVcgCnJUAIfwm8fx9mXKoVXR5tQ3gGLAeUiIrRSx3cy

