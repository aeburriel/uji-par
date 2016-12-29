package es.uji.apps.par.exceptions;

@SuppressWarnings("serial")
public class SesionNoEncontradaException extends GeneralPARException
{
    public SesionNoEncontradaException(Long sesionId)
    {
        super(SESION_NO_ENCONTRADA_CODE, "Codigo sesión: " + sesionId);
    }

	public SesionNoEncontradaException()
	{
		super(SESION_NO_ENCONTRADA_CODE);
	}
}
