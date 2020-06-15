package agents;

import javax.ejb.Stateful;

import model.ACLMessage;
import model.Agent;

@Stateful
public class PongAgent extends Agent{

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("PONG AGENT HANDLE MESSAGE");
		
	}
}
