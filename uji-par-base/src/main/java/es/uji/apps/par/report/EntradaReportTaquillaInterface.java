package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public interface EntradaReportTaquillaInterface {
	EntradaReportTaquillaInterface create(Locale locale, Configuration configuration) throws SAXException, IOException;
	void setTitulo(String titulo);
	void setFecha(String fecha);
	void setHora(String hora);
	void setHoraApertura(String horaApertura);
	void setCif(String cif);
	void setPromotor(String promotor);
	void setNifPromotor(String nifPromotor);
	void generaPaginaButaca(EntradaModelReport entrada, String urlPublic);
	void generaPaginasReciboPinpad(String reciboPinpad);
	void serialize(OutputStream output) throws ReportSerializationException;
	void setUrlPortada(String urlPortada);
	void setNombreEntidad(String nombreEntidad);
	void setDireccion(String direccion);
}
