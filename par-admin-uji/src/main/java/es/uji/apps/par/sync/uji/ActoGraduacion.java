package es.uji.apps.par.sync.uji;

import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.*;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.sync.rss.jaxb.Item;
import es.uji.apps.par.sync.utils.SyncUtils;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class ActoGraduacion implements EventosTipoSync {
    private static final Logger log = LoggerFactory.getLogger(ActoGraduacion.class);

    @Autowired
    private TiposEventosDAO tiposEventosDAO;

    @Autowired
    private TpvsDAO tpvsDAO;

    @Autowired
    private SesionesDAO sesionesDAO;

    @Autowired
    private EventosDAO eventosDAO;

    @Autowired
    private SalasDAO salasDAO;

    @Autowired
    private LocalizacionesDAO localizacionesDAO;

    @Autowired
    private TarifasDAO tarifasDAO;

    @Autowired
    private ComprasDAO comprasDAO;

    @Autowired
    private PlantillasDAO plantillasDAO;

    @Autowired
    private UsersService usersService;

    @Context
    HttpServletRequest currentRequest;

    @Override
    public void createNewTipoEvento(Item item, CineDTO cineDTO, String userUID) throws MalformedURLException {
        log.info(String.format("RSS insertando nuevo evento: %s - \"%s\"", item.getContenidoId(), item.getTitle()));

        EventoDTO evento = new EventoDTO();
        evento.setParTpv(tpvsDAO.getTpvDefault(cineDTO.getId()));
        evento.setAsientosNumerados(false);
        evento.setRssId(item.getContenidoId());
        evento.setParCine(cineDTO);

        updateTipoEvento(evento, item, userUID);
    }

    @Override
    public void updateTipoEvento(EventoDTO evento, Item item, String userUID) throws MalformedURLException {
        Cine cine = usersService.getUserCineByUserUID(userUID);

        log.info(String.format("RSS actualizando evento de tipo %s existente: %s - \"%s\"", item.getTipo(), evento.getRssId(),
                evento.getTituloVa()));

        if (item.getEnclosures() != null && item.getEnclosures().size() > 0) {
            String urlImagen = item.getEnclosures().get(0).getUrl();

            byte[] imagen = SyncUtils.getImageFromUrl(urlImagen);
            if (imagen != null)
                evento.setImagen(imagen);

            evento.setImagenSrc(urlImagen);
            evento.setImagenContentType(item.getEnclosures().get(0).getType());
        }

        evento.setTituloVa(item.getTitulo());
        evento.setCaracteristicasVa(item.getResumen());
        evento.setDuracionVa(item.getDuracio());
        evento.setDescripcionVa(item.getContenido());

        String tipo = Utils.toUppercaseFirst(item.getTipo().trim());
        TipoEventoDTO tipoEvento = tiposEventosDAO.getTipoEventoByNombreEs(tipo, userUID);
        if (tipoEvento == null) {
            tipoEvento = TipoEventoDTO.fromTipoEvento(tiposEventosDAO.addTipoEvento(new TipoEvento(tipo, tipo, false, cine)));
        }
        evento.setParTiposEvento(tipoEvento);

        // Para que no de error en BD
        if (evento.getTituloEs() == null) {
            evento.setTituloEs(evento.getTituloVa());
        }

        // Para que no de error en BD
        if (evento.getTituloVa() == null) {
            evento.setTituloVa(evento.getTituloEs());
        }

        if (evento.getParTiposEvento() != null) {
            List<Sala> salas = salasDAO.getSalas(userUID);
            List<TarifaDTO> tarifas = tarifasDAO.getAll(null, 0, Integer.MAX_VALUE, userUID);

            if (salas.size() > 0 && tarifas.size() > 0)
            {
                evento = eventosDAO.updateEventoDTO(evento);
                List<SesionDTO> parSesiones = sesionesDAO.getSesiones(evento.getId(), null, 0, Integer.MAX_VALUE, userUID);
                Sesion sesion = null;
                if (parSesiones != null && parSesiones.size() > 0)
                {
                    sesion = Sesion.SesionDTOToSesion(parSesiones.get(0));
                    sesion.setFechaCelebracionWithDate(item.getDate());
                    sesion.setHoraCelebracion(DateUtils.getHourAndMinutesWithLeadingZeros(item.getDate()));
                    sesion.setHoraApertura(item.getApertura());

                    List<CompraDTO> comprasOfSesion = comprasDAO.getComprasOfSesion(sesion.getId());
                    sesionesDAO.updateSesion(sesion, comprasOfSesion.size() > 0, userUID);
                }
                else {
                    sesion = new Sesion();
                    sesion.setEvento(Evento.eventoDTOtoEvento(evento));
                    sesion.setCanalInternet("0");
                    sesion.setSala(salas.get(0));
                    sesion.setCanalTaquilla("1");
                    sesion.setFechaCelebracionWithDate(item.getDate());
                    sesion.setHoraCelebracion(DateUtils.getHourAndMinutesWithLeadingZeros(item.getDate()));
                    sesion.setHoraApertura(item.getApertura());

                    List<PreciosSesion> preciosSesion = new ArrayList<PreciosSesion>();
                    List<LocalizacionDTO> localizacionDTOs = localizacionesDAO.get(null, 0, Integer.MAX_VALUE, (int) salas.get(0).getId());
                    for (LocalizacionDTO localizacionDTO : localizacionDTOs) {
                        PreciosSesion precioSesion = new PreciosSesion();
                        precioSesion.setLocalizacion(Localizacion.localizacionDTOtoLocalizacion(localizacionDTO));
                        precioSesion.setPrecio(BigDecimal.ZERO);
                        precioSesion.setInvitacion(BigDecimal.ZERO);
                        precioSesion.setTarifa(Tarifa.tarifaDTOToTarifa(tarifas.get(0)));
                        preciosSesion.add(precioSesion);
                    }

                    sesion.setPreciosSesion(preciosSesion);
                    List<PlantillaDTO> plantillas = plantillasDAO.get(false, "", 0, 100, userUID);
                    Plantilla plantilla = Plantilla.plantillaPreciosDTOtoPlantillaPrecios(plantillas.get(0));
                    sesion.setPlantillaPrecios(plantilla);

                    sesionesDAO.addSesion(sesion, userUID);
                }
            }
        }
    }
}
