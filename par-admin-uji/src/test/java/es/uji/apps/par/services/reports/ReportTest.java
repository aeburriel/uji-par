package es.uji.apps.par.services.reports;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.LocalizacionesDAO;
import es.uji.apps.par.dao.PlantillasDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.ReportDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.enums.TipoPago;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Localizacion;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.PreciosSesion;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.Tarifa;
import es.uji.apps.par.model.Tpv;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.report.InformeInterface;
import es.uji.apps.par.services.ExcelService;
import es.uji.apps.par.services.ReportService;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class ReportTest {

	@Autowired
	ReportService reportService;

	@Autowired
	CinesDAO cinesDAO;

	@Autowired
	SalasDAO salasDAO;

	@Autowired
	UsuariosDAO usuariosDAO;

	@Autowired
	SesionesDAO sesionesDAO;

	@Autowired
	EventosDAO eventosDAO;

	@Autowired
	TpvsDAO tpvsDAO;

	@Autowired
	PlantillasDAO plantillasDAO;

	@Autowired
	TarifasDAO tarifasDAO;

	@Autowired
	LocalizacionesDAO localizacionesDAO;

	@Autowired
	ComprasDAO comprasDAO;

	@Autowired
	ButacasDAO butacasDAO;

	private Cine cine;
	private Evento evento;
	private Tpv tpv;
	private Tarifa tarifa;
	private Localizacion localizacion;

	@Transactional
	private void altaCine() {
		cine = new Cine();
		cine.setNombre("Cine Prueba");
		cinesDAO.addCine(cine);

		altaTpv();
	}

	@Transactional
	private Sala altaSala(String nombre) {
		Sala sala = new Sala();
		sala.setNombre(nombre);
		sala.setCine(cine);
		salasDAO.addSala(sala);
		return sala;
	}

	@Transactional
	private Usuario altaUsuario(String nombre, String mail, String login) {
		Usuario usuario = new Usuario();
		usuario.setNombre(nombre);
		usuario.setMail(mail);
		usuario.setUsuario(login);
		return usuariosDAO.addUser(usuario);
	}

	@Transactional
	private void altaTpv() {
		tpv = new Tpv();
		tpv.setNombre("TPV Prueba");
		TpvsDTO tpvDefecto = tpvsDAO.getTpvDefault(cine.getId());
		if (tpvDefecto == null)
			tpvsDAO.addTpv(tpv, cine.getId());

		TpvsDTO tpvDefectoInsertado = tpvsDAO.getTpvDefault(cine.getId());
		tpv.setId(tpvDefectoInsertado.getId());
	}

	@Transactional
	private void altaEvento() {
		evento = new Evento();
		evento.setTituloEs("Nombre Evento Castellano");
		evento.setTituloVa("Nombre Evento Valenciano");
		evento.setParTpv(tpv);

		evento = eventosDAO.addEvento(evento);
	}

	@Transactional
	private Plantilla altaPlantilla(Sala sala) {
		Plantilla plantilla = new Plantilla();
		plantilla.setSala(sala);
		plantilla.setNombre("Sin plantilla");
		Plantilla plantillaInsertada = plantillasDAO.add(plantilla);
		plantilla.setId(plantillaInsertada.getId());
		return plantilla;
	}

	@Transactional
	private void altaTarifa() {
		tarifa = new Tarifa();
		tarifa.setNombre("Tarifa");
		tarifa.setDefecto("on");
		tarifa.setIsPublico("on");
		TarifaDTO tarifaInsertada = tarifasDAO.add(Tarifa.toDTO(tarifa));
		tarifa.setId(tarifaInsertada.getId());
	}

	@Transactional
	private void altaLocalizacion() {
		localizacion = new Localizacion();
		localizacion.setNombreEs("Localizacion");
		localizacion.setNombreVa("Localitzacio");
		localizacion.setCodigo("codi");
		localizacion.setTotalEntradas(100);
		localizacionesDAO.add(localizacion);
	}

	@Transactional
	private Sesion altaSesion(Sala sala, Plantilla plantilla, String hora, Usuario usuario) {
		Sesion sesion = new Sesion();
		sesion.setEvento(evento);
		sesion.setFechaCelebracionWithDate(Calendar.getInstance().getTime());
		sesion.setHoraCelebracion(hora);
		sesion.setSala(sala);
		sesion.setPlantillaPrecios(plantilla);
		List<PreciosSesion> preciosSesion = new ArrayList<PreciosSesion>();
		PreciosSesion precioSesion = new PreciosSesion();
		precioSesion.setSesion(sesion);
		precioSesion.setPrecio(new BigDecimal(1));
		precioSesion.setLocalizacion(localizacion);
		precioSesion.setTarifa(tarifa);
		preciosSesion.add(precioSesion);
		sesion.setPreciosSesion(preciosSesion);
		Sesion sesionInsertada = sesionesDAO.addSesion(sesion, usuario.getUsuario());
		sesion.setId(sesionInsertada.getId());
		return sesion;
	}

	@Transactional
	private void altaRelacionSalaUsuario(Sala sala, Usuario usuario) {
		usuariosDAO.addSalaUsuario(sala, usuario);
	}

	@Transactional
	private void altaReports(String reportClass, String tipo, Sala sala) {
		ReportDTO reportDTO = new ReportDTO(sala.getId(), tipo, reportClass);
		salasDAO.addReport(reportDTO);
	}

	@Transactional
	private Compra altaCompra(Sesion sesion, String fila, String asiento, boolean taquilla, Usuario usuario) {
		BigDecimal importe = new BigDecimal(1);
		CompraDTO compraDTO = comprasDAO.insertaCompra(sesion.getId(), Calendar.getInstance().getTime(), taquilla, importe, usuario.getUsuario());
		compraDTO.setPagada(true);
		compraDTO.setTipoPago(TipoPago.METALICO.toString());
		Butaca butaca = new Butaca();
		butaca.setFila(fila);
		butaca.setNumero(asiento);
		butaca.setLocalizacion(localizacion.getCodigo());
		butaca.setTipo(String.valueOf(tarifa.getId()));
		butacasDAO.reservaButacas(sesion.getId(), compraDTO, Arrays.asList(butaca), usuario.getUsuario());
		Compra compra = Compra.compraDTOtoCompra(compraDTO);
		return compra;
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFTaquilla() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaReport", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA, sala);
		InformeInterface informe = reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario(), "logo", true, "ciudad");
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeTaquillaReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA,
				sala);
		reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario(), "logo", true, "ciudad");
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaReport", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA, sala);
		reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				"NOTEXISTS", "logo", true, "ciudad");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFEfectivo() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEfectivoReport", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO, sala);
		InformeInterface informe = reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario(), "logo", true, "ciudad");
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeEfectivoReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEfectivoCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEfectivoReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO,
				sala);
		reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario(), "logo", true, "ciudad");
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEfectivoWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEfectivoReport", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO, sala);
		reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				"NOTEXISTS", "logo", true, "ciudad");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotales() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", EntradaReportFactory
				.TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES, sala);
		InformeInterface informe = reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new
				Locale("es"), usuario.getUsuario(), "logo", true, "ciudad");
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotalesCuandoReportClassNoExiste() throws ReportSerializationException,
			ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReportPersonalizado", EntradaReportFactory
				 .TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES, sala);
		reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new Locale("es"), usuario
				 .getUsuario(), "logo", true, "ciudad");
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotalesWhenUserNotExists() throws ReportSerializationException,
			ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", EntradaReportFactory
				 .TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES, sala);
		reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new Locale("es"), "NOTEXISTS", "logo", true, "ciudad");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFEventos() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEventosReport", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS, sala);
		InformeInterface informe = reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario(), "logo", true, "ciudad");
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeEventosReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEventosCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEventosReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS,
				sala);
		reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"), usuario.getUsuario(), "logo", true, "ciudad");
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEventosWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		altaReports("es.uji.apps.par.report.InformeEventosReport", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS, sala);
		reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"), "NOTEXISTS", "logo", true, "ciudad");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFSesiones() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		Plantilla plantilla = altaPlantilla(sala);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		altaReports("es.uji.apps.par.report.InformeSesionReport", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES, sala);
		InformeInterface informe = reportService.generaYRellenaPDFSesiones(Arrays.asList(sesion), new Locale("es"), usuario
				.getUsuario
				(), "logo", true, "ciudad", true);
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeSesionReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFSesionesCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		Sesion s = new Sesion(1);
		altaReports("es.uji.apps.par.report.InformeSesionReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES,
				sala);
		reportService.generaYRellenaPDFSesiones(Arrays.asList(s), new Locale("es"), usuario.getUsuario(), "logo", true, "ciudad", true);
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFSesionesWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		altaRelacionSalaUsuario(sala, usuario);
		Sesion s = new Sesion(1);
		altaReports("es.uji.apps.par.report.InformeSesionReport", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES, sala);
		reportService.generaYRellenaPDFSesiones(Arrays.asList(s), new Locale("es"), "NOTEXISTS", "logo", true, "ciudad", true);
	}

	@Test
	@Transactional
	public void testInformeExcelTaquillaSoloMuestraDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		altaCompra(sesion, "1", "2", true, usuario);
		altaCompra(sesion, "1", "3", false, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		ExcelService excelServiceConDosComprasTaquilla = reportService.getExcelServiceVentas(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
				new Locale("es"), usuario.getUsuario(), false);
		Assert.assertEquals(2, excelServiceConDosComprasTaquilla.getTotalFilas());
		Object fechaSesionConDosComprasTaquilla = excelServiceConDosComprasTaquilla.getCellValue("B2");
		Assert.assertEquals("12:00", fechaSesionConDosComprasTaquilla.toString().split(" ")[1]);
		Object cantidadDeComprasDeSesionConDosCompras = excelServiceConDosComprasTaquilla.getCellValue("E2");
		Assert.assertEquals(2.0, cantidadDeComprasDeSesionConDosCompras);
		ExcelService excelServiceConUnaCompraTaquilla = reportService.getExcelServiceVentas(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
				new Locale("es"), usuario2.getUsuario(), false);
		Assert.assertEquals(2, excelServiceConUnaCompraTaquilla.getTotalFilas());
		Object fechaSesionConUnaCompra = excelServiceConUnaCompraTaquilla.getCellValue("B2");
		Assert.assertEquals("13:00", fechaSesionConUnaCompra.toString().split(" ")[1]);
		Object cantidadDeComprasDeSesionConUnaCompraTaquilla = excelServiceConUnaCompraTaquilla.getCellValue("E2");
		Assert.assertEquals(1.0, cantidadDeComprasDeSesionConUnaCompraTaquilla);
	}

	@Test
	@Transactional
	public void testInformeExcelVentasSoloMuestraDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		altaCompra(sesion, "1", "2", true, usuario);
		altaCompra(sesion, "1", "3", false, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		ExcelService excelServiceConTresCompras = reportService.getExcelServiceVentas(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
			new Locale("es"), usuario.getUsuario(), true);
		Assert.assertEquals(2, excelServiceConTresCompras.getTotalFilas());
		Object fechaSesionConDosCompras = excelServiceConTresCompras.getCellValue("B2");
		Assert.assertEquals("12:00", fechaSesionConDosCompras.toString().split(" ")[1]);
		Object cantidadDeComprasDeSesionConTresCompras = excelServiceConTresCompras.getCellValue("E2");
		Assert.assertEquals(3.0, cantidadDeComprasDeSesionConTresCompras);
		ExcelService excelServiceConUnaCompra = reportService.getExcelServiceVentas(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
			new Locale("es"), usuario2.getUsuario(), true);
		Assert.assertEquals(2, excelServiceConUnaCompra.getTotalFilas());
		Object fechaSesionConUnaCompra = excelServiceConUnaCompra.getCellValue("B2");
		Assert.assertEquals("13:00", fechaSesionConUnaCompra.toString().split(" ")[1]);
		Object cantidadDeComprasDeSesionConUnaCompra = excelServiceConUnaCompra.getCellValue("E2");
		Assert.assertEquals(1.0, cantidadDeComprasDeSesionConUnaCompra);
	}

	@Test
	@Transactional
	public void testInformeExcelEventosSoloMuestraDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		altaCompra(sesion, "1", "2", true, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		ExcelService excelServiceConDosCompras = reportService.getExcelServiceEventos(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
				new Locale("es"), usuario.getUsuario());
		Assert.assertEquals(2, excelServiceConDosCompras.getTotalFilas());
		Object fechaSesionConDosCompras = excelServiceConDosCompras.getCellValue("B2");
		Object cantidadDeComprasDeSesionConDosCompras = excelServiceConDosCompras.getCellValue("D2");
		Assert.assertEquals(2.0, cantidadDeComprasDeSesionConDosCompras);

		ExcelService excelServiceConUnaCompra = reportService.getExcelServiceEventos(sdf.format(cal.getTime()), sdf.format(compra.getFecha()),
				new Locale("es"), usuario2.getUsuario());
		Assert.assertEquals(2, excelServiceConUnaCompra.getTotalFilas());
		Object fechaSesionConUnaCompra = excelServiceConUnaCompra.getCellValue("B2");
		Object cantidadDeComprasDeSesionConUnaCompra = excelServiceConUnaCompra.getCellValue("D2");
		Assert.assertEquals(1.0, cantidadDeComprasDeSesionConUnaCompra);
	}

	@Test
	@Transactional
	public void testInformePDFTaquillaDondeTotalTaquillaTPVSoloMuestranDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra.getId(), "123");
		Compra compra2 = altaCompra(sesion, "1", "2", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra2.getId(), "1234");
		Compra compra3 = altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		comprasDAO.guardarCodigoPagoTarjeta(compra3.getId(), "12345");
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = sdf.format(cal.getTime());
		String fechaFin = sdf.format(compra.getFecha());

		Object[] totalTaquillaTPVUsuario1 = comprasDAO.getTotalTaquillaTpv(fechaInicio, fechaFin, usuario.getUsuario());
		Assert.assertEquals("2.00", totalTaquillaTPVUsuario1[0].toString());
		Assert.assertEquals("2", totalTaquillaTPVUsuario1[1].toString());
		Object[] totalTaquillaTPVUsuario2 = comprasDAO.getTotalTaquillaTpv(fechaInicio, fechaFin, usuario2.getUsuario());
		Assert.assertEquals("1.00", totalTaquillaTPVUsuario2[0].toString());
		Assert.assertEquals("1", totalTaquillaTPVUsuario2[1].toString());
	}

	@Test
	@Transactional
	public void testInformePDFTaquillaDondeTotalTaquillaEfectivoSoloMuestranDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		altaCompra(sesion, "1", "2", true, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = sdf.format(cal.getTime());
		String fechaFin = sdf.format(compra.getFecha());

		Object[] totalTaquillaTPVUsuario1 = comprasDAO.getTotalTaquillaEfectivo(fechaInicio, fechaFin, usuario.getUsuario());
		Assert.assertEquals("2.00", totalTaquillaTPVUsuario1[0].toString());
		Assert.assertEquals("2", totalTaquillaTPVUsuario1[1].toString());
		Object[] totalTaquillaTPVUsuario2 = comprasDAO.getTotalTaquillaEfectivo(fechaInicio, fechaFin, usuario2.getUsuario());
		Assert.assertEquals("1.00", totalTaquillaTPVUsuario2[0].toString());
		Assert.assertEquals("1", totalTaquillaTPVUsuario2[1].toString());
	}

	@Test
	@Transactional
	public void testInformePDFTaquillaDondeTotalOnlineSoloMuestranDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", false, usuario);
		altaCompra(sesion, "1", "2", false, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", false, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = sdf.format(cal.getTime());
		String fechaFin = sdf.format(compra.getFecha());

		Object[] totalTaquillaTPVUsuario1 = comprasDAO.getTotalOnline(fechaInicio, fechaFin, usuario.getUsuario());
		Assert.assertEquals("2.00", totalTaquillaTPVUsuario1[0].toString());
		Assert.assertEquals("2", totalTaquillaTPVUsuario1[1].toString());
		Object[] totalTaquillaTPVUsuario2 = comprasDAO.getTotalOnline(fechaInicio, fechaFin, usuario2.getUsuario());
		Assert.assertEquals("1.00", totalTaquillaTPVUsuario2[0].toString());
		Assert.assertEquals("1", totalTaquillaTPVUsuario2[1].toString());
	}

	@Test
	@Transactional
	public void testInformePDFEfectivoDondeComprasSoloMuestranDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		altaCompra(sesion, "1", "2", true, usuario);
		altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = sdf.format(cal.getTime());
		String fechaFin = sdf.format(compra.getFecha());

		List<Object[]> comprasEfectivo = comprasDAO.getComprasEfectivo(fechaInicio, fechaFin, usuario.getUsuario());
		String totalButacas = comprasEfectivo.get(0)[3].toString();
		Assert.assertEquals("2", totalButacas);
		List<Object[]> comprasEfectivo2 = comprasDAO.getComprasEfectivo(fechaInicio, fechaFin, usuario2.getUsuario());
		String totalButacasSesion2 = comprasEfectivo2.get(0)[3].toString();
		Assert.assertEquals("1", totalButacasSesion2);
	}

	@Test
	@Transactional
	public void testInformePdfTpvSubtotalesDondeComprasSoloMuestranDatosDeLasSalasDelUsuario() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra.getId(), "123");
		Compra compra2 = altaCompra(sesion, "1", "2", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra2.getId(), "1234");
		Compra compra3 = altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		comprasDAO.guardarCodigoPagoTarjeta(compra3.getId(), "12345");
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaInicio = sdf.format(cal.getTime());
		String fechaFin = sdf.format(compra.getFecha());

		List<Object[]> comprasTpv = comprasDAO.getComprasTpv(fechaInicio, fechaFin, usuario.getUsuario());
		Assert.assertEquals(2, comprasTpv.size());
		List<Object[]> comprasTpv1 = comprasDAO.getComprasTpv(fechaInicio, fechaFin, usuario2.getUsuario());
		Assert.assertEquals(1, comprasTpv1.size());
	}

	@Test
	@Transactional
	public void testFiltraSalasParaInforme() throws IOException {
		altaCine();
		Sala sala = altaSala("Sala 1");
		Sala salaDondeUsuario1NoTienePermiso = altaSala("Sala 2");
		Usuario usuario = altaUsuario("Nombre", "Mail", "login");
		Usuario usuario2 = altaUsuario("Nombre", "Mail", "login2");
		altaRelacionSalaUsuario(sala, usuario);
		altaRelacionSalaUsuario(salaDondeUsuario1NoTienePermiso, usuario2);
		Plantilla plantilla = altaPlantilla(sala);
		Plantilla plantilla2 = altaPlantilla(salaDondeUsuario1NoTienePermiso);
		altaLocalizacion();
		altaTarifa();
		altaEvento();
		Sesion sesion = altaSesion(sala, plantilla, "12:00", usuario);
		Sesion sesionDondeUsuarioNoTienePermiso = altaSesion(salaDondeUsuario1NoTienePermiso, plantilla2, "13:00", usuario2);
		Compra compra = altaCompra(sesion, "1", "1", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra.getId(), "123");
		Compra compra2 = altaCompra(sesion, "1", "2", true, usuario);
		comprasDAO.guardarCodigoPagoTarjeta(compra2.getId(), "1234");
		Compra compra3 = altaCompra(sesionDondeUsuarioNoTienePermiso, "1", "1", true, usuario2);
		comprasDAO.guardarCodigoPagoTarjeta(compra3.getId(), "12345");
		Calendar cal = Calendar.getInstance();
		cal.setTime(compra.getFecha());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		/*org.hsqldb.util.DatabaseManager.main(new String[] {
				"--url",  "jdbc:hsqldb:mem:paranimf-example-db;", "--user", "sa"
		});*/
		List<SesionDTO> sesiones1 = sesionesDAO.getSesionesCinePorFechas(null, null, null, usuario.getUsuario());
		Assert.assertEquals(1, sesiones1.size());
		Assert.assertEquals("12:00", sdf.format(sesiones1.get(0).getFechaCelebracion()));
		List<SesionDTO> sesiones2 = sesionesDAO.getSesionesCinePorFechas(null, null, null, usuario2.getUsuario());
		Assert.assertEquals(1, sesiones2.size());
		Assert.assertEquals("13:00", sdf.format(sesiones2.get(0).getFechaCelebracion()));
	}
}
