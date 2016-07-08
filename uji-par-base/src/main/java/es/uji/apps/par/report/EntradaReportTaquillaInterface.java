package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public interface EntradaReportTaquillaInterface {
	public EntradaReportTaquillaInterface create(Locale locale, Configuration configuration) throws SAXException, IOException;
	public void setTitulo(String titulo);
	public void setFecha(String fecha);
	public void setHora(String hora);
	public void setHoraApertura(String horaApertura);
	public void setCif(String cif);
	public void setPromotor(String promotor);
	public void setNifPromotor(String nifPromotor);
	public void generaPaginaButaca(EntradaModelReport entrada, String urlPublic);
	public void generaPaginasReciboPinpad(String reciboPinpad);
	public void serialize(OutputStream output) throws ReportSerializationException;
	public void setUrlPortada(String urlPortada);
}
