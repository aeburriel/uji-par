package es.uji.apps.par.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * The persistent class for the PAR_LOCALIZACIONES database table.
 * 
 */
@Entity
@Table(name="PAR_LOCALIZACIONES")
public class LocalizacionDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_LOCALIZACIONES_ID_GENERATOR", sequenceName="par_localizaciones_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_LOCALIZACIONES_ID_GENERATOR")
	private long id;

	@Column(name="CODIGO")
	private String codigo;
	
	@Column(name="NOMBRE_ES")
	private String nombreEs;

	@Column(name="NOMBRE_VA")
	private String nombreVa;
	
	@Column(name="INICIALES")
	private String iniciales;

	@Column(name="TOTAL_ENTRADAS")
	private BigDecimal totalEntradas;

	//bi-directional many-to-one association to PreciosPlantillaDTO
	@OneToMany(mappedBy="parLocalizacione")
	private List<PreciosPlantillaDTO> parPreciosPlantillas;

	//bi-directional many-to-one association to PreciosSesionDTO
	@OneToMany(mappedBy="parLocalizacione")
	private List<PreciosSesionDTO> parPreciosSesions;
	
	@ManyToOne
	@JoinColumn(name="SALA_ID")
	private SalaDTO sala;

	public LocalizacionDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public String getNombreEs() {
		return this.nombreEs;
	}

	public void setNombreEs(String nombreEs) {
		this.nombreEs = nombreEs;
	}

	public String getNombreVa() {
		return this.nombreVa;
	}

	public void setNombreVa(String nombreVa) {
		this.nombreVa = nombreVa;
	}

	public BigDecimal getTotalEntradas() {
		return this.totalEntradas;
	}

	public void setTotalEntradas(BigDecimal totalEntradas) {
		this.totalEntradas = totalEntradas;
	}

	public List<PreciosPlantillaDTO> getParPreciosPlantillas() {
		return this.parPreciosPlantillas;
	}

	public void setParPreciosPlantillas(List<PreciosPlantillaDTO> parPreciosPlantillas) {
		this.parPreciosPlantillas = parPreciosPlantillas;
	}

	public List<PreciosSesionDTO> getParPreciosSesions() {
		return this.parPreciosSesions;
	}

	public void setParPreciosSesions(List<PreciosSesionDTO> parPreciosSesions) {
		this.parPreciosSesions = parPreciosSesions;
	}

	public SalaDTO getSala() {
		return sala;
	}

	public void setSala(SalaDTO sala) {
		this.sala = sala;
	}

	public String getIniciales() {
		return iniciales;
	}

	public void setIniciales(String iniciales) {
		this.iniciales = iniciales;
	}

}