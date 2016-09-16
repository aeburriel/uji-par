package es.uji.apps.par.services;

import com.mysema.query.Tuple;
import es.uji.apps.par.comparator.ButacaComparator;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.config.ConfigurationSelector;
import es.uji.apps.par.dao.ButacasDAO;
import es.uji.apps.par.dao.LocalizacionesDAO;
import es.uji.apps.par.db.ButacaDTO;
import es.uji.apps.par.db.CompraDTO;
import es.uji.apps.par.db.LocalizacionDTO;
import es.uji.apps.par.db.TarifaDTO;
import es.uji.apps.par.exceptions.ButacaOcupadaException;
import es.uji.apps.par.i18n.ResourceProperties;
import es.uji.apps.par.model.Butaca;
import es.uji.apps.par.model.DisponiblesLocalizacion;
import es.uji.apps.par.model.Localizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ButacasService
{
    @Autowired
    private ButacasDAO butacasDAO;
    
    @Autowired
    private LocalizacionesDAO localizacionesDAO;

	@Autowired
	Configuration configuration;

	@Autowired
	ConfigurationSelector configurationSelector;

    public ButacaDTO getButaca(long idButaca)
    {
        return butacasDAO.getButaca(idButaca);
    }

    public List<ButacaDTO> getButacas(long idSesion, String codigoLocalizacion)
    {
        return butacasDAO.getButacas(idSesion, codigoLocalizacion);
    }

    public boolean estaOcupada(long idSesion, String codigoLocalizacion, String fila, String numero)
    {
        return butacasDAO.estaOcupada(idSesion, codigoLocalizacion, fila, numero);
    }

    private Boolean esReserva(ButacaDTO butacaDTO)
    {
        return butacaDTO.getParCompra().getReserva();
    }

    public Map<String, String> estilosButacasOcupadas(Long sesionId, List<Localizacion> localizaciones, boolean mostrarReservadas)
    {
        Map<String, String> clasesOcupadas = new HashMap<>();
        for (Localizacion localizacion : localizaciones) {
            List<ButacaDTO> butacas = getButacas(sesionId, localizacion.getCodigo());
            for (ButacaDTO butaca : butacas) {
                if (butaca.getAnulada() == null || !butaca.getAnulada()) {
                    String key = String.format("%s_%s_%s", butaca.getParLocalizacion().getCodigo(),
                            butaca.getFila(), butaca.getNumero());

                    String value = (configurationSelector.showButacasHanEntradoEnDistintoColor() && butaca.getPresentada() !=
							null && mostrarReservadas) ? "mapaPresentada" : "mapaOcupada";
                    if (mostrarReservadas && esReserva(butaca))
                        value = "mapaReservada";

                    clasesOcupadas.put(key, value);
                }
            }
        }

        return clasesOcupadas;
    }

    public List<Butaca> estanOcupadas(long sesionId, List<Butaca> butacas, String uuidCompra)
    {
        List<Long> sesionIds = new ArrayList<Long>();
        sesionIds.add(sesionId);

        return estanOcupadas(sesionIds, butacas, uuidCompra);
    }

    public List<Butaca> estanOcupadas(List<Long> sesionIds, List<Butaca> butacas, String uuidCompra)
    {
        List<Butaca> ocupadas = new ArrayList<Butaca>();

        for (Butaca butaca : butacas)
        {
            for (Long sesionId : sesionIds)
            {
                CompraDTO compra = butacasDAO.getCompra(sesionId, butaca.getLocalizacion(), butaca.getFila(), butaca.getNumero());

                if (compra != null) {
                    if (compra.getPagada()) {
                        ocupadas.add(butaca);
                    } else {
                        if (!compra.getUuid().equals(uuidCompra))
                            ocupadas.add(butaca);
                    }
                }
            }
        }

        return ocupadas;
    }
    
    public List<DisponiblesLocalizacion> getDisponiblesNoNumerada(long idSesion)
    {
        List<DisponiblesLocalizacion> disponibles = new ArrayList<DisponiblesLocalizacion>();
        List<LocalizacionDTO> localizaciones = localizacionesDAO.getFromSesion(idSesion);
        
        for (LocalizacionDTO localizacion: localizaciones)
        {
            DisponiblesLocalizacion disponible = new DisponiblesLocalizacion(localizacion.getCodigo(), 
                    localizacion.getTotalEntradas().intValue() - butacasDAO.getOcupadas(idSesion, localizacion.getCodigo()));
            
            disponibles.add(disponible);
        }
            
        return disponibles;
    }

	public List<Butaca> getButacasCompra(Long idCompra, String sort, int start, int limit, String language) {
		List<Tuple> listaButacasDTO = butacasDAO.getButacasCompra(idCompra, sort, start, limit);
		List<Butaca> listaButacas = new ArrayList<Butaca>();
		for (Tuple objButacaTarifa: listaButacasDTO) {
			ButacaDTO butacaDTO = objButacaTarifa.get(0, ButacaDTO.class);
			TarifaDTO tarifaDTO = objButacaTarifa.get(1, TarifaDTO.class);
			Butaca butaca = new Butaca(butacaDTO, configuration.isIdEntrada(), language);
			butaca.setTipo(tarifaDTO.getNombre());
			listaButacas.add(butaca);
		}
		
		return listaButacas;
	}

	public int getTotalButacasCompra(Long idCompra) {
		return butacasDAO.getTotalButacasCompra(idCompra);
	}

    public List<Butaca> getButacasNoAnuladas(Long idSesion, String language)
    {
         return Butaca.butacasDTOToButacas(butacasDAO.getButacasNoAnuladas(idSesion), configuration.isIdEntrada(), language);
    }

    public void updatePresentadas(List<Butaca> butacas)
    {
        butacasDAO.updatePresentadas(butacas);
    }

    public long updatePresentada(Butaca butaca)
    {
        return butacasDAO.updatePresentada(butaca);
    }

    public List<Butaca> getButacasDisponibles(Long butacaId, String tarifaId, Locale locale) throws IOException {
        ButacaDTO butacaDTO = getButaca(butacaId);
        List<ButacaDTO> butacasOcupadas = getButacasNoAnuladasPorLocalizacion(butacaDTO.getParSesion().getId(), tarifaId);

        HashMap<String, List<String>> posicionesOcupadas = new HashMap<>();
        for (ButacaDTO butacaOcupada:butacasOcupadas) {
            if (!posicionesOcupadas.containsKey(butacaOcupada.getFila()))
            {
                posicionesOcupadas.put(butacaOcupada.getFila(), new ArrayList<String>());
            }
            posicionesOcupadas.get(butacaOcupada.getFila()).add(butacaOcupada.getNumero());
        }

        List<Butaca> butacasDisponibles = new ArrayList<>();


        byte[] encoded = Files.readAllBytes(Paths.get(configuration.getPathJson() + tarifaId + ".json"));
        List<Butaca> butacasTotales = Butaca.parseaJSON(new String(encoded, "UTF-8"));

        int i = 1;
        for (Butaca butaca : butacasTotales) {
            if (posicionesOcupadas.get(butaca.getFila()) == null || !posicionesOcupadas.get(butaca.getFila()).contains(butaca.getNumero())) {
                butaca.setId(i);
                butaca.setTexto(String.format(ResourceProperties.getProperty(locale, "entrada.butaca"), butaca.getFila(), butaca.getNumero()));
                butaca.setUuid(butaca.getFila() + "#" + butaca.getNumero());

                butacasDisponibles.add(butaca);
                i++;
            }
        }

        Collections.sort(butacasDisponibles, new ButacaComparator());

        return butacasDisponibles;
    }

    public List<ButacaDTO> getButacasNoAnuladasPorLocalizacion(long id, String tarifaId) {
        return butacasDAO.getButacasOcupadasNoAnuladasPorLocalizacion(id, tarifaId);
    }

    public void cambiaFilaNumero(Long butacaId, String fila, String numero) {
        ButacaDTO butaca = getButaca(butacaId);
        List<ButacaDTO> butacasOcupadasPorLocalizacion = getButacasNoAnuladasPorLocalizacion(butaca.getParSesion().getId(), butaca.getParLocalizacion().getCodigo());

        for (ButacaDTO butacaDTO:butacasOcupadasPorLocalizacion) {
            if (butacaDTO.getFila().equals(fila) && butacaDTO.getNumero().equals(numero))
            {
                throw new ButacaOcupadaException(butaca.getParSesion().getId(), butaca.getParLocalizacion().getCodigo(), fila, numero);
            }
        }

        butacasDAO.cambiaFilaNumero(butacaId, fila, numero);
    }
}
