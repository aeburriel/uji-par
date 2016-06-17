package es.uji.apps.par.services.dao;

import com.mysema.query.Tuple;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.ClientesDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.Localizacion;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.services.ComprasService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = {"/applicationContext-db-test.xml"})
public class ComprasDAOTest extends BaseDAOTest {
    @Autowired
    ClientesDAO clientesDAO;

    @Autowired
    ComprasDAO comprasDAO;

    @Autowired
    ButacasDAO butacasDAO;

    @Autowired
    ComprasService comprasService;

    private Localizacion localizacion;
    private SesionDTO sesion;

    @Before
    public void before() {
        localizacion = preparaLocalizacion("anfiteatro");
        sesion = preparaSesion(Localizacion.localizacionToLocalizacionDTO(localizacion));
    }

    public Sesion preparaSesion() {
        return new Sesion(preparaSesion(Localizacion.localizacionToLocalizacionDTO(localizacion)));
    }

    @Test
    @Transactional
    public void guardaCompraOk() {
        CompraDTO compraDTO = comprasDAO.insertaCompra(sesion.getId(), new Date(), true, BigDecimal.ONE, "");

        assertNotNull(compraDTO.getId());
    }

    @Test
    @Transactional
    public void testGetRecaudacionSinCompras() {
        Sesion sesion1 = preparaSesion();

        assertEquals(new BigDecimal(0).doubleValue(), comprasDAO.getRecaudacionSesiones(Arrays.asList(sesion1)).doubleValue(), 0.00001);
    }

    @Test
    @Transactional
    public void testGetRecaudacion() {
        Sesion sesion1 = preparaSesion();
        CompraDTO compra1 = comprasDAO.insertaCompra(sesion1.getId(), new Date(), true, new BigDecimal(2), usuario1.getUsuario());
        ButacaDTO butaca1 = preparaButaca(Sesion.SesionToSesionDTO(sesion1),
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "1", "2",
                new BigDecimal(2));
        butaca1.setParCompra(compra1);
        butacasDAO.addButaca(butaca1);
        CompraDTO compra2 = comprasDAO.insertaCompra(sesion1.getId(), new Date(), true, new BigDecimal(3), usuario1.getUsuario());
        ButacaDTO butaca2 = preparaButaca(Sesion.SesionToSesionDTO(sesion1),
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "3", "4",
                new BigDecimal(3));
        butaca2.setParCompra(compra2);
        butacasDAO.addButaca(butaca2);

        assertEquals(new BigDecimal(5).doubleValue(), comprasDAO.getRecaudacionSesiones(Arrays.asList(sesion1)).doubleValue(), 0.00001);
    }

    @Test
    @Transactional
    public void testGetRecaudacionVariasSesiones() {
        Sesion sesion1 = preparaSesion();
        CompraDTO compra1 = comprasDAO.insertaCompra(sesion1.getId(), new Date(), true, new BigDecimal(2), usuario1.getUsuario());

        ButacaDTO butaca1 = preparaButaca(Sesion.SesionToSesionDTO(sesion1), Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
                new BigDecimal(2));
        butaca1.setParCompra(compra1);
        butacasDAO.addButaca(butaca1);

        Sesion sesion2 = preparaSesion();
        CompraDTO compra2 = comprasDAO.insertaCompra(sesion2.getId(), new Date(), true, new BigDecimal(1), usuario1.getUsuario());
        ButacaDTO butaca2 = preparaButaca(Sesion.SesionToSesionDTO(sesion2),
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "1", "2",
                new BigDecimal(1));
        butaca2.setParCompra(compra2);
        butacasDAO.addButaca(butaca2);

        CompraDTO compra3 = comprasDAO.insertaCompra(sesion2.getId(), new Date(), true, new BigDecimal(5), usuario1.getUsuario());
        ButacaDTO butaca3 = preparaButaca(Sesion.SesionToSesionDTO(sesion2),
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "3", "4",
                new BigDecimal(5));
        butaca3.setParCompra(compra3);
        butacasDAO.addButaca(butaca3);

        assertEquals(new BigDecimal(8).doubleValue(), comprasDAO.getRecaudacionSesiones(Arrays.asList(sesion1, sesion2)).doubleValue(), 0.00001);
    }

    @Test
    @Transactional
    public void testGetQueryComprasBySesion() {
        Sesion sesion1 = preparaSesion();
        CompraDTO compraDTO = comprasDAO.insertaCompra(sesion1.getId(), new Date(), true, new BigDecimal(2), usuario1.getUsuario());
        ButacaDTO butaca1 = preparaButaca(Sesion.SesionToSesionDTO(sesion1),
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "1", "2",
                new BigDecimal(2));
        butaca1.setParCompra(compraDTO);
        butacasDAO.addButaca(butaca1);

        assertNotNull(compraDTO.getId());

        List<Tuple> tuples = comprasDAO.getComprasBySesion(sesion.getId(), 0, "id", 0, 10, 1, "");
        assertNotNull(tuples);

        for (Tuple tupla: tuples) {
            Tuple tupleCompra = tupla.get(0, Tuple.class);
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
            compraDTO.setTelefono(tupleCompra.get(14, String.class));
            compraDTO.setUuid(tupleCompra.get(25, String.class));

            BigDecimal importe = tupla.get(1, BigDecimal.class);
            compraDTO.setImporte(importe);
        }

        List<Compra> compras = comprasService.getComprasBySesion(sesion.getId(), 0, "id", 0, 10, 1, "");
        assertNotNull(compras);
    }

    @Test
    @Transactional
    public void getClientes() {
        final String MAIL = "test@test.com";

        CompraDTO compraDTO = comprasDAO.insertaCompra(sesion.getId(), new Date(), true, new BigDecimal(2), usuario1.getUsuario());
        ButacaDTO butaca1 = preparaButaca(sesion,
                Localizacion.localizacionToLocalizacionDTO(localizacion),
                "1", "2",
                new BigDecimal(2));

        compraDTO.setInfoPeriodica(true);
        compraDTO.setEmail(MAIL);

        butaca1.setParCompra(compraDTO);
        butacasDAO.addButaca(butaca1);

        List<Tuple> clientes = clientesDAO.getClientes("nombre", 0, 10, usuario1.getUsuario());

        assertNotNull(clientes);
        assertEquals(clientes.size(), clientesDAO.getTotalClientes(usuario1.getUsuario()));
        assertTrue(clientes.get(0).get(8, String.class).equals(MAIL));
    }

    //TODO Anular compras y ver que marca la sesion con incidencias del ICAA

    //TODO Desanular compras y ver que si no queda ninguna butaca anulada en la sesion, que elimina la incidencia de la sesion

    //TODO Desanular compras y ver que si aun queda alguna butaca anulada en la sesion, NO elimina la incidencia de la sesion
}
