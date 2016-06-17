package es.uji.apps.par.drawer;

import org.apache.commons.lang3.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MapaDrawerConDivs implements MapaDrawerInterface {
	@Override
	public ByteArrayOutputStream generaImagen(long idSesion, String codigoLocalizacion, boolean mostrarReservadas) throws IOException {
		throw new NotImplementedException("generaImagen");
	}

	@Override
	public ByteArrayOutputStream generaImagenAbono(long abonoId, String codigoLocalizacion, boolean mostrarReservadas, String userUID) throws IOException {
		throw new NotImplementedException("generaImagenAbono");
	}
}
