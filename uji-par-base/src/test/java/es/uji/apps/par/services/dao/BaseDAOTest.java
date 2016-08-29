package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.*;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

public class BaseDAOTest
{
    @Autowired
    EventosDAO eventosDao;
    
    @Autowired
    SesionesDAO sesionesDao;

    @Autowired
    LocalizacionesDAO localizacionesDao;

    @Autowired
    TarifasDAO tarifasDAO;

    @Autowired
    private CinesDAO cinesDAO;

    @Autowired
    private SalasDAO salasDao;

    @Autowired
    TpvsDAO tpvsDAO;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

    @Autowired
    private UsuariosDAO usuariosDAO;

    @PersistenceContext
    protected EntityManager entityManager;

    UsuarioDTO usuario1;

    @Before
    public void setUp()
    {
        usuario1 = new UsuarioBuilder("User 1", "user1@test.com", "user1")
                .build(entityManager);

        entityManager.flush();
        entityManager.clear();
    }

    protected SesionDTO preparaSesion(LocalizacionDTO localizacion)
    {
        TipoEvento tipoEvento = new TipoEvento();
        tipoEvento.setNombreEs("Tipo Es");
        tipoEvento.setNombreVa("Tipo Va");

        tiposEventosDAO.addTipoEvento(tipoEvento);

        Cine cine = new Cine();
        cine.setNombre("Cine");
        cinesDAO.addCine(cine);

        Tpv tpv = new Tpv();
        if (tpvsDAO.getTpvDefault(cine.getId()) == null) {
            tpv = addTpvDefault(cine);
        }

        Evento evento = new Evento();
        evento.setAsientosNumerados(true);
        evento.setParTipoEvento(tipoEvento);
        evento.setParTpv(tpv);
        evento.setCine(cine);
        evento = eventosDao.addEvento(evento);

        Sala sala = new Sala();
        sala.setCodigo("567");
        sala.setNombre("Sala 1");
        salasDao.addSala(sala);
        usuariosDAO.addSalaUsuario(sala, new Usuario(usuario1));
        
        SesionDTO sesionDTO = new SesionDTO();
        sesionDTO.setParEvento(Evento.eventoToEventoDTO(evento));
        sesionDTO.setFechaCelebracion(new Timestamp(100));
        sesionDTO.setFechaInicioVentaOnline(new Timestamp(0));
        sesionDTO.setFechaFinVentaOnline(new Timestamp(1));
        sesionDTO.setParSala(Sala.salaToSalaDTO(sala));
        
        SesionDTO sesion = sesionesDao.persistSesion(sesionDTO);

        PreciosSesionDTO precioSesion = new PreciosSesionDTO(BigDecimal.valueOf(1), BigDecimal.valueOf(5),
                BigDecimal.valueOf(10), localizacion);

        TarifaDTO tarifaDTO = new TarifaDTO();
        tarifaDTO.setNombre("tarifa1");
        tarifaDTO.setIsPublica(false);
        tarifasDAO.persistTarifa(tarifaDTO);

        precioSesion.setParTarifa(tarifaDTO);
        sesion.setParPreciosSesions(Arrays.asList(precioSesion));

        return sesion;
    }

    private Tpv addTpvDefault(Cine cine) {
        Tpv tpv = new Tpv();
        tpv.setNombre("TPV Prueba");
        TpvsDTO tpvDefecto = tpvsDAO.getTpvDefault(cine.getId());
        if (tpvDefecto == null)
            tpvsDAO.addTpv(tpv, cine.getId());

        TpvsDTO tpvDefectoInsertado = tpvsDAO.getTpvDefault(cine.getId());
        tpv.setId(tpvDefectoInsertado.getId());

        return tpv;
    }

    protected Localizacion preparaLocalizacion(String codigoLocalizacion)
    {
        Localizacion localizacion = new Localizacion();
        localizacion.setCodigo(codigoLocalizacion);
        localizacion.setTotalEntradas(10);
        
        return localizacionesDao.add(localizacion);
    }
    
    protected void setSesionNoNumerada(SesionDTO sesion)
    {
        EventoDTO evento = sesion.getParEvento();
        evento.setAsientosNumerados(false);
        
        eventosDao.updateEvento(Evento.eventoDTOtoEvento(evento), usuario1.getUsuario());
    }

    protected ButacaDTO preparaButaca(SesionDTO sesion, LocalizacionDTO localizacion, String fila, String numero,
            BigDecimal precio)
    {
        ButacaDTO butacaDTO = new ButacaDTO();

        butacaDTO.setAnulada(false);
        butacaDTO.setFila(fila);
        butacaDTO.setNumero(numero);
        butacaDTO.setPrecio(precio);
        butacaDTO.setParSesion(sesion);
        butacaDTO.setParLocalizacion(localizacion);

        return butacaDTO;
    }

}
