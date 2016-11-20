package es.uji.apps.par.sync;

import org.junit.Ignore;

import java.io.InputStream;

@Ignore
public class SyncBaseTest
{
	protected InputStream loadFromClasspath(String filePath)
    {
        return SyncBaseTest.class.getClassLoader().getResourceAsStream(filePath);
    }
}