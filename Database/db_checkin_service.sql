--
-- PostgreSQL database dump
--

\restrict VAya7bPGhFDonhfTukXw7fFU1HAPNYuHtyVlb8GfKPBdX8ksFBNUUMWsI0AIXIP

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:07:48

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

DROP DATABASE db_checkin_service;
--
-- TOC entry 2827 (class 1262 OID 19627)
-- Name: db_checkin_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_checkin_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict VAya7bPGhFDonhfTukXw7fFU1HAPNYuHtyVlb8GfKPBdX8ksFBNUUMWsI0AIXIP
\connect db_checkin_service
\restrict VAya7bPGhFDonhfTukXw7fFU1HAPNYuHtyVlb8GfKPBdX8ksFBNUUMWsI0AIXIP

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
-- TOC entry 203 (class 1259 OID 19630)
-- Name: riwayat_checkin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.riwayat_checkin (
    id bigint NOT NULL,
    acara_id bigint NOT NULL,
    tiket_id bigint NOT NULL,
    kode_qr character varying(255) NOT NULL,
    petugas_id bigint NOT NULL,
    lokasi_gate character varying(100) NOT NULL,
    status_checkin character varying(50) DEFAULT 'SUCCESS'::character varying NOT NULL,
    waktu_checkin timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19628)
-- Name: riwayat_checkin_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.riwayat_checkin_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2828 (class 0 OID 0)
-- Dependencies: 202
-- Name: riwayat_checkin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.riwayat_checkin_id_seq OWNED BY public.riwayat_checkin.id;


--
-- TOC entry 2687 (class 2604 OID 19633)
-- Name: riwayat_checkin id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.riwayat_checkin ALTER COLUMN id SET DEFAULT nextval('public.riwayat_checkin_id_seq'::regclass);


--
-- TOC entry 2821 (class 0 OID 19630)
-- Dependencies: 203
-- Data for Name: riwayat_checkin; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.riwayat_checkin VALUES (6, 1, 8, 'TK-9453E9A9-3998', 24, 'Gate A (Utama)', 'SUCCESS', '2026-09-14 09:25:13.030745');
INSERT INTO public.riwayat_checkin VALUES (7, 1, 0, 'TK-9453E9A9-3998', 24, 'Gate A (Utama)', 'FAILED_ALREADY_USED', '2026-09-14 09:25:18.191144');
INSERT INTO public.riwayat_checkin VALUES (8, 1, 0, 'FAKE-QR-NONEXISTENT-999', 24, 'Gate A (Utama)', 'FAILED_INVALID', '2026-09-14 09:25:22.773583');
INSERT INTO public.riwayat_checkin VALUES (9, 999, 0, 'TK-6F548496-9448', 24, 'Gate A (Utama)', 'FAILED_INVALID_EVENT', '2026-09-14 09:25:25.157303');
INSERT INTO public.riwayat_checkin VALUES (10, 1, 4, 'TK-DEFDFD42-5629', 23, 'Gate VIP', 'SUCCESS', '2026-09-14 09:25:29.972332');
INSERT INTO public.riwayat_checkin VALUES (11, 1, 0, 'TK-964E85D0-3779', 23, 'Gate A - VIP', 'FAILED_INVALID', '2026-09-14 10:38:03.374563');
INSERT INTO public.riwayat_checkin VALUES (12, 1, 14, 'TK-5DC305DA-1497', 23, 'Gate A - Regular', 'SUCCESS', '2026-09-14 10:38:06.119234');
INSERT INTO public.riwayat_checkin VALUES (13, 1, 0, 'TK-FE524572-7485', 23, 'Gate A - VIP', 'FAILED_INVALID', '2026-09-14 10:39:19.925052');
INSERT INTO public.riwayat_checkin VALUES (14, 1, 16, 'TK-0F695409-9260', 23, 'Gate A - Regular', 'SUCCESS', '2026-09-14 10:39:22.445292');
INSERT INTO public.riwayat_checkin VALUES (15, 1, 0, 'TK-0F695409-9260', 23, 'Gate A', 'FAILED_ALREADY_USED', '2026-09-14 10:40:01.763373');
INSERT INTO public.riwayat_checkin VALUES (16, 1, 5, 'TK-6F548496-9448', 24, 'Gate A (Utama)', 'SUCCESS', '2026-09-17 00:20:37.564626');
INSERT INTO public.riwayat_checkin VALUES (17, 1, 0, 'TK-6F548496-9448', 24, 'Gate A (Utama)', 'FAILED_ALREADY_USED', '2026-09-17 00:21:49.064303');


--
-- TOC entry 2829 (class 0 OID 0)
-- Dependencies: 202
-- Name: riwayat_checkin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.riwayat_checkin_id_seq', 17, true);


--
-- TOC entry 2693 (class 2606 OID 19637)
-- Name: riwayat_checkin riwayat_checkin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.riwayat_checkin
    ADD CONSTRAINT riwayat_checkin_pkey PRIMARY KEY (id);


--
-- TOC entry 2690 (class 1259 OID 19638)
-- Name: idx_checkin_acara; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkin_acara ON public.riwayat_checkin USING btree (acara_id);


--
-- TOC entry 2691 (class 1259 OID 19639)
-- Name: idx_checkin_kode_qr; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_checkin_kode_qr ON public.riwayat_checkin USING btree (kode_qr);


-- Completed on 2026-09-18 21:07:51

--
-- PostgreSQL database dump complete
--

\unrestrict VAya7bPGhFDonhfTukXw7fFU1HAPNYuHtyVlb8GfKPBdX8ksFBNUUMWsI0AIXIP

