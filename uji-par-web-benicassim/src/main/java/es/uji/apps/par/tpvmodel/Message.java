package es.uji.apps.par.tpvmodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Message")
public class Message {
	Request Request;
	private String Signature;
	
	public Message() {
		
	}

	@XmlElement(name="Request")
	public Request getRequest() {
		return Request;
	}
	
	public void setRequest(Request request) {
		this.Request = request;
	}

	@XmlElement(name="Signature")
	public String getSignature() {
		return Signature;
	}
	
	public void setSignature(String signature) {
		this.Signature = signature;
	}
}
