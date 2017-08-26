-- Butacas
CREATE INDEX ON par_butacas (sesion_id);
CREATE INDEX ON par_butacas (localizacion_id);
CREATE INDEX ON par_butacas (compra_id);
CREATE INDEX ON par_butacas (tipo);
CREATE INDEX ON par_butacas (anulada);
CREATE INDEX ON par_butacas (presentada);

-- Compras
CREATE INDEX ON par_compras (sesion_id);
CREATE INDEX ON par_compras USING BRIN (fecha);
CREATE INDEX ON par_compras (taquilla);
CREATE INDEX ON par_compras (codigo_pago_tarjeta);
CREATE INDEX ON par_compras (pagada);
CREATE UNIQUE INDEX ON par_compras (uuid);
CREATE INDEX ON par_compras (codigo_pago_pasarela);
CREATE INDEX ON par_compras (reserva);
CREATE INDEX ON par_compras (desde, hasta);
CREATE INDEX ON par_compras (anulada);
CREATE INDEX ON par_compras (caducada);
CREATE INDEX ON par_compras (referencia_pago);
CREATE INDEX ON par_compras (abonado_id);
CREATE INDEX ON par_compras (tipo);
CREATE INDEX ON par_compras (porcentaje_iva);

-- Envios-Sesiones
CREATE INDEX ON par_envios_sesiones (par_envio_id, par_sesion_id);

-- Eventos
CREATE INDEX ON par_eventos (tipo_evento_id);
CREATE INDEX ON par_eventos (cine_id);

-- Localizaciones
CREATE INDEX ON par_localizaciones (sala_id);

-- Mails
CREATE INDEX ON par_mails (para);
CREATE INDEX ON par_mails USING BRIN (fecha_creado);
CREATE INDEX ON par_mails (fecha_enviado);
CREATE INDEX ON par_mails (compra_uuid);

-- Plantas-Sala
CREATE INDEX ON par_plantas_sala (sala_id);

-- Plantillas
CREATE INDEX ON par_plantillas (sala_id);

-- Reports
CREATE INDEX ON par_reports (sala_id);

-- Salas
CREATE INDEX ON par_salas (cine_id);

-- Salas-Usuarios
CREATE INDEX ON par_salas_usuarios (usuario_id, sala_id);

-- Sesiones
CREATE INDEX ON par_sesiones (evento_id);
CREATE INDEX ON par_sesiones (fecha_celebracion);
CREATE INDEX ON par_sesiones (fecha_inicio_venta_online, fecha_fin_venta_online);
CREATE INDEX ON par_sesiones (hora_apertura);
CREATE INDEX ON par_sesiones (canal_internet);
CREATE INDEX ON par_sesiones (canal_taquilla);
CREATE INDEX ON par_sesiones (plantilla_id);
CREATE INDEX ON par_sesiones (sala_id);
CREATE INDEX ON par_sesiones (incidencia_id);
CREATE INDEX ON par_sesiones (anulada);

-- Sesiones-Abonos
CREATE INDEX ON par_sesiones_abonos (sesion_id, abono_id);

-- Sesiones-Formato-IDI-ICAA
CREATE INDEX ON par_sesiones_formato_idi_icaa (evento_id);

-- Tarifas
CREATE INDEX ON par_tarifas (ispublica);
CREATE INDEX ON par_tarifas (defecto);
CREATE INDEX ON par_tarifas (cine_id);

-- Tarifas-Cines
CREATE INDEX ON par_tarifas_cines (par_cine_id, par_tarifa_id);

-- Tipos-Evento
CREATE INDEX ON par_tipos_evento (exportar_icaa);
CREATE INDEX ON par_tipos_evento (cine_id);


INSERT INTO par_version_bbdd (VERSION) VALUES ('2017-08-26.SQL');
