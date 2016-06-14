package es.uji.apps.par.drawer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MapaDrawerConDivs implements MapaDrawerInterface {
	@Override
	public ByteArrayOutputStream generaImagen(long idSesion, String codigoLocalizacion, boolean mostrarReservadas) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public ByteArrayOutputStream generaImagenAbono(long abonoId, String codigoLocalizacion, boolean mostrarReservadas) throws IOException {
		throw new NotImplementedException();
	}
}
