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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.uji.apps.par.butacas.DatosButaca;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.ComprasDAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.dao.TarifasDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.exceptions.ButacaAccesibleAnularSinAnularButacaAcompanante;
import es.uji.apps.par.exceptions.ButacaOcupadaAlActivarException;
import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.Compra;
import es.uji.apps.par.model.ResultadoCompra;

@Service
public class ButacasVinculadasService {
	@Autowired
	Configuration configuration;

	@Autowired
    ButacasService butacasService;

	@Autowired
	private ButacasDAO butacasDAO;

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
	private static final String MENSAJE_BLOQUEO = "Butaca discapacitado";
	private static final String TARIFA_INVITACION = "Invitació";
	private static final Date FECHAINFINITO = new Date(95649033600000L);

	private Multimap<String, DatosButaca> butacasAccesiblesPorLocalizacion = MultimapBuilder.hashKeys().arrayListValues().build();
	private Multimap<String, DatosButaca> butacasAcompanantesPorLocalizacion = MultimapBuilder.hashKeys().arrayListValues().build();
	private Multimap<String, DatosButaca> butacasAsociadasPorLocalizacion = MultimapBuilder.hashKeys().arrayListValues().build();
	private Multimap<DatosButaca, DatosButaca> butacasVinculadas = MultimapBuilder.hashKeys().arrayListValues().build();
	private Map<DatosButaca, DatosButaca> butacasAcompanantes = new HashMap<DatosButaca, DatosButaca>();
	private TarifaDTO tarifaInvitacion;

	/**
	 * Compone el mensaje de reserva-bloqueo para la butaca accesible indicada
	 *
	 * @param butaca accesible o null para no incluir referencia a la butaca
	 * @return El mensaje de bloqueo
	 */
	private String mensajeBloqueo(final DatosButaca butaca) {
		if (butaca == null) {
			return MENSAJE_BLOQUEO;
		} else {
			return MENSAJE_BLOQUEO + String.format(" %s_%s_%s ", butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero());
		}
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
	 * @return true si lo es
	 */
	private boolean isButacaAccesible(final Butaca butaca) {
		final DatosButaca datosButaca = new DatosButaca(butaca);
		return butacasVinculadas.containsKey(datosButaca);
	}

	/**
	 * Determina si la compra indicada es una reserva-bloqueo de butacas accesibles
	 * @param compra a comprobar
	 * @return true si lo es
	 */
	private boolean isReservaBloqueo(final CompraDTO compra) {
		final String mensaje = compra.getObservacionesReserva();
		if (mensaje == null) {
			return false;
		}
		return mensaje.startsWith(mensajeBloqueo(null));
	}

	/**
	 * Determina si la butaca indicada es accesible
	 * @param butaca
	 * @return la butaca accesible o null si no lo es
	 */
	private DatosButaca getButacaAccesible(ButacaDTO butaca) {
		for (final DatosButaca candidata : butacasVinculadas.keySet()) {
			if (isButacaEqual(candidata, butaca)) {
				return candidata;
			}
		}
		return null;
	}

	/**
	 * Determina la butaca accesible correspondiente a una butaca asociada
	 * @param butaca
	 * @return la butaca accesible o null si no lo es
	 */
	private DatosButaca getButacaAccesiblePorAsociada(ButacaDTO butaca) {
		for (final DatosButaca accesible : butacasVinculadas.keySet()) {
			for (final DatosButaca candidata : butacasVinculadas.get(accesible)) {
				if (isButacaEqual(candidata, butaca)) {
					return accesible;
				}
			}
		}
		return null;
	}

	/**
	 * Determina si la butaca indicada es de acompañante
	 * @param butaca
	 * @return la butaca de acompañante o null si no lo es
	 */
	private DatosButaca getButacaAcompanante(final ButacaDTO butaca) {
		for (final DatosButaca candidata : butacasAcompanantes.keySet()) {
			if (isButacaEqual(candidata, butaca)) {
				return candidata;
			}
		}
		return null;
	}

	/**
	 * Devuelve la butaca de acompañante correspondiente a la butaca accesible indicada
	 * @param acesible butaca accesible
	 * @return la butaca de acompañante vinculada o null si no se encuentra
	 */
	private DatosButaca getButacaAcompanantePorAccesible(final DatosButaca accesible) {
		for (final DatosButaca acompanante : butacasAcompanantes.keySet()) {
			final DatosButaca candidataAccesible = butacasAcompanantes.get(acompanante);
			if (candidataAccesible.equals(accesible)) {
				return acompanante;
			}
		}

		return null;
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
	 * Comprueba si ambas butacas son la misma
	 *
	 * @param butaca1
	 * @param butaca2
	 * @return true si representan la misma butaca
	 */
	private boolean isButacaEqual(final DatosButaca butaca1, final ButacaDTO butaca2) {
		return butaca1.getFila() == Integer.parseInt(butaca2.getFila())
				&& butaca1.getNumero() == Integer.parseInt(butaca2.getNumero())
				&& butaca1.getLocalizacion().equals(butaca2.getParLocalizacion().getCodigo());
	}

	/**
	 * Determina si la Reserva indicada corresponde a un bloqueo de butaca accesible
	 *
	 * @param bloqueo La reserva-bloqueo
	 * @return true si lo es
	 */
	private boolean isBloqueoVentaAccesible(final Compra bloqueo) {
		return bloqueo.getHasta().compareTo(FECHAINFINITO) >= 0;
	}

	/**
	 * Determina si la Reserva indicada corresponde a un bloqueo de butaca accesible
	 *
	 * @param bloqueo La reserva-bloqueo
	 * @return true si lo es
	 */
	private boolean isBloqueoVentaAccesible(final CompraDTO bloqueo) {
		return bloqueo.getHasta().compareTo(FECHAINFINITO) >= 0;
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
		return enVigorReservaButacasAccesibles(sesion, ahora);
	}

	/**
	 * Determina si en el momento de la llamada están en vigor la exclusividad de
	 * las butacas accesibles
	 *
	 * @param sesionId identificador de sesión
	 * @return true si la exclusividad de butacas accesibles (dos butacas
	 *         vinculadas) están en vigor
	 */
	public boolean enVigorReservaButacasAccesibles(final long sesionId) {
		final SesionDTO sesionDTO = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		return enVigorReservaButacasAccesibles(sesionDTO);
	}

	/**
	 * Determina si en la fecha indicada están en vigor la exclusividad de
	 * las butacas accesibles
	 *
	 * @param sesion del evento
	 * @param fecha a comparar
	 * @return true si la exclusividad de butacas accesibles (dos butacas
	 *         vinculadas) están en vigor
	 */
	public boolean enVigorReservaButacasAccesibles(final SesionDTO sesion, final Date fecha) {
		return fecha.before(fechaFinReservaButacasAccesibles(sesion));
	}

	/**
	 * Inicializa las estructuras de datos utilizadas en este servicio
	 *
	 * @throws IOException
	 */
	@PostConstruct
	private void leeJsonsButacas() throws IOException {
		if (!butacasVinculadas.isEmpty()) {
			return;
		}

		// Cargamos JSONs de las butacas vinculadas a butacas para discapacitados (van por parejas)
		for (final String localizacionSala : configuration.getImagenesFondo()) {
			for (final String localizacionZona : configuration.getLocalizacionesEnImagen(localizacionSala)) {
				for (final DatosButaca butaca : parseaJsonButacas(localizacionZona)) {
					if (butaca.isDiscapacidad()) {
						butacasAccesiblesPorLocalizacion.put(localizacionSala, butaca);
					} else if (butaca.isAsociada()) {
						butacasAsociadasPorLocalizacion.put(localizacionSala, butaca);
					} else if (butaca.isAcompanante()) {
						butacasAcompanantesPorLocalizacion.put(localizacionSala, butaca);
					}
				}
			}
		}

		// Vinculamos cada butaca accesible con su butaca asociada
		for (final DatosButaca butacaAccesible : butacasAccesiblesPorLocalizacion.get(LOCALIZACION)) {
			for (final DatosButaca butaca : butacasAsociadasPorLocalizacion.get(LOCALIZACION)) {
				if (butaca.getNumero_enlazada() == butacaAccesible.getNumero()
						&& butaca.getFila() == butacaAccesible.getFila()
						&& butaca.getLocalizacion().equals(butacaAccesible.getLocalizacion())) {
					butacasVinculadas.put(butacaAccesible, butaca);
				}
			}
		}

		// Vinculamos cada butaca de acompañante con su butaca accesible
		for (final DatosButaca butacaAccesible : butacasVinculadas.keySet()) {
			for (final DatosButaca butaca : butacasAcompanantesPorLocalizacion.get(LOCALIZACION)) {
				if (butaca.getNumero_enlazada() == butacaAccesible.getNumero()
						&& butaca.getFila() == butacaAccesible.getFila()
						&& butaca.getLocalizacion().equals(butacaAccesible.getLocalizacion())) {
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
	 * Actualiza el bloqueo de las butacas asociadas a las butacas accesibles de la sesión hasta el
	 * fin de la venta online
	 * Se tiene que llamar obligatoriamente nada más actualizar una sesión.
	 *
	 * @param sesion  a aprovisionar
	 * @param userUID Identificador del usuario
	 * @return true en caso de que se haya bloqueado alguna butaca asociada
	 */
	@Transactional
	public boolean actualizaBloqueoButacasVinculadasDiscapacidad(final SesionDTO sesion, final String userUID) {
		// Buscamos las butacas a bloquear
		final List<Compra> bloqueos = getReservasBloqueoButacaAccesible(sesion, null);
		final Date hasta = fechaFinReservaButacasAccesibles(sesion);

		boolean resultado = false;
		for (Compra bloqueo : bloqueos) {
			// Solo hay que actualizar las reserva-bloqueo, no los bloqueos-venta accesibles
			if (!isBloqueoVentaAccesible(bloqueo)) {
				comprasDAO.actualizarFechaCaducidad(bloqueo.getId(), hasta);
				resultado = true;
			}
		}

		return resultado;
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
	@Transactional
	public boolean bloqueaButacasVinculadasDiscapacidad(final SesionDTO sesion, final String userUID) {
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		// Buscamos las butacas a bloquear
		final Collection<DatosButaca> datosButacas = butacasAccesiblesPorLocalizacion.get(LOCALIZACION);

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
	@Transactional
	private boolean creaReservaBloqueo(final SesionDTO sesion, final DatosButaca datosButacaAccesible,
			final String userUID) {
		if (datosButacaAccesible == null) {
			return false;
		}
		final Date desde = new Date();
		final Date hasta = fechaFinReservaButacasAccesibles(sesion);
		final Collection<DatosButaca> butacasAsociadas = butacasVinculadas.get(datosButacaAccesible);

		final List<Butaca> butacas = new ArrayList<Butaca>();
		for (DatosButaca butacaAsociada : butacasAsociadas) {
			final Butaca butaca = new Butaca(butacaAsociada.getLocalizacion(), String.valueOf(tarifaInvitacion.getId()));
			butaca.setPrecio(BigDecimal.ZERO);
			butaca.setFila(String.valueOf(butacaAsociada.getFila()));
			butaca.setNumero(String.valueOf(butacaAsociada.getNumero()));
			butacas.add(butaca);
		}

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
	 * Cambia el estado de las butacas asociadas a la butaca accesible
	 *
	 * @param sesion          del evento
	 * @param butacaAccesible La butaca accesible
	 * @param inhabilita      true si el bloqueo es permanente, false si se libera
	 *                        (quedará bloqueada hasta el fin de la venta online)
	 * @param userUID         Identificador del usuario
	 * @return true en caso de éxito
	 */
	@Transactional
	private boolean actualizaBloqueoButacasAsociadas(final SesionDTO sesion, final DatosButaca butacaAccesible,
			final boolean inhabilita, final String userUID) {
		if (butacaAccesible == null) {
			return false;
		}
		final List<Compra> reservasBloqueo = getReservasBloqueoButacaAccesible(sesion, butacaAccesible);
		if (reservasBloqueo.isEmpty()) {
			return false;
		}

		boolean resultado = true;
		for (final Compra bloqueo : reservasBloqueo) {
			Date fecha;
			if (inhabilita) {
				fecha = FECHAINFINITO;
			} else {
				fecha = fechaFinReservaButacasAccesibles(sesionesDAO.getSesion(sesion.getId(), userUID));
			}
			if (!comprasDAO.actualizarFechaCaducidad(bloqueo.getId(), fecha)) {
				resultado = false;
			}
		}

		return resultado;
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
	private List<Compra> getReservasBloqueoButacaAccesible(final SesionDTO sesion, final DatosButaca butacaAccesible) {
		// La reserva-bloqueo se hace a nombre de la butaca accesible
		final String textoABloquar = mensajeBloqueo(butacaAccesible);

		return comprasService.getComprasBySesion(Long.valueOf(sesion.getId()), 0,
				"[{\"property\":\"fecha\",\"direction\":\"ASC\"}]", 0, 1000, 0, textoABloquar);
	}

	/**
	 * Devuelve la butaca accesible indicada como DatosButaca.
	 *
	 * @param butaca accesible
	 * @return objeto DatosButaca o null si no es butaca accesible
	 */
	private DatosButaca getDatosButaca(final Butaca butaca) {
		if (butaca == null) {
			return null;
		}
		final int fila = Integer.parseInt(butaca.getFila());
		final int numero = Integer.parseInt(butaca.getNumero());
		for (final DatosButaca butacaAccesible : butacasAccesiblesPorLocalizacion.get(LOCALIZACION)) {
			if (butacaAccesible.getNumero() == numero
					&& butacaAccesible.getFila() == fila
					&& butacaAccesible.getLocalizacion().equals(butaca.getLocalizacion())) {
				return butacaAccesible;
			}
		}
		return null;
	}

	/**
	 * Inhabilita permanentemente las butacas asociadas a la butaca accesible indicada.
	 *
	 * @param sesion  del evento
	 * @param butaca  accesible
	 * @param userUID Identificador del usuario
	 * @return true si la operación se completó con éxito
	 */
	private boolean inhabilitaButacasAsociadas(final SesionDTO sesion, final Butaca butaca, final String userUID) {
		final DatosButaca butacaAsociada = getDatosButaca(butaca);
		if (butacaAsociada == null) {
			return false;
		}

		return actualizaBloqueoButacasAsociadas(sesion, butacaAsociada, true, userUID);
	}

	/**
	 * Inhabilita permanentemente las butacas asociadas a la butaca accesible indicada.
	 * Se tiene que llamar obligatoriamente a este método al completar la compra
	 * para garantizar que las butacas asociadas a las butacas accesibles se
	 * bloqueen indefinidamente para la sesión.
	 *
	 * @param sesionId         Identificador de sesión del evento
	 * @param butacasCompradas Lista con las butacas compradas
	 * @param userUID          Identificador del usuario
	 * @return true si la operación se completó con éxito
	 */
	@Transactional
	public boolean inhabilitaButacasAsociadas(final Long sesionId, final List<Butaca> butacasCompradas, final String userUID) {
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, userUID);
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		boolean resultado = false;
		for (final Butaca butaca : butacasCompradas) {
			if (butaca != null && isButacaAccesible(butaca) && inhabilitaButacasAsociadas(sesion, butaca, userUID)) {
				resultado = true;
			}
		}
		return resultado;
	}

	/**
	 * Libera las butacas asociadas implicadas en una venta cancelada.
	 * Se tiene que llamar obligatoriamente a este método justo antes
	 * de cancelar una compra para garantizar la integridad de los
	 * bloqueos-reserva.
	 *
	 * @param sesionId         Identificador de sesión del evento
	 * @param userUID          Identificador del usuario
	 * @throws ButacaOcupadaException si se trata de un bloqueo-reserva por una butaca accesible en vigor
	 * @return true si la operación se completó con éxito
	 */
	@Transactional
	public boolean ventaAnulada(final Long compraId) throws ButacaOcupadaException {
		final CompraDTO compra = comprasDAO.getCompraById(compraId);
		if(!compra.getParSesion().getParEvento().getAsientosNumerados()) {
			return true;
		}

		// No podemos anular una reserva-bloqueo con su butaca accesible vendida
		if (isReservaBloqueo(compra) && isBloqueoVentaAccesible(compra) && !compra.getParButacas().isEmpty()) {
			// No permitimos anular parcialmente una reserva-bloqueo,
			// por lo que es seguro comprobar solo la primera butaca
			final ButacaDTO butaca = compra.getParButacas().get(0);
			final DatosButaca butacaAccesible = getButacaAccesiblePorAsociada(butaca);
			throw new ButacaOcupadaException(compra.getParSesion().getId(), butacaAccesible.getLocalizacion(),
					String.valueOf(butacaAccesible.getFila()), String.valueOf(butacaAccesible.getNumero()));
		}

		boolean resultado = false;
		for (final ButacaDTO butaca : compra.getParButacas()) {
			// Saltamos las butacas anuladas
			if (butaca == null || butaca.getAnulada()) {
				continue;
			}
			final DatosButaca butacaAccesible = getButacaAccesible(butaca);
			if (butacaAccesible != null
					&& actualizaBloqueoButacasAsociadas(compra.getParSesion(), butacaAccesible, false, ADMIN_UID)) {
				resultado = true;
			}
		}

		return resultado;
	}

	/**
	 * Bloquea las butacas asociadas implicadas en una venta desanulada.
	 * Se tiene que llamar obligatoriamente a este método nada más
	 * desanular una compra para garantizar la integridad de los
	 * bloqueos-reserva.
	 *
	 * @param sesionId         Identificador de sesión del evento
	 * @param userUID          Identificador del usuario
	 * @throws ButacaOcupadaAlActivarException si alguna de las butacas accesibles,
	 * 		de acompañante o asociadas está ocupada
	 * @return true si la operación se completó con éxito
	 */
	@Transactional
	public boolean ventaDesanulada(final Long compraId) throws ButacaOcupadaAlActivarException {
		final CompraDTO compra = comprasDAO.getCompraById(compraId);
		if(!compra.getParSesion().getParEvento().getAsientosNumerados()) {
			return true;
		}
		final SesionDTO sesion = compra.getParSesion();

		// Si es una reserva-bloqueo accesible,
		// solo podemos proceder si la butaca accesible y la de acompañante están disponibles
		if (isReservaBloqueo(compra) && !compra.getParButacas().isEmpty()) {
			// No permitimos anular parcialmente una reserva-bloqueo,
			// por lo que es seguro comprobar solo la primera butaca
			final ButacaDTO butaca = compra.getParButacas().get(0);
			final DatosButaca butacaAccesible = getButacaAccesiblePorAsociada(butaca);
			if (butacaAccesible != null) {
				final Compra compraAccesible = butacasService.getCompra(sesion.getId(), butacaAccesible.getLocalizacion(), String.valueOf(butacaAccesible.getFila()), String.valueOf(butacaAccesible.getNumero()));
				if (compraAccesible != null) {
					throw new ButacaOcupadaAlActivarException(sesion.getId(), butacaAccesible.getLocalizacion(),
							String.valueOf(butacaAccesible.getFila()), String.valueOf(butacaAccesible.getNumero()), "",
							butaca.getParCompra().getTaquilla());
				}

				final DatosButaca butacaAcompanante = getButacaAcompanantePorAccesible(butacaAccesible);
				final Compra compraAcompanante = butacasService.getCompra(sesion.getId(), butacaAcompanante.getLocalizacion(), String.valueOf(butacaAcompanante.getFila()), String.valueOf(butacaAcompanante.getNumero()));
				if (compraAcompanante != null) {
					throw new ButacaOcupadaAlActivarException(sesion.getId(), butacaAcompanante.getLocalizacion(),
							String.valueOf(butacaAcompanante.getFila()), String.valueOf(butacaAcompanante.getNumero()), "",
							butaca.getParCompra().getTaquilla());

				}
			}
		}

		boolean resultado = false;
		for (final ButacaDTO butaca : compra.getParButacas()) {
			// Saltamos las butacas anuladas
			if (butaca == null || butaca.getAnulada()) {
				continue;
			}
			final DatosButaca butacaAccesible = getButacaAccesible(butaca);
			if (butacaAccesible != null && enVigorReservaButacasAccesibles(sesion, compra.getFecha())) {
				if (actualizaBloqueoButacasAsociadas(compra.getParSesion(), butacaAccesible, true, ADMIN_UID)) {
					resultado = true;
				} else {
					// Las butacas asociadas están bloqueadas permanentemente por otra compra, no podemos continuar
					throw new ButacaOcupadaAlActivarException(butaca
							.getParSesion().getId(), butaca
							.getParLocalizacion().getCodigo(),
							butaca.getFila(), butaca.getNumero(), "",
							butaca.getParCompra().getTaquilla());
				}
			}
		}

		return resultado;
	}

	/**
	 * Procesa una lista de butacas a anular y gestiona las reservas-bloqueo
	 * @param idsButacas Las butacas a anular
	 * @throws ButacaAccesibleAnularSinAnularButacaAcompanante al intentar
	 * 		anular una butaca accesible sin anular su butaca de acompañante
	 * @throws ButacaOcupadaException al intentar anular una butaca de una
	 * 		reserva-bloqueo
	 * @return true si se ha actualizado alguna reserva-bloqueo accesible
	 */
	@Transactional
	public boolean anularButacas(final List<Long> idsButacas)
			throws ButacaAccesibleAnularSinAnularButacaAcompanante, ButacaOcupadaException {
		boolean resultado = false;
		for (final Long idButaca : idsButacas) {
			final ButacaDTO butaca = butacasDAO.getButaca(idButaca);
			if (butaca == null) {
				continue;
			}

			// Solo hay que procesar las butacas de sesiones numeradas
			final CompraDTO compra = butaca.getParCompra();
			if(!compra.getParSesion().getParEvento().getAsientosNumerados()) {
				continue;
			}

			// Las butacas de una reserva-bloqueo no se deben anular individualmente
			if (isReservaBloqueo(compra)) {
				throw new ButacaOcupadaException(compra.getParSesion().getId(), butaca.getParLocalizacion().getCodigo(), butaca.getFila(), butaca.getNumero());
			}

			final SesionDTO sesion = compra.getParSesion();
			final DatosButaca butacaAccesible = getButacaAccesible(butaca);
			if (butacaAccesible != null && enVigorReservaButacasAccesibles(sesion, compra.getFecha())) {
				// Hay que comprobar que si esta butaca accesible tiene también una butaca de acompañante comprada,
				// esta esté también incluída en la lista de butacas a cancelar
				final DatosButaca butacaAcompanante = getButacaAcompanantePorAccesible(butacaAccesible);
				boolean encontrada = false;
				for (final Long idCandidata : idsButacas) {
					if (idButaca.equals(idCandidata)) {
						continue;
					}

					final ButacaDTO candidata = butacasDAO.getButaca(idCandidata);
					if (isButacaEqual(butacaAcompanante, candidata)) {
						encontrada = true;
						break;
					}
				}

				// Si la butaca de acompanante no está entre las butacas a borrar,
				// hay que comprobar si forma parte de esta venta
				if (!encontrada) {
					for (final ButacaDTO butacaComprada : compra.getParButacas()) {
						if (!butacaComprada.getAnulada() && butacaAcompanante.equals(getButacaAcompanante(butacaComprada))) {
							throw new ButacaAccesibleAnularSinAnularButacaAcompanante(compra.getId(), butacaAccesible, butacaAcompanante);
						}
					}
				}

				// Ajustamos el bloqueo-reserva de la butaca accesible
				if (actualizaBloqueoButacasAsociadas(compra.getParSesion(), butacaAccesible, false, ADMIN_UID)) {
					resultado = true;
				}
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
	 * 3. Todas las butacas asociadas formen parte del bloqueo-reserva.
	 * 4. No haya ninguna butaca de acompañante elegida sin su correspondiente butaca accesible.
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butacas  Lista con las butacas seleccionadas
	 * @param butaca   Butaca sobre la que estamos haciendo la comprobación
	 * @return true si las butacas elegidas están permitidas
	 */
	public boolean validaButacas(final Long sesionId, final List<Butaca> butacas, final Butaca butaca) {
		if (butaca == null) {
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

		// Si es butaca accesible,
		// el bloqueo-reserva debe contener todas las butacas asociadas
		final DatosButaca butacaAccesible = getDatosButaca(butaca);
		if (butacaAccesible != null && enVigorReservaButacasAccesibles(sesion)) {
			final List<Compra> bloqueos = getReservasBloqueoButacaAccesible(sesion, butacaAccesible);
			final Collection<DatosButaca> asociadasRealidad = butacasVinculadas.get(butacaAccesible);
			final Set<DatosButaca> asociadasBloqueo = new HashSet<DatosButaca>();
			for (final Compra bloqueo : bloqueos) {
				final CompraDTO compraDTO = comprasService.getCompraById(bloqueo.getId());
				for (final ButacaDTO butacaBloqueada : compraDTO.getParButacas()) {
					DatosButaca bloqueada = new DatosButaca(butacaBloqueada.getParLocalizacion().getCodigo(),
							Integer.parseInt(butacaBloqueada.getFila()), Integer.parseInt(butacaBloqueada.getNumero()));
					asociadasBloqueo.add(bloqueada);
				}
			}

			if (!asociadasBloqueo.containsAll(asociadasRealidad)) {
				return false;
			}
		}

		// Si la butaca es de acompañante, no se permite si no lo está también su butaca accesible
		for (final DatosButaca butacaAcompanante : butacasAcompanantes.keySet()) {
			if (isButacaEqual(butacaAcompanante, butaca)) {
				final DatosButaca butacaAccesibleAsociada = butacasAcompanantes.get(butacaAcompanante);
				for (final Butaca candidata : butacas) {
					if (candidata != null && isButacaEqual(butacaAccesibleAsociada, candidata)) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Verifica que el cambio de butaca solicitado sea posible
	 * Se tiene que llamar antes de hacer un cambio de butaca.
	 * @param butaca Butaca sobre la que estamos haciendo la comprobación
	 * @param fila Nueva fila, en la misma zona que la butaca inicial
	 * @param numero Nuevo número, en la misma zona que la butaca inicial
	 * @return true si la butaca destino es aceptable
	 */
	public boolean cambiaFilaNumero(final ButacaDTO butaca, final String fila, final String numero) {
		if (butaca == null || fila == null || numero == null) {
			return false;
		}
		final CompraDTO compra = butaca.getParCompra();
		// No se puede reasignar una butaca de una reserva-bloqueo
		if (isReservaBloqueo(compra)) {
			return false;
		}

		// Si la butaca es accesible
		final DatosButaca butacaAccesible = getButacaAccesible(butaca);
		if (butacaAccesible != null && esDiscapacitado(compra.getId(), butacaAccesible)) {
			return false;
		}

		// Si la butaca puede ser de acompañante
		final DatosButaca butacaAcompanante = getButacaAcompanante(butaca);
		if (butacaAcompanante != null) {
			// Confirmamos si realmente es de acompañante buscando su butaca accesible
			// asociada en la venta
			for (final ButacaDTO butacaComprada : compra.getParButacas()) {
				if (!butacaComprada.getAnulada() && butacaAcompanante.equals(getButacaAcompanante(butacaComprada))) {
					return false;
				}
			}
		}

		// Butaca origen convencional, validamos butaca destino
		// Si la reserva de butacas accesibles está en vigor,
		// comprobamos que el destino no sea butaca accesible o de acompañante
		if (enVigorReservaButacasAccesibles(compra.getParSesion())) {
			final DatosButaca candidata = new DatosButaca();
			candidata.setLocalizacion(butaca.getParLocalizacion().getCodigo());
			candidata.setFila(Integer.parseInt(fila));
			candidata.setNumero(Integer.parseInt(numero));

			if (butacasVinculadas.containsKey(candidata) || butacasAcompanantes.containsKey(candidata)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Determina si la butaca en el evento indicada es de acompañante (está ocupada
	 * y su butaca accesible asociada vendida en la misma operación)
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butaca
	 * @return true si lo es
	 */
	public boolean esAcompanante(final Long sesionId, final DatosButaca butaca) {
		final DatosButaca accesible = butacasAcompanantes.get(butaca);
		if (accesible == null) {
			return false;
		}
		final Compra compraAccesible = butacasService.getCompra(sesionId, butaca.getLocalizacion(), String.valueOf(butaca.getFila()), String.valueOf(butaca.getNumero()));
		if (compraAccesible == null) {
			return false;
		}
		final Compra compraAcompanante = butacasService.getCompra(sesionId, accesible.getLocalizacion(), String.valueOf(accesible.getFila()), String.valueOf(accesible.getNumero()));
		if (compraAcompanante == null) {
			return false;
		}
		return butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), String.valueOf(butaca.getFila()),	String.valueOf(butaca.getNumero()))
				&& esDiscapacitado(sesionId, accesible)
				&& compraAccesible.getId() == compraAcompanante.getId();
	}

	/**
	 * Determina si la butaca en el evento indicada es de discapacitado (ancho doble)
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butaca
	 * @return true si lo es
	 */
	public boolean esDiscapacitado(final Long sesionId, final DatosButaca butaca) {
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		if (!sesion.getParEvento().getAsientosNumerados()) {
			return false;
		}

		final List<Compra> reservasBloqueo = getReservasBloqueoButacaAccesible(sesion, butaca);
		for (final Compra bloqueo : reservasBloqueo) {
			if (isBloqueoVentaAccesible(bloqueo))
				return true;
		}
		return false;
	}

	/**
	 * Devuelve la lista de butacas de discapacitado en la sesión en el momento de
	 * la llamada
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @return lista con las butacas de discapacitado o null en caso de error
	 */
	public List<DatosButaca> getButacasAccesibles(final long sesionId) {
		final List<DatosButaca> butacas = new ArrayList<DatosButaca>();

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Collection<DatosButaca> accesibles = butacasVinculadas.keySet();
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
	public List<DatosButaca> getButacasAcompanantes(final long sesionId) {
		final List<DatosButaca> butacas = new ArrayList<DatosButaca>();

		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Set<DatosButaca> acompanantes = butacasAcompanantes.keySet();
		if (enVigorReservaButacasAccesibles(sesion)) {
			butacas.addAll(acompanantes);
		} else {
			for (final DatosButaca butaca : acompanantes) {
				if (esAcompanante(sesion.getId(), butaca)) {
					butacas.add(butaca);
				}
			}
		}

		return butacas;
	}
}
