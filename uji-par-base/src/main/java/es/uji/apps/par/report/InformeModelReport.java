package es.uji.apps.par.report;

import es.uji.apps.par.enums.TipoPago;
import es.uji.apps.par.exceptions.AnticipadaFormatException;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.enums.TipoVenta;
import es.uji.apps.par.utils.DateUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

public class InformeModelReport {
    private String evento;
    private String sesion;
    private String fechaCompra;
    private String tipoEntrada;
    private int numeroEntradas;
    private BigDecimal numeroEntradasTPV;
    private BigDecimal numeroEntradasEfectivo;
    private BigDecimal numeroEntradasTransferencia;
    private BigDecimal numeroEntradasOnline;
    private BigDecimal total;
    private String tipoCompra;
    private TipoVenta tipoVenta;
    private TipoPago tipoPago;
    private BigDecimal iva;
    private String localizacion;
    private BigDecimal aforo;
    private long eventoId;
    private long sesionId;
    private Integer vendidasTPV;
    private Integer vendidasMetalico;
    private Integer canceladasTaquilla;
    private Integer vendidasTaquilla;
    private Boolean anulada;
	private Boolean isReserva;

    public InformeModelReport() {
    }

    public BigDecimal getNumeroEntradasTPV() {
        return numeroEntradasTPV;
    }

    public void setNumeroEntradasTPV(BigDecimal numeroEntradasTPV) {
        this.numeroEntradasTPV = numeroEntradasTPV;
    }

    public BigDecimal getNumeroEntradasEfectivo() {
        return numeroEntradasEfectivo;
    }

    public void setNumeroEntradasEfectivo(BigDecimal numeroEntradasEfectivo) {
        this.numeroEntradasEfectivo = numeroEntradasEfectivo;
    }

    public BigDecimal getNumeroEntradasOnline() {
        return numeroEntradasOnline;
    }

    public BigDecimal getNumeroEntradasTransferencia()
    {
        return numeroEntradasTransferencia;
    }

    public void setNumeroEntradasTransferencia(BigDecimal numeroEntradasTransferencia)
    {
        this.numeroEntradasTransferencia = numeroEntradasTransferencia;
    }

    public void setNumeroEntradasOnline(BigDecimal numeroEntradasOnline) {
        this.numeroEntradasOnline = numeroEntradasOnline;
    }

    public BigDecimal getAforo() {
        return aforo;
    }

    public TipoVenta getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(TipoVenta tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public void setAforo(BigDecimal aforo) {
        this.aforo = aforo;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getSesion() {
        return sesion;
    }

    public void setSesion(String sesion) {
        this.sesion = sesion;
    }

    public String getTipoEntrada() {
        return tipoEntrada;
    }

    public void setTipoEntrada(String tipoEntrada) {
        this.tipoEntrada = tipoEntrada;
    }

    public int getNumeroEntradas() {
        return numeroEntradas;
    }

    public void setNumeroEntradas(int numeroEntradas) {
        this.numeroEntradas = numeroEntradas;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTipoCompra() {
        return tipoCompra;
    }

    public void setTipoCompra(String tipoCompra) {
        this.tipoCompra = tipoCompra;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public String getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(String localizacion) {
        this.localizacion = localizacion;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public long getEventoId() {
        return eventoId;
    }

    public void setEventoId(long eventoId) {
        this.eventoId = eventoId;
    }

    public long getSesionId() {
        return sesionId;
    }

    public void setSesionId(long sesionId) {
        this.sesionId = sesionId;
    }

    public Integer getVendidasTPV() {
        return vendidasTPV;
    }

    public void setVendidasTPV(Integer vendidasTPV) {
        this.vendidasTPV = vendidasTPV;
    }

    public Integer getVendidasMetalico() {
        return vendidasMetalico;
    }

    public void setVendidasMetalico(Integer vendidasMetalico) {
        this.vendidasMetalico = vendidasMetalico;
    }

    public Integer getCanceladasTaquilla() {
        return canceladasTaquilla;
    }

    public void setCanceladasTaquilla(Integer canceladasTaquilla) {
        this.canceladasTaquilla = canceladasTaquilla;
    }

    public Integer getVendidasTaquilla() {
        return vendidasTaquilla;
    }

    public void setVendidasTaquilla(Integer vendidasTaquilla) {
        this.vendidasTaquilla = vendidasTaquilla;
    }

    public TipoPago getTipoPago()
    {
        return tipoPago;
    }

    public void setTipoPago(TipoPago tipoPago)
    {
        this.tipoPago = tipoPago;
    }

    public static TipoVenta getTipoVentaPorDia(Timestamp fechaInicioSesion, Timestamp fechaCompraSesion, Integer dias) {
        Calendar fechaInicioSesionCalendar = Calendar.getInstance();
        fechaInicioSesionCalendar.setTimeInMillis(fechaInicioSesion.getTime());

        Calendar fechaCompraSesionCalendar = Calendar.getInstance();
        fechaCompraSesionCalendar.setTimeInMillis(fechaCompraSesion.getTime());

        fechaInicioSesionCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fechaInicioSesionCalendar.set(Calendar.MINUTE, 0);
        fechaInicioSesionCalendar.set(Calendar.SECOND, 0);
        fechaInicioSesionCalendar.set(Calendar.MILLISECOND, 0);

        fechaCompraSesionCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fechaCompraSesionCalendar.set(Calendar.MINUTE, 0);
        fechaCompraSesionCalendar.set(Calendar.SECOND, 0);
        fechaCompraSesionCalendar.set(Calendar.MILLISECOND, 0);

        long diferenciaMilis = fechaInicioSesionCalendar.getTimeInMillis() - fechaCompraSesionCalendar.getTimeInMillis();
        long diferenciaDias = diferenciaMilis / (24 * 60 * 60 * 1000);

        if (diferenciaDias >= dias) {
            return TipoVenta.FISICA_ANTICIPADA;
        } else {
            return TipoVenta.FISICA_TAQUILLA;
        }
    }

    public static TipoVenta getTipoVentaPorHoras(Timestamp fechaInicioSesion, Timestamp fechaCompraSesion, Integer horas) {
        Calendar fechaInicioSesionCalendar = Calendar.getInstance();
        fechaInicioSesionCalendar.setTimeInMillis(fechaInicioSesion.getTime());

        Calendar fechaCompraSesionCalendar = Calendar.getInstance();
        fechaCompraSesionCalendar.setTimeInMillis(fechaCompraSesion.getTime());

        long diferenciaMilis = fechaInicioSesionCalendar.getTimeInMillis() - fechaCompraSesionCalendar.getTimeInMillis();
        long diferenciaHoras = diferenciaMilis / (60 * 60 * 1000);

        if (diferenciaHoras >= horas) {
            return TipoVenta.FISICA_ANTICIPADA;
        } else {
            return TipoVenta.FISICA_TAQUILLA;
        }
    }

    public static TipoVenta getTipoVenta(Boolean taquilla, Timestamp fechaInicioSesion, Timestamp fechaCompra, String anticipada) {
        if (!taquilla) {
            return TipoVenta.TPV;
        } else {
            if (anticipada != null) {
                if (anticipada.matches("\\d+d")) {
                    int dias = Integer.parseInt(anticipada.substring(0, anticipada.length() - 1));
                    return getTipoVentaPorDia(fechaInicioSesion, fechaCompra, dias);
                } else if (anticipada.matches("\\d+")) {
                    return getTipoVentaPorHoras(fechaInicioSesion, fechaCompra, Integer.parseInt(anticipada));
                } else {
                    throw new AnticipadaFormatException(anticipada);
                }
            } else {
                throw new AnticipadaFormatException(anticipada);
            }
        }
    }

    public static InformeModelReport fromButaca(ButacaDTO butaca, String anticipada, boolean localizacionEnValenciano) {
        InformeModelReport informeModel = new InformeModelReport();
        informeModel.setSesion(DateUtils.dateToSpanishStringWithHour(butaca.getParSesion().getFechaCelebracion()).toString());
        informeModel.setTipoEntrada(butaca.getTipo());
        informeModel.setNumeroEntradas(1);
        informeModel.setFechaCompra(DateUtils.dateToSpanishString(butaca.getParCompra().getFecha()));
        informeModel.setTotal(butaca.getPrecio());
        informeModel.setLocalizacion((localizacionEnValenciano)?butaca.getParLocalizacion().getNombreVa():butaca
				.getParLocalizacion().getNombreEs());
        informeModel.setAnulada(butaca.getAnulada());
        informeModel.setAforo(butaca.getParLocalizacion().getTotalEntradas());
		informeModel.setReserva(butaca.getParCompra().getReserva());

        if (butaca.getParCompra().getTipoPago() != null)
            informeModel.setTipoPago(TipoPago.valueOf(butaca.getParCompra().getTipoPago().toUpperCase()));

        if (anticipada != null)
            informeModel.setTipoVenta(getTipoVenta(butaca.getParCompra().getTaquilla(), butaca.getParSesion().getFechaCelebracion
					(), butaca.getParCompra().getFecha(), anticipada));
		else {
			String codigoPagoTarjeta = (butaca.getParCompra().getCodigoPagoTarjeta() != null) ? butaca.getParCompra()
					.getCodigoPagoTarjeta() : (butaca.getParCompra().getReferenciaPago() != null) ? butaca.getParCompra()
					.getReferenciaPago(): null;
			informeModel.setTipoVenta(getTipoVenta(butaca.getParCompra().getTaquilla(), codigoPagoTarjeta));
		}

        return informeModel;
    }

	private static TipoVenta getTipoVenta(boolean taquilla, String codigoPagoTarjeta) {
		if (!taquilla)
			return TipoVenta.TPV;
		else if (codigoPagoTarjeta != null && !codigoPagoTarjeta.trim().equals(""))
			return TipoVenta.FISICA_TPV;
		else
			return TipoVenta.FISICA_TAQUILLA;
	}

	public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

	public Boolean getReserva() {
		return isReserva;
	}

	public void setReserva(Boolean reserva) {
		isReserva = reserva;
	}
}
