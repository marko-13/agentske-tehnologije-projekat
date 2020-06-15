package model;

import javax.ejb.Local;

@Local
public interface AgentInterface {

	void handleMssage(ACLMessage message);
}
