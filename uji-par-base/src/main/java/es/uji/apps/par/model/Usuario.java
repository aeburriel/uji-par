package es.uji.apps.par.model;

import es.uji.apps.par.db.UsuarioDTO;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Usuario
{
    private long id;
    private String nombre;
    private String mail;
    private String usuario;
    private String url;

    public Usuario()
    {
    }

    public Usuario(UsuarioDTO usuarioDTO)
    {
        this.id = usuarioDTO.getId();
        this.nombre = usuarioDTO.getNombre();
        this.mail = usuarioDTO.getMail();
        this.usuario = usuarioDTO.getUsuario();
        this.url = usuarioDTO.getUrl();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getMail()
    {
        return mail;
    }

    public void setMail(String mail)
    {
        this.mail = mail;
    }

    public String getUsuario()
    {
        return usuario;
    }

    public void setUsuario(String usuario)
    {
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
}