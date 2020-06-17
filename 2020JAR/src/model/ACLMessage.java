package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class ACLMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Performative performative;
	private AID sender;
	private AID[] receivers;
	private AID replyTo;
	private String content;
	private Object contentObj;
	private HashMap<String, Object> userArgs;
	private String language;
	private String encoding;
	private String ontology;
	private String protocol;
	private String conversationID;
	private String replyWith;
	private Long replyBy;
	
	public ACLMessage() {
	}
	
	
	
	public ACLMessage(Performative performative, AID sender, AID[] receivers) {
		super();
		this.performative = performative;
		this.sender = sender;
		this.receivers = receivers;
		this.replyTo = null;
		this.content = "TEKSIC";
		this.contentObj = null;
		this.userArgs = null;
		this.language = null;
		this.encoding = null;
		this.ontology = null;
		this.protocol = null;
		this.conversationID = null;
		this.replyWith = null;
		this.replyBy = null;
	}



	public ACLMessage(ACLMessage copy, int reciver) {
		this.setSender(copy.getSender());
		this.setReceivers(new AID[] { copy.getReceivers()[reciver] });
		this.setContent(copy.getContent());
		this.setContentObj(copy.getContentObj());
		this.setConversationID(copy.getConversationID());
		this.setPerformative(copy.getPerformative());
		this.setProtocol(copy.getProtocol());
		this.setEncoding(copy.getEncoding());
		this.setReplyTo(copy.getReplyTo());
		this.setUserArgs(copy.getUserArgs());;
	}

	public Performative getPerformative() {
		return performative;
	}

	public void setPerformative(Performative performative) {
		this.performative = performative;
	}

	public AID getSender() {
		return sender;
	}

	public void setSender(AID sender) {
		this.sender = sender;
	}

	public AID[] getReceivers() {
		return receivers;
	}

	public void setReceivers(AID[] receivers) {
		this.receivers = receivers;
	}

	public AID getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(AID replyTo) {
		this.replyTo = replyTo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Object getContentObj() {
		return contentObj;
	}

	public void setContentObj(Object contentObj) {
		this.contentObj = contentObj;
	}

	public HashMap<String, Object> getUserArgs() {
		return userArgs;
	}

	public void setUserArgs(HashMap<String, Object> userArgs) {
		this.userArgs = userArgs;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}

	public String getReplyWith() {
		return replyWith;
	}

	public void setReplyWith(String replyWith) {
		this.replyWith = replyWith;
	}

	public Long getReplyBy() {
		return replyBy;
	}

	public void setReplyBy(Long replyBy) {
		this.replyBy = replyBy;
	}
	
}
