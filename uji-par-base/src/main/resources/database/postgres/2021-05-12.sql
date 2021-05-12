ALTER TABLE par_plantillas DROP CONSTRAINT par_plantillas_nombre_key;
ALTER TABLE par_plantillas ADD CONSTRAINT par_plantillas_nombre_key UNIQUE (nombre, id);

INSERT INTO par_version_bbdd (VERSION) VALUES ('2021-05-12.SQL');
