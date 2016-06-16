package es.uji.apps.par.sync;

import es.uji.apps.par.builders.CineBuilder;
import es.uji.apps.par.builders.SalaBuilder;
import es.uji.apps.par.builders.UsuarioBuilder;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.model.Sala;
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
import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class EventosSyncServiceBenicassimTest extends SyncBaseTest
{
    private static final String RSS_CA = "benicassim/rss-ca.xml";
    private static final String RSS_ES = "benicassim/rss-es.xml";

    @Autowired
    EventosSyncService syncService;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    SesionesDAO sesionesDao;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @Autowired
    PlantillasDAO plantillasDao;
    
    @Autowired
    SalasDAO salasDAO;

    @PersistenceContext
    protected EntityManager entityManager;
    
    private Plantilla plantilla;
    private Sala sala;
    private UsuarioDTO user;

    @Before
    public void setup()
    {
        syncService.setTipo("benicassim");

        CineDTO cine1 = new CineBuilder("Cine 1")
                .build(entityManager);

        SalaDTO salaDTO = new SalaBuilder("Sala 1", cine1)
                .build(entityManager);

        user = new UsuarioBuilder("user", "user@test.com", "user")
                .withSala(salaDTO)
                .build(entityManager);

        sala = Sala.salaDTOtoSala(salaDTO);

        insertaPlantillaPrecios();
        insertaTiposEventos();
    }

    private void insertaPlantillaPrecios()
    {
        plantilla = new Plantilla("test");
        plantilla.setSala(sala);
        plantillasDao.add(plantilla);
    }

    private void insertaTiposEventos()
    {
        insertaTipo("Teatro", "Teatre");
        insertaTipo("Cine", "Cinema");
        insertaTipo("Libro y lectura", "Llibre i lectura");
        insertaTipo("Música", "Música");

        insertaTipo("Actividades infantiles", "Activitats infantils");
        insertaTipo("Exposiciones", "Exposicions");
        insertaTipo("Teatro infantil", "Teatre infantil");
        insertaTipo("Cine infantil", "Cine infantil");
    }

    private void insertaTipo(String nombreEs, String nombreVa)
    {
        TipoEvento tipo = new TipoEvento();
        tipo.setNombreEs(nombreEs);
        tipo.setNombreVa(nombreVa);
        tiposEventosDAO.addTipoEvento(tipo);
    }

    @Test
    @Transactional
    public void testSyncNuevosItems() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        List<Evento> eventos = eventosDAO.getEventosConSesiones(user.getUsuario());

        assertEquals("Número de eventos nuevos", 1, eventos.size());
    }

    @Test
    @Transactional
    public void testSyncNuevoItemDatos() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        assertNotNull("Evento nuevo insertado", evento);

        assertEquals("rssId del evento", "789", evento.getRssId());
        assertEquals("Título VA del evento", "JUSTIN Y LA ESPADA DEL VALOR", evento.getTituloVa());
        assertEquals("Descripción VA del evento", "", evento.getDescripcionVa());

        assertEquals("Tipo del evento", "Cinema", evento.getParTiposEvento().getNombreVa());

        assertEquals("Asientos numerados del evento", false, evento.getAsientosNumerados());

        assertEquals("URL imagen del evento",
                "http://www.benicassimcultura.es/ficheros_sw/adjuntos/5294bcadaf1d3_justinweb.jpg", evento.getImagenSrc());
        assertEquals("Contenido imagen del evento", 347431, evento.getImagen().length);
        assertEquals("Content-type imagen del evento", "image/jpg", evento.getImagenContentType());
    }

    @Test
    @Transactional
    @SuppressWarnings("deprecation")
    public void testSyncSesiones() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        List<SesionDTO> sesiones = sesionesDao.getSesiones(evento.getId(), "", 0, 100, user.getUsuario());

        assertEquals("Número sesiones", 3, sesiones.size());
        
        SesionDTO sesion1 = sesiones.get(0);
        assertEquals("Sesión 1 fecha", new Timestamp(113, 11, 27, 18, 30, 0, 0), sesion1.getFechaCelebracion());
        assertEquals("Sesión 1 RSS id", "7", sesion1.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 1 fecha inicio venta online", new Timestamp(113, 10, 27, 18, 30, 0, 0), sesion1.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 1 fecha fin venta online", new Timestamp(113, 11, 27, 17, 30, 0, 0), sesion1.getFechaFinVentaOnline());
        
        SesionDTO sesion2 = sesiones.get(1);
        assertEquals("Sesión 2 fecha", new Timestamp(113, 11, 29, 18, 30, 0, 0), sesion2.getFechaCelebracion());
        assertEquals("Sesión 2 RSS id", "8", sesion2.getRssId());

        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 2 fecha inicio venta online", new Timestamp(113, 10, 29, 18, 30, 0, 0), sesion2.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 2 fecha fin venta online", new Timestamp(113, 11, 29, 17, 30, 0, 0), sesion2.getFechaFinVentaOnline());
        
        SesionDTO sesion3 = sesiones.get(2);
        assertEquals("Sesión 3 fecha", new Timestamp(113, 11, 30, 18, 30, 0, 0), sesion3.getFechaCelebracion());
        assertEquals("Sesión 3 RSS id", "9", sesion3.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 3 fecha inicio venta online", new Timestamp(113, 10, 30, 18, 30, 0, 0), sesion3.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 3 fecha fin venta online", new Timestamp(113, 11, 30, 17, 30, 0, 0), sesion3.getFechaFinVentaOnline());
    }
    
    @Test
    @Transactional
    @SuppressWarnings("deprecation")
    public void testAnyadeNuevaSesion() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("789");
        eventoDTO = eventosDAO.updateEventoDTO(eventoDTO);
        
        SesionDTO sesionExistente = new SesionDTO();
        sesionExistente.setParEvento(eventoDTO);
        sesionExistente.setRssId("123");
        sesionExistente.setFechaCelebracion(new Timestamp(113, 6, 7, 20, 0, 0, 0));
        sesionExistente.setParSala(Sala.salaToSalaDTO(sala));
        sesionExistente = sesionesDao.persistSesion(sesionExistente);
        
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        List<SesionDTO> sesiones = sesionesDao.getSesiones(evento.getId(), "", 0, 100, user.getUsuario());

        assertEquals("Número sesiones", 4, sesiones.size());
        
        SesionDTO sesion1 = sesiones.get(0);
        assertEquals("Sesión 1 fecha", sesionExistente.getFechaCelebracion(), sesion1.getFechaCelebracion());
        assertEquals("Sesión 1 RSS id", sesionExistente.getRssId(), sesion1.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 1 fecha inicio venta online", sesionExistente.getFechaInicioVentaOnline(), sesion1.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 1 fecha fin venta online", sesionExistente.getFechaFinVentaOnline(), sesion1.getFechaFinVentaOnline());
        
        SesionDTO sesion2 = sesiones.get(1);
        assertEquals("Sesión 2 fecha", new Timestamp(113, 11, 27, 18, 30, 0, 0), sesion2.getFechaCelebracion());
        assertEquals("Sesión 2 RSS id", "7", sesion2.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 2 fecha inicio venta online", new Timestamp(113, 10, 27, 18, 30, 0, 0), sesion2.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 2 fecha fin venta online", new Timestamp(113, 11, 27, 17, 30, 0, 0), sesion2.getFechaFinVentaOnline());
    }
    
    @Test
    @Transactional
    @SuppressWarnings("deprecation")
    public void testModificaSesionExistente() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("789");
        eventoDTO = eventosDAO.updateEventoDTO(eventoDTO);
        
        SesionDTO sesionExistente = new SesionDTO();
        sesionExistente.setParEvento(eventoDTO);
        sesionExistente.setRssId("7");
        sesionExistente.setFechaCelebracion(new Timestamp(0));
        sesionExistente.setFechaInicioVentaOnline(new Timestamp(0));
        sesionExistente.setFechaFinVentaOnline(new Timestamp(0));
        sesionExistente.setParSala(Sala.salaToSalaDTO(sala));
        sesionExistente.setParPlantilla(Plantilla.plantillaPreciosToPlantillaPreciosDTO(plantilla));
        sesionExistente = sesionesDao.persistSesion(sesionExistente);
        
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());
        List<SesionDTO> sesiones = sesionesDao.getSesiones(evento.getId(), "", 0, 100, user.getUsuario());

        assertEquals("Número sesiones", 3, sesiones.size());
        
        SesionDTO sesion1 = sesiones.get(0);
        assertEquals("Sesión 1 id", sesionExistente.getId(), sesion1.getId());
        assertEquals("Sesión 1 fecha", new Timestamp(113, 11, 27, 18, 30, 0, 0), sesion1.getFechaCelebracion());
        assertEquals("Sesión 1 RSS id", sesionExistente.getRssId(), sesion1.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 1 fecha inicio venta online", new Timestamp(113, 10, 27, 18, 30, 0, 0), sesion1.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 1 fecha fin venta online", new Timestamp(113, 11, 27, 17, 30, 0, 0), sesion1.getFechaFinVentaOnline());
        
        SesionDTO sesion2 = sesiones.get(1);
        assertEquals("Sesión 2 fecha", new Timestamp(113, 11, 29, 18, 30, 0, 0), sesion2.getFechaCelebracion());
        assertEquals("Sesión 2 RSS id", "8", sesion2.getRssId());
        
        // El inicio de venta online depende de fichero de config
        //assertEquals("Sesión 2 fecha inicio venta online", new Timestamp(113, 10, 29, 18, 30, 0, 0), sesion2.getFechaInicioVentaOnline());
        
        assertEquals("Sesión 2 fecha fin venta online", new Timestamp(113, 11, 29, 17, 30, 0, 0), sesion2.getFechaFinVentaOnline());
    }


    @Test
    @Transactional
    public void testSyncNuevoItemDatosIdiomaCa() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        assertEquals("Título VA del evento", "JUSTIN Y LA ESPADA DEL VALOR", evento.getTituloVa());
        assertEquals("Descripción VA del evento", "", evento.getDescripcionVa());

        assertEquals("Tipo del evento", "Cinema", evento.getParTiposEvento().getNombreVa());
    }

    @Test
    @Transactional
    public void testSyncNuevoItemDatosIdiomaEs() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_ES), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        assertEquals("Título ES del evento", "JUSTIN Y LA ESPADA DEL VALOR", evento.getTituloEs());
        assertEquals("Descripción ES del evento", "", evento.getDescripcionEs());

        assertEquals("Tipo del evento", "Cine infantil", evento.getParTiposEvento().getNombreEs());
    }

    @Test
    @Transactional
    // Comprobar que al ir a guardar el campo título del otro idioma no es null ni cadena vacía (petaría la constraint de Oracle)
    public void testSyncNuevoItemOtroIdiomaOk() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("789", user.getUsuario());

        assertNotNull("Título del otro idioma no nulo", evento.getTituloEs());
        assertTrue("Título del otro idioma distinto de \"\"", !evento.getTituloEs().equals(""));
    }

    @Test
    @Transactional
    public void testSyncYaExistente() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("789");
        eventosDAO.updateEventoDTO(eventoDTO);

        syncService.sync(loadFromClasspath(RSS_CA), "");

        EventoDTO evento = eventosDAO.getEventoByRssId("789", "");

        assertEquals("Título VA de item existente", "JUSTIN Y LA ESPADA DEL VALOR", evento.getTituloVa());
        assertEquals("Tipo del evento", "Cinema", evento.getParTiposEvento().getNombreVa());
    }

    @Test
    @Transactional
    public void testSyncYaExistenteNoModificaOtroIdioma() throws Exception
    {
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setRssId("839");
        eventoDTO.setTituloEs("Título ES");
        eventoDTO.setCaracteristicasEs("Características ES");
        eventoDTO.setDescripcionEs("Descripción ES");
        eventoDTO.setDuracionEs("Duración ES");
        eventosDAO.updateEventoDTO(eventoDTO);

        syncService.sync(loadFromClasspath(RSS_CA), user.getUsuario());

        EventoDTO evento = eventosDAO.getEventoByRssId("839", user.getUsuario());

        assertEquals("Título ES de item existente", "Título ES", evento.getTituloEs());
        assertEquals("Características ES de item existente", "Características ES", evento.getCaracteristicasEs());
        assertEquals("Descripción ES de item existente", "Descripción ES", evento.getDescripcionEs());
        assertEquals("Duración ES de item existente", "Duración ES", evento.getDuracionEs());
    }
}
