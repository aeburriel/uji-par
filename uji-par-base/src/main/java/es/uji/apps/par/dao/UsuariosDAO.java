package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuariosDAO extends BaseDAO
{
	private QUsuarioDTO qUserDTO = QUsuarioDTO.usuarioDTO;

	@Transactional
	public List<Usuario> getUsers(String sortParameter, int start, int limit)
	{
		List<Usuario> users = new ArrayList<Usuario>();
		List<UsuarioDTO> usuariosDTO = getQueryUsuarios().orderBy(getSort(qUserDTO, sortParameter)).limit(limit).offset(start).list(qUserDTO);

		for (UsuarioDTO userDB : usuariosDTO)
		{
			users.add(new Usuario(userDB));
		}

		return users;
	}

	@Transactional
	private JPAQuery getQueryUsuarios()
	{
		JPAQuery query = new JPAQuery(entityManager);
		return query.from(qUserDTO);
	}

	@Transactional
	public long removeUser(long id)
	{
		JPADeleteClause delete = new JPADeleteClause(entityManager, qUserDTO);
		return delete.where(qUserDTO.id.eq(id)).execute();
	}

	@Transactional
	public Usuario addUser(Usuario user)
	{
		UsuarioDTO usuarioDTO = new UsuarioDTO();
		usuarioDTO.setNombre(user.getNombre());
		usuarioDTO.setMail(user.getMail());
		usuarioDTO.setUsuario(user.getUsuario());

		entityManager.persist(usuarioDTO);

		user.setId(usuarioDTO.getId());
		return user;
	}

	@Transactional
	public Usuario updateUser(Usuario user)
	{
		JPAUpdateClause update = new JPAUpdateClause(entityManager, qUserDTO);
		update.set(qUserDTO.nombre, user.getNombre()).set(qUserDTO.mail, user.getMail()).set(qUserDTO.usuario, user.getUsuario()).where(qUserDTO.id.eq(user.getId())).execute();

		return user;
	}

	@Transactional
	public boolean userExists(Usuario user)
	{
		JPAQuery query = new JPAQuery(entityManager);
		List<UsuarioDTO> usuarios = query.from(qUserDTO).where(qUserDTO.usuario.eq(user.getUsuario())).list(qUserDTO);

		if (usuarios.size() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Transactional
	public int getTotalUsuarios()
	{
		return (int) getQueryUsuarios().count();
	}

	@Transactional
	public void addSalaUsuario(Sala sala, Usuario usuario)
	{
		SalasUsuarioDTO cinesUsuarioDTO = new SalasUsuarioDTO();
		cinesUsuarioDTO.setParSala(new SalaDTO(sala.getId()));
		cinesUsuarioDTO.setParUsuario(new UsuarioDTO(usuario.getId()));
		entityManager.persist(cinesUsuarioDTO);
	}

	@Transactional
	public String getReportClassNameForUserAndType(String login, String tipoInformePdf)
	{
		JPAQuery query = new JPAQuery(entityManager);
		QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;
		QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		QReportDTO qReportDTO = QReportDTO.reportDTO;

		return query.from(qUsuarioDTO).join(qUsuarioDTO.parSalasUsuario, qSalasUsuarioDTO).join(qSalasUsuarioDTO.parSala, qSalaDTO).join(qSalaDTO.parReports, qReportDTO).where(qUsuarioDTO.usuario.toUpperCase().eq(login.toUpperCase()).and(qReportDTO.tipo.toUpperCase().eq(tipoInformePdf.toUpperCase()))).uniqueResult(qReportDTO.clase);
	}

	public Usuario getUserByDomainUrl(String domainUrl)
	{
		JPAQuery query = new JPAQuery(entityManager);

		List<UsuarioDTO> users = query.from(qUserDTO).where(qUserDTO.url.eq(domainUrl)).list(qUserDTO);

		if (users.size() > 0)
		{
			return new Usuario(users.get(0));
		}
		else
		{
			Usuario usuario = new Usuario();
			usuario.setUsuario("");

			return usuario;
		}
	}

	public Cine getUserCineByDomainUrl(String domainUrl)
	{
		JPAQuery query = new JPAQuery(entityManager);
		QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;
		QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		QCineDTO qCineDTO = QCineDTO.cineDTO;

		List<CineDTO> cines = query.from(qUsuarioDTO).join(qUsuarioDTO.parSalasUsuario, qSalasUsuarioDTO).join(qSalasUsuarioDTO.parSala, qSalaDTO).join(qSalaDTO.parCine, qCineDTO).where(qUserDTO.url.eq(domainUrl)).list(qCineDTO);

		return Cine.cineDTOToCine(cines.get(0));
	}

	public Cine getUserCineByUserUID(String userUID)
	{
		JPAQuery query = new JPAQuery(entityManager);
		QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;
		QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		QCineDTO qCineDTO = QCineDTO.cineDTO;

		List<CineDTO> cines = query.from(qUsuarioDTO).join(qUsuarioDTO.parSalasUsuario, qSalasUsuarioDTO).join(qSalasUsuarioDTO.parSala, qSalaDTO).join(qSalaDTO.parCine, qCineDTO).where(qUserDTO.usuario.eq(userUID)).list(qCineDTO);

		return Cine.cineDTOToCine(cines.get(0));
	}
}
