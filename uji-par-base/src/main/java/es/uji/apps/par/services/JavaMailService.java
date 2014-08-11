package es.uji.apps.par.services;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;
import es.uji.apps.par.db.MailDTO;
import es.uji.commons.messaging.client.MessageNotSentException;

@Service
public class JavaMailService implements MailInterface
{
    public static Logger log = Logger.getLogger(MailService.class);
    
    @Autowired
	MailDAO mailDao;

    public JavaMailService()
    {
        
    }

    public void anyadeEnvio(String to, String titulo, String texto)
    {
        mailDao.insertaMail(Configuration.getMailFrom(), to, titulo, texto);
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
    
    private Message createMailMessage(String de, String para, String titulo) throws MessagingException
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", Configuration.getMailHost());

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

    private void enviaMail(String de, String para, String titulo, String texto) throws MessageNotSentException
    {
        try {
            Message message = createMailMessage(de, para, titulo);
            MimeMultipart multipart = createMailBodyMessage(texto);
            message.setContent(multipart);
            Transport.send(message);
        }catch (MessagingException me) {
        	log.error("Error enviando mail ", me);
            throw new MessageNotSentException();
     	}
    }
            
            
    private void enviaMailMultipart(String de, String para, String titulo, String texto, String uuid, 
    		EntradasService entradasService) throws MessageNotSentException {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		entradasService.generaEntrada(uuid, baos);
    		Message message = createMailMessage(de, para, titulo);
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(texto);
            multipart.addBodyPart(messageBodyPart);

            byte[] pdf = baos.toByteArray();
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setFileName("entrada.pdf");
            attachment.setContent(pdf, "application/pdf");
            multipart.addBodyPart(attachment);

            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception me) {
        	log.error("Error enviando mail multipart", me);
        	try {
        		enviaMail(de, para, titulo, texto);
        	} catch (Exception e) {
        		log.error("Error enviando mail", me);
        		throw new MessageNotSentException();
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
    public synchronized void enviaPendientes(MailDAO mailDAO, EntradasService entradasService) throws MessageNotSentException
    {
        log.info("** - Enviando mails pendientes desde JavaMailService...");

        List<MailDTO> mails = mailDAO.getMailsPendientes();

        for (MailDTO mail : mails)
        {
        	enviaMailMultipart(mail.getDe(), mail.getPara(), mail.getTitulo(), mail.getTexto(), mail.getUuid(), entradasService);
            mailDAO.marcaEnviado(mail.getId());
        }
    }
    
    public static void main(String[] args) throws MessageNotSentException
    {
    	JavaMailService mail = new JavaMailService();

        mail.enviaMailMultipart("no_reply@uji.es", "nicolas.manero@4tic.com", "Esto es el cuerpo", "Hola que tal!", "uuid", null);
    }
}