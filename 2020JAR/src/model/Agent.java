package model;

public class Agent implements AgentInterface{

	private AID id;
	
	public Agent() {
		this.id = null;
	}

	public Agent(AID id) {
		super();
		this.id = id;
	}

	public AID getId() {
		return id;
	}

	public void setId(AID id) {
		this.id = id;
	}

	@Override
	public void init(AID aid) {
		// TODO Auto-generated method stub
		this.id = aid;
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		System.out.println("AGENT STOPED");
	}

	@Override
	public void handleMEssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("AGENT HANDLE MESSAGE");
		
	}

	@Override
	public void setAid(AID aid) {
		// TODO Auto-generated method stub
		this.id = aid;
	}

	@Override
	public AID getAid() {
		// TODO Auto-generated method stub
		return id;
	}
	
}
