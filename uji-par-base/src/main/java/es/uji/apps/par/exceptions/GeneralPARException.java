package es.uji.apps.par.exceptions;

import es.uji.apps.par.config.Configuration;

import javax.ws.rs.WebApplicationException;
import java.util.HashMap;

@SuppressWarnings("serial")
public class GeneralPARException extends WebApplicationException
{
    protected static HashMap<Integer, String> messages;

    public static final String ERROR_GENERAL = "Error general";
    public static final String REQUIRED_FIELD = "El campo es obligatorio: ";
    public static final String BUTACA_OCUPADA = "Butaca ocupada: "; 
    public static final String TIPO_INCORRECTO_COMPRA_INTERNET = "Compra de tipo Aula de Teatro por internet";
    public static final String COMPRA_BUTACA_DESCUENTO_NO_DISPONIBLE = "Se ha intentado comprar una butaca con descuento cero";
    public static final String COMPRA_BUTACA_NO_EXISTENTE = "Se ha intentado comprar una butaca que no existe";
    public static final String COMPRA_INVITACION_INTERNET_NO_DISPONIBLE = "Compra de entrada tipo invitación por internet";
    public static final String COMPRA_SIN_BUTACAS = "Compra sin butacas seleccionadas";
    public static final String EVENTO_NO_ENCONTRADO = "Evento no encontrado: "; 
    public static final String FECHAS_INVALIDAS = "Fechas inválidas ";
    public static final String VENTA_FUERA_DE_PLAZO = "Fuera de plazo para venta por internet: ";
    public static final String SESION_SIN_BUTACAS_LIBRES = "No hay butacas libres: ";
    public static final String PRECIO_REPETIDO = "Precio repetido";
    public static final String USUARIO_YA_EXISTE = "Ya existe un usuario con este nombre de usuario";
    public static final String TIPO_ENVIO_INVALIDO = "El tipo de envío es inválido. Debe ser FL o AT";
    public static final String EVENTO_SIN_IVA = "Evento sin IVA: "; 
    public static final String SESION_CON_COMPRAS = "La sesión ya tiene compras realizadas";
    public static final String EVENTO_SIN_FORMATO = "Evento sin formato: ";
    public static final String EVENTO_SIN_SESIONES = "Evento sin sesiones para estos datos: ";
    public static final String SESIONES_CON_FICHEROS_YA_GENERADOS = "Existen sesiones con ficheros ya generados";
    public static final String SALA_NULA = "El código de sala de la sesión programada es nulo";
    public static final String FECHA_SESION_PROGRAMADA_NULA = "La fecha de la sesión programada es nula";
    public static final String HORA_SESION_PROGRAMADA_NULA = "La hora de la sesión de emisión de la película es nula";
    public static final String FORMATO_HORA_INCORRECTO = "El formato de la hora es incorrecto, tienen que ser 4 carácteres";
    public static final String NOMBRE_SALA_NULO = "El nombre de la sala es nulo";
    public static final String CODIGO_CINE_NULO = "El código de cine es nulo";
    public static final String TIPO_ENVIO_NULO = "El tipo de envío es nulo";
    public static final String FORMATO_CODIGO_CINE_INCORRECTO = "El código del cine es un string de tamaño distinto de 3 carácteres";
    public static final String RECAUDACION_NULA = "La recaudacion de la sesión es nula";
    public static final String INCIDENCIAS_NULAS = "La sesión tiene las incidencias nulas. Si no tiene incidencias debe tener el valor correspondiente a SIN_INCIDENCIAS";
    public static final String CODIGO_PELICULA_NULO = "El código de película es nulo";
    public static final String CODIGO_EXPEDIENTE_NULO = "El código de expediente es nulo";
    public static final String TITULO_PELICULA_NULO = "El titulo de la película es nulo";
    public static final String CODIGO_DISTRIBUIDORA_NULO = "El codigo de la distribuidora de la película es nulo";
    public static final String NOMBRE_DISTRIBUIDORA_NULO = "El nombre de la distribuidora de la película es nulo";
    public static final String VERSION_ORIGINAL_NULO = "El campo versión original de la película es nulo";
    public static final String VERSION_LINGUISTICA_NULO = "La version linguística de la película es nula";
    public static final String IDIOMA_SUBTITULOS_NULO = "El campo de idioma de los subtitulos de la película es nulo";
    public static final String FORMATO_PROYECCION_NULO = "El formato de proyeccion de la película es nulo";
    public static final String CODIGO_PELICULA_LARGO = "El código de la película tiene más de 5 carácteres";
    public static final String CODIGO_EXPEDIENTE_LARGO = "El codigo de expediente de la película tiene más de 12 carácteres";
    public static final String CODIGO_DISTRIBUIDORA_LARGO = "El codigo de la distribuidora de la película tiene más de 12 carácteres";
    public static final String NOMBRE_DISTRIBUIDORA_LARGO = "El nombre de la distribuidora de la película tiene más de 50 carácteres";
    public static final String DIGITOS_VERSION_ORIGINAL = "El campo versionOriginal de la película tiene que ser 1 dígito";
    public static final String DIGITOS_VERSION_LINGUISTICA = "El campo versionLinguistica de la película tiene que ser 1 dígito";
    public static final String DIGITOS_IDIOMA_SUBTITULOS = "El campo idiomaSubtitulos de la película tiene que ser 1 dígito";
    public static final String DIGITOS_FORMATO_PROYECCION = "El campo formatoProyeccion de la película tiene que ser 1 dígito";
    public static final String CODIGO_SALA_LARGO = "El código de sala es un string de tamaño mayor de 6 carácteres";
    public static final String ANTICIPADA_FORMAT_ERROR = "Formato incorrecto de la propiedad " + Configuration.HORAS_VENTA_ANTICIPADA;
    public static final String NOT_FOUND_INCIDENCIA = "Incidencia no encontrada";
	public static final String EDICION_ANULADA_EXCEPTION = "No se puede editar una sesión anulada";
    public static final String ABONO_CON_ABONADOS = "Abono con abonados";

    public static final Integer ERROR_GENERAL_CODE = 500;
    public static final Integer REQUIRED_FIELD_CODE = 501;
    public static final Integer BUTACA_OCUPADA_CODE = 502;
    public static final Integer TIPO_INCORRECTO_COMPRA_INTERNET_CODE = 503;
    public static final Integer COMPRA_BUTACA_DESCUENTO_NO_DISPONIBLE_CODE = 504;
    public static final Integer COMPRA_INVITACION_INTERNET_NO_DISPONIBLE_CODE = 505;
    public static final Integer COMPRA_SIN_BUTACAS_CODE = 506;
    public static final Integer EVENTO_NO_ENCONTRADO_CODE = 507;
    public static final Integer FECHAS_INVALIDAS_CODE = 508;
    public static final Integer VENTA_FUERA_DE_PLAZO_CODE = 509;
    public static final Integer SESION_SIN_BUTACAS_LIBRES_CODE = 510;
    public static final Integer PRECIO_REPETIDO_CODE = 511;
    public static final Integer USUARIO_YA_EXISTE_CODE = 512;
    public static final Integer TIPO_ENVIO_INVALIDO_CODE = 513;
    public static final Integer EVENTO_SIN_IVA_CODE = 514;
    public static final Integer SESION_CON_COMPRAS_CODE = 515;
    public static final Integer EVENTO_SIN_FORMATO_CODE = 516;
    public static final Integer EVENTO_SIN_SESIONES_CODE = 517;
    public static final Integer SESIONES_CON_FICHEROS_YA_GENERADOS_CODE = 518;
    public static final Integer SALA_NULA_CODE = 519;
    public static final Integer FECHA_SESION_PROGRAMADA_NULA_CODE = 520;
    public static final Integer HORA_SESION_PROGRAMADA_NULA_CODE = 521;
    public static final Integer FORMATO_HORA_INCORRECTO_CODE = 522;
    public static final Integer NOMBRE_SALA_NULO_CODE = 523;
    public static final Integer CODIGO_CINE_NULO_CODE = 524;
    public static final Integer TIPO_ENVIO_NULO_CODE = 525;
    public static final Integer FORMATO_CODIGO_CINE_INCORRECTO_CODE = 526;
    public static final Integer RECAUDACION_NULA_CODE = 527;
    public static final Integer INCIDENCIAS_NULAS_CODE = 528; 
    public static final Integer CODIGO_PELICULA_NULO_CODE = 529;
    public static final Integer CODIGO_EXPEDIENTE_NULO_CODE = 530;
    public static final Integer TITULO_PELICULA_NULO_CODE = 531;
    public static final Integer CODIGO_DISTRIBUIDORA_NULO_CODE = 532;
    public static final Integer NOMBRE_DISTRIBUIDORA_NULO_CODE = 533;
    public static final Integer VERSION_ORIGINAL_NULO_CODE = 534;
    public static final Integer VERSION_LINGUISTICA_NULO_CODE = 535;
    public static final Integer IDIOMA_SUBTITULOS_NULO_CODE = 536;
    public static final Integer FORMATO_PROYECCION_NULO_CODE = 537;
    public static final Integer CODIGO_PELICULA_LARGO_CODE = 538;
    public static final Integer CODIGO_EXPEDIENTE_LARGO_CODE = 539;
    public static final Integer CODIGO_DISTRIBUIDORA_LARGO_CODE = 540;
    public static final Integer NOMBRE_DISTRIBUIDORA_LARGO_CODE = 541;
    public static final Integer DIGITOS_VERSION_ORIGINAL_CODE = 542;
    public static final Integer DIGITOS_VERSION_LINGUISTICA_CODE = 543;
    public static final Integer DIGITOS_IDIOMA_SUBTITULOS_CODE = 544;
    public static final Integer DIGITOS_FORMATO_PROYECCION_CODE = 545;
    public static final Integer CODIGO_SALA_LARGO_CODE = 546;
    public static final Integer ANTICIPADA_FORMAT_ERROR_CODE = 547;
    public static final Integer NOT_FOUND_INCIDENCIA_CODE = 548;
	public static final Integer EDICION_ANULADA_EXCEPTION_CODE = 549;
    public static final Integer ABONO_CON_ABONADOS_CODE = 550;
    public static final Integer COMPRA_BUTACA_NO_EXISTENTE_CODE = 551;

    static {
		messages = new HashMap<Integer,String>();
		messages.put(ERROR_GENERAL_CODE, ERROR_GENERAL);
		messages.put(REQUIRED_FIELD_CODE, REQUIRED_FIELD);
		messages.put(BUTACA_OCUPADA_CODE, BUTACA_OCUPADA);
		messages.put(TIPO_INCORRECTO_COMPRA_INTERNET_CODE, TIPO_INCORRECTO_COMPRA_INTERNET);
		messages.put(COMPRA_BUTACA_DESCUENTO_NO_DISPONIBLE_CODE, COMPRA_BUTACA_DESCUENTO_NO_DISPONIBLE);
		messages.put(COMPRA_INVITACION_INTERNET_NO_DISPONIBLE_CODE, COMPRA_INVITACION_INTERNET_NO_DISPONIBLE);
		messages.put(COMPRA_SIN_BUTACAS_CODE, COMPRA_SIN_BUTACAS);
		messages.put(EVENTO_NO_ENCONTRADO_CODE, EVENTO_NO_ENCONTRADO);
		messages.put(FECHAS_INVALIDAS_CODE, FECHAS_INVALIDAS);
		messages.put(VENTA_FUERA_DE_PLAZO_CODE, VENTA_FUERA_DE_PLAZO);
		messages.put(SESION_SIN_BUTACAS_LIBRES_CODE, SESION_SIN_BUTACAS_LIBRES);
		messages.put(PRECIO_REPETIDO_CODE, PRECIO_REPETIDO);
		messages.put(USUARIO_YA_EXISTE_CODE, USUARIO_YA_EXISTE);
		messages.put(TIPO_ENVIO_INVALIDO_CODE, TIPO_ENVIO_INVALIDO);
		messages.put(EVENTO_SIN_IVA_CODE, EVENTO_SIN_IVA);
		messages.put(SESION_CON_COMPRAS_CODE, SESION_CON_COMPRAS);
		messages.put(EVENTO_SIN_FORMATO_CODE, EVENTO_SIN_FORMATO);
		messages.put(EVENTO_SIN_SESIONES_CODE, EVENTO_SIN_SESIONES);
		messages.put(SESIONES_CON_FICHEROS_YA_GENERADOS_CODE, SESIONES_CON_FICHEROS_YA_GENERADOS);
		messages.put(SALA_NULA_CODE, SALA_NULA);
		messages.put(FECHA_SESION_PROGRAMADA_NULA_CODE, FECHA_SESION_PROGRAMADA_NULA);
		messages.put(HORA_SESION_PROGRAMADA_NULA_CODE, HORA_SESION_PROGRAMADA_NULA);
		messages.put(FORMATO_HORA_INCORRECTO_CODE, FORMATO_HORA_INCORRECTO);
		messages.put(NOMBRE_SALA_NULO_CODE, NOMBRE_SALA_NULO);
		messages.put(CODIGO_CINE_NULO_CODE, CODIGO_CINE_NULO);
		messages.put(TIPO_ENVIO_NULO_CODE, TIPO_ENVIO_NULO);
		messages.put(FORMATO_CODIGO_CINE_INCORRECTO_CODE, FORMATO_CODIGO_CINE_INCORRECTO);
		messages.put(RECAUDACION_NULA_CODE, RECAUDACION_NULA);
		messages.put(INCIDENCIAS_NULAS_CODE, INCIDENCIAS_NULAS);
		messages.put(CODIGO_PELICULA_NULO_CODE, CODIGO_PELICULA_NULO);
		messages.put(CODIGO_EXPEDIENTE_NULO_CODE, CODIGO_EXPEDIENTE_NULO);
		messages.put(TITULO_PELICULA_NULO_CODE, TITULO_PELICULA_NULO);
		messages.put(CODIGO_DISTRIBUIDORA_NULO_CODE, CODIGO_DISTRIBUIDORA_NULO);
		messages.put(NOMBRE_DISTRIBUIDORA_NULO_CODE, NOMBRE_DISTRIBUIDORA_NULO);
		messages.put(VERSION_ORIGINAL_NULO_CODE, VERSION_ORIGINAL_NULO);
		messages.put(VERSION_LINGUISTICA_NULO_CODE, VERSION_LINGUISTICA_NULO);
		messages.put(IDIOMA_SUBTITULOS_NULO_CODE, IDIOMA_SUBTITULOS_NULO);
		messages.put(FORMATO_PROYECCION_NULO_CODE, FORMATO_PROYECCION_NULO);
		messages.put(CODIGO_PELICULA_LARGO_CODE, CODIGO_PELICULA_LARGO);
		messages.put(CODIGO_EXPEDIENTE_LARGO_CODE, CODIGO_EXPEDIENTE_LARGO);
		messages.put(CODIGO_DISTRIBUIDORA_LARGO_CODE, CODIGO_DISTRIBUIDORA_LARGO);
		messages.put(NOMBRE_DISTRIBUIDORA_LARGO_CODE, NOMBRE_DISTRIBUIDORA_LARGO);
		messages.put(DIGITOS_VERSION_ORIGINAL_CODE, DIGITOS_VERSION_ORIGINAL);
		messages.put(DIGITOS_VERSION_LINGUISTICA_CODE, DIGITOS_VERSION_LINGUISTICA);
		messages.put(DIGITOS_IDIOMA_SUBTITULOS_CODE, DIGITOS_IDIOMA_SUBTITULOS);
		messages.put(DIGITOS_FORMATO_PROYECCION_CODE, DIGITOS_FORMATO_PROYECCION);
		messages.put(CODIGO_SALA_LARGO_CODE, CODIGO_SALA_LARGO);
		messages.put(ANTICIPADA_FORMAT_ERROR_CODE, ANTICIPADA_FORMAT_ERROR);
		messages.put(NOT_FOUND_INCIDENCIA_CODE, NOT_FOUND_INCIDENCIA);
		messages.put(EDICION_ANULADA_EXCEPTION_CODE, EDICION_ANULADA_EXCEPTION);
        messages.put(ABONO_CON_ABONADOS_CODE, ABONO_CON_ABONADOS);
        messages.put(COMPRA_BUTACA_NO_EXISTENTE_CODE, COMPRA_BUTACA_NO_EXISTENTE);
    }

    public GeneralPARException(int errorCode) {
		super(new Throwable(messages.get(errorCode)), errorCode);
	}
	
	public GeneralPARException(int errorCode, String message) {
		super(new Throwable(message), errorCode);
	}

	public static Integer getCodeFromMessage(String message) {
		for (Integer key: messages.keySet()) {
			if (messages.get(key).equals(message)) {
				return key;
			} else if (message.startsWith(messages.get(key)))
				return key;
		}
		return ERROR_GENERAL_CODE;
	}
}
