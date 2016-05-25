package es.uji.apps.par.tpvmodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Request {
	/*
	 * "<Fecha>23/04/2014</Fecha>" +
	    			"<Hora>18:25</Hora>" +
	    			"<Ds_SecurePayment>1</Ds_SecurePayment>" +
	    			"<Ds_Card_Country>724</Ds_Card_Country>" +
	    			"<Ds_Amount>300</Ds_Amount>" +
	    			"<Ds_Currency>978</Ds_Currency>" +
	    			"<Ds_Order>000017002</Ds_Order>" +
	    			"<Ds_MerchantCode>055136832</Ds_MerchantCode>" +
	    			"<Ds_Terminal>001</Ds_Terminal>" +
	    			"<Ds_Response>0000</Ds_Response>" +
	    			"<Ds_MerchantData>17002</Ds_MerchantData>" +
	    			"<Ds_TransactionType>0</Ds_TransactionType>" +
	    			"<Ds_ConsumerLanguage>3</Ds_ConsumerLanguage>" +
	    			"<Ds_AuthorisationCode>399941</Ds_AuthorisationCode>" +
	 */
	private String Fecha;
	private String Hora;
	private String Ds_SecurePayment;
	private String Ds_Card_Country;
	private String Ds_Amount;
	private String Ds_Currency;
	private String Ds_Order;
	private String Ds_MerchantCode;
	private String Ds_Terminal;
	private String Ds_Response;
	private String Ds_MerchantData;
	private String Ds_TransactionType;
	private String Ds_ConsumerLanguage;
	private String Ds_AuthorisationCode;
	
	public Request() {
		
	}
	
	@XmlElement(name="Fecha")
	public String getFecha() {
		return Fecha;
	}
	
	public void setFecha(String fecha) {
		Fecha = fecha;
	}
	
	@XmlElement(name="Hora")
	public String getHora() {
		return Hora;
	}
	public void setHora(String hora) {
		Hora = hora;
	}
	
	@XmlElement(name="Ds_SecurePayment")
	public String getDs_SecurePayment() {
		return Ds_SecurePayment;
	}
	public void setDs_SecurePayment(String ds_SecurePayment) {
		Ds_SecurePayment = ds_SecurePayment;
	}
	
	@XmlElement(name="Ds_Card_Country")
	public String getDs_Card_Country() {
		return Ds_Card_Country;
	}
	public void setDs_Card_Country(String ds_Card_Country) {
		Ds_Card_Country = ds_Card_Country;
	}
	
	@XmlElement(name="Ds_Amount")
	public String getDs_Amount() {
		return Ds_Amount;
	}
	public void setDs_Amount(String ds_Amount) {
		Ds_Amount = ds_Amount;
	}
	
	@XmlElement(name="Ds_Currency")
	public String getDs_Currency() {
		return Ds_Currency;
	}
	public void setDs_Currency(String ds_Currency) {
		Ds_Currency = ds_Currency;
	}
	
	@XmlElement(name="Ds_Order")
	public String getDs_Order() {
		return Ds_Order;
	}
	public void setDs_Order(String ds_Order) {
		Ds_Order = ds_Order;
	}
	
	@XmlElement(name="Ds_MerchantCode")
	public String getDs_MerchantCode() {
		return Ds_MerchantCode;
	}
	public void setDs_MerchantCode(String ds_MerchantCode) {
		Ds_MerchantCode = ds_MerchantCode;
	}
	
	@XmlElement(name="Ds_Terminal")
	public String getDs_Terminal() {
		return Ds_Terminal;
	}
	public void setDs_Terminal(String ds_Terminal) {
		Ds_Terminal = ds_Terminal;
	}
	
	@XmlElement(name="Ds_Response")
	public String getDs_Response() {
		return Ds_Response;
	}
	public void setDs_Response(String ds_Response) {
		Ds_Response = ds_Response;
	}
	
	@XmlElement(name="Ds_MerchantData")
	public String getDs_MerchantData() {
		return Ds_MerchantData;
	}
	public void setDs_MerchantData(String ds_MerchantData) {
		Ds_MerchantData = ds_MerchantData;
	}
	
	@XmlElement(name="Ds_TransactionType")
	public String getDs_TransactionType() {
		return Ds_TransactionType;
	}
	public void setDs_TransactionType(String ds_TransactionType) {
		Ds_TransactionType = ds_TransactionType;
	}
	
	@XmlElement(name="Ds_ConsumerLanguage")
	public String getDs_ConsumerLanguage() {
		return Ds_ConsumerLanguage;
	}
	public void setDs_ConsumerLanguage(String ds_ConsumerLanguage) {
		Ds_ConsumerLanguage = ds_ConsumerLanguage;
	}
	
	@XmlElement(name="Ds_AuthorisationCode")
	public String getDs_AuthorisationCode() {
		return Ds_AuthorisationCode;
	}
	public void setDs_AuthorisationCode(String ds_AuthorisationCode) {
		Ds_AuthorisationCode = ds_AuthorisationCode;
	}
}
