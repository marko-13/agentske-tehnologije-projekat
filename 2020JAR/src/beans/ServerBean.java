package beans;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

import model.ACLMessage;
import model.AID;
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
	// REGISTRACIJA NOVOG CVORA
	// KAD SE PODIGNE NOVI CVOR ON CE POSLATI POST REQUEST NA MASTER I MASTER CE DA PRODJE KROZ LISTU HOSTOVA I PROSLEDI IM INFORMACIJE O NOVOM CVORU
	// NOVI CVOR SALJE POST REQUEST MASTERU SA INFORMACIJAMA O SEBI
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
	 
	
	// MASTER PROSLEDJUJE OSTALIM CVOROVIMA INFORMACIJE O NOVOM STARTOVANOM CVORU
	// MASTER SALJE POST REQUEST NA OSTALE CVOROVE SA INFORMACIJOM O NOVOM CVORU
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public String informNodesAboutNewNode(Host newHost) {
		System.out.println("\n\nMASTER INFORMED YOU ABOUT NEW NODE, ADDING TO THE LIST OF HOSTS NOW");
		
		db.getHosts().put(newHost.getAlias(), newHost);
		
		return "NEW NODE ADDED TO THE LIST OF HOSTS";
	}
	
	// NOVI STARTOVANI CVOR DOBIJA INFORMACIJU OD MASTERA KOJI SVE HOSTOVI POSTOJE
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
	
	// NOVI STARTOVANI CVOR DOBIJA INFORMACIJU OD MASTERA KOJI SVE ULOGOVANI KORISNICI POSTOJE
	// NOVI CVOR SALJE POST REQUEST NA MASTER I DOBIJA LISTU ULOGOVANIH KORISNIKA
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
	
	// NOVI STARTOVAN CVOR DOBIJA INFORMACIJU OD MASTERA KOJI SVE REGISTROVANI KORISNICI POSTOJE
	// NOVI CVOR SALJE POST REQUEST NA MASTER I DOBIJA LISTU REGISTROVANIH KORISNIKA
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
	
	// UKOLIKO HANDSHAKE NIJE USPEO ILI SE NIJE JAVIO NA HEARTBEATU NOVI CVOR JAVLJA MASTERU DA GA MASTER OBRISE IZ LISTE
	@DELETE
	@Path("/node/{alias}")
	public String deleteNodeIfHandshakeHasFailed(@PathParam("alias")String alias) {
		System.out.println("HANDSHAKE FAILED OR NODE HEARTBEAT FAILED OR NODE WAS DELETED");
		System.out.println("DELETE SAID NODE FROM ALL LISTS AND DELETE LOGGED IN USERS FROM THAT NODE");
		
		String hostIP = db.getHosts().get(alias).getAddress();
		
		db.getHosts().remove(alias);
		
		// treba i da obrise sve ulogovane korisnike sa novog cvora - OVO BI TREBALO DA JE UVEK PRAZNO OSIM AKO NE OTKAZE HEARTBEAT
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
		
		//  master treba da obrise i agent types od novog cvora ukoliko su dodati u listu - OVO SE MOZE DESITI
		for (AgentType aType : db.getAgentTypes().values()) {
			if (aType.getModule().equals(hostIP)) {
				System.out.println("REMOVING AGENT TYPE: " + aType.getName());
				db.getAgentTypes().remove(aType.getName());
				
				// i obrisi sa frontenda
				//                               content         category
				Message myMessage = new Message(aType.getName(), 5);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
			}
		}
		
		// master treba da obrise i all running agents sa novog cvora - TO JE UVEK PRAZNO OSIM AKO NE OTKAZE HEARTBEAT
		for (AID aRunning : db.getAgentsRunning().values()) {
			if (aRunning.getHost().getAddress().equals(hostIP)) {
				System.out.println("REMOVING RUNNING AGENT: " + aRunning.getName());
				db.getAgentsRunning().remove(aRunning.getName());
				
				// i obrisi sa frontenda
				//                               content                 category
				Message myMessage = new Message(aRunning.getName(), 7);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
			}
		}
		System.out.println("BECAUSE HOST WAS STOPPED");
		
		return "OK";
	}
	
	
	// predestroy hosta poziva get request na master i master ovde treba da obrise podatke o tom hostu, njegove tipove agenta, running agents i logged in users
	@GET
	@Path("/node/informmaster/{alias}/{masterip}")
	public String informMasterForDeletion(@PathParam("alias") String alias, @PathParam("masterip") String masterip) {
		
		
		String hostIP = db.getHosts().get(alias).getAddress();
		
		System.out.println("REMOVING DELETED HOST FROM MASTER AND MASTER IS SENDING INFORMATION TO OTHER HOSTS");
		db.getHosts().remove(alias);
		// obrisi i sve tipove agenata iz ugasenog cvora
		// brisanje sa fronta sa ostalih hostova je odradjeno u DELETE server/agentType/hostIP
		// brisanje sa fronta mastera je ovde u for petlji
		for(AgentType aType : db.getAgentTypes().values()) {
			if (aType.getModule().equals(hostIP)) {
				System.out.println("DELETING AGENT TYPE FROM STOPPED HOST");
				db.getAgentTypes().remove(aType.getName());
				// i obrisi sa frontenda na masteru
				//                               content         category
				Message myMessage = new Message(aType.getName(), 5);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
				
			}
		}
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
				System.out.println("ERROR IN AGENT TYPE DELETION");
				return "Error";
			}
		}

		
		// obrisi sve running agents koji se nalaze na ugasenom cvoru sa ostalih cvorova
		for(AID aRunning : db.getAgentsRunning().values()) {
			if (aRunning.getHost().getAddress().equals(hostIP)) {
				System.out.println("DELETING RUNNING AGENT FROM STOPPED HOST");
				db.getAgentsRunning().remove(aRunning.getName());
				// i obrisi sa frontenda na masteru
				//                               content                 category
				Message myMessage = new Message(aRunning.getName(), 7);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
				
				// prodji kroz sve ostale hostove i obrisi i iz njih
				for (Host h : db.getHosts().values()) {
					if (h.getAddress().equals(masterip)) {
						continue;
					}
					
				}
			}
		}
		for (Host host : db.getHosts().values()) {
			if (host.getAddress().equals(masterip)) {
				continue;
			}
			String hostPath = "http://" + host.getAddress() + ":8080/2020WAR/rest/server/agentRunning/" + hostIP;
			
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().delete();
				String ret = res.readEntity(String.class);
				System.out.println("DELETE AGENT RUNNING FROM OTHER HOSTS RET: " + ret);
			}
			catch (Exception e) {
				System.out.println("ERROR IN AGENT RUNNING DELETION");
				return "Error";
			}
		}
		
		
		// obrisi i sve ulogovane korisnike
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
	
	// KORISTI SE KAD MASTER OBAVESTAVA DRUGE HOSTOVE DA JE NEKI OD HOSTOVA STOPIRAN
	@DELETE
	@Path("/agentType/{deletedHostIP}")
	public String deleteAgentTypeThatWasOnDeletedHost(@PathParam("deletedHostIP")String deletedHostIP) {
		for(AgentType aType : db.getAgentTypes().values()) {
			if (aType.getModule().equals(deletedHostIP)) {
				System.out.println("DELETING AGENT TYPE FROM STOPPED HOST ON RUNNING HOST");
				db.getAgentTypes().remove(aType.getName());
				
				// uradi da se obrise i sa frontenda
				// i obrisi sa frontenda
				//                               content         category
				Message myMessage = new Message(aType.getName(), 5);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());
			}
		}
		
		return "OK";
	}
	
	// KORISTI SE KAD MASTER OBAVESTAVA DRUGE HOSTOVE DA JE NEKI OD HOSTOVA STOPIRAN
	@DELETE
	@Path("/agentRunning/{deletedHostIP}")
	public String deleteAgentRunningThatWasOnDeletedHOst(@PathParam("deletedHostIP")String deletedHostIP) {
		for (AID aRunning : db.getAgentsRunning().values()) {
			if (aRunning.getHost().getAddress().equals(deletedHostIP)) {
				System.out.println("DELETING AGENT RUNNING FROM STOPPED HOST ON RUNNING HOST");
				db.getAgentsRunning().remove(aRunning.getName());
				
				// uradi da se obrise i sa frontends
				// i obrisi sa frontenda
				//                               content                 category
				Message myMessage = new Message(aRunning.getName(), 7);
				db.getAllMessages().put(myMessage.getId(), myMessage);
				ws.echoTextMessage(myMessage.getId().toString());				
			}
		}
		
		return "OK";
	}
	
	// HEARTBEAT
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
	public Collection<AID> getRunningAgents() {
		
		return db.getAgentsRunning().values();
	}
	
	
	// BITNOOOOOOO
	// OVO TREBA POZVATI KAD SE KREIRA NOVI AGENT U KLASI AGENT BEAN
	// kad se pokrene novi agent
	// newRunning agent
	// saljes masteru i master obavlja ovo dole u funkciji i prosledjuje ostalim hostovima
	@POST
	@Path("/hostStartedNewAgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response hostStartedNewAgent(AID agent) {
		
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).build();
		}
		// prvo prosledi svim hostovima
		for (Host h : db.getHosts().values()) {
			// ako je to cvor iz koga je pozvano preskoci jer je on vec dodao
			if (h.getAddress().equals(agent.getHost().getAddress())) {
				continue;
			}
			// ako si na masteru preskoci
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/newAgentRunning/";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new AID(agent.getName(), agent.getHost(), agent.getType())), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println(ret);
		}
		
		db.getAgentsRunning().put(agent.getName(), agent);
		// obavesti i frontend
		Message myMessage = new Message(agent.getName(), 6);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return Response.status(200).build();
	}
	
	// poziva se iz host started new agent
	@POST
	@Path("/newAgentRunning")
	@Consumes(MediaType.APPLICATION_JSON)
	public String addNewRunningAgent(AID agent) {
		
		db.getAgentsRunning().put(agent.getName(), agent);
		// obavesti i frontend
		Message myMessage = new Message(agent.getName(), 6);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return "OK";
	}
	
	// BITNOOOOO
	// pozovi ovo i kad neko zaustavi agent u agentbeanu
	// kad se zaustavi agent
	// stop running agent
	// saljes masteru 
	@POST
	@Path("/hostStoppedAgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response hostStoppedAgent(AID agent) {
		
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).build();
		}
		// prvo prosledi svim hostovima
		for (Host h : db.getHosts().values()) {
			// ako je to cvor iz koga je pozvano preskoci jer je on vec dodao
			if (h.getAddress().equals(agent.getHost().getAddress())) {
				continue;
			}
			// ako si na masteru preskoci
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/newAgentStopped/";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new AID(agent.getName(), agent.getHost(), agent.getType())), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println(ret);
		}
		
		db.getAgentsRunning().remove(agent.getName());
		// obavesti i frontend
		Message myMessage = new Message(agent.getName(), 7);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return Response.status(200).build();
	}
	
	// pozove se iz host stopped new agent
	@POST
	@Path("/newAgentStopped")
	@Consumes(MediaType.APPLICATION_JSON)
	public String newAgentStopped(AID agent) {
		
		db.getAgentsRunning().remove(agent.getName());
		// obavesti i frontend
		Message myMessage = new Message(agent.getName(), 7);
		db.getAllMessages().put(myMessage.getId(), myMessage);
		ws.echoTextMessage(myMessage.getId().toString());
		
		return "OK";
	}
	
	
	// ENDPOINT DA TI PROSLEDI ACL PORUKU
	@GET
	@Path("/newACLMessage")
	@Consumes(MediaType.APPLICATION_JSON)
	public String newACLMessage(ACLMessage message) {
		JMSBuilder.sendACL(message);
		UUID uuid = UUID.randomUUID();
		
		db.getAclMessages().put(uuid, message);
		return "OK";
	}
	
	// KAD TI NEMASTER CVOR POSALJE PODATKE IZ FAJLA
	@GET
	@Path("/csvData")
	@Consumes(MediaType.APPLICATION_JSON)
	public String csvData(String csvFile) {
		
		String temp_lines = db.getCsvData();
		db.setCsvData(temp_lines + csvFile);
		
		return "OK";
	}
	
	// KAD SE DOBIJE RESENJE PREDIKCIJE POSLAJI PREKO WEBSOCKETA NA FRONT
	// kad se dobije poruka prosledi je na websocket
	@GET
	@Path("/prediction/{predictionVal}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response prediction(@PathParam("predictionVal")String predictionVal) {
		
		Message m = new Message(predictionVal, 8);
		db.getAllMessages().put(m.getId(), m);
		ws.echoTextMessage(m.getId().toString());
		
		return Response.status(200).build();
	}
}
