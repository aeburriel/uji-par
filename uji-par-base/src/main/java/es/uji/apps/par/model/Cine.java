package es.uji.apps.par.model;

import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.RegistroSerializaException;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

@XmlRootElement
public class Cine implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id;
    private String codigo;
    private String nombre;
    private String cif;
    private String direccion;
    private String codigoMunicipio;
    private String nombreMunicipio;
    private String cp;
    private String empresa;
    private String codigoRegistro;
    private String telefono;
    private BigDecimal iva;
    private String urlPublic;

    public Cine()
    {
    }

    public Cine(String codigo, String nombre, String cif, String direccion, String codigoMunicipio,
            String nombreMunicipio, String cp, String empresa, String codigoRegistro, String telefono, BigDecimal iva)
    {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cif = cif;
        this.direccion = direccion;
        this.codigoMunicipio = codigoMunicipio;
        this.nombreMunicipio = nombreMunicipio;
        this.cp = cp;
        this.empresa = empresa;
        this.codigoRegistro = codigoRegistro;
        this.telefono = telefono;
        this.iva = iva;
    }

    public static CineDTO cineToCineDTO(Cine cine)
    {
        CineDTO cineDTO = new CineDTO();

        cineDTO.setId(cine.getId());
        cineDTO.setCodigo(cine.getCodigo());
        cineDTO.setNombre(cine.getNombre());
        cineDTO.setCif(cine.getCif());
        cineDTO.setDireccion(cine.getDireccion());
        cineDTO.setCodigoMunicipio(cine.getCodigoMunicipio());
        cineDTO.setNombreMunicipio(cine.getNombreMunicipio());
        cineDTO.setCp(cine.getCp());
        cineDTO.setEmpresa(cine.getEmpresa());
        cineDTO.setCodigoRegistro(cine.getCodigoRegistro());
        cineDTO.setTelefono(cine.getTelefono());
        cineDTO.setIva(cine.getIva());
        cineDTO.setUrlPublic(cine.getUrlPublic());

        return cineDTO;
    }

    public static Cine cineDTOToCine(CineDTO cineDTO)
    {
        Cine cine = new Cine();

        cine.setId(cineDTO.getId());
        cine.setCodigo(cineDTO.getCodigo());
        cine.setNombre(cineDTO.getNombre());
        cine.setCif(cineDTO.getCif());
        cine.setDireccion(cineDTO.getDireccion());
        cine.setCodigoMunicipio(cineDTO.getCodigoMunicipio());
        cine.setNombreMunicipio(cineDTO.getNombreMunicipio());
        cine.setCp(cineDTO.getCp());
        cine.setEmpresa(cineDTO.getEmpresa());
        cine.setCodigoRegistro(cineDTO.getCodigoRegistro());
        cine.setTelefono(cineDTO.getTelefono());
        cine.setIva(cineDTO.getIva());
        cine.setUrlPublic(cineDTO.getUrlPublic());

        return cine;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String getCif()
    {
        return cif;
    }

    public void setCif(String cif)
    {
        this.cif = cif;
    }

    public String getDireccion()
    {
        return direccion;
    }

    public void setDireccion(String direccion)
    {
        this.direccion = direccion;
    }

    public String getCodigoMunicipio()
    {
        return codigoMunicipio;
    }

    public void setCodigoMunicipio(String codigoMunicipio)
    {
        this.codigoMunicipio = codigoMunicipio;
    }

    public String getNombreMunicipio()
    {
        return nombreMunicipio;
    }

    public void setNombreMunicipio(String nombreMunicipio)
    {
        this.nombreMunicipio = nombreMunicipio;
    }

    public String getCp()
    {
        return cp;
    }

    public void setCp(String cp)
    {
        this.cp = cp;
    }

    public String getEmpresa()
    {
        return empresa;
    }

    public void setEmpresa(String empresa)
    {
        this.empresa = empresa;
    }

    public String getCodigoRegistro()
    {
        return codigoRegistro;
    }

    public void setCodigoRegistro(String codigoRegistro)
    {
        this.codigoRegistro = codigoRegistro;
    }

    public String getTelefono()
    {
        return telefono;
    }

    public void setTelefono(String telefono)
    {
        this.telefono = telefono;
    }

    public BigDecimal getIva()
    {
        return iva;
    }

    public void setIva(BigDecimal iva)
    {
        this.iva = iva;
    }

    public String getUrlPublic()
    {
        return urlPublic;
    }

    public void setUrlPublic(String urlPublic)
    {
        this.urlPublic = urlPublic;
    }

    public static void checkValidity(String codi) throws RegistroSerializaException {
    	if (codi == null)
            throw new RegistroSerializaException(GeneralPARException.CODIGO_CINE_NULO_CODE);
    	
    	if (codi.length() != 3)
            throw new RegistroSerializaException(GeneralPARException.FORMATO_CODIGO_CINE_INCORRECTO_CODE);
    }
}