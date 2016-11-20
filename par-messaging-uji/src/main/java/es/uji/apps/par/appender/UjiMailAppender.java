package es.uji.apps.par.appender;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.helpers.CyclicBuffer;

import com.sun.mail.smtp.SMTPTransport;

import es.uji.commons.messaging.client.MessagingClient;
import es.uji.commons.messaging.client.model.MailMessage;

import java.io.FileInputStream;
import java.util.Properties;

public class UjiMailAppender extends SMTPAppender
{
	private static final Logger log = LoggerFactory.getLogger(UjiMailAppender.class);
    protected Session session;

    private MessagingClient client;
	private boolean enviarMailsError;
	private String entorno;

    public UjiMailAppender()
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
			entorno = properties.getProperty("uji.par.entorno");
			entorno = (entorno == null || entorno.trim().equals("")) ? "NO DEFINIDO" : entorno;
		} catch (Exception e) {
			enviarMailsError = true;
			entorno = "NO DEFINIDO";
		}
        client = new MessagingClient();
    }

    protected void sendBuffer()
    {
        if (!enviarMailsError)
        {
            return;
        }

        try
        {
            String body = getMailContent();

            MailMessage mensaje = new MailMessage("PAR");
            mensaje.setTitle(getSubject() + " (" + entorno + ")");
            mensaje.setContentType(MediaType.TEXT_PLAIN);
            mensaje.setSender(getFrom());
            String mails = "";
            for (String to: getToAsListOfString()) {
            	mails += (mails.equals(""))?to:","+to;
            }
            mensaje.addToRecipient(mails);
            mensaje.setContent(body);

            client.send(mensaje);
        }
        catch (Exception e)
        {
            log.error("Error occured while sending e-mail notification.", e);
        }
    }

    private String getMailContent()
    {
        StringBuffer sbuf = new StringBuffer();
        String t = layout.getFileHeader();

        if (t != null)
            sbuf.append(t);

        //int len = getCyclicBufferTracker().getBufferSize();

        for (CyclicBuffer<ILoggingEvent> mm: getCyclicBufferTracker().allComponents())
        //for (int i = 0; i < len; i++)
        {
            ILoggingEvent event = mm.get();
            sbuf.append(layout.doLayout(event));
            /*if (layout.ignoresThrowable())
            {
                StackTraceElementProxy[] s = event.getThrowableProxy().getStackTraceElementProxyArray();
                if (s != null)
                {
                    for (int j = 0; j < s.length; j++)
                    {
                        sbuf.append(s[j]);
                        sbuf.append(CoreConstants.LINE_SEPARATOR);
                    }
                }
            }*/
        }
        t = layout.getFileFooter();

        if (t != null)
            sbuf.append(t);

        return sbuf.toString();
    }

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