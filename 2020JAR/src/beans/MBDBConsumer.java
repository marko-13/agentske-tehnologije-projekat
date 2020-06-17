package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.ws.rs.core.Response;

import agents.Collector;
import agents.Master;
import agents.Predictor;
import model.ACLMessage;
import model.AID;
import model.Agent;
import ws.WSEndPoint;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destionationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})
public class MBDBConsumer implements MessageListener{
	
	/*@Override
	public void onMessage(Message msg) {
		TextMessage tmsg = (TextMessage)msg;
		try {
			System.out.println("MDB: " + tmsg.getText());
			System.out.println("\n\n\nUDJE I OVDE");
			//ws.echoTextMessage(tmsg.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}*/
	
	//@EJB
	//WSEndPoint ws;
	
	@Override
	public void onMessage(Message message) {
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			ACLMessage msg = (ACLMessage) ((ObjectMessage) message).getObject();
			AID[] receivers = msg.getReceivers();
			System.out.println("MESSAGE ON QUEUE! - " + message);
			for (AID a : receivers) {
				//am.msgToAgent(a, msg);
				// PROVERI DA LI JE U RECEIVERIMA RECEIVER KOJI JE NA OVOM HOSTU, AKO JESTE ONDA IDE NA ODREDJENOG AGENTA
				if(a.getHost().getAddress().equals(ip.getHostAddress())) {
					
					if(a.getType().getName().contains("COLLECTOR")) {
						Collector c = new Collector(a);
						c.handleMssage(msg);
					}
					else if (a.getType().getName().contains("PREDICTOR")) {
						Predictor p = new Predictor(a);
						p.handleMssage(msg);
					}
					else if (a.getType().getName().contains("MASTER")) {
						Master m = new Master(a);
						m.handleMssage(msg);
					}
					else {
						System.out.println("INVALID AGENT TYPE");
						return;
					}
				}
			}
		} catch (JMSException e) {
			System.out.println("MDBD Consumer exception");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("MDBD Consumer exception");
			e.printStackTrace();
		}
		
	}
}
