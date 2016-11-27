package es.uji.apps.par.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


/**
 * The persistent class for the PAR_BUTACAS database table.
 * 
 */
@Entity
@Table(name="PAR_BUTACAS", uniqueConstraints={@UniqueConstraint(columnNames={"SESION_ID", "LOCALIZACION_ID", "FILA", "NUMERO"})})
public class ButacaDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="PAR_BUTACAS_ID_GENERATOR", sequenceName="par_butacas_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PAR_BUTACAS_ID_GENERATOR")
	private long id;

	@Column(name="FILA")
	private String fila;

	@Column(name="NUMERO")
	private String numero;
	   
	@Column(name="PRECIO")
	private BigDecimal precio;

    //bi-directional many-to-one association to SesionDTO
    @ManyToOne
    @JoinColumn(name="SESION_ID")
    private SesionDTO parSesion;

    //bi-directional many-to-one association to LocalizacionDTO
    @ManyToOne
    @JoinColumn(name="LOCALIZACION_ID")
    private LocalizacionDTO parLocalizacion;
    
    //bi-directional many-to-one association to CompraDTO
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="COMPRA_ID")
    private CompraDTO parCompra;
    
    @Column(name="TIPO")
    private String tipo;
    
    @Column(name = "ANULADA")
    private Boolean anulada;
    
    @Column(name = "PRESENTADA")
    private Date presentada;

    @Column(name = "ID_ENTRADA")
    private Integer idEntrada;
    
	public ButacaDTO() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

    public String getFila()
    {
        return fila;
    }

    public void setFila(String fila)
    {
        this.fila = fila;
    }

    public String getNumero()
    {
        return numero;
    }

    public void setNumero(String numero)
    {
        this.numero = numero;
    }

    public BigDecimal getPrecio()
    {
        return precio;
    }

    public void setPrecio(BigDecimal precio)
    {
        this.precio = precio;
    }

    public SesionDTO getParSesion()
    {
        return parSesion;
    }

    public void setParSesion(SesionDTO parSesion)
    {
        this.parSesion = parSesion;
    }
    
    public LocalizacionDTO getParLocalizacion()
    {
        return parLocalizacion;
    }

    public void setParLocalizacion(LocalizacionDTO parLocalizacion)
    {
        this.parLocalizacion = parLocalizacion;
    }    

    public CompraDTO getParCompra()
    {
        return parCompra;
    }

    public void setParCompra(CompraDTO parCompra)
    {
        this.parCompra = parCompra;
    }

    public String getTipo()
    {
        return tipo;
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

	public Boolean getAnulada() {
		return anulada;
	}

	public void setAnulada(Boolean anulada) {
		this.anulada = anulada;
	}

    public Date getPresentada() {
        return presentada;
    }

    public void setPresentada(Date presentada) {
        this.presentada = presentada;
    }

    public Integer getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Integer idEntrada) {
        this.idEntrada = idEntrada;
    }
}