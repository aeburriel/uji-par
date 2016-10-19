package com.tgerm.log4j.appender;

import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.helpers.CyclicBuffer;
import com.sun.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

public class GmailSMTPAppender extends SMTPAppender
{
	private static final Logger log = LoggerFactory.getLogger(GmailSMTPAppender.class);
    protected Session session;
	private boolean enviarMailsError;

    public GmailSMTPAppender()
    {
        super();
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("/etc/uji/par/app.properties"));
			try {
				String propEnviarMails = properties.getProperty("uji.par.enviarMailsError");
				enviarMailsError = (propEnviarMails == null || propEnviarMails.trim().equals("")) ? false : (propEnviarMails.trim
						().toUpperCase().equals("TRUE"));
			} catch (Exception e) {
				enviarMailsError = true;
			}
		} catch (Exception e) {
			enviarMailsError = true;
		}
    }

    /**
     * Create mail session.
     * 
     * @return mail session, may not be null.
     */
    protected Session createSession()
    {
        Properties props = new Properties();
        props.put("mail.smtps.host", getSMTPHost());
        props.put("mail.smtps.auth", "true");

        Authenticator auth = null;
        if (getPassword() != null && getUsername() != null)
        {
            auth = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(getUsername(), getPassword());
                }
            };
        }
        session = Session.getInstance(props, auth);
        return session;
    }

    /**
     * Send the contents of the cyclic buffer as an e-mail message.
     */
    protected void sendBuffer()
    {
        if (!enviarMailsError)
        {
            return;
        }
        
        try
        {
            MimeBodyPart part = new MimeBodyPart();

            StringBuffer sbuf = new StringBuffer();
            String t = layout.getFileHeader();
            if (t != null)
                sbuf.append(t);
            //int len = cb.length();
            //for (int i = 0; i < len; i++)
            for (CyclicBuffer<ILoggingEvent> mm: getCyclicBufferTracker().allComponents())
            {
                ILoggingEvent event = mm.get();
                sbuf.append(layout.doLayout(event));
            }
            t = layout.getFileFooter();
            if (t != null)
                sbuf.append(t);
            part.setContent(sbuf.toString(), layout.getContentType());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            getMessage().setContent(mp);

            getMessage().setSentDate(new Date());
            send(getMessage());
        }
        catch (Exception e)
        {
        	log.error("Error occured while sending e-mail notification.", e);
        }
    }

    /**
     * Pulled email send stuff i.e. Transport.send()/Transport.sendMessage(). So
     * that on required this logic can be enhanced.
     * 
     * @param msg
     *            Email Message
     * @throws MessagingException
     */
    protected void send(Message msg) throws MessagingException
    {
        SMTPTransport t = (SMTPTransport) session.getTransport("smtps");
        try
        {
            t.connect(getSMTPHost(), getUsername(), getPassword());
            t.sendMessage(msg, msg.getAllRecipients());
        }
        finally
        {
            System.out.println("Response: " + t.getLastServerResponse());
            t.close();
        }
    }
}