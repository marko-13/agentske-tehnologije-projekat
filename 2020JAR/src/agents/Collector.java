package agents;

import javax.ejb.Stateful;

import model.ACLMessage;
import model.Agent;

@Stateful
public class Collector extends Agent{

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("COLLECTOR AGENT HANDLE MESSAGE");
		
	}
}
