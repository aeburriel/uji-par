package es.uji.apps.par.services;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.mysema.query.Tuple;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.EventosDAO;
import es.uji.apps.par.db.EventoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.exceptions.CampoRequeridoException;
import es.uji.apps.par.exceptions.GeneralPARException;
import es.uji.apps.par.exceptions.IncidenciaNotFoundException;
import es.uji.apps.par.exceptions.RegistroSerializaException;
import es.uji.apps.par.exceptions.SesionSinFormatoIdiomaIcaaException;
import es.uji.apps.par.dao.CinesDAO;
import es.uji.apps.par.dao.ComunicacionesICAADAO;
import es.uji.apps.par.dao.SesionesDAO;
import es.uji.apps.par.db.CineDTO;
import es.uji.apps.par.db.SesionDTO;
import es.uji.apps.par.ficheros.registros.FicheroRegistros;
import es.uji.apps.par.ficheros.service.FicherosService;
import es.uji.apps.par.model.Cine;
import es.uji.apps.par.model.Evento;
import es.uji.apps.par.model.Sala;
import es.uji.apps.par.model.Sesion;
import es.uji.apps.par.utils.DateUtils;
import es.uji.apps.par.utils.Utils;

@Service
public class ComunicacionesICAAService {
	
	@Autowired
    FicherosService ficherosService;
	
	@Autowired
	ComunicacionesICAADAO comunicacionesICAADAO;
	
	@Autowired
	SesionesDAO sesionesDAO;

	@Autowired
	EventosDAO eventosDAO;
	
	@Autowired
	CinesDAO cinesDAO;

	@Autowired
	Configuration configuration;

	public byte[] generaFicheroICAA(List<Integer> ids, String fechaEnvioHabitualAnterior, String tipoEnvio) 
			throws RegistroSerializaException, 
			IncidenciaNotFoundException, NoSuchProviderException, IOException, InterruptedException, GeneralPARException {
		Sesion.checkTipoEnvio(tipoEnvio);
		checkIds(ids);
		
		Date fechaEnvioAnterior = getDateEnvio(fechaEnvioHabitualAnterior);
		List<Sesion> sesiones = getSesionesFromIDs(ids);
		FicheroRegistros ficheroRegistros = ficherosService.generaFicheroRegistros(fechaEnvioAnterior, tipoEnvio, sesiones);
		byte[] contenido = ficheroRegistros.toByteArray();
		byte[] contenidoCifrado = ficherosService.encryptData(contenido);
		
		comunicacionesICAADAO.addNewEnvio(ids, ficheroRegistros.getRegistroBuzon().getFechaEnvio(), tipoEnvio);
		
		if (configuration.getGenerarCifrado())
			return contenidoCifrado;
		else
			return contenido;
	}

	private List<Sesion> getSesionesFromIDs(List<Integer> ids) {
		List<Sesion> sesiones = new ArrayList<Sesion>();
		for (Integer id: ids) {
			sesiones.add(new Sesion(id));
		}
		return sesiones;
	}

	private Date getDateEnvio(String fechaEnvioHabitualAnterior) {
		return DateUtils.spanishStringToDate(fechaEnvioHabitualAnterior);
	}

	private void checkIds(List<Integer> ids) throws GeneralPARException {
		if (ids == null || ids.size() == 0)
			throw new CampoRequeridoException("identificadores de sesión");
		if (comunicacionesICAADAO.checkIfFicherosGenerados(Utils.listIntegerToListLong(ids)) > 0)
			throw new GeneralPARException(GeneralPARException.SESIONES_CON_FICHEROS_YA_GENERADOS_CODE);
	}

	public void marcaEnviosComoEnviados(List<Long> ids) {
		Calendar cal = Calendar.getInstance();
		Date fechaEnvio = cal.getTime();
		comunicacionesICAADAO.marcaEnviosComoEnviados(ids, fechaEnvio);
	}
	
	private void checkDatosCine() throws RegistroSerializaException {
		List<CineDTO> cines = cinesDAO.getCines();
        CineDTO cine = cines.get(0);
        Cine.checkValidity(cine.getCodigo());
	}

	public void checkEventosBeforeGenerateICAAFile(List<Integer> ids, String tipoEnvio) throws GeneralPARException {
		checkDatosCine();
		Sesion.checkTipoEnvio(tipoEnvio);
		
		List<SesionDTO> sesiones = sesionesDAO.getSesiones(Utils.listIntegerToListLong(ids));
		for (SesionDTO sesion: sesiones) {
			Sala.checkValidity(sesion.getParSala().getNombre(), sesion.getParSala().getCodigo());
			List<Tuple> peliculasMultisesion = eventosDAO.getPeliculasMultisesion(sesion.getParEvento().getId());

			if (peliculasMultisesion.size() > 0) {
				for (Tuple peliculaMultisesion: peliculasMultisesion) {
                    EventoDTO eventoDTO = peliculaMultisesion.get(0, EventoDTO.class);
                    String versionLinguistica = peliculaMultisesion.get(1, String.class);

					Evento.checkValidity(new Long(eventoDTO.getId()).intValue(), eventoDTO.getExpediente(),
                            eventoDTO.getTituloEs(), eventoDTO.getCodigoDistribuidora(),
                            eventoDTO.getNombreDistribuidora(), eventoDTO.getVo(),
                            versionLinguistica, eventoDTO.getSubtitulos(), eventoDTO.getFormato());
				}
			} else {
				Evento.checkValidity(new Long(sesion.getParEvento().getId()).intValue(), sesion.getParEvento().getExpediente(),
						sesion.getParEvento().getTituloEs(), sesion.getParEvento().getCodigoDistribuidora(),
						sesion.getParEvento().getNombreDistribuidora(), sesion.getParEvento().getVo(),
						sesion.getVersionLinguistica(), sesion.getParEvento().getSubtitulos(),
						sesion.getParEvento().getFormato());
			}
			Sesion.checkSesion(sesion.getFechaCelebracion(), tipoEnvio, sesion.getIncidenciaId());
		}
	}

}
