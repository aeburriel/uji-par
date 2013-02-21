package es.uji.apps.par.model;

import java.util.List;

public class ResultadoCompra
{
    private boolean correcta;
    private List<Butaca> butacasOcupadas;

    public boolean getCorrecta()
    {
        return correcta;
    }

    public void setCorrecta(boolean correcta)
    {
        this.correcta = correcta;
    }

    public List<Butaca> getButacasOcupadas()
    {
        return butacasOcupadas;
    }

    public void setButacasOcupadas(List<Butaca> butacasOcupadas)
    {
        this.butacasOcupadas = butacasOcupadas;
    }

}
