package es.uji.apps.par.sync;

import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.model.*;
import es.uji.apps.par.services.EventosSyncService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = {
        "/applicationContext-db-test.xml",
        "/applicationContext-uji-test.xml",
})
public class EventosSyncServiceUjiTest extends SyncBaseTest
{
    private static final String RSS_TIPO_GRADUACION = "uji/rss-graduacion.xml";
    private static final String RSS_TIPO_GRADUACION_UPDATE = "uji/rss-graduacion-update.xml";

    @Autowired
    EventosSyncService syncService;

    @Autowired
    EventosDAO eventosDAO;

    @Autowired
    SesionesDAO sesionesDAO;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @Autowired
    TpvsDAO tpvsDAO;

    @Autowired
    private SalasDAO salasDAO;

    @Autowired
    private CinesDAO cinesDAO;

    @Autowired
    private TarifasDAO tarifasDAO;

    @Autowired
    private LocalizacionesDAO localizacionesDAO;

    @Autowired
    private PlantillasDAO plantillasDAO;

    @Autowired
    private UsuariosDAO usuariosDAO;

    Usuario usuario;

    @Before
    public void setup()
    {
        syncService.setTipo("uji");

        insertaTarifa();
        insertaTpvDefault();
        insertaSala();
        insertaTiposEventos();
    }

    private void insertaLocalizacion(Sala sala) {
        Plantilla plantilla = new Plantilla();
        plantilla.setNombre("");
        plantilla.setSala(sala);
        plantillasDAO.add(plantilla);

        Localizacion localizacionDTO = new Localizacion();
        localizacionDTO.setNombreVa("");
        localizacionDTO.setNombreEs("");
        localizacionDTO.setCodigo("CODIGO");
        localizacionDTO.setTotalEntradas(200);

        localizacionesDAO.add(localizacionDTO, Sala.salaToSalaDTO(sala));
    }

    private void insertaTarifa() {
        TarifaDTO tarifaDTO = new TarifaDTO();
        tarifaDTO.setNombre("GENERAL");
        tarifaDTO.setDefecto(false);
        tarifaDTO.setIsPublica(false);

        tarifasDAO.add(tarifaDTO);
    }

    private void insertaSala()
    {
        Cine cine = cinesDAO.addCine(new Cine("CODIGO", "nombre", "cif", "direccion", "codigoMunicipio", "nombreMunicipio", "cp", "empresa", "codigoRegistro", "telefono", BigDecimal.ONE));
        Sala sala = salasDAO.addSala(new Sala("SALA", "SALA", 10, 0, 10, "Cine", "Formato", "", cine));

        usuario = new Usuario();
        usuario.setNombre("usuario");
        usuario.setMail("usuario@test.com");
        usuario.setUsuario("usuario");

        usuario = usuariosDAO.addUser(usuario);
        usuariosDAO.addSalaUsuario(sala, usuario);

        insertaLocalizacion(sala);
    }

    private void insertaTpvDefault()
    {
        tpvsDAO.addTpvDefault();
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
    public void testSyncNuevosItemsGraduacion() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_TIPO_GRADUACION), usuario.getUsuario());

        List<Evento> eventos = eventosDAO.getEventosConSesiones(usuario.getUsuario());

        assertEquals("Número de eventos nuevos", 2, eventos.size());
    }

    @Test
    @Transactional
	@Ignore
    public void testSyncUpdateItemsGraduacion() throws Exception
    {
        syncService.sync(loadFromClasspath(RSS_TIPO_GRADUACION), usuario.getUsuario());
        List<Evento> eventos = eventosDAO.getEventosConSesiones(usuario.getUsuario());
        assertEquals("Número de eventos nuevos", 2, eventos.size());

        syncService.sync(loadFromClasspath(RSS_TIPO_GRADUACION_UPDATE), usuario.getUsuario());
        List<SesionDTO> sesiones = sesionesDAO.getSesionesPorRssId("2508948", usuario.getUsuario());

        //No cambia la fecha
        assertEquals("Fecha primera sesión", sesiones.get(0).getFechaCelebracion().toString(), "2015-10-16 19:00:00.0");

        //Sí que actualiza la hora de apertura
        assertEquals("Hora apertura", sesiones.get(0).getHoraApertura(), "18:00");
    }
}
