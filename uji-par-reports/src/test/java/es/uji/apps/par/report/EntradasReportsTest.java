package es.uji.apps.par.report;

import es.uji.apps.fopreports.serialization.ReportSerializationException;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.ReportDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.db.TpvsDTO;
import es.uji.apps.par.model.*;
import es.uji.apps.par.services.EntradasService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
@Transactional
public class EntradasReportsTest {
	@Autowired
	CinesDAO cinesDAO;

	@Autowired
	EventosDAO eventosDAO;

	@Autowired
	TpvsDAO tpvsDAO;

	@Autowired
	SalasDAO salasDAO;

	@Autowired
	SesionesDAO sesionesDAO;

	@Autowired
	LocalizacionesDAO localizacionesDAO;

	@Autowired
	TarifasDAO tarifasDAO;

	@Autowired
	PlantillasDAO plantillasDAO;

	@Autowired
	ComprasDAO comprasDAO;

	@Autowired
	ButacasDAO butacasDAO;

	@Autowired
	EntradasService entradasService;

	@Autowired
	TiposEventosDAO tiposEventosDAO;

	@Autowired
	private UsuariosDAO usuariosDAO;

	private Cine cine;
	private Tpv tpv;
	private Evento evento;
	private Sala sala;
	private Localizacion localizacion;
	private Tarifa tarifa;
	private Plantilla plantilla;
	private Sesion sesion;
	private Compra compra;
	protected Usuario usuario;

	@Before
	public void altaElementos() {
		altaUsuario();
		altaCine();
		altaLocalizacion();
		altaSala();
		usuariosDAO.addSalaUsuario(sala, usuario);
		altaTpv();
		altaPlantilla();
		altaTarifa();
		altaEvento();
		altaSesion();
		altaCompra();
	}

	private void altaUsuario() {
		usuario = new Usuario();
		usuario.setUsuario("login");
		usuario.setMail("mail");
		usuario.setNombre("nombre");
		usuariosDAO.addUser(usuario);
	}

	private void altaPlantilla() {
		plantilla = new Plantilla();
		plantilla.setSala(sala);
		plantilla.setNombre("Sin plantilla");
		Plantilla plantillaInsertada = plantillasDAO.add(plantilla);
		plantilla.setId(plantillaInsertada.getId());
	}


	private void altaTarifa() {
		tarifa = new Tarifa();
		tarifa.setNombre("Tarifa");
		tarifa.setDefecto("on");
		tarifa.setIsPublico("on");
		TarifaDTO tarifaInsertada = tarifasDAO.add(Tarifa.toDTO(tarifa));
		tarifa.setId(tarifaInsertada.getId());
	}

	private void altaLocalizacion() {
		localizacion = new Localizacion();
		localizacion.setNombreEs("Localizacion");
		localizacion.setNombreVa("Localitzacio");
		localizacion.setCodigo("codi");
		localizacion.setTotalEntradas(100);
		localizacionesDAO.add(localizacion);
	}

	private void altaSala() {
		sala = new Sala();
		sala.setNombre("Sala 1");
		sala.setCine(cine);
		salasDAO.addSala(sala);
	}

	private void altaTpv() {
		tpv = new Tpv();
		tpv.setNombre("TPV Prueba");
		TpvsDTO tpvDefecto = tpvsDAO.getTpvDefault();
		if (tpvDefecto == null)
			tpvsDAO.addTpv(tpv, true);

		TpvsDTO tpvDefectoInsertado = tpvsDAO.getTpvDefault();
		tpv.setId(tpvDefectoInsertado.getId());
	}

	private void altaCine() {
		cine = new Cine();
		cine.setNombre("Cine Prueba");
		cinesDAO.addCine(cine);
	}

	private void altaEvento() {
		evento = new Evento();
		evento.setTituloEs("Nombre Evento Castellano");
		evento.setTituloVa("Nombre Evento Valenciano");
		evento.setParTpv(tpv);
		eventosDAO.addEvento(evento);
	}

	private void altaSesion() {
		sesion = new Sesion();
		sesion.setEvento(evento);
		sesion.setFechaCelebracionWithDate(Calendar.getInstance().getTime());
		sesion.setHoraCelebracion("12:00");
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
	}

	private void altaCompra() {
		boolean taquilla = true;
		BigDecimal importe = new BigDecimal(1);
		CompraDTO compraDTO = comprasDAO.insertaCompra(sesion.getId(), Calendar.getInstance().getTime(), taquilla, importe, usuario.getUsuario());
		Butaca butaca = new Butaca();
		butaca.setFila("1");
		butaca.setNumero("1");
		butaca.setLocalizacion(localizacion.getCodigo());
		butaca.setTipo(String.valueOf(tarifa.getId()));
		butacasDAO.reservaButacas(sesion.getId(), compraDTO, Arrays.asList(butaca), usuario.getUsuario());
		compra = Compra.compraDTOtoCompra(compraDTO);
	}

	private void altaReports(String reportClass, String tipo) {
		ReportDTO reportDTO = new ReportDTO(sala.getId(), tipo, reportClass);
		salasDAO.addReport(reportDTO);
	}

	@Test
	public void testNombreClaseEntradaTaquilla() throws ReportSerializationException, SAXException, IOException {
		altaReports("es.uji.apps.par.report.EntradaTaquillaReport", EntradaReportFactory.TIPO_ENTRADA_TAQUILLA);
		EntradaReportTaquillaInterface entrada = entradasService.generaEntradaTaquillaYRellena(compra.getUuid(), usuario.getUsuario(), "");
		Assert.assertNotNull(entrada);
		Assert.assertEquals("es.uji.apps.par.report.EntradaTaquillaReport", entrada.getClass().getName());
	}

	@Test
	public void testNombreClaseEntradaOnline() throws ReportSerializationException, SAXException, IOException {
		altaReports("es.uji.apps.par.report.EntradaReport", EntradaReportFactory.TIPO_ENTRADA_ONLINE);
		EntradaReportOnlineInterface entrada = entradasService.generaEntradaOnlineYRellena(compra.getUuid(), usuario.getUsuario(), "");
		Assert.assertNotNull(entrada);
		Assert.assertEquals("es.uji.apps.par.report.EntradaReport", entrada.getClass().getName());
	}

	@Test
	public void testNombreClaseEntradaOnlineActoGraduacion() throws ReportSerializationException, SAXException, IOException {
		TipoEvento tipoEvento = new TipoEvento("ActoGraduacion", "ActoGraduacion", false);
		TipoEvento tipoEventoInsertado = tiposEventosDAO.addTipoEvento(tipoEvento);

		Evento eventoActoGraduacion = new Evento();
		eventoActoGraduacion.setTituloEs("Nombre Evento Castellano");
		eventoActoGraduacion.setTituloVa("Nombre Evento Valenciano");
		eventoActoGraduacion.setParTipoEvento(tipoEventoInsertado);
		eventoActoGraduacion.setParTpv(tpv);
		eventosDAO.addEvento(eventoActoGraduacion);

		Sesion sesionActoGraduacion = new Sesion();
		sesionActoGraduacion.setEvento(eventoActoGraduacion);
		sesionActoGraduacion.setFechaCelebracionWithDate(Calendar.getInstance().getTime());
		sesionActoGraduacion.setHoraCelebracion("12:00");
		sesionActoGraduacion.setSala(sala);
		sesionActoGraduacion.setPlantillaPrecios(plantilla);
		List<PreciosSesion> preciosSesion = new ArrayList<PreciosSesion>();
		PreciosSesion precioSesion = new PreciosSesion();
		precioSesion.setSesion(sesionActoGraduacion);
		precioSesion.setPrecio(new BigDecimal(1));
		precioSesion.setLocalizacion(localizacion);
		precioSesion.setTarifa(tarifa);
		preciosSesion.add(precioSesion);
		sesionActoGraduacion.setPreciosSesion(preciosSesion);
		Sesion sesionInsertada = sesionesDAO.addSesion(sesionActoGraduacion, usuario.getUsuario());
		sesionActoGraduacion.setId(sesionInsertada.getId());

		boolean taquilla = true;
		BigDecimal importe = new BigDecimal(1);
		CompraDTO compraDTO = comprasDAO.insertaCompra(sesionActoGraduacion.getId(), Calendar.getInstance().getTime(), taquilla, importe, usuario.getUsuario());
		Butaca butaca = new Butaca();
		butaca.setFila("1");
		butaca.setNumero("1");
		butaca.setLocalizacion(localizacion.getCodigo());
		butaca.setTipo(String.valueOf(tarifa.getId()));
		butacasDAO.reservaButacas(sesionActoGraduacion.getId(), compraDTO, Arrays.asList(butaca), usuario.getUsuario());
		Compra compraActoGraduacion = Compra.compraDTOtoCompra(compraDTO);

		EntradaReportOnlineInterface entrada = entradasService.generaEntradaOnlineYRellena(compraActoGraduacion.getUuid(), usuario.getUsuario(), "");
		Assert.assertNotNull(entrada);
		Assert.assertEquals("es.uji.apps.par.report.EntradaActoGraduacionReport", entrada.getClass().getName());
	}
}
