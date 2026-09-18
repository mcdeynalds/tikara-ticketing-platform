--
-- PostgreSQL database dump
--

\restrict J0s6AjYazwlU8S8Nj3Uofl8uvyqqfAygp3lrzioYTrrPBAgifs7buNLqb5Csqex

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:06:17

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

DROP DATABASE db_admin_service;
--
-- TOC entry 2841 (class 1262 OID 19671)
-- Name: db_admin_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_admin_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict J0s6AjYazwlU8S8Nj3Uofl8uvyqqfAygp3lrzioYTrrPBAgifs7buNLqb5Csqex
\connect db_admin_service
\restrict J0s6AjYazwlU8S8Nj3Uofl8uvyqqfAygp3lrzioYTrrPBAgifs7buNLqb5Csqex

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
-- TOC entry 203 (class 1259 OID 19674)
-- Name: audit_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_log (
    id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    aksi character varying(100) NOT NULL,
    modul character varying(100) NOT NULL,
    detail text,
    ip_address character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19672)
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2842 (class 0 OID 0)
-- Dependencies: 202
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- TOC entry 205 (class 1259 OID 19688)
-- Name: laporan_penjualan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.laporan_penjualan (
    id bigint NOT NULL,
    acara_id bigint NOT NULL,
    total_tiket_terjual integer DEFAULT 0 NOT NULL,
    total_pendapatan numeric(14,2) DEFAULT 0 NOT NULL,
    periode character varying(50) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 204 (class 1259 OID 19686)
-- Name: laporan_penjualan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.laporan_penjualan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2843 (class 0 OID 0)
-- Dependencies: 204
-- Name: laporan_penjualan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.laporan_penjualan_id_seq OWNED BY public.laporan_penjualan.id;


--
-- TOC entry 2694 (class 2604 OID 19677)
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- TOC entry 2696 (class 2604 OID 19691)
-- Name: laporan_penjualan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.laporan_penjualan ALTER COLUMN id SET DEFAULT nextval('public.laporan_penjualan_id_seq'::regclass);


--
-- TOC entry 2833 (class 0 OID 19674)
-- Dependencies: 203
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.audit_log VALUES (1, 29, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789582918-53F1 total Rp 5055000.00 status: PAID', '127.0.0.1', '2026-09-17 01:21:58.765925');
INSERT INTO public.audit_log VALUES (2, 24, 'PUBLISH_EVENT', 'EVENT', 'Publikasi konser: "Senja di Bandung 2026 - Indie Music Festival" (Status: PUBLISHED)', '127.0.0.1', '2026-09-17 01:17:54.456647');
INSERT INTO public.audit_log VALUES (3, 32, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789578244-51C1 total Rp 1485000.00 status: PAID', '127.0.0.1', '2026-09-17 00:04:04.555541');
INSERT INTO public.audit_log VALUES (4, 32, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: amiiperii@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-17 00:01:36.133795');
INSERT INTO public.audit_log VALUES (5, 29, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789571746-FF03 total Rp 668000.00 status: PAID', '127.0.0.1', '2026-09-16 22:15:46.760397');
INSERT INTO public.audit_log VALUES (6, 31, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: buditest@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-16 15:15:40.247991');
INSERT INTO public.audit_log VALUES (7, 30, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: organizer.test@gmail.com (Peran: PENYELENGGARA)', '127.0.0.1', '2026-09-15 15:58:36.985775');
INSERT INTO public.audit_log VALUES (8, 29, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: huljannahmiftah98@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-15 11:17:42.285913');
INSERT INTO public.audit_log VALUES (9, 26, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789359818-F9F0 total Rp 618000.00 status: PAID', '127.0.0.1', '2026-09-14 11:23:38.251872');
INSERT INTO public.audit_log VALUES (10, 28, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: rudi.test@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-14 11:07:13.135341');
INSERT INTO public.audit_log VALUES (11, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789357144-E0B1 total Rp 668000.00 status: PAID', '127.0.0.1', '2026-09-14 10:39:04.502102');
INSERT INTO public.audit_log VALUES (12, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789357067-F5DF total Rp 668000.00 status: PAID', '127.0.0.1', '2026-09-14 10:37:47.096964');
INSERT INTO public.audit_log VALUES (13, 27, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: siti.test@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-14 10:28:40.620298');
INSERT INTO public.audit_log VALUES (14, 25, 'ORDER_CANCELLED', 'PEMESANAN', 'Pesanan #ORD-1789355496-C25B total Rp 1994000.00 status: CANCELLED', '127.0.0.1', '2026-09-14 10:11:36.431207');
INSERT INTO public.audit_log VALUES (15, 25, 'ORDER_CANCELLED', 'PEMESANAN', 'Pesanan #ORD-1789355493-BCEE total Rp 1331000.00 status: CANCELLED', '127.0.0.1', '2026-09-14 10:11:33.448982');
INSERT INTO public.audit_log VALUES (16, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789355493-4801 total Rp 668000.00 status: PAID', '127.0.0.1', '2026-09-14 10:11:33.117621');
INSERT INTO public.audit_log VALUES (17, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789355492-D292 total Rp 618000.00 status: PAID', '127.0.0.1', '2026-09-14 10:11:32.503373');
INSERT INTO public.audit_log VALUES (18, 25, 'ORDER_CANCELLED', 'PEMESANAN', 'Pesanan #ORD-1789355188-C71B total Rp 1994000.00 status: CANCELLED', '127.0.0.1', '2026-09-14 10:06:28.213837');
INSERT INTO public.audit_log VALUES (19, 25, 'ORDER_CANCELLED', 'PEMESANAN', 'Pesanan #ORD-1789355184-04F0 total Rp 1331000.00 status: CANCELLED', '127.0.0.1', '2026-09-14 10:06:24.603934');
INSERT INTO public.audit_log VALUES (20, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789355184-7F66 total Rp 668000.00 status: PAID', '127.0.0.1', '2026-09-14 10:06:24.315982');
INSERT INTO public.audit_log VALUES (21, 25, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789355181-1BD1 total Rp 618000.00 status: PAID', '127.0.0.1', '2026-09-14 10:06:21.989223');
INSERT INTO public.audit_log VALUES (22, 26, 'PAYMENT_SUCCESS', 'PEMESANAN', 'Pesanan #ORD-1789350811-A29A total Rp 1535000.00 status: PAID', '127.0.0.1', '2026-09-14 08:53:31.382075');
INSERT INTO public.audit_log VALUES (23, 26, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: testuser88@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-13 21:17:35.859566');
INSERT INTO public.audit_log VALUES (24, 25, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: budi.test@gmail.com (Peran: PEMBELI)', '127.0.0.1', '2026-09-12 08:53:50.967911');
INSERT INTO public.audit_log VALUES (25, 24, 'PUBLISH_EVENT', 'EVENT', 'Publikasi konser: "Konser Musik Harmoni Nusantara 2026" (Status: PUBLISHED)', '127.0.0.1', '2026-09-12 07:58:10.032303');
INSERT INTO public.audit_log VALUES (26, 24, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: deyaminfo@gmail.com (Peran: PENYELENGGARA)', '127.0.0.1', '2026-09-05 11:24:46.631474');
INSERT INTO public.audit_log VALUES (27, 23, 'USER_REGISTER', 'AUTH', 'Registrasi akun pengguna: deapriliart@gmail.com (Peran: ADMIN)', '127.0.0.1', '2026-09-05 10:17:32.000964');


--
-- TOC entry 2835 (class 0 OID 19688)
-- Dependencies: 205
-- Data for Name: laporan_penjualan; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.laporan_penjualan VALUES (1, 1, 16, 17200000.00, 'September 2026', '2026-09-17 06:31:44.815348');
INSERT INTO public.laporan_penjualan VALUES (2, 6, 4, 5000000.00, 'September 2026', '2026-09-17 06:31:44.948681');


--
-- TOC entry 2844 (class 0 OID 0)
-- Dependencies: 202
-- Name: audit_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.audit_log_id_seq', 27, true);


--
-- TOC entry 2845 (class 0 OID 0)
-- Dependencies: 204
-- Name: laporan_penjualan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.laporan_penjualan_id_seq', 2, true);


--
-- TOC entry 2701 (class 2606 OID 19683)
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- TOC entry 2705 (class 2606 OID 19696)
-- Name: laporan_penjualan laporan_penjualan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.laporan_penjualan
    ADD CONSTRAINT laporan_penjualan_pkey PRIMARY KEY (id);


--
-- TOC entry 2702 (class 1259 OID 19685)
-- Name: idx_audit_log_modul; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_log_modul ON public.audit_log USING btree (modul);


--
-- TOC entry 2703 (class 1259 OID 19684)
-- Name: idx_audit_log_pengguna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_audit_log_pengguna ON public.audit_log USING btree (pengguna_id);


-- Completed on 2026-09-18 21:06:20

--
-- PostgreSQL database dump complete
--

\unrestrict J0s6AjYazwlU8S8Nj3Uofl8uvyqqfAygp3lrzioYTrrPBAgifs7buNLqb5Csqex

