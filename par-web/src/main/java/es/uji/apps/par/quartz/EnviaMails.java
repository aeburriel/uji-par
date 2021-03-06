package es.uji.apps.par.quartz;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;
import es.uji.apps.par.services.EntradasService;
import es.uji.apps.par.services.MailInterface;
import es.uji.apps.par.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

@Service
public class EnviaMails
{
	@Autowired
	MailDAO mailDAO;
	
	@Autowired
	EntradasService entradasService;

	@Autowired
	UsersService usersService;

	@Autowired
	Configuration configuration;

	private static final Logger log = LoggerFactory.getLogger(EnviaMails.class);
	
	public MailInterface newInstanceMailSender()
    {
		log.info("Inicializamos la clase de envio de mails: " + configuration.getMailingClass());
    	try {
			return (MailInterface) Class.forName(configuration.getMailingClass()).newInstance();
		} catch(Exception e) {
    		throw new RuntimeException("Imposible instanciar la clase de envio de mails: " + configuration.getMailingClass());
    	}
    }

	public void ejecuta() throws MessagingException
	{
        log.info("Inicializamos envío de mails");

		MailInterface mailInterface = newInstanceMailSender();
		mailInterface.enviaPendientes(mailDAO, entradasService, usersService, configuration);
    }
}
