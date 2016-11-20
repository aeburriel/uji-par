package es.uji.apps.par.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import es.uji.apps.par.config.Configuration;
import org.xml.sax.SAXException;

import es.uji.apps.fopreports.serialization.ReportSerializationException;

public interface EntradaReportTaquillaInterface {
	public EntradaReportTaquillaInterface create(Locale locale, Configuration configuration) throws SAXException, IOException;
	public void setTitulo(String titulo);
	public void setFecha(String fecha);
	public void setHora(String hora);
	public void setHoraApertura(String horaApertura);
	public void generaPaginaButaca(EntradaModelReport entrada, String urlPublic);
	public void generaPaginasReciboPinpad(String reciboPinpad);
	public void serialize(OutputStream output) throws ReportSerializationException;
	public void setUrlPortada(String urlPortada);
}
