package beans;

import java.util.Collection;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import model.ACLMessage;

@Stateless
@Path("/messages")
@LocalBean
public class MessagesBean {

	@EJB
	DBBean db;
	
	@POST
	@Path("")
	public Response postMessage(ACLMessage message) {
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO POST MESSAGE ENDPOINT");
		
		// prodji kroz sve hostove i dodaj poruku u dbbean
		
		db.getAclMessages().put(UUID.randomUUID(), message);
		
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
