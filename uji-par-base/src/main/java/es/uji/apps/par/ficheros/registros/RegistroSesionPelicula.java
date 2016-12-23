package es.uji.apps.par.ficheros.registros;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.uji.apps.par.exceptions.RegistroSerializaException;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;

public class RegistroSesionPelicula
{
    private static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("ddMMyy");

    private String codigoSala;
    private Date fecha;
    private String hora;
    private int codigoPelicula;

    public String getCodigoSala()
    {
        return codigoSala;
    }

    public void setCodigoSala(String codigoSala)
    {
        this.codigoSala = codigoSala;
    }

    public Date getFecha()
    {
        return fecha;
    }

    public void setFecha(Date fecha)
    {
        this.fecha = fecha;
    }

    public String getHora()
    {
        return hora;
    }

    public void setHora(String hora)
    {
        this.hora = hora;
    }

    public int getCodigoPelicula()
    {
        return codigoPelicula;
    }

    public void setCodigoPelicula(int codigoPelicula)
    {
        this.codigoPelicula = codigoPelicula;
    }

    public String serializa() throws RegistroSerializaException
    {
    	Sala.checkValidity(codigoSala);
    	Sesion.checkFechaCelebracion(fecha);
    	Sesion.checkHoraCelebracion(hora);
        String result;
        synchronized(DAY_FORMAT) {
            result = String.format(Locale.ENGLISH, "3%-12s%s%s%05d", codigoSala, DAY_FORMAT.format(fecha), hora,
                codigoPelicula);
        }

        return result;
    }
}
