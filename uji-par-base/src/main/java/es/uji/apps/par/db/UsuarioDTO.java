package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * The persistent class for the PAR_USUARIOS database table.
 * 
 */
@Entity
@Table(name="PAR_USUARIOS")
public class UsuarioDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_USUARIOS_ID_GENERATOR", sequenceName="HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_USUARIOS_ID_GENERATOR")
	private long id;

	private String mail;

	private String nombre;

	private String usuario;

	private String url;

	@OneToMany(mappedBy = "parUsuario", fetch = FetchType.LAZY)
	private List<SalasUsuarioDTO> parSalasUsuario;

	public UsuarioDTO() {
	}

	public UsuarioDTO(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMail() {
		return this.mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUsuario() {
		return this.usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public List<SalasUsuarioDTO> getParSalasUsuario() {
		return parSalasUsuario;
	}

	public void setParSalasUsuario(List<SalasUsuarioDTO> parSalasUsuario) {
		this.parSalasUsuario = parSalasUsuario;
	}
}