package es.uji.apps.par.services;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.sync.uji.EventosSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

@Service
public class EventosSyncService
{
    @Autowired
    @Qualifier("syncUji")
    EventosSync syncUji;

    @Autowired
    @Qualifier("syncBenicassim")
    EventosSync syncBenicassim;

	@Autowired
	Configuration configuration;

    private String tipo;

	@Autowired
    public EventosSyncService(Configuration configuration)
    {
        tipo = configuration.getSyncTipo();
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    @Transactional
    public void sync(InputStream rssInputStream) throws JAXBException, IOException, ParseException, IllegalAccessException, InstantiationException {
        if (tipo.equals("uji"))
            syncUji.sync(rssInputStream);
        else if (tipo.equals("benicassim"))
            syncBenicassim.sync(rssInputStream);
        else
            throw new RuntimeException(
                    "No se ha encontrado sincronizador para el tipo definido en la config uji.sync.lugar="
                            + configuration.getSyncTipo());
    }
}
