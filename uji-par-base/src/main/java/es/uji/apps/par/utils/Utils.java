package es.uji.apps.par.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.OrdreGrid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.ResponseBuilder;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utils
{
	private static final Logger log = LoggerFactory.getLogger(Utils.class);
	public static final String COMPRA_NOMBRE_INTERNO = "★★★★★";

	private static final String LOCALIZACION_TEATRO_ANFITEATRO_CENTRO = "anfiteatro_central";
	private static final String LOCALIZACION_TEATRO_ANFITEATRO_IMPAR = "anfiteatro_lateral_senar";
	private static final String LOCALIZACION_TEATRO_ANFITEATRO_PAR = "anfiteatro_lateral_par";
	private static final String LOCALIZACION_TEATRO_GENERAL = "general";

	public static String stripAccents(String texto) {
    	return StringUtils.stripAccents(texto);
    }
	
    public static String sha1(String string)
    {
    	log.info("Preparamos sha1 con: " + string);
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance("SHA-1");
            return byteArrayToHexString(md.digest(string.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static String byteArrayToHexString(byte[] b)
    {
        String result = "";
        for (int i = 0; i < b.length; i++)
        {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String monedaToCents(BigDecimal cantidad)
    {
        BigDecimal importeCentimos = cantidad.multiply(new BigDecimal(100));
        return Integer.toString(importeCentimos.intValue());
    }

    public static ResponseBuilder noCache(ResponseBuilder builder)
    {
        return builder.header("Cache-Control", "no-cache, no-store, must-revalidate").header("Pragma", "no-cache")
                .header("Expires", "0");
    }
    
    public static OrdreGrid getSortFromParameter(String sortParameter) {
		try {
			if (sortParameter != null && !sortParameter.equals("")) {
				Gson gson = new Gson();
				OrdreGrid sort = new OrdreGrid();
				Type collectionType = new TypeToken<List<HashMap<String,String>>>(){}.getType();
				List<HashMap<String,String>> list = gson.fromJson(sortParameter, collectionType);
				sort.setPropietat(list.get(0).get("property"));
				sort.setOrdre(list.get(0).get("direction"));
				return sort;
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static int inicializarLimitSiNecesario(int limit) {
		return (limit==0)?1000:limit;
	}

	public static int getAnuladaFromParameter(String anuladaParameter) {
		//try {
			if (anuladaParameter != null && !anuladaParameter.equals("")) {
				Gson gson = new Gson();
				
				Type collectionType = new TypeToken<List<HashMap<String, Object>>>(){}.getType();
				List<HashMap<String, Object>> list = gson.fromJson(anuladaParameter, collectionType);
				String value = list.get(0).get("value").toString();
				return (int) Double.parseDouble(value);
			} else
				return 0;
		/*} catch (Exception e) {
			return 0;
		}*/
	}
	
	public static String toUppercaseFirst(String text)
	{
	    return Character.toUpperCase(text.charAt(0)) + text.substring(1); 
	}
	
	public static String safeObjectToString(Object object) {
		if (object != null)
			return object.toString();
		else
			return "";
	}

	public static int safeObjectToInt(Object object) {
		if (object == null)
			return 0;
		else
			return (Integer) object;
	}
	
	public static int safeObjectBigDecimalToInt(Object object) {
		if (object == null)
			return 0;
		else
			return ((BigDecimal) object).intValue();
	}
	
    public static long safeObjectBigDecimalToLong(Object object) {
        if (object == null)
            return 0;
        else
            return ((BigDecimal) object).longValue();
    }	
	
	public static float safeObjectToFloat(Object object) {
		if (object == null)
			return 0;
		else
			return ((BigDecimal) object).floatValue();
	}

	public static Date objectToDate(Object object) {
		return (Date) object;
	}
	
	public static boolean isAsientosNumerados(Evento evento)
	{
		// TODO: Esto se hacía comparando con BigDecimal.ONE
		if (evento.getAsientosNumerados() == null)
			return false;
		return evento.getAsientosNumerados().equals(true);
	}
	
	public static List<Long> listIntegerToListLong(List<Integer> enteros) {
		List<Long> listaLong = new ArrayList<Long>();
		for (Integer entero: enteros) {
			listaLong.add(Long.valueOf(entero));
		}
		return listaLong;
	}

	public static String sinHTTPS(String url)
	{
		//return url.replaceFirst("^https://", "http://");
		return url;
	}

	public static String sinUnicodes(String text)
	{
		return text != null ? text.replaceAll("\\u2028", "") : null;
	}

	public static boolean isCompraInterna(final CompraDTO compra) {
		return compra.getTaquilla()
				&& compra.getObservacionesReserva() != null
				&& COMPRA_NOMBRE_INTERNO.equals(compra.getNombre())
				&& COMPRA_NOMBRE_INTERNO.equals(compra.getApellidos());
	}

	public static boolean isCompraInterna(final Compra compra) {
		return compra.isTaquilla()
				&& compra.getObservacionesReserva() != null
				&& COMPRA_NOMBRE_INTERNO.equals(compra.getNombre())
				&& COMPRA_NOMBRE_INTERNO.equals(compra.getApellidos());
	}

	public static void setCompraInterna(final CompraDTO compra) {
		compra.setTaquilla(true);
		compra.setNombre(Utils.COMPRA_NOMBRE_INTERNO);
		compra.setApellidos(Utils.COMPRA_NOMBRE_INTERNO);
	}

	/**
	 * Devuelve los datos de numeración de la fila correspondiente a la butaca
	 * indicada
	 *
	 * @param butaca
	 * @return FilaNumeracion
	 */
	public static FilaNumeracion getFilaNumeracion(final Butaca butaca) {
		final String localizacion = butaca.getLocalizacion();
		final int fila = Integer.parseInt(butaca.getFila());
		final boolean par = Integer.parseInt(butaca.getNumero()) % 2 == 0;

		return getFilaNumeracion(localizacion, fila, par);
	}

	/**
	 * Devuelve el número de filas para la zona indicada
	 *
	 * @return
	 */
	public static int getFilas(final String localizacion) {
		final int filas;
		switch (localizacion) {
		case LOCALIZACION_TEATRO_ANFITEATRO_CENTRO:
			filas = 5;
			break;
		case LOCALIZACION_TEATRO_ANFITEATRO_IMPAR:
		case LOCALIZACION_TEATRO_ANFITEATRO_PAR:
			filas = 6;
			break;
		case LOCALIZACION_TEATRO_GENERAL:
			filas = 12;
			break;
		default:
			filas = 0;
		}

		return filas;
	}

	/**
	 * Devuelve los datos de numeración de la fila correspondiente a la ubicación
	 * indicada
	 *
	 * @param localizacion
	 * @param fila
	 * @param par          true si la numeración de la subzona es par
	 * @return FilaNumeracion
	 */
	public static FilaNumeracion getFilaNumeracion(final String localizacion, final int fila, final boolean par) {
		final int primera;
		final int ultima;
		final int paso;

		switch (localizacion) {
		case LOCALIZACION_TEATRO_ANFITEATRO_CENTRO:
			primera = 1;
			ultima = 7;
			paso = 1;
			break;
		case LOCALIZACION_TEATRO_ANFITEATRO_IMPAR:
			primera = 1;
			if (fila <= 2) {
				ultima = 13;
			} else if (fila <= 4) {
				ultima = 15;
			} else {
				ultima = 17;
			}
			paso = 2;
			break;
		case LOCALIZACION_TEATRO_ANFITEATRO_PAR:
			primera = 2;
			ultima = 14;
			paso = 2;
			break;
		case LOCALIZACION_TEATRO_GENERAL:
			if (fila == 1) {
				paso = 2;
				if (par) {
					// Butacas pares
					primera = 2;
					ultima = 18;
				} else {
					// Butacas impares
					primera = 1;
					ultima = 17;
				}
			} else if (par) {
				// Resto de butacas pares
				primera = 2;
				ultima = 20;
				paso = 2;
			} else {
				// Resto de butacas impares
				primera = 1;
				paso = 2;
				if (fila == 2) {
					ultima = 17;
				} else if (fila <= 4) {
					ultima = 19;
				} else if (fila <= 6) {
					ultima = 21;
				} else if (fila <= 8) {
					ultima = 23;
				} else {
					ultima = 25;
				}
			}
			break;
		default:
			return null;
		}

		return new FilaNumeracion(localizacion, fila, primera, ultima, paso);
	}
}
