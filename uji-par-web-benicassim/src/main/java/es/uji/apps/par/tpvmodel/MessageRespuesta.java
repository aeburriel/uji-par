package es.uji.apps.par.tpvmodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Message")
public class MessageRespuesta {
	private	Response Response;
	private String Signature;
	
	public MessageRespuesta() {
		
	}

	@XmlElement(name="Response")
	public Response getResponse() {
		return Response;
	}

	public void setResponse(Response response) {
		Response = response;
	}

	@XmlElement(name="Signature")
	public String getSignature() {
		return Signature;
	}

	public void setSignature(String signature) {
		Signature = signature;
	}
}
