package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;

import java.io.OutputStream;
import java.util.Locale;

public interface EntradaReportOnlineInterface {
	public EntradaReportOnlineInterface create(Locale locale, Configuration configuration);
	public void setTitulo(String titulo);
	public void setFecha(String fecha);
	public void setHora(String hora);
	public void setHoraApertura(String horaApertura);
	public void setCif(String cif);
	public void setPromotor(String promotor);
	public void setNifPromotor(String nifPromotor);
	public void generaPaginaButaca(EntradaModelReport entrada, String urlPublic);
	public void serialize(OutputStream output) throws ReportSerializationException;
	public void setUrlPublicidad(String urlPublicidad);
	public void setUrlPortada(String urlPortada);
	public boolean esAgrupada();
	public void setTotalButacas(int totalButacas);
}
