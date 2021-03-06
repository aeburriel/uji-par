CREATE TABLE "PAR_TPVS"
(
  "ID" NUMBER,
  "NOMBRE" VARCHAR2(255) NOT NULL,
  "CODE" VARCHAR2(255) NOT NULL,
  "CURRENCY" VARCHAR2(255) NOT NULL,
  "TERMINAL" VARCHAR2(255) NOT NULL,
  "TRANSACTION_CODE" VARCHAR2(255) NOT NULL,
  "ORDER_PREFIX" VARCHAR2(255) NOT NULL,
  "LANG_CA_CODE" VARCHAR2(255) NOT NULL,
  "LANG_ES_CODE" VARCHAR2(255) NOT NULL,
  "URL" VARCHAR2(255) NOT NULL,
  "WSDL_URL" VARCHAR2(255) NOT NULL,
  "SECRET" VARCHAR2(255) NOT NULL,
  "DEFAULT_TPV" NUMBER(1,0) DEFAULT 0,
  CONSTRAINT "PAR_TPVS_PKEY" PRIMARY KEY ("ID")
);

ALTER TABLE PAR_EVENTOS ADD (
  TPV_ID integer DEFAULT 1
);

--Insert de el TPV de Benicassim, Vila-real con ID = 0 para ponerlo por defecto en la relación entre evento y TPV
INSERT INTO PAR_TPVS (ID, NOMBRE, CODE, CURRENCY, TERMINAL, TRANSACTION_CODE, ORDER_PREFIX, LANG_CA_CODE, LANG_ES_CODE, URL, WSDL_URL, SECRET, DEFAULT_TPV)
              VALUES (1, 'PARANIMF', '', '', '', '', '', '', '', '', 'http://ujiapps.uji.es/par-public/rest/tpv/resultado', 'ertyudfghjcvbnm', 1);

ALTER TABLE PAR_EVENTOS ADD CONSTRAINT "PAR_EVENTOS_TPVS_FK1" FOREIGN KEY ("TPV_ID") REFERENCES "PAR_TPVS" ("ID") ENABLE;

INSERT INTO par_version_bbdd (VERSION) VALUES ('2014-12-18.SQL');