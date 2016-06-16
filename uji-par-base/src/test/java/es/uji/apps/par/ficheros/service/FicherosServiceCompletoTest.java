package es.uji.apps.par.ficheros.service;

import es.uji.apps.par.exceptions.PrecioRepetidoException;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class FicherosServiceCompletoTest extends FicherosServiceBaseTest
{
    @Autowired
    private FicherosService service;

    @Before
    public void setup() throws PrecioRepetidoException {
        super.setup();
    }

    @Test
    @Transactional
    public void testGeneraRegistroBuzonSinEspectadores() throws Exception
    {
        Sesion sesion = creaSesion(sala, evento, usuario.getUsuario());
        String fichero = service.generaFicheroRegistros(generaFechaEnvio().getTime(), "FL", Arrays.asList(sesion), usuario.getUsuario()).serializa();
        String idInternoPelicula = String.format("%05d", evento.getId());
        String expected = String.format("0123FL%03d%03d00000000006000000000010000000000000000000.00\n" +
                          "1567         Sala 1                        \n" +
                          "2567         1112132200010000000000.00000\n" +
                          "3567         1112132200" + idInternoPelicula + "\n" +
                          "4567         " + idInternoPelicula + "1a          2a                                                3a          4a                                                5163\n" +
                          "5567         11121301\n", generaFechaEnvio().get(Calendar.DAY_OF_YEAR), getDiaAnyoActual());
        Assert.assertEquals(expected, fichero);
    }

	private int getDiaAnyoActual() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_YEAR);
	}
    
    private Calendar generaFechaEnvio() {
    	Calendar cal = Calendar.getInstance();
    	cal.set(2013, 4, 22);
    	return cal;
    }
    
    @Test
    @Transactional
    public void testGeneraRegistroBuzonConEspectadores() throws Exception
    {
        Sesion sesion = creaSesion(sala, evento, "05/06/2013", "15:30", usuario.getUsuario());
        Butaca butaca1 = creaButaca("1", "1");
        Butaca butaca2 = creaButaca("1", "2");
        registraCompra(sesion, usuario.getUsuario(), butaca1, butaca2);
        String fichero = service.generaFicheroRegistros(generaFechaEnvio().getTime(), "FL", Arrays.asList(sesion), usuario.getUsuario()).serializa();
        String idInternoPelicula = String.format("%05d", evento.getId());
        String expected = String.format("0123FL%03d%03d00000000006000000000010000000000200000002.20\n" +
                          "1567         Sala 1                        \n" +
                          "2567         0506131530010000200002.20005\n" +
                          "3567         0506131530" + idInternoPelicula + "\n" +
                          "4567         " + idInternoPelicula + "1a          2a                                                3a          4a                                                5163\n" +
                          "5567         05061301\n", generaFechaEnvio().get(Calendar.DAY_OF_YEAR), getDiaAnyoActual());
        Assert.assertEquals(expected, fichero);
    }
    
    
    @Test
    @Transactional
    public void testGeneraRegistroBuzonConEspectadoresVariasSesionesVariasSalas() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "05/06/2013", "15:30", usuario.getUsuario());
        Butaca butaca1 = creaButaca("1", "1");
        Butaca butaca2 = creaButaca("1", "2");
        registraCompra(sesion1, usuario.getUsuario(), butaca1, butaca2);
        Evento evento2 = creaEvento(tipoEvento, "exp1", "El Hobbit", "555", "Distribuidora 4", "1", "2");
        Sala sala2 = creaSala("789", "Sala 2");
        addSalaUsuario(sala2, usuario);
        Sesion sesion2 = creaSesion(sala2, evento2, "06/07/2013", "20:30", usuario.getUsuario());
        Butaca butaca3 = creaButaca("1", "1");
        Butaca butaca4 = creaButaca("1", "2");
        registraCompra(sesion2, usuario.getUsuario(), butaca3, butaca4);
        Sesion sesion3 = creaSesion(sala2, evento2, "06/07/2013", "22:00", usuario.getUsuario());
        Butaca butaca5 = creaButaca("1", "1");
        Butaca butaca6 = creaButaca("1", "2");
	    registraCompra(sesion3, usuario.getUsuario(), butaca5, butaca6);

        String fichero = service.generaFicheroRegistros(generaFechaEnvio().getTime(), "FL", Arrays.asList(sesion1, sesion2,
				sesion3), usuario.getUsuario()).serializa();
        
        String idInternoPelicula1 = String.format("%05d", evento.getId());
        String idInternoPelicula2 = String.format("%05d", evento2.getId());

        String expected =   String.format("0123FL%03d%03d00000000014000000000030000000000600000006.60\n" +
                            "1567         Sala 1                        \n" +
                            "1789         Sala 2                        \n" +
                            "2567         0506131530010000200002.20005\n" +
                            "2789         0607132030010000200002.20005\n" +
                            "2789         0607132200010000200002.20005\n" +
                            "3567         0506131530" + idInternoPelicula1 + "\n" +
                            "3789         0607132030" + idInternoPelicula2 + "\n" +
                            "3789         0607132200" + idInternoPelicula2 + "\n" +
                            "4567         " + idInternoPelicula1 + "1a          2a                                                3a          4a                                                5163\n" +
                            "4789         " + idInternoPelicula2 + "exp1        El Hobbit                                         555         Distribuidora 4                                   1123\n" +
                            "4789         " + idInternoPelicula2 + "exp1        El Hobbit                                         555         Distribuidora 4                                   1123\n" +
                            "5567         05061301\n" +
                            "5789         06071302\n", generaFechaEnvio().get(Calendar.DAY_OF_YEAR), getDiaAnyoActual());
        Assert.assertEquals(expected, fichero);
    }
}
