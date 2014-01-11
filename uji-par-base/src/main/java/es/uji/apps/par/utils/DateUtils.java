package es.uji.apps.par.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import es.uji.apps.par.config.Configuration;

public class DateUtils
{
    private static final SimpleDateFormat FORMAT_DAY = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATABASE_DAY = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat FORMAT_HOUR = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat FORMAT_DAY_HOUR = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat DATABASE_WITH_SECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FORMAT_DDMMYY = new SimpleDateFormat("ddMMyy");

    public static Date spanishStringToDate(String spanishDate)
    {
    	Calendar cal = Calendar.getInstance();
    	
    	// Si no los millis son los del momento actual
    	cal.setTimeInMillis(0);
    	
        if (spanishDate == null || spanishDate.isEmpty())
            return null;
        
        if (isTimestamp(spanishDate)) 
        {
            cal.setTimeInMillis(Long.valueOf(spanishDate));
        }
        else
        {
	        try
            {
                cal.setTime(FORMAT_DAY.parse(spanishDate));
            }
            catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        } 
        
        return cal.getTime();
    }

    private static boolean isTimestamp(String spanishDate) {
    	if (spanishDate.contains("/"))
    		return false;
    	else
    		return true;
	}

	public static Timestamp dateToTimestampSafe(Date fecha)
    {
        if (fecha == null)
            return null;
        else
            return new Timestamp(fecha.getTime());
    }

    public static String getHourAndMinutesWithLeadingZeros(Date date)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
        return date_format.format(date);
    }
    
    public static Date addTimeToDate(Date startDate, String hour)
    {
        if (startDate == null)
        {
            return null;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        
        if (hour != null && !hour.equals(""))
        {
            String[] arrHoraMinutos = hour.split(":");
    
            int hora = Integer.parseInt(arrHoraMinutos[0]);
            int minutos = Integer.parseInt(arrHoraMinutos[1]);
    
            cal.set(Calendar.HOUR_OF_DAY, hora);
            cal.set(Calendar.MINUTE, minutos);
            cal.set(Calendar.SECOND, 0);
        }

        return cal.getTime();
    }
    
    public static String dateToSpanishString(Date fecha) {
    	if (fecha == null)
    		throw new NullPointerException();
    	
    	return FORMAT_DAY.format(fecha);
    }
    
    public static String dateToHourString(Date fecha) {
        if (fecha == null)
            throw new NullPointerException();

        return FORMAT_HOUR.format(fecha);
    }
    
    public static long millisecondsToSeconds(long time) {
    	return time/1000;
    }

	public static Object dateToSpanishStringWithHour(Date fecha) {
		if (fecha == null)
    		throw new NullPointerException();
    	
    	return FORMAT_DAY_HOUR.format(fecha);
	}
	
    public static Date spanishStringWithHourstoDate(String spanishDate) throws ParseException {
        return FORMAT_DAY_HOUR.parse(spanishDate);
    }
   
    public static Date databaseWithSecondsToDate(String dateString) throws ParseException {
       return DATABASE_WITH_SECONDS.parse(dateString);
    }
	
	public static Date databaseStringToDate(String databaseDate) throws ParseException {
	    return DATABASE_DAY.parse(databaseDate);
	}
    
    /**
     * Se usa para mostrar eventos y sesiones como activos pasados X minutos de la hora del espectáculo
     */
    public static Date dateConMargenTrasVenta()
    {
        Calendar limite = Calendar.getInstance();
        limite.add(Calendar.MINUTE, -Configuration.getMargenVentaTaquillaMinutos());
        
        return limite.getTime();
    }
    
    public static String formatDdmmyy(Date fecha)
    {
        return FORMAT_DDMMYY.format(fecha);
    }
}
