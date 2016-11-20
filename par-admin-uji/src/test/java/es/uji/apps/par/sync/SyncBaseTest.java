package es.uji.apps.par.sync;

import es.uji.apps.par.config.Configuration;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

@Ignore
public class SyncBaseTest
{
	@Autowired
	Configuration configuration;

    public SyncBaseTest()
    {
        configuration.desactivaLogGmail();
    }
    
    protected InputStream loadFromClasspath(String filePath)
    {
        return SyncBaseTest.class.getClassLoader().getResourceAsStream(filePath);

    }
}