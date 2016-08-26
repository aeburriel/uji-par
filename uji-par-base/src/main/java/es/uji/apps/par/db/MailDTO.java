package es.uji.apps.par.db;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the PAR_MAILS database table.
 * 
 */
@Entity
@Table(name = "PAR_MAILS")
public class MailDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "PAR_MAILS_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_MAILS_ID_GENERATOR")
    private long id;

    @Column(name = "PARA")
    private String para;

    @Column(name = "DE")
    private String de;

    @Column(name = "TITULO")
    private String titulo;

    @Column(name = "TEXTO")
    private String texto;

    @Column(name = "FECHA_CREADO")
    private Timestamp fechaCreado;

    @Column(name = "FECHA_ENVIADO")
    private Timestamp fechaEnviado;
    
    @Column(name = "COMPRA_UUID")
    private String uuid;

    @Column(name = "URL_PUBLIC")
    private String urlPublic;

    public MailDTO()
    {
    }

    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getPara()
    {
        return para;
    }

    public void setPara(String para)
    {
        this.para = para;
    }

    public String getDe()
    {
        return de;
    }

    public void setDe(String de)
    {
        this.de = de;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }

    public String getTexto()
    {
        return texto;
    }

    public void setTexto(String texto)
    {
        this.texto = texto;
    }

    public Timestamp getFechaCreado()
    {
        return fechaCreado;
    }

    public void setFechaCreado(Timestamp fechaCreado)
    {
        this.fechaCreado = fechaCreado;
    }

    public Timestamp getFechaEnviado()
    {
        return fechaEnviado;
    }

    public void setFechaEnviado(Timestamp fechaEnviado)
    {
        this.fechaEnviado = fechaEnviado;
    }

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public String getUrlPublic()
    {
        return urlPublic;
    }

    public void setUrlPublic(String urlPublic)
    {
        this.urlPublic = urlPublic;
    }
}