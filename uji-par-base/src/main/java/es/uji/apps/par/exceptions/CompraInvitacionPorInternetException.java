package es.uji.apps.par.exceptions;


@SuppressWarnings("serial")
public class CompraInvitacionPorInternetException extends GeneralPARException
{
    public CompraInvitacionPorInternetException()
    {
        super(COMPRA_INVITACION_INTERNET_NO_DISPONIBLE_CODE);
    }
}
