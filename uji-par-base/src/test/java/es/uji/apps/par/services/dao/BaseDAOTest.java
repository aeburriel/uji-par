package es.uji.apps.par.services.dao;

import es.uji.apps.par.builders.*;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Localizacion;
import es.uji.apps.par.model.TipoEvento;
import es.uji.apps.par.model.Tpv;
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
    TpvsDAO tpvsDAO;

    @Autowired
    TiposEventosDAO tiposEventosDAO;

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

        TpvsDTO tpvDefault = tpvsDAO.getTpvDefault();
        if (tpvDefault == null) {
            tpvsDAO.addTpvDefault();
            tpvDefault = tpvsDAO.getTpvDefault();
        }

        Evento evento = new Evento();
        evento.setAsientosNumerados(true);
        evento.setParTipoEvento(tipoEvento);
        evento.setParTpv(Tpv.tpvDTOToTpv(tpvDefault));
        evento = eventosDao.addEvento(evento);
        
        SesionDTO sesionDTO = new SesionDTO();
        sesionDTO.setParEvento(Evento.eventoToEventoDTO(evento));
        sesionDTO.setFechaCelebracion(new Timestamp(100));
        sesionDTO.setFechaInicioVentaOnline(new Timestamp(0));
        sesionDTO.setFechaFinVentaOnline(new Timestamp(1));
        
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
