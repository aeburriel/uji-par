package es.uji.apps.par.services;

import com.mysema.query.Tuple;
import es.uji.apps.par.dao.*;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.PreciosSesionDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.exceptions.EventoConCompras;
import es.uji.apps.par.exceptions.FechasInvalidasException;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.model.*;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SesionesService
{
    @Autowired
    private SesionesDAO sesionDAO;
    
    @Autowired
    private EventosDAO eventosDAO;

    @Autowired
    private ComprasDAO comprasDAO;

    @Autowired
    private SesionesAbonosDAO sesionAbonoDAO;
    
    @Autowired
    private LocalizacionesDAO localizacionesDAO;
    
    @Autowired
    private PreciosPlantillaService preciosPlantillaService;

    public List<SesionAbono> getSesionesAbono(Long abonoId, String sortParameter, int start, int limit)
    {
        List<SesionAbono> sesionesAbonos = sesionAbonoDAO.getSesiones(abonoId, sortParameter, start, limit);

        for (SesionAbono sesionAbono : sesionesAbonos)
        {
            Sesion sesion = sesionAbono.getSesion();
            sesion.setFechaCelebracionWithDate(new Date(sesion.getFechaCelebracion().getTime()));
            if (sesion.getFechaInicioVentaOnline() != null)
                sesion.setFechaInicioVentaOnlineWithDate(new Date(sesion.getFechaInicioVentaOnline().getTime()));
            if (sesion.getFechaFinVentaOnline() != null)
                sesion.setFechaFinVentaOnlineWithDate(new Date(sesion.getFechaFinVentaOnline().getTime()));
        }

        return sesionesAbonos;
    }
    
    public List<Sesion> getSesiones(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
    	return getSesiones(eventoId, false, sortParameter, start, limit, userUID);
    }
    
    private List<Sesion> getSesionesConVendidas(Long eventoId, boolean activas, String sortParameter, int start, int limit, String userUID)
    {
        List<Sesion> listaSesiones = new ArrayList<>();
        
        List<Tuple> sesiones;
		if (activas)
		{
			sesiones = sesionDAO.getSesionesActivasConButacasVendidas(eventoId, sortParameter, start, limit, userUID);
		}
		else
		{
			sesiones = sesionDAO.getSesionesConButacasVendidas(eventoId, sortParameter, start, limit, userUID);
		}
        
        for (Tuple fila: sesiones) {
            
            SesionDTO sesionDTO = fila.get(0, SesionDTO.class);
            Long butacasVendidas = fila.get(1, Long.class);
            Long butacasReservadas = fila.get(2, Long.class);
            
            Sesion sesion = new Sesion(sesionDTO);
            sesion.setButacasVendidas(butacasVendidas);
            sesion.setButacasReservadas(butacasReservadas);
            
            listaSesiones.add(sesion);
        }
        
        return listaSesiones;
    }

    public List<Sesion> getSesionesPorSala(Long eventoId, Long salaId, String sortParameter, String userUID) {

        List<Sesion> listaSesiones = new ArrayList<Sesion>();
        List<SesionDTO> sesiones = sesionDAO.getSesionesPorSala(eventoId, salaId, sortParameter, userUID);

        for (SesionDTO sesionDB: sesiones) {
            listaSesiones.add(new Sesion(sesionDB));
        }
        return listaSesiones;
    }
    
    public List<Sesion> getSesiones(Long eventoId, String userUID) {
		return getSesiones(eventoId, false, "", 0, 100, userUID);
	}
    
    public List<Sesion> getSesionesActivas(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
        return getSesiones(eventoId, true, sortParameter, start, limit, userUID);
    }
    
    private List<Sesion> getSesiones(Long eventoId, boolean activos, String sortParameter, int start, int limit, String userUID)
    {
        List<Sesion> listaSesiones = new ArrayList<Sesion>();
        
        List<SesionDTO> sesiones;
        
        if (activos)
            sesiones = sesionDAO.getSesionesActivas(eventoId, sortParameter, start, limit, userUID);
        else
            sesiones = sesionDAO.getSesiones(eventoId, sortParameter, start, limit, userUID);
        
        for (SesionDTO sesionDB: sesiones) {
            listaSesiones.add(new Sesion(sesionDB));
        }
        return listaSesiones;
    }

    public List<Sesion> getSesionesPorRssId(String rssId, String userUID)
    {
        List<Sesion> listaSesiones = new ArrayList<Sesion>();
        List<SesionDTO> sesiones = sesionDAO.getSesionesPorRssId(rssId, userUID);

        for (SesionDTO sesionDB: sesiones) {
            listaSesiones.add(new Sesion(sesionDB));
        }
        return listaSesiones;
    }
    
    // Para el Ext que espera recibir segundos en vez de milisegundos
    public List<Sesion> getSesionesDateEnSegundos(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
      return getSesionesDateEnSegundos(eventoId, false, sortParameter, start, limit, userUID);
    }
    
    // Para el Ext que espera recibir segundos en vez de milisegundos
    public List<Sesion> getSesionesActivasDateEnSegundos(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
    	return getSesionesDateEnSegundos(eventoId, true, sortParameter, start, limit, userUID);
    }    
    
    public List<Sesion> getSesionesDateEnSegundos(Long eventoId, boolean activos, String sortParameter, int start, int limit, String userUID)
    {
        List<Sesion> sesiones;
        
        if (activos)
            sesiones = getSesionesActivas(eventoId, sortParameter, start, limit, userUID);
        else
            sesiones = getSesiones(eventoId, sortParameter, start, limit, userUID);
        
        for (Sesion sesion : sesiones)
        {
            sesion.setFechaCelebracionWithDate(new Date(sesion.getFechaCelebracion().getTime()/1000));
            if (sesion.getFechaInicioVentaOnline() != null)
                sesion.setFechaInicioVentaOnlineWithDate(new Date(sesion.getFechaInicioVentaOnline().getTime()/1000));
            if (sesion.getFechaFinVentaOnline() != null)
                sesion.setFechaFinVentaOnlineWithDate(new Date(sesion.getFechaFinVentaOnline().getTime()/1000));
        }
        
        return sesiones;
    }    
    
    public List<Sesion> getSesionesConVendidasDateEnSegundos(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
        return getSesionesConVendidasDateEnSegundos(eventoId, false, sortParameter, start, limit, userUID);
    } 
    
    public List<Sesion> getSesionesActivasConVendidasDateEnSegundos(Long eventoId, String sortParameter, int start, int limit, String userUID)
    {
        return getSesionesConVendidasDateEnSegundos(eventoId, true, sortParameter, start, limit, userUID);
    }
    
    public List<Sesion> getSesionesConVendidasDateEnSegundos(Long eventoId, boolean activas, String sortParameter, int start, int limit, String userUID)
    {
        List<Sesion> sesiones = getSesionesConVendidas(eventoId, activas, sortParameter, start, limit, userUID);
        
        for (Sesion sesion : sesiones)
        {
            sesion.setFechaCelebracionWithDate(new Date(sesion.getFechaCelebracion().getTime()/1000));
            if (sesion.getFechaInicioVentaOnline() != null)
                sesion.setFechaInicioVentaOnlineWithDate(new Date(sesion.getFechaInicioVentaOnline().getTime()/1000));
            if (sesion.getFechaFinVentaOnline() != null)
                sesion.setFechaFinVentaOnlineWithDate(new Date(sesion.getFechaFinVentaOnline().getTime()/1000));
        }
        
        return sesiones;
    }  

    public void removeSesion(Integer id)
    {
        if (hasSesionCompras(id))
        {
            throw new EventoConCompras(id);
        }
        else {
            sesionDAO.removeSesion(id);
        }
    }

    private boolean hasSesionCompras(Integer sesionId) {
        return comprasDAO.getComprasOfSesion(sesionId).size() > 0;
    }

    public void removeSesionAbono(Long id)
    {
        sesionAbonoDAO.removeSesionAbono(id);
    }

    @Transactional(rollbackForClassName={"CampoRequeridoException","FechasInvalidasException", "IncidenciaNotFoundException"})
    public Sesion addSesion(long eventoId, Sesion sesion, String userUID) throws CampoRequeridoException, FechasInvalidasException, IncidenciaNotFoundException {
    	checkSesionAndDates(sesion);

    	sesion.setEvento(Evento.eventoDTOtoEvento(eventosDAO.getEventoById(eventoId, userUID)));
    	List<PreciosSesion> listaPreciosSesion = new ArrayList<PreciosSesion>();
    	if (sesion.getPreciosSesion() !=  null) {
        	for (PreciosSesion preciosSesion: sesion.getPreciosSesion()) {
        		preciosSesion.setLocalizacion(Localizacion.localizacionDTOtoLocalizacion(localizacionesDAO.getLocalizacionById(preciosSesion.getLocalizacion().getId())));
        		preciosSesion.setSesion(sesion);
        		listaPreciosSesion.add(preciosSesion);
        	}
        	sesion.setPreciosSesion(listaPreciosSesion);
        }
    	
    	sesionDAO.addSesion(sesion, userUID);
        return sesion;
    }

    private void addPreciosSesion(SesionDTO sesionDTO) {
		if (sesionDTO.getParPreciosSesions() != null) {
			for (PreciosSesionDTO precioSesionDTO : sesionDTO.getParPreciosSesions()) {
    			precioSesionDTO.setParSesione(sesionDTO);
    			sesionDAO.addPrecioSesion(precioSesionDTO);
    		}
    	}
	}
    
    private Evento createParEventoWithId(long eventoId)
    {
        Evento evento = new Evento();
        evento.setId(eventoId);
        return evento;
    }

	private void checkSesionAndDates(Sesion sesion) throws CampoRequeridoException, FechasInvalidasException {
		checkRequiredFields(sesion);

        Date fechaCelebracion = DateUtils.addTimeToDate(sesion.getFechaCelebracion(), sesion.getHoraCelebracion());

    	if (sesion.getCanalInternet()) {
            Date fechaInicioVentaOnline = DateUtils.addTimeToDate(sesion.getFechaInicioVentaOnline(),
                    sesion.getHoraInicioVentaOnline());
            Date fechaFinVentaOnline = DateUtils.addTimeToDate(sesion.getFechaFinVentaOnline(),
                    sesion.getHoraFinVentaOnline());
        
        	checkIfDatesAreValid(fechaInicioVentaOnline, fechaFinVentaOnline, fechaCelebracion);
    	}
	}
    
	@Transactional(rollbackForClassName={"CampoRequeridoException","FechasInvalidasException",
            "IncidenciaNotFoundException"})
    public void updateSesion(long eventoId, Sesion sesion, String userUID) throws CampoRequeridoException, FechasInvalidasException, IncidenciaNotFoundException {
		checkSesionAndDates(sesion);
		sesion.setEvento(createParEventoWithId(eventoId));

        sesionDAO.deleteExistingPreciosSesion(sesion.getId());
        List<CompraDTO> comprasOfSesion = comprasDAO.getComprasOfSesion(sesion.getId());
        boolean hasCompras = comprasOfSesion != null ? comprasOfSesion.size() > 0 : false;
        sesionDAO.updateSesion(sesion, hasCompras, userUID);
        addPreciosSesion(Sesion.SesionToSesionDTO(sesion));
    }

    private void checkIfDatesAreValid(Date fechaInicio, Date fechaFin, Date fechaCelebracion) throws
            FechasInvalidasException {
		if (DateUtils.millisecondsToSeconds(fechaFin.getTime()) <
				DateUtils.millisecondsToSeconds(fechaInicio.getTime()))
			throw new FechasInvalidasException(FechasInvalidasException.FECHA_INICIO_VENTA_POSTERIOR_FECHA_FIN_VENTA, 
			        fechaInicio, fechaFin);
		
		if (DateUtils.millisecondsToSeconds(fechaFin.getTime()) >
				DateUtils.millisecondsToSeconds(fechaCelebracion.getTime()))
			throw new FechasInvalidasException(FechasInvalidasException.FECHA_FIN_VENTA_POSTERIOR_FECHA_CELEBRACION,
			        fechaCelebracion, fechaFin);
	}

	private void checkRequiredFields(Sesion sesion) throws CampoRequeridoException {
		if (sesion.getFechaCelebracion() == null)
			throw new CampoRequeridoException("Fecha de celebración");
		if (sesion.getHoraCelebracion() == null)
			throw new CampoRequeridoException("Hora de celebración");
		
		if (sesion.getCanalInternet()) {
			if (sesion.getFechaInicioVentaOnline() == null)
				throw new CampoRequeridoException("Fecha de inicio de la venta online");
			if (sesion.getFechaFinVentaOnline() == null)
				throw new CampoRequeridoException("Fecha de fin de la venta online");
			if (sesion.getHoraInicioVentaOnline() == null)
				throw new CampoRequeridoException("Hora de inicio de la venta online");
			if (sesion.getHoraFinVentaOnline() == null)
				throw new CampoRequeridoException("Hora de fin de la venta online");
		}
	}
	
	private boolean mostrarTarifa(TarifaDTO tarifa, boolean mostrarTarifasInternas) {
		if ((tarifa.getIsPublica() == null && !mostrarTarifasInternas) || 
    		(tarifa.getIsPublica() != null && !tarifa.getIsPublica() && !mostrarTarifasInternas))
    		return false;
		else
			return true;
	}

	public List<PreciosSesion> getPreciosSesion(Long sesionId, String sortParameter, int start, int limit, boolean mostrarTarifasInternas, String userUID) {
		List<PreciosSesion> listaPreciosSesion = new ArrayList<PreciosSesion>();
    	
		Sesion sesion = getSesion(sesionId, userUID);
		
		if (sesion.getPlantillaPrecios().getId() == -1)
		{
        	for (PreciosSesionDTO precioSesionDB: sesionDAO.getPreciosSesion(sesionId, sortParameter, start, limit)) {
        		if (mostrarTarifa(precioSesionDB.getParTarifa(), mostrarTarifasInternas))
        			listaPreciosSesion.add(new PreciosSesion(precioSesionDB));
        	}
		}
		else
		{
		    List<PreciosPlantilla> preciosPlantilla = preciosPlantillaService.getPreciosOfPlantilla(sesion.getPlantillaPrecios().getId(), sortParameter, start, limit);
            
            for(PreciosPlantilla precioPlantilla: preciosPlantilla) {
            	if (!precioPlantilla.getTarifa().getIsPublico().equals("on") && !mostrarTarifasInternas)
        			continue;
            	
                listaPreciosSesion.add(new PreciosSesion(precioPlantilla));
            }
		}
		
        return listaPreciosSesion;
	}
	
	public List<PreciosSesion> getPreciosSesion(Long sesionId, String userUID) {
		return getPreciosSesion(sesionId, "", 0, 100, true, userUID);
	}
	
	public List<PreciosSesion> getPreciosSesionPublicos(Long sesionId, String userUID) {
		return getPreciosSesion(sesionId, "", 0, 100, false, userUID);
	}
	
	
	
	private Map<String, Map<Long, PreciosSesion>> getPreciosSesionPorLocalizacion(Long sesionId, boolean mostrarTarifasInternas, String userUID)
	{
	    Map<String, Map<Long, PreciosSesion>> resultado = new HashMap<String, Map<Long, PreciosSesion>>();
	    
	    List<PreciosSesion> preciosSesion = getPreciosSesion(sesionId, userUID);
	    for (Localizacion localizacion : localizacionesPorSesion(preciosSesion))
	    {
	    	Map<Long, PreciosSesion> tarifasPrecios = new HashMap<Long, PreciosSesion>();
	    	for (PreciosSesion precio: preciosSesion) {
	    		if (precio.getLocalizacion().getCodigo().equals(localizacion.getCodigo())) {
	    			if (!precio.getTarifa().getIsPublico().equals("on") && !mostrarTarifasInternas)
	    				continue;
	    			
	    			tarifasPrecios.put(precio.getTarifa().getId(), precio);
	    		}
	    	}
	    	resultado.put(localizacion.getCodigo(), tarifasPrecios);
	    }
	    
        return resultado;
	}
	
	public Map<String, Map<Long, PreciosSesion>> getPreciosSesionPublicosPorLocalizacion(long sesionId, String userUID) {
		return getPreciosSesionPorLocalizacion(sesionId, false, userUID);
	}
	
	public Map<String, Map<Long, PreciosSesion>> getPreciosSesionPorLocalizacion(long sesionId, String userUID) {
		return getPreciosSesionPorLocalizacion(sesionId, true, userUID);
	}
	
	private List<Localizacion> localizacionesPorSesion(List<PreciosSesion> preciosSesion) {
		List<Localizacion> localizaciones = new ArrayList<Localizacion>();
		
		for (PreciosSesion precioSesion : preciosSesion)
		{
			Localizacion localizacion = precioSesion.getLocalizacion();
			if (!localizaciones.contains(localizacion))
					localizaciones.add(localizacion);
		}
		
		return localizaciones;
	}

	public Sesion getSesion(long id, String userUID)
	{
	    return new Sesion(sesionDAO.getSesion(id, userUID));
	}

	public int getTotalSesionesActivas(Long eventoId, String userUID) {
		return sesionDAO.getTotalSesionesActivas(eventoId, userUID);
	}

	public int getTotalSesiones(Long eventoId, String userUID) {
		return sesionDAO.getTotalSesiones(eventoId, userUID);
	}

	public int getTotalPreciosSesion(Long sesionId) {
		return sesionDAO.getTotalPreciosSesion(sesionId);
	}
	
	private List<Sesion> getSesionesPorFechas(List<SesionDTO> sesionesDTO, boolean conEnvios) {
		List<Sesion> listaSesiones = new ArrayList<Sesion>();
		
		for (SesionDTO sesionDTO: sesionesDTO) {
			Sesion sesion = Sesion.SesionDTOToSesion(sesionDTO);
			sesion.setFechaCelebracionWithDate(new Date(sesion.getFechaCelebracion().getTime()/1000));
			
			if (conEnvios) {
				if (sesionDTO.getParEnviosSesion().size() > 0) {
					sesion.setFechaGeneracionFichero(new Date(
						sesionDTO.getParEnviosSesion().get(0).getParEnvio().getFechaGeneracionFichero().getTime()/1000
					));
					
					if (sesionDTO.getParEnviosSesion().get(0).getParEnvio().getFechaEnvioFichero() != null)
						sesion.setFechaEnvioFichero(new Date(
								sesionDTO.getParEnviosSesion().get(0).getParEnvio().getFechaEnvioFichero().getTime()/1000
						));
					sesion.setTipoEnvio(sesionDTO.getParEnviosSesion().get(0).getTipoEnvio());
					sesion.setIdEnvioFichero(sesionDTO.getParEnviosSesion().get(0).getParEnvio().getId());
					sesion.setIncidenciaId(sesionDTO.getIncidenciaId());
				}
			}
			listaSesiones.add(sesion);
		}
		return listaSesiones;
	}

	public List<Sesion> getSesionesCinePorFechas(String fechaInicio, String fechaFin, String sort, String userUID) {
		Date dtInicio = DateUtils.spanishStringToDate(fechaInicio);
		Date dtFin = DateUtils.spanishStringToDate(fechaFin);
		dtFin = DateUtils.addTimeToDate(dtFin, "23:59");
		List<SesionDTO> sesionesDTO = sesionDAO.getSesionesCinePorFechas(dtInicio, dtFin, sort, userUID);
		return getSesionesPorFechas(sesionesDTO, false);
	}
	
	public List<Sesion> getSesionesICAAPorFechas(String fechaInicio, String fechaFin, String sort, String userUID) {
		Date dtInicio = DateUtils.spanishStringToDate(fechaInicio);
		Date dtFin = DateUtils.spanishStringToDate(fechaFin);
		dtFin = DateUtils.addTimeToDate(dtFin, "23:59");
		List<SesionDTO> sesionesDTO = sesionDAO.getSesionesICAAPorFechas(dtInicio, dtFin, sort, userUID);
		return getSesionesPorFechas(sesionesDTO, true);
	}
	
	public List<Sesion> getSesionesPorFechas(String fechaInicio, String fechaFin, String sort, String userUID) {
		Date dtInicio = DateUtils.spanishStringToDate(fechaInicio);
		Date dtFin = DateUtils.spanishStringToDate(fechaFin);
		dtFin = DateUtils.addTimeToDate(dtFin, "23:59");
		List<SesionDTO> sesionesDTO = sesionDAO.getSesionesPorFechas(dtInicio, dtFin, sort, userUID);
		return getSesionesPorFechas(sesionesDTO, false);
	}

	public List<Tarifa> getTarifasConPrecioSinPlantilla(long sesionId) {
		return _getTarifasConPrecioSinPlantilla(sesionId, true);
	}

	public List<Tarifa> getTarifasConPrecioConPlantilla(long sesionId) {
		return _getTarifasConPrecioConPlantilla(sesionId, true);
	}
	
	private List<Tarifa> _getTarifasConPrecioConPlantilla(long sesionId, boolean tambienInternas) {
		List<TarifaDTO> tarifasDTO = sesionDAO.getTarifasPreciosPlantilla(sesionId);
		List<Tarifa> tarifas = new ArrayList<Tarifa>();
		
		for (TarifaDTO tarifaDTO: tarifasDTO) {
			//if (tarifaDTO.getIsPublica() != null && !tarifaDTO.getIsPublica() && !tambienInternas)
			if (!mostrarTarifa(tarifaDTO, tambienInternas))
				continue;
			Tarifa tarifa = Tarifa.tarifaDTOToTarifa(tarifaDTO);
			tarifas.add(tarifa);
		}
		return tarifas;
	}
	
	private List<Tarifa> _getTarifasConPrecioSinPlantilla(long sesionId, boolean tambienInternas) {
		List<TarifaDTO> tarifasDTO = sesionDAO.getTarifasPreciosSesion(sesionId);
		List<Tarifa> tarifas = new ArrayList<Tarifa>();
		
		for (TarifaDTO tarifaDTO: tarifasDTO) {
			//if (!tarifaDTO.getIsPublica() && !tambienInternas)
			if (!mostrarTarifa(tarifaDTO, tambienInternas))
				continue;
			Tarifa tarifa = Tarifa.tarifaDTOToTarifa(tarifaDTO);
			tarifas.add(tarifa);
		}
		return tarifas;
	}

	public void setIncidencia(long sesionId, int incidenciaId) {
		sesionDAO.setIncidencia(sesionId, incidenciaId);
	}

	public List<Tarifa> getTarifasPublicasConPrecioConPlantilla(long sesionId) {
		return _getTarifasConPrecioConPlantilla(sesionId, false);
	}

	public List<Tarifa> getTarifasPublicasConPrecioSinPlantilla(long sesionId) {
		return _getTarifasConPrecioSinPlantilla(sesionId, false);
	}

    public Pair getNumeroSesionesMismaHoraYSala(Long sesionId, long salaId, Date fechaCelebracion, String userUID) {
        return sesionDAO.getCantidadSesionesMismaFechaYLocalizacion(DateUtils.dateToTimestampSafe(fechaCelebracion),
                salaId, sesionId, userUID);
    }

    public int getTotalSesionesAbono(Long abonoId) {
        return sesionAbonoDAO.getTotalSesionesAbono(abonoId);
    }
}
