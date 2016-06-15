package es.uji.apps.par.services;

import es.uji.apps.par.dao.MailDAO;

import javax.mail.MessagingException;

public interface MailInterface {
	public void enviaPendientes(MailDAO mailDAO, EntradasService entradasService, String userUID) throws MessagingException;
	public void anyadeEnvio(String to, String titulo, String texto, String uuid);
}
