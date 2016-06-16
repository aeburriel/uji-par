package es.uji.apps.par.services;

import com.mysema.query.Tuple;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.AbonadosDAO;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.AbonadoDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.exceptions.*;
import es.uji.apps.par.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class ComprasService
{
    @Autowired
    private ComprasDAO comprasDAO;

    @Autowired
    private AbonadosDAO abonadosDAO;

    @Autowired
    private ButacasDAO butacasDAO;

    @Autowired
    private SesionesService sesionesService;

	@Autowired
	private SesionesDAO sesionesDAO;

    @Autowired
    private AbonosService abonosService;

	@Autowired
	Configuration configuration;

    public ResultadoCompra registraCompraTaquilla(Long sesionId, List<Butaca> butacasSeleccionadas, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        return registraCompra(sesionId, butacasSeleccionadas, true, userUID);
    }

    public List<CompraYUso> getComprasYPresentadas(long sesionId) {
        List<CompraYUso> comprasPresentadas = new ArrayList<CompraYUso>();

        List<Tuple> resultados = comprasDAO.getComprasYPresentadas(sesionId);
        for (Tuple resultado : resultados) {
            String email = resultado.get(0, String.class);
            long compras = resultado.get(1, Long.class);
            long presentadas = resultado.get(2, Long.class);

            comprasPresentadas.add(new CompraYUso(email, compras, presentadas));
        }

        return comprasPresentadas;
    }

    @Transactional(rollbackForClassName={"CompraButacaDescuentoNoDisponible","FueraDePlazoVentaInternetException",
    		"NoHayButacasLibresException","ButacaOcupadaException","CompraSinButacasException"})
    public ResultadoCompra realizaCompraInternet(Long sesionId, List<Butaca> butacasSeleccionadas, String uuidCompraActual, String userUID) throws Exception
    {
        Sesion sesion = sesionesService.getSesion(sesionId, userUID);
        Evento evento = sesion.getEvento();
        Map<String, Map<Long, PreciosSesion>> preciosPorZona = sesionesService.getPreciosSesionPublicosPorLocalizacion(sesionId, userUID);

        if (!sesion.getEnPlazoVentaInternet())
            throw new FueraDePlazoVentaInternetException(sesionId);
        
        for (Butaca butaca : butacasSeleccionadas)
        {
        	Map<Long, PreciosSesion> mapaTarifasPrecios = preciosPorZona.get(butaca.getLocalizacion());
        	
        	//TODO -> Con los cambios de ahora, esta parte igual no funciona para la UJI (ya que solamente lo usa ella)
        	if (butaca.getTipo() != null)
        		if (esButacaDescuentoNoDisponible(butaca.getTipo(), evento, mapaTarifasPrecios.get(butaca.getTipo())))
        			throw new CompraButacaDescuentoNoDisponible();
        }
        
        // Si teníamos una compra en marcha la eliminamos (puede pasar cuando intentamos pagar con tarjeta y volvemos atrás)        
        if (uuidCompraActual != null && !uuidCompraActual.equals(""))
            comprasDAO.borrarCompraNoPagada(uuidCompraActual);

        return registraCompra(sesionId, butacasSeleccionadas, false, userUID);
    }

    
	// Intentamos comprar una butaca de tipo descuento pero el descuento no está disponible (casos: descuento 0 ó cine/teatro con precio < 8 €)
    public boolean esButacaDescuentoNoDisponible(String tipoButaca, Evento evento, PreciosSesion precioLocalizacion)
    {
        return tipoButaca != null && tipoButaca.equals("descuento") && 
        		(descuentoCero(precioLocalizacion) || cineTeatroMenorDe(evento, precioLocalizacion, new BigDecimal(8)));
    }

    private boolean descuentoCero(PreciosSesion precioSesion)
    {
        return precioSesion.getDescuento() == null || precioSesion.getDescuento().equals(BigDecimal.ZERO);
    }
    
    private boolean cineTeatroMenorDe(Evento evento, PreciosSesion precioSesion, BigDecimal descuentoLimite)
    {
        String tipoEvento = evento.getParTiposEvento().getNombreEs().toLowerCase();
        
        return (tipoEvento.equals("cine") || tipoEvento.equals("teatro")) && precioSesion.getPrecio().compareTo(descuentoLimite) < 0;
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException",
    		"CompraSinButacasException","IncidenciaNotFoundException"})
    private synchronized ResultadoCompra registraCompra(Long sesionId, List<Butaca> butacasSeleccionadas, boolean taquilla, CompraDTO compraDTO, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        ResultadoCompra resultadoCompra = new ResultadoCompra();
        butacasDAO.reservaButacas(sesionId, compraDTO, butacasSeleccionadas, userUID);

        resultadoCompra.setCorrecta(true);
        resultadoCompra.setId(compraDTO.getId());
        resultadoCompra.setUuid(compraDTO.getUuid());

        if (taquilla) {
            SesionDTO sesionDTO = sesionesDAO.getSesion(sesionId, userUID);
            long totalAnuladas = sesionesDAO.getButacasAnuladasTotal(sesionId);
            boolean isVentaDegradada = configuration.isDataDegradada(sesionDTO.getFechaCelebracion());
            boolean isReprogramada = sesionesDAO.isSesionReprogramada(sesionDTO.getFechaCelebracion(),
                    sesionDTO.getParSala().getId(), sesionId, userUID);

            int tipoIncidenciaId = sesionesDAO.getTipoIncidenciaSesion(totalAnuladas, isVentaDegradada, isReprogramada);
            sesionesDAO.setIncidencia(sesionId, tipoIncidenciaId);
        }

        return resultadoCompra;
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException",
            "CompraSinButacasException","IncidenciaNotFoundException"})
    private synchronized ResultadoCompra registraCompra(Long sesionId, List<Butaca> butacasSeleccionadas, boolean taquilla, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        if (butacasSeleccionadas.size() == 0)
            throw new CompraSinButacasException();

        BigDecimal importe = calculaImporteButacas(sesionId, butacasSeleccionadas, taquilla, userUID);
        CompraDTO compraDTO = comprasDAO.insertaCompra(sesionId, new Date(), taquilla, importe, userUID);

        return registraCompra(sesionId, butacasSeleccionadas, taquilla, compraDTO, userUID);
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException",
            "CompraSinButacasException","IncidenciaNotFoundException"})
    public synchronized ResultadoCompra registraCompra(Long sesionId, List<Butaca> butacasSeleccionadas, boolean taquilla, BigDecimal importe, String email, String nombre, String apellidos, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        if (butacasSeleccionadas.size() == 0)
            throw new CompraSinButacasException();

        CompraDTO compraDTO = comprasDAO.insertaCompra(sesionId, new Date(), taquilla, importe, email, nombre, apellidos, userUID);

        return registraCompra(sesionId, butacasSeleccionadas, taquilla, compraDTO, userUID);
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException",
            "CompraSinButacasException","IncidenciaNotFoundException"})
    public synchronized ResultadoCompra registraCompraAbonoTaquilla(Long abonoId, CompraAbonado compraAbonado, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException, IncidenciaNotFoundException {
        if (compraAbonado != null && compraAbonado.getButacasSeleccionadas() != null && compraAbonado.getButacasSeleccionadas().size() == 0)
            throw new CompraSinButacasException();

        ResultadoCompra resultadoCompra = new ResultadoCompra();
        Abono abono = abonosService.getAbono(abonoId);

        Abonado abonado = compraAbonado.getAbonado();
        abonado.setImporte(calculaImporteButacasAbono(abonoId, compraAbonado.getButacasSeleccionadas()));
        abonado.setAbono(abono);
        abonado = abonadosDAO.addAbonado(abonado);

        for (SesionAbono sesion : abono.getSesiones()) {
            CompraDTO compraDTO = comprasDAO.insertaCompraAbono(sesion.getSesion().getId(), new Date(), true, abonado, userUID);
            butacasDAO.reservaButacas(sesion.getSesion().getId(), compraDTO, compraAbonado.getButacasSeleccionadas(), userUID);
        }
        resultadoCompra.setCorrecta(true);
        resultadoCompra.setId(abonado.getId());

        return resultadoCompra;
    }

    public BigDecimal calculaImporteButacas(Long sesionId, List<Butaca> butacasSeleccionadas, boolean taquilla, String userUID)
    {
        BigDecimal importe = new BigDecimal("0");
        Map<String, Map<Long, PreciosSesion>> preciosLocalizacion = sesionesService.getPreciosSesionPorLocalizacion(sesionId, userUID);

        for (Butaca butaca : butacasSeleccionadas)
        {
        	Map<Long, PreciosSesion> mapaTarifasPrecios = preciosLocalizacion.get(butaca.getLocalizacion());
            importe = importe.add(mapaTarifasPrecios.get(Long.valueOf(butaca.getTipo())).getPrecio());
        }

        if (!taquilla)
        {
            BigDecimal gastosGestion = new BigDecimal(configuration.getGastosGestion());
                    
            importe = importe.add(gastosGestion);
        }
        
        return importe;
    }

    public BigDecimal calculaImporteButacasAbono(Long abonoId, List<Butaca> butacasSeleccionadas)
    {
        BigDecimal importe = new BigDecimal("0");
        List<PreciosSesion> preciosSesion = abonosService.getPreciosAbono(abonoId);

        Map<String, Map<Long, PreciosSesion>> preciosLocalizacion = new HashMap<String, Map<Long, PreciosSesion>>();
        for (PreciosSesion sesion : preciosSesion) {
            if (preciosLocalizacion.get(sesion.getLocalizacion().getCodigo()) != null)
            {
                preciosLocalizacion.get(sesion.getLocalizacion().getCodigo()).put(sesion.getTarifa().getId(), sesion);
            }
            else {
                Map<Long, PreciosSesion> precios = new HashMap<Long, PreciosSesion>();
                precios.put(sesion.getTarifa().getId(), sesion);
                preciosLocalizacion.put(sesion.getLocalizacion().getCodigo(), precios);
            }
        }

        for (Butaca butaca : butacasSeleccionadas)
        {
            Map<Long, PreciosSesion> mapaTarsifasPrecios = preciosLocalizacion.get(butaca.getLocalizacion());
            importe = importe.add(mapaTarsifasPrecios.get(Long.valueOf(butaca.getTipo())).getPrecio());
        }

        return importe;
    }

    @Transactional
    public void marcaPagada(long idCompra)
    {
        comprasDAO.marcarPagada(idCompra);
        if (configuration.isIdEntrada()) {
            butacasDAO.asignarIdEntrada(idCompra);
        }
    }

    @Transactional
	public void marcarPagadaConReferenciaDePago(Long idCompra, String referenciaDePago) {
		
		comprasDAO.marcarPagadaConReferenciaDePago(idCompra, referenciaDePago);
        if (configuration.isIdEntrada()) {
            butacasDAO.asignarIdEntrada(idCompra);
        }
	}

    @Transactional
    public void marcaAbonadoPagado(long idAbonado)
    {
        AbonadoDTO abonado = abonadosDAO.getAbonado(idAbonado);
        for (CompraDTO compra : abonado.getParCompras()) {
            comprasDAO.marcarPagada(compra.getId());
            if (configuration.isIdEntrada()) {
                butacasDAO.asignarIdEntrada(compra.getId());
            }
        }
    }

    @Transactional
    public void marcarAbonadoPagadoConReferenciaDePago(Long idAbonado, String referenciaDePago)
    {
        AbonadoDTO abonado = abonadosDAO.getAbonado(idAbonado);
        for (CompraDTO compra : abonado.getParCompras()) {
            comprasDAO.marcarPagadaConReferenciaDePago(compra.getId(), referenciaDePago);
            if (configuration.isIdEntrada()) {
                butacasDAO.asignarIdEntrada(compra.getId());
            }
        }
    }

    public void eliminaPendientes() throws IncidenciaNotFoundException {
        comprasDAO.eliminaComprasPendientes();
    }

    public CompraDTO getCompraById(long idCompra)
    {
        return comprasDAO.getCompraById(idCompra);
    }

    public CompraDTO getCompraByUuid(String uuidCompra)
    {
        return comprasDAO.getCompraByUuid(uuidCompra);
    }

    public void marcaPagadaPasarela(long idCompra, String codigoPago)
    {
        comprasDAO.marcarPagadaPasarela(idCompra, codigoPago);
    }

    public void rellenaDatosComprador(String uuidCompra, String nombre, String apellidos, String direccion,
            String poblacion, String cp, String provincia, String telefono, String email, String infoPeriodica)
    {
        comprasDAO.rellenaDatosComprador(uuidCompra, nombre, apellidos, direccion, poblacion, cp, provincia, telefono, email, infoPeriodica);
    }
    
    private Date addHoraMinutoToFecha(Date fecha, int hora, int minuto) {
    	Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, hora);
        cal.set(Calendar.MINUTE, minuto);
        return cal.getTime();
    }

    @Transactional(rollbackForClassName={"NoHayButacasLibresException","ButacaOcupadaException",
	"CompraSinButacasException"})
    public ResultadoCompra reservaButacas(Long sesionId, Date desde, Date hasta, List<Butaca> butacasSeleccionadas, String observaciones, 
    		int horaInicial, int horaFinal, int minutoInicial, int minutoFinal, String userUID)
            throws NoHayButacasLibresException, ButacaOcupadaException, CompraSinButacasException
    {
        if (butacasSeleccionadas.size() == 0)
            throw new CompraSinButacasException();

        ResultadoCompra resultadoCompra = new ResultadoCompra();
        
        
        desde = addHoraMinutoToFecha(desde, horaInicial, minutoInicial);
        hasta = addHoraMinutoToFecha(hasta, horaFinal, minutoFinal);

        CompraDTO compraDTO = comprasDAO.reserva(sesionId, new Date(), desde, hasta, observaciones, userUID);

        butacasDAO.reservaButacas(sesionId, compraDTO, butacasSeleccionadas, userUID);

        resultadoCompra.setCorrecta(true);
        resultadoCompra.setId(compraDTO.getId());
        resultadoCompra.setUuid(compraDTO.getUuid());

        return resultadoCompra;
    }

    public List<Compra> getComprasBySesion(long sesionId, int showAnuladas, String sortParameter, int start, int limit, int showOnline, String search)
    {
        List<Compra> result = new ArrayList<Compra>();
        
        List<Tuple> tuples = comprasDAO.getComprasBySesion(sesionId, showAnuladas, sortParameter, start, limit, showOnline, search);

        for (Tuple tupla: tuples) {
            Tuple tupleCompra = tupla.get(0, Tuple.class);
        	CompraDTO compraDTO = new CompraDTO();
            compraDTO.setId(tupleCompra.get(0, Long.class));
            compraDTO.setAnulada(tupleCompra.get(1, Boolean.class));
            compraDTO.setApellidos(tupleCompra.get(2, String.class));
            compraDTO.setCaducada(tupleCompra.get(3, Boolean.class));
            compraDTO.setCodigoPagoPasarela(tupleCompra.get(4, String.class));
            compraDTO.setCodigoPagoTarjeta(tupleCompra.get(5, String.class));
            compraDTO.setCp(tupleCompra.get(6, String.class));
            compraDTO.setDesde(tupleCompra.get(7, Timestamp.class));
            compraDTO.setDireccion(tupleCompra.get(8, String.class));
            compraDTO.setEmail(tupleCompra.get(9, String.class));
            compraDTO.setFecha(tupleCompra.get(10, Timestamp.class));
            compraDTO.setHasta(tupleCompra.get(11, Timestamp.class));
            compraDTO.setImporte(tupleCompra.get(12, BigDecimal.class));
            compraDTO.setInfoPeriodica(tupleCompra.get(13, Boolean.class));
            compraDTO.setNombre(tupleCompra.get(14, String.class));
            compraDTO.setObservacionesReserva(tupleCompra.get(15, String.class));
            compraDTO.setPagada(tupleCompra.get(16, Boolean.class));
            compraDTO.setParSesion(new SesionDTO(tupleCompra.get(17, Long.class)));
            compraDTO.setPoblacion(tupleCompra.get(18, String.class));
            compraDTO.setProvincia(tupleCompra.get(19, String.class));
            compraDTO.setReciboPinpad(tupleCompra.get(20, String.class));
            compraDTO.setReferenciaPago(tupleCompra.get(21, String.class));
            compraDTO.setReserva(tupleCompra.get(22, Boolean.class));
            compraDTO.setTaquilla(tupleCompra.get(23, Boolean.class));
            compraDTO.setTelefono(tupleCompra.get(24, String.class));
            compraDTO.setUuid(tupleCompra.get(25, String.class));

        	BigDecimal importe = tupla.get(1, BigDecimal.class);
        	compraDTO.setImporte(importe);
            result.add(new Compra(compraDTO));
        }
        
        return result;
    }
    

    public List<Compra> getComprasBySesionFechaSegundos(long sesionId, int showAnuladas, String sortParameter, int start, int limit, int showOnline, String search)
    {
        List<Compra> compras = getComprasBySesion(sesionId, showAnuladas, sortParameter, start, limit, showOnline, search);
        
        for (Compra compra : compras)
        {
            compra.setFecha(new Date(compra.getFecha().getTime()/1000));
            if (compra.getDesde() != null)
            	compra.setDesde(new Date(compra.getDesde().getTime()/1000));
            
            if (compra.getHasta() != null)
            	compra.setHasta(new Date(compra.getHasta().getTime()/1000));
        }
        
        return compras;
    }

	public int getTotalComprasBySesion(Long sesionId, int showAnuladas, int showOnline, String search) {
		return comprasDAO.getTotalComprasBySesion(sesionId, showAnuladas, showOnline, search);
	}

	public void anularCompraReserva(Long idCompraReserva) throws IncidenciaNotFoundException {
		comprasDAO.anularCompraReserva(idCompraReserva, true);
	}

    @Transactional
    public void anularCompraAbonado(Long idAbonado) throws IncidenciaNotFoundException {
        AbonadoDTO abonado = abonadosDAO.getAbonado(idAbonado);
        for (CompraDTO compra : abonado.getParCompras()) {
            comprasDAO.anularCompraReserva(compra.getId(), true);
        }
    }

	@Transactional
    public void desanularCompraReserva(Long idCompraReserva, String userUID) throws ButacaOcupadaAlActivarException {
        comprasDAO.desanularCompraReserva(idCompraReserva);

		CompraDTO compraDTO = getCompraById(idCompraReserva);
		if (compraDTO.getReserva() == null || compraDTO.getReserva() == false) {
			long totalAnuladas = sesionesDAO.getButacasAnuladasTotal(compraDTO.getParSesion().getId());
			if (totalAnuladas == 0L) {
				sesionesDAO.removeAnulacionVentasFromSesion(compraDTO.getParSesion().getId(), userUID);
			}
		}
	}
	
    public void anulaReservasCaducadas() throws IncidenciaNotFoundException {
        List<CompraDTO> aCaducar = comprasDAO.getReservasACaducar(new Date());
        
        for (CompraDTO compraDTO:aCaducar)
			comprasDAO.anularCompraReserva(compraDTO.getId(), false);
    }
	
	@Transactional
    public void anularButacas(List<Long> idsButacas) throws IncidenciaNotFoundException {
	    for (Long idButaca: idsButacas)
	    	butacasDAO.anularButaca(idButaca);
	}

    public void rellenaCodigoPagoPasarela(long idCompra, String recibo) {
        comprasDAO.rellenaCodigoPagoPasarela(idCompra, recibo);
    }

	public void passarACompra(Long sesionId, Long idCompraReserva) {
		comprasDAO.passarACompra(sesionId, idCompraReserva);
        if (configuration.isIdEntrada()) {
            butacasDAO.asignarIdEntrada(idCompraReserva);
        }
	}

    @Transactional
    public void actualizaDatosAbonado(Abonado abonado) {
        comprasDAO.updateDatosAbonadoCompra(abonado);
    }
}