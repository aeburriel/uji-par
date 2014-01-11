-- DATOS EVENTOS

ALTER TABLE PAR_EVENTOS ADD COLUMN SUBTITULOS VARCHAR(400);

-- DATOS SESIONES

ALTER TABLE PAR_SESIONES ADD COLUMN VER_LING VARCHAR(400);
ALTER TABLE PAR_SESIONES ADD COLUMN RSS_ID VARCHAR(400);

-- TIPOS EVENTO

ALTER TABLE PAR_TIPOS_EVENTO ADD COLUMN exportar_icaa BOOLEAN DEFAULT false;

-- ENVIOS

CREATE TABLE PAR_ENVIOS
(    
  ID SERIAL PRIMARY KEY, 
  FECHA_GENERACION_FICHERO TIMESTAMP,
  FECHA_ENVIO_FICHERO TIMESTAMP
);

-- LOCALIZACIONES

ALTER TABLE par_localizaciones ADD COLUMN SALA_ID integer;
ALTER TABLE par_localizaciones ADD FOREIGN KEY (SALA_ID) REFERENCES PAR_SALAS (ID) ON UPDATE CASCADE ON DELETE CASCADE;

--PLANTILLAS

ALTER TABLE PAR_PLANTILLAS ADD COLUMN SALA_ID integer;
ALTER TABLE PAR_PLANTILLAS ADD FOREIGN KEY (sala_id) REFERENCES PAR_SALAS (id) ON UPDATE CASCADE ON DELETE CASCADE;


-- ENVIOS SESIONES

CREATE TABLE PAR_ENVIOS_SESIONES
(
  ID serial NOT NULL,
  PAR_ENVIO_ID integer NOT NULL,
  PAR_SESION_ID integer NOT NULL,
  TIPO_ENVIO VARCHAR(2) NOT NULL,
  CONSTRAINT "PAR_ENVIOS_SESIONES_pkey" PRIMARY KEY (ID),
  CONSTRAINT "PAR_ENVIOS_SESIONES_PAR_ENVIO_ID_fkey" FOREIGN KEY (PAR_ENVIO_ID)
      REFERENCES par_envios (ID) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT "PAR_ENVIOS_SESIONES_PAR_SESION_ID_fkey" FOREIGN KEY (PAR_SESION_ID)
      REFERENCES par_sesiones (ID) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

--CLAVES PRIMARIAS

ALTER TABLE PAR_BUTACAS ADD PRIMARY KEY (id);
ALTER TABLE PAR_CINES ADD PRIMARY KEY (id);
ALTER TABLE PAR_COMPRAS ADD PRIMARY KEY (id);
ALTER TABLE PAR_EVENTOS ADD PRIMARY KEY (id);
ALTER TABLE PAR_LOCALIZACIONES ADD PRIMARY KEY (id);
ALTER TABLE PAR_MAILS ADD PRIMARY KEY (id);
ALTER TABLE PAR_PLANTAS_SALA ADD PRIMARY KEY (id);
ALTER TABLE PAR_PLANTILLAS ADD PRIMARY KEY (id);
ALTER TABLE PAR_PRECIOS_PLANTILLA ADD PRIMARY KEY (id);
ALTER TABLE PAR_PRECIOS_SESION ADD PRIMARY KEY (id);
ALTER TABLE PAR_SALAS ADD PRIMARY KEY (id);
ALTER TABLE PAR_SESIONES ADD PRIMARY KEY (id);
ALTER TABLE PAR_TIPOS_EVENTO ADD PRIMARY KEY (id);
ALTER TABLE PAR_USUARIOS ADD PRIMARY KEY (id);
