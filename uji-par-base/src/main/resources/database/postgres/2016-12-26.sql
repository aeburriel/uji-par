CREATE TYPE mediopago AS ENUM ('METALICO', 'TARJETA', 'TARJETAOFFLINE', 'TRANSFERENCIA');

CREATE FUNCTION par_mediopago_cast(varchar) RETURNS mediopago AS $$
    SELECT ('' || UPPER($1))::mediopago;
$$ LANGUAGE SQL;

CREATE FUNCTION lower(mediopago) RETURNS varchar AS $$
    SELECT LOWER($1::varchar);
$$ LANGUAGE SQL;

CREATE CAST (varchar AS mediopago) WITH FUNCTION par_mediopago_cast(varchar) AS ASSIGNMENT;

ALTER TABLE par_compras ALTER COLUMN tipo SET DATA TYPE mediopago;


INSERT INTO par_version_bbdd (VERSION) VALUES ('2016-12-26.SQL');
