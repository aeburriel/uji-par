package es.uji.apps.par.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.RegistroSerializaException;
import es.uji.apps.par.exceptions.SesionSinFormatoIdiomaIcaaException;
import es.uji.apps.par.exceptions.TipoEnvioInvalidoException;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.ficheros.registros.TipoIncidencia;
import es.uji.apps.par.utils.DateUtils;

@XmlRootElement
public class Sesion
{
	private static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HHmm");
	
    private long id;
    private Evento evento;
    private Date fechaCelebracion;
    private Date fechaInicioVentaOnline;
    private Date fechaFinVentaOnline;
    private Date fechaEnvioFichero;
    private Date fechaGeneracionFichero;
    private String horaApertura;
    private Boolean canalInternet;
    private Boolean canalTaquilla;
    private String horaCelebracion;
    private String horaInicioVentaOnline;
    private String horaFinVentaOnline;
    private Plantilla plantillaPrecios;
    private List<PreciosSesion> preciosSesion;
    private long butacasVendidas;
    private long butacasReservadas;
    private String nombre;
    private Sala sala;
    private String versionLinguistica;
    private String rssId;
    private String tipoEnvio;
    private Long idEnvioFichero;
    private Integer incidenciaId;
	private Boolean anulada;

    public Sesion()
    {
    }

    public Sesion(SesionDTO sesionDTO)
    {
        this.id = sesionDTO.getId();
        this.evento = Evento.eventoDTOtoEvento(sesionDTO.getParEvento());
        this.fechaCelebracion = new Date(sesionDTO.getFechaCelebracion().getTime());
        if (sesionDTO.getFechaInicioVentaOnline() != null)
            this.fechaInicioVentaOnline = new Date(sesionDTO.getFechaInicioVentaOnline().getTime());
        if (sesionDTO.getFechaFinVentaOnline() != null)
            this.fechaFinVentaOnline = new Date(sesionDTO.getFechaFinVentaOnline().getTime());
        this.horaApertura = sesionDTO.getHoraApertura();
        this.canalInternet = sesionDTO.getCanalInternet();
        this.canalTaquilla = sesionDTO.getCanalTaquilla();
        this.plantillaPrecios = Plantilla.plantillaPreciosDTOtoPlantillaPrecios(sesionDTO.getParPlantilla());
        //this.preciosSesion = PreciosSesion.listaPreciosSesionDTOToListaPreciosSesion(sesionDTO.getParPreciosSesions());

        this.horaCelebracion = DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaCelebracion());

        if (sesionDTO.getFechaInicioVentaOnline() != null)
            this.horaInicioVentaOnline = DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaInicioVentaOnline());
        if (sesionDTO.getFechaFinVentaOnline() != null)
            this.horaFinVentaOnline = DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaFinVentaOnline());
        
        this.nombre = sesionDTO.getNombre();
        this.versionLinguistica = sesionDTO.getVersionLinguistica();
        this.rssId = sesionDTO.getRssId();
        this.incidenciaId = sesionDTO.getIncidenciaId();
        
        if (sesionDTO.getParSala() != null)
            this.sala = new Sala(sesionDTO.getParSala());
		this.anulada = sesionDTO.getAnulada();
    }
    
    public Sesion(Integer id) {
		this.id = id;
	}

	public static List<Long> getIdsSesiones(List<Sesion> sesiones)
    {
        List<Long> ids = new ArrayList<Long>();
        
        for (Sesion sesion : sesiones)
            ids.add(sesion.getId());
        
        return ids ;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Evento getEvento()
    {
        return evento;
    }

    public void setEvento(Evento evento)
    {
        this.evento = evento;
    }

    public Date getFechaCelebracion()
    {
        return fechaCelebracion;
    }

    public Date getFechaInicioVentaOnline()
    {
        return fechaInicioVentaOnline;
    }

    public Date getFechaFinVentaOnline()
    {
        return fechaFinVentaOnline;
    }

    public String getHoraApertura()
    {
        return horaApertura;
    }

    public void setHoraApertura(String horaApertura)
    {
        this.horaApertura = horaApertura;
    }

    public Boolean getCanalInternet()
    {
		return (canalInternet == null)?false:canalInternet;
    }

    public void setCanalInternet(String canalInternet)
    {
        this.canalInternet = (canalInternet != null && (canalInternet.equals("1") || canalInternet.equals("true")));
    }

    public Boolean getCanalTaquilla()
    {
        return (canalTaquilla == null)?false:canalTaquilla;
    }

    public void setCanalTaquilla(String canalTaquilla)
    {
        this.canalTaquilla = (canalTaquilla != null && (canalTaquilla.equals("1") || canalTaquilla.equals("true")));
    }

    // TODO
    // definidos porque desde el ext se envía automaticamente la fecha con formato dd/mm/YYYY y no
    // serializa bien si el parametro es un Date
    public void setFechaCelebracion(String fechaCelebracion)
    {
        this.fechaCelebracion = DateUtils.spanishStringToDate(fechaCelebracion);
    }

    public void setFechaCelebracionWithDate(Date fechaCelebracion)
    {
        this.fechaCelebracion = fechaCelebracion;
    }

    public void setFechaInicioVentaOnline(String fechaInicioVentaOnline)
    {
        this.fechaInicioVentaOnline = DateUtils.spanishStringToDate(fechaInicioVentaOnline);
    }
    
    public void setFechaInicioVentaOnlineWithDate(Date fechaInicioVentaOnline)
    {
        this.fechaInicioVentaOnline = fechaInicioVentaOnline;
    }

    public void setFechaFinVentaOnline(String fechaFinVentaOnline)
    {
        this.fechaFinVentaOnline = DateUtils.spanishStringToDate(fechaFinVentaOnline);
    }
    
    public void setFechaFinVentaOnlineWithDate(Date fechaFinVentaOnline)
    {
        this.fechaFinVentaOnline = fechaFinVentaOnline;
    }

    public String getHoraCelebracion()
    {
        return horaCelebracion;
    }

    public void setHoraCelebracion(String horaCelebracion)
    {
        this.horaCelebracion = horaCelebracion;
    }

	public static Sesion SesionDTOToSesion(SesionDTO sesionDTO) {
		Sesion sesion = new Sesion();
		
		if (sesionDTO.getCanalInternet() != null)
		    sesion.setCanalInternet(sesionDTO.getCanalInternet().toString());
		
		if (sesionDTO.getCanalTaquilla() != null) 
		    sesion.setCanalTaquilla(sesionDTO.getCanalTaquilla().toString());
		
		sesion.setEvento(Evento.eventoDTOtoEvento(sesionDTO.getParEvento()));
		sesion.setFechaCelebracionWithDate(sesionDTO.getFechaCelebracion());
        if (sesionDTO.getFechaFinVentaOnline() != null)
		    sesion.setFechaFinVentaOnline(DateUtils.dateToSpanishString(sesionDTO.getFechaFinVentaOnline()));
        if (sesionDTO.getFechaInicioVentaOnline() != null)
		    sesion.setFechaInicioVentaOnline(DateUtils.dateToSpanishString(sesionDTO.getFechaInicioVentaOnline()));
		
		sesion.setHoraApertura(sesionDTO.getHoraApertura());
        if (sesionDTO.getFechaInicioVentaOnline() != null)
		    sesion.setHoraInicioVentaOnline(DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaInicioVentaOnline()));
        if (sesionDTO.getFechaFinVentaOnline() != null)
		    sesion.setHoraFinVentaOnline(DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaFinVentaOnline()));
		
		sesion.setHoraCelebracion(DateUtils.getHourAndMinutesWithLeadingZeros(sesionDTO.getFechaCelebracion()));
		sesion.setId(sesionDTO.getId());
		sesion.setPlantillaPrecios(Plantilla.plantillaPreciosDTOtoPlantillaPrecios(sesionDTO.getParPlantilla()));
		
		sesion.setNombre(sesionDTO.getNombre());
		sesion.setVersionLinguistica(sesionDTO.getVersionLinguistica());
		sesion.setRssId(sesionDTO.getRssId());
		
		sesion.setSala(new Sala(sesionDTO.getParSala()));
		sesion.setIncidenciaId(sesionDTO.getIncidenciaId());
		sesion.setAnulada(sesionDTO.getAnulada());
		
		return sesion;
	}
	
	public static Sesion SesionDTOToSesionSinEvento(SesionDTO sesionDTO) {
	    Sesion sesion = SesionDTOToSesion(sesionDTO);
	    sesion.setEvento(null);
	    
	    return sesion;
	}
	
	public static SesionDTO SesionToSesionDTO(Sesion sesion) {
		SesionDTO sesionDTO = new SesionDTO();
		sesionDTO.setCanalInternet(sesion.getCanalInternet());
        sesionDTO.setCanalTaquilla(true);
		sesionDTO.setParEvento(Evento.eventoToEventoDTO(sesion.getEvento()));
		sesionDTO.setFechaCelebracion(DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion.getFechaCelebracion(), sesion.getHoraCelebracion())));

        if (sesion.getCanalInternet()) {
            sesionDTO.setFechaFinVentaOnline(DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion.getFechaFinVentaOnline(), sesion.getHoraFinVentaOnline())));
            sesionDTO.setFechaInicioVentaOnline(DateUtils.dateToTimestampSafe(DateUtils.addTimeToDate(sesion.getFechaInicioVentaOnline(), sesion.getHoraInicioVentaOnline())));
        } else {
            sesionDTO.setFechaFinVentaOnline(null);
            sesionDTO.setFechaInicioVentaOnline(null);
        }
		
		sesionDTO.setHoraApertura(sesion.getHoraApertura());
		sesionDTO.setId(sesion.getId());
		sesionDTO.setParPlantilla(Plantilla.plantillaPreciosToPlantillaPreciosDTO(sesion.getPlantillaPrecios()));
		
		if (sesion.getPreciosSesion() != null) {
			for (PreciosSesion preciosSesion: sesion.getPreciosSesion()) {
				sesionDTO.addParPreciosSesion(PreciosSesion.precioSesionToPrecioSesionDTO(preciosSesion));
			}
		}
		
		if (sesion.getSala() != null && sesion.getSala().getId()!=0)
		    sesionDTO.setParSala(Sala.salaToSalaDTO(sesion.getSala()));
		
		sesionDTO.setNombre(sesion.getNombre());
		sesionDTO.setVersionLinguistica(sesion.getVersionLinguistica());
		sesionDTO.setRssId(sesion.getRssId());
		sesionDTO.setIncidenciaId((sesion.getIncidenciaId()==null)?0:sesion.getIncidenciaId());
		sesionDTO.setAnulada(sesion.getAnulada());

		return sesionDTO;
	}
	
	synchronized public static List<SesionDTO> sesionsToSesionsDTO(List<Sesion> sesiones)
	{
	    List<SesionDTO> sesionesDTO = new ArrayList<SesionDTO>();
	    
	    for (Sesion sesion: sesiones)
        {
            sesionesDTO.add(Sesion.SesionToSesionDTO(sesion));
        }
	    
        return sesionesDTO;
	}
	
    synchronized public static List<Sesion> sesionsDTOToSesions(List<SesionDTO> sesionesDTO)
    {
        List<Sesion> sesiones = new ArrayList<Sesion>();
        
        for (SesionDTO sesionDTO: sesionesDTO)
        {
            sesiones.add(Sesion.SesionDTOToSesion(sesionDTO));
        }
        
        return sesiones;
    }
	
	public Plantilla getPlantillaPrecios() {
		return plantillaPrecios;
	}

	public void setPlantillaPrecios(Plantilla plantillaPrecios) {
		this.plantillaPrecios = plantillaPrecios;
	}

	public String getHoraInicioVentaOnline() {
		return horaInicioVentaOnline;
	}

	public void setHoraInicioVentaOnline(String horaInicioVentaOnline) {
		this.horaInicioVentaOnline = horaInicioVentaOnline;
	}

	public String getHoraFinVentaOnline() {
		return horaFinVentaOnline;
	}

	public void setHoraFinVentaOnline(String horaFinVentaOnline) {
		this.horaFinVentaOnline = horaFinVentaOnline;
	}

	public List<PreciosSesion> getPreciosSesion() {
		return preciosSesion;
	}

	@JsonIgnore
	public void setPreciosSesion(List<PreciosSesion> preciosSesion) {
		this.preciosSesion = preciosSesion;
	}
	
	@JsonProperty("preciosSesion")
	public void setPreciosSesionFromString(String preciosSesion) {
		Gson gson = new Gson();
		List<PreciosSesion> lista = gson.fromJson(preciosSesion, new TypeToken<List<PreciosSesion>>(){}.getType());
		this.preciosSesion = lista;
	}
	
	@JsonIgnore
	public boolean getEnPlazoVentaInternet()
    {
		if (fechaInicioVentaOnline == null || fechaFinVentaOnline == null)
			return false;

		Date ahora = new Date();
        return ahora.before(fechaFinVentaOnline) && ahora.after(fechaInicioVentaOnline);
    }

    public long getButacasVendidas()
    {
        return butacasVendidas;
    }

    public void setButacasVendidas(long butacasVendidas)
    {
        this.butacasVendidas = butacasVendidas;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public Sala getSala()
    {
        return sala;
    }

    public void setSala(Sala sala)
    {
        this.sala = sala;
    }

    public String getVersionLinguistica()
    {
        return versionLinguistica;
    }

    public void setVersionLinguistica(String versionLinguistica)
    {
        this.versionLinguistica = versionLinguistica;
    }

    public String getRssId()
    {
        return rssId;
    }

    public void setRssId(String rssId)
    {
        this.rssId = rssId;
    }

	public Date getFechaEnvioFichero() {
		return fechaEnvioFichero;
	}

	public void setFechaEnvioFichero(Date fechaEnvioFichero) {
		this.fechaEnvioFichero = fechaEnvioFichero;
	}

	public Date getFechaGeneracionFichero() {
		return fechaGeneracionFichero;
	}

	public void setFechaGeneracionFichero(Date fechaGeneracionFichero) {
		this.fechaGeneracionFichero = fechaGeneracionFichero;
	}

	public String getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(String tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
	}

	public Long getIdEnvioFichero() {
		return idEnvioFichero;
	}

	public void setIdEnvioFichero(Long idEnvioFichero) {
		this.idEnvioFichero = idEnvioFichero;
	}

	public Integer getIncidenciaId() {
		return incidenciaId;
	}

	public void setIncidenciaId(Integer incidenciaId) {
		this.incidenciaId = incidenciaId;
	}

	public long getButacasReservadas() {
		return butacasReservadas;
	}

	public void setButacasReservadas(long butacasReservadas) {
		this.butacasReservadas = butacasReservadas;
	}
	
	public static void checkFechaCelebracion(String fechaCelebracion) throws RegistroSerializaException {
		if (fechaCelebracion == null)
            throw new RegistroSerializaException(GeneralPARException.FECHA_SESION_PROGRAMADA_NULA_CODE);
	}
	
	public static void checkTipoEnvio(String tipoEnvio) throws TipoEnvioInvalidoException {
		if (tipoEnvio == null || (!tipoEnvio.equals("FL") && !tipoEnvio.equals("AT")))
			throw new TipoEnvioInvalidoException();
	}

	public static void checkFechaCelebracion(Date fecha) throws RegistroSerializaException {
		if (fecha == null)
            throw new RegistroSerializaException(GeneralPARException.FECHA_SESION_PROGRAMADA_NULA_CODE);
	}

	public static void checkHoraCelebracion(String hora) throws RegistroSerializaException {
		if (hora == null)
            throw new RegistroSerializaException(GeneralPARException.HORA_SESION_PROGRAMADA_NULA_CODE);
		
		if (hora.length() != 4)
            throw new RegistroSerializaException(GeneralPARException.FORMATO_HORA_INCORRECTO_CODE);
	}

	public static void checkIncidencia(TipoIncidencia incidencia) throws RegistroSerializaException {
		if (incidencia == null)
            throw new RegistroSerializaException(GeneralPARException.INCIDENCIAS_NULAS_CODE);
	}
	
	public static void checkIncidencia(Integer incidenciaId) throws RegistroSerializaException {
		if (incidenciaId == null)
            throw new RegistroSerializaException(GeneralPARException.INCIDENCIAS_NULAS_CODE);
	}

	public static void checkRecaudacion(BigDecimal recaudacion) throws RegistroSerializaException {
		if (recaudacion == null)
            throw new RegistroSerializaException(GeneralPARException.RECAUDACION_NULA_CODE);
	}
	
	//TODO deberíamos comprobar la recaudacion
	synchronized public static void checkSesion(Date fechaCelebracion, String tipoEnvio, Integer incidenciaId)
			throws RegistroSerializaException {
		Sesion.checkFechaCelebracion(fechaCelebracion);
		Sesion.checkTipoEnvio(tipoEnvio);
		Sesion.checkHoraCelebracion(HOUR_FORMAT.format(fechaCelebracion));
		Sesion.checkIncidencia(incidenciaId);
		//Sesion.checkRecaudacion(recaudacion);
	}

	public static void checkSesionValoresIcaa(String formato, long eventoId, String versionLinguistica) {
		if (formato == null || formato.equals(""))
			throw new SesionSinFormatoIdiomaIcaaException(eventoId, formato, versionLinguistica);

		if (versionLinguistica == null || versionLinguistica.equals(""))
			throw new SesionSinFormatoIdiomaIcaaException(eventoId, formato, versionLinguistica);

		//TODO -se podria mejorar mirando en el caso que sea multisesion, si existe version linguistica en la tabla adjunta
		//en teoria se valida en cliente
	}

	public Boolean getAnulada() {
		return (anulada == null)?false:anulada;
	}

	public void setAnulada(Boolean anulada) {
		this.anulada = anulada;
	}
}