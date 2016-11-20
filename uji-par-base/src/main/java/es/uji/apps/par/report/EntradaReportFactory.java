package es.uji.apps.par.report;

import es.uji.apps.par.config.Configuration;

public class EntradaReportFactory
{
	public static final String TIPO_ENTRADA_TAQUILLA = "ENTRADATAQUILLA";
	public static final String TIPO_ENTRADA_ONLINE = "ENTRADAONLINE";
	public static final String TIPO_INFORME_PDF_TAQUILLA = "pdfTaquilla";
	public static final String TIPO_INFORME_PDF_EFECTIVO = "pdfEfectiu";
	public static final String TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES = "pdfTpv";
	public static final String TIPO_INFORME_PDF_EVENTOS = "pdfSGAE";
	public static final String TIPO_INFORME_PDF_SESIONES = "pdfSesion";

	public static EntradaReportTaquillaInterface newInstanceTaquilla(String clase)
    {
    	try {
    		return (EntradaReportTaquillaInterface) Class.forName(clase).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de entrada de taquilla: " + clase);
    	}
    }
    
    public static EntradaReportOnlineInterface newInstanceOnline(String clase)
    {
    	try {
    		return (EntradaReportOnlineInterface) Class.forName(clase).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de entrada de venta online: " + clase);
    	}
    }

    public static EntradaReportOnlineInterface newInstanceByClassName(String className)
    {
        try {
            return (EntradaReportOnlineInterface) Class.forName(className).newInstance();
        } catch(Exception e) {
            throw new RuntimeException("Imposible instanciar la clase del pdf de entrada de venta online: " + className);
        }
    }

	public static InformeInterface newInstanceInformeTaquilla(String className) {
		try {
			return (InformeInterface) Class.forName(className).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de informe de taquilla: " + className);
    	}
	}

	public static InformeInterface newInstanceInformeEfectivo(String className) {
		try {
    		return (InformeInterface) Class.forName(className).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de informe de efectivo: " + className);
    	}
	}

	public static InformeInterface newInstanceInformeTaquillaTpvSubtotalesReport(String className) {
		try {
    		return (InformeInterface) Class.forName(className).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de informe de taquilla tpv subtotales: " + className);
    	}
	}

	public static InformeInterface newInstanceInformeEventosReport(String className) {
		try {
    		return (InformeInterface) Class.forName(className).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de informe de eventos: " + className);
    	}
	}

	public static InformeInterface newInstanceInformeSesionReport(String className) {
		try {
    		return (InformeInterface) Class.forName(className).newInstance();
    	} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase del pdf de informe de sesión: " + className);
    	}
	}

    public static InformeInterface newInstanceInformeReport(String tipo, Configuration configuration) {
        try {
            return (InformeInterface) Class.forName(configuration.getInformeReport(tipo)).newInstance();
        } catch(Exception e) {
            throw new RuntimeException("Imposible instanciar la clase del pdf de informe: " + tipo);
        }
    }
}
