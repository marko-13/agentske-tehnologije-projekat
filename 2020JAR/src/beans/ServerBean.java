package beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.websocket.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.Agent;
import model.AgentType;
import model.Host;
import model.Message;
import model.User;
import ws.WSEndPoint;

@Stateless
@Path("/server")
@LocalBean
public class ServerBean {

	@EJB
	DBBean db;
	
	@EJB
	WSEndPoint ws;
	
	@GET
	@Path("/test")
	public String test() {
		System.out.println("UDJE U TEST");
		
		return "OK";
	}
	
	
	// NOVI CVOR SE JAVLJA MASTERU I MASTER GA DODAJE U LISTU HOSTOVA
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public String registerNewNode(Host newHost) {
		System.out.println("\n\nMASTER REGISTERING NEW NODE:\nAddress: " + newHost.getAddress() + "\nAlias: " + newHost.getAlias());
		
		
		// prodji kroz sve cvorove i javi im da dodaju novi cvor u svoje liste
		for (Host h: db.getHosts().values()) {
			System.out.println("MASTER INFORMING NODE: " + h.getAddress() + "TO ADD NEW NODE TO HOSTS LIST");
			String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/node/";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(newHost.getAlias(), newHost.getAddress(), false), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println(ret);
		}
		
		db.getHosts().put(newHost.getAlias(), newHost);

		return "MASTER ADDED NEW NODE AND PASSED IT TO ALL OTHER HOSTS";
	}
	 
	
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public String informNodesAboutNewNode(Host newHost) {
		System.out.println("\n\nMASTER INFORMED YOU ABOUT NEW NODE, ADDING TO THE LIST OF HOSTS NOW");
		
		db.getHosts().put(newHost.getAlias(), newHost);
		
		return "NEW NODE ADDED TO THE LIST OF HOSTS";
	}
	
	@POST
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Host> informNewNodeAboutAllExistingNodes(Host newHost) {
		System.out.println("MASTER NODE IS INFORMING NEW NODE ABOUT ALL OTHER NODES");
		
		// .....
		List<Host> hosts = new ArrayList<>();
		for (Host h : db.getHosts().values()) {
			System.out.println("Host: " + h.getAlias());
			hosts.add(h);
		}
		Collection<Host> myHosts = hosts;
		return myHosts;
	}
	
	@POST
	@Path("/users/loggedin")
	@Consumes(MediaType.APPLICATION_JSON)
	public Collection<User> sendAllLoggedInUsersToNewNode(Host newHost) {
		System.out.println("ALL LOGGED IN USERS ARE BEING SENT TO NEW NODE");
		
		// .....
		List<User> users = new ArrayList<>();
		for (User u : db.getLoggedInUsers().values()) {
			System.out.println("User: " + u.getUsername());
			users.add(u);
		}
		Collection<User> myUsers = users;
		return myUsers;
	}
	
	@POST
	@Path("/users/registered")
	@Consumes(MediaType.APPLICATION_JSON)
	public Collection<User> sendAllRegisteredUsersToNewNode(Host newHost) {
		System.out.println("ALL REGISTERED USERS ARE BEING SENT TO NEW NODE");
		
		// .....
		List<User> users = new ArrayList<>();
		for (User u : db.getUsers().values()) {
			System.out.println("User: " + u.getUsername());
			users.add(u);
		}
		Collection<User> myUsers = users;
		return myUsers;
	}
	
	@DELETE
	@Path("/node/{alias}")
	public String deleteNodeIfHandshakeHasFailed(@PathParam("alias")String alias) {
		System.out.println("HANDSHAKE FAILED OR NODE HEARTBEAT FAILED OR NODE WAS DELETED");
		System.out.println("DELETE SAID NODE FROM ALL LISTS AND DELETE LOGGED IN USERS FROM THAT NODE");
		
		String hostIP = db.getHosts().get(alias).getAddress();
		
		db.getHosts().remove(alias);
		
		for (User u : db.getLoggedInUsers().values()) {
			if (u.getHost().equals(hostIP)) {
				System.out.println("REMOVING USERS: " + u.getUsername());
				db.getLoggedInUsers().remove(u.getUsername());
				
				// i obrisi sa frontenda
				Message myMessage = new Message(u.getUsername(), 3);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
			}
		}
		System.out.println("BECAUSE HOST WAS STOPPED");
		
		return "OK";
	}
	
	@DELETE
	@Path("/agentType/{deletedHostIP}")
	public String deleteAgentTypeThatWasOnDeletedHost(@PathParam("deletedHostIP")String deletedHostIP) {
		for(AgentType aType : db.getAgentTypes().values()) {
			if (aType.getModule().equals(deletedHostIP)) {
				System.out.println("DELETING AGENT TYPE FROM STOPPED HOST");
				db.getAgentTypes().remove(aType.getName());
				
				// uradi da se obrise i sa frontenda
			}
		}
		
		return "OK";
	}
	
	@GET
	@Path("/node/informmaster/{alias}/{masterip}")
	public String informMasterForDeletion(@PathParam("alias") String alias, @PathParam("masterip") String masterip) {
		
		
		String hostIP = db.getHosts().get(alias).getAddress();
		
		System.out.println("REMOVING DELETED HOST FROM MASTER AND MASTER IS SENDING INFORMATION TO OTHER HOSTS");
		db.getHosts().remove(alias);
		// obrisi i sve tipove agenata iz ugasenog cvora
		for(AgentType aType : db.getAgentTypes().values()) {
			if (aType.getModule().equals(hostIP)) {
				System.out.println("DELETING AGENT TYPE FROM STOPPED HOST");
				db.getAgentTypes().remove(aType.getName());
				
				// prodji kroz sve hostove i obrisi i iz njih
				for (Host host : db.getHosts().values()) {
					if (host.getAddress().equals(masterip)) {
						continue;
					}
					String hostPath = "http://" + host.getAddress() + ":8080/2020WAR/rest/server/agentType/" + hostIP;
					
					try {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target(hostPath);
						Response res = target.request().delete();
						String ret = res.readEntity(String.class);
						System.out.println("DELETE AGENT TYPE FROM OTHER HOSTS RET: " + ret);
					}
					catch (Exception e) {
						System.out.println("ERROR IN NODE DELETION");
						return "Error";
					}
				}
			}
		}
		// URADITI DA SE OBRISE I SA FRONTA AGENT TYPES
		
		// obrisi sve running agents koji se nalaze na ugasenom cvoru sa ostalih cvorova
		
		
		
		for (User u : db.getLoggedInUsers().values()) {
			if (u.getHost().equals(hostIP)) {
				System.out.println("REMOVING USERS: " + u.getUsername());
				db.getLoggedInUsers().remove(u.getUsername());
				
				// i obrisi sa frontenda
				Message myMessage = new Message(u.getUsername(), 3);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
			}
		}
		
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(masterip)) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/node/" + alias;
			
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().delete();
				String ret = res.readEntity(String.class);
				System.out.println("DELETE HOST RET: " + ret);
			}
			catch (Exception e) {
				System.out.println("ERROR IN NODE DELETION");
				return "Error";
			}
		}
		
		return "OK";
	}
	
	@GET
	@Path("/node")
	public String heartbeat() {
		System.out.println("PERIODICNO PROVERAVAJ DA LI SU SVI CVOROVI AKTIVNI");
		return "OK";
	}
	
	
	// kad se neko registruje/loguje treba javiti i ostalim hostovima da azuriraju svoje liste
	@POST
	@Path("/newUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newUser(User myUser) {
		
		db.getUsers().put(myUser.getUsername(), myUser);
		db.getLoggedInUsers().put(myUser.getUsername(), myUser);
		
		// INFORM FRONTEND
		// kategorija poruke za dodavanje
		Message myMessage = new Message(myUser.getUsername(), 2);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return Response.status(200).build();
	}
	
	//kad se neko logoutuje treba javiti i ostalim hostovima da azuriraju svoje liste
	@POST
	@Path("/logoutUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logoutUser(User myUser) {
		System.out.println("UDJE OVDE U DELETE ON LOGOUT");
		db.getLoggedInUsers().remove(myUser.getUsername());
		
		// INFORM FRONTEND
		// kategorija poruke za brisanje
		Message myMessage = new Message(myUser.getUsername(), 3);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return Response.status(200).build();
	}
	
	
	// kad se dobije poruka prosledi je na websocket
	@POST
	@Path("/message")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addNewUser(Message myMessage) {
		
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return Response.status(200).build();
	}
	
	// prosledi koji tipovi agenata postoje kod tebe
	@GET
	@Path("/agents/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<AgentType> getAgentTypes() {
		
		AgentType at = new AgentType("Test agent 1", "Modul 1");
		db.getAgentTypes().put("Test agent 1", at);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET AGENT TYPES ENDPOINT");
	
		return db.getAgentTypes().values();
	}
	
	// novi cvor ti salje sve tipove agenata koji postoje na novom cvoru
	// ti ih samo dodaj
	@POST
	@Path("/newAgentTypeMaster")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addAgentTypesFromNewNode(AgentType newAgentType) {
		
		if (!db.getAgentTypes().containsKey(newAgentType.getName())) {
			
			for (Host h : db.getHosts().values()) {
				String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/newAgentType/";
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new AgentType(newAgentType.getName(), newAgentType.getModule()), MediaType.APPLICATION_JSON));
				String ret = res.readEntity(String.class);
				System.out.println(ret);
			}
			
			db.getAgentTypes().put(newAgentType.getName(), newAgentType);
		}
		
		return Response.status(200).build();
	}
	
	@POST
	@Path("/newAgentType")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addAgentTypesFromNewNodeNotMaster(AgentType newAgentType) {
		
		db.getAgentTypes().put(newAgentType.getName(), newAgentType);
		
		return Response.status(200).build();
	}
	
	@GET
	@Path("/runningAgents")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Agent> getRunningAgents() {
		
		return db.getAgentsRunning().values();
	}
}
