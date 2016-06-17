package es.uji.apps.par.services.dao;

import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.exceptions.NoHayButacasLibresException;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Localizacion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "transactionManager")
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class ButacasDAOTest extends BaseDAOTest
{
    @Autowired
    ButacasDAO butacasDao;

    @Autowired
    ComprasDAO comprasDao;

    private Localizacion localizacion;

    private SesionDTO sesion;

    @Before
    public void before()
    {
        localizacion = preparaLocalizacion("anfiteatro");
        sesion = preparaSesion(Localizacion.localizacionToLocalizacionDTO(localizacion));
    }

    @Test
    @Transactional
    public void getButacas()
    {
        Localizacion localizacion2 = preparaLocalizacion("platea1");

        ButacaDTO butaca = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
                BigDecimal.ONE);
        butacasDao.addButaca(butaca);

        ButacaDTO butaca2 = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion2), "2", "3",
                BigDecimal.ONE);
        butacasDao.addButaca(butaca2);

        List<ButacaDTO> butacas = butacasDao.getButacas(sesion.getId(), localizacion.getCodigo());

        Assert.assertEquals(1, butacas.size());
        Assert.assertEquals(butaca, butacas.get(0));
    }

    @Test
    @Transactional
    public void butacaLibreAlInicio()
    {
        Localizacion localizacion = preparaLocalizacion("anfiteatro");
        SesionDTO sesion = preparaSesion(Localizacion.localizacionToLocalizacionDTO(localizacion));

        Assert.assertFalse(butacasDao.estaOcupada(sesion.getId(), localizacion.getCodigo(), "1", "2"));
    }

    @Test
    @Transactional
    public void getButacaOcupada()
    {
        ButacaDTO butaca = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
                BigDecimal.ONE);
        butacasDao.addButaca(butaca);

        Assert.assertTrue(butacasDao.estaOcupada(sesion.getId(), localizacion.getCodigo(), "1", "2"));
    }

    @Test
    @Transactional
    public void reservaButacas() throws NoHayButacasLibresException, ButacaOcupadaException
    {
        ButacaDTO butacaDTO = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
                BigDecimal.ONE);

        Butaca butaca = new Butaca(butacaDTO, false);
        butaca.setTipo(String.valueOf(sesion.getParPreciosSesions().get(0).getParTarifa().getId()));

        butacasDao.reservaButacas(sesion.getId(), insertaCompra(), Arrays.asList(butaca), usuario1.getUsuario());

        List<ButacaDTO> butacas = butacasDao.getButacas(sesion.getId(), localizacion.getCodigo());

        Assert.assertEquals(sesion.getId(), butacas.get(0).getParSesion().getId());
        Assert.assertEquals(localizacion.getCodigo(), butacas.get(0).getParLocalizacion().getCodigo());
        Assert.assertEquals(butaca.getFila(), butacas.get(0).getFila());
        Assert.assertEquals(butaca.getNumero(), butacas.get(0).getNumero());
        Assert.assertEquals(10, butacas.get(0).getPrecio().intValue());
    }
    
    @Transactional
    public void reservaNoNumeradasHayLibres() throws NoHayButacasLibresException, ButacaOcupadaException
    {
        reservaNoNumeradas(localizacion.getTotalEntradas());
    }

    @Test(expected=NoHayButacasLibresException.class)
    @Transactional
    public void reservaNoNumeradasNoHayLibres() throws NoHayButacasLibresException, ButacaOcupadaException
    {
        reservaNoNumeradas(localizacion.getTotalEntradas()+1);
    }

    private void reservaNoNumeradas(int butacasAReservar) throws NoHayButacasLibresException, ButacaOcupadaException
    {
        setSesionNoNumerada(sesion);
        
        List<Butaca> butacas = new ArrayList<Butaca>();
        
        for (int i=0; i<butacasAReservar; i++)
        {
            ButacaDTO butacaDTO = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), null, null,
                BigDecimal.ONE);

            Butaca butaca = new Butaca(butacaDTO, false);
            butaca.setTipo(String.valueOf(sesion.getParPreciosSesions().get(0).getParTarifa().getId()));
            
            butacas.add(butaca);
        }

        butacasDao.reservaButacas(sesion.getId(), insertaCompra(), butacas, usuario1.getUsuario());
    }

    private CompraDTO insertaCompra()
    {
        return comprasDao.insertaCompra(sesion.getId(), new Date(), false, BigDecimal.ONE, "");
    }
    
    @Test(expected=ButacaOcupadaException.class)
	@Ignore
	//TODO en lugar de la exception lanza una violacion de clave unica
    @Transactional
    public void reservaButacasButacaOcupada() throws NoHayButacasLibresException, ButacaOcupadaException
    {
        ButacaDTO butacaDTO = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
            BigDecimal.ONE);

        Butaca butaca = new Butaca(butacaDTO, false);
        butaca.setTipo(String.valueOf(sesion.getParPreciosSesions().get(0).getParTarifa().getId()));
            
        butacasDao.reservaButacas(sesion.getId(), insertaCompra(), Arrays.asList(butaca), usuario1.getUsuario());
        
        butacasDao.reservaButacas(sesion.getId(), insertaCompra(), Arrays.asList(butaca), usuario1.getUsuario());
    }
    
    @Test
    @Transactional
    public void cuentaButacasOcupadasNinguna()
    {
        Assert.assertEquals(0, butacasDao.getOcupadas(sesion.getId(), localizacion.getCodigo()));
    }
    
    @Ignore
    @Transactional
    public void cuentaButacasOcupadasNoContarAnuladas() throws IncidenciaNotFoundException {
        ButacaDTO butaca = preparaButaca(sesion, Localizacion.localizacionToLocalizacionDTO(localizacion), "1", "2",
                BigDecimal.ONE);
    
        butacasDao.addButaca(butaca);
    
        Assert.assertEquals(1, butacasDao.getOcupadas(sesion.getId(), localizacion.getCodigo()));
        butacasDao.anularButaca(butaca.getId());
        
        Assert.assertEquals(0, butacasDao.getOcupadas(sesion.getId(), localizacion.getCodigo()));
    }

    /*@Repository
    private class ButacasDAOTesteable extends ButacasDAO {
           @Override
           protected boolean isButacaFromReserva(Long idButaca) {
               return true;
           }
    }*/

	//TODO Anular butacas y ver que marca la sesion con incidencias del ICAA

	//TODO Desanular butacas y ver que si no queda ninguna anulada, que elimina la incidencia de la sesion

	//TODO Desanular butacas y ver que si aun queda alguna anulada, NO elimina la incidencia de la sesion
}
