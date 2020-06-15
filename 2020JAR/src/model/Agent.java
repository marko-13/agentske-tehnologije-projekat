package model;

import javax.ejb.Singleton;

@Singleton
public abstract class Agent implements AgentInterface{

	protected AID id;

	public AID getId() {
		return id;
	}
	
	public AID getAid() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}
	
}
