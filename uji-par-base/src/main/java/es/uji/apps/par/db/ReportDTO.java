package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="PAR_REPORTS")
public class ReportDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "PAR_REPORTS_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_REPORTS_ID_GENERATOR")
	private long id;

	@ManyToOne
	@JoinColumn(name="SALA_ID")
	private SalaDTO sala;

	private String tipo;

	private String clase;

	public ReportDTO() {
	}

	public ReportDTO(long salaId, String tipoEntrada, String clase) {
		this.sala = new SalaDTO(salaId);
		this.tipo = tipoEntrada;
		this.clase = clase;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SalaDTO getSala() {
		return sala;
	}

	public void setSala(SalaDTO sala) {
		this.sala = sala;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}
}
