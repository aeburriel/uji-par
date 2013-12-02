package es.uji.apps.par.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * The persistent class for the PAR_CINES database table.
 */
@Entity
@Table(name = "PAR_CINES")
public class CineDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "PAR_CINES_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_CINES_ID_GENERATOR")
	private long id;

	@Column(name = "CODIGO")
	private String codigo;

	@Column(name = "NOMBRE")
	private String nombre;
	
    @Column(name = "CIF")
    private String cif;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "COD_MUNICIPIO")
    private String codigoMunicipio;

	@Column(name = "NOM_MUNICIPIO")
	private String nombreMunicipio;

	@Column(name = "CP")
	private String cp;
	
    @Column(name = "EMPRESA")
    private String empresa;	
    
    @Column(name = "COD_REGISTRO")
    private String codigoRegistro; 
    
    @Column(name = "TFNO")
    private String telefono;
    
    @Column(name = "IVA")
    private BigDecimal iva;

	@OneToMany(mappedBy = "parCine", fetch=FetchType.LAZY)
	private List<SalaDTO> parSalas;
    
	public CineDTO()
    {
    }
	
	public CineDTO(String codigo, String nombre, String cif, String direccion,
            String codigoMunicipio, String nombreMunicipio, String cp, String empresa, String codigoRegistro,
            String telefono, BigDecimal iva)
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

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	
	public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public String getCif()
    {
        return cif;
    }

    public void setCif(String cif)
    {
        this.cif = cif;
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

    public List<SalaDTO> getParSalas()
    {
        return parSalas;
    }

    public void setParSalas(List<SalaDTO> parSalas)
    {
        this.parSalas = parSalas;
    }
}