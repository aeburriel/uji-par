package es.uji.apps.par.db;

import java.io.Serializable;
import java.util.List;

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
 * The persistent class for the PAR_PLANTILLAS database table.
 * 
 */
@Entity
@Table(name="PAR_PLANTILLAS")
public class PlantillaDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_PLANTILLAS_ID_GENERATOR", sequenceName="par_plantillas_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_PLANTILLAS_ID_GENERATOR")
	private long id;

	private String nombre;

	//bi-directional many-to-one association to PreciosPlantillaDTO
	@OneToMany(mappedBy="parPlantilla")
	private List<PreciosPlantillaDTO> parPreciosPlantillas;

	//bi-directional many-to-one association to SesionDTO
	@OneToMany(mappedBy="parPlantilla")
	private List<SesionDTO> parSesiones;

    //bi-directional many-to-one association to SesionDTO
    @OneToMany(mappedBy="parPlantilla")
    private List<AbonoDTO> parAbonos;
	
	@ManyToOne
	@JoinColumn(name="SALA_ID")
	private SalaDTO sala;

	public PlantillaDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<PreciosPlantillaDTO> getParPreciosPlantillas() {
		return this.parPreciosPlantillas;
	}

	public void setParPreciosPlantillas(List<PreciosPlantillaDTO> parPreciosPlantillas) {
		this.parPreciosPlantillas = parPreciosPlantillas;
	}

	public List<SesionDTO> getParSesiones() {
		return this.parSesiones;
	}

	public void setParSesiones(List<SesionDTO> parSesiones) {
		this.parSesiones = parSesiones;
	}

    public List<AbonoDTO> getParAbonos() {
        return parAbonos;
    }

    public void setParAbonos(List<AbonoDTO> parAbonos) {
        this.parAbonos = parAbonos;
    }

    public SalaDTO getSala() {
		return sala;
	}

	public void setSala(SalaDTO sala) {
		this.sala = sala;
	}

}