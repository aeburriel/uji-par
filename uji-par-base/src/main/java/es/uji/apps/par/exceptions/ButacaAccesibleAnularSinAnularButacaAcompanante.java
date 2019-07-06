package es.uji.apps.par.exceptions;

import es.uji.apps.par.butacas.DatosButaca;

@SuppressWarnings("serial")
public class ButacaAccesibleAnularSinAnularButacaAcompanante extends GeneralPARException {
	private final Long ventaId;
	private final DatosButaca accesible;
	private final DatosButaca acompanante;

    public ButacaAccesibleAnularSinAnularButacaAcompanante(final Long ventaId, final DatosButaca accesible, final DatosButaca acompanante) {
        super(BUTACA_OCUPADA_CODE, BUTACA_OCUPADA + String.format("ventaId = %d, butaca accesible=%s, butaca acompañante", ventaId, accesible, acompanante));
        
        this.ventaId = ventaId;
        this.accesible = accesible;
        this.acompanante = acompanante;
    }

    public Long getVentaId() {
        return ventaId;
    }
   
    public DatosButaca getButacaAccesible() {
    	return accesible;
    }

    public DatosButaca getButacaAcompanante() {
    	return acompanante;
    }
}
