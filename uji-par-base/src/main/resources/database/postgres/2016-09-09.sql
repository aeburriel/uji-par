ALTER TABLE PAR_CINES ADD COLUMN API_KEY VARCHAR(255);

--Rellenar y hacer no nulas
--ALTER TABLE public.par_cines ALTER COLUMN API_KEY SET NOT NULL;

INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-09-09.SQL');