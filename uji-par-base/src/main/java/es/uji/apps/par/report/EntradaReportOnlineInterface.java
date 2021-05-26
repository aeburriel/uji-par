package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public interface EntradaReportOnlineInterface {
	EntradaReportOnlineInterface create(Locale locale, Configuration configuration) throws SAXException, IOException;
	void setTitulo(String titulo);
	void setFecha(String fecha);
	void setHora(String hora);
	void setHoraApertura(String horaApertura);
	void setEmpresa(String empresa);
	void setCif(String cif);
	void setPromotor(String promotor);
	void setNifPromotor(String nifPromotor);
	void generaPaginaButaca(EntradaModelReport entrada, String urlPublic);
	void serialize(OutputStream output) throws ReportSerializationException;
	void setUrlPublicidad(String urlPublicidad);
	void setUrlPortada(String urlPortada);
	boolean esAgrupada();
	void setTotalButacas(int totalButacas);
	void setNombreEntidad(String nombreEntidad);
	void setDireccion(String direccion);
	void setLogo(final String archivo);
}
