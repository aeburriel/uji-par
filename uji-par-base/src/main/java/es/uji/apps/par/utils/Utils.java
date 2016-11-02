package es.uji.apps.par.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
			listaLong.add(new Long(entero));
		}
		return listaLong;
	}

	public static String sinHTTPS(String url)
	{
		return url.replaceFirst("^https://", "http://");
	}

	public static String sinUnicodes(String text)
	{
		return text.replaceAll("\\u2028", "");
	}
}
