package es.uji.apps.par.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.CampoRequeridoException;
import es.uji.apps.par.GeneralPARException;
import es.uji.apps.par.UsuarioYaExisteException;
import es.uji.apps.par.dao.UsuariosDAO;
import es.uji.apps.par.model.Usuario;

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
}
