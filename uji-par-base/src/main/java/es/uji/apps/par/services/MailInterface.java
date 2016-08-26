package es.uji.apps.par.services;

import es.uji.apps.par.config.Configuration;
import es.uji.apps.par.dao.MailDAO;

import javax.mail.MessagingException;

public interface MailInterface {
	public void enviaPendientes(MailDAO mailDAO, EntradasService entradasService, Configuration configuration) throws MessagingException;
	public void anyadeEnvio(String from, String to, String titulo, String texto, String uuid, String urlPublic, String urlPieEntrada);
}
