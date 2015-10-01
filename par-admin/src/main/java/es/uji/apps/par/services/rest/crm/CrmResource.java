package es.uji.apps.par.services.rest.crm;

import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.exceptions.*;
import es.uji.apps.par.model.*;
import es.uji.apps.par.services.ButacasService;
import es.uji.apps.par.services.ComprasService;
import es.uji.apps.par.services.SesionesService;
import es.uji.apps.par.services.rest.BaseResource;

import javax.ws.rs.*;
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

    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response compraEntrada(@PathParam("id") String rssId, @QueryParam("entradas") int numeroEntradas, @QueryParam("email") String email, @QueryParam("nombre") String nombre, @QueryParam("apellidos") String apellidos) {

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

        //TODO: solamente se permiten ids de eventos de tipo Graduación

        List<Sesion> sesiones = sesionesService.getSesionesPorRssId(rssId);
        if (sesiones != null && sesiones.size() == 1) {
            Sesion sesion = sesiones.get(0);

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
                ResultadoCompra resultadoCompra = comprasService.registraCompra(sesion.getId(), butacasSeleccionadas, true, BigDecimal.ZERO, email, nombre, apellidos);

                if (resultadoCompra.getCorrecta()) {
                    comprasService.marcaPagada(resultadoCompra.getId());
                    String urlPdf = Configuration.getUrlPublic() + "/rest/compra/" + resultadoCompra.getUuid() + "/pdf";
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
            return errorResponse("error.multiplesSesiones");
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response muestraCompras(@PathParam("id") String rssId) {
        //TODO: solamente se permiten ids de eventos de tipo Graduación

        List<Sesion> sesiones = sesionesService.getSesionesPorRssId(rssId);
        if (sesiones != null && sesiones.size() == 1) {
            Sesion sesion = sesiones.get(0);

            List<CompraYUso> comprasYPresentadas = comprasService.getComprasYPresentadas(sesion.getId());
            return Response.ok(comprasYPresentadas).build();
        }
        else {
            return errorResponse("error.multiplesSesiones");
        }
    }
}
