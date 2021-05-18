package es.uji.apps.par.drawer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.core.InjectParam;
import es.uji.apps.par.butacas.DatosButaca;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.config.ConfigurationSelector;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Abono;
import es.uji.apps.par.model.SesionAbono;
import es.uji.apps.par.services.AbonosService;
import es.uji.apps.par.services.ButacasService;
import es.uji.apps.par.services.ButacasVinculadasService;

import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaDrawer implements MapaDrawerInterface
{
    @InjectParam
    ButacasService butacasService;

    @InjectParam
    private AbonosService abonosService;

    @InjectParam
    Configuration configuration;

    @InjectParam
    ConfigurationSelector configurationSelector;

    @InjectParam
    ButacasVinculadasService butacasVinculadasService;

    @InjectParam
    private SesionesDAO sesionesDAO;

    private BufferedImage butacaOcupada;
    private BufferedImage butacaReservada;
    private BufferedImage butacaOcupadaAcompanante;
    private BufferedImage butacaReservadaAcompanante;
    private BufferedImage butacaOcupadaDiscapacitado;
    private BufferedImage butacaReservadaDiscapacitado;
    private BufferedImage butacaPresentada;
    private BufferedImage butacaVaciaDiscapacitado;
    private BufferedImage butacaVaciaAcompanante;

    // Para cuando necesitamos saber el (x, y) que ocupa la butaca en la imagen
    private Map<String, DatosButaca> datosButacas;
    
    // Imágenes de distintas localizaciones
    private Map<String, BufferedImage> imagenes;

	@Autowired
    public MapaDrawer(ButacasService butacasService, AbonosService abonosService, Configuration configuration, ConfigurationSelector configurationSelector, ButacasVinculadasService butacasVinculadasService, SesionesDAO sesionesDAO) throws IOException
    {
        this.butacasService = butacasService;
        this.abonosService = abonosService;
        this.configuration = configuration;
        this.configurationSelector = configurationSelector;
        this.butacasVinculadasService = butacasVinculadasService;
        this.sesionesDAO = sesionesDAO;
        cargaImagenes();
        leeJson();
    }

    private String[] getLocalizacionesEnImagen(String localizacion)
    {
    	return configuration.getLocalizacionesEnImagen(localizacion);
    }

    public ByteArrayOutputStream generaImagen(long idSesion, String codigoLocalizacion, boolean mostrarReservadas) throws IOException
    {
        BufferedImage img = dibujaButacas(idSesion, codigoLocalizacion, mostrarReservadas);

        return imagenToOutputStream(img);
    }

    public ByteArrayOutputStream generaImagenAbono(long abonoId, String codigoLocalizacion, boolean mostrarReservadas, String userUID) throws IOException
    {
        Abono abono = abonosService.getAbono(abonoId, userUID);
        List<Long> sesionIds = new ArrayList<Long>();
        for (SesionAbono sesion : abono.getSesiones()) {
            sesionIds.add(sesion.getSesion().getId());
        }

        BufferedImage img = dibujaButacas(sesionIds, codigoLocalizacion, mostrarReservadas);

        return imagenToOutputStream(img);
    }

    private void leeJson() throws IOException
    {
        if (datosButacas == null)
        {
            datosButacas = new HashMap<String, DatosButaca>();

            for (String localizacion : configuration.getImagenesFondo())
            {
            	for (String localizacionImagen: configuration.getLocalizacionesEnImagen(localizacion))
            		loadJsonLocalizacion(localizacionImagen);
            }
        }
    }

    private void loadJsonLocalizacion(String localizacion) throws IOException
    {
		if (!configuration.isLoadedFromResource()) {
			List<DatosButaca> listaButacas = parseaJsonButacas(localizacion);

			for (DatosButaca datosButaca : listaButacas) {
				datosButacas.put(
						String.format("%s_%d_%d", datosButaca.getLocalizacion(), datosButaca.getFila(),
								datosButaca.getNumero()), datosButaca);
			}
		}
    }

    private List<DatosButaca> parseaJsonButacas(String localizacion) throws IOException
    {
        final Gson gson = new Gson();
        final Type fooType = new TypeToken<List<DatosButaca>>()
        {
        }.getType();
        
        final InputStream inputStream = Files.newInputStream(Paths.get(configuration.getPathJson(localizacion)));
        final InputStreamReader jsonReader = new InputStreamReader(inputStream);
        List<DatosButaca> butacas;
        try {
        	butacas = gson.fromJson(jsonReader, fooType);
        } finally {
        	jsonReader.close();
        	inputStream.close();
        }
        return butacas;
    }

    private ByteArrayOutputStream imagenToOutputStream(BufferedImage img) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        bos.close();
        return bos;
    }

    private BufferedImage dibujaButacas(long idSesion, String localizacionDeImagen, boolean mostrarReservadas)
    {
        List<Long> sesionIds = new ArrayList<Long>();
        sesionIds.add(idSesion);

        return dibujaButacas(sesionIds, localizacionDeImagen, mostrarReservadas);
    }

    private BufferedImage dibujaButacas(List<Long> sesionIds, String localizacionDeImagen, boolean mostrarReservadas)
    {
        final BufferedImage imgButacas = imagenes.get(localizacionDeImagen);
        final BufferedImage imgResult = new BufferedImage(imgButacas.getWidth(), imgButacas.getHeight(), imgButacas.getType());
        final Graphics2D graphics = imgResult.createGraphics();
        graphics.drawImage(imgButacas, 0, 0, null);

		// Señalamos las butacas accesibles y de acompañante
		for (final Long idSesion : sesionIds) {
			final SesionDTO sesionDTO = sesionesDAO.getSesion(idSesion, "admin");
			if (butacasVinculadasService.enVigorReservaButacasAccesibles(sesionDTO)) {
				for (final DatosButaca butaca : butacasVinculadasService.getButacasAccesibles(sesionDTO, false)) {
					graphics.drawImage(butacaVaciaDiscapacitado, butaca.getxIni(), butaca.getyIni(), null);
				}
				for (final DatosButaca butaca : butacasVinculadasService.getButacasAcompanantes(sesionDTO)) {
					graphics.drawImage(butacaVaciaAcompanante, butaca.getxIni(), butaca.getyIni(), null);
				}
			} else if(butacasVinculadasService.enCambioAforo(sesionDTO)) {
				for (final DatosButaca butaca : butacasVinculadasService.getButacasAccesibles(sesionDTO, true)) {
					graphics.drawImage(butacaOcupada, butaca.getxIni(), butaca.getyIni(), null);
				}

			}
		}

		final boolean destacaUsadas = configurationSelector.showButacasHanEntradoEnDistintoColor();
        for (final String localizacion : getLocalizacionesEnImagen(localizacionDeImagen)) {
            for (final Long idSesion : sesionIds) {
                final List<ButacaDTO> butacas = butacasService.getButacas(idSesion, localizacion);

                for (final ButacaDTO butacaDTO : butacas) {
                    if (butacaDTO.getAnulada() == null || !butacaDTO.getAnulada()) {
                        final String key = String.format("%s_%s_%s", butacaDTO.getParLocalizacion().getCodigo(),
                                butacaDTO.getFila(), butacaDTO.getNumero());
                        final DatosButaca butaca = datosButacas.get(key);

                        final BufferedImage imagenOcupada;

                        if (mostrarReservadas && destacaUsadas && butacaPresentada != null && butacaDTO.getPresentada() != null) {
                            imagenOcupada = butacaPresentada;
                        } else {
                            if (esDiscapacitado(idSesion, butaca)) {
                                if (mostrarReservadas && esReserva(butacaDTO))
                                    imagenOcupada = butacaReservadaDiscapacitado;
                                else
                                    imagenOcupada = butacaOcupadaDiscapacitado;
                            } else if (esAcompanante(idSesion, butaca)) {
                            	if (mostrarReservadas && esReserva(butacaDTO))
                            		imagenOcupada = butacaReservadaAcompanante;
                            	else
                            		imagenOcupada = butacaOcupadaAcompanante;
                            } else {
                                if (mostrarReservadas && esReserva(butacaDTO))
                                    imagenOcupada = butacaReservada;
                                else
                                    imagenOcupada = butacaOcupada;
                            }
                        }

                        if (butaca != null)
                            graphics.drawImage(imagenOcupada, butaca.getxIni(), butaca.getyIni(), null);
                    }
                }
            }
        }

        return imgResult;
    }

    private Boolean esReserva(ButacaDTO butacaDTO)
    {
        return butacaDTO.getParCompra().getReserva();
    }

    private boolean esAcompanante(Long sesionId, DatosButaca butaca) {
    	if (butaca == null)
    		return false;

    	return butacasVinculadasService.esAcompananteOcupada(sesionId, butaca);
    }

	private boolean esDiscapacitado(Long sesionId, DatosButaca butaca)
    {
		if (butaca == null)
			return false;

		return butacasVinculadasService.esDiscapacitadoOcupada(sesionId, butaca);
    }

    private void cargaImagenes() throws IOException
    {
		if (!configuration.isLoadedFromResource()) {
			if (imagenes == null) {
				imagenes = new HashMap<String, BufferedImage>();

				for (String localizacion : configuration.getImagenesFondo()) {
					imagenes.put(localizacion, readImage(localizacion));
				}
			}

			if (butacaOcupada == null) {
				butacaOcupada = readImage("ocupada");
			}

			if (butacaOcupadaAcompanante == null) {
				butacaOcupadaAcompanante = readImage("ocupadaAcompanante");
			}

			if (butacaOcupadaDiscapacitado == null) {
				butacaOcupadaDiscapacitado = readImage("ocupadaDiscapacitado");
			}

			if (butacaReservada == null) {
				butacaReservada = readImage("reservada");
			}

			if (butacaReservadaAcompanante == null) {
				butacaReservadaAcompanante = readImage("reservadaAcompanante");
			}

			if (butacaReservadaDiscapacitado == null) {
				butacaReservadaDiscapacitado = readImage("reservadaDiscapacitado");
			}

			if (butacaPresentada == null) {
				butacaPresentada = readImage("presentada");
			}
			if (butacaVaciaDiscapacitado == null) {
				butacaVaciaDiscapacitado = readImage("vaciaDiscapacitado");
			}
			if (butacaVaciaAcompanante == null) {
				butacaVaciaAcompanante = readImage("vaciaAcompanante");
			}
		}
    }

    private BufferedImage readImage(final String localizacion) throws IOException {
        final File f = new File(configuration.getPathImagen(localizacion + ".png"));
        if (!f.exists()) {
            return null;
        }
        return ImageIO.read(f);
    }

}
