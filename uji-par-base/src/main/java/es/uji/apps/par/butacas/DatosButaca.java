package es.uji.apps.par.butacas;

public class DatosButaca
{
    private int fila;
    private int numero;
    private int numero_enlazada = -1;
    private boolean discapacidad = false;
    private String localizacion;
    private int xFin;
    private int xIni;
    private int yFin;
    private int yIni;

    public DatosButaca()
    {
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

	public boolean isDiscapacidad() {
		return discapacidad;
	}

	public void setDiscapacidad(boolean discapacidad) {
		this.discapacidad = discapacidad;
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
