ALTER TABLE PAR_CINES ADD COLUMN URL_PUBLIC VARCHAR(255);

ALTER TABLE PAR_MAILS ADD COLUMN URL_PUBLIC VARCHAR(255) NOT NULL;

--Rellenar y hacer no nulas

INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-08-25.SQL');