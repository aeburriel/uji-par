package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.exceptions.SinIvaException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.InformeSesion;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public interface InformeInterface {
	public InformeInterface create(Locale locale, Configuration configuration);
	public void serialize(OutputStream output) throws ReportSerializationException;
	public void genera(String inicio, String fin, List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
            BigDecimal totalTaquillaEfectivo, BigDecimal totalOnline);
	public void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
    		String firmanteInformeEfectivo) throws SinIvaException;
	public void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException;
	public void genera(String inicio, String fin, List<InformeModelReport> compras) throws SinIvaException;
    public void genera(long sesionId) throws SinIvaException;
	public void genera(String fechaInicio, String fechaFin, String userUID) throws ParseException;
}
