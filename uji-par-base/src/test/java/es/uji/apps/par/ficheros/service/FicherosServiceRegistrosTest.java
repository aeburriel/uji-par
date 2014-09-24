package es.uji.apps.par.ficheros.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import es.uji.apps.par.PrecioRepetidoException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.ficheros.registros.RegistroBuzon;
import es.uji.apps.par.ficheros.registros.RegistroPelicula;
import es.uji.apps.par.ficheros.registros.RegistroSala;
import es.uji.apps.par.ficheros.registros.RegistroSesion;
import es.uji.apps.par.ficheros.registros.RegistroSesionPelicula;
import es.uji.apps.par.ficheros.registros.RegistroSesionProgramada;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.utils.DateUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext-db-test.xml" })
public class FicherosServiceRegistrosTest extends FicherosServiceBaseTest
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
        Sesion sesion = creaSesion(sala, evento);

        Date fechaEnvio = new Date();

        RegistroBuzon registro = service.generaFicheroRegistros(fechaEnvio, "FL", Arrays.asList(sesion))
                .getRegistroBuzon();

        Assert.assertEquals(cine.getCodigo(), registro.getCodigo());
        Assert.assertEquals(fechaEnvio, registro.getFechaEnvio());
        Assert.assertEquals("FL", registro.getTipo());
        Assert.assertEquals(null, registro.getFechaEnvioHabitualAnterior());

        Assert.assertEquals(1, registro.getSesiones());
        Assert.assertEquals(0, registro.getEspectadores());
        Assert.assertEquals(0.0, registro.getRecaudacion().doubleValue(), 0.00001);

        // TODO: Falta comprobar el número de líneas
        //Assert.assertEquals(1 + 1 + 1 + 1 + , registro.getLineas());
    }

    @Test
    @Transactional
    public void testGeneraRegistroBuzonConEspectadores() throws Exception
    {
        Sesion sesion = creaSesion(sala, evento);

        Butaca butaca1 = creaButaca("1", "1", "normal");
        Butaca butaca2 = creaButaca("1", "2", "normal");

        registraCompra(sesion, butaca1, butaca2);

        RegistroBuzon registro = service.generaFicheroRegistros(new Date(), "FL", Arrays.asList(sesion))
                .getRegistroBuzon();

        Assert.assertEquals(1, registro.getSesiones());
        Assert.assertEquals(2, registro.getEspectadores());
        Assert.assertEquals(2.20, registro.getRecaudacion().doubleValue(), 0.00001);

        // TODO: Falta comprobar el número de líneas
    }

    @Test
    @Transactional
    public void testGeneraRegistroBuzonConEspectadoresVariasComprasEnVariasSesiones() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento);
        Sesion sesion2 = creaSesion(sala, evento);

        Butaca butaca1 = creaButaca("1", "1", "normal");
        Butaca butaca2 = creaButaca("1", "2", "normal");

        registraCompra(sesion1, butaca1, butaca2);

        Butaca butaca3 = creaButaca("1", "4", "descuento");

        registraCompra(sesion2, butaca3);

        RegistroBuzon registro = service.generaFicheroRegistros(new Date(), "FL", Arrays.asList(sesion1, sesion2))
                .getRegistroBuzon();

        Assert.assertEquals(2, registro.getSesiones());
        Assert.assertEquals(3, registro.getEspectadores());
        Assert.assertEquals(2.70, registro.getRecaudacion().doubleValue(), 0.00001);

        // TODO: Falta comprobar el número de líneas
    }

    @Test
    @Transactional
    public void testGeneraRegistroSalasUnaSala() throws Exception
    {
        Sesion sesion = creaSesion(sala, evento);

        List<RegistroSala> registros = service.generaFicheroRegistros(new Date(), "", Arrays.asList(sesion))
                .getRegistrosSalas();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(sala.getCodigo(), registros.get(0).getCodigo());
        Assert.assertEquals(sala.getNombre(), registros.get(0).getNombre());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSalasVariasSalas() throws Exception
    {
        Sala sala2 = creaSala("678", "Sala 2");
        Sesion sesion1 = creaSesion(sala, evento);
        Sesion sesion2 = creaSesion(sala2, evento);

        List<RegistroSala> registros = service.generaFicheroRegistros(new Date(), "", Arrays.asList(sesion1, sesion2))
                .getRegistrosSalas();

        Assert.assertEquals(2, registros.size());
        Assert.assertEquals(sala.getCodigo(), registros.get(0).getCodigo());
        Assert.assertEquals(sala.getNombre(), registros.get(0).getNombre());
        Assert.assertEquals(sala2.getCodigo(), registros.get(1).getCodigo());
        Assert.assertEquals(sala2.getNombre(), registros.get(1).getNombre());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesion() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento);

        List<RegistroSesion> registros = service.generaFicheroRegistros(new Date(), "", Arrays.asList(sesion1))
                .getRegistrosSesiones();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(1, registros.get(0).getPeliculas());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("11/12/2013 22:00"), registros.get(0).getFecha());
        Assert.assertEquals("2200", registros.get(0).getHora());
        Assert.assertEquals(0, registros.get(0).getEspectadores());
        Assert.assertEquals(0.0, registros.get(0).getRecaudacion().floatValue(), 0.000001);
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionVariasSesiones() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "1/2/2013", "16:00");
        Butaca butaca1 = creaButaca("1", "1", "normal");
        Butaca butaca2 = creaButaca("1", "2", "normal");

        registraCompra(sesion1, butaca1, butaca2);

        Sesion sesion2 = creaSesion(sala, evento, "3/4/2013", "20:30");
        Butaca butaca3 = creaButaca("1", "3", "descuento");
        Butaca butaca4 = creaButaca("1", "4", "descuento");
        Butaca butaca5 = creaButaca("2", "5", "normal");

        registraCompra(sesion2, butaca3, butaca4, butaca5);

        List<RegistroSesion> registros = service
                .generaFicheroRegistros(new Date(), "", Arrays.asList(sesion1, sesion2)).getRegistrosSesiones();

        Assert.assertEquals(2, registros.size());

        Assert.assertEquals(sala.getCodigo(), registros.get(0).getCodigoSala());
        Assert.assertEquals(1, registros.get(0).getPeliculas());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("1/2/2013 16:00"), registros.get(0).getFecha());
        Assert.assertEquals(2, registros.get(0).getEspectadores());
        Assert.assertEquals(2.20, registros.get(0).getRecaudacion().floatValue(), 0.000001);

        Assert.assertEquals(sala.getCodigo(), registros.get(1).getCodigoSala());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("3/4/2013 20:30"), registros.get(1).getFecha());
        Assert.assertEquals(1, registros.get(1).getPeliculas());
        Assert.assertEquals(3, registros.get(1).getEspectadores());
        Assert.assertEquals(2.10, registros.get(1).getRecaudacion().floatValue(), 0.000001);

        //TODO: Falta incidencia por sesión
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionPelicula() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");

        List<RegistroSesionPelicula> registros = service.generaFicheroRegistros(new Date(), "", Arrays.asList(sesion1))
                .getRegistrosSesionesPeliculas();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(sala.getCodigo(), registros.get(0).getCodigoSala());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("2/3/2013 11:05"), registros.get(0).getFecha());
        Assert.assertEquals(evento.getId(), registros.get(0).getCodigoPelicula());
    }

    @Test
    @Transactional
    public void testGeneraRegistroPeliculaVariasSesiones() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "1/2/2013", "16:00");

        Evento evento2 = creaEvento(tipoEvento);
        Sesion sesion2 = creaSesion(sala, evento2, "3/4/2013", "20:30");
        Sesion sesion3 = creaSesion(sala, evento2, "3/4/2013", "22:30");

        List<RegistroSesionPelicula> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1, sesion2, sesion3)).getRegistrosSesionesPeliculas();

        Assert.assertEquals(3, registros.size());

        RegistroSesionPelicula registro1 = registros.get(0);
        Assert.assertEquals(sala.getCodigo(), registro1.getCodigoSala());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("1/2/2013 16:00"), registro1.getFecha());
        Assert.assertEquals("1600", registro1.getHora());
        Assert.assertEquals(evento.getId(), registro1.getCodigoPelicula());

        RegistroSesionPelicula registro2 = registros.get(1);
        Assert.assertEquals(sala.getCodigo(), registro2.getCodigoSala());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("3/4/2013 20:30"), registro2.getFecha());
        Assert.assertEquals("2030", registro2.getHora());
        Assert.assertEquals(evento2.getId(), registro2.getCodigoPelicula());

        RegistroSesionPelicula registro3 = registros.get(2);
        Assert.assertEquals(sala.getCodigo(), registro3.getCodigoSala());
        Assert.assertEquals(DateUtils.spanishStringWithHourstoDate("3/4/2013 22:30"), registro3.getFecha());
        Assert.assertEquals("2230", registro3.getHora());
        Assert.assertEquals(evento2.getId(), registro3.getCodigoPelicula());
    }

    @Test
    @Transactional
    public void testGeneraRegistroPelicula() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");

        List<RegistroPelicula> registros = service.generaFicheroRegistros(new Date(), "", Arrays.asList(sesion1))
                .getRegistrosPeliculas();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(sala.getCodigo(), registros.get(0).getCodigoSala());
        Assert.assertEquals(evento.getId(), registros.get(0).getCodigoPelicula());
        Assert.assertEquals(evento.getExpediente(), registros.get(0).getCodigoExpediente());
        Assert.assertEquals(evento.getTituloEs(), registros.get(0).getTitulo());
        Assert.assertEquals(evento.getCodigoDistribuidora(), registros.get(0).getCodigoDistribuidora());
        Assert.assertEquals(evento.getNombreDistribuidora(), registros.get(0).getNombreDistribuidora());
        Assert.assertEquals(evento.getVo(), registros.get(0).getVersionOriginal());
        Assert.assertEquals(sesion1.getVersionLinguistica(), registros.get(0).getVersionLinguistica());
        Assert.assertEquals(evento.getSubtitulos(), registros.get(0).getIdiomaSubtitulos());
        Assert.assertEquals(sesion1.getEvento().getFormato(), registros.get(0).getFormatoProyeccion());
    }

    @Test
    @Transactional
    public void testGeneraRegistroPeliculaVariasSesionesYEventos() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento);

        Evento evento2 = creaEvento(tipoEvento, "a", "s", "d", "f", "g", "h");
        Sesion sesion2 = creaSesion(sala, evento2);

        List<RegistroPelicula> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1, sesion2)).getRegistrosPeliculas();

        Assert.assertEquals(2, registros.size());

        RegistroPelicula registro0 = registros.get(0);
        Assert.assertEquals(sala.getCodigo(), registro0.getCodigoSala());
        Assert.assertEquals(evento.getId(), registro0.getCodigoPelicula());
        Assert.assertEquals(evento.getExpediente(), registro0.getCodigoExpediente());
        Assert.assertEquals(evento.getTituloEs(), registro0.getTitulo());
        Assert.assertEquals(evento.getCodigoDistribuidora(), registro0.getCodigoDistribuidora());
        Assert.assertEquals(evento.getNombreDistribuidora(), registro0.getNombreDistribuidora());
        Assert.assertEquals(evento.getVo(), registro0.getVersionOriginal());
        Assert.assertEquals(evento.getSubtitulos(), registro0.getIdiomaSubtitulos());
        Assert.assertEquals(sesion1.getVersionLinguistica(), registro0.getVersionLinguistica());
        Assert.assertEquals(sesion1.getEvento().getFormato(), registro0.getFormatoProyeccion());

        RegistroPelicula registro1 = registros.get(1);
        Assert.assertEquals(sala.getCodigo(), registro1.getCodigoSala());
        Assert.assertEquals(evento2.getId(), registro1.getCodigoPelicula());
        Assert.assertEquals(evento2.getExpediente(), registro1.getCodigoExpediente());
        Assert.assertEquals(evento2.getTituloEs(), registro1.getTitulo());
        Assert.assertEquals(evento2.getCodigoDistribuidora(), registro1.getCodigoDistribuidora());
        Assert.assertEquals(evento2.getNombreDistribuidora(), registro1.getNombreDistribuidora());
        Assert.assertEquals(evento2.getVo(), registro1.getVersionOriginal());
        Assert.assertEquals(evento2.getSubtitulos(), registro1.getIdiomaSubtitulos());
        Assert.assertEquals(sesion2.getVersionLinguistica(), registro1.getVersionLinguistica());
        Assert.assertEquals(sesion2.getEvento().getFormato(), registro1.getFormatoProyeccion());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionProgramada() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");

        List<RegistroSesionProgramada> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1)).getRegistrosSesionesProgramadas();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(sesion1.getSala().getCodigo(), registros.get(0).getCodigoSala());
        Assert.assertEquals("020313", registros.get(0).getFechaSesion());
        Assert.assertEquals(1, registros.get(0).getNumeroSesiones());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionProgramadaVariasSesionesMismoDia() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");
        Sesion sesion2 = creaSesion(sala, evento, "2/3/2013", "12:05");

        List<RegistroSesionProgramada> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1, sesion2)).getRegistrosSesionesProgramadas();

        Assert.assertEquals(1, registros.size());
        Assert.assertEquals(sesion1.getSala().getCodigo(), registros.get(0).getCodigoSala());
        Assert.assertEquals("020313", registros.get(0).getFechaSesion());
        Assert.assertEquals(2, registros.get(0).getNumeroSesiones());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionProgramadaVariasSesionesVariosDias() throws Exception
    {
        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");
        Sesion sesion2 = creaSesion(sala, evento, "2/3/2013", "12:05");
        Sesion sesion3 = creaSesion(sala, evento, "4/3/2013", "22:00");

        List<RegistroSesionProgramada> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1, sesion2, sesion3)).getRegistrosSesionesProgramadas();

        Assert.assertEquals(2, registros.size());

        RegistroSesionProgramada registro1 = registros.get(0);
        Assert.assertEquals(sesion1.getSala().getCodigo(), registro1.getCodigoSala());
        Assert.assertEquals("020313", registro1.getFechaSesion());
        Assert.assertEquals(2, registro1.getNumeroSesiones());

        RegistroSesionProgramada registro2 = registros.get(1);
        Assert.assertEquals(sesion3.getSala().getCodigo(), registro2.getCodigoSala());
        Assert.assertEquals("040313", registro2.getFechaSesion());
        Assert.assertEquals(1, registro2.getNumeroSesiones());
    }

    @Test
    @Transactional
    public void testGeneraRegistroSesionProgramadaVariasSesionesVariosDiasVariasSalas() throws Exception
    {
        Sala sala2 = creaSala("sala2", "Sala 2");

        Sesion sesion1 = creaSesion(sala, evento, "2/3/2013", "11:05");
        Sesion sesion2 = creaSesion(sala, evento, "2/3/2013", "12:05");

        Sesion sesion3 = creaSesion(sala, evento, "4/3/2013", "22:00");

        Sesion sesion4 = creaSesion(sala2, evento, "4/3/2013", "22:00");

        Sesion sesion5 = creaSesion(sala2, evento, "5/3/2013", "23:00");
        Sesion sesion6 = creaSesion(sala2, evento, "5/3/2013", "22:00");

        List<RegistroSesionProgramada> registros = service.generaFicheroRegistros(new Date(), "",
                Arrays.asList(sesion1, sesion2, sesion3, sesion4, sesion5, sesion6)).getRegistrosSesionesProgramadas();

        Assert.assertEquals(4, registros.size());

        RegistroSesionProgramada registro1 = registros.get(0);
        Assert.assertEquals(sala.getCodigo(), registro1.getCodigoSala());
        Assert.assertEquals("020313", registro1.getFechaSesion());
        Assert.assertEquals(2, registro1.getNumeroSesiones());

        RegistroSesionProgramada registro2 = registros.get(1);
        Assert.assertEquals(sala.getCodigo(), registro2.getCodigoSala());
        Assert.assertEquals("040313", registro2.getFechaSesion());
        Assert.assertEquals(1, registro2.getNumeroSesiones());

        RegistroSesionProgramada registro3 = registros.get(2);
        Assert.assertEquals(sala2.getCodigo(), registro3.getCodigoSala());
        Assert.assertEquals("040313", registro3.getFechaSesion());
        Assert.assertEquals(1, registro3.getNumeroSesiones());

        RegistroSesionProgramada registro4 = registros.get(3);
        Assert.assertEquals(sala2.getCodigo(), registro4.getCodigoSala());
        Assert.assertEquals("050313", registro4.getFechaSesion());
        Assert.assertEquals(2, registro4.getNumeroSesiones());
    }
}
