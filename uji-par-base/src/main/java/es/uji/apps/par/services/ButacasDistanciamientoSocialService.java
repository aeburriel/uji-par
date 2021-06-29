/**
 * Gestión de las compras de butacas con distanciamiento social en sesiones numeradas.
 * Copyright (c) 2020 Antonio Eugenio Burriel <aeburriel@gmail.com>
 */
package es.uji.apps.par.services;

import java.util.ArrayList;
import java.util.Collection;
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

import es.uji.apps.par.butacas.DatosButaca;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.LocalizacionDTO;
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
	ButacasVinculadasService butacasVinculadasService;

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
	public boolean isButacaLibrePermitida(final long sesionId, final List<Butaca> butacas, final Butaca butaca) {
		if (!configuration.isAforoDistanciamientoSocial() || !configuration.isAforoDistanciamientoSocialUF()) {
			return true;
		}

		// No comprobamos restricciones si la sesión usa bloqueos estáticos de aforo
		final SesionDTO sesion = sesionesDAO.getSesion(sesionId, ADMIN_UID);
		if (reservasProtocolo.isDistanciamientoSocialSimple(sesion)) {
			return true;
		}

		// Excluimos de la comprobación las butacas accesibles
		if (butacasVinculadasService.esButacaAccesibleDisponible(sesion, butaca)) {
			return true;
		}

		final Set<Butaca> entorno = getButacasEntorno(butaca);

		for (final Butaca candidata : entorno) {
			if (butacasService.estaOcupada(sesionId, butaca.getLocalizacion(), butaca.getFila(), candidata.getNumero())
					|| butacasVinculadasService.esButacaAccesibleAjenaDisponible(sesion, butacas, candidata)) {
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
	 * @param butacas   seleccionadas en la compra
	 * @param butacaDTO a comprobar
	 * @return true si la butaca puede estar disponible para su venta
	 */
	public boolean isButacaLibrePermitida(final long sesionId, final List<Butaca> butacas, final ButacaDTO butacaDTO) {
		final Butaca butaca = new Butaca();
		butaca.setLocalizacion(butacaDTO.getParLocalizacion().getCodigo());
		butaca.setFila(butacaDTO.getFila());
		butaca.setNumero(butacaDTO.getNumero());

		return isButacaLibrePermitida(sesionId, butacas, butaca);
	}

	private boolean isButacaOcupada(final SesionDTO sesion, final Collection<Butaca> butacas, final Butaca butaca) {
		return (butacasService.estaOcupada(sesion.getId(), butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero()) &&
				!butacasVinculadasService.esButacaAsociadaPropia(sesion, butacas, butaca))
				|| butacasVinculadasService.esButacaAccesibleAjenaDisponible(sesion, butacas, butaca);
	}

	/**
	 * Verifica que las butacas seleccionadas de una misma fila respetan la
	 * distancia de seguridad
	 * Se comprueba que:
	 * las butacas estén alineadas con uno de los extremos de la fila, siempre que se
	 * respete la distancia de seguridad mínima con la siguiente butaca,
	 * o bien se respeten las distancia de guarda en ambos extremos con la siguiente
	 * butaca ocupada, o accesible / de acompañante disponible para su venta.
	 * No se permite una distancia de guarda mayor que la mínima, con el fin de
	 * maximizar la ocupación del aforo.
	 *
	 * @param fila    FilaNumeracion
	 * @param butacas asociadas a la fila
	 * @return true si no hay huecos innecesarios por alguno de los extremos
	 */
	private boolean isButacasFilaGuarda(final SesionDTO sesion, final FilaNumeracion fila,
			final SortedSet<Butaca> butacas) {
		final int i0 = fila.getIndice(Integer.parseInt(butacas.first().getNumero()));
		final int i1 = fila.getIndice(Integer.parseInt(butacas.last().getNumero()));

		// 0. Fila completa, nada más que comprobar
		if (butacas.size() == fila.getCantidadButacas()) {
			return true;
		}

		// 1. Comprobamos las distancias de seguridad en los extremos a la siguiente butaca ocupada
		final Butaca candidata = new Butaca();
		candidata.setLocalizacion(fila.getLocalizacion());
		candidata.setFila(String.valueOf(fila.getFila()));

		int distanciaSup = 0;
		boolean ocupadaSup = false;
		int i = i1;
		while (++i < fila.getCantidadButacas()) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesion, butacas, candidata)) {
				ocupadaSup = true;
				break;
			}
			distanciaSup++;
		}

		// Extremo inferior con guarda suficiente en el superior
		if (distanciaSup >= BUTACAS_GUARDA && i0 == fila.getIndice(fila.getPrimera())) {
			return true;
		}

		int distanciaInf = 0;
		boolean ocupadaInf = false;
		i = i0;
		while (i-- > 0) {
			candidata.setNumero(String.valueOf(fila.getNumeroButaca(i)));
			if (isButacaOcupada(sesion, butacas, candidata)) {
				ocupadaInf = true;
				break;
			}
			distanciaInf++;
		}

		// Extremo superior con guarda suficiente en el inferior
		if (distanciaInf >= BUTACAS_GUARDA && i1 == fila.getIndice(fila.getUltima())) {
			return true;
		}

		// Guarda mímima en uno de los extremos
		if ((ocupadaInf && distanciaInf == BUTACAS_GUARDA && distanciaSup >= BUTACAS_GUARDA)
				|| (ocupadaSup && distanciaSup == BUTACAS_GUARDA && distanciaInf >= BUTACAS_GUARDA)) {
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
	 *   0. Las restricciones de distanciamiento social estén activas.
	 *   1. Se respete la restricción porcentual de aforo.
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
		// 0. Estado distanciamiento social
		if (!configuration.isAforoDistanciamientoSocial() || !configuration.isAforoDistanciamientoSocialUF()) {
			return true;
		}

		// 1. Restricción de aforo máximo
		final long ocupacion = configuration.getAforoDistanciamientoSocialUFLimite();
		if (configuration.isAforoDistanciamientoSocialUF() && ocupacion < 100L) {
			final long id_sesion = sesionId.longValue();
			if (sesionesDAO.getAforoTotal(id_sesion) * ocupacion < (sesionesDAO.getAforoOcupado(id_sesion) + butacas.size()) * 100L) {
				return false;
			}
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

		// Clonamos la selección de butacas y añadimos las butacas asociadas a las butacas accesibles
		final List<Butaca> butacasTodas = new ArrayList<Butaca>(butacas);
		for (final Butaca butaca : butacas) {
			if (butacasVinculadasService.esButacaAccesibleDisponible(sesion, butaca)) {
				butacasTodas.addAll(butacasVinculadasService.getButacasAsociadas(butaca));
			}
		}
		final SortedSetMultimap<FilaNumeracion, Butaca> b2f = butacas2Filas(butacasTodas);
		for (final FilaNumeracion fila : b2f.keySet()) {
			final SortedSet<Butaca> butacasFila = b2f.get(fila);

			// 3. Butacas contiguas
			if (!isButacasFilaCompacta(fila, butacasFila)) {
				return false;
			}

			// 4. Distancia de seguridad con la butaca ocupada más próxima
			if (!isButacasFilaGuarda(sesion, fila, butacasFila)) {
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
	@SuppressWarnings("unlikely-arg-type")
	public boolean cambiaFilaNumero(final ButacaDTO butaca, final String fila, final String numero) {
		if (butaca == null || fila == null || numero == null) {
			return false;
		}

		if (!(configuration.isAforoDistanciamientoSocial() && configuration.isAforoDistanciamientoSocialUF())) {
			return true;
		}

		final CompraDTO compra = butaca.getParCompra();
		final List<ButacaDTO> butacas = compra.getParButacas();
		final DatosButaca origen = new DatosButaca(butaca.getParLocalizacion().getCodigo(), Integer.parseInt(butaca.getFila()), Integer.parseInt(butaca.getNumero()));

		final ArrayList<Butaca> candidatas = new ArrayList<Butaca>();
		for (final ButacaDTO butacaDTO : butacas) {
			final Butaca nueva = new Butaca(butacaDTO, false, "");
			if (origen.equals(butacaDTO)) {
				nueva.setFila(fila);
				nueva.setNumero(numero);
			}
			candidatas.add(nueva);
		}
		return  validaButacas(Long.valueOf(compra.getParSesion().getId()), candidatas);
	}

	/**
	 * Calcula el número de butacas totales por localización con limitación
	 * porcentual del aforo
	 *
	 * @param localizacion
	 * @return máximo número de butacas disponible para su venta
	 */
	public int getTotalEntradasLimite(final LocalizacionDTO localizacion) {
		int total = localizacion.getTotalEntradas().intValue();

		if (configuration.isAforoDistanciamientoSocialUF()) {
			final int ocupacion = configuration.getAforoDistanciamientoSocialUFLimite();
			if (ocupacion < 100) {
				total = total * ocupacion / 100;
			}
		}

		return total;
	}
}
