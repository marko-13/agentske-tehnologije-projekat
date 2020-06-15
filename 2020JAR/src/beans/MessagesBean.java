package beans;

import java.net.InetAddress;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import model.ACLMessage;
import model.Host;
import model.Message;

@Stateless
@Path("/messages")
@LocalBean
public class MessagesBean {

	@EJB
	DBBean db;
	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	
	@POST
	@Path("")
	public Response postMessage(ACLMessage ACLMessage) {
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO POST MESSAGE ENDPOINT");
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).build();
		}
		// prodji kroz sve hostove i dodaj poruku u dbbean
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			//treba napisati rest za prijem poruke
			// na drugim hostovima
		}
		
		UUID uuid = UUID.randomUUID();
		
		db.getAclMessages().put(uuid, ACLMessage);
		
		// napravi promenljivu text koja je json reprezentacija ACLMessage objekta
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String msgJSON = "";
		try {
			msgJSON = ow.writeValueAsString(ACLMessage);
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String text = msgJSON;
		// JMS MESSAGE QUEUE
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			// create and publish a message
			TextMessage message = session.createTextMessage();
			message.setText(text);
			sender.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		return Response.status(200).entity("OK").build();
	}
	
	@GET
	@Path("")
	public Collection<ACLMessage> getMessages(){
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET MESSAGES ENDPOINT");
		
		return db.getAclMessages().values();
	}
}
