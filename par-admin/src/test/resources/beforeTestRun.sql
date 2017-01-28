insert into PAR_USUARIOS (ID, NOMBRE, USUARIO, MAIL, URL) VALUES (1,'Sergio', 'sergio', 'sergio4tic.com', 'localhost');
insert into PAR_CINES (ID, CODIGO, NOMBRE, CIF, DIRECCION, COD_MUNICIPIO, NOM_MUNICIPIO, CP, EMPRESA, COD_REGISTRO, TFNO, IVA, URL_PUBLIC, URL_PRIVACIDAD, URL_COMO_LLEGAR, URL_PIE_ENTRADA, MAIL_FROM, LOGO_REPORT)
                VALUES (1, ''      , 'CINE NOMBRE'   , ''  , ''       , ''           , ''         , '', ''      , ''          , ''  , 0, 'https://ejemplo.de.url', 'http://example.com/condiciones.html', 'http://example.com/documento.pdf', 'http://example.com/example.jpg', 'mailFrom', 'logo-vertical-color.svg');
insert into PAR_SALAS (ID, CODIGO, NOMBRE, ASIENTOS, ASIENTO_DISC, ASIENTO_NORES, TIPO, FORMATO, SUBTITULO, CINE_ID, HTML_TEMPLATE_NAME, ASIENTOS_NUMERADOS)
 VALUES (1, '', '', 1, 1, 0, '', '', '', 1, '', 1);
insert into PAR_SALAS_USUARIOS (id, USUARIO_ID, SALA_ID) VALUES (1, 1, 1);
INSERT INTO PAR_TPVS (id, nombre, code, currency, terminal, transaction_code, order_prefix, lang_ca_code, lang_es_code, url, wsdl_url, secret, cif, signature_method)
 VALUES (1, 'Palacio de Congresos', '43242', '978', '00000001', '2', '0000554027', '2', '1', 'https://pgw.ceca.es/cgi-bin/tpv', '-', '321312', 'A-3213', 'CECA_SHA1');
INSERT INTO PAR_TPVS_CINES (id, tpv_id, cine_id) VALUES (1, 1, 1);
INSERT INTO PAR_TIPOS_EVENTO (id, nombre_es, nombre_va, exportar_icaa, cine_id) VALUES (1, 'Teatro', 'Teatre', false, 1);