package es.uji.apps.par.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import es.uji.apps.par.db.CompraDTO;

@XmlRootElement
public class Compra
{
    private long id;
    private String nombre;
    private String apellidos;
    private String direccion;
    private String poblacion;
    private String cp;
    private String provincia;
    private String telefono;
    private String email;
    private boolean infoPeriodica;
    private boolean reserva;
    private boolean pagada;
    private boolean taquilla;
    private List<Butaca> parButacas;
    private Date fecha;
    private Float importe;
    private boolean anulada;
    private Date desde;
    private Date hasta;
    private String uuid;
	private String tipo;
    private String observacionesReserva;
    private boolean caducada;
    private String idDevolucion;
    private BigDecimal porcentajeIVA;

    public Compra(CompraDTO compraDTO)
    {
        this.setId(compraDTO.getId());
        this.setNombre(compraDTO.getNombre());
        this.setApellidos(compraDTO.getApellidos());
        this.setTelefono(compraDTO.getTelefono());
        this.setEmail(compraDTO.getEmail());
        this.setPagada(compraDTO.getPagada());
        this.setReserva(compraDTO.getReserva());
        this.setFecha(compraDTO.getFecha());
        this.setTaquilla(compraDTO.getTaquilla());
        this.setImporte(compraDTO.getImporte().floatValue());
        this.setUuid(compraDTO.getUuid());
		this.setTipo(compraDTO.getTipoPago());
		this.setDireccion(compraDTO.getDireccion());
		this.setPoblacion(compraDTO.getPoblacion());
		this.setCp(compraDTO.getCp());
		this.setProvincia(compraDTO.getProvincia());
		this.setInfoPeriodica(compraDTO.getInfoPeriodica());
		this.setPorcentajeIVA(compraDTO.getPorcentajeIva());

        if (compraDTO.getAnulada() != null)
        	this.setAnulada(compraDTO.getAnulada());
        
        this.setDesde(compraDTO.getDesde());
        this.setHasta(compraDTO.getHasta());
        this.setObservacionesReserva(compraDTO.getObservacionesReserva());
        
        if (compraDTO.getCaducada() != null)
            this.setCaducada(compraDTO.getCaducada());
        
        if (compraDTO.getCodigoPagoPasarela()!=null)
            this.setIdDevolucion(compraDTO.getCodigoPagoPasarela());
        else if (compraDTO.getCodigoPagoTarjeta()!=null)
            this.setIdDevolucion(compraDTO.getCodigoPagoTarjeta());
		else if (compraDTO.getReferenciaPago()!=null)
			this.setIdDevolucion(compraDTO.getReferenciaPago());
    }

    public static Compra compraDTOtoCompra(CompraDTO compraDTO)
    {
        return new Compra(compraDTO);
    }

    public long getId()
    {
        return this.id;
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

    public String getApellidos()
    {
        return apellidos;
    }

    public void setApellidos(String apellidos)
    {
        this.apellidos = apellidos;
    }

    public String getTelefono()
    {
        return telefono;
    }

    public void setTelefono(String telefono)
    {
        this.telefono = telefono;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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

    public boolean isInfoPeriodica() {
        return infoPeriodica;
    }

    public void setInfoPeriodica(boolean infoPeriodica) {
        this.infoPeriodica = infoPeriodica;
    }

    public List<Butaca> getParButacas()
    {
        return parButacas;
    }

    public void setParButacas(List<Butaca> parButacas)
    {
        this.parButacas = parButacas;
    }

    public boolean isReserva()
    {
        return reserva;
    }

    public void setReserva(boolean reserva)
    {
        this.reserva = reserva;
    }

    public boolean isPagada()
    {
        return pagada;
    }

    public void setPagada(boolean pagada)
    {
        this.pagada = pagada;
    }

    public Date getFecha()
    {
        return fecha;
    }

    public void setFecha(Date fecha)
    {
        this.fecha = fecha;
    }

    public boolean isTaquilla()
    {
        return taquilla;
    }

    public void setTaquilla(boolean taquilla)
    {
        this.taquilla = taquilla;
    }

	public Float getImporte() {
		return importe;
	}

	public void setImporte(Float importe) {
		this.importe = importe;
	}

	public boolean isAnulada() {
		return anulada;
	}

	public void setAnulada(boolean anulada) {
		this.anulada = anulada;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public String getObservacionesReserva() {
		return observacionesReserva;
	}

	public void setObservacionesReserva(String observacionesReserva) {
		this.observacionesReserva = observacionesReserva;
	}

    public boolean isCaducada() {
        return caducada;
    }

    public void setCaducada(boolean caducada) {
        this.caducada = caducada;
    }

    public String getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(String idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

	public String getTipo()
	{
		return tipo;
	}

	public void setTipo(String tipo)
	{
		this.tipo = tipo;
	}

    public BigDecimal getPorcentajeIVA() {
        return porcentajeIVA;
    }

    public void setPorcentajeIVA(BigDecimal porcentajeIVA) {
        this.porcentajeIVA = porcentajeIVA;
    }
}