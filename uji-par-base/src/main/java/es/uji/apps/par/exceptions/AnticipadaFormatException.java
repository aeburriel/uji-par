package es.uji.apps.par.exceptions;

public class AnticipadaFormatException extends GeneralPARException
{
    public AnticipadaFormatException(String anticipada)
    {
        super(ANTICIPADA_FORMAT_ERROR_CODE, ANTICIPADA_FORMAT_ERROR + ": " + anticipada);
    }
}
