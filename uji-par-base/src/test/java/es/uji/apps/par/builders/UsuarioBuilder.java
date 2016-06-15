package es.uji.apps.par.builders;

import es.uji.apps.par.db.SalaDTO;
import es.uji.apps.par.db.SalasUsuarioDTO;
import es.uji.apps.par.db.UsuarioDTO;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class UsuarioBuilder
{
	private UsuarioDTO usuario;
	private List<SalasUsuarioDTO> salas;

	public UsuarioBuilder(String nombre, String mail, String login)
	{
		salas = new ArrayList<>();

		usuario = new UsuarioDTO();
		usuario.setNombre(nombre);
		usuario.setMail(mail);
		usuario.setUsuario(login);
	}

	public UsuarioBuilder withSala(SalaDTO sala)
	{
		SalasUsuarioDTO salasUsuarioDTO = new SalasUsuarioDTO();
		salasUsuarioDTO.setParSala(sala);

		salas.add(salasUsuarioDTO);

		return this;
	}

	public UsuarioDTO build(EntityManager entityManager)
	{
		entityManager.persist(usuario);

		for (SalasUsuarioDTO salaUsuario : salas)
		{
			salaUsuario.setParUsuario(usuario);
			entityManager.persist(salaUsuario);
		}

		return usuario;
	}
}