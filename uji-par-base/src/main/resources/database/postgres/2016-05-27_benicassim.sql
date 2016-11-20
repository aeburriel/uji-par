INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (1, 1, 'ENTRADATAQUILLA', 'es.uji.apps.par.report.EntradaTaquillaReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (2, 1, 'ENTRADAONLINE', 'es.uji.apps.par.report.EntradaReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (3, 1, 'pdfTaquilla', 'es.uji.apps.par.report.InformeTaquillaReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (4, 1, 'pdfEfectiu', 'es.uji.apps.par.report.InformeEfectivoReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (5, 1, 'pdfTpv', 'es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (6, 1, 'pdfSGAE', 'es.uji.apps.par.report.InformeEventosReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (7, 1, 'pdfIncidencias', 'es.uji.apps.par.report.InformeIncidenciasReport');
INSERT INTO par_reports("id", sala_id, tipo, clase) VALUES (8, 1, 'pdfSesion', 'es.uji.apps.par.report.InformeSesionReport');

-- Añadimos Administrador por defecto con permisos para todas las salas
INSERT INTO par_usuarios("id", nombre, usuario, mail) VALUES (1, 'Administrador', 'admin', 'teatreadmin@benicassim.org') ON CONFLICT DO NOTHING;
INSERT INTO par_salas_usuarios("id", usuario_id, sala_id) VALUES (1, 1, 1);
INSERT INTO par_salas_usuarios("id", usuario_id, sala_id) VALUES (2, 1, 2);
