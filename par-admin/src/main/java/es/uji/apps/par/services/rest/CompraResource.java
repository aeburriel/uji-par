package es.uji.apps.par.services.rest;

import com.sun.jersey.api.core.InjectParam;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import es.uji.apps.par.auth.AuthChecker;
import es.uji.apps.par.enums.TipoPago;
import es.uji.apps.par.exceptions.ButacaOcupadaAlActivarException;
import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.exceptions.CompraSinButacasException;
import es.uji.apps.par.exceptions.Constantes;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.exceptions.NoHayButacasLibresException;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.CompraAbonado;
import es.uji.apps.par.model.DisponiblesLocalizacion;
import es.uji.apps.par.model.ResultadoCompra;
import es.uji.apps.par.model.Usuario;
import es.uji.apps.par.services.AbonosService;
import es.uji.apps.par.services.ButacasService;
import es.uji.apps.par.services.ComprasService;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.services.UsersService;
import es.uji.apps.par.utils.Utils;
import es.uji.commons.web.template.HTMLTemplate;
import es.uji.commons.web.template.Template;

@Path("compra")
public class CompraResource extends BaseResource
{
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

	@InjectParam
	private UsersService usersService;

	@Context
	HttpServletResponse currentResponse;

	@POST
	@Path("{idCompra}/pagada")
	@Consumes(MediaType.APPLICATION_JSON)
	public void marcaPagada(@PathParam("idCompra") Long idCompra, @QueryParam("referencia") String referenciaDePago, @QueryParam("tipopago") String tipoPago)
	{
		AuthChecker.canWrite(currentRequest);

		if (!isTipoPagoTarjetaOffline(tipoPago))
		{
			referenciaDePago = "";
		}

		comprasService.marcarPagadaConReferenciaDePago(idCompra, referenciaDePago, tipoPago);
	}

    @POST
    @Path("{idAbonado}/pagada/abonado")
    @Consumes(MediaType.APPLICATION_JSON)
    public void marcaAbonadoPagada(@PathParam("idAbonado") Long idAbonado, @QueryParam("referencia") String referenciaDePago, @QueryParam("tipopago") String tipoPago)
    {
        AuthChecker.canWrite(currentRequest);

		if (!isTipoPagoTarjetaOffline(tipoPago))
		{
			referenciaDePago = "";
		}

		comprasService.marcarAbonadoPagadoConReferenciaDePago(idAbonado, referenciaDePago, tipoPago);
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
		Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

		AuthChecker.canWrite(currentRequest);

		if (!isTipoPagoTarjetaOffline(tipoPago))
		{
			recibo = "";
		}

		comprasService.passarACompra(sesionId, idCompraReserva, recibo, tipoPago, user.getUsuario());
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

		comprasService.passarButacasACompra(sesionId, idCompraReserva, recibo, tipoPago, idsButacas, language, userUID);
		return Response.ok().build();
	}

	private boolean isTipoPagoTarjetaOffline(String tipoPago)
	{
		return tipoPago != null && tipoPago.trim().toLowerCase().equals(TipoPago.TARJETAOFFLINE.toString().toLowerCase());
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
	@Path("{id}/print/{pdfType}")
	@Produces(MediaType.TEXT_HTML)
	public Template imprimeEntrada(@PathParam("id") String uuidCompra, @PathParam("pdfType") String pdfType)
	{
		Template template = new HTMLTemplate(Constantes.PLANTILLAS_DIR + "print", getLocale(), APP);

		template.put("urlAdmin", configurationSelector.getUrlAdmin());
		template.put("uuid", uuidCompra);
		template.put("pdfType", pdfType);

		return template;
	}

	@GET
	@Path("{id}/pdf")
	@Produces("application/pdf")
	public Response generaEntradaPrintAtHome(@PathParam("id") String uuidCompra)
			throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String userUID = AuthChecker.getUserUID(currentRequest);
		entradasService.generaEntrada(uuidCompra, bos, userUID, configurationSelector.getUrlPublicSinHTTPS(), null);

		final ResponseBuilder builder = Response.ok(bos.toByteArray())
				.header("Content-Disposition", "attachment; filename=\"entrada " + uuidCompra + ".pdf\"");

		return Utils.noCache(builder).build();
	}

	@GET
	@Path("{id}/pdftaquilla")
	@Produces("application/x-pdf")
	public Response generaEntradaTaquilla(@PathParam("id") String uuidCompra)
			throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String userUID = AuthChecker.getUserUID(currentRequest);
		entradasService.generaEntradaTaquilla(uuidCompra, bos, userUID, configurationSelector.getUrlPublicSinHTTPS());

		final ResponseBuilder builder = Response.ok(bos.toByteArray())
				.header("Content-Disposition", "inline; filename=\"ticket " + uuidCompra + ".pdf\"");

		return Utils.noCache(builder).build();
	}
}
