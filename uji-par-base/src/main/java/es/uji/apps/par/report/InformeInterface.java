package es.uji.apps.par.report;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.exceptions.SinIvaException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.InformeSesion;

public interface InformeInterface
{
	InformeInterface create(Locale locale, Configuration configuration, String logoReport, boolean showIVA, String location);
	void serialize(OutputStream output) throws ReportSerializationException;
	void genera(String inicio, String fin, List<InformeModelReport> compras, BigDecimal totalTaquillaTPV,
            BigDecimal totalTaquillaEfectivo, BigDecimal totalTaquillaTransferencia, BigDecimal totalOnline);
	void genera(String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
    		String firmanteInformeEfectivo) throws SinIvaException;
	void genera(String titulo, String inicio, String fin, List<InformeModelReport> compras, List<InformeAbonoReport> abonos, String cargoInformeEfectivo,
			String firmanteInformeEfectivo) throws SinIvaException;
	void genera(String cargo, String firmante, List<InformeSesion> informesSesion, Cine cine, boolean printSesion) throws SinIvaException;
	void genera(String inicio, String fin, List<InformeModelReport> compras) throws SinIvaException;
    void genera(long sesionId, String userUID) throws SinIvaException;
	void genera(String fechaInicio, String fechaFin, String userUID) throws ParseException;
}
