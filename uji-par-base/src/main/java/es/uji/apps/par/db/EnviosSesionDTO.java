package es.uji.apps.par.db;

import java.io.Serializable;

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
 * The persistent class for the PAR_ENVIOS_SESIONES database table.
 * 
 */
@Entity
@Table(name="PAR_ENVIOS_SESIONES")
public class EnviosSesionDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_ENVIOS_SESIONES_ID_GENERATOR", sequenceName="par_envios_sesiones_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_ENVIOS_SESIONES_ID_GENERATOR")
	private long id;
	
	@Column(name="TIPO_ENVIO")
	private String tipoEnvio;
	
	//bi-directional many-to-one association to SesionDTO
	@ManyToOne
	@JoinColumn(name="par_sesion_id")
	private SesionDTO parSesion;
	
	//bi-directional many-to-one association to EnvioDTO
	@ManyToOne
	@JoinColumn(name="par_envio_id")
	private EnvioDTO parEnvio;

	public EnviosSesionDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SesionDTO getParSesion() {
		return parSesion;
	}

	public void setParSesion(SesionDTO parSesion) {
		this.parSesion = parSesion;
	}

	public EnvioDTO getParEnvio() {
		return parEnvio;
	}

	public void setParEnvio(EnvioDTO parEnvio) {
		this.parEnvio = parEnvio;
	}

	public String getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(String tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
	}
}