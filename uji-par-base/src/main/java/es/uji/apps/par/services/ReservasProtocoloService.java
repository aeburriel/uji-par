/**
 * Gestión de las reservas automáticas de protocolo
 * Copyright (c) 2019 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.db.EventoDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.Localizacion;
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.FilaNumeracion;
import es.uji.apps.par.utils.Utils;

@Service
public class ReservasProtocoloService {
	@Autowired
	Configuration configuration;

	@Autowired
	ButacasService butacasService;

	@Autowired
	private ButacasVinculadasService butacasVinculadasService;

	@Autowired
	private ComprasDAO comprasDAO;

	@Autowired
	private EventosDAO eventosDAO;

	@Autowired
	private ComprasService comprasService;

	@Autowired
	private LocalizacionesService localizacionesService;

	@Autowired
	private PlantillasService plantillasService;

	@Autowired
	private TarifasService tarifasService;

	private static final String ADMIN_UID = "admin";
	private static final String ZONA_TEATRO_ANFITEATRO_CENTRO = "anfiteatro_central";
	private static final String ZONA_TEATRO_ANFITEATRO_IMPAR = "anfiteatro_lateral_senar";
	private static final String ZONA_TEATRO_ANFITEATRO_PAR = "anfiteatro_lateral_par";
	private static final String ZONA_TEATRO_GENERAL = "general";
	private static TarifaDTO tarifaInvitacion;

	private static final String RESERVA_ANFITEATRO_CENTRAL = "Protocolo Anfiteatro Central";
	class ButacasProtocoloAnfiteatro implements Function<SesionDTO, List<Butaca>> {
		@Override
		public List<Butaca> apply(final SesionDTO sesion) {
			return buildButacas(sesion, ZONA_TEATRO_ANFITEATRO_CENTRO, 1, 2, 1, 7, 1, 1);
		}
	}

	private static final boolean[] BLOQUEO_1_0 = {true};
	private static final String RESERVA_DISTANCIA_SOCIAL = "Distanciamiento Social";
	class ButacasDistanciamientoSocial_UF implements Function <SesionDTO, List<Butaca>> {
		@Override
		public List<Butaca> apply(final SesionDTO sesion) {
			final List<Butaca> butacas = new ArrayList<Butaca>();
			final List<Localizacion> localizaciones = localizacionesService.getLocalizacionesSesion(sesion.getId());
			for (final Localizacion localizacion : localizaciones) {
				final String clocalizacion = localizacion.getCodigo();
				final int filas = Utils.getFilas(clocalizacion);
				FilaNumeracion fn;
				switch (clocalizacion) {
				case ZONA_TEATRO_GENERAL:
					// Filas sala pares
					for (int fila = 2; fila <= filas; fila += 2) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, true);
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_1_0, 0));
					}

					// Filas sala impares
					for (int fila = 1; fila <= filas; fila += 2) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_1_0, 0));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_IMPAR:
					for (int fila = 1; fila <= filas; fila += 2) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_1_0, 0));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_CENTRO:
					for (int fila = 2; fila <= filas; fila += 2) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_1_0, 0));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_PAR:
					for (int fila = 1; fila <= filas; fila += 2) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, true);
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_1_0, 0));
					}
					break;
				}
			}
			return butacas;
		}
	}

	private static final boolean[] BLOQUEO_2_1 = {true, true, false};
	class ButacasDistanciamientoSocial implements Function <SesionDTO, List<Butaca>> {
		@Override
		public List<Butaca> apply(final SesionDTO sesion) {
			final List<Butaca> butacas = new ArrayList<Butaca>();
			final List<Localizacion> localizaciones = localizacionesService.getLocalizacionesSesion(sesion.getId());
			FilaNumeracion fn;
			for (final Localizacion localizacion : localizaciones) {
				final String clocalizacion = localizacion.getCodigo();
				final int filas = Utils.getFilas(clocalizacion);
				int desplazamiento;
				switch (clocalizacion) {
				case ZONA_TEATRO_GENERAL:
					// Butacas impares
					for (int fila = 1; fila <= filas; fila++) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						desplazamiento = fn.isFilaPar() ? 1 : 2;
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_2_1, desplazamiento));
					}

					// Butacas pares
					for (int fila = 1; fila <= filas; fila++) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, true);
						desplazamiento = fn.isFilaPar() ? 2 : 1;
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_2_1, desplazamiento));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_IMPAR:
					for (int fila = 1; fila <= filas; fila++) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						desplazamiento = fn.isFilaPar() ? 2 : 1;
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_2_1, desplazamiento));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_CENTRO:
					for (int fila = 1; fila <= filas; fila++) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, false);
						desplazamiento = fn.isFilaPar() ? 1 : 2;
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_2_1, desplazamiento));
					}
					break;
				case ZONA_TEATRO_ANFITEATRO_PAR:
					for (int fila = 1; fila <= filas; fila++) {
						fn = Utils.getFilaNumeracion(clocalizacion, fila, true);
						desplazamiento = fn.isFilaPar() ? 2 : 1;
						butacas.addAll(buildButacasPatron(sesion, fn, BLOQUEO_2_1, desplazamiento));
					}
					break;
				}
			}
			return butacas;
		}
	}

	@PostConstruct
	private void inicializa() {
		tarifaInvitacion = tarifasService.getTarifaInvitacion(ADMIN_UID);
	}
	
	/**
	 * Devuelve las reservas de protocolo según su descripción
	 *
	 * @param sesion      de la reserva
	 * @param descripcion Texto de la reserva
	 * @return lista de las reservas
	 */
	private List<Compra> reservasProtocolo(final SesionDTO sesion, final String descripcion) {
		return comprasService.getComprasBySesion(Long.valueOf(sesion.getId()), 0,
				"[{\"property\":\"fecha\",\"direction\":\"ASC\"}]", 0, 1000, 0, descripcion);
	}

	/**
	 * Genera un listado de butacas de tipo invitación según un intervalo
	 * Las butacas que no están disponibles las marca como anuladas
	 *
	 * @param sesion  en la que comprobar la disponibilidad de las butacas, si null, no se comprueba
	 * @param zona    Nombre de la localización según el mapa
	 * @param fila0   fila inicial
	 * @param filan   fila final (fila0 <= filan)
	 * @param numero0 número inicial (numero0 <= numeron)
	 * @param numeron número final (numero0 <= numeron)
	 * @param f_paso  paso en la secuencia de filas (f_paso >= 1)
	 * @param n_paso  diferencia entre numero1 y numero0 (n_paso >= 1)
	 * @return Lista de butacas
	 */
	private List<Butaca> buildButacas(final SesionDTO sesion, final String zona, final int fila0, final int filan, final int numero0,
			final int numeron, final int f_paso, final int n_paso) {
		final List<Butaca> butacas = new ArrayList<Butaca>();

		for (int fila = fila0; fila <= filan; fila += f_paso) {
			for (int numero = numero0; numero <= numeron; numero += n_paso) {
				final Butaca butaca = new Butaca(zona, String.valueOf(tarifaInvitacion.getId()));
				butaca.setPrecio(BigDecimal.ZERO);
				butaca.setFila(String.valueOf(fila));
				butaca.setNumero(String.valueOf(numero));
				if (sesion != null) {
					butaca.setAnulada(butacasService.estaOcupada(sesion.getId(), zona, butaca.getFila(), butaca.getNumero()));
				}
				butacas.add(butaca);
			}
		}

		return butacas;
	}

	/**
	 * Genera un listado de butacas de tipo invitación siguiendo un patrón Las
	 * butacas que no están disponibles las marca como anuladas
	 *
	 * @param sesion     en la que comprobar la disponibilidad de las butacas, si
	 *                   null, no se comprueba
	 * @param fila       definición de la fila
	 * @param bloqueadas vector con el patrón de bloqueo
	 * @param offset     primera posición del vector de bloqueo a utilizar
	 * @return Lista de butacas
	 */
	private List<Butaca> buildButacasPatron(final SesionDTO sesion, final FilaNumeracion fila,
			final boolean[] bloqueadas, final int offset) {
		final List<Butaca> butacas = new ArrayList<Butaca>();

		for(int i = 0; i < fila.getCantidadButacas(); i++) {
			if (bloqueadas[(i + offset) % bloqueadas.length]) {
				final Butaca butaca = new Butaca(fila.getLocalizacion(), String.valueOf(tarifaInvitacion.getId()));
				butaca.setPrecio(BigDecimal.ZERO);
				butaca.setFila(String.valueOf(fila.getFila()));
				butaca.setNumero(String.valueOf(fila.getNumeroButaca(i)));
				if (sesion != null) {
					butaca.setAnulada(butacasService.estaOcupada(sesion.getId(), fila.getLocalizacion(), butaca.getFila(), butaca.getNumero()));
				}
				butacas.add(butaca);
			}
		}

		return butacas;
	}

	/**
	 * Gestiona las reservas de protocolo al actualizar una sesión Se tiene que
	 * llamar al crear y al actualizar una sesión.
	 *
	 * @param sesion
	 * @param userUID identificador del usuario
	 */
	@Transactional
	public void gestionaReservasProtocolo(final SesionDTO sesionDTO, final String userUID) {
		// Solamente tenemos que procesar sesiones numeradas
		final EventoDTO evento = eventosDAO.getEventoById(sesionDTO.getParEvento().getId(), ADMIN_UID);
		final Boolean numerado = evento.getAsientosNumerados();
		if (numerado == null || !numerado) {
			return;
		}

		if (configuration.isAforoDistanciamientoSocial()) {
			// No permitimos butacas accesibles
			butacasVinculadasService.cancelaButacasAccesibles(sesionDTO);

			// Anulación de butacas para mantener el distanciamiento social por COVID19
			gestionaReservaDistanciamientoSocial(sesionDTO, userUID);
		}

		final String plantilla = getPlantillaNombre(sesionDTO);
		if (plantilla != null && StringUtils.startsWithIgnoreCase(plantilla, "Teatro, Música, Danza")) {
			// Primera fila del anfiteatro central
			gestionaReservaProtocoloAnfiteatro(sesionDTO, userUID);
		}
	}

	/**
	 * Obtiene el nombre de la plantilla de precios de la sesión
	 *
	 * @param sesion
	 * @return el nombre de la plantilla o null si no se ha podido obtener
	 */
	private String getPlantillaNombre(final SesionDTO sesion) {
		final long salaId = sesion.getParSala().getId();
		final long plantillaId = sesion.getParPlantilla().getId();

		final List<Plantilla> plantillas = plantillasService.getBySala(salaId,
				"[{\"property\":\"id\",\"direction\":\"ASC\"}]", 1, 1000, ADMIN_UID);
		for (final Plantilla plantilla : plantillas) {
			if (plantilla.getId() == plantillaId) {
				return plantilla.getNombre();
			}
		}
		return null;
	}

	/**
	 * Realiza o actualiza una reserva de protocolo
	 * @param sesion
	 * @param generadorButacas Productor de la lista de butacas a incluir en la reserva
	 * @param descripcion de la reserva
	 * @param hasta Fecha de fin de validez de la reserva
	 * @param userUID identificador del usuario
	 * @return true si la operación se ha completado con éxito
	 */
	@Transactional
	private boolean gestionaReserva(final SesionDTO sesion, final Function <SesionDTO, List<Butaca>> generadorButacas, final String descripcion, final Date hasta, final String userUID) {
		final List<Compra> reservas = reservasProtocolo(sesion, descripcion);
		if (reservas.isEmpty()) {
			// Creamos reserva
			final Date desde = new Date();
			final List<Butaca> butacas = generadorButacas.apply(sesion);
			return comprasService.reservaInternaButacas(sesion, desde, hasta, butacas, descripcion, userUID);
		} else {
			// Actualizamos fecha de bloqueo
			for (final Compra reserva : reservas) {
				comprasDAO.actualizarFechaCaducidad(reserva.getId(), hasta);
			}
			return true;
		}
	}

	/**
	 * Reserva de butacas para mantener la distancia social
	 *
	 * @param sesion
	 * @param userUID identificador del usuario
	 * @return true si la operación se ha completado con éxito
	 */
	@Transactional
	private boolean gestionaReservaDistanciamientoSocial(final SesionDTO sesion, final String userUID) {
		final Function<SesionDTO, List<Butaca>> butacas;
		if (configuration.isAforoDistanciamientoSocialUF()) {
			butacas = new ButacasDistanciamientoSocial_UF();
		} else {
			butacas = new ButacasDistanciamientoSocial();
		}

		return gestionaReserva(sesion, butacas, RESERVA_DISTANCIA_SOCIAL, DateUtils.FECHAINFINITO, userUID);
	}

	/**
	 * Reserva de butacas de protocolo en el anfieatro
	 * (dos primeras filas del anfiteatro central)
	 *
	 * @param sesion
	 * @param userUID identificador del usuario
	 * @return true si la operación se ha completado con éxito
	 */
	@Transactional
	private boolean gestionaReservaProtocoloAnfiteatro(final SesionDTO sesion, final String userUID) {
		return gestionaReserva(sesion, new ButacasProtocoloAnfiteatro(), RESERVA_ANFITEATRO_CENTRAL, DateUtils.FECHAINFINITO, userUID);
	}
}
