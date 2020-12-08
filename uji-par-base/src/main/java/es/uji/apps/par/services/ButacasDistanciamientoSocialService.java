/**
 * Gestión de las compras de butacas con distanciamiento social en sesiones numeradas.
 * Copyright (c) 2020 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedSetMultimap;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.utils.FilaNumeracion;
import es.uji.apps.par.utils.Utils;

@Service
public class ButacasDistanciamientoSocialService {
	@Autowired
	Configuration configuration;

	@Autowired
	ButacasService butacasService;

	@Autowired
	private SesionesDAO sesionesDAO;

	@Autowired
	private ReservasProtocoloService reservasProtocolo;

	private static final String ADMIN_UID = "admin";

	private static int BUTACAS_GUARDA;

	@PostConstruct
	private void inicializa() {
		 BUTACAS_GUARDA = configuration.getAforoDistanciamientoSocialUFGuarda();
	}

	/**
	 * Clasifica por filas un conjunto de butacas
	 *
	 * @param butacas a clasificar
	 * @return Multimap
	 */
	private SortedSetMultimap<FilaNumeracion, Butaca> butacas2Filas(final List<Butaca> butacas) {
		// Al clasificar por localización y fila solo hay que comparar el número de la butaca
		class SortbyNumeracion implements Comparator<Butaca> {
			@Override
			public int compare(final Butaca b0, final Butaca b1) {
				if (b0 == null && b1 == null) {
					return 0;
				}
				if (b0 == null) {
					return -1;
				}
				if (b1 == null) {
					return 1;
				}

				final int n0 = Integer.parseInt(b0.getNumero());
				final int n1 = Integer.parseInt(b1.getNumero());
				return n0 - n1;
			}

		}
		final SortedSetMultimap<FilaNumeracion, Butaca> filas = MultimapBuilder.hashKeys()
				.treeSetValues(new SortbyNumeracion()).build();

		for (final Butaca butaca : butacas) {
			final FilaNumeracion fila = Utils.getFilaNumeracion(butaca);
			filas.put(fila, butaca);
		}

		return filas;
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param fila FilaNumeracion
	 * @param bi0  índice inferior del grupo de butacas
	 * @param bi1  índice superior del grupo de butacas
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final FilaNumeracion fila, final int bi0, final int bi1) {
		int fi0 = fila.getIndice(fila.getPrimera());
		int fin = fila.getIndice(fila.getUltima());

		final List<Integer> indices = new ArrayList<Integer>();
		if (bi0 - fi0 > BUTACAS_GUARDA) {
			fi0 = bi0 - BUTACAS_GUARDA;
		}
		if (fin - bi1 > BUTACAS_GUARDA) {
			fin = bi1 + BUTACAS_GUARDA;
		}
		for (int i = fi0; i < bi0; i++) {
			indices.add(i);
		}
		for (int i = bi1 + 1; i <= fin; i++) {
			indices.add(i);
		}

		final Set<Butaca> entorno = Sets.newHashSet();
		for (final Integer i : indices) {
			final Butaca butaca = new Butaca();
			butaca.setLocalizacion(fila.getLocalizacion());
			butaca.setFila(String.valueOf(fila.getFila()));
			butaca.setNumero(String.valueOf(fila.getNumeroButaca(i)));

			entorno.add(butaca);
		}
		return entorno;
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param butaca0 Butaca
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final Butaca butaca0) {
		final FilaNumeracion fila = Utils.getFilaNumeracion(butaca0);
		final int bi = fila.getIndice(Integer.parseInt(butaca0.getNumero()));
		return getButacasEntorno(fila, bi, bi);
	}

	/**
	 * Devuelve conjunto de butacas alrededor de la butaca indicada, contenido en la
	 * distancia de guarda
	 *
	 * @param fila          FilaNumeracion
	 * @param seleccionadas Butacas seleccionadas
	 * @return Set con las butacas en el área de distanciamiento social
	 */
	private Set<Butaca> getButacasEntorno(final FilaNumeracion fila, final SortedSet<Butaca> seleccionadas) {
		final int bi0 = fila.getIndice(Integer.parseInt(seleccionadas.first().getNumero()));
		final int bi1 = fila.getIndice(Integer.parseInt(seleccionadas.last().getNumero()));

		return getButacasEntorno(fila, bi0, bi1);
	}

	/**
	 * Determina si la butaca indicada puede estar disponible, atendiendo al
	 * criterio de distanciamiento social. Este método se tiene que llamar al
	 * generar la lista de butacas ocupadas.
	 *
	 * @param sesion de la compra
	 * @param butaca a comprobar
	 * @return true si la butaca puede estar disponible para su venta
	 */
	public boolean isButacaLibrePermitida(final long sesionId, final Butaca butaca) {
		if (!configuration.isAforoDistanciamientoSocial() || !configuration.isAforoDistanciamientoSocialUF()) {
			return true;
		}

		// No comprobamos restricciones si la sesión usa bloqueos estáticos de aforo
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		if (reservasProtocolo.isDistanciamientoSocialSimple(sesion)) {
			return true;
		}

		final Set<Butaca> entorno = getButacasEntorno(butaca);

		for (final Butaca candidata : entorno) {
			if (butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), butaca.getFila(),
					candidata.getNumero())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Determina si la butaca indicada puede estar disponible, atendiendo al
	 * criterio de distanciamiento social. Este método se tiene que llamar al
	 * generar la lista de butacas ocupadas.
	 *
	 * @param sesion    de la compra
	 * @param butacaDTO a comprobar
	 * @return true si la butaca puede estar disponible para su venta
	 */
	public boolean isButacaLibrePermitida(final long sesionId, final ButacaDTO butacaDTO) {
		final Butaca butaca = new Butaca();
		butaca.setLocalizacion(butacaDTO.getParLocalizacion().getCodigo());
		butaca.setFila(butacaDTO.getFila());
		butaca.setNumero(butacaDTO.getNumero());

		return isButacaLibrePermitida(sesionId, butaca);
	}

	private boolean isButacaOcupada(final long sesionId, final Butaca butaca) {
		return butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero());
	}

	/**
	 * Verifica que las butacas seleccionadas de una misma fila respetan la
	 * distancia de seguridad
	 * Se comprueba que, o bien están alineadas con uno de los extremos de la fila,
	 * o bien se respeta la distancia de guarda con la siguiente butaca ocupada.
	 * No se permite una distancia de guarda mayor que la mínima, con el fin de
	 * maximizar el aforo.
	 *
	 * @param fila    FilaNumeracion
	 * @param butacas asociadas a la fila
	 * @return true si no hay huecos innecesarios por alguno de los extremos
	 */
	private boolean isButacasFilaGuarda(final long sesionId, final FilaNumeracion fila,
			final SortedSet<Butaca> butacas) {
		final int i0 = fila.getIndice(Integer.parseInt(butacas.first().getNumero()));
		final int i1 = fila.getIndice(Integer.parseInt(butacas.last().getNumero()));
		final String localizacion = fila.getLocalizacion();

		// 1. Comprobamos si alguna de las butacas está en el extremo de la fila
		if (i0 == fila.getIndice(fila.getPrimera()) || i1 == fila.getIndice(fila.getUltima())) {
			return true;
		}

		// 2. Comprobamos si alguno de los extremos está exactamente a la distancia de
		// seguridad de la siguiente butaca ocupada
		final Butaca candidata = new Butaca();
		candidata.setLocalizacion(localizacion);
		candidata.setFila(String.valueOf(fila.getFila()));

		int distancia = 0;
		boolean ocupada = false;
		int i = i0;
		while (i-- > 0) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesionId, candidata)) {
				ocupada = true;
				break;
			}
			distancia++;
		}
		if (ocupada && distancia == BUTACAS_GUARDA) {
			return true;
		}

		distancia = 0;
		ocupada = false;
		i = i1;
		while (++i < fila.getCantidadButacas()) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesionId, candidata)) {
				ocupada = true;
				break;
			}
			distancia++;
		}
		if (ocupada && distancia == BUTACAS_GUARDA) {
			return true;
		}

		return false;
	}

	/**
	 * Comprueba si entre las butacas seleccionadas de una misma fila hay algún
	 * hueco
	 *
	 * @param fila    FilaNumeracion
	 * @param butacas asociadas a la fila
	 * @return true si no hay ningún hueco entre la primera y última butaca
	 *         seleccionada
	 */
	private boolean isButacasFilaCompacta(final FilaNumeracion fila, final SortedSet<Butaca> butacas) {
		final int n0 = Integer.parseInt(butacas.first().getNumero());
		final int n1 = Integer.parseInt(butacas.last().getNumero());
		return fila.getCantidadButacas(n0, n1) == butacas.size();
	}

	/**
	 * Verifica si la combinación de butacas elegida esté permitida
	 * Se tiene que llamar cada vez que se comprueban las butacas en una venta
	 * en proceso. En particular se comprueba que:
	 *   1. Las restricciones de distanciamiento social estén activas.
	 *   2. Sea una sesión numerada.
	 *   3. En cada fila, las butacas elegidas sean contiguas.
	 *   4. En cada fila, se respete el espacio de guarda en ambos extremos o
	 *   exactamente la distancia de seguridad con la butaca ocupada más cercana.
	 *
	 * @param sesionId Identificador de sesión del evento
	 * @param butacas  Lista con las butacas seleccionadas
	 * @return true si las butacas elegidas están permitidas
	 */
	public boolean validaButacas(final Long sesionId, final List<Butaca> butacas) {
		// 1. Estado distanciamiento social
		if (!configuration.isAforoDistanciamientoSocial() || !configuration.isAforoDistanciamientoSocialUF()) {
			return true;
		}

		// 2. Sesión numerada
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		final Boolean numerado = sesion.getParEvento().getAsientosNumerados();
		if (numerado == null || !numerado) {
			return true;
		}

		// Aforo distanciamiento social simple activo
		if (reservasProtocolo.isDistanciamientoSocialSimple(sesion)) {
			return true;
		}

		final SortedSetMultimap<FilaNumeracion, Butaca> b2f = butacas2Filas(butacas);
		for (final FilaNumeracion fila : b2f.keySet()) {
			final SortedSet<Butaca> butacasFila = b2f.get(fila);

			// 3. Butacas contiguas
			if (!isButacasFilaCompacta(fila, butacasFila)) {
				return false;
			}

			// 4. Distancia de seguridad con la butaca ocupada más próxima
			if (!isButacasFilaGuarda(sesionId, fila, butacasFila)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Verifica que el cambio de butaca solicitado sea posible Se tiene que llamar
	 * antes de hacer un cambio de butaca.
	 *
	 * @param butaca Butaca sobre la que estamos haciendo la comprobación
	 * @param fila   Nueva fila, en la misma localización que la butaca inicial
	 * @param numero Nuevo número, en la misma localización que la butaca inicial
	 * @return true si la butaca destino es aceptable
	 */
	public boolean cambiaFilaNumero(final ButacaDTO butaca, final String fila, final String numero) {
		if (butaca == null || fila == null || numero == null) {
			return false;
		}

		if (!(configuration.isAforoDistanciamientoSocial() && configuration.isAforoDistanciamientoSocialUF())) {
			return true;
		}

		// No permitimos cambios de butacas
		return false;
	}

}
