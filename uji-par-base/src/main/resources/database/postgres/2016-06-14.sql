ALTER TABLE PAR_EVENTOS ADD COLUMN CINE_ID INTEGER NULL;

ALTER TABLE PAR_EVENTOS ADD CONSTRAINT PAR_EVENTOS_CINES_FK1 FOREIGN KEY (CINE_ID)
REFERENCES PAR_CINES(ID) ON DELETE CASCADE;

ALTER TABLE PAR_USUARIOS ADD COLUMN URL VARCHAR(255) NULL;

ALTER TABLE PAR_TARIFAS ADD COLUMN CINE_ID INTEGER NULL;

ALTER TABLE PAR_TARIFAS ADD CONSTRAINT PAR_TARIFAS_CINES_FK1 FOREIGN KEY (CINE_ID)
REFERENCES PAR_CINES(ID) ON DELETE CASCADE;

INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-06-14.SQL');