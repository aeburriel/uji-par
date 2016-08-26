package es.uji.apps.par.services;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;
import es.uji.apps.par.db.MailDTO;
import es.uji.commons.messaging.client.MessageNotSentException;
import es.uji.commons.messaging.client.MessagingClient;
import es.uji.commons.messaging.client.model.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Service
public class MailService implements MailInterface
{
	private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    MailDAO mailDao;

	@Autowired
	Configuration configuration;

    private MessagingClient client;

    public MailService()
    {
    	client = new MessagingClient();
    }

    public void anyadeEnvio(String from, String to, String titulo, String texto, String uuid, String urlPublic, String urlPieEntrada)
    {
        mailDao.insertaMail(from, to, titulo, texto, uuid, urlPublic, urlPieEntrada);
    }

    private void enviaMail(String de, String para, String titulo, String texto) throws MessageNotSentException
    {
        MailMessage mensaje = new MailMessage("PAR");
        mensaje.setTitle(titulo);
        mensaje.setContentType(MediaType.TEXT_PLAIN);
        mensaje.setSender(de);
        mensaje.setContent(texto);

        mensaje.addToRecipient(para);

        client.send(mensaje);
    }

    //al llamarse desde el job de quartz, no se inyecta el mailDAO, y lo enviamos desde la interfaz
    public synchronized void enviaPendientes(MailDAO mailDAO, EntradasService entradasService, UsersService usersService, Configuration configuration) throws MessagingException
    {
        log.info("Enviando mails pendientes...");

        List<MailDTO> mails = mailDAO.getMailsPendientes();

        for (MailDTO mail : mails)
        {
			try {
            	enviaMail(mail.getDe(), mail.getPara(), mail.getTitulo(), mail.getTexto());
			} catch (MessageNotSentException e) {
				throw new MessagingException();
			}
            mailDAO.marcaEnviado(mail.getId());
        }
    }

    @SuppressWarnings("resource")
	public static void main(String[] args) throws MessagingException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext-db.xml");

        MailService mail = ctx.getBean(MailService.class);

		try {
        	mail.enviaMail("no_reply@uji.es", "soporte@4tic.com", "Esto es el cuerpo", "Hola que tal!");
		} catch (MessageNotSentException e) {
			throw new MessagingException();
		}
    }
}
