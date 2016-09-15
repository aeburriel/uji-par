package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.exceptions.*;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.CompraAbonado;
import es.uji.apps.par.model.DisponiblesLocalizacion;
import es.uji.apps.par.model.ResultadoCompra;
import es.uji.apps.par.services.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Path("compra")
public class CompraResource extends BaseResource {

	final String TARJETA_OFFLINE = "tarjetaoffline";

	@InjectParam
	private ComprasService comprasService;

	@InjectParam
	private SesionesService sesionesService;

	@InjectParam
	private ButacasService butacasService;

	@InjectParam
	private EntradasService entradasService;

    @InjectParam
    private AbonosService abonosService;

	@Context
	HttpServletResponse currentResponse;

	@POST
	@Path("{idCompra}/pagada")
	@Consumes(MediaType.APPLICATION_JSON)
	public void marcaPagada(@PathParam("idCompra") Long idCompra, @QueryParam("referencia") String referenciaDePago)
	{
		AuthChecker.canWrite(currentRequest);

		if (referenciaDePago != null)
			comprasService.marcarPagadaConReferenciaDePago(idCompra, referenciaDePago);
		else
			comprasService.marcaPagada(idCompra);
	}

    @POST
    @Path("{idAbonado}/pagada/abonado")
    @Consumes(MediaType.APPLICATION_JSON)
    public void marcaAbonadoPagada(@PathParam("idAbonado") Long idAbonado, @QueryParam("referencia") String referenciaDePago)
    {
        AuthChecker.canWrite(currentRequest);

        if (referenciaDePago != null)
            comprasService.marcarAbonadoPagadoConReferenciaDePago(idAbonado, referenciaDePago);
        else
            comprasService.marcaAbonadoPagado(idAbonado);
    }
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCompras(@PathParam("id") Long sesionId,
			@QueryParam("showAnuladas") int showAnuladas,
			@QueryParam("showOnline") int showOnline,
			@QueryParam("sort") String sort, @QueryParam("start") int start,
			@QueryParam("limit") @DefaultValue("1000") int limit,
			@QueryParam("search") @DefaultValue("") String search) {
		return Response
				.ok()
				.entity(new RestResponse(true, comprasService
						.getComprasBySesionFechaSegundos(sesionId,
								showAnuladas, sort, start, limit, showOnline,
								search), comprasService
						.getTotalComprasBySesion(sesionId, showAnuladas,
								showOnline, search))).build();
	}

	@PUT
	@Path("{idSesion}/{idCompraReserva}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response anularCompraOReserva(@PathParam("idSesion") Long sesionId,
			@PathParam("idCompraReserva") Long idCompraReserva) throws IncidenciaNotFoundException {
		AuthChecker.canWrite(currentRequest);

		comprasService.anularCompraReserva(idCompraReserva);
		return Response.ok().build();
	}

    @PUT
    @Path("abonado/{idAbonado}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response anularCompraAbonado(@PathParam("idAbonado") Long abonadoId) throws IncidenciaNotFoundException {
        AuthChecker.canWrite(currentRequest);

        comprasService.anularCompraAbonado(abonadoId);
        return Response.ok().build();
    }

	@PUT
	@Path("{idSesion}/desanuladas/{idCompraReserva}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response desanularCompraOReserva(
			@PathParam("idSesion") Long sesionId,
			@PathParam("idCompraReserva") Long idCompraReserva) {
		AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

		try {
			comprasService.desanularCompraReserva(idCompraReserva, userUID);
			return Response.ok().build();
		} catch (ButacaOcupadaAlActivarException e) {
			return errorResponse("error.butacaOcupadaAlActivar",
					e.getTaquilla() ? "taquilla" : "online", e.getComprador(),
					getProperty("localizacion." + e.getLocalizacion()),
					e.getFila(), e.getNumero());
		}
	}
	
	@PUT
	@Path("{idSesion}/passaracompra/{idCompra}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response passarACompra(@PathParam("idSesion") Long sesionId, @PathParam("idCompra") Long idCompraReserva, @QueryParam
			("recibo") String recibo, @QueryParam("tipopago") String tipoPago) {
		AuthChecker.canWrite(currentRequest);

		if (!isTipoPagoTarjetaOffline(tipoPago))
		{
			recibo = "";
		}

		comprasService.passarACompra(sesionId, idCompraReserva, recibo);
		return Response.ok().build();
	}

	@PUT
	@Path("{idSesion}/butacapassaracompra/{idCompra}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response passarButacasACompra(@PathParam("idSesion") Long sesionId, @PathParam("idCompra") Long idCompraReserva, @QueryParam
			("recibo") String recibo, @QueryParam("tipopago") String tipoPago, List<Long> idsButacas) {
		AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

		if (!isTipoPagoTarjetaOffline(tipoPago))
		{
			recibo = "";
		}

		Locale locale = getLocale();
		String language = locale.getLanguage();

		comprasService.passarButacasACompra(sesionId, idCompraReserva, recibo, idsButacas, language, userUID);
		return Response.ok().build();
	}

	private boolean isTipoPagoTarjetaOffline(String tipoPago)
	{
		return tipoPago != null && tipoPago.trim().toLowerCase().equals(TARJETA_OFFLINE);
	}

	@PUT
	@Path("{idSesion}/{idCompraReserva}/{idButaca}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response anularButaca(@PathParam("idSesion") Long sesionId,
			@PathParam("idCompraReserva") Long idCompraReserva,
			@PathParam("idButaca") Long idButaca) throws IncidenciaNotFoundException {
		AuthChecker.canWrite(currentRequest);

		comprasService.anularButacas(Arrays.asList(idButaca));
		return Response.ok().build();
	}

	@PUT
	@Path("{idSesion}/butacas/anuladas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response anularButacas(@PathParam("idSesion") Long sesionId,
			List<Long> idsButacas) throws IncidenciaNotFoundException {
		AuthChecker.canWrite(currentRequest);

		comprasService.anularButacas(idsButacas);
		return Response.ok().build();
	}

	@POST
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response compraEntrada(@PathParam("id") Long sesionId,
			List<Butaca> butacasSeleccionadas)
            throws IncidenciaNotFoundException {
		AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

		try {
			ResultadoCompra resultadoCompra = comprasService
					.registraCompraTaquilla(sesionId, butacasSeleccionadas, userUID);
			return Response.ok(resultadoCompra).build();
		} catch (NoHayButacasLibresException e) {
			return errorResponse("error.noHayButacas",
					getProperty("localizacion." + e.getLocalizacion()));
		} catch (ButacaOcupadaException e) {
			return errorResponse("error.butacaOcupada",
					getProperty("localizacion." + e.getLocalizacion()),
					e.getFila(), e.getNumero());
		} catch (CompraSinButacasException e) {
			return errorResponse("error.compraSinButacas");
		}
	}

    @POST
    @Path("{id}/abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response compraAbono(@PathParam("id") Long abonoId,
                                  CompraAbonado compraAbonado)
            throws IncidenciaNotFoundException {
        AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

        try {
            ResultadoCompra resultadoCompra = comprasService
                    .registraCompraAbonoTaquilla(abonoId, compraAbonado, userUID);
            return Response.ok(resultadoCompra).build();
        } catch (NoHayButacasLibresException e) {
            return errorResponse("error.noHayButacas",
                    getProperty("localizacion." + e.getLocalizacion()));
        } catch (ButacaOcupadaException e) {
            return errorResponse("error.butacaOcupada",
                    getProperty("localizacion." + e.getLocalizacion()),
                    e.getFila(), e.getNumero());
        } catch (CompraSinButacasException e) {
            return errorResponse("error.compraSinButacas");
        }
    }

	@GET
	@Path("{id}/precios")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPreciosSesion(@PathParam("id") Long sesionId,
			@QueryParam("sort") String sort, @QueryParam("start") int start,
			@QueryParam("limit") @DefaultValue("1000") int limit) {
		String userUID = AuthChecker.getUserUID(currentRequest);

		return Response
				.ok()
				.entity(new RestResponse(true, sesionesService
						.getPreciosSesion(sesionId, sort, start, limit, true, userUID),
						sesionesService.getTotalPreciosSesion(sesionId)))
				.build();
	}

	// Para una sesión no numerada devuelve las butacas disponibles por
	// localización
	@GET
	@Path("{id}/disponibles")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOcupacionesNoNumerada(@PathParam("id") Long sesionId) {
		List<DisponiblesLocalizacion> listadoOcupacionesNoNumeradas = butacasService
				.getDisponiblesNoNumerada(sesionId);
		return Response
				.ok()
				.entity(new RestResponse(true, listadoOcupacionesNoNumeradas,
						listadoOcupacionesNoNumeradas.size())).build();
	}

	@POST
	@Path("{id}/importe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImportesButacas(@PathParam("id") Long sesionId,
			List<Butaca> butacasSeleccionadas) {
		AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

		BigDecimal importe = comprasService.calculaImporteButacas(sesionId,
				butacasSeleccionadas, true, userUID);

		return Response.ok().entity(importe.setScale(2, BigDecimal.ROUND_HALF_UP).toString()).build();
	}

    @POST
    @Path("{id}/importe/abono")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getImportesAbonoButacas(@PathParam("id") Long abonoId,
                                       List<Butaca> butacasSeleccionadas) {
        AuthChecker.canWrite(currentRequest);
		String userUID = AuthChecker.getUserUID(currentRequest);

        BigDecimal importe = comprasService.calculaImporteButacasAbono(abonoId, butacasSeleccionadas, userUID);

        return Response.ok().entity(importe.setScale(2, BigDecimal.ROUND_HALF_UP).toString()).build();
    }

	@GET
	@Path("{idCompra}/butacas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getButacasCompra(@PathParam("idCompra") Long idCompra,
			@QueryParam("sort") String sort, @QueryParam("start") int start,
			@QueryParam("limit") @DefaultValue("1000") int limit) {
		Locale locale = getLocale();
		String language = locale.getLanguage();

		return Response
				.ok()
				.entity(new RestResponse(true, butacasService.getButacasCompra(
						idCompra, sort, start, limit, language), butacasService
						.getTotalButacasCompra(idCompra))).build();
	}

	@GET
	@Path("{id}/pdf")
	@Produces("application/pdf")
	public Response generaEntradaPrintAtHome(@PathParam("id") String uuidCompra)
			throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String userUID = AuthChecker.getUserUID(currentRequest);
		entradasService.generaEntrada(uuidCompra, bos, userUID, configurationSelector.getUrlPublicSinHTTPS(), configurationSelector.getUrlPieEntrada());

		Response response = Response.ok(bos.toByteArray())
				.header("Cache-Control", "no-cache, no-store, must-revalidate")
				.header("Pragma", "no-cache")
				.header("Expires", "0")
				.header("Content-Disposition", "attachment; filename=\"entrada " + uuidCompra + ".pdf\"")
				.build();

		return response;
	}

	@GET
	@Path("{id}/pdftaquilla")
	@Produces("application/x-pdf")
	public Response generaEntradaTaquilla(@PathParam("id") String uuidCompra)
			throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String userUID = AuthChecker.getUserUID(currentRequest);
		entradasService.generaEntradaTaquilla(uuidCompra, bos, userUID, configurationSelector.getUrlPublicSinHTTPS());

		Response response = Response.ok(bos.toByteArray())
				.header("Cache-Control", "no-cache, no-store, must-revalidate")
				.header("Pragma", "no-cache")
				.header("Expires", "0")
				.header("Content-Disposition", "attachment; filename=\"ticket " + uuidCompra + ".pdf\"")
				.build();

		return response;
	}
}
