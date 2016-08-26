package es.uji.apps.par.services;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;
import es.uji.apps.par.db.MailDTO;
import es.uji.apps.par.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class JavaMailService implements MailInterface
{
	private static final Logger log = LoggerFactory.getLogger(JavaMailService.class);
    
    @Autowired
	MailDAO mailDao;

	@Autowired
	Configuration configuration;

    public JavaMailService()
    {
        
    }

    public void anyadeEnvio(String to, String titulo, String texto, String uuid, String urlPublic)
    {
        mailDao.insertaMail(configuration.getMailFrom(), to, titulo, texto, uuid, urlPublic);
    }
    
    private Address[] getMailAddressList(String path) throws AddressException
    {
        if (path == null || path.equals(""))
        {
            return null;
        }

        String[] addrList = path.split(",");
        Address[] result = new Address[addrList.length];

        for (int i = 0; i < addrList.length; i++)
        {
            result[i] = new InternetAddress(addrList[i].trim());
        }

        return result;
    }
    
    private Message createMailMessage(String de, String para, String titulo, Configuration configuration) throws MessagingException
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", configuration.getMailHost());

        Session session = Session.getInstance(props, null);
        Message message = new MimeMessage(session);

        message.addHeader("Auto-Submitted", "auto-generated");
        
        message.setSubject(titulo);
        message.setSentDate(new Date());

        message.setFrom(new InternetAddress(de));
        message.setReplyTo(getMailAddressList(para));
        message.setRecipients(javax.mail.Message.RecipientType.TO, getMailAddressList(para));
        message.setRecipients(javax.mail.Message.RecipientType.BCC, getMailAddressList("nicolas.manero@4tic.com"));

        return message;
    }

    private void enviaMail(String de, String para, String titulo, String texto, Configuration configuration) throws MessagingException
    {
        try {
            Message message = createMailMessage(de, para, titulo, configuration);
            MimeMultipart multipart = createMailBodyMessage(texto);
            message.setContent(multipart);
            Transport.send(message);
        }catch (MessagingException me) {
        	log.error("Error enviando mail ", me);
            throw new MessagingException();
     	}
    }
            
            
    private void enviaMailMultipart(String de, String para, String titulo, String texto, String uuid, String urlPublicSinHTTPS,
    		EntradasService entradasService, Configuration configuration) throws MessagingException {
    	try {
			if (uuid == null) {
				log.error("UUID nulo en el método enviaMailMultipart");
				throw new NullPointerException();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			entradasService.generaEntrada(uuid, baos, configuration.getAdminLogin().get(0), urlPublicSinHTTPS);
			Message message = createMailMessage(de, para, titulo, configuration);
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(texto);
			multipart.addBodyPart(messageBodyPart);

			if (baos != null && baos.size() > 0) {
				byte[] pdf = baos.toByteArray();
				MimeBodyPart attachment = new MimeBodyPart();
				attachment.setFileName("entrada.pdf");
				attachment.setContent(pdf, "application/pdf");
				multipart.addBodyPart(attachment);
			}

			message.setContent(multipart);
			Transport.send(message);
        } catch (NullPointerException e) {
            log.error("Error enviando mail multipart (elemento nulo)", e);
			throw new MessagingException();
		} catch (Exception me) {
        	if (!para.contains("4tic"))
        		log.error("Error enviando mail multipart", me);
        	
        	try {
        		enviaMail(de, para, titulo, texto, configuration);
        	} catch (Exception e) {
        		log.error("Error enviando mail", me);
        		throw new MessagingException();
        	}
     	}
    }
    
    private MimeMultipart createMailBodyMessage(String texto) throws MessagingException
    {
        MimeMultipart multipart = new MimeMultipart();

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText( texto );
        multipart.addBodyPart(messageBodyPart);

        return multipart;
    }

    //al llamarse desde el job de quartz, no se inyecta el mailDAO, ni el service y lo enviamos desde la interfaz
    public synchronized void enviaPendientes(MailDAO mailDAO, EntradasService entradasService, Configuration configuration) throws MessagingException {
    	if (configuration.getEnviarMailsEntradas() == null || configuration.getEnviarMailsEntradas().equals("true")) {
	        log.info("** - Enviando mails pendientes desde JavaMailService...");
	
	        List<MailDTO> mails = mailDAO.getMailsPendientes();
	
	        for (MailDTO mail : mails)
	        {
	        	enviaMailMultipart(mail.getDe(), mail.getPara(), mail.getTitulo(), mail.getTexto(), mail.getUuid(), Utils.sinHTTPS(mail.getUrlPublic()), entradasService, configuration);
	            mailDAO.marcaEnviado(mail.getId());
	        }
    	}
    }
    
    public static void main(String[] args) throws MessagingException
    {
    	JavaMailService mail = new JavaMailService();

        mail.enviaMailMultipart("no_reply@uji.es", "nicolas.manero@4tic.com", "Esto es el cuerpo", "Hola que tal!", "uuid", "url", null, mail.configuration);
    }
}
