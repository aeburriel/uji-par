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


CREATE TABLE PAR_TARIFAS
(
  ID serial NOT NULL,
  NOMBRE VARCHAR(100) NOT NULL,
  ISPUBLICA boolean default true,
  DEFECTO boolean default false,
  CONSTRAINT "PAR_TARIFAS_pkey" PRIMARY KEY (ID)
);

ALTER TABLE par_precios_plantilla ADD COLUMN tarifa_id integer;
ALTER TABLE par_precios_plantilla ADD CONSTRAINT par_precios_plantilla_par_fk3 FOREIGN KEY (tarifa_id) REFERENCES
par_tarifas (id) ON UPDATE CASCADE ON DELETE CASCADE;

/*ALTER TABLE par_precios_plantilla DROP CONSTRAINT par_precios_plantilla_plantilla_id_localizacion_id_tarifa_i_key;*/
ALTER TABLE par_precios_plantilla DROP CONSTRAINT par_precios_plantilla_uk1;
ALTER TABLE par_precios_plantilla ADD UNIQUE (localizacion_id, plantilla_id, tarifa_id);

ALTER TABLE par_precios_sesion ADD COLUMN tarifa_id integer;
ALTER TABLE par_precios_sesion DROP CONSTRAINT par_precios_sesion_uk1;
ALTER TABLE par_precios_sesion ADD CONSTRAINT par_precios_sesion_par_ta_fk2 FOREIGN KEY (tarifa_id) 
	REFERENCES par_tarifas (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE par_precios_sesion ADD UNIQUE (localizacion_id, sesion_id, tarifa_id);

ALTER TABLE par_sesiones ADD COLUMN incidencia_id integer;
update par_sesiones set incidencia_id = 0;

/*CREATE TABLE PAR_TARIFAS_CINES
(
  ID serial NOT NULL,
  PAR_CINE_ID integer NOT NULL,
  PAR_TARIFA_ID integer NOT NULL,
  CONSTRAINT "PAR_TARIFAS_CINES_pkey" PRIMARY KEY (ID),
  CONSTRAINT "PAR_TARIFAS_PAR_TARIFAS_ID_fkey" FOREIGN KEY (PAR_TARIFA_ID)
      REFERENCES PAR_TARIFAS (ID) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT,
  CONSTRAINT "PAR_TARIFAS_PAR_CINES_ID_fkey" FOREIGN KEY (PAR_CINE_ID)
      REFERENCES PAR_CINES (ID) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);*/

ALTER TABLE par_compras ADD COLUMN referencia_pago character varying(255);

ALTER TABLE par_localizaciones ADD COLUMN iniciales character varying(100);

CREATE TABLE par_sesiones_formato_idioma_icaa
(
  id serial NOT NULL,
  formato character varying(400) NOT NULL,
  ver_ling character varying(400) NOT NULL,
  evento_id integer NOT NULL,
  CONSTRAINT par_sesiones_formato_idioma_icaa_pkey PRIMARY KEY (id ),
  CONSTRAINT par_sesiones_formato_idioma_icaa_evento_id_fkey FOREIGN KEY (evento_id)
      REFERENCES par_eventos (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

/*insert into par_sesiones_formato_idioma_icaa (formato, ver_ling, evento_id) 
select s.formato, s.ver_ling, s.evento_id from par_sesiones s, par_eventos e, par_tipos_evento t where s.evento_id = e.id and e.tipo_evento_id = t.id and t.exportar_icaa = true;
després eliminar evento_id, formato, idioma_icaa repetits
*/