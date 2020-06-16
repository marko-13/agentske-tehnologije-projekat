package dto;

import java.util.HashMap;

import model.AID;
import model.Performative;

public class ACLMessageDTO {

	private String performative;
	private String sender;
	private String[] receivers;
	
	public ACLMessageDTO(String performative, String sender, String[] receivers) {
		super();
		this.performative = performative;
		this.sender = sender;
		this.receivers = receivers;
	}

	public ACLMessageDTO() {
		super();
	}

	public String getPerformative() {
		return performative;
	}

	public void setPerformative(String performative) {
		this.performative = performative;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String[] getReceivers() {
		return receivers;
	}

	public void setReceivers(String[] receivers) {
		this.receivers = receivers;
	}
	
	
}
