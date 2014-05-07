package es.uji.apps.par.services;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;
import es.uji.apps.par.db.MailDTO;
import es.uji.commons.messaging.client.MessageNotSentException;
import es.uji.commons.messaging.client.MessagingClient;
import es.uji.commons.messaging.client.model.MailMessage;

@Service
public class MailService implements MailInterface
{
    public static Logger log = Logger.getLogger(MailService.class);

    @Autowired
    MailDAO mailDao;

    private MessagingClient client;

    public MailService()
    {
        
    }

    public void anyadeEnvio(String to, String titulo, String texto)
    {
        mailDao.insertaMail(Configuration.getMailFrom(), to, titulo, texto);
    }

    private void enviaMail(String de, String para, String titulo, String texto) throws MessageNotSentException
    {
        MailMessage mensaje = new MailMessage("PAR");
        mensaje.setTitle(titulo);
        mensaje.setContentType(MediaType.TEXT_PLAIN);
        mensaje.setSender(de);
        mensaje.setContent(texto);

        mensaje.addToRecipient(para);

        //client.send(mensaje);
    }

    //al llamarse desde el job de quartz, no se inyecta el mailDAO, y lo enviamos desde la interfaz
    public synchronized void enviaPendientes(MailDAO mailDAO) throws MessageNotSentException
    {
        log.info("Enviando mails pendientes...");

        List<MailDTO> mails = mailDAO.getMailsPendientes();

        for (MailDTO mail : mails)
        {
            enviaMail(mail.getDe(), mail.getPara(), mail.getTitulo(), mail.getTexto());
            mailDAO.marcaEnviado(mail.getId());
        }
    }

    public static void main(String[] args) throws MessageNotSentException
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext-db.xml");

        MailService mail = ctx.getBean(MailService.class);

        mail.enviaMail("no_reply@uji.es", "soporte@4tic.com", "Esto es el cuerpo", "Hola que tal!");
    }
}
