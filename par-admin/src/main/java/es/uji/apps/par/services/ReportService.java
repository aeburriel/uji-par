package es.uji.apps.par.services;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.mysema.query.Tuple;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.SinIvaException;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.database.DatabaseHelper;
import es.uji.apps.par.database.DatabaseHelperFactory;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Informe;
import es.uji.apps.par.model.InformeSesion;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.report.InformeInterface;
import es.uji.apps.par.report.InformeModelReport;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;

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

	private DatabaseHelper dbHelper;

	private InformeInterface informeTaquillaReport;
	private InformeInterface informeEfectivoReport;
	private InformeInterface informeTaquillaTpvSubtotalesReport;
	private InformeInterface informeEventosReport;
	private InformeInterface informeSesionReport;

	/*
	 * static { entradaTaquillaReport =
	 * EntradaReportFactory.newInstanceTaquilla(); entradaOnlineReport =
	 * EntradaReportFactory.newInstanceOnline(); }
	 */

	public ReportService() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		dbHelper = DatabaseHelperFactory.newInstance();
		informeTaquillaReport = EntradaReportFactory.newInstanceInformeTaquilla();
		informeEfectivoReport = EntradaReportFactory.newInstanceInformeEfectivo();
		informeTaquillaTpvSubtotalesReport = EntradaReportFactory.newInstanceInformeTaquillaTpvSubtotalesReport();
		informeEventosReport = EntradaReportFactory.newInstanceInformeEventosReport();
		informeSesionReport = EntradaReportFactory.newInstanceInformeSesionReport();
	}

	public ByteArrayOutputStream getExcelTaquilla(String fechaInicio,
			String fechaFin) throws IOException {
		List<Object[]> files = comprasDAO.getComprasInFechas(fechaInicio,
				fechaFin);
		ExcelService excelService = new ExcelService();
		int rownum = 0;

		if (files != null && files.size() > 0) {
			excelService.addFulla("Informe taquilla " + fechaInicio + " - "
					+ fechaFin);
			excelService.generaCeldes(excelService.getEstilNegreta(), 0,
					"Event", "Sessió", "Tipus d'entrada", "Localització",
					"Nombre d'entrades", "Total");

			for (Object[] fila : files) {
				rownum++;
				addDadesTaquilla(rownum, objectToInforme(fila), excelService);
			}
		}
		return excelService.getExcel();
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
		String tipoEntrada = Utils.safeObjectToString(fila[8]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));
		informe.setIva(dbHelper.castBigDecimal(fila[6]));

		return informe;
	}

	private InformeModelReport objectToInformeTpv(Object[] fila) {
		InformeModelReport informe = new InformeModelReport();
		informe.setEvento(Utils.safeObjectToString(fila[0]));
		informe.setSesion(DateUtils.dateToSpanishStringWithHour(
				Utils.objectToDate(fila[1])).toString());
		String tipoEntrada = Utils.safeObjectToString(fila[9]);
		informe.setTipoEntrada(tipoEntrada);
		informe.setNumeroEntradas(Utils.safeObjectBigDecimalToInt(dbHelper
				.castBigDecimal(fila[3])));
		informe.setTotal(dbHelper.castBigDecimal(fila[4]));
		informe.setIva(dbHelper.castBigDecimal(fila[6]));
		informe.setFechaCompra(DateUtils.dateToSpanishString(Utils
				.objectToDate(fila[8])));

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

	public ByteArrayOutputStream getExcelEventos(String fechaInicio,
			String fechaFin) throws IOException {
		List<Object[]> files = comprasDAO.getComprasPorEventoInFechas(
				fechaInicio, fechaFin);
		ExcelService excelService = new ExcelService();
		int rownum = 0;

		if (files != null && files.size() > 0) {
			excelService.addFulla("Informe taquilla " + fechaInicio + " - "
					+ fechaFin);
			excelService.generaCeldes(excelService.getEstilNegreta(), 0,
					"Event", "Tipus d'entrada", "Online o taquilla",
					"Nombre d'entrades", "Total");

			for (Object[] fila : files) {
				rownum++;
				addDadesEvento(rownum, objectToInformeEvento(fila),
						excelService);
			}
		}
		return excelService.getExcel();
	}

	public void getPdfTaquilla(String fechaInicio, String fechaFin, OutputStream bos) throws ReportSerializationException,
			ParseException {
		InformeInterface informe = informeTaquillaReport.create(new Locale("ca"));

		List<InformeModelReport> compras = objectsToInformes(comprasDAO.getComprasPorEventoInFechas(fechaInicio, fechaFin));

        Object[] taquillaTpv = comprasDAO.getTotalTaquillaTpv(fechaInicio, fechaFin);
		Object[] taquillaEfectivo = comprasDAO.getTotalTaquillaEfectivo(fechaInicio, fechaFin);
        Object[] online = comprasDAO.getTotalOnline(fechaInicio, fechaFin);

        BigDecimal totalTaquillaTpv = new BigDecimal(0);
        BigDecimal countTaquillaTpv = new BigDecimal(0);
        if (taquillaTpv != null) {
            if (taquillaTpv.length > 0 && taquillaTpv[0] != null) {
                totalTaquillaTpv = (BigDecimal) taquillaTpv[0];
            }
            if (taquillaTpv.length > 1 && taquillaTpv[1] != null) {
                countTaquillaTpv = (BigDecimal) taquillaTpv[1];
            }
        }

        BigDecimal totalTaquillaEfectivo = new BigDecimal(0);
        BigDecimal countTaquillaEfectivo = new BigDecimal(0);
        if (taquillaEfectivo != null) {
            if (taquillaEfectivo.length > 0 && taquillaEfectivo[0] != null) {
                totalTaquillaEfectivo = (BigDecimal) taquillaEfectivo[0];
            }
            if (taquillaEfectivo.length > 1 && taquillaEfectivo[1] != null) {
                countTaquillaEfectivo = (BigDecimal) taquillaEfectivo[1];
            }
        }

        BigDecimal totalOnline = new BigDecimal(0);
        BigDecimal countOnline = new BigDecimal(0);
        if (online != null) {
            if (online.length > 0 && online[0] != null) {
                totalOnline = (BigDecimal) online[0];
            }
            if (online.length > 1 && online[1] != null) {
                countOnline = (BigDecimal) online[1];
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

		informe.serialize(bos);
	}

	private String getSpanishStringDateFromBBDDString(String fecha)	throws ParseException {
		Date dt = DateUtils.databaseStringToDate(fecha);
		return DateUtils.dateToSpanishString(dt);
	}

	public void getPdfEfectivo(String fechaInicio, String fechaFin,	OutputStream bos) throws ReportSerializationException,
			ParseException, SinIvaException {
		InformeInterface informe = informeEfectivoReport.create(new Locale("ca"));
		List<InformeModelReport> compras = objectsSesionesToInformesIva(comprasDAO.getComprasEfectivo(fechaInicio, fechaFin));

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio),
				getSpanishStringDateFromBBDDString(fechaFin), compras,
				Configuration.getCargoInformeEfectivo(),
				Configuration.getFirmanteInformeEfectivo());

		informe.serialize(bos);
	}

	public void getPdfTpvSubtotales(String fechaInicio, String fechaFin,
			OutputStream bos) throws ReportSerializationException,
			ParseException, SinIvaException {
		InformeInterface informe = informeTaquillaTpvSubtotalesReport
				.create(new Locale("ca"));

		List<InformeModelReport> compras = objectsSesionesToInformesTpv(comprasDAO
				.getComprasTpv(fechaInicio, fechaFin));

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio),
				getSpanishStringDateFromBBDDString(fechaFin), compras,
				Configuration.getCargoInformeEfectivo(),
				Configuration.getFirmanteInformeEfectivo());

		informe.serialize(bos);
	}

	public void getPdfEventos(String fechaInicio, String fechaFin,
			OutputStream bos) throws ReportSerializationException,
			ParseException, SinIvaException {
		InformeInterface informe = informeEventosReport
				.create(new Locale("ca"));

		List<InformeModelReport> compras = objectsSesionesToInformesEventos(comprasDAO
				.getComprasEventos(fechaInicio, fechaFin));

		informe.genera(getSpanishStringDateFromBBDDString(fechaInicio),
				getSpanishStringDateFromBBDDString(fechaFin), compras);

		informe.serialize(bos);
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
	}
	
	public void getPdfSesion(long sesionId, ByteArrayOutputStream bos) throws SinIvaException, ReportSerializationException, IOException {
		InformeInterface informe = informeSesionReport.create(new Locale("ca"));
		Cine cine = Cine.cineDTOToCine(cinesDAO.getCines().get(0));
		List<InformeSesion> informesSesion = new ArrayList<InformeSesion>();
		informesSesion.add(getInformeSesion(sesionId));
		informe.genera(Configuration.getCargoInformeEfectivo(), Configuration.getFirmanteInformeEfectivo(), informesSesion, cine, true);

		informe.serialize(bos);
	}
	
	public void getPdfSesiones(List<Sesion> sesiones, ByteArrayOutputStream bos) throws SinIvaException, ReportSerializationException, IOException {
		InformeInterface informe = informeSesionReport.create(new Locale("ca"));
		List<InformeSesion> informesSesion = new ArrayList<InformeSesion>();
		Cine cine = Cine.cineDTOToCine(cinesDAO.getCines().get(0));
		for (Sesion sesion: sesiones)
		{
			long sesionId = sesion.getId();
			informesSesion.add(getInformeSesion(sesionId));
		}
		
		informe.genera(Configuration.getCargoInformeEfectivo(), Configuration.getFirmanteInformeEfectivo(), informesSesion, cine, false);

		informe.serialize(bos);
	}

	private InformeSesion getInformeSesion(long sesionId) {
		SesionDTO sesionDTO = sesionesDAO.getSesion(sesionId);
		Sesion sesion = Sesion.SesionDTOToSesion(sesionDTO);
		Sala sala = Sala.salaDTOtoSala(sesionDTO.getParSala());
		Evento evento = Evento.eventoDTOtoEvento(sesionDTO.getParEvento());
		InformeModelReport resumen = comprasDAO.getResumenSesion(sesionId);
		List<Tuple> butacasYTarifas = butacasDAO.getButacas(sesionId);
		
		List<InformeModelReport> compras = new ArrayList<InformeModelReport>();
		for (Tuple butacaYTarifa: butacasYTarifas) {
			ButacaDTO butacaDTO = butacaYTarifa.get(0, ButacaDTO.class);
			String nombreTarifa = butacaYTarifa.get(1, String.class);
			InformeModelReport informeModel = InformeModelReport.fromButaca(butacaDTO, Configuration.getHorasVentaAnticipada());
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
	
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/applicationContext-db.xml");

		ReportService service = ctx.getBean(ReportService.class);

		service.getPdfEventos("2013-10-01", "2013-10-30", new FileOutputStream(
				"/tmp/informe.pdf"));
	}
}