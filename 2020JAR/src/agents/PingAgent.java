package agents;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.DBBean;
import beans.JMSBuilder;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.AgentType;
import model.Host;
import model.Performative;
import ws.WSEndPoint;

@Stateful
public class PingAgent extends Agent{

	@EJB
	DBBean db;
	
	public PingAgent() {
		super();
	}
	
	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("PING AGENT HANDLE MESSAGE");
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		
		
		if (message.getPerformative() == Performative.REQUEST) {
			
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(this.id.getName() + " recieved message from " + message.getSender().getName() + ": " + message.getContent());
			} catch (NamingException e) {
				e.printStackTrace();
			}
			
			AID receiver = new AID();
			receiver.setName(message.getSender().getName());
			System.out.println("Request to send message " + message.getContent() + " to Pong.");
			AgentType type = new AgentType(PongAgent.class.getSimpleName(), PongAgent.class.getPackage().getName());
			receiver.setType(type);
			Host host = db.getHosts().get(ip.getHostAddress());
			if (host == null) {
				System.out.println("Error: Cannot locate host");
				return;
			}
			receiver.setHost(host);
			ACLMessage msg = new ACLMessage();
			msg.setPerformative(Performative.REQUEST);
			msg.setReceivers(new AID[] { receiver });
			msg.setSender(this.getId());
			msg.setContent(message.getContent());
			JMSBuilder.sendACL(msg);
		} else if (message.getPerformative() == Performative.INFORM) {
			try {
				Context context = new InitialContext();
				WSEndPoint ws = (WSEndPoint) context.lookup(WSEndPoint.LOOKUP);
				ws.echoTextMessage(this.id.getName() + " recieved message from " + message.getSender().getName() + ": " + message.getContent());
			} catch (NamingException e) {
				e.printStackTrace();
			}			System.out.println("Reply received from  " + message.getSender());
			System.out.println("Reply content: " + message.getContent());
		} 
	}
}
