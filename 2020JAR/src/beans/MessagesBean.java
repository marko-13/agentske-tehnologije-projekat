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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dto.ACLMessageDTO;
import model.ACLMessage;
import model.AID;
import model.Host;
import model.Message;
import model.Performative;

@Stateless
@Path("/messages")
@LocalBean
public class MessagesBean {

	@EJB
	DBBean db;
	
	
	
	@POST
	@Path("")
	public Response postMessage(ACLMessageDTO ACLMessageDTO) {
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO POST ACL MESSAGE ENDPOINT");
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).build();
		}
		
		ACLMessage myACL = new ACLMessage();
		if (ACLMessageDTO.getPerformative().equals("REQUEST")) {
			myACL.setPerformative(Performative.REQUEST);
		}
		else {
			myACL.setPerformative(Performative.INFORM);
		}

		myACL.setReceivers(new AID[ACLMessageDTO.getReceivers().length]);
		int brojac = 0;
		for (String s : ACLMessageDTO.getReceivers()) {
			myACL.getReceivers()[brojac] = db.getAgentsRunning().get(s);
			brojac ++;
		}
		
		myACL.setSender(db.getAgentsRunning().get(ACLMessageDTO.getSender()));
		
		// prodji kroz sve hostove i dodaj poruku u dbbean
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			// URADI
			//treba napisati rest za prijem poruke
			// na drugim hostovima
			try {
				String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/newACLMessage/";
				int numberOfRecivers = ACLMessageDTO.getReceivers().length;
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new ACLMessage(myACL, numberOfRecivers)), MediaType.APPLICATION_JSON));
				String ret = res.readEntity(String.class);
				System.out.println("FORWARD ACL TO OTHER HOSTS: " + ret);
			}
			catch (Exception e) {
				System.out.println("ERROR IN FORWARDING ACL MESSAGE TO ALL HOSTS");
				return Response.status(400).build();
			}
		}
		
		UUID uuid = UUID.randomUUID();
		
		db.getAclMessages().put(uuid, myACL);

		JMSBuilder.sendACL(myACL);
		
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
