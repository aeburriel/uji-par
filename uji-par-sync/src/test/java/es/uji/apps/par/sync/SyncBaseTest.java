package es.uji.apps.par.sync;

import java.io.InputStream;

import org.junit.Ignore;

@Ignore
public class SyncBaseTest
{
    protected InputStream loadFromClasspath(String filePath)
    {
        return SyncBaseTest.class.getClassLoader().getResourceAsStream(filePath);
    }
}