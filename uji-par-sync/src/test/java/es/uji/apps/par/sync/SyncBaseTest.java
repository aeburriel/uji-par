package es.uji.apps.par.sync;

import java.io.InputStream;

import org.junit.Ignore;

import es.uji.apps.par.config.Configuration;

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