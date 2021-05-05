package es.uji.apps.par.butacas;

import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.model.Butaca;

public class DatosButaca
{
    private int fila;
    private int numero;
    private int numero_enlazada = -1;
    private String tipo;
    private String localizacion;
    private int xFin;
    private int xIni;
    private int yFin;
    private int yIni;

    public static final String TIPO_ACOMPANANTE = "acompañante";
    public static final String TIPO_ASOCIADA = "asociada";
    public static final String TIPO_DISCAPACIDAD = "discapacidad";

    public DatosButaca() {
    }

    public DatosButaca(final String localizacion, final int fila, final int numero) {
    	this.localizacion = localizacion;
    	this.fila = fila;
    	this.numero = numero;
    }

    public DatosButaca(final Butaca butaca) {
    	this.localizacion = butaca.getLocalizacion();
    	this.fila = Integer.parseInt(butaca.getFila());
    	this.numero = Integer.parseInt(butaca.getNumero());
    	try {
    		this.numero_enlazada = Integer.parseInt(butaca.getNumero_enlazada());
    	} catch (NumberFormatException e) {
    	}
    }

    public DatosButaca(final ButacaDTO butaca) {
    	this.localizacion = butaca.getParLocalizacion().getCodigo();
    	this.fila = Integer.parseInt(butaca.getFila());
    	this.numero = Integer.parseInt(butaca.getNumero());
    }

    @Override
    public boolean equals(Object object) {
    	if (this == object) {
    		return true;
    	}

    	if (object == null) {
    		return false;
    	}

    	final int fila, numero;
    	final String localizacion;
    	if (object.getClass() == DatosButaca.class) {
        	final DatosButaca butaca = (DatosButaca) object;
        	fila = butaca.fila;
        	numero = butaca.numero;
        	localizacion = butaca.localizacion;
        } else if (object.getClass() == Butaca.class) {
        	final Butaca butaca = (Butaca) object;
        	fila = Integer.parseInt(butaca.getFila());
        	numero = Integer.parseInt(butaca.getNumero());
        	localizacion = butaca.getLocalizacion();
        } else if (object.getClass() == ButacaDTO.class) {
        	final ButacaDTO butaca = (ButacaDTO) object;
        	fila = Integer.parseInt(butaca.getFila());
        	numero = Integer.parseInt(butaca.getNumero());
        	localizacion = butaca.getParLocalizacion().getCodigo();
    	} else {
    		return false;
    	}

    	return this.fila == fila && this.numero == numero
    			&& this.localizacion.equals(localizacion);
    }

    @Override
    public String toString() {
    	return String.format("DatosButaca(%s_%d_%d)", localizacion, fila, numero);
    }

    @Override
    public int hashCode() {
    	return toString().hashCode();
    }

    public int getFila()
    {
        return fila;
    }

    public void setFila(int fila)
    {
        this.fila = fila;
    }

    public int getNumero()
    {
        return numero;
    }

    public void setNumero(int numero)
    {
        this.numero = numero;
    }

    public int getNumero_enlazada() {
		return numero_enlazada;
	}

	public void setNumero_enlazada(int numero_enlazada) {
		this.numero_enlazada = numero_enlazada;
	}

	public boolean isAcompanante() {
		return TIPO_ACOMPANANTE.equals(tipo);
	}

	public boolean isAsociada() {
		return TIPO_ASOCIADA.equals(tipo);
	}

	public boolean isDiscapacidad() {
		return TIPO_DISCAPACIDAD.equals(tipo);
	}

	public boolean isNumerada() {
		return fila != 0 || numero != 0;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getLocalizacion()
    {
        return localizacion;
    }

    public void setLocalizacion(String localizacion)
    {
        this.localizacion = localizacion;
    }

    public int getxFin()
    {
        return xFin;
    }

    public void setxFin(int xFin)
    {
        this.xFin = xFin;
    }

    public int getxIni()
    {
        return xIni;
    }

    public void setxIni(int xIni)
    {
        this.xIni = xIni;
    }

    public int getyFin()
    {
        return yFin;
    }

    public void setyFin(int yFin)
    {
        this.yFin = yFin;
    }

    public int getyIni()
    {
        return yIni;
    }

    public void setyIni(int yIni)
    {
        this.yIni = yIni;
    }
}
