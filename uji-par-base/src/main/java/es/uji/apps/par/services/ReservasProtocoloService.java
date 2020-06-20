/**
 * Gestión de las reservas automáticas de protocolo
 * Copyright (c) 2019 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import es.uji.apps.par.model.Plantilla;
import es.uji.apps.par.utils.DateUtils;

@Service
public class ReservasProtocoloService {
	@Autowired
	Configuration configuration;

	@Autowired
	ButacasService butacasService;

	@Autowired
	private ComprasDAO comprasDAO;

	@Autowired
	private EventosDAO eventosDAO;

	@Autowired
	private ComprasService comprasService;

	@Autowired
	private PlantillasService plantillasService;

	@Autowired
	private TarifasService tarifasService;

	private static final String ADMIN_UID = "admin";
	private static final String RESERVA_ANFITEATRO_CENTRAL = "Protocolo Anfiteatro Central";
	private TarifaDTO tarifaInvitacion;

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
	 *
	 * @param zona    Nombre de la localización según el mapa
	 * @param fila0   fila inicial
	 * @param filan   fila final (fila0 <= filan)
	 * @param numero0 número inicial (numero0 <= numeron)
	 * @param numeron número final (numero0 <= numeron)
	 * @param paso    diferencia entre numero1 y numero0 (paso >= 1)
	 * @return Lista de butacas
	 */
	private List<Butaca> buildButacas(final String zona, final int fila0, final int filan, final int numero0,
			final int numeron, final int paso) {
		final List<Butaca> butacas = new ArrayList<Butaca>();

		for (int fila = fila0; fila <= filan; fila++) {
			for (int numero = numero0; numero <= numeron; numero += paso) {
				final Butaca butaca = new Butaca(zona, String.valueOf(tarifaInvitacion.getId()));
				butaca.setPrecio(BigDecimal.ZERO);
				butaca.setFila(String.valueOf(fila));
				butaca.setNumero(String.valueOf(numero));
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
		final String plantilla = getPlantillaNombre(sesionDTO);
		if (plantilla == null) {
			return;
		}

		if (StringUtils.startsWithIgnoreCase(plantilla, "Teatro, Música, Danza")) {
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
	 * Reserva de butacas de protocolo en el anfieatro
	 * (dos primeras filas del anfiteatro central)
	 *
	 * @param sesion
	 * @param userUID identificador del usuario
	 * @return true si la operación se ha completado con éxito
	 */
	@Transactional
	private boolean gestionaReservaProtocoloAnfiteatro(final SesionDTO sesion, final String userUID) {
		final Date hasta = DateUtils.FECHAINFINITO;

		final List<Compra> reservas = reservasProtocolo(sesion, RESERVA_ANFITEATRO_CENTRAL);
		if (reservas.isEmpty()) {
			// Creamos reserva
			final Date desde = new Date();
			final List<Butaca> butacas = buildButacas("anfiteatro_central", 1, 2, 1, 7, 1);
			return comprasService.reservaButacas(sesion, desde, hasta, butacas, RESERVA_ANFITEATRO_CENTRAL, userUID);
		} else {
			// Actualizamos fecha de bloqueo
			for (final Compra reserva : reservas) {
				comprasDAO.actualizarFechaCaducidad(reserva.getId(), hasta);
			}
			return true;
		}
	}
}
