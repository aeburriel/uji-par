package es.uji.apps.par.services;

import com.mysema.query.Tuple;
import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.exceptions.SinIvaException;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.*;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.report.InformeAbonoReport;
import es.uji.apps.par.report.InformeInterface;
import es.uji.apps.par.report.InformeModelReport;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
public class ReportService {

	@Autowired
	ComprasDAO comprasDAO;

	@Autowired
	ButacasDAO butacasDAO;

	@Autowired
	SesionesDAO sesionesDAO;

	@Autowired
	CinesDAO cinesDAO;

	@Autowired
	UsuariosDAO usuariosDAO;

	Configuration configuration;

	private DatabaseHelper dbHelper;

    private InformeInterface informeReport;

	@Autowired
	public ReportService(Configuration configuration) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		this.configuration = configuration;
		dbHelper = DatabaseHelperFactory.newInstance(configuration);
	}

	public ByteArrayOutputStream getExcelTaquilla(String fechaInicio, String fechaFin, Locale locale, String userUID) throws
			IOException {
		ExcelService excelService = getExcelServiceTaquilla(fechaInicio, fechaFin, locale, userUID);
		return excelService.getExcel();
	}

	public ExcelService getExcelServiceTaquilla(String fechaInicio, String fechaFin, Locale locale, String userUID) {
		List<Object[]> files = comprasDAO.getComprasInFechas(fechaInicio, fechaFin, userUID);
		ExcelService excelService = new ExcelService();
		int rownum = 0;

		if (files != null && files.size() > 0) {
			excelService.addFulla(ResourceProperties.getProperty(locale, "excelTaquilla.titulo") + " " + fechaInicio + " - "
					+ fechaFin);

			excelService.generaCeldes(excelService.getEstilNegreta(), 0,
                    ResourceProperties.getProperty(locale, "excelTaquilla.evento"), ResourceProperties.getProperty(locale,
							"excelTaquilla.sesion"), ResourceProperties.getProperty(locale, "excelTaquilla.tipoEntrada"),
					ResourceProperties.getProperty(locale, "excelTaquilla.localizacion"),
                    ResourceProperties.getProperty(locale, "excelTaquilla.numeroEntradas"), ResourceProperties.getProperty
							(locale, "excelTaquilla.total"));

			for (Object[] fila : files) {
				rownum++;
				addDadesTaquilla(rownum, objectToInforme(fila), excelService);
			}
		}
		return excelService;
	}

	private Informe objectToInforme(Object[] fila) {
		Informe informe = new Informe();
		informe.setEvento(Utils.safeObjectToString(fila[0]));
		informe.setSesion(DateUtils.dateToSpanishStringWithHour(
				Utils.objectToDate(fila[1])).toString());
		String tipoEntrada = Utils.safeObjectToString(fila[8]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setLocalizacion(Utils.safeObjectToString(fila[7]));
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[4])));
		informe.setTotal(dbHelper.castBigDecimal(fila[5]));

		return informe;
	}

	private InformeModelReport objectToInformeIva(Object[] fila) {
		InformeModelReport informe = new InformeModelReport();
		informe.setEvento(Utils.safeObjectToString(fila[0]));
		informe.setSesion(DateUtils.dateToSpanishStringWithHour(
				Utils.objectToDate(fila[1])).toString());
		String tipoEntrada = Utils.safeObjectToString(fila[7]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));
		informe.setIva(dbHelper.castBigDecimal(fila[6]));

		return informe;
	}

	private InformeAbonoReport objectToInformeAbonoIva(Object[] fila) {
		InformeAbonoReport informeAbonoReport = new InformeAbonoReport();
		informeAbonoReport.setNombre(Utils.safeObjectToString(fila[0]));
		informeAbonoReport.setAbonados(Utils.safeObjectBigDecimalToInt(dbHelper.castBigDecimal(fila[1])));
		informeAbonoReport.setTotal(dbHelper.castBigDecimal(fila[2]));

		return informeAbonoReport;
	}

	private InformeModelReport objectToInformeTpv(Object[] fila) {
		InformeModelReport informe = new InformeModelReport();
		informe.setEvento(Utils.safeObjectToString(fila[0]));
		informe.setSesion(DateUtils.dateToSpanishStringWithHour(
				Utils.objectToDate(fila[1])).toString());
		String tipoEntrada = Utils.safeObjectToString(fila[8]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));
		informe.setIva(dbHelper.castBigDecimal(fila[6]));
		informe.setFechaCompra(DateUtils.dateToSpanishString(Utils
				.objectToDate(fila[7])));

		return informe;
	}

	private InformeModelReport objectToEvento(Object[] fila) {
		InformeModelReport informe = new InformeModelReport();
		informe.setEvento(Utils.safeObjectToString(fila[0]));
		informe.setSesion(DateUtils.dateToSpanishStringWithHour(
				Utils.objectToDate(fila[1])).toString());
		String tipoEntrada = Utils.safeObjectToString(fila[9]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));
		informe.setIva(dbHelper.castBigDecimal(fila[5]));
		informe.setEventoId(Utils.safeObjectBigDecimalToLong(dbHelper
				.castBigDecimal(fila[7])));
		informe.setSesionId(Utils.safeObjectBigDecimalToLong(dbHelper
				.castBigDecimal(fila[8])));

		return informe;
	}

	private InformeModelReport objectToInformeEvento(Object[] fila) {
		InformeModelReport informe = new InformeModelReport();
		informe.setEvento(Utils.safeObjectToString(fila[1]));
		String tipoEntrada = Utils.safeObjectToString(fila[6]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));

		int taquilla = Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[5]));
		informe.setTipoCompra((taquilla == 0) ? "ONLINE" : "TAQUILLA");

		return informe;
	}

	private void addDadesTaquilla(int i, Informe fila, ExcelService excelService) {
		Row row = excelService.getNewRow(i);
		excelService.addCell(0, fila.getEvento(), null, row);
		excelService.addCell(1, fila.getSesion(), null, row);
		excelService.addCell(2, fila.getTipoEntrada(), null, row);
		excelService.addCell(3, fila.getLocalizacion(), null, row);
		excelService.addCell(4, fila.getNumeroEntradas(), null, row);
		excelService.addCell(5, fila.getTotal().floatValue(), row);
	}

	private void addDadesEvento(int i, InformeModelReport fila,
			ExcelService excelService) {
		Row row = excelService.getNewRow(i);
		excelService.addCell(0, fila.getEvento(), null, row);
		excelService.addCell(1, fila.getTipoEntrada(), null, row);
		excelService.addCell(2, fila.getTipoCompra(), null, row);
		excelService.addCell(3, fila.getNumeroEntradas(), null, row);
		excelService.addCell(4, fila.getTotal().floatValue(), row);
	}

	public ByteArrayOutputStream getExcelEventos(String fechaInicio, String fechaFin, Locale locale, String userUID) throws
			IOException {
		ExcelService excelService = getExcelServiceEventos(fechaInicio, fechaFin, locale, userUID);
		return excelService.getExcel();
	}

	public ExcelService getExcelServiceEventos(String fechaInicio, String fechaFin, Locale locale, String userUID) {
		List<Object[]> files = comprasDAO.getComprasPorEventoInFechas(fechaInicio, fechaFin, userUID);
		ExcelService excelService = new ExcelService();
		int rownum = 0;

		if (files != null && files.size() > 0) {
			excelService.addFulla(ResourceProperties.getProperty(locale, "excelEventos.titulo") + " " + fechaInicio + " - "
					+ fechaFin);
			excelService.generaCeldes(excelService.getEstilNegreta(), 0,
                    ResourceProperties.getProperty(locale, "excelEventos.evento"), ResourceProperties.getProperty(locale,
							"excelEventos.tipoEntrada"), ResourceProperties.getProperty(locale, "excelEventos.tipo"),
                    ResourceProperties.getProperty(locale, "excelEventos.numeroEntradas"), ResourceProperties.getProperty(locale,
							"excelEventos.total"));

			for (Object[] fila : files) {
				rownum++;
				addDadesEvento(rownum, objectToInformeEvento(fila),
						excelService);
			}
		}
		return excelService;
	}

	public void getPdfTaquilla(String fechaInicio, String fechaFin, OutputStream bos, Locale locale, String userUID, String logoReport) throws
			ReportSerializationException, ParseException {
		InformeInterface informe = generaYRellenaInformePDFTaquilla(fechaInicio, fechaFin, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public InformeInterface generaYRellenaInformePDFTaquilla(String fechaInicio, String fechaFin, Locale locale, String userUID, String logoReport)
			throws ParseException {
		String className = usuariosDAO.getReportClassNameForUserAndType(userUID, EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA);
		InformeInterface informeTaquillaReport = EntradaReportFactory.newInstanceInformeTaquilla(className);
		InformeInterface informe = informeTaquillaReport.create(locale, configuration, logoReport);

		List<InformeModelReport> compras = objectsToInformes(comprasDAO.getComprasPorEventoInFechas(fechaInicio, fechaFin, userUID));

		Object[] taquillaTpv = comprasDAO.getTotalTaquillaTpv(fechaInicio, fechaFin, userUID);
		Object[] taquillaEfectivo = comprasDAO.getTotalTaquillaEfectivo(fechaInicio, fechaFin, userUID);
		Object[] online = comprasDAO.getTotalOnline(fechaInicio, fechaFin, userUID);

		BigDecimal totalTaquillaTpv = new BigDecimal(0);
		BigDecimal countTaquillaTpv = new BigDecimal(0);
		if (taquillaTpv != null) {
			if (taquillaTpv.length > 0 && taquillaTpv[0] != null) {
				totalTaquillaTpv = dbHelper.castBigDecimal(taquillaTpv[0]);
			}
			if (taquillaTpv.length > 1 && taquillaTpv[1] != null) {
				countTaquillaTpv = dbHelper.castBigDecimal(taquillaTpv[1]);
			}
		}

		BigDecimal totalTaquillaEfectivo = new BigDecimal(0);
		BigDecimal countTaquillaEfectivo = new BigDecimal(0);
		if (taquillaEfectivo != null) {
			if (taquillaEfectivo.length > 0 && taquillaEfectivo[0] != null) {
				totalTaquillaEfectivo = dbHelper.castBigDecimal(taquillaEfectivo[0]);
			}
			if (taquillaEfectivo.length > 1 && taquillaEfectivo[1] != null) {
				countTaquillaEfectivo = dbHelper.castBigDecimal(taquillaEfectivo[1]);
			}
		}

		BigDecimal totalOnline = new BigDecimal(0);
		BigDecimal countOnline = new BigDecimal(0);
		if (online != null) {
			if (online.length > 0 && online[0] != null) {
				totalOnline = dbHelper.castBigDecimal(online[0]);
			}
			if (online.length > 1 && online[1] != null) {
				countOnline = dbHelper.castBigDecimal(online[1]);
			}
		}

		// TODO: Esto hay que pasarlo como parámetro y no meterlo duplicado en todas las compras
		for (InformeModelReport compra: compras) {
			compra.setNumeroEntradasTPV(countTaquillaTpv);
			compra.setNumeroEntradasEfectivo(countTaquillaEfectivo);
			compra.setNumeroEntradasOnline(countOnline);
		}

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio), getSpanishStringDateFromBBDDString(fechaFin), compras,
				totalTaquillaTpv, totalTaquillaEfectivo, totalOnline);
		return informe;
	}

	private String getSpanishStringDateFromBBDDString(String fecha)	throws ParseException {
		Date dt = DateUtils.databaseStringToDate(fecha);
		return DateUtils.dateToSpanishString(dt);
	}

	public void getPdfEfectivo(String fechaInicio, String fechaFin,	OutputStream bos, Locale locale, String userUID, String logoReport) throws
			ReportSerializationException, ParseException, SinIvaException {
		InformeInterface informe = generaYRellenaInformePDFEfectivo(fechaInicio, fechaFin, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public InformeInterface generaYRellenaInformePDFEfectivo(String fechaInicio, String fechaFin, Locale locale, String userUID, String logoReport)
			throws ParseException {
		String className = usuariosDAO.getReportClassNameForUserAndType(userUID, EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO);
		InformeInterface informeEfectivoReport = EntradaReportFactory.newInstanceInformeEfectivo(className);
		InformeInterface informe = informeEfectivoReport.create(locale, configuration, logoReport);
		List<InformeModelReport> compras = objectsSesionesToInformesIva(comprasDAO.getComprasEfectivo(fechaInicio, fechaFin,
				userUID));
		List<InformeAbonoReport> abonos = new ArrayList<InformeAbonoReport>();

		if (configuration.isMenuAbono()) {
			abonos = objectsAbonosToInformesIva(comprasDAO.getAbonosEfectivo(fechaInicio, fechaFin, userUID));
		}

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio), getSpanishStringDateFromBBDDString(fechaFin), compras,
				abonos, configuration.getCargoInformeEfectivo(), configuration.getFirmanteInformeEfectivo());
		return informe;
	}

	public void getPdfTpvSubtotales(String fechaInicio, String fechaFin, OutputStream bos, Locale locale, String userUID, String logoReport) throws
			ReportSerializationException, ParseException, SinIvaException {
		InformeInterface informe = generaYRellenaInformePDFTaquillaTPVSubtotales(fechaInicio, fechaFin, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public void getPdfTpvOnlineSubtotales(String fechaInicio, String fechaFin, OutputStream bos, Locale locale, String userUID, String logoReport) throws
			ReportSerializationException, ParseException, SinIvaException {
		InformeInterface informe = generaYRellenaInformePDFOnlineTPVSubtotales(fechaInicio, fechaFin, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public InformeInterface generaYRellenaInformePDFTaquillaTPVSubtotales(String fechaInicio, String fechaFin, Locale locale,
			String userUID, String logoReport) throws ParseException {
		return generaYRellenaInformePDFTypeTPVSubtotales(fechaInicio, fechaFin, locale, userUID, false, logoReport);
	}

	public InformeInterface generaYRellenaInformePDFOnlineTPVSubtotales(String fechaInicio, String fechaFin, Locale locale,
			String userUID, String logoReport) throws ParseException {
		return generaYRellenaInformePDFTypeTPVSubtotales(fechaInicio, fechaFin, locale, userUID, true, logoReport);
	}

	private InformeInterface generaYRellenaInformePDFTypeTPVSubtotales(String fechaInicio, String fechaFin, Locale locale,
			String userUID, boolean onlyOnline, String logoReport) throws ParseException {
		String className = usuariosDAO.getReportClassNameForUserAndType(userUID, EntradaReportFactory
				.TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES);
		InformeInterface informeTaquillaTpvSubtotalesReport = EntradaReportFactory.newInstanceInformeTaquillaTpvSubtotalesReport
				(className);
		InformeInterface informe = informeTaquillaTpvSubtotalesReport.create(locale, configuration, logoReport);
		List<Object[]> comprasTpv = onlyOnline ? comprasDAO.getComprasTpvOnline(fechaInicio, fechaFin, userUID) : comprasDAO.getComprasTpv(fechaInicio, fechaFin, userUID);
		List<InformeModelReport> compras = objectsSesionesToInformesTpv(comprasTpv);

		String title = onlyOnline ? ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.tituloCabeceraOnline") : ResourceProperties.getProperty(locale, "informeTaquillaTpvSubtotales.tituloCabecera");
		informe.genera(title, getSpanishStringDateFromBBDDString(fechaInicio), getSpanishStringDateFromBBDDString(fechaFin),
				compras, null, configuration.getCargoInformeEfectivo(), configuration.getFirmanteInformeEfectivo());
		return informe;
	}

	public void getPdfEventos(String fechaInicio, String fechaFin, OutputStream bos, Locale locale, String userUID, String logoReport) throws
			ReportSerializationException, ParseException, SinIvaException {
		InformeInterface informe = generaYRellenaInformePDFEventos(fechaInicio, fechaFin, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public InformeInterface generaYRellenaInformePDFEventos(String fechaInicio, String fechaFin, Locale locale, String userUID, String logoReport)
			throws ParseException {
		String className = usuariosDAO.getReportClassNameForUserAndType(userUID, EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS);
		InformeInterface informeEventosReport = EntradaReportFactory.newInstanceInformeEventosReport(className);
		InformeInterface informe = informeEventosReport.create(locale, configuration, logoReport);

		List<InformeModelReport> compras = objectsSesionesToInformesEventos(comprasDAO.getComprasEventos(fechaInicio, fechaFin,
				userUID));

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio),
				getSpanishStringDateFromBBDDString(fechaFin), compras);
		return informe;
	}

	private List<InformeModelReport> objectsToInformes(List<Object[]> compras) {
		List<InformeModelReport> result = new ArrayList<InformeModelReport>();

		for (Object[] compra : compras) {
			result.add(objectToInformeEvento(compra));
		}

		return result;
	}

	private List<InformeModelReport> objectsSesionesToInformesIva(
			List<Object[]> compras) {
		List<InformeModelReport> result = new ArrayList<InformeModelReport>();

		for (Object[] compra : compras) {
			result.add(objectToInformeIva(compra));
		}

		return result;
	}

	private List<InformeAbonoReport> objectsAbonosToInformesIva(List<Object[]> abonos) {
		List<InformeAbonoReport> result = new ArrayList<InformeAbonoReport>();

		for (Object[] abono : abonos) {
			result.add(objectToInformeAbonoIva(abono));
		}

		return result;
	}

	private List<InformeModelReport> objectsSesionesToInformesTpv(
			List<Object[]> compras) {
		List<InformeModelReport> result = new ArrayList<InformeModelReport>();

		for (Object[] compra : compras) {
			result.add(objectToInformeTpv(compra));
		}

		return result;
	}

	private List<InformeModelReport> objectsSesionesToInformesEventos(
			List<Object[]> compras) {
		List<InformeModelReport> result = new ArrayList<InformeModelReport>();

		for (Object[] compra : compras) {
			result.add(objectToEvento(compra));
		}

		return result;
		//informeSesionReport = EntradaReportFactory.newInstanceInformeSesionReport(configuration);
	}

	public void getPdfSesion(long sesionId, ByteArrayOutputStream bos, Locale locale, String userUID, String logoReport) throws SinIvaException,
			ReportSerializationException, IOException {
		Sesion sesion = new Sesion(new Long(sesionId).intValue());
		InformeInterface informe = generaYRellenaPDFSesiones(Arrays.asList(sesion), locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public void getPdfSesiones(List<Sesion> sesiones, ByteArrayOutputStream bos, Locale locale, String userUID, String logoReport) throws
			SinIvaException, ReportSerializationException, IOException {
		InformeInterface informe = generaYRellenaPDFSesiones(sesiones, locale, userUID, logoReport);
		informe.serialize(bos);
	}

	public InformeInterface generaYRellenaPDFSesiones(List<Sesion> sesiones, Locale locale, String userUID, String logoReport) {
		String className = usuariosDAO.getReportClassNameForUserAndType(userUID, EntradaReportFactory.TIPO_INFORME_PDF_SESIONES);
		InformeInterface informeSesionReport = EntradaReportFactory.newInstanceInformeSesionReport(className);
		InformeInterface informe = informeSesionReport.create(locale, configuration, logoReport);
		List<InformeSesion> informesSesion = new ArrayList<InformeSesion>();
		Cine cine = Cine.cineDTOToCine(cinesDAO.getCines(userUID).get(0));

		for (Sesion sesion: sesiones)
		{
			long sesionId = sesion.getId();
			informesSesion.add(getInformeSesion(sesionId, userUID));
		}

		boolean printSesion = (sesiones.size() == 1) ? true : false;
		informe.genera(configuration.getCargoInformeEfectivo(), configuration.getFirmanteInformeEfectivo(), informesSesion, cine,
				printSesion);
		return informe;
	}

	public void getPdf(long sesionId, ByteArrayOutputStream bos, String tipo, Locale locale, String userUID, String logoReport) throws SinIvaException,
			ReportSerializationException, IOException {
        informeReport = EntradaReportFactory.newInstanceInformeReport(tipo, configuration);
        InformeInterface informe = informeReport.create(locale, configuration, logoReport);
        informe.genera(sesionId, userUID);

        informe.serialize(bos);
    }

	private InformeSesion getInformeSesion(long sesionId, String userUID) {
		SesionDTO sesionDTO = sesionesDAO.getSesion(sesionId, userUID);
		Sesion sesion = Sesion.SesionDTOToSesion(sesionDTO);
		Sala sala = Sala.salaDTOtoSala(sesionDTO.getParSala());
		Evento evento = Evento.eventoDTOtoEvento(sesionDTO.getParEvento());
		InformeModelReport resumen = comprasDAO.getResumenSesion(sesionId);
		List<Tuple> butacasYTarifas = butacasDAO.getButacas(sesionId);

		List<InformeModelReport> compras = new ArrayList<InformeModelReport>();
		for (Tuple butacaYTarifa: butacasYTarifas) {
			ButacaDTO butacaDTO = butacaYTarifa.get(0, ButacaDTO.class);
			String nombreTarifa = butacaYTarifa.get(1, String.class);
			InformeModelReport informeModel = InformeModelReport.fromButaca(butacaDTO, configuration.getHorasVentaAnticipada(),
					configuration.getLocalizacionEnValenciano());
			informeModel.setTipoEntrada(nombreTarifa);
			compras.add(informeModel);
		}

		InformeSesion informeSesion = new InformeSesion();
		informeSesion.setSala(sala);
		informeSesion.setSesion(sesion);
		informeSesion.setEvento(evento);
		informeSesion.setVendidas(resumen.getNumeroEntradas());
		informeSesion.setAnuladas(resumen.getCanceladasTaquilla());
		informeSesion.setTotal(resumen.getTotal());
		informeSesion.setCompras(compras);

		return informeSesion;
	}

	public void getPdfPorFechas(String fechaInicio, String fechaFin, String tipo, ByteArrayOutputStream ostream, Locale locale,
			String userUID, String logoReport) throws ReportSerializationException, ParseException {
		informeReport = EntradaReportFactory.newInstanceInformeReport(tipo, configuration);
		InformeInterface informe = informeReport.create(locale, configuration, logoReport);
		informe.genera(fechaInicio, fechaFin, userUID);
		informe.serialize(ostream);
	}
}
