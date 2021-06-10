package es.uji.apps.par.dao;

import com.mysema.query.jpa.impl.JPADeleteClause;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;

import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.db.*;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
	public Usuario getUserById(String userUID)
	{
		JPAQuery query = new JPAQuery(entityManager);

		UsuarioDTO usuarioDTO = query.from(qUserDTO).where(qUserDTO.usuario.eq(userUID)).uniqueResult(qUserDTO);

		return new Usuario(usuarioDTO);
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
		usuarioDTO.setUrl(user.getUrl());

		entityManager.persist(usuarioDTO);

		user.setId(usuarioDTO.getId());
		return user;
	}

	@Transactional
	public Usuario updateUser(Usuario user)
	{
		final JPAUpdateClause update = new JPAUpdateClause(entityManager, qUserDTO);
		update.set(qUserDTO.nombre, user.getNombre()).set(qUserDTO.mail, user.getMail())
				.set(qUserDTO.usuario, user.getUsuario()).set(qUserDTO.password, user.getPassword())
				.set(qUserDTO.readonly, user.isReadonly())
				.where(qUserDTO.id.eq(user.getId())).execute();

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

	public Usuario getUserByServerName(final String serverName)
	{
		final JPAQuery query = new JPAQuery(entityManager);

		final List<UsuarioDTO> users = query.from(qUserDTO)
				.where(qUserDTO.url.eq(serverName))
				.orderBy(qUserDTO.id.asc())
				.list(qUserDTO);

		final Usuario usuario;
		if (users.isEmpty()) {
			usuario = new Usuario();
			usuario.setUsuario("");
		} else {
			usuario = new Usuario(users.get(0));
		}

		return usuario;
	}

	public Cine getUserCineByServerName(final String serverName)
	{
		final List<CineDTO> cines = getCineDTOByServerName(serverName);

		return Cine.cineDTOToCine(cines.get(0));
	}

	public String getApiKeyByServerName(final String serverName)
	{
		final List<CineDTO> cines = getCineDTOByServerName(serverName);

		return cines.get(0).getApiKey();
	}

	private List<CineDTO> getCineDTOByServerName(final String serverName)
	{
		final JPAQuery query = new JPAQuery(entityManager);
		final QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;
		final QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		final QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		final QCineDTO qCineDTO = QCineDTO.cineDTO;

		return query.from(qUsuarioDTO)
				.join(qUsuarioDTO.parSalasUsuario, qSalasUsuarioDTO)
				.join(qSalasUsuarioDTO.parSala, qSalaDTO)
				.join(qSalaDTO.parCine, qCineDTO)
				.where(qUserDTO.url.eq(serverName))
				.orderBy(qCineDTO.id.asc())
				.list(qCineDTO);
	}

	public Cine getUserCineByUserUID(final String userUID)
	{
		final JPAQuery query = new JPAQuery(entityManager);
		final QUsuarioDTO qUsuarioDTO = QUsuarioDTO.usuarioDTO;
		final QSalasUsuarioDTO qSalasUsuarioDTO = QSalasUsuarioDTO.salasUsuarioDTO;
		final QSalaDTO qSalaDTO = QSalaDTO.salaDTO;
		final QCineDTO qCineDTO = QCineDTO.cineDTO;

		final List<CineDTO> cines = query.from(qUsuarioDTO)
				.join(qUsuarioDTO.parSalasUsuario, qSalasUsuarioDTO)
				.join(qSalasUsuarioDTO.parSala, qSalaDTO)
				.join(qSalaDTO.parCine, qCineDTO)
				.where(qUserDTO.usuario.eq(userUID))
				.orderBy(qCineDTO.id.asc())
				.list(qCineDTO);

		return Cine.cineDTOToCine(cines.get(0));
	}

	public Cine getCineByRequest(final HttpServletRequest request) {
		final Cine cine;

		final String user = AuthChecker.getUserUID(request);
		if (user == null) {
			cine = getUserCineByServerName(request.getServerName());
		} else {
			cine = getUserCineByUserUID(user);
		}

		return cine;
	}
}
