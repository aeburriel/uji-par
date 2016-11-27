package es.uji.apps.par.db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * The persistent class for the PAR_TARIFAS_CINES database table.
 * 
 */
@Entity
@Table(name="PAR_TARIFAS_CINES")
public class TarifasCineDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_TARIFAS_CINE_ID_GENERATOR", sequenceName="par_tarifas_cines_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_TARIFAS_CINE_ID_GENERATOR")
	private long id;

	//bi-directional many-to-one association to LocalizacionDTO
	@ManyToOne
	@JoinColumn(name="PAR_CINE_ID")
	private CineDTO parCine;

	//bi-directional many-to-one association to SesionDTO
	@ManyToOne
	@JoinColumn(name="PAR_TARIFA_ID")
	private TarifaDTO parTarifa;

	public TarifasCineDTO() {
	}

    public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CineDTO getParCine() {
		return parCine;
	}

	public void setParCine(CineDTO parCine) {
		this.parCine = parCine;
	}

	public TarifaDTO getParTarifa() {
		return parTarifa;
	}

	public void setParTarifa(TarifaDTO parTarifa) {
		this.parTarifa = parTarifa;
	}
}