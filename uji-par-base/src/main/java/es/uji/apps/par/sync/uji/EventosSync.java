package es.uji.apps.par.sync.uji;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface EventosSync
{
    public void sync(InputStream rssInputStream, String userUID) throws JAXBException, IOException, ParseException, InstantiationException, IllegalAccessException;
}