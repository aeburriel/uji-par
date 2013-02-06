package es.uji.apps.par.db;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;


/**
 * The persistent class for the PAR_EVENTOS database table.
 * 
 */
@Entity
@Table(name="PAR_EVENTOS")
public class EventoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_EVENTOS_ID_GENERATOR", sequenceName="HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_EVENTOS_ID_GENERATOR")
	private long id;

	@Column(name="ASIENTOS_NUMERADOS")
	private BigDecimal asientosNumerados;

	@Column(name="CARACTERISTICAS_ES")
	private String caracteristicasEs;

	@Column(name="CARACTERISTICAS_VA")
	private String caracteristicasVa;

	@Column(name="COMENTARIOS_ES")
	private String comentariosEs;

	@Column(name="COMENTARIOS_VA")
	private String comentariosVa;

	@Column(name="COMPANYIA_ES")
	private String companyiaEs;

	@Column(name="COMPANYIA_VA")
	private String companyiaVa;

	@Column(name="DESCRIPCION_ES")
	private String descripcionEs;

	@Column(name="DESCRIPCION_VA")
	private String descripcionVa;

	@Column(name="DURACION_ES")
	private String duracionEs;

	@Column(name="DURACION_VA")
	private String duracionVa;

	@Lob
	private byte[] imagen;

	@Column(name="IMAGEN_CONTENT_TYPE")
	private String imagenContentType;

	@Column(name="IMAGEN_SRC")
	private String imagenSrc;

	@Column(name="INTERPRETES_ES")
	private String interpretesEs;

	@Column(name="INTERPRETES_VA")
	private String interpretesVa;

	@Column(name="IVA_SGAE")
	private BigDecimal ivaSgae;

	@Column(name="PORCENTAJE_IVA")
	private BigDecimal porcentajeIva;

	@Column(name="PREMIOS_ES")
	private String premiosEs;

	@Column(name="PREMIOS_VA")
	private String premiosVa;

	@Column(name="RETENCION_SGAE")
	private BigDecimal retencionSgae;

	@Column(name="TITULO_ES")
	private String tituloEs;

	@Column(name="TITULO_VA")
	private String tituloVa;

	//bi-directional many-to-one association to TipoEventoDTO
	@ManyToOne
	@JoinColumn(name="TIPO_EVENTO_ID")
	private TipoEventoDTO parTiposEvento;

	//bi-directional many-to-one association to SesionDTO
	@OneToMany(mappedBy="parEvento")
	private List<SesionDTO> parSesiones;

	public EventoDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getAsientosNumerados() {
		return this.asientosNumerados;
	}

	public void setAsientosNumerados(BigDecimal asientosNumerados) {
		this.asientosNumerados = asientosNumerados;
	}

	public String getCaracteristicasEs() {
		return this.caracteristicasEs;
	}

	public void setCaracteristicasEs(String caracteristicasEs) {
		this.caracteristicasEs = caracteristicasEs;
	}

	public String getCaracteristicasVa() {
		return this.caracteristicasVa;
	}

	public void setCaracteristicasVa(String caracteristicasVa) {
		this.caracteristicasVa = caracteristicasVa;
	}

	public String getComentariosEs() {
		return this.comentariosEs;
	}

	public void setComentariosEs(String comentariosEs) {
		this.comentariosEs = comentariosEs;
	}

	public String getComentariosVa() {
		return this.comentariosVa;
	}

	public void setComentariosVa(String comentariosVa) {
		this.comentariosVa = comentariosVa;
	}

	public String getCompanyiaEs() {
		return this.companyiaEs;
	}

	public void setCompanyiaEs(String companyiaEs) {
		this.companyiaEs = companyiaEs;
	}

	public String getCompanyiaVa() {
		return this.companyiaVa;
	}

	public void setCompanyiaVa(String companyiaVa) {
		this.companyiaVa = companyiaVa;
	}

	public String getDescripcionEs() {
		return this.descripcionEs;
	}

	public void setDescripcionEs(String descripcionEs) {
		this.descripcionEs = descripcionEs;
	}

	public String getDescripcionVa() {
		return this.descripcionVa;
	}

	public void setDescripcionVa(String descripcionVa) {
		this.descripcionVa = descripcionVa;
	}

	public String getDuracionEs() {
		return this.duracionEs;
	}

	public void setDuracionEs(String duracionEs) {
		this.duracionEs = duracionEs;
	}

	public String getDuracionVa() {
		return this.duracionVa;
	}

	public void setDuracionVa(String duracionVa) {
		this.duracionVa = duracionVa;
	}

	public byte[] getImagen() {
		return this.imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}

	public String getImagenContentType() {
		return this.imagenContentType;
	}

	public void setImagenContentType(String imagenContentType) {
		this.imagenContentType = imagenContentType;
	}

	public String getImagenSrc() {
		return this.imagenSrc;
	}

	public void setImagenSrc(String imagenSrc) {
		this.imagenSrc = imagenSrc;
	}

	public String getInterpretesEs() {
		return this.interpretesEs;
	}

	public void setInterpretesEs(String interpretesEs) {
		this.interpretesEs = interpretesEs;
	}

	public String getInterpretesVa() {
		return this.interpretesVa;
	}

	public void setInterpretesVa(String interpretesVa) {
		this.interpretesVa = interpretesVa;
	}

	public BigDecimal getIvaSgae() {
		return this.ivaSgae;
	}

	public void setIvaSgae(BigDecimal ivaSgae) {
		this.ivaSgae = ivaSgae;
	}

	public BigDecimal getPorcentajeIva() {
		return this.porcentajeIva;
	}

	public void setPorcentajeIva(BigDecimal porcentajeIva) {
		this.porcentajeIva = porcentajeIva;
	}

	public String getPremiosEs() {
		return this.premiosEs;
	}

	public void setPremiosEs(String premiosEs) {
		this.premiosEs = premiosEs;
	}

	public String getPremiosVa() {
		return this.premiosVa;
	}

	public void setPremiosVa(String premiosVa) {
		this.premiosVa = premiosVa;
	}

	public BigDecimal getRetencionSgae() {
		return this.retencionSgae;
	}

	public void setRetencionSgae(BigDecimal retencionSgae) {
		this.retencionSgae = retencionSgae;
	}

	public String getTituloEs() {
		return this.tituloEs;
	}

	public void setTituloEs(String tituloEs) {
		this.tituloEs = tituloEs;
	}

	public String getTituloVa() {
		return this.tituloVa;
	}

	public void setTituloVa(String tituloVa) {
		this.tituloVa = tituloVa;
	}

	public TipoEventoDTO getParTiposEvento() {
		return this.parTiposEvento;
	}

	public void setParTiposEvento(TipoEventoDTO parTiposEvento) {
		this.parTiposEvento = parTiposEvento;
	}

	public List<SesionDTO> getParSesiones() {
		return this.parSesiones;
	}

	public void setParSesiones(List<SesionDTO> parSesiones) {
		this.parSesiones = parSesiones;
	}

}