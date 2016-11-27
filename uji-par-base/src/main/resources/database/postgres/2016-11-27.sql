DROP SEQUENCE hibernate_sequence;

UPDATE par_butacas SET tipo = '-1' WHERE tipo = 'descuento';
ALTER TABLE par_butacas ALTER COLUMN tipo TYPE INTEGER USING tipo::INTEGER;
ALTER TABLE par_butacas ADD CONSTRAINT par_butacas_ti_fk1 FOREIGN KEY (tipo) REFERENCES public.par_tarifas (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE NO ACTION;

INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-11-27.SQL');
