DO $$
DECLARE salaId INTEGER;
BEGIN
  INSERT INTO par_salas (id, codigo, nombre, asientos, asiento_disc, asiento_nores, tipo, formato, subtitulo, cine_id, html_template_name, asientos_numerados) VALUES
    (nextval('hibernate_sequence'), ?, ?, ?, ?, ?, '1', '1', '1', ?, 'butacasFragment', ?)
  RETURNING id INTO salaId;

  INSERT INTO par_salas_usuarios (id, usuario_id, sala_id)
    select DISTINCT nextval('hibernate_sequence'), u.id, salaId from par_usuarios u left join par_salas_usuarios su on su.usuario_id=u.id LEFT JOIN  par_salas s on s.id=su.sala_id LEFT JOIN par_cines c on s.cine_id=c.id
    where c.id=?4 GROUP BY  u.id;

  INSERT INTO par_localizaciones (id, codigo, nombre_es, total_entradas, nombre_va, sala_id, iniciales)
  VALUES (nextval('hibernate_sequence'), ?, 'General', ?3, 'General', salaId, ?);

  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.EntradaTaquillaReport', 'ENTRADATAQUILLA');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.EntradaReport', 'ENTRADAONLINE');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeTaquillaReport', 'pdfTaquilla');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeEfectivoReport', 'pdfEfectiu');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport', 'pdfTpv');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeEventosReport', 'pdfSGAE');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeIncidenciasReport', 'pdfIncidencias');
  INSERT INTO par_reports (id, sala_id, clase, tipo) VALUES (nextval('hibernate_sequence'), salaId, 'es.uji.apps.par.report.InformeSesionReport', 'pdfSesion');
END $$;