insert into par_reports("id", sala_id, tipo, clase) values (1, 1, 'ENTRADATAQUILLA', 'es.uji.apps.par.report.EntradaTaquillaReport');
insert into par_reports("id", sala_id, tipo, clase) values (2, 1, 'ENTRADAONLINE', 'es.uji.apps.par.report.EntradaReport');
insert into par_reports("id", sala_id, tipo, clase) values (3, 1, 'pdfTaquilla', 'es.uji.apps.par.report.InformeTaquillaReport');
insert into par_reports("id", sala_id, tipo, clase) values (4, 1, 'pdfEfectiu', 'es.uji.apps.par.report.InformeEfectivoReport');
insert into par_reports("id", sala_id, tipo, clase) values (5, 1, 'pdfTpv', 'es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport');
insert into par_reports("id", sala_id, tipo, clase) values (6, 1, 'pdfSGAE', 'es.uji.apps.par.report.InformeEventosReport');
insert into par_reports("id", sala_id, tipo, clase) values (7, 1, 'pdfIncidencias', 'es.uji.apps.par.report.InformeIncidenciasReport');
insert into par_reports("id", sala_id, tipo, clase) values (8, 1, 'pdfSesion', 'es.uji.apps.par.report.InformeSesionReport');

--Dar de alta a Silvia y al resto de usuarios para que vean lo que les corresponda
insert into par_cines_usuario();
