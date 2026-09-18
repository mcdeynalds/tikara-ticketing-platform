--
-- PostgreSQL database dump
--

\restrict A0VhtJXEVOcdxftkBKRrcVX1kOuQTlHgKVssBDPTh4urTscr5Qi0dqeyylp5jwR

-- Dumped from database version 12.4
-- Dumped by pg_dump version 18.4

-- Started on 2026-09-18 21:11:19

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

DROP DATABASE db_user_service;
--
-- TOC entry 2861 (class 1262 OID 19391)
-- Name: db_user_service; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE db_user_service WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Indonesian_Indonesia.1252';


\unrestrict A0VhtJXEVOcdxftkBKRrcVX1kOuQTlHgKVssBDPTh4urTscr5Qi0dqeyylp5jwR
\connect db_user_service
\restrict A0VhtJXEVOcdxftkBKRrcVX1kOuQTlHgKVssBDPTh4urTscr5Qi0dqeyylp5jwR

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
-- TOC entry 203 (class 1259 OID 19394)
-- Name: pengguna; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pengguna (
    id bigint NOT NULL,
    nama character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    kata_sandi_hash character varying(255) NOT NULL,
    peran character varying(255) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at timestamp without time zone
);


--
-- TOC entry 202 (class 1259 OID 19392)
-- Name: pengguna_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pengguna_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2862 (class 0 OID 0)
-- Dependencies: 202
-- Name: pengguna_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pengguna_id_seq OWNED BY public.pengguna.id;


--
-- TOC entry 205 (class 1259 OID 19411)
-- Name: profil_pengguna; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profil_pengguna (
    id bigint NOT NULL,
    pengguna_id bigint NOT NULL,
    no_telepon character varying(50),
    alamat text,
    foto_profil character varying(512),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    foto_profil_url character varying(255),
    nomor_telepon character varying(255),
    tanggal_lahir date
);


--
-- TOC entry 204 (class 1259 OID 19409)
-- Name: profil_pengguna_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profil_pengguna_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2863 (class 0 OID 0)
-- Dependencies: 204
-- Name: profil_pengguna_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.profil_pengguna_id_seq OWNED BY public.profil_pengguna.id;


--
-- TOC entry 207 (class 1259 OID 19699)
-- Name: reset_password_otp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reset_password_otp (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    otp_code character varying(6) NOT NULL,
    expired_at timestamp without time zone NOT NULL,
    is_used boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- TOC entry 206 (class 1259 OID 19697)
-- Name: reset_password_otp_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reset_password_otp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2864 (class 0 OID 0)
-- Dependencies: 206
-- Name: reset_password_otp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reset_password_otp_id_seq OWNED BY public.reset_password_otp.id;


--
-- TOC entry 2701 (class 2604 OID 19397)
-- Name: pengguna id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pengguna ALTER COLUMN id SET DEFAULT nextval('public.pengguna_id_seq'::regclass);


--
-- TOC entry 2704 (class 2604 OID 19414)
-- Name: profil_pengguna id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profil_pengguna ALTER COLUMN id SET DEFAULT nextval('public.profil_pengguna_id_seq'::regclass);


--
-- TOC entry 2707 (class 2604 OID 19702)
-- Name: reset_password_otp id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reset_password_otp ALTER COLUMN id SET DEFAULT nextval('public.reset_password_otp_id_seq'::regclass);


--
-- TOC entry 2851 (class 0 OID 19394)
-- Dependencies: 203
-- Data for Name: pengguna; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.pengguna VALUES (30, 'Promotor Musik TIKARA', 'organizer.test@gmail.com', '$2a$10$RDfr8ODh2l0U0peu6sUVYObL//78hK/DX5RPnBmfa5E0SFJMkGMv.', 'PENYELENGGARA', '2026-09-15 15:58:36.985775', '2026-09-15 15:58:36.985775', NULL);
INSERT INTO public.pengguna VALUES (31, 'Budi Santoso', 'buditest@gmail.com', '$2a$10$VDVpNloSgp0mNR1yiDlK5ek60OV0dV.SycXi7v7dSjzUv7Sl1BIHq', 'PEMBELI', '2026-09-16 15:15:40.247991', '2026-09-16 15:15:40.247991', NULL);
INSERT INTO public.pengguna VALUES (23, 'Admin Tikara', 'deapriliart@gmail.com', '$2a$10$zkwMlwdCWcix81xh3N5s3uzgkWdsDuMMgZX.DSVWF.T24qXNvERga', 'ADMIN', '2026-09-05 10:17:32.000964', '2026-09-05 10:23:42.766527', NULL);
INSERT INTO public.pengguna VALUES (32, 'Aminatul Zahro', 'amiiperii@gmail.com', '$2a$10$Ocbd4FFaiQ/EoX3ZGI.xsOmC6jtyTKZlDgWhVNJHpf8XmwmzGBwKa', 'PEMBELI', '2026-09-17 00:01:36.133795', '2026-09-17 00:14:56.29526', NULL);
INSERT INTO public.pengguna VALUES (29, 'Miftah Huljannah Harahap', 'huljannahmiftah98@gmail.com', '$2a$10$0YIOJlnxLiPqeGGb6Emb/u7DN669mUS8Pere9jh0sy8snVqhQRuiS', 'PEMBELI', '2026-09-15 11:17:42.285913', '2026-09-17 01:21:58.745633', NULL);
INSERT INTO public.pengguna VALUES (1, 'Mutia Ananda', 'moondaisy938@gmail.com', '$2a$10$1QjHPLUpsynzpr4ZO6sBSuoKq/iyP.gAQ9/9dVBFSTfQf7GtTT9Ee', 'PEMBELI', '2026-09-01 10:26:22.238448', '2026-09-12 15:44:22.917985', NULL);
INSERT INTO public.pengguna VALUES (25, 'Budi Santoso', 'budi.test@gmail.com', '$2a$10$zkwMlwdCWcix81xh3N5s3uzgkWdsDuMMgZX.DSVWF.T24qXNvERga', 'PEMBELI', '2026-09-12 08:53:50.967911', '2026-09-12 08:53:50.967911', NULL);
INSERT INTO public.pengguna VALUES (27, 'Siti Pembeli', 'siti.test@gmail.com', '$2a$10$z.twvhoIcfKLD6dDPhD67Ok8ZZHe57IgPGVYe/uiPVfgx40S56Tem', 'PEMBELI', '2026-09-14 10:28:40.620298', '2026-09-14 10:28:40.620298', NULL);
INSERT INTO public.pengguna VALUES (28, 'Rudi Tester Updated', 'rudi.test@gmail.com', '$2a$10$XbPZj/5gGH5FS8rl84kZyO0JJi6o1THAHgD4yOdFck/5Jm7mRFWxC', 'PEMBELI', '2026-09-14 11:07:13.135341', '2026-09-14 11:11:35.063987', NULL);
INSERT INTO public.pengguna VALUES (26, 'Test User', 'testuser88@gmail.com', '$2a$10$fpwLRPWKcJFA0q67YHuPT.Q5yRXM4DFp5it25raHTAHwYN7TGR9zq', 'PEMBELI', '2026-09-13 21:17:35.859566', '2026-09-14 11:23:38.290516', NULL);
INSERT INTO public.pengguna VALUES (24, 'Dhea Ananda', 'deyaminfo@gmail.com', '$2a$10$AELvg25iTndi2cKDMevEVOuoADjtgDxm.aDkMu/wWYTKqTbuL6nA.', 'PENYELENGGARA', '2026-09-05 11:24:46.631474', '2026-09-15 11:26:19.695899', NULL);
INSERT INTO public.pengguna VALUES (33, 'Dea Afrilia', 'deafriliart@gmail.com', '$2a$10$Q8vKkkmspy8lgWQJrgbXX.Dm17/71lNs.PgnsDbWYwlc2KpNBtkE.', 'PENYELENGGARA', '2026-09-18 10:20:27.871661', '2026-09-18 10:24:20.882999', NULL);
INSERT INTO public.pengguna VALUES (34, 'Anisa Wijaya', 'anisa.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (35, 'Fajar Nugraha', 'fajar.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (36, 'Gita Gutawa', 'gita.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (37, 'Hendra Saputra', 'hendra.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (38, 'Indah Permatasari', 'indah.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (39, 'Joko Susilo', 'joko.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (40, 'Kartika Putri', 'kartika.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);
INSERT INTO public.pengguna VALUES (41, 'Lukman Hakim', 'lukman.pembeli@gmail.com', '$2a$10$wT3Wp.g7Y7hC98f5r6Y3aei7V9wXh029uW635fQxJb9qI/7m5qHce', 'PEMBELI', '2026-09-18 15:37:47.780606', '2026-09-18 15:37:47.780606', NULL);


--
-- TOC entry 2853 (class 0 OID 19411)
-- Dependencies: 205
-- Data for Name: profil_pengguna; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.profil_pengguna VALUES (1, 1, NULL, NULL, NULL, '2026-09-01 10:26:22.481965', '2026-09-05 00:20:19.208715', '/uploads/avatars/8dd5c01a-e5d4-4b64-b390-d7b96601b3c1.JPG', NULL, NULL);
INSERT INTO public.profil_pengguna VALUES (23, 23, NULL, 'Headquarter TIKARA Jakarta', NULL, '2026-09-05 10:17:36.582251', '2026-09-05 10:17:36.582251', NULL, '081299998888', NULL);
INSERT INTO public.profil_pengguna VALUES (24, 24, NULL, NULL, NULL, '2026-09-05 11:24:47.096249', '2026-09-05 11:24:47.096249', NULL, '085959257370', NULL);
INSERT INTO public.profil_pengguna VALUES (25, 25, NULL, NULL, NULL, '2026-09-12 08:53:51.373849', '2026-09-12 08:53:51.373849', NULL, '081234567890', NULL);
INSERT INTO public.profil_pengguna VALUES (27, 27, NULL, NULL, NULL, '2026-09-14 10:28:40.749408', '2026-09-14 10:28:40.749408', NULL, '081299887766', NULL);
INSERT INTO public.profil_pengguna VALUES (28, 28, NULL, NULL, NULL, '2026-09-14 11:07:13.431407', '2026-09-14 11:11:35.089349', NULL, '081299998888', NULL);
INSERT INTO public.profil_pengguna VALUES (26, 26, NULL, NULL, NULL, '2026-09-13 21:17:36.469013', '2026-09-14 11:23:38.312056', NULL, '081234567890', NULL);
INSERT INTO public.profil_pengguna VALUES (30, 30, NULL, NULL, NULL, '2026-09-15 15:58:37.399753', '2026-09-15 15:58:37.399753', NULL, '081299887766', NULL);
INSERT INTO public.profil_pengguna VALUES (31, 31, NULL, NULL, NULL, '2026-09-16 15:15:40.788438', '2026-09-16 15:15:40.788438', NULL, '081234567890', NULL);
INSERT INTO public.profil_pengguna VALUES (32, 32, NULL, NULL, NULL, '2026-09-17 00:01:36.556364', '2026-09-17 00:04:03.744358', NULL, '082183227773', NULL);
INSERT INTO public.profil_pengguna VALUES (29, 29, NULL, NULL, NULL, '2026-09-15 11:17:42.450975', '2026-09-17 01:21:58.761263', NULL, '081283800302', NULL);
INSERT INTO public.profil_pengguna VALUES (33, 33, NULL, NULL, NULL, '2026-09-18 10:20:28.468012', '2026-09-18 10:24:20.914419', '/uploads/avatars/a2892451-5f6a-4843-8a1a-c9e5d2616b12.JPG', '081278923789', NULL);
INSERT INTO public.profil_pengguna VALUES (34, 34, NULL, 'Jl. Sukajadi No. 45, Bandung', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887701', '1998-05-12');
INSERT INTO public.profil_pengguna VALUES (35, 35, NULL, 'Jl. Gejayan No. 18, Yogyakarta', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887702', '1996-08-20');
INSERT INTO public.profil_pengguna VALUES (36, 36, NULL, 'Jl. Tebet Barat No. 8, Jakarta Selatan', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887703', '1999-01-15');
INSERT INTO public.profil_pengguna VALUES (37, 37, NULL, 'Jl. Pemuda No. 102, Surabaya', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887704', '1995-11-03');
INSERT INTO public.profil_pengguna VALUES (38, 38, NULL, 'Jl. Gatot Subroto No. 55, Denpasar Bali', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887705', '1997-07-28');
INSERT INTO public.profil_pengguna VALUES (39, 39, NULL, 'Jl. Pandanaran No. 14, Semarang', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887706', '1994-03-09');
INSERT INTO public.profil_pengguna VALUES (40, 40, NULL, 'Jl. Merdeka No. 70, Malang', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887707', '1998-09-17');
INSERT INTO public.profil_pengguna VALUES (41, 41, NULL, 'Jl. Sudirman No. 88, Solo', NULL, '2026-09-18 15:38:08.644865', '2026-09-18 15:38:08.644865', NULL, '081299887708', '1993-12-25');


--
-- TOC entry 2855 (class 0 OID 19699)
-- Dependencies: 207
-- Data for Name: reset_password_otp; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.reset_password_otp VALUES (1, 'moondaisy938@gmail.com', '637751', '2026-09-02 22:57:21.012522', true, '2026-09-02 22:47:21.012522');
INSERT INTO public.reset_password_otp VALUES (2, 'moondaisy938@gmail.com', '434809', '2026-09-02 22:59:00.769161', true, '2026-09-02 22:49:00.769161');
INSERT INTO public.reset_password_otp VALUES (3, 'moondaisy938@gmail.com', '145036', '2026-09-04 22:16:56.995558', true, '2026-09-04 22:06:56.995558');
INSERT INTO public.reset_password_otp VALUES (4, 'moondaisy938@gmail.com', '811665', '2026-09-04 22:17:02.290766', true, '2026-09-04 22:07:02.290766');
INSERT INTO public.reset_password_otp VALUES (5, 'moondaisy938@gmail.com', '638286', '2026-09-04 22:37:56.512383', true, '2026-09-04 22:27:56.512383');
INSERT INTO public.reset_password_otp VALUES (6, 'moondaisy938@gmail.com', '593578', '2026-09-04 22:54:51.628382', true, '2026-09-04 22:44:51.628382');
INSERT INTO public.reset_password_otp VALUES (7, 'moondaisy938@gmail.com', '344722', '2026-09-04 23:07:48.810058', true, '2026-09-04 22:57:48.810058');
INSERT INTO public.reset_password_otp VALUES (8, 'deyaminfo@gmail.com', '842059', '2026-09-05 11:49:21.61505', true, '2026-09-05 11:39:21.61505');
INSERT INTO public.reset_password_otp VALUES (9, 'moondaisy938@gmail.com', '555329', '2026-09-12 15:53:15.307656', true, '2026-09-12 15:43:15.307656');
INSERT INTO public.reset_password_otp VALUES (10, 'rudi.test@gmail.com', '117761', '2026-09-14 11:23:24.152561', false, '2026-09-14 11:13:24.154556');
INSERT INTO public.reset_password_otp VALUES (11, 'huljannahmiftah98@gmail.com', '886853', '2026-09-15 11:28:06.542975', true, '2026-09-15 11:18:06.542975');
INSERT INTO public.reset_password_otp VALUES (12, 'deyaminfo@gmail.com', '316903', '2026-09-15 11:35:18.927396', true, '2026-09-15 11:25:18.927396');
INSERT INTO public.reset_password_otp VALUES (13, 'amiiperii@gmail.com', '968594', '2026-09-17 00:23:26.86027', true, '2026-09-17 00:13:26.86027');
INSERT INTO public.reset_password_otp VALUES (14, 'deafriliart@gmail.com', '873728', '2026-09-18 10:31:24.764238', true, '2026-09-18 10:21:24.764238');


--
-- TOC entry 2865 (class 0 OID 0)
-- Dependencies: 202
-- Name: pengguna_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pengguna_id_seq', 41, true);


--
-- TOC entry 2866 (class 0 OID 0)
-- Dependencies: 204
-- Name: profil_pengguna_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profil_pengguna_id_seq', 41, true);


--
-- TOC entry 2867 (class 0 OID 0)
-- Dependencies: 206
-- Name: reset_password_otp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reset_password_otp_id_seq', 14, true);


--
-- TOC entry 2713 (class 2606 OID 19406)
-- Name: pengguna pengguna_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pengguna
    ADD CONSTRAINT pengguna_email_key UNIQUE (email);


--
-- TOC entry 2715 (class 2606 OID 19404)
-- Name: pengguna pengguna_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pengguna
    ADD CONSTRAINT pengguna_pkey PRIMARY KEY (id);


--
-- TOC entry 2717 (class 2606 OID 19423)
-- Name: profil_pengguna profil_pengguna_pengguna_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profil_pengguna
    ADD CONSTRAINT profil_pengguna_pengguna_id_key UNIQUE (pengguna_id);


--
-- TOC entry 2719 (class 2606 OID 19421)
-- Name: profil_pengguna profil_pengguna_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profil_pengguna
    ADD CONSTRAINT profil_pengguna_pkey PRIMARY KEY (id);


--
-- TOC entry 2722 (class 2606 OID 19706)
-- Name: reset_password_otp reset_password_otp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reset_password_otp
    ADD CONSTRAINT reset_password_otp_pkey PRIMARY KEY (id);


--
-- TOC entry 2710 (class 1259 OID 19408)
-- Name: idx_pengguna_deleted_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pengguna_deleted_at ON public.pengguna USING btree (deleted_at);


--
-- TOC entry 2711 (class 1259 OID 19407)
-- Name: idx_pengguna_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pengguna_email ON public.pengguna USING btree (email);


--
-- TOC entry 2720 (class 1259 OID 19707)
-- Name: idx_reset_otp_email_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reset_otp_email_code ON public.reset_password_otp USING btree (email, otp_code);


--
-- TOC entry 2723 (class 2606 OID 19424)
-- Name: profil_pengguna fk_profil_pengguna_pengguna; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profil_pengguna
    ADD CONSTRAINT fk_profil_pengguna_pengguna FOREIGN KEY (pengguna_id) REFERENCES public.pengguna(id) ON DELETE CASCADE;


-- Completed on 2026-09-18 21:11:22

--
-- PostgreSQL database dump complete
--

\unrestrict A0VhtJXEVOcdxftkBKRrcVX1kOuQTlHgKVssBDPTh4urTscr5Qi0dqeyylp5jwR

