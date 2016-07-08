package es.uji.apps.par.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.RegistroSerializaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class Evento
{
	private static final Logger logger = LoggerFactory.getLogger(Evento.class);
	
    private long id;
    private String tituloEs;
    private String tituloVa;
    private String descripcionEs;
    private String descripcionVa;
    private String companyiaEs;
    private String companyiaVa;
    private String interpretesEs;
    private String interpretesVa;
    private String duracionEs;
    private String duracionVa;
    private byte[] imagen;
    private String imagenSrc;
    private String imagenContentType;
    private String premiosEs;
    private String premiosVa;
    private String caracteristicasEs;
    private String caracteristicasVa;
    private String comentariosEs;
    private String comentariosVa;
    private TipoEvento parTiposEvento;
    private String promotor;
    private String nifPromotor;
    private Tpv parTpv;
    private long tipoEvento;
    private Boolean asientosNumerados;
    private BigDecimal porcentajeIVA;
    private BigDecimal ivaSGAE;
    private BigDecimal retencionSGAE;
    private Date fechaPrimeraSesion;
    private List<Sesion> sesiones;
    private String rssId;
    private Boolean multipleTpv;
    private String formato;
    private String expediente;
    private String codigoDistribuidora;
    private String nombreDistribuidora;
    private String nacionalidad;
    private String vo;
    private String metraje;  
    private String subtitulos;
	private String multisesion; //representa al checkbox
	private List<EventoMultisesion> eventosMultisesion;
    private Cine cine;

    public Evento()
    {
        sesiones = new ArrayList<Sesion>();
    }

	public Evento(Integer idEvento) {
		this.id = idEvento;
	}

	public static Evento eventoDTOtoEvento(EventoDTO eventoDTO) {
    	Evento evento = new Evento();
    	
    	evento.setId(eventoDTO.getId());
    	evento.setTituloEs(eventoDTO.getTituloEs());
    	evento.setTituloVa(eventoDTO.getTituloVa());

    	evento.setDescripcionEs(eventoDTO.getDescripcionEs());
    	evento.setDescripcionVa(eventoDTO.getDescripcionVa());

    	evento.setCompanyiaEs(eventoDTO.getCompanyiaEs());
    	evento.setCompanyiaVa(eventoDTO.getCompanyiaVa());

    	evento.setInterpretesEs(eventoDTO.getInterpretesEs());
    	evento.setInterpretesVa(eventoDTO.getInterpretesVa());

    	evento.setDuracionEs(eventoDTO.getDuracionEs());
    	evento.setDuracionVa(eventoDTO.getDuracionVa());

    	evento.setImagen(eventoDTO.getImagen());
    	evento.setImagenContentType(eventoDTO.getImagenContentType());
    	evento.setImagenSrc(eventoDTO.getImagenSrc());

    	evento.setPremiosEs(eventoDTO.getPremiosEs());
    	evento.setPremiosVa(eventoDTO.getPremiosVa());

    	evento.setCaracteristicasEs(eventoDTO.getCaracteristicasEs());
    	evento.setCaracteristicasVa(eventoDTO.getCaracteristicasVa());

    	evento.setComentariosEs(eventoDTO.getComentariosEs());
    	evento.setComentariosVa(eventoDTO.getComentariosVa());

        if (eventoDTO.getParTiposEvento() != null)
        	evento.setParTipoEvento(TipoEvento.tipoEventoDTOToTipoEvento(eventoDTO.getParTiposEvento()));

        if (eventoDTO.getParTpv() != null) {
            evento.setParTpv(new Tpv(eventoDTO.getParTpv()));
        }
        
        evento.setAsientosNumerados(eventoDTO.getAsientosNumerados());
        evento.setIvaSGAE(eventoDTO.getIvaSgae());
        evento.setRetencionSGAE(eventoDTO.getRetencionSgae());
        evento.setPorcentajeIVA(eventoDTO.getPorcentajeIva());
        
        evento.setRssId(eventoDTO.getRssId());

        evento.setFormato(eventoDTO.getFormato());
        evento.setExpediente(eventoDTO.getExpediente());
        evento.setCodigoDistribuidora(eventoDTO.getCodigoDistribuidora());
        evento.setNombreDistribuidora(eventoDTO.getNombreDistribuidora());
        evento.setNacionalidad(eventoDTO.getNacionalidad());
        evento.setVo(eventoDTO.getVo());
        evento.setMetraje(eventoDTO.getMetraje());
        evento.setSubtitulos(eventoDTO.getSubtitulos());
    	
    	return evento;
    }
    
    public static EventoDTO eventoToEventoDTO(Evento evento) {
    	EventoDTO eventoDTO = new EventoDTO();
    	
    	eventoDTO.setId(evento.getId());
    	eventoDTO.setTituloEs(evento.getTituloEs());
    	eventoDTO.setTituloVa(evento.getTituloVa());

    	eventoDTO.setDescripcionEs(evento.getDescripcionEs());
    	eventoDTO.setDescripcionVa(evento.getDescripcionVa());

    	eventoDTO.setCompanyiaEs(evento.getCompanyiaEs());
    	eventoDTO.setCompanyiaVa(evento.getCompanyiaVa());

    	eventoDTO.setInterpretesEs(evento.getInterpretesEs());
    	eventoDTO.setInterpretesVa(evento.getInterpretesVa());

    	eventoDTO.setDuracionEs(evento.getDuracionEs());
    	eventoDTO.setDuracionVa(evento.getDuracionVa());

    	eventoDTO.setImagen(evento.getImagen());
    	eventoDTO.setImagenContentType(evento.getImagenContentType());
    	eventoDTO.setImagenSrc(evento.getImagenSrc());

    	eventoDTO.setPremiosEs(evento.getPremiosEs());
    	eventoDTO.setPremiosVa(evento.getPremiosVa());

    	eventoDTO.setCaracteristicasEs(evento.getCaracteristicasEs());
    	eventoDTO.setCaracteristicasVa(evento.getCaracteristicasVa());

    	eventoDTO.setComentariosEs(evento.getComentariosEs());
    	eventoDTO.setComentariosVa(evento.getComentariosVa());

        if (eventoDTO.getParTiposEvento() != null)
        	eventoDTO.setParTiposEvento(TipoEvento.tipoEventoToTipoEventoDTO(evento.getParTiposEvento()));
        else if (evento.getParTiposEvento() != null)
            eventoDTO.setParTiposEvento(TipoEvento.tipoEventoToTipoEventoDTO(evento.getParTiposEvento()));

        if (evento.getParTpv() != null)
        {
            eventoDTO.setParTpv(Tpv.tpvToTpvDTO(evento.getParTpv()));
        }
        
        eventoDTO.setAsientosNumerados(evento.getAsientosNumerados());
        eventoDTO.setIvaSgae(evento.getIvaSGAE());
        eventoDTO.setRetencionSgae(evento.getRetencionSGAE());
        eventoDTO.setPorcentajeIva(evento.getPorcentajeIVA());

        eventoDTO.setFormato(evento.getFormato());
        eventoDTO.setExpediente(evento.getExpediente());
        eventoDTO.setCodigoDistribuidora(evento.getCodigoDistribuidora());
        eventoDTO.setNombreDistribuidora(evento.getNombreDistribuidora());
        eventoDTO.setNacionalidad(evento.getNacionalidad());
        eventoDTO.setVo(evento.getVo());
        eventoDTO.setMetraje(evento.getMetraje());
        eventoDTO.setSubtitulos(evento.getSubtitulos());
    	
    	return eventoDTO;
	}

    public Evento(EventoDTO eventoDTO, boolean crearConImagen)
    {
        this.sesiones = new ArrayList<Sesion>();
        
        this.id = eventoDTO.getId();
        this.tituloEs = eventoDTO.getTituloEs();
        this.tituloVa = eventoDTO.getTituloVa();

        this.descripcionEs = eventoDTO.getDescripcionEs();
        this.descripcionVa = eventoDTO.getDescripcionVa();

        this.companyiaEs = eventoDTO.getCompanyiaEs();
        this.companyiaVa = eventoDTO.getCompanyiaVa();

        this.interpretesEs = eventoDTO.getInterpretesEs();
        this.interpretesVa = eventoDTO.getInterpretesVa();

        this.duracionEs = eventoDTO.getDuracionEs();
        this.duracionVa = eventoDTO.getDuracionVa();

        if (crearConImagen)
        {
            this.imagen = eventoDTO.getImagen();
        }

        this.imagenContentType = eventoDTO.getImagenContentType();
        this.imagenSrc = eventoDTO.getImagenSrc();

        this.premiosEs = eventoDTO.getPremiosEs();
        this.premiosVa = eventoDTO.getPremiosVa();

        this.caracteristicasEs = eventoDTO.getCaracteristicasEs();
        this.caracteristicasVa = eventoDTO.getCaracteristicasVa();

        this.comentariosEs = eventoDTO.getComentariosEs();
        this.comentariosVa = eventoDTO.getComentariosVa();

        if (eventoDTO.getParTiposEvento() != null)
        {
            this.parTiposEvento = new TipoEvento();
            this.parTiposEvento.setId(eventoDTO.getParTiposEvento().getId());
            this.parTiposEvento.setNombreEs(eventoDTO.getParTiposEvento().getNombreEs());
            this.parTiposEvento.setNombreVa(eventoDTO.getParTiposEvento().getNombreVa());
            this.tipoEvento = eventoDTO.getParTiposEvento().getId();
        }

        if (eventoDTO.getParTpv() != null)
        {
            this.parTpv = new Tpv(eventoDTO.getParTpv());
        }
        
        this.asientosNumerados = eventoDTO.getAsientosNumerados();
        this.ivaSGAE = eventoDTO.getIvaSgae();
        this.retencionSGAE = eventoDTO.getRetencionSgae();
        this.porcentajeIVA = eventoDTO.getPorcentajeIva();
        this.rssId = eventoDTO.getRssId();
    }

    public Evento(String tituloEs, TipoEvento tipoEvento)
    {
        this.sesiones = new ArrayList<Sesion>();
        this.parTiposEvento = new TipoEvento();
        this.parTiposEvento = tipoEvento;
        this.tituloEs = tituloEs;
    }

    public Evento(String rssId, String tituloEs, String descripcionEs, String companyiaEs, String interpretesEs,
            String duracionEs, String premiosEs, String caracteristicasEs, String comentariosEs,
            String tituloVa, String descripcionVa, String companyiaVa, String interpretesVa,
            String duracionVa, String premiosVa, String caracteristicasVa, String comentariosVa,
            byte[] dataBinary, String nombreArchivo, String mediaType, Integer tipoEventoId, Integer tpvId,
            BigDecimal porcentajeIVA, BigDecimal retencionSGAE, BigDecimal ivaSGAE, Boolean asientosNumerados, 
            String expediente, String codigoDistribuidora, String nombreDistribuidora, String nacionalidad, String vo, String metraje, String subtitulos, String formato, Cine cine, String promotor, String nifPromotor)
    {
        this.sesiones = new ArrayList<Sesion>();
        this.rssId = rssId;
        this.tituloEs = tituloEs;
        this.descripcionEs = descripcionEs;
        this.companyiaEs = companyiaEs;
        this.interpretesEs = interpretesEs;
        this.duracionEs = duracionEs;
        this.premiosEs = premiosEs;
        this.caracteristicasEs = caracteristicasEs;
        this.comentariosEs = comentariosEs;

        this.tituloVa = tituloVa;
        this.descripcionVa = descripcionVa;
        this.companyiaVa = companyiaVa;
        this.interpretesVa = interpretesVa;
        this.duracionVa = duracionVa;
        this.premiosVa = premiosVa;
        this.caracteristicasVa = caracteristicasVa;
        this.comentariosVa = comentariosVa;

        this.imagen = dataBinary;
        this.imagenSrc = nombreArchivo;
        this.imagenContentType = mediaType;

        if (tipoEventoId != null)
        {
            this.parTiposEvento = new TipoEvento();
            this.parTiposEvento.setId(tipoEventoId);
            this.tipoEvento = tipoEventoId;
        }

        if (tpvId != null)
        {
            this.parTpv = new Tpv();
            this.parTpv.setId(tpvId);
        }

        this.promotor = promotor;
        this.nifPromotor = nifPromotor;
        
        this.porcentajeIVA = porcentajeIVA;
        this.retencionSGAE = retencionSGAE;
        this.ivaSGAE = ivaSGAE;
        this.asientosNumerados = asientosNumerados;

        this.formato = formato;
        this.expediente = expediente;
        this.codigoDistribuidora = codigoDistribuidora;
        this.nombreDistribuidora = nombreDistribuidora;
        this.nacionalidad = nacionalidad;
        this.vo = vo;
        this.metraje = metraje;
        this.subtitulos = subtitulos;
        this.cine = cine;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @XmlTransient
    public byte[] getImagen()
    {
        return imagen;
    }

    public void setImagen(byte[] imagen)
    {
        this.imagen = imagen;
    }

    public void setImagenSrc(String imagenSrc)
    {
        this.imagenSrc = imagenSrc;
    }

    public String getImagenContentType()
    {
        return imagenContentType;
    }

    public void setImagenContentType(String imagenContentType)
    {
        this.imagenContentType = imagenContentType;
    }

    public TipoEvento getParTiposEvento()
    {
        return parTiposEvento;
    }

    public void setParTipoEvento(TipoEvento parTiposEvento)
    {
        this.parTiposEvento = parTiposEvento;
    }

    public long getTipoEvento()
    {
        if (parTiposEvento != null)
            return parTiposEvento.getId();
        else
            return 0;
    }

    public Tpv getParTpv() {
        return parTpv;
    }

    public void setParTpv(Tpv parTpv) {
        this.parTpv = parTpv;
    }

    public void setTipoEvento(long tipoEvento)
    {
        this.tipoEvento = tipoEvento;
    }

    public String getTituloEs()
    {
        return tituloEs;
    }

    public void setTituloEs(String tituloEs)
    {
        this.tituloEs = tituloEs;
    }

    public String getTituloVa()
    {
        return tituloVa;
    }

    public void setTituloVa(String tituloVa)
    {
        this.tituloVa = tituloVa;
    }

    public String getDescripcionEs()
    {
        return descripcionEs;
    }

    public void setDescripcionEs(String descripcionEs)
    {
        this.descripcionEs = descripcionEs;
    }

    public String getDescripcionVa()
    {
        return descripcionVa;
    }

    public void setDescripcionVa(String descripcionVa)
    {
        this.descripcionVa = descripcionVa;
    }

    public String getCompanyiaEs()
    {
        return companyiaEs;
    }

    public void setCompanyiaEs(String companyiaEs)
    {
        this.companyiaEs = companyiaEs;
    }

    public String getCompanyiaVa()
    {
        return companyiaVa;
    }

    public void setCompanyiaVa(String companyiaVa)
    {
        this.companyiaVa = companyiaVa;
    }

    public String getInterpretesEs()
    {
        return interpretesEs;
    }

    public void setInterpretesEs(String interpretesEs)
    {
        this.interpretesEs = interpretesEs;
    }

    public String getInterpretesVa()
    {
        return interpretesVa;
    }

    public void setInterpretesVa(String interpretesVa)
    {
        this.interpretesVa = interpretesVa;
    }

    public String getDuracionEs()
    {
        return duracionEs;
    }

    public void setDuracionEs(String duracionEs)
    {
        this.duracionEs = duracionEs;
    }

    public String getDuracionVa()
    {
        return duracionVa;
    }

    public void setDuracionVa(String duracionVa)
    {
        this.duracionVa = duracionVa;
    }

    public String getPremiosEs()
    {
        return premiosEs;
    }

    public void setPremiosEs(String premiosEs)
    {
        this.premiosEs = premiosEs;
    }

    public String getPremiosVa()
    {
        return premiosVa;
    }

    public void setPremiosVa(String premiosVa)
    {
        this.premiosVa = premiosVa;
    }

    public String getCaracteristicasEs()
    {
        return caracteristicasEs;
    }

    public void setCaracteristicasEs(String caracteristicasEs)
    {
        this.caracteristicasEs = caracteristicasEs;
    }

    public String getCaracteristicasVa()
    {
        return caracteristicasVa;
    }

    public void setCaracteristicasVa(String caracteristicasVa)
    {
        this.caracteristicasVa = caracteristicasVa;
    }

    public String getComentariosEs()
    {
        return comentariosEs;
    }

    public void setComentariosEs(String comentariosEs)
    {
        this.comentariosEs = comentariosEs;
    }

    public String getComentariosVa()
    {
        return comentariosVa;
    }

    public void setComentariosVa(String comentariosVa)
    {
        this.comentariosVa = comentariosVa;
    }

    public String getImagenSrc()
    {
        return imagenSrc;
    }

	public Boolean getAsientosNumerados()
    {
        return asientosNumerados;
    }

    public void setAsientosNumerados(Boolean asientosNumerados)
    {
        this.asientosNumerados = asientosNumerados;
    }

    public BigDecimal getPorcentajeIVA() {
		return porcentajeIVA;
	}

	public void setPorcentajeIVA(BigDecimal porcentajeIVA) {
		this.porcentajeIVA = porcentajeIVA;
	}

	public BigDecimal getIvaSGAE() {
		return ivaSGAE;
	}

	public void setIvaSGAE(BigDecimal ivaSGAE) {
		this.ivaSGAE = ivaSGAE;
	}

	public BigDecimal getRetencionSGAE() {
		return retencionSGAE;
	}

	public void setRetencionSGAE(BigDecimal retencionSGAE) {
		this.retencionSGAE = retencionSGAE;
	}

	public Date getFechaPrimeraSesion() {
		return fechaPrimeraSesion;
	}

	public void setFechaPrimeraSesion(Date fechaPrimeraSesion) {
		this.fechaPrimeraSesion = fechaPrimeraSesion;
	}

    public List<Sesion> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<Sesion> sesiones) {
        this.sesiones = sesiones;
    }

    public String getRssId() {
        return rssId;
    }

    public void setRssId(String rssId) {
        this.rssId = rssId;
    }

    public String getExpediente()
    {
        return expediente;
    }

    public void setExpediente(String expediente)
    {
        this.expediente = expediente;
    }

    public String getCodigoDistribuidora()
    {
        return codigoDistribuidora;
    }

    public void setCodigoDistribuidora(String codigoDistribuidora)
    {
        this.codigoDistribuidora = codigoDistribuidora;
    }

    public String getNombreDistribuidora()
    {
        return nombreDistribuidora;
    }

    public void setNombreDistribuidora(String nombreDistribuidora)
    {
        this.nombreDistribuidora = nombreDistribuidora;
    }

    public String getNacionalidad()
    {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad)
    {
        this.nacionalidad = nacionalidad;
    }

    public String getVo()
    {
        return vo;
    }

    public void setVo(String vo)
    {
        this.vo = vo;
    }

    public String getMetraje()
    {
        return metraje;
    }

    public void setMetraje(String metraje)
    {
        this.metraje = metraje;
    }

    public void setParTiposEvento(TipoEvento parTiposEvento)
    {
        this.parTiposEvento = parTiposEvento;
    }

    public String getSubtitulos()
    {
        return subtitulos;
    }

    public void setSubtitulos(String subtitulos)
    {
        this.subtitulos = subtitulos;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Cine getCine()
    {
        return cine;
    }

    public void setCine(Cine cine)
    {
        this.cine = cine;
    }

    public String getPromotor()
    {
        return promotor;
    }

    public void setPromotor(String promotor)
    {
        this.promotor = promotor;
    }

    public String getNifPromotor()
    {
        return nifPromotor;
    }

    public void setNifPromotor(String nifPromotor)
    {
        this.nifPromotor = nifPromotor;
    }

    public static void checkValidity(int codigoPelicula, String codigoExpediente, String titulo, String codigoDistribuidora2,
			String nombreDistribuidora2, String versionOriginal, String versionLinguistica, String idiomaSubtitulos, 
			String formatoProyeccion) throws RegistroSerializaException {
		if (codigoPelicula == 0)
        	throw new RegistroSerializaException(GeneralPARException.CODIGO_PELICULA_NULO_CODE);

        if (codigoExpediente == null)
            throw new RegistroSerializaException(GeneralPARException.CODIGO_EXPEDIENTE_NULO_CODE);

        if (titulo == null)
            throw new RegistroSerializaException(GeneralPARException.TITULO_PELICULA_NULO_CODE);

        if (codigoDistribuidora2 == null)
            throw new RegistroSerializaException(GeneralPARException.CODIGO_DISTRIBUIDORA_NULO_CODE);

        if (nombreDistribuidora2 == null)
            throw new RegistroSerializaException(GeneralPARException.NOMBRE_DISTRIBUIDORA_NULO_CODE);

        if (versionOriginal == null)
            throw new RegistroSerializaException(GeneralPARException.VERSION_ORIGINAL_NULO_CODE);

        if (versionLinguistica == null)
            throw new RegistroSerializaException(GeneralPARException.VERSION_LINGUISTICA_NULO_CODE);

        if (idiomaSubtitulos == null)
            throw new RegistroSerializaException(GeneralPARException.IDIOMA_SUBTITULOS_NULO_CODE);

        if (formatoProyeccion == null)
            throw new RegistroSerializaException(GeneralPARException.FORMATO_PROYECCION_NULO_CODE);
        
        if (Integer.toString(codigoPelicula).length() > 5) {
        	logger.error("El codigo de película tiene más de 5 caracteres: " + codigoPelicula);
            throw new RegistroSerializaException(GeneralPARException.CODIGO_PELICULA_LARGO_CODE);
        }

        if (codigoExpediente.length() > 12) {
        	logger.error("El codigo de expediente tiene más de 12 caracteres: " + codigoExpediente);
            throw new RegistroSerializaException(GeneralPARException.CODIGO_EXPEDIENTE_LARGO_CODE);
        }

        if (codigoDistribuidora2.length() > 12) {
        	logger.error("El codigo de distribuidora tiene más de 12 caracteres: " + codigoDistribuidora2);
            throw new RegistroSerializaException(GeneralPARException.CODIGO_DISTRIBUIDORA_LARGO_CODE);
        }

        if (nombreDistribuidora2.length() > 50) {
        	logger.error("El nombre de distribuidora tiene más de 50 caracteres: " + nombreDistribuidora2);
        	throw new RegistroSerializaException(GeneralPARException.NOMBRE_DISTRIBUIDORA_LARGO_CODE);
        }

        if (versionOriginal.length() != 1)
            throw new RegistroSerializaException(GeneralPARException.DIGITOS_VERSION_ORIGINAL_CODE);

        if (versionLinguistica.length() != 1)
            throw new RegistroSerializaException(GeneralPARException.DIGITOS_VERSION_LINGUISTICA_CODE);

        if (idiomaSubtitulos.length() != 1)
            throw new RegistroSerializaException(GeneralPARException.DIGITOS_IDIOMA_SUBTITULOS_CODE);
        
        if (formatoProyeccion.length() != 1)
        	throw new RegistroSerializaException(GeneralPARException.DIGITOS_FORMATO_PROYECCION_CODE);
	}


	public List<EventoMultisesion> getEventosMultisesion() {
		return eventosMultisesion;
	}

	public void setEventosMultisesion(String jsonEventosMultisesion) {
        Gson gson = new Gson();
        List<EventoMultisesion> eventos = gson.fromJson(jsonEventosMultisesion, new TypeToken<List<EventoMultisesion>>(){}.getType());
        this.eventosMultisesion = eventos;
	}

	public String getMultisesion() {
		return multisesion;
	}

	public void setMultisesion(String multisesion) {
		this.multisesion = multisesion;
	}

    public Boolean getMultipleTpv() {
        return multipleTpv;
    }

    public void setMultipleTpv(Boolean multipleTpv) {
        this.multipleTpv = multipleTpv;
    }
}