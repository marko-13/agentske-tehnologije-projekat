package beans;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import model.ACLMessage;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destionationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})
public class MBDBConsumer implements MessageListener{
	
	@Override
	public void onMessage(Message msg) {
		TextMessage tmsg = (TextMessage)msg;
		try {
			System.out.println("MDB: " + tmsg.getText());
			System.out.println("\n\n\nUDJE I OVDE");
			//ws.echoTextMessage(tmsg.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
