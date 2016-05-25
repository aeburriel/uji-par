package es.uji.apps.par.tpvmodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Response")
public class Response {
	private String Ds_Response_Merchant;
	
	public Response() {

	}

	@XmlElement(name="Ds_Response_Merchant")
	public String getDs_Response_Merchant() {
		return Ds_Response_Merchant;
	}

	public void setDs_Response_Merchant(String ds_Response_Merchant) {
		Ds_Response_Merchant = ds_Response_Merchant;
	}
	
	public void setDs_Response_Merchant(boolean estado) {
		if (estado)
			Ds_Response_Merchant = "OK";
		else
			Ds_Response_Merchant = "KO";
	}
}
