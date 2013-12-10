package es.uji.apps.par.ficheros;

import java.util.Locale;

import es.uji.apps.par.RegistroSerializaException;
import es.uji.apps.par.ficheros.utils.FicherosUtils;

public class RegistroSala
{
    private String codigo;
    private String nombre;

    public String getCodigo()
    {
        return codigo;
    }

    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }

    public String getNombre()
    {
        return nombre;
    }

    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }

    public String serializa() throws RegistroSerializaException
    {
        if (codigo == null)
            throw new RegistroSerializaException("El codigo es null");

        if (nombre == null)
            throw new RegistroSerializaException("El nombre es null");

        FicherosUtils.compruebaCodigoSala(codigo);

        String result = String.format(Locale.ENGLISH, "1%-12s%-30s", codigo, nombre);

        return result;
    }
}
