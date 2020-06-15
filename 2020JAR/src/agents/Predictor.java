package agents;

import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import model.ACLMessage;
import model.Agent;
import model.Performative;
import ws.WSEndPoint;

@Stateful
public class Predictor extends Agent{

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("PREDICTOR AGENT HANDLE MESSAGE");
		
		if (message.getPerformative() == Performative.REQUEST) {
			
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(this.id.getName() + " recieved message from " + message.getSender().getName() + ": " + message.getContent());
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		
	}
}
