insert into PAR_USUARIOS (ID, NOMBRE, USUARIO, MAIL, URL) VALUES (1,'Sergio', 'sergio', 'sergio4tic.com', 'localhost');
insert into PAR_CINES (ID, CODIGO, NOMBRE, CIF, DIRECCION, COD_MUNICIPIO, NOM_MUNICIPIO, CP, EMPRESA, COD_REGISTRO, TFNO, IVA, URL_PUBLIC)
                VALUES (1, ''      , ''   , ''  , ''       , ''           , ''         , '', ''      , ''          , ''  , 0, 'https://ejemplo.de.url');
insert into PAR_SALAS (ID, CODIGO, NOMBRE, ASIENTOS, ASIENTO_DISC, ASIENTO_NORES, TIPO, FORMATO, SUBTITULO, CINE_ID, HTML_TEMPLATE_NAME, ASIENTOS_NUMERADOS)
 VALUES (1, '', '', 1, 1, 0, '', '', '', 1, '', 1);
insert into PAR_SALAS_USUARIOS (id, USUARIO_ID, SALA_ID) VALUES (1, 1, 1);