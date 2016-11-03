package es.uji.apps.par.db;

import es.uji.apps.par.enums.TipoPago;
import es.uji.apps.par.model.Abonado;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the PAR_COMPRAS database table.
 * 
 */
@Entity
@Table(name = "PAR_COMPRAS")
public class CompraDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "PAR_COMPRAS_ID_GENERATOR", sequenceName = "HIBERNATE_SEQUENCE")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PAR_COMPRAS_ID_GENERATOR")
	private long id;

	@Column(name = "NOMBRE")
	private String nombre;

	@Column(name = "APELLIDOS")
	private String apellidos;
	
    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "POBLACION")
    private String poblacion;

    @Column(name = "CP")
    private String cp;

    @Column(name = "PROVINCIA")
    private String provincia;

	@Column(name = "TFNO")
	private String telefono;

	@Column(name = "EMAIL")
	private String email;
	
    @Column(name = "INFO_PERIODICA")
    private Boolean infoPeriodica;	

	@Column(name = "FECHA")
	private Timestamp fecha;
	
	@Column(name = "TAQUILLA")
	private Boolean taquilla;
	
	@Column(name = "IMPORTE")
	private BigDecimal importe;
	
	@Column(name = "REFERENCIA_PAGO")
	private String referenciaPago;

	@Column(name = "TIPO")
	private String tipoPago;
	
    @ManyToOne
    @JoinColumn(name="SESION_ID")
    private SesionDTO parSesion;

	// bi-directional many-to-one association to ButacaDTO
	@OneToMany(mappedBy = "parCompra", fetch=FetchType.EAGER)
	private List<ButacaDTO> parButacas;
	
    @Column(name = "CODIGO_PAGO_TARJETA")
    private String codigoPagoTarjeta;	
    
    @Column(name = "CODIGO_PAGO_PASARELA")
    private String codigoPagoPasarela;
    
    @Column(name = "PAGADA")
    private Boolean pagada;
    
    @Column(name = "UUID")
    private String uuid;   
    
    @Column(name = "RESERVA")
    private Boolean reserva;    
    
    @Column(name = "DESDE")
    private Timestamp desde;
    
    @Column(name = "HASTA")
    private Timestamp hasta;    
    
    @Column(name = "OBSERVACIONES_RESERVA")
    private String observacionesReserva;
    
    @Column(name = "ANULADA")
    private Boolean anulada;
    
    @Column(name = "RECIBO_PINPAD")
    private String reciboPinpad;
    
    @Column(name = "CADUCADA")
    private Boolean caducada;

    @ManyToOne
    @JoinColumn(name="ABONADO_ID")
    private AbonadoDTO parAbonado;
    
	public CompraDTO()
    {
    }

	public CompraDTO(SesionDTO sesion, Timestamp fecha, Boolean taquilla, BigDecimal importe, String uuid) {
		this.parSesion = sesion;
		this.fecha = fecha;
		this.taquilla = taquilla;
        this.importe = importe;
        this.pagada = false;
        this.uuid = uuid;
        this.infoPeriodica = false;
        this.reserva = false;
        this.anulada = false;
        this.caducada = false;
	}

    public CompraDTO(SesionDTO sesion, Timestamp fecha, Boolean taquilla, BigDecimal importe, String uuid, String email, String nombre, String apellidos) {
        this.parSesion = sesion;
        this.fecha = fecha;
        this.taquilla = taquilla;
        this.importe = importe;
        this.pagada = false;
        this.uuid = uuid;
        this.infoPeriodica = false;
        this.reserva = false;
        this.anulada = false;
        this.caducada = false;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public CompraDTO(SesionDTO sesion, Timestamp fecha, Boolean taquilla, BigDecimal importe, String uuid, Abonado abonado) {
        this.parSesion = sesion;
        this.fecha = fecha;
        this.taquilla = taquilla;
        this.importe = importe;
        this.pagada = false;
        this.uuid = uuid;
        this.reserva = false;
        this.anulada = false;
        this.caducada = false;

        this.nombre = abonado.getNombre();
        this.apellidos = abonado.getApellidos();
        this.telefono = abonado.getTelefono();
        this.email = abonado.getEmail();
        this.direccion = abonado.getDireccion();
        this.poblacion = abonado.getPoblacion();
        this.cp = abonado.getCp();
        this.provincia = abonado.getProvincia();
        this.infoPeriodica = abonado.getInfoPeriodica();
        this.parAbonado = new AbonadoDTO(abonado.getId());
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

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Boolean getInfoPeriodica() {
        return infoPeriodica;
    }

    public void setInfoPeriodica(Boolean infoPeriodica) {
        this.infoPeriodica = infoPeriodica;
    }

    public Timestamp getFecha() {
		return fecha;
	}

	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}

	public List<ButacaDTO> getParButacas() {
		return parButacas;
	}

	public void setParButacas(List<ButacaDTO> parButacas) {
		this.parButacas = parButacas;
	}

    public Boolean getTaquilla() {
        return taquilla;
    }

    public void setTaquilla(Boolean taquilla) {
        this.taquilla = taquilla;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public SesionDTO getParSesion() {
        return parSesion;
    }

    public void setParSesion(SesionDTO parSesion) {
        this.parSesion = parSesion;
    }

    public String getCodigoPagoTarjeta() {
        return codigoPagoTarjeta;
    }

    public void setCodigoPagoTarjeta(String codigoPagoTarjeta) {
        this.codigoPagoTarjeta = codigoPagoTarjeta;
    }

    public Boolean getPagada() {
        return pagada;
    }

    public void setPagada(Boolean pagada) {
        this.pagada = pagada;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCodigoPagoPasarela() {
        return codigoPagoPasarela;
    }

    public void setCodigoPagoPasarela(String codigoPagoPasarela) {
        this.codigoPagoPasarela = codigoPagoPasarela;
    }

    public Boolean getReserva() {
        return reserva;
    }

    public void setReserva(Boolean reserva) {
        this.reserva = reserva;
    }

    public Timestamp getDesde() {
        return desde;
    }

    public void setDesde(Timestamp desde) {
        this.desde = desde;
    }

    public Timestamp getHasta() {
        return hasta;
    }

    public void setHasta(Timestamp hasta) {
        this.hasta = hasta;
    }

    public String getObservacionesReserva() {
        return observacionesReserva;
    }

    public void setObservacionesReserva(String observacionesReserva) {
        this.observacionesReserva = observacionesReserva;
    }

	public Boolean getAnulada() {
		return anulada;
	}

	public void setAnulada(Boolean anulada) {
		this.anulada = anulada;
	}

    public String getReciboPinpad() {
        return reciboPinpad;
    }

    public void setReciboPinpad(String reciboPinpad) {
        this.reciboPinpad = reciboPinpad;
    }

    public Boolean getCaducada() {
        return caducada;
    }

    public void setCaducada(Boolean caducada) {
        this.caducada = caducada;
    }

	public String getReferenciaPago() {
		return referenciaPago;
	}

	public void setReferenciaPago(String referenciaPago) {
		this.referenciaPago = referenciaPago;
	}

	public String getTipoPago()
	{
		return tipoPago;
	}

	public void setTipoPago(String tipoPago)
	{
		this.tipoPago = tipoPago;
	}

	public AbonadoDTO getParAbonado() {
        return parAbonado;
    }

    public void setParAbonado(AbonadoDTO parAbonado) {
        this.parAbonado = parAbonado;
    }

    public static CompraDTO resultsetToDTO(ResultSet res) throws SQLException {
		CompraDTO compra = new CompraDTO();
		compra.setCaducada(res.getBoolean("caducada"));
		compra.setId(res.getLong("id"));
		compra.setUuid(res.getString("uuid"));
		compra.setEmail(res.getString("email"));
        compra.setPagada(res.getBoolean("pagada"));

		return compra;
	}
}