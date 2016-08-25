package es.uji.apps.par.services;

import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.UsuarioYaExisteException;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService
{
    @Autowired
    private UsuariosDAO usuariosDAO;

    public List<Usuario> getUsuarios(String sortParameter, int start, int limit)
    {
        return usuariosDAO.getUsers(sortParameter, start, limit);
    }

    public void removeUser(Integer id)
    {
        usuariosDAO.removeUser(id);
    }

    public Usuario addUser(Usuario user) throws GeneralPARException
    {
        checkRequiredFields(user);

        if (usuariosDAO.userExists(user))
            throw new UsuarioYaExisteException();
        else
            return usuariosDAO.addUser(user);
    }

    private void checkRequiredFields(Usuario user) throws CampoRequeridoException
    {
        if (user.getMail() == null || user.getMail().isEmpty())
            throw new CampoRequeridoException("Mail");
        if (user.getNombre() == null || user.getNombre().isEmpty())
            throw new CampoRequeridoException("Nombre");
        if (user.getUsuario() == null || user.getUsuario().isEmpty())
            throw new CampoRequeridoException("Usuario");
    }

    public void updateUser(Usuario user) throws CampoRequeridoException
    {
        checkRequiredFields(user);
        usuariosDAO.updateUser(user);
    }

	public int getTotalUsuarios() {
		return usuariosDAO.getTotalUsuarios();
	}

    public Usuario getUserByServerName(String serverName)
    {
        return usuariosDAO.getUserByServerName(serverName);
    }

    public Cine getUserCineByServerName(String serverName)
    {
        return usuariosDAO.getUserCineByServerName(serverName);
    }

    public Cine getUserCineByUserUID(String userUID)
    {
        return usuariosDAO.getUserCineByUserUID(userUID);
    }

    public Usuario getUserById(String userUID)
    {
        return usuariosDAO.getUserById(userUID);
    }
}
