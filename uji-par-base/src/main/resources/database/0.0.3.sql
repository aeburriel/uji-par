DELETE FROM PAR_PRECIOS_PLANTILLA WHERE LOCALIZACION_ID = (SELECT ID FROM PAR_LOCALIZACIONES WHERE NOMBRE_ES LIKE '%Discapacitados anfiteatro%');
DELETE FROM PAR_PRECIOS_SESION WHERE LOCALIZACION_ID = (SELECT ID FROM PAR_LOCALIZACIONES WHERE NOMBRE_ES LIKE '%Discapacitados anfiteatro%');
DELETE FROM PAR_BUTACAS WHERE LOCALIZACION_ID = (SELECT ID FROM PAR_LOCALIZACIONES WHERE NOMBRE_ES LIKE '%Discapacitados anfiteatro%');
DELETE FROM PAR_LOCALIZACIONES WHERE NOMBRE_ES LIKE '%Discapacitados anfiteatro%';