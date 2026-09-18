--
-- PostgreSQL database dump
--

\restrict H9SRgpdqVGu7QjiOdBEtZ4AsLskki7nXCpyFptzqk7gEOo9GVhdAEaPKHosxRQu

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:09:51

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

DROP DATABASE db_order_service;
--
-- TOC entry 2864 (class 1262 OID 19537)
-- Name: db_order_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_order_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict H9SRgpdqVGu7QjiOdBEtZ4AsLskki7nXCpyFptzqk7gEOo9GVhdAEaPKHosxRQu
\connect db_order_service
\restrict H9SRgpdqVGu7QjiOdBEtZ4AsLskki7nXCpyFptzqk7gEOo9GVhdAEaPKHosxRQu

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
-- TOC entry 205 (class 1259 OID 19583)
-- Name: detail_pemesanan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.detail_pemesanan (
    id bigint NOT NULL,
    pemesanan_id bigint NOT NULL,
    kategori_tiket_id bigint NOT NULL,
    jumlah integer NOT NULL,
    harga_satuan numeric(12,2) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    nama_kategori_tiket character varying(255)
);


--
-- TOC entry 204 (class 1259 OID 19581)
-- Name: detail_pemesanan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.detail_pemesanan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2865 (class 0 OID 0)
-- Dependencies: 204
-- Name: detail_pemesanan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.detail_pemesanan_id_seq OWNED BY public.detail_pemesanan.id;


--
-- TOC entry 203 (class 1259 OID 19568)
-- Name: pemesanan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pemesanan (
    id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    nomor_pesanan character varying(255) NOT NULL,
    total_harga numeric(12,2) NOT NULL,
    status character varying(255) DEFAULT 'PENDING'::character varying NOT NULL,
    tenggat_pembayaran timestamp without time zone NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone,
    subtotal_tiket numeric(12,2),
    biaya_layanan numeric(12,2),
    biaya_admin numeric(12,2),
    diskon numeric(12,2) DEFAULT 0,
    kode_promo character varying(255),
    refund_status character varying(255),
    refund_alasan character varying(255),
    refund_amount numeric(12,2),
    refund_requested_at timestamp without time zone
);


--
-- TOC entry 202 (class 1259 OID 19566)
-- Name: pemesanan_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pemesanan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2866 (class 0 OID 0)
-- Dependencies: 202
-- Name: pemesanan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pemesanan_id_seq OWNED BY public.pemesanan.id;


--
-- TOC entry 207 (class 1259 OID 19788)
-- Name: refund_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refund_request (
    id bigint NOT NULL,
    pemesanan_id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    nomor_pesanan character varying(255),
    alasan text NOT NULL,
    jumlah_refund numeric(12,2) NOT NULL,
    potongan_admin numeric(12,2) DEFAULT 0 NOT NULL,
    status character varying(255) DEFAULT 'PENDING_REVIEW'::character varying NOT NULL,
    catatan_admin text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    processed_at timestamp without time zone
);


--
-- TOC entry 206 (class 1259 OID 19786)
-- Name: refund_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refund_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2867 (class 0 OID 0)
-- Dependencies: 206
-- Name: refund_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refund_request_id_seq OWNED BY public.refund_request.id;


--
-- TOC entry 2706 (class 2604 OID 19586)
-- Name: detail_pemesanan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detail_pemesanan ALTER COLUMN id SET DEFAULT nextval('public.detail_pemesanan_id_seq'::regclass);


--
-- TOC entry 2701 (class 2604 OID 19571)
-- Name: pemesanan id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pemesanan ALTER COLUMN id SET DEFAULT nextval('public.pemesanan_id_seq'::regclass);


--
-- TOC entry 2707 (class 2604 OID 19791)
-- Name: refund_request id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refund_request ALTER COLUMN id SET DEFAULT nextval('public.refund_request_id_seq'::regclass);


--
-- TOC entry 2856 (class 0 OID 19583)
-- Dependencies: 205
-- Data for Name: detail_pemesanan; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.detail_pemesanan VALUES (1, 1, 1, 2, 1500000.00, 3000000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (2, 2, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (3, 3, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (4, 4, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (5, 5, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (6, 6, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (7, 7, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (8, 8, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (9, 9, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (10, 10, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (11, 11, 2, 2, 650000.00, 1300000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (12, 12, 2, 3, 650000.00, 1950000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (13, 13, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (14, 14, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (15, 15, 2, 2, 650000.00, 1300000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (16, 16, 2, 3, 650000.00, 1950000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (17, 17, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (18, 18, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (19, 19, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (20, 20, 2, 1, 650000.00, 650000.00, 'Festival Regular');
INSERT INTO public.detail_pemesanan VALUES (21, 21, 1, 1, 1500000.00, 1500000.00, 'VIP Front Row');
INSERT INTO public.detail_pemesanan VALUES (22, 22, 3, 4, 1250000.00, 5000000.00, 'VIP');


--
-- TOC entry 2854 (class 0 OID 19568)
-- Dependencies: 203
-- Data for Name: pemesanan; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.pemesanan VALUES (1, 1, 'ORD-1789174692-9993', 3065000.00, 'PAID', '2026-09-12 08:13:12.286111', '2026-09-12 07:58:12.286111', '2026-09-12 07:58:13.465235', NULL, 3000000.00, 60000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (2, 1, 'ORD-1789177647-4E9C', 1485000.00, 'PAID', '2026-09-12 09:02:27.818071', '2026-09-12 08:47:27.818071', '2026-09-12 08:47:29.387012', NULL, 1500000.00, 30000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (3, 1, 'ORD-1789202833-F4A2', 1535000.00, 'PAID', '2026-09-12 16:02:13.064894', '2026-09-12 15:47:13.064894', '2026-09-12 15:47:45.769444', NULL, 1500000.00, 30000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (4, 1, 'ORD-1789202947-7B05', 1485000.00, 'PAID', '2026-09-12 16:04:07.910249', '2026-09-12 15:49:07.910249', '2026-09-12 15:49:49.728922', NULL, 1500000.00, 30000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (5, 25, 'ORD-1789307715-99A0', 1535000.00, 'REFUNDED', '2026-09-13 21:10:15.449646', '2026-09-13 20:55:15.449646', '2026-09-13 20:55:17.86922', NULL, 1500000.00, 30000.00, 5000.00, 0.00, NULL, 'APPROVED', 'Jadwal dinas ke luar kota mendadak bertabrakan dengan hari H konser musik', 1350000.00, '2026-09-13 20:55:17.590302');
INSERT INTO public.pemesanan VALUES (6, 25, 'ORD-1789348262-9E07', 1485000.00, 'PAID', '2026-09-14 08:26:02.617311', '2026-09-14 08:11:02.617311', '2026-09-14 08:11:03.127076', NULL, 1500000.00, 30000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (7, 25, 'ORD-1789348326-C573', 1535000.00, 'CANCELLED', '2026-09-14 08:27:06.30247', '2026-09-14 08:12:06.30247', '2026-09-14 08:12:23.15734', NULL, 1500000.00, 30000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (8, 26, 'ORD-1789350811-A29A', 1535000.00, 'PAID', '2026-09-14 09:08:31.382075', '2026-09-14 08:53:31.382075', '2026-09-14 08:54:24.048725', NULL, 1500000.00, 30000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (9, 25, 'ORD-1789355181-1BD1', 618000.00, 'PAID', '2026-09-14 10:21:21.989223', '2026-09-14 10:06:21.989223', '2026-09-14 10:06:23.832418', NULL, 650000.00, 13000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (10, 25, 'ORD-1789355184-7F66', 668000.00, 'PAID', '2026-09-14 10:21:24.315982', '2026-09-14 10:06:24.315982', '2026-09-14 10:06:24.44335', NULL, 650000.00, 13000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (12, 25, 'ORD-1789355188-C71B', 1994000.00, 'CANCELLED', '2026-09-14 09:56:30.818949', '2026-09-14 10:06:28.213837', '2026-09-14 10:11:02.208851', NULL, 1950000.00, 39000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (11, 25, 'ORD-1789355184-04F0', 1331000.00, 'CANCELLED', '2026-09-14 10:01:28.096785', '2026-09-14 10:06:24.603934', '2026-09-14 10:11:02.249892', NULL, 1300000.00, 26000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (13, 25, 'ORD-1789355492-D292', 618000.00, 'PAID', '2026-09-14 10:26:32.503373', '2026-09-14 10:11:32.503373', '2026-09-14 10:11:32.821583', NULL, 650000.00, 13000.00, 5000.00, 50000.00, 'VALID1789355491', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (14, 25, 'ORD-1789355493-4801', 668000.00, 'PAID', '2026-09-14 10:26:33.117621', '2026-09-14 10:11:33.117621', '2026-09-14 10:11:33.256692', NULL, 650000.00, 13000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (15, 25, 'ORD-1789355493-BCEE', 1331000.00, 'CANCELLED', '2026-09-14 10:06:36.19173', '2026-09-14 10:11:33.448982', '2026-09-14 10:11:36.368871', NULL, 1300000.00, 26000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (16, 25, 'ORD-1789355496-C25B', 1994000.00, 'CANCELLED', '2026-09-14 10:01:39.271628', '2026-09-14 10:11:36.431207', '2026-09-14 10:11:41.638311', NULL, 1950000.00, 39000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (17, 25, 'ORD-1789357067-F5DF', 668000.00, 'PAID', '2026-09-14 10:52:47.096964', '2026-09-14 10:37:47.096964', '2026-09-14 10:37:47.491612', NULL, 650000.00, 13000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (18, 25, 'ORD-1789357144-E0B1', 668000.00, 'PAID', '2026-09-14 10:54:04.502102', '2026-09-14 10:39:04.502102', '2026-09-14 10:39:04.650355', NULL, 650000.00, 13000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (19, 26, 'ORD-1789359818-F9F0', 618000.00, 'PAID', '2026-09-14 11:38:38.251872', '2026-09-14 11:23:38.251872', '2026-09-14 11:24:34.5663', NULL, 650000.00, 13000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (20, 29, 'ORD-1789571746-FF03', 668000.00, 'PAID', '2026-09-16 22:30:46.760397', '2026-09-16 22:15:46.760397', '2026-09-16 22:16:18.535967', NULL, 650000.00, 13000.00, 5000.00, 0.00, NULL, NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (21, 32, 'ORD-1789578244-51C1', 1485000.00, 'PAID', '2026-09-17 00:19:04.555541', '2026-09-17 00:04:04.555541', '2026-09-17 00:04:18.095454', NULL, 1500000.00, 30000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);
INSERT INTO public.pemesanan VALUES (22, 29, 'ORD-1789582918-53F1', 5055000.00, 'PAID', '2026-09-17 01:36:58.765925', '2026-09-17 01:21:58.765925', '2026-09-17 01:22:11.653128', NULL, 5000000.00, 100000.00, 5000.00, 50000.00, 'TIKARAHEMAT', NULL, NULL, NULL, NULL);


--
-- TOC entry 2858 (class 0 OID 19788)
-- Dependencies: 207
-- Data for Name: refund_request; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.refund_request VALUES (1, 5, 25, 'ORD-1789307715-99A0', 'Jadwal dinas ke luar kota mendadak bertabrakan dengan hari H konser musik', 1350000.00, 150000.00, 'APPROVED', 'Syarat pembatalan terpenuhi sebelum H-3 konser. Tiket dibatalkan dan kuota dikembalikan.', '2026-09-13 20:55:17.586838', '2026-09-13 20:55:17.86922', '2026-09-13 20:55:17.86922');


--
-- TOC entry 2868 (class 0 OID 0)
-- Dependencies: 204
-- Name: detail_pemesanan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.detail_pemesanan_id_seq', 22, true);


--
-- TOC entry 2869 (class 0 OID 0)
-- Dependencies: 202
-- Name: pemesanan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pemesanan_id_seq', 22, true);


--
-- TOC entry 2870 (class 0 OID 0)
-- Dependencies: 206
-- Name: refund_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.refund_request_id_seq', 1, true);


--
-- TOC entry 2719 (class 2606 OID 19588)
-- Name: detail_pemesanan detail_pemesanan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detail_pemesanan
    ADD CONSTRAINT detail_pemesanan_pkey PRIMARY KEY (id);


--
-- TOC entry 2715 (class 2606 OID 19709)
-- Name: pemesanan pemesanan_nomor_pesanan_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pemesanan
    ADD CONSTRAINT pemesanan_nomor_pesanan_key UNIQUE (nomor_pesanan);


--
-- TOC entry 2717 (class 2606 OID 19576)
-- Name: pemesanan pemesanan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pemesanan
    ADD CONSTRAINT pemesanan_pkey PRIMARY KEY (id);


--
-- TOC entry 2724 (class 2606 OID 19800)
-- Name: refund_request refund_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refund_request
    ADD CONSTRAINT refund_request_pkey PRIMARY KEY (id);


--
-- TOC entry 2712 (class 1259 OID 19579)
-- Name: idx_pemesanan_pengguna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pemesanan_pengguna ON public.pemesanan USING btree (pengguna_id);


--
-- TOC entry 2713 (class 1259 OID 19711)
-- Name: idx_pemesanan_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pemesanan_status ON public.pemesanan USING btree (status);


--
-- TOC entry 2720 (class 1259 OID 19807)
-- Name: idx_refund_pemesanan; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refund_pemesanan ON public.refund_request USING btree (pemesanan_id);


--
-- TOC entry 2721 (class 1259 OID 19806)
-- Name: idx_refund_pengguna; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refund_pengguna ON public.refund_request USING btree (pengguna_id);


--
-- TOC entry 2722 (class 1259 OID 19821)
-- Name: idx_refund_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_refund_status ON public.refund_request USING btree (status);


--
-- TOC entry 2725 (class 2606 OID 19589)
-- Name: detail_pemesanan fk_detail_pemesanan_pemesanan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detail_pemesanan
    ADD CONSTRAINT fk_detail_pemesanan_pemesanan FOREIGN KEY (pemesanan_id) REFERENCES public.pemesanan(id) ON DELETE CASCADE;


--
-- TOC entry 2726 (class 2606 OID 19801)
-- Name: refund_request fk_refund_pemesanan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refund_request
    ADD CONSTRAINT fk_refund_pemesanan FOREIGN KEY (pemesanan_id) REFERENCES public.pemesanan(id) ON DELETE CASCADE;


-- Completed on 2026-09-18 21:09:54

--
-- PostgreSQL database dump complete
--

\unrestrict H9SRgpdqVGu7QjiOdBEtZ4AsLskki7nXCpyFptzqk7gEOo9GVhdAEaPKHosxRQu

