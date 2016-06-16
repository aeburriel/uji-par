package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.dao.TpvsDAO;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.model.Tpv;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
@Transactional
public class EventosDAOTest
{
	private static final String SORT = "[{\"property\":\"tituloVa\", \"direction\":\"ASC\"}]";
	private static final int START = 0;
	private static final int LIMIT = 100;

    @Autowired
	EventosDAO eventosDAO;

    @Autowired
	TiposEventosDAO tiposEventosDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    @Before
    public void setup()
    {
        tpvsDAO.addTpvDefault();
    }

	@PersistenceContext
	protected EntityManager entityManager;

	UsuarioDTO usuarioConEventos;
	UsuarioDTO usuarioSinEventos;
	EventoDTO evento1;
	final String RSS_ID = "100";

	@Before
	public void setUp()
	{
		//Usuario1 -> Cine1 -> Evento1 -> Sesion1 (Sala1)
		//   "          "   ->    "    -> Sesion2 (Sala1)
		//   "          "   -> Evento2

		//   -     -> Cine2 -> Evento3 -> Sesion3 (Sala2)
		//   -     ->   "   ->    "    -> Sesion4 (Sala2)
		//   -     ->   "   ->    "    -> Sesion5 (Sala2)

		//   -     ->  null -> Evento4 -> Sesion6 (Sala2)

		//Usuario2

		TipoEventoDTO tipoEvento = new TipoEventoBuilder("tipo", "tipo", false)
				.build(entityManager);


		CineDTO cine1 = new CineBuilder("Cine 1")
				.build(entityManager);

		SalaDTO sala1 = new SalaBuilder("Sala 1", cine1)
				.build(entityManager);

		evento1 = new EventoBuilder("Evento 1", "Esdeveniment 1", cine1, tipoEvento)
				.withRssId(RSS_ID)
				.withSesion("Sesión 1", sala1)
				.withSesion("Sesión 2", sala1)
				.build(entityManager);

		new EventoBuilder("Evento 2", "Esdeveniment 2", cine1, tipoEvento)
				.build(entityManager);

		usuarioConEventos = new UsuarioBuilder("User 1", "user1@test.com", "user1")
				.withSala(sala1)
				.build(entityManager);



		CineDTO cine2 = new CineBuilder("Cine 2")
				.build(entityManager);

		SalaDTO sala2 = new SalaBuilder("Sala 2", cine2)
				.build(entityManager);

		new EventoBuilder("Evento 3", "Esdeveniment 3", cine2, tipoEvento)
				.withSesion("Sesión 3", sala2)
				.withSesion("Sesión 4", sala2)
				.withSesion("Sesión 5", sala2)
				.build(entityManager);




		usuarioSinEventos = new UsuarioBuilder("User 2", "user2@test.com", "user2")
				.build(entityManager);

		new EventoBuilder("Evento 4", "Esdeveniment 4", null, tipoEvento)
				.withSesion("Sesión 6", sala2)
				.build(entityManager);



		entityManager.flush();
		entityManager.clear();
	}

	private Evento getEvento()
	{
		TipoEvento tipoEvento = tiposEventosDAO.addTipoEvento(new TipoEvento("Tipo evento"));
		Tpv tpv = new Tpv();
		tpv.setNombre("Test TPV");
		if (tpvsDAO.getTpvDefault() == null)
			tpvsDAO.addTpv(tpv, true);

		TpvsDTO tpvDefault = tpvsDAO.getTpvDefault();
		tpv.setId(tpvDefault.getId());
		Evento evento = new Evento("Nombre", tipoEvento);
		evento.setParTpv(tpv);
		return evento;
	}

    @Test
    public void addEvento()
    {
        Evento parEvento = getEvento();
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
    }

    @Test
    public void deleteEvento()
    {
    	Evento parEvento = getEvento();
    	parEvento = eventosDAO.addEvento(parEvento);
        Assert.assertEquals(1, eventosDAO.removeEvento(parEvento.getId()));
    }

    @Test
    public void updateEvento()
    {
    	Evento parEvento = getEvento();
    	parEvento = eventosDAO.addEvento(parEvento);

        parEvento.setTituloEs("Prueba2");
        Evento eventoActualizado = eventosDAO.updateEvento(parEvento, usuarioConEventos.getUsuario());
        Assert.assertEquals(parEvento.getId(), eventoActualizado.getId());
    }

    @Test
    public void addEventoConIdiomas()
    {
        Evento parEvento = getEvento();
        parEvento.setCaracteristicasVa("valencia");
        parEvento.setComentariosEs("comentarios");
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
    }

    @Test
    public void deleteImagen()
    {
        Evento parEvento = getEvento();
        parEvento.setCaracteristicasVa("valencia");
        parEvento.setComentariosEs("comentarios");
        parEvento.setImagen("hola".getBytes());
        parEvento.setImagenSrc("hola");
        parEvento.setImagenContentType("");
        parEvento = eventosDAO.addEvento(parEvento);

        Assert.assertNotNull(parEvento.getId());
        eventosDAO.deleteImagen(parEvento.getId());
    }

	@Test
	public void getEventosUsuarioConEventos()
	{
		List<Evento> eventos = eventosDAO.getEventos(EventosDAOTest.SORT, EventosDAOTest.START, EventosDAOTest.LIMIT, usuarioConEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 3);
	}

	@Test
	public void getEventosUsuarioSinEventos()
	{
		List<Evento> eventos = eventosDAO.getEventos(EventosDAOTest.SORT, EventosDAOTest.START, EventosDAOTest.LIMIT, usuarioSinEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 1);
	}

	@Test
	public void getEventosActivosUsuarioConEventos()
	{
		List<Evento> eventos = eventosDAO.getEventosActivos(EventosDAOTest.SORT, EventosDAOTest.START, EventosDAOTest.LIMIT, usuarioConEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 2);
	}

	@Test
	public void getEventosActivosUsuarioSinEventos()
	{
		List<Evento> eventos = eventosDAO.getEventosActivos(EventosDAOTest.SORT, EventosDAOTest.START, EventosDAOTest.LIMIT, usuarioSinEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 1);
	}

	@Test
	public void getEventosConSesionesUsuarioConEventos()
	{
		List<Evento> eventos = eventosDAO.getEventosConSesiones(usuarioConEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 1);
	}

	@Test
	public void getEventosConSesionesUsuarioSinEventos()
	{
		List<Evento> eventos = eventosDAO.getEventosConSesiones(usuarioSinEventos.getUsuario());

		Assert.assertNotNull(eventos);
		Assert.assertTrue(eventos.size() == 0);
	}

	@Test
	public void getEventoByIdUsuarioConEventos()
	{
		EventoDTO evento = eventosDAO.getEventoById(evento1.getId(), usuarioConEventos.getUsuario());

		Assert.assertNotNull(evento);
		Assert.assertEquals(evento1.getId(), evento.getId());
		Assert.assertEquals(evento1.getTituloEs(), evento.getTituloEs());
	}

	@Test
	public void getEventoByIdUsuarioSinEventos()
	{
		EventoDTO evento = eventosDAO.getEventoById(evento1.getId(), usuarioSinEventos.getUsuario());

		Assert.assertNull(evento);
	}

	@Test
	public void getEventosByRssIdUsuarioConEventos()
	{
		EventoDTO evento = eventosDAO.getEventoByRssId(evento1.getRssId(), usuarioConEventos.getUsuario());

		Assert.assertNotNull(evento);
		Assert.assertTrue(evento.getRssId().equals(RSS_ID));
		Assert.assertEquals(evento1.getId(), evento.getId());
		Assert.assertEquals(evento1.getTituloEs(), evento.getTituloEs());
	}

	@Test
	public void getEventosByRssIdUsuarioSinEventos()
	{
		EventoDTO evento = eventosDAO.getEventoByRssId(evento1.getRssId(), usuarioSinEventos.getUsuario());

		Assert.assertNull(evento);
	}

	@Test
	public void getTotalEventosActivosUsuarioConEventos()
	{
		int totalEventosActivos = eventosDAO.getTotalEventosActivos(usuarioConEventos.getUsuario());

		Assert.assertTrue(totalEventosActivos == 2);
	}

	@Test
	public void getTotalEventosActivosUsuarioSinEventos()
	{
		int totalEventosActivos = eventosDAO.getTotalEventosActivos(usuarioSinEventos.getUsuario());

		Assert.assertTrue(totalEventosActivos == 1);
	}

	@Test
	public void getTotalEventosUsuarioConEventos()
	{
		int totalEventosActivos = eventosDAO.getTotalEventos(usuarioConEventos.getUsuario());

		Assert.assertTrue(totalEventosActivos == 3);
	}

	@Test
	public void getTotalEventosUsuarioSinEventos()
	{
		int totalEventosActivos = eventosDAO.getTotalEventos(usuarioSinEventos.getUsuario());

		Assert.assertTrue(totalEventosActivos == 1);
	}
}
