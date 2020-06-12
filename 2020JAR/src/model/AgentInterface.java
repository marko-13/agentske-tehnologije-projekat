package model;

public interface AgentInterface {

	void init(AID aid);
	void stop();
	void handleMssage(ACLMessage message);
	void setAid(AID aid);
	AID getAid();
}
