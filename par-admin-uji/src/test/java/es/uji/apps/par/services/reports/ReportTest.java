package es.uji.apps.par.services.reports;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.SalasDAO;
import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.db.ReportDTO;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.report.InformeInterface;
import es.uji.apps.par.services.ReportService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;

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

	private Cine cine;

	private Usuario usuario;

	private Sala sala;

	@Transactional
	private void altaCine() {
		cine = new Cine();
		cine.setNombre("Cine Prueba");
		cinesDAO.addCine(cine);
	}

	@Transactional
	private void altaSala() {
		sala = new Sala();
		sala.setNombre("Sala 1");
		sala.setCine(cine);
		salasDAO.addSala(sala);
	}

	@Transactional
	private void altaUsuario() {
		usuario = new Usuario();
		usuario.setNombre("Nombre");
		usuario.setMail("Mail");
		usuario.setUsuario("login");
		usuariosDAO.addUser(usuario);
	}

	@Transactional
	private void altaRelacionSalaUsuario() {
		usuariosDAO.addSalaUsuario(sala, usuario);
	}

	@Transactional
	private void altaReports(String reportClass, String tipo) {
		ReportDTO reportDTO = new ReportDTO(sala.getId(), tipo, reportClass);
		salasDAO.addReport(reportDTO);
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFTaquilla() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaReport", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA);
		InformeInterface informe = reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario());
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeTaquillaReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA);
		reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaReport", EntradaReportFactory.TIPO_INFORME_PDF_TAQUILLA);
		reportService.generaYRellenaInformePDFTaquilla("2016-01-01", "2016-02-01", new Locale("es"),
				"NOTEXISTS");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFEfectivo() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEfectivoReport", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO);
		InformeInterface informe = reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario());
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeEfectivoReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEfectivoCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEfectivoReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO);
		reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEfectivoWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEfectivoReport", EntradaReportFactory.TIPO_INFORME_PDF_EFECTIVO);
		reportService.generaYRellenaInformePDFEfectivo("2016-01-01", "2016-02-01", new Locale("es"),
				"NOTEXISTS");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotales() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", EntradaReportFactory
				.TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES);
		InformeInterface informe = reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new
				Locale("es"), usuario.getUsuario());
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotalesCuandoReportClassNoExiste() throws ReportSerializationException,
			ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReportPersonalizado", EntradaReportFactory
				 .TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES);
		reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new Locale("es"), usuario
				 .getUsuario());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFTaquillaTpvSubtotalesWhenUserNotExists() throws ReportSerializationException,
			ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeTaquillaTpvSubtotalesReport", EntradaReportFactory
				 .TIPO_INFORME_PDF_TAQUILLA_TPV_SUBTOTALES);
		reportService.generaYRellenaInformePDFTaquillaTPVSubtotales("2016-01-01", "2016-02-01", new Locale("es"), "NOTEXISTS");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFEventos() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEventosReport", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS);
		InformeInterface informe = reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"),
				usuario.getUsuario());
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeEventosReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEventosCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEventosReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS);
		reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"), usuario.getUsuario());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFEventosWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		altaReports("es.uji.apps.par.report.InformeEventosReport", EntradaReportFactory.TIPO_INFORME_PDF_EVENTOS);
		reportService.generaYRellenaInformePDFEventos("2016-01-01", "2016-02-01", new Locale("es"), "NOTEXISTS");
	}

	@Test
	@Transactional
	public void testNombreClaseInformePDFSesiones() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		Sesion s = new Sesion(1);
		altaReports("es.uji.apps.par.report.InformeSesionReport", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES);
		InformeInterface informe = reportService.generaYRellenaPDFSesiones(Arrays.asList(s), new Locale("es"), usuario.getUsuario
				());
		Assert.assertNotNull(informe);
		Assert.assertEquals("es.uji.apps.par.report.InformeSesionReport", informe.getClass().getName());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFSesionesCuandoReportClassNoExiste() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		Sesion s = new Sesion(1);
		altaReports("es.uji.apps.par.report.InformeSesionReportPersonalizado", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES);
		reportService.generaYRellenaPDFSesiones(Arrays.asList(s), new Locale("es"), usuario.getUsuario());
	}

	@Test(expected = RuntimeException.class)
	@Transactional
	public void testNombreClaseInformePDFSesionesWhenUserNotExists() throws ReportSerializationException, ParseException {
		altaCine();
		altaSala();
		altaUsuario();
		altaRelacionSalaUsuario();
		Sesion s = new Sesion(1);
		altaReports("es.uji.apps.par.report.InformeSesionReport", EntradaReportFactory.TIPO_INFORME_PDF_SESIONES);
		reportService.generaYRellenaPDFSesiones(Arrays.asList(s), new Locale("es"), "NOTEXISTS");
	}
}
