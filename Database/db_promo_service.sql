--
-- PostgreSQL database dump
--

\restrict WN6fI8GIW0hfoWYLdVEUzJ5IEkdhhTfBHrslpC0CmrJZPfVcdn859tzgOWCGH2c

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:10:21

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

DROP DATABASE db_promo_service;
--
-- TOC entry 2844 (class 1262 OID 19640)
-- Name: db_promo_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_promo_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict WN6fI8GIW0hfoWYLdVEUzJ5IEkdhhTfBHrslpC0CmrJZPfVcdn859tzgOWCGH2c
\connect db_promo_service
\restrict WN6fI8GIW0hfoWYLdVEUzJ5IEkdhhTfBHrslpC0CmrJZPfVcdn859tzgOWCGH2c

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
-- TOC entry 203 (class 1259 OID 19643)
-- Name: kode_promo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.kode_promo (
    id bigint NOT NULL,
    kode character varying(50) NOT NULL,
    persen_diskon numeric(5,2) NOT NULL,
    potongan_maksimal numeric(12,2) NOT NULL,
    minimal_transaksi numeric(12,2) DEFAULT 0 NOT NULL,
    kuota_promo integer NOT NULL,
    terpakai integer DEFAULT 0 NOT NULL,
    mulai_berlaku timestamp without time zone NOT NULL,
    selesai_berlaku timestamp without time zone NOT NULL,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 19641)
-- Name: kode_promo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.kode_promo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2845 (class 0 OID 0)
-- Dependencies: 202
-- Name: kode_promo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.kode_promo_id_seq OWNED BY public.kode_promo.id;


--
-- TOC entry 205 (class 1259 OID 19659)
-- Name: penggunaan_promo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penggunaan_promo (
    id bigint NOT NULL,
    kode_promo_id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    pemesanan_id bigint NOT NULL,
    potongan_didapat numeric(12,2) NOT NULL,
    digunakan_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 204 (class 1259 OID 19657)
-- Name: penggunaan_promo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.penggunaan_promo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2846 (class 0 OID 0)
-- Dependencies: 204
-- Name: penggunaan_promo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.penggunaan_promo_id_seq OWNED BY public.penggunaan_promo.id;


--
-- TOC entry 2693 (class 2604 OID 19646)
-- Name: kode_promo id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kode_promo ALTER COLUMN id SET DEFAULT nextval('public.kode_promo_id_seq'::regclass);


--
-- TOC entry 2699 (class 2604 OID 19662)
-- Name: penggunaan_promo id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penggunaan_promo ALTER COLUMN id SET DEFAULT nextval('public.penggunaan_promo_id_seq'::regclass);


--
-- TOC entry 2836 (class 0 OID 19643)
-- Dependencies: 203
-- Data for Name: kode_promo; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.kode_promo VALUES (2, 'FESTIVAL2026', 15.00, 75000.00, 150000.00, 250, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-13 20:55:14.265812', '2026-09-13 20:55:14.439824');
INSERT INTO public.kode_promo VALUES (3, 'EXP1789355184', 20.00, 50000.00, 50000.00, 100, 0, '2025-01-01 00:00:00', '2025-02-01 00:00:00', 'ACTIVE', '2026-09-14 10:06:24.102487', '2026-09-14 10:06:24.102487');
INSERT INTO public.kode_promo VALUES (4, 'COMP1789355184', 10.00, 30000.00, 50000.00, 50, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-14 10:06:24.173802', '2026-09-14 10:06:24.244985');
INSERT INTO public.kode_promo VALUES (5, 'VALID1789355491', 10.00, 50000.00, 100000.00, 100, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-14 10:11:31.683184', '2026-09-14 10:11:31.683184');
INSERT INTO public.kode_promo VALUES (6, 'EXPIRED1789355491', 10.00, 50000.00, 100000.00, 100, 0, '2025-01-01 00:00:00', '2025-02-01 00:00:00', 'ACTIVE', '2026-09-14 10:11:32.956356', '2026-09-14 10:11:32.957998');
INSERT INTO public.kode_promo VALUES (7, 'COMP1789355491', 10.00, 50000.00, 100000.00, 100, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-14 10:11:33.023396', '2026-09-14 10:11:33.079139');
INSERT INTO public.kode_promo VALUES (8, 'STK1789355491', 10.00, 50000.00, 100000.00, 100, 0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-14 10:11:33.336858', '2026-09-14 10:11:33.336858');
INSERT INTO public.kode_promo VALUES (1, 'TIKARAHEMAT', 10.00, 50000.00, 100000.00, 500, 4, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE', '2026-09-12 08:31:32.727985', '2026-09-12 08:31:32.727985');
INSERT INTO public.kode_promo VALUES (9, 'RC24DEY', 15.00, 45000.00, 200000.00, 150, 0, '2026-09-18 17:43:00', '2026-10-18 17:43:00', 'ACTIVE', '2026-09-18 17:44:08.349865', '2026-09-18 17:44:08.349865');


--
-- TOC entry 2838 (class 0 OID 19659)
-- Dependencies: 205
-- Data for Name: penggunaan_promo; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.penggunaan_promo VALUES (1, 1, 25, 9, 50000.00, '2026-09-14 10:06:23.597572');
INSERT INTO public.penggunaan_promo VALUES (3, 5, 25, 13, 50000.00, '2026-09-14 10:11:32.770109');
INSERT INTO public.penggunaan_promo VALUES (5, 1, 26, 19, 50000.00, '2026-09-14 11:24:33.410112');
INSERT INTO public.penggunaan_promo VALUES (6, 1, 32, 21, 50000.00, '2026-09-17 00:04:17.564463');
INSERT INTO public.penggunaan_promo VALUES (7, 1, 29, 22, 50000.00, '2026-09-17 01:22:11.422276');


--
-- TOC entry 2847 (class 0 OID 0)
-- Dependencies: 202
-- Name: kode_promo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.kode_promo_id_seq', 9, true);


--
-- TOC entry 2848 (class 0 OID 0)
-- Dependencies: 204
-- Name: penggunaan_promo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.penggunaan_promo_id_seq', 7, true);


--
-- TOC entry 2703 (class 2606 OID 19655)
-- Name: kode_promo kode_promo_kode_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kode_promo
    ADD CONSTRAINT kode_promo_kode_key UNIQUE (kode);


--
-- TOC entry 2705 (class 2606 OID 19653)
-- Name: kode_promo kode_promo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kode_promo
    ADD CONSTRAINT kode_promo_pkey PRIMARY KEY (id);


--
-- TOC entry 2707 (class 2606 OID 19665)
-- Name: penggunaan_promo penggunaan_promo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penggunaan_promo
    ADD CONSTRAINT penggunaan_promo_pkey PRIMARY KEY (id);


--
-- TOC entry 2701 (class 1259 OID 19656)
-- Name: idx_kode_promo_kode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kode_promo_kode ON public.kode_promo USING btree (kode);


--
-- TOC entry 2708 (class 2606 OID 19666)
-- Name: penggunaan_promo fk_penggunaan_promo_kode_promo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penggunaan_promo
    ADD CONSTRAINT fk_penggunaan_promo_kode_promo FOREIGN KEY (kode_promo_id) REFERENCES public.kode_promo(id) ON DELETE CASCADE;


-- Completed on 2026-09-18 21:10:24

--
-- PostgreSQL database dump complete
--

\unrestrict WN6fI8GIW0hfoWYLdVEUzJ5IEkdhhTfBHrslpC0CmrJZPfVcdn859tzgOWCGH2c

