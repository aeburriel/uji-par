ALTER TABLE PAR_TPVS ADD COLUMN SIGNATURE_METHOD VARCHAR(255);

INSERT INTO par_version_bbdd (VERSION) VALUES ('2015-11-18.SQL');