package es.uji.apps.par.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * The persistent class for the PAR_ENVIOS database table.
 * 
 */
@Entity
@Table(name="PAR_ENVIOS")
public class EnvioDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_ENVIOS_ID_GENERATOR", sequenceName="par_envios_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_ENVIOS_ID_GENERATOR")
	private long id;

	@Column(name="FECHA_GENERACION_FICHERO")
	private Timestamp fechaGeneracionFichero;

	@Column(name="FECHA_ENVIO_FICHERO")
	private Timestamp fechaEnvioFichero;

	//bi-directional many-to-one association to EnviosSesionDTO
	@OneToMany(mappedBy="parEnvio")
	private List<EnviosSesionDTO> parEnviosSesion;

	public EnvioDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Timestamp getFechaGeneracionFichero() {
		return fechaGeneracionFichero;
	}

	public void setFechaGeneracionFichero(Timestamp fechaGeneracionFichero) {
		this.fechaGeneracionFichero = fechaGeneracionFichero;
	}

	public Timestamp getFechaEnvioFichero() {
		return fechaEnvioFichero;
	}

	public void setFechaEnvioFichero(Timestamp fechaEnvioFichero) {
		this.fechaEnvioFichero = fechaEnvioFichero;
	}

	public List<EnviosSesionDTO> getParEnviosSesion() {
		return parEnviosSesion;
	}

	public void setParEnviosSesion(List<EnviosSesionDTO> parEnviosSesion) {
		this.parEnviosSesion = parEnviosSesion;
	}
}