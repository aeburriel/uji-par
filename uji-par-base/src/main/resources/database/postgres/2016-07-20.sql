ALTER TABLE PAR_EVENTOS ADD COLUMN IMAGEN_PUBLI OID;
ALTER TABLE PAR_EVENTOS ADD COLUMN IMAGEN_PUBLI_CONTENT_TYPE VARCHAR(255);
ALTER TABLE PAR_EVENTOS ADD COLUMN IMAGEN_PUBLI_SRC VARCHAR(255);

INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-07-20.SQL');