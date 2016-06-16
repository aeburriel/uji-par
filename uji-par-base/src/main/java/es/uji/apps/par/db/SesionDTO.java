package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the PAR_SESIONES database table.
 *
 */
@Entity
@Table(name = "PAR_SESIONES")
public class SesionDTO implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "PAR_SESIONES_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_SESIONES_ID_GENERATOR")
	private long id;

	@Column(name = "ANULADA")
	private Boolean anulada;

	@Column(name = "CANAL_INTERNET")
	private Boolean canalInternet;

	@Column(name = "CANAL_TAQUILLA")
	private Boolean canalTaquilla;

	@Column(name = "FECHA_CELEBRACION")
	private Timestamp fechaCelebracion;

	@Column(name = "FECHA_FIN_VENTA_ONLINE")
	private Timestamp fechaFinVentaOnline;

	@Column(name = "FECHA_INICIO_VENTA_ONLINE")
	private Timestamp fechaInicioVentaOnline;

	@Column(name = "HORA_APERTURA")
	private String horaApertura;

	@Column(name = "NOMBRE")
	private String nombre;

	@Column(name = "VER_LING")
	private String versionLinguistica;

	@Column(name = "RSS_ID")
	private String rssId;

	@Column(name = "INCIDENCIA_ID")
	private Integer incidenciaId;

	//bi-directional many-to-one association to PreciosSesionDTO
	@OneToMany(mappedBy = "parSesione", cascade = CascadeType.PERSIST)
	private List<PreciosSesionDTO> parPreciosSesions;

	//bi-directional many-to-one association to EventoDTO
	@ManyToOne
	@JoinColumn(name = "EVENTO_ID")
	private EventoDTO parEvento;

	//bi-directional many-to-one association to PlantillaDTO
	@ManyToOne
	@JoinColumn(name = "PLANTILLA_ID")
	private PlantillaDTO parPlantilla;

	@ManyToOne
	@JoinColumn(name = "SALA_ID")
	private SalaDTO parSala;

	//bi-directional many-to-one association to SesionDTO
	@OneToMany(mappedBy = "parSesion")
	private List<ButacaDTO> parButacas;

	@OneToMany(mappedBy = "parSesion")
	private List<CompraDTO> parCompras;

	//bi-directional many-to-one association to EnviosSesionDTO
	@OneToMany(mappedBy = "parSesion")
	private List<EnviosSesionDTO> parEnviosSesion;

	@OneToMany(mappedBy = "parSesion")
	private List<SesionAbonoDTO> parAbonos;

	public SesionDTO()
	{
	}

	public SesionDTO(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Boolean getCanalInternet()
	{
		return canalInternet;
	}

	public void setCanalInternet(Boolean canalInternet)
	{
		this.canalInternet = canalInternet;
	}

	public Boolean getCanalTaquilla()
	{
		return canalTaquilla;
	}

	public void setCanalTaquilla(Boolean canalTaquilla)
	{
		this.canalTaquilla = canalTaquilla;
	}

	public Timestamp getFechaCelebracion()
	{
		return this.fechaCelebracion;
	}

	public void setFechaCelebracion(Timestamp fechaCelebracion)
	{
		this.fechaCelebracion = fechaCelebracion;
	}

	public Timestamp getFechaFinVentaOnline()
	{
		return this.fechaFinVentaOnline;
	}

	public void setFechaFinVentaOnline(Timestamp fechaFinVentaOnline)
	{
		this.fechaFinVentaOnline = fechaFinVentaOnline;
	}

	public Timestamp getFechaInicioVentaOnline()
	{
		return this.fechaInicioVentaOnline;
	}

	public void setFechaInicioVentaOnline(Timestamp fechaInicioVentaOnline)
	{
		this.fechaInicioVentaOnline = fechaInicioVentaOnline;
	}

	public String getHoraApertura()
	{
		return this.horaApertura;
	}

	public void setHoraApertura(String horaApertura)
	{
		this.horaApertura = horaApertura;
	}

	public List<PreciosSesionDTO> getParPreciosSesions()
	{
		return this.parPreciosSesions;
	}

	public void setParPreciosSesions(List<PreciosSesionDTO> parPreciosSesions)
	{
		this.parPreciosSesions = parPreciosSesions;
	}

	public void addParPreciosSesion(PreciosSesionDTO preciosSesionDTO)
	{
		if (this.parPreciosSesions == null)
		{
			this.parPreciosSesions = new ArrayList<PreciosSesionDTO>();
		}

		preciosSesionDTO.setParSesione(this);
		this.parPreciosSesions.add(preciosSesionDTO);
	}

	public EventoDTO getParEvento()
	{
		return this.parEvento;
	}

	public void setParEvento(EventoDTO parEvento)
	{
		this.parEvento = parEvento;
	}

	public PlantillaDTO getParPlantilla()
	{
		return this.parPlantilla;
	}

	public void setParPlantilla(PlantillaDTO parPlantilla)
	{
		this.parPlantilla = parPlantilla;
	}

	public List<ButacaDTO> getParButacas()
	{
		return parButacas;
	}

	public void setParButacas(List<ButacaDTO> parButacas)
	{
		this.parButacas = parButacas;
	}

	public String getNombre()
	{
		return nombre;
	}

	public void setNombre(String nombre)
	{
		this.nombre = nombre;
	}

	public SalaDTO getParSala()
	{
		return parSala;
	}

	public void setParSala(SalaDTO parSala)
	{
		this.parSala = parSala;
	}

	public List<CompraDTO> getParCompras()
	{
		return parCompras;
	}

	public void setParCompras(List<CompraDTO> parCompras)
	{
		this.parCompras = parCompras;
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

	public List<EnviosSesionDTO> getParEnviosSesion()
	{
		return parEnviosSesion;
	}

	public void setParEnviosSesion(List<EnviosSesionDTO> parEnviosSesion)
	{
		this.parEnviosSesion = parEnviosSesion;
	}

	public Integer getIncidenciaId()
	{
		return incidenciaId;
	}

	public void setIncidenciaId(Integer incidenciaId)
	{
		this.incidenciaId = incidenciaId;
	}

	public Boolean getAnulada()
	{
		return anulada;
	}

	public void setAnulada(Boolean anulada)
	{
		this.anulada = anulada;
	}

	public List<SesionAbonoDTO> getParAbonos()
	{
		return parAbonos;
	}

	public void setParAbonos(List<SesionAbonoDTO> parAbonos)
	{
		this.parAbonos = parAbonos;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof SesionDTO)
		{
			SesionDTO toCompare = (SesionDTO) o;
			return this.id == toCompare.id;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(id).intValue();
	}
}