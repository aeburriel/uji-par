package es.uji.apps.par.sync;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.sync.uji.EventosSync;

@Service
public class EventosSyncService
{
    @Autowired
    @Qualifier("syncUji")
    EventosSync syncUji;

	@Autowired
	Configuration configuration;

    private String tipo;

    public EventosSyncService()
    {
        tipo = configuration.getSyncTipo();
    }

    public void setTipo(String tipo)
    {
        this.tipo = tipo;
    }

    @Transactional
    public void sync(InputStream rssInputStream) throws JAXBException, MalformedURLException, IOException, ParseException
    {
        if (tipo.equals("uji"))
            syncUji.sync(rssInputStream);
        else
            throw new RuntimeException(
                    "No se ha encontrado sincronizador para el tipo definido en la config uji.sync.lugar="
                            + configuration.getSyncTipo());
    }
}
