package es.uji.apps.par.drawer;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public interface MapaDrawerInterface {
	ByteArrayOutputStream generaImagen(long idSesion, String codigoLocalizacion, boolean mostrarReservadas) throws IOException;
	ByteArrayOutputStream generaImagenAbono(long abonoId, String codigoLocalizacion, boolean mostrarReservadas, String userUID) throws IOException;
}
