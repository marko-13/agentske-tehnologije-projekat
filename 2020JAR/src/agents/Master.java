package agents;

import javax.ejb.Stateful;

import model.ACLMessage;
import model.Agent;

@Stateful
public class Master extends Agent{

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("MASTER AGENT HANDLE MESSAGE");
		
	}
}
