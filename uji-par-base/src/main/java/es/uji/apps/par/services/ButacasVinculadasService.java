/**
 * Gestión de las butacas accesibles y sus butacas asociadas en sesiones numeradas.
 * Copyright (c) 2019 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.core.InjectParam;

import es.uji.apps.par.butacas.DatosButaca;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.ResultadoCompra;

@Service
public class ButacasVinculadasService {
	@Autowired
	Configuration configuration;

	@InjectParam
    ButacasService butacasService;

	@Autowired
	private ComprasDAO comprasDAO;

	@Autowired
	private ComprasService comprasService;

	@Autowired
	private SesionesDAO sesionesDAO;

	@Autowired
	private TarifasDAO tarifasDAO;

	private static final String ADMIN_UID = "admin";
	private static final String BUTACAS_PATH = "/etc/uji/par/butacas/";
	private static final String LOCALIZACION = "general"; // TODO: generalizar a múltiples salas
	private static final String MENSAJE_BLOQUEO = "Butaca discapacitado %s_%s_%s";
	private static final String TARIFA_INVITACION = "Invitació";
	private static final Date fechaInfinito = new Date(95649033600000L);

	private Map<String, List<DatosButaca>> butacasAccesiblesPorLocalizacion = new HashMap<String, List<DatosButaca>>();
	private Map<String, List<DatosButaca>> butacasAsociadasPorLocalizacion = new HashMap<String, List<DatosButaca>>();
	private Map<DatosButaca, DatosButaca> butacasVinculadas = new HashMap<DatosButaca, DatosButaca>();
	private Map<DatosButaca, DatosButaca> butacasAcompanantes = new HashMap<DatosButaca, DatosButaca>();
	private TarifaDTO tarifaInvitacion;

	/**
	 * Compone el mensaje de reserva-bloqueo para la butaca accesible indicada
	 *
	 * @param butaca accesible
	 * @return El mensaje de bloqueo
	 */
	private String mensajeBloqueo(final DatosButaca butaca) {
		return String.format(MENSAJE_BLOQUEO, butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero());
	}

	/**
	 * Calcula la fecha de fin de reserva de las butacas accesibles para la sesión
	 * indicada
	 *
	 * @param sesion del evento
	 * @return La fecha y hora de fin de reserva
	 */
	private Date fechaFinReservaButacasAccesibles(final SesionDTO sesion) {
		return DateUtils.addMinutes(sesion.getFechaCelebracion(),
				-configuration.getMargenFinButacasAccesiblesMinutos());
	}

	/**
	 * Determina si la butaca indicada es accesible
	 * @param butaca
	 * @return true
	 */
	private boolean isButacaAccesible(Butaca butaca) {
		for (final DatosButaca candidata : butacasVinculadas.keySet()) {
			if (isButacaEqual(candidata, butaca)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Comprueba si ambas butacas son la misma
	 *
	 * @param butaca1
	 * @param butaca2
	 * @return true si representan la misma butaca
	 */
	private boolean isButacaEqual(final DatosButaca butaca1, final Butaca butaca2) {
		return butaca1.getFila() == Integer.parseInt(butaca2.getFila())
				&& butaca1.getNumero() == Integer.parseInt(butaca2.getNumero())
				&& butaca1.getLocalizacion().equals(butaca2.getLocalizacion());
	}

	/**
	 * Determina si en el momento de la llamada están en vigor la exclusividad de
	 * las butacas accesibles
	 *
	 * @param sesion del evento
	 * @return true si la exclusividad de butacas accesibles (dos butacas
	 *         vinculadas) están en vigor
	 */
	public boolean enVigorReservaButacasAccesibles(final SesionDTO sesion) {
		final Date ahora = new Date();
		return ahora.before(fechaFinReservaButacasAccesibles(sesion));
	}

	/**
	 * Inicializa las estructuras de datos utilizadas en este servicio
	 *
	 * @throws IOException
	 */
	private void leeJsonsButacas() throws IOException {
		if (!butacasVinculadas.isEmpty()) {
			return;
		}

		// Cargamos JSONs de las butacas vinculadas a butacas para discapacitados (van por parejas)
		for (final String localizacionSala : configuration.getImagenesFondo()) {
			for (final String localizacionZona : configuration.getLocalizacionesEnImagen(localizacionSala)) {
				for (final DatosButaca butaca : parseaJsonButacas(localizacionZona)) {
					if (butaca.isDiscapacidad() && butaca.getNumero_enlazada() >= 0) {
						if (!butacasAccesiblesPorLocalizacion.containsKey(localizacionSala)) {
							butacasAccesiblesPorLocalizacion.put(localizacionSala, new ArrayList<DatosButaca>());
						}
						butacasAccesiblesPorLocalizacion.get(localizacionSala).add(butaca);
					} else {
						if (!butacasAsociadasPorLocalizacion.containsKey(localizacionSala)) {
							butacasAsociadasPorLocalizacion.put(localizacionSala, new ArrayList<DatosButaca>());
						}
						butacasAsociadasPorLocalizacion.get(localizacionSala).add(butaca);
					}
				}
			}
		}

		// Vinculamos cada butaca accesible con su butaca asociada
		for (final DatosButaca butacaAccesible : butacasAccesiblesPorLocalizacion.get(LOCALIZACION)) {
			for (final DatosButaca butaca : butacasAsociadasPorLocalizacion.get(LOCALIZACION)) {
				if (butacaAccesible.getNumero_enlazada() == butaca.getNumero()
						&& butacaAccesible.getNumero() == butaca.getNumero_enlazada()
						&& butacaAccesible.getFila() == butaca.getFila()
						&& butacaAccesible.getLocalizacion().equals(butaca.getLocalizacion())) {
					butacasVinculadas.put(butacaAccesible, butaca);
					break; // Continuamos con la siguiente butaca, ya que van por parejas
				}
			}
		}

		// Vinculamos cada butaca de acompañante con su butaca accesible
		for (final DatosButaca butacaAccesible : butacasVinculadas.keySet()) {
			for (final DatosButaca butaca : butacasAsociadasPorLocalizacion.get(LOCALIZACION)) {
				if (butacaAccesible.getNumero() == butaca.getNumero_enlazada()
						&& butacaAccesible.getFila() == butaca.getFila()
						&& butacaAccesible.getNumero_enlazada() != butaca.getNumero()
						&& butacaAccesible.getLocalizacion().equals(butaca.getLocalizacion())) {
					butacasAcompanantes.put(butaca, butacaAccesible);
					break; // Continuamos con la siguiente butaca, ya que van por parejas
				}
			}
		}

		// Obtenemos la tarifa Invitación
		for (final TarifaDTO tarifa : tarifasDAO.getAll(ADMIN_UID)) {
			if (TARIFA_INVITACION.equals(tarifa.getNombre())) {
				tarifaInvitacion = tarifa;
				break;
			}
		}
	}

	/**
	 * Procesa el JSON de butacas de la localización indicada
	 *
	 * @param localizacion a leer
	 * @return Lista con las butacas
	 * @throws IOException
	 */
	private List<DatosButaca> parseaJsonButacas(final String localizacion) throws IOException {
		final Gson gson = new Gson();
		final Type fooType = new TypeToken<List<DatosButaca>>() {
		}.getType();

		final InputStream inputStream = Files.newInputStream(Paths.get(BUTACAS_PATH + "/" + localizacion + ".json"));
		final InputStreamReader jsonReader = new InputStreamReader(inputStream);

		return gson.fromJson(jsonReader, fooType);
	}

	/**
	 * Bloquea las butacas asociadas a las butacas accesibles de la sesión hasta el
	 * fin de la venta online
	 * Se tiene que llamar obligatoriamente nada más crear una sesión.
	 *
	 * @param sesion  a aprovisionar
	 * @param userUID Identificador del usuario
	 * @return true en caso de que se haya bloqueado alguna butaca asociada
	 */
	public boolean bloqueaButacasVinculadasDiscapacidad(final SesionDTO sesion, final String userUID) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return false;
		}

		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		// Buscamos las butacas a bloquear
		final List<DatosButaca> datosButacas = butacasAccesiblesPorLocalizacion.get(LOCALIZACION);

		// Para cada butaca hacemos una reserva-bloqueo
		boolean resultado = false;
		for (final DatosButaca datosButaca : datosButacas) {
			if (creaReservaBloqueo(sesion, datosButaca, userUID)) {
				resultado = true;
			}
		}

		return resultado;
	}

	/**
	 * Crea la reserva-bloqueo correspondiente a la butaca accesible indicada
	 *
	 * @param sesion               del evento
	 * @param datosButacaAccesible La butaca accesible
	 * @param userUID              El identificador de usuario
	 * @return true en caso de éxito
	 */
	private boolean creaReservaBloqueo(final SesionDTO sesion, final DatosButaca datosButacaAccesible,
			final String userUID) {
		final Date desde = new Date();
		final Date hasta = fechaFinReservaButacasAccesibles(sesion);
		final DatosButaca butacaAsociada = butacasVinculadas.get(datosButacaAccesible);

		final List<Butaca> butacas = new ArrayList<Butaca>();
		final Butaca butaca = new Butaca(butacaAsociada.getLocalizacion(), String.valueOf(tarifaInvitacion.getId()));
		butaca.setPrecio(BigDecimal.ZERO);
		butaca.setFila(String.valueOf(butacaAsociada.getFila()));
		butaca.setNumero(String.valueOf(butacaAsociada.getNumero()));
		butacas.add(butaca);

		// Hacemos la reserva
		final String observaciones = mensajeBloqueo(datosButacaAccesible);
		final ResultadoCompra resultadoCompra = comprasService.reservaButacas(Long.valueOf(sesion.getId()),
				DateUtils.truncate(desde, Calendar.DAY_OF_MONTH), DateUtils.truncate(hasta, Calendar.DAY_OF_MONTH),
				butacas, observaciones,
				desde.getHours(), hasta.getHours(),
				desde.getMinutes(), hasta.getMinutes(), userUID);

		return resultadoCompra.getCorrecta();
	}

	/**
	 * Cambia el estado de la butaca asociada a la butaca accesible
	 *
	 * @param sesion          del evento
	 * @param butacaAccesible La butaca accesible
	 * @param inhabilita      true si el bloqueo es permanente, false si se libera
	 *                        (quedará bloqueada hasta el fin de la venta online)
	 * @param userUID         Identificador del usuario
	 * @return true en caso de éxito
	 */
	private boolean actualizaBloqueoButacaAsociada(final SesionDTO sesion, final DatosButaca butacaAccesible,
			final boolean inhabilita, final String userUID) {
		final List<Compra> reservasBloqueo = getReservasBloqueoButacaAccesible(sesion, butacaAccesible);
		if (reservasBloqueo.isEmpty()) {
			return false;
		}

		for (final Compra bloqueo : reservasBloqueo) {
			Date fecha;
			if (inhabilita) {
				fecha = fechaInfinito;
			} else {
				fecha = fechaFinReservaButacasAccesibles(sesionesDAO.getSesion(sesion.getId(), userUID));
			}
			comprasDAO.actualizarFechaCaducidad(bloqueo.getId(), fecha);
		}

		return true;
	}

	/**
	 * Devuelve los bloqueos-reserva de butacas accesibles en vigor para la sesión
	 * indicada
	 *
	 * @param sesion          Identificador de la sesión
	 * @param butacaAccesible Butaca sobre la que obtener la lista de bloqueos-reserva
	 * @return Lista de bloqueos-reserva, el número máximo de elementos a devolver
	 *         debería ser 1.
	 */
	private List<Compra> getReservasBloqueoButacaAccesible(final SesionDTO sesion, DatosButaca butacaAccesible) {
		// La reserva-bloqueo se hace a nombre de la butaca accesible
		final String textoABloquar = mensajeBloqueo(butacaAccesible);

		return comprasService.getComprasBySesion(Long.valueOf(sesion.getId()), 0,
				"[{\"property\":\"fecha\",\"direction\":\"ASC\"}]", 0, 1000, 0, textoABloquar);
	}

	/**
	 * Devuelve la butaca asociada a la butaca accesible indicada.
	 *
	 * @param butaca accesible
	 * @return su butaca "normal" asociada o null si no existe
	 */
	private DatosButaca getDatosButacaAsociada(final Butaca butaca) {
		for (final DatosButaca butacaAccesible : butacasAccesiblesPorLocalizacion.get(LOCALIZACION)) {
			if (butacaAccesible.getLocalizacion().equals(butaca.getLocalizacion())
					&& butacaAccesible.getFila() == Integer.parseInt(butaca.getFila())
					&& butacaAccesible.getNumero() == Integer.parseInt(butaca.getNumero())) {
				return butacaAccesible;
			}
		}
		return null;
	}

	/**
	 * Inhabilita permanentemente la butaca asociada a la butaca accesible indicada.
	 *
	 * @param sesion  del evento
	 * @param butaca  accesible
	 * @param userUID Identificador del usuario
	 * @return true si la operación se completó con éxito
	 */
	private boolean inhabilitaButacaAsociada(final SesionDTO sesion, final Butaca butaca, final String userUID) {
		if (butaca == null) {
			return false;
		}
		final DatosButaca butacaAsociada = getDatosButacaAsociada(butaca);

		return actualizaBloqueoButacaAsociada(sesion, butacaAsociada, true, userUID);
	}

	/**
	 * Inhabilita permanentemente la butaca asociada a la butaca accesible indicada.
	 * Se tiene que llamar obligatoriamente a este método al completar la compra
	 * para garantizar que las butacas asociadas a las butacas accesibles se
	 * bloqueen indefinidamente par la sesión.
	 *
	 * @param sesionId         Identificador de sesión del evento
	 * @param butacasCompradas Lista con las butacas compradas
	 * @param userUID          Identificador del usuario
	 * @return true si la operación se completó con éxito
	 */
	public boolean inhabilitaButacaAsociada(Long sesionId, final List<Butaca> butacasCompradas, final String userUID) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return false;
		}

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, userUID);
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		boolean resultado = false;
		for (final Butaca butaca : butacasCompradas) {
			if (isButacaAccesible(butaca) && inhabilitaButacaAsociada(sesion, butaca, userUID)) {
				resultado = true;
			}
		}
		return resultado;
	}

	/**
	 * Verifica si la combinación de butacas elegida está permitida
	 * Se tiene que llamar cada vez que se comprueban las butacas en una venta en proceso.
	 * En particular se comprueba que:
	 * 1. Sea una sesión numerada.
	 * 2. Los bloqueos de butacas accesibles estén en vigor para la sesión.
	 * 3. No haya ninguna butaca de acompañante elegida sin su correspondiente butaca accesible.
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butacas  Lista con las butacas seleccionadas
	 * @param butaca   Butaca sobre la que estamos haciendo la comprobación
	 * @return true si las butacas elegidas están permitidas
	 */
	public boolean validaButacas(final Long sesionId, final List<Butaca> butacas, final Butaca butaca) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return false;
		}

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return true;
		}

		// Si ya ha finalizado la reserva de butacas accesibles, no hay nada más que comprobar
		if (!enVigorReservaButacasAccesibles(sesion)) {
			return true;
		}

		// Si la butaca es de acompañante, no se permite si no lo está también su butaca accesible
		for (final DatosButaca butacaAcompanante : butacasAcompanantes.keySet()) {
			if (isButacaEqual(butacaAcompanante, butaca)) {
				final DatosButaca butacaAccesible = butacasAcompanantes.get(butacaAcompanante);
				for (final Butaca candidata : butacas) {
					if (isButacaEqual(butacaAccesible, candidata)) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Determina si la butaca en el evento indicada es de discapacitado (ancho doble)
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butaca
	 * @return true si lo es
	 */
	public boolean esDiscapacitado(final Long sesionId, final DatosButaca butaca) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return false;
		}

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		final List<Compra> reservasBloqueo = getReservasBloqueoButacaAccesible(sesion, butaca);
		for (final Compra bloqueo : reservasBloqueo) {
			if (bloqueo.getHasta().compareTo(fechaInfinito) >= 0)
				return true;
		}
		return false;
	}

	/**
	 * Devuelve la lista de butacas de discapacitado en la sesión en el momento de
	 * la llamada
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @return lista con las butacas de discapacitado
	 */
	public List<DatosButaca> getButacasAccesibles(long sesionId) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return null;
		}

		List<DatosButaca> butacas = new ArrayList<DatosButaca>();

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Collection<DatosButaca> accesibles = butacasAcompanantes.values();
		if (enVigorReservaButacasAccesibles(sesion)) {
			butacas.addAll(accesibles);
		} else {
			for (final DatosButaca butaca : accesibles) {
				if (esDiscapacitado(sesion.getId(), butaca)) {
					butacas.add(butaca);
				}
			}
		}

		return butacas;
	}

	/**
	 * Devuelve la lista de butacas de acompañante en la sesión en el momento de la
	 * llamada
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @return lista con las butacas de acompañante
	 */
	public List<DatosButaca> getButacasAcompanantes(long sesionId) {
		try {
			leeJsonsButacas();
		} catch (IOException e) {
			return null;
		}

		List<DatosButaca> butacas = new ArrayList<DatosButaca>();

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Set<DatosButaca> acompanantes = butacasAcompanantes.keySet();
		if (enVigorReservaButacasAccesibles(sesion)) {
			butacas.addAll(acompanantes);
		} else {
			for (final DatosButaca butaca : acompanantes) {
				final DatosButaca accesible = butacasAcompanantes.get(butaca);
				if (butacasService.estaOcupada(sesion.getId(), butaca.getLocalizacion(),
						String.valueOf(butaca.getFila()), String.valueOf(butaca.getNumero()))
						&& esDiscapacitado(sesion.getId(), accesible)) {
					butacas.add(butaca);
				}
			}
		}

		return butacas;
	}
}