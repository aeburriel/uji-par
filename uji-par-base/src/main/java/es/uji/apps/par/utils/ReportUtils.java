package es.uji.apps.par.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Calendar;

public class ReportUtils {
	private static final Logger log = LoggerFactory.getLogger(ReportUtils.class);
	
	public static String formatEuros(BigDecimal importe)
    {
        return importe.setScale(2, BigDecimal.ROUND_HALF_UP).toString().replace('.', ',');
    }
	
	public static String getMesValenciaConDe(Calendar fecha)
    {
        int mes = fecha.get(Calendar.MONTH);
        
        switch (mes)
        {
        case 0:
            return "de gener";
        case 1:
            return "de febrer";
        case 2:
            return "de març";
        case 3:
            return "d'abril";
        case 4:
            return "de maig";
        case 5:
            return "de juny";
        case 6:
            return "de juliol";
        case 7:
            return "d'agost";
        case 8:
            return "de setembre";
        case 9:
            return "d'octubre";
        case 10:
            return "de novembre";
        default:
            return "de desembre";
        }
    }

    public static String getMesEspanolConDe(Calendar fecha)
    {
        int mes = fecha.get(Calendar.MONTH);

        switch (mes)
        {
            case 0:
                return "de enero";
            case 1:
                return "de febrero";
            case 2:
                return "de marzo";
            case 3:
                return "de abril";
            case 4:
                return "de mayo";
            case 5:
                return "de junio";
            case 6:
                return "de julio";
            case 7:
                return "de agosto";
            case 8:
                return "de septiembre";
            case 9:
                return "de octubre";
            case 10:
                return "de noviembre";
            default:
                return "de diciembre";
        }
    }
}
