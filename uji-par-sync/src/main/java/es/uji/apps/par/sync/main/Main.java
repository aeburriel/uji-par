package es.uji.apps.par.sync.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.sync.EventosSyncService;

@Service
public class Main
{
    private static final int SYNC_SLEEP = 10 * 60 * 1000;

    public static Logger log = Logger.getLogger(Main.class);

    @Autowired
    EventosSyncService syncService;

	@Autowired
	Configuration configuration;

    private void sync() throws MalformedURLException, JAXBException, IOException, ParseException
    {
        for (String urlRss:configuration.getSyncUrlsRss())
        {
            syncService.sync(new URL(urlRss).openStream());
        }
    }
    
    private static boolean tieneOpcion(String [] args, String opcion)
    {
        for (String arg:args)
        {
            if (arg.equals(opcion))
                return true;
        }
        
        return false;
    }

    public static void main(String[] args) throws MalformedURLException, JAXBException, IOException
    {
        final boolean modoCron = tieneOpcion(args, "--cron");
        
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext-db.xml");

        final Main main = ctx.getBean(Main.class);
        
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        main.sync();
                        
                        if (modoCron)
                            return;
                    }
                    catch (Exception e)
                    {
                        log.error("Error", e);
                    }

                    try
                    {
                        Thread.sleep(SYNC_SLEEP);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        };

        runnable.run();
    }
}
