package es.uji.apps.par.services.rest.crm;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.enums.TipoPago;
import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.exceptions.CompraSinButacasException;
import es.uji.apps.par.exceptions.NoHayButacasLibresException;
import es.uji.apps.par.exceptions.ResponseMessage;
import es.uji.apps.par.model.*;
import es.uji.apps.par.report.EntradaReportFactory;
import es.uji.apps.par.services.*;
import es.uji.apps.par.services.rest.BaseResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("crm")
public class CrmResource extends BaseResource {

    @InjectParam
    private ComprasService comprasService;

    @InjectParam
    private SesionesService sesionesService;

    @InjectParam
    private ButacasService butacasService;

    @InjectParam
    private UsersService usersService;

    @Context
    HttpServletRequest currentRequest;

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compraEntrada(@PathParam("id") String rssId, @QueryParam("entradas") int numeroEntradas, @QueryParam("email") String email, @QueryParam("nombre") String nombre, @QueryParam("apellidos") String apellidos)
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        if (email == null) {
            return errorResponse("error.datosComprador.email");
        }
        else if (nombre == null) {
            return errorResponse("error.datosComprador.nombre");
        }
        else if (apellidos == null) {
            return errorResponse("error.datosComprador.apellidos");
        }
        else if (numeroEntradas == 0) {
            return errorResponse("error.compraSinButacas");
        }

        List<Sesion> sesiones = sesionesService.getSesionesPorRssId(rssId, user.getUsuario());
        if (sesiones != null && sesiones.size() == 1) {
            Sesion sesion = sesiones.get(0);

            TipoEvento parTiposEvento = sesion.getEvento().getParTiposEvento();
            if (parTiposEvento != null && parTiposEvento.getNombreEs() != null) {
                try {
                    EntradaReportFactory.newInstanceByClassName("es.uji.apps.par.report." + EntradasService.BEAN_REPORT_PREFIX +
							parTiposEvento.getNombreEs() + EntradasService.BEAN_REPORT_SUFFIX);
                } catch(Exception e) {
                    return errorResponse("error.tipoNoPermitido");
                }

                if (sesion.getFechaCelebracion().before(new Date())) {
                    return errorResponse("error.sesionPasada");
                }

                List<Butaca> butacasSeleccionadas = new ArrayList<Butaca>();
                int quedanPorReservar = numeroEntradas;
                List<DisponiblesLocalizacion> disponiblesNoNumerada = butacasService.getDisponiblesNoNumerada(sesion.getId());
                for (DisponiblesLocalizacion disponiblesLocalizacion : disponiblesNoNumerada) {
                    for (int i = 0; i < disponiblesLocalizacion.getDisponibles() && quedanPorReservar > 0; i++) {
                        if (quedanPorReservar > 0) {
                            Butaca butaca = new Butaca();
                            butaca.setLocalizacion(disponiblesLocalizacion.getLocalizacion());
                            butaca.setPrecio(BigDecimal.ZERO);
                            butaca.setTipo("1");
                            butacasSeleccionadas.add(butaca);
                            quedanPorReservar--;
                        }
                    }

                    if (quedanPorReservar == 0)
                        break;
                }

                if (quedanPorReservar > 0) {
                    return errorResponse("error.noHayButacasParaLocalizacion");
                }

                try {
                    ResultadoCompra resultadoCompra = comprasService.registraCompra(sesion.getId(), butacasSeleccionadas, true, BigDecimal.ZERO, email, nombre, apellidos, user.getUsuario());

                    if (resultadoCompra.getCorrecta()) {
                        comprasService.marcaPagada(resultadoCompra.getId(), TipoPago.METALICO);
                        String urlPdf = configurationSelector.getUrlPublic() + "/rest/compra/" + resultadoCompra.getUuid() + "/pdf";
                        return Response.ok(new ResponseMessage(true, urlPdf)).build();
                    }
                    else {
                        return errorResponse("error.ws");
                    }
                } catch (NoHayButacasLibresException e) {
                    return errorResponse("error.noHayButacas", getProperty("localizacion." + e.getLocalizacion()));
                } catch (ButacaOcupadaException e) {
                    return errorResponse("error.butacaOcupada", getProperty("localizacion." + e.getLocalizacion()), e.getFila(), e.getNumero());
                } catch (CompraSinButacasException e) {
                    return errorResponse("error.compraSinButacas");
                }
            }
            else {
                return errorResponse("error.tipoNoEncontrado");
            }

        }
        else {
            return errorResponse("error.multiplesSesiones");
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response muestraCompras(@PathParam("id") String rssId)
    {
        Usuario user = usersService.getUserByServerName(currentRequest.getServerName());

        List<Sesion> sesiones = sesionesService.getSesionesPorRssId(rssId, user.getUsuario());
        if (sesiones != null && sesiones.size() == 1) {
            Sesion sesion = sesiones.get(0);

            TipoEvento parTiposEvento = sesion.getEvento().getParTiposEvento();
            if (parTiposEvento != null && parTiposEvento.getNombreEs() != null) {
                try {
                    EntradaReportFactory.newInstanceByClassName("es.uji.apps.par.report." + EntradasService.BEAN_REPORT_PREFIX +
							parTiposEvento.getNombreEs() + EntradasService.BEAN_REPORT_SUFFIX);
                } catch (Exception e) {
                    return errorResponse("error.tipoNoPermitido");
                }

                List<CompraYUso> comprasYPresentadas = comprasService.getComprasYPresentadas(sesion.getId());
                return Response.ok(comprasYPresentadas).build();
            }
            else {
                return errorResponse("error.tipoNoEncontrado");
            }
        }
        else {
            return errorResponse("error.multiplesSesiones");
        }
    }
}
