package es.uji.apps.par.db;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * The persistent class for the PAR_PRECIOS_SESION database table.
 * 
 */
@Entity
@Table(name="PAR_PRECIOS_SESION")
public class PreciosSesionDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_PRECIOS_SESION_ID_GENERATOR", sequenceName="par_precios_sesion_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_PRECIOS_SESION_ID_GENERATOR")
	private long id;

	private BigDecimal descuento;

	private BigDecimal invitacion;

	private BigDecimal precio;
	
	@Column(name="AULA_TEATRO")
	private BigDecimal aulaTeatro;

	//bi-directional many-to-one association to LocalizacionDTO
	@ManyToOne
	@JoinColumn(name="LOCALIZACION_ID")
	private LocalizacionDTO parLocalizacione;

	//bi-directional many-to-one association to SesionDTO
	@ManyToOne
	@JoinColumn(name="SESION_ID")
	private SesionDTO parSesione;
	
	@ManyToOne
	@JoinColumn(name="TARIFA_ID")
	private TarifaDTO parTarifa;

	public PreciosSesionDTO() {
	}

	public PreciosSesionDTO(BigDecimal descuento, BigDecimal invitacion, BigDecimal precio, LocalizacionDTO localizacion)
    {
        super();
        this.descuento = descuento;
        this.invitacion = invitacion;
        this.precio = precio;
        this.parLocalizacione = localizacion;
    }

    public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getDescuento() {
		return this.descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	public BigDecimal getInvitacion() {
		return this.invitacion;
	}

	public void setInvitacion(BigDecimal invitacion) {
		this.invitacion = invitacion;
	}

	public BigDecimal getPrecio() {
		return this.precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
	
	public BigDecimal getAulaTeatro() {
        return aulaTeatro;
    }

    public void setAulaTeatro(BigDecimal aulaTeatro) {
        this.aulaTeatro = aulaTeatro;
    }

    public LocalizacionDTO getParLocalizacione() {
		return this.parLocalizacione;
	}

	public void setParLocalizacione(LocalizacionDTO parLocalizacione) {
		this.parLocalizacione = parLocalizacione;
	}

	public SesionDTO getParSesione() {
		return this.parSesione;
	}

	public void setParSesione(SesionDTO parSesione) {
		this.parSesione = parSesione;
	}

	public TarifaDTO getParTarifa() {
		return parTarifa;
	}

	public void setParTarifa(TarifaDTO parTarifa) {
		this.parTarifa = parTarifa;
	}

}