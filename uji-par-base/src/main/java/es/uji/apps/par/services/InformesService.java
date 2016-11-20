package es.uji.apps.par.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.model.TipoInforme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
public class InformesService
{
	@Autowired
	Configuration configuration;

    public List<TipoInforme> getTiposInforme(String language)
    {
        String tiposInforme = configuration.getTiposInforme();
        return parseInformes(tiposInforme, language);
    }

	public List<TipoInforme> getTiposInformeGenerales(String language) {
		String tiposInforme = configuration.getTiposInformeGenerales();
		return parseInformes(tiposInforme, language);
	}

	private List<TipoInforme> parseInformes(String tiposInforme, String language) {
		Type listType = new TypeToken<ArrayList<TipoInforme>>() {}.getType();
		List<TipoInforme> tiposInformeDisponibles = new Gson().fromJson(tiposInforme, listType);

		for (TipoInforme tipoInformeDisponible:tiposInformeDisponibles) {
			if (language != null) {
				if (language.equals("es"))
					tipoInformeDisponible.setNombre(tipoInformeDisponible.getNombreES());
				else
					tipoInformeDisponible.setNombre(tipoInformeDisponible.getNombreCA());
			}
		}
		return tiposInformeDisponibles;
	}
}
