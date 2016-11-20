package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="PAR_SALAS_USUARIOS")
public class SalasUsuarioDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_SALAS_USUARIOS_ID_GENERATOR", sequenceName="HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_SALAS_USUARIOS_ID_GENERATOR")
	private long id;

	@ManyToOne
	@JoinColumn(name="usuario_id")
	private UsuarioDTO parUsuario;

	@ManyToOne
	@JoinColumn(name="sala_id")
	private SalaDTO parSala;

	public SalasUsuarioDTO() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public UsuarioDTO getParUsuario() {
		return parUsuario;
	}

	public void setParUsuario(UsuarioDTO parUsuario) {
		this.parUsuario = parUsuario;
	}

	public SalaDTO getParSala() {
		return parSala;
	}

	public void setParSala(SalaDTO parSala) {
		this.parSala = parSala;
	}
}
