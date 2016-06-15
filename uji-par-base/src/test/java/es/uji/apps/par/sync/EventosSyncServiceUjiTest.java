package es.uji.apps.par.sync;

import es.uji.apps.par.builders.UsuarioBuilder;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.dao.TiposEventosDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.UsuarioDTO;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.services.EventosSyncService;
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

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class EventosSyncServiceUjiTest extends SyncBaseTest
{
    private static final String RSS_CA = "uji/rss-ca.xml";
    private static final String RSS_ES = "uji/rss-es.xml";

    @Autowired
    EventosSyncService syncService;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    private UsuarioDTO user;

    @Before
    public void setup()
    {
        syncService.setTipo("uji");
        insertaTiposEventos();

        user = new UsuarioBuilder("user", "user@test.com", "user").build(entityManager);
    }

    private void insertaTiposEventos()
    {
        TipoEvento tipo = new TipoEvento();
        tipo.setNombreVa("Teatre");
        tipo.setNombreEs("Teatro");

        tiposEventosDAO.addTipoEvento(tipo);

        tipo = new TipoEvento();
        tipo.setNombreVa("Cinema");
        tipo.setNombreEs("Cine");

        tiposEventosDAO.addTipoEvento(tipo);
    }

    @Test
    @Transactional
    public void testSyncNuevosItems() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), "");

        List<Evento> eventos = eventosDAO.getEventosConSesiones(user.getUsuario());

        assertEquals("Número de eventos nuevos", 2, eventos.size());
    }

    @Test
    @Transactional
    public void testSyncNuevoItemDatos() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertNotNull("Evento nuevo insertado", evento);

        assertEquals("rssId del evento", "1", evento.getRssId());
        assertEquals("Título VA del evento", "Madre Coraje", evento.getTituloVa());
        assertTrue(
                "Descripción VA del evento",
                evento.getDescripcionVa().startsWith(
                        "<p>\r\n\t<strong>Adaptaci&oacute; i direcci&oacute;</strong>: Ricardo Iniesta"));

        assertEquals("Características VA del evento",
                "Basada en la Historia de la vida de la estafadora y aventurera Coraje de Grimmelshausen.",
                evento.getCaracteristicasVa());
        assertEquals("Duración VA del evento", "120", evento.getDuracionVa());
        assertEquals("Tipo del evento", "Teatre", evento.getParTiposEvento().getNombreVa());

        assertEquals("Asientos numerados del evento", true, evento.getAsientosNumerados());
        assertNotNull("Tipo del evento no nulo", evento.getParTiposEvento());

        assertEquals("URL imagen del evento", "https://www.google.es/images/srpr/logo4w.png", evento.getImagenSrc());
        assertEquals("Contenido imagen del evento", 18946, evento.getImagen().length);
        assertEquals("Content-type imagen del evento", "image/jpeg", evento.getImagenContentType());
    }

    @Test
    @Transactional
    public void testSyncNuevoItemDatosIdiomaCa() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertEquals("Título VA del evento", "Madre Coraje", evento.getTituloVa());
        assertTrue(
                "Descripción VA del evento",
                evento.getDescripcionVa().startsWith(
                        "<p>\r\n\t<strong>Adaptaci&oacute; i direcci&oacute;</strong>: Ricardo Iniesta"));

        assertEquals("Características VA del evento",
                "Basada en la Historia de la vida de la estafadora y aventurera Coraje de Grimmelshausen.",
                evento.getCaracteristicasVa());

        assertEquals("Duración VA del evento", "120", evento.getDuracionVa());
        assertEquals("Tipo del evento", "Teatre", evento.getParTiposEvento().getNombreVa());
    }

    @Test
    @Transactional
    public void testSyncNuevoItemDatosIdiomaEs() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_ES), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertEquals("Título ES del evento", "Madre Coraje (ES)", evento.getTituloEs());
        assertTrue(
                "Descripción ES del evento",
                evento.getDescripcionEs().startsWith(
                        "(ES)<p>\r\n\t<strong>Adaptaci&oacute; i direcci&oacute;</strong>: Ricardo Iniesta"));
        assertEquals("Duración ES del evento", "120 (ES)", evento.getDuracionEs());
        assertEquals("Tipo del evento", "Teatro", evento.getParTiposEvento().getNombreEs());
        assertEquals("Características del evento",
                "Basada en la Historia de la vida de la estafadora y aventurera Coraje de Grimmelshausen. (ES)",
                evento.getCaracteristicasEs());
    }

    @Test
    @Transactional
    // Comprobar que al ir a guardar el campo título del otro idioma no es null ni cadena vacía (petaría la constraint de Oracle)
    public void testSyncNuevoItemOtroIdiomaOk() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertNotNull("Título del otro idioma no nulo", evento.getTituloEs());
        assertTrue("Título del otro idioma distinto de \"\"", !evento.getTituloEs().equals(""));
    }

    @Test
    @Transactional
    public void testSyncYaExistente() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("1");
        eventosDAO.updateEventoDTO(eventoDTO);

        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertEquals("Título VA de item existente", "Madre Coraje", evento.getTituloVa());
        assertEquals("Tipo del evento", "Teatre", evento.getParTiposEvento().getNombreVa());
    }

    @Test
    @Transactional
    public void testSyncYaExistenteNoModificaOtroIdioma() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("1");
        eventoDTO.setTituloEs("Título ES");
        eventoDTO.setCaracteristicasEs("Características ES");
        eventoDTO.setDescripcionEs("Descripción ES");
        eventoDTO.setDuracionEs("Duración ES");
        eventosDAO.updateEventoDTO(eventoDTO);

        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("1", "");

        assertEquals("Título ES de item existente", "Título ES", evento.getTituloEs());
        assertEquals("Características ES de item existente", "Características ES", evento.getCaracteristicasEs());
        assertEquals("Descripción ES de item existente", "Descripción ES", evento.getDescripcionEs());
        assertEquals("Duración ES de item existente", "Duración ES", evento.getDuracionEs());
    }
}
