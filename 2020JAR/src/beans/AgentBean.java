package beans;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import model.AID;
import model.AgentType;
import model.Host;
import ws.WSEndPoint;

@Stateless
@Path("/agents")
@LocalBean
public class AgentBean{

	private static final String MASTERIP = "192.168.1.9";
	
	@EJB
	WSEndPoint ws;
	
	@EJB
	DBBean db;

	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "OK";
	}


	// VRACA SVE TIPOVE AGENATA SA SVIH HOSTOVA
	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<AgentType> getAgentTypes() {
		// MODUL TIPA AGENTA JE IP ADRESA HOSTA
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET AGENT TYPES ENDPOINT");
	
		return db.getAgentTypes().values();
	}
	
	// VRACA SVE POKRENUTE AGENTE SA SVIH HOSTOVA
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<AID> getRunningAgents() {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET AGENT TYPES ENDPOINT");
		
		return db.getAgentsRunning().values();
	}
	
	// POKRECE NOVOG AGENTA
	// MORAS POZVATI I REQUEST DA MASTER OBAVESTI OSTALE CVOROVE
	@PUT
	@Path("/running/{type}/{name}")
	public Response startAgent(@PathParam("type")String type, @PathParam("name")String name) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO PUT AGENT ENDPOINT");
		// Ako vec postoji agent sa takvim imenom vrati error
		for (AID agent : db.getAgentsRunning().values()) {
			if (agent.getName().equals(name)) {
				return Response.status(400).entity("Naming error").build();
			}
		}
		
		//--------------------------------------------------------
		// Nadji tip agenta
		AgentType myAgentType = null;
		for (AgentType agentType : db.getAgentTypes().values()) {
			if(agentType.getName().equals(type)) {
				myAgentType = agentType;
				break;
			}
		}
		if (myAgentType.equals(null)) {
			return Response.status(400).entity("Type does not exist").build();
		}
		
		//---------------------------------------------------------
		// Nadji sa cijeg hosta je pokrenut agent
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).entity("IP error").build();
		}
		Host myHost = null;
		for (Host host : db.getHosts().values()) {
			if (host.getAddress().equals(myAgentType.getModule())) {
				myHost = host;
			}
		}
		if (myHost.equals(null)) {
			return Response.status(400).entity("IP error").build();
		}
				
		
		//--------------------------------------------------------------------------------
		// prodji kroz sve hostove i posalji im novog agenta koji je pokrenut
		String hostPath = "http://" + MASTERIP + ":8080/2020WAR/rest/server/hostStartedNewAgent/";
		
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new AID(name, myHost, myAgentType)), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println("NEW AGENT RUNNING RET: " + ret);
		}
		catch (Exception e) {
			System.out.println("ERROR IN ADDING NEW RUNNING AGENT");
			return Response.status(400).build();
		}
		
		AID newAgent = (new AID(name, myHost, myAgentType));
		db.getAgentsRunning().put(name, newAgent);
		
		return Response.status(200).build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	public Response stopAgent(@PathParam("aid")String aid) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO PUT AGENT ENDPOINT");
		// URADITI
		AID myAgent = null;
		for (AID agent : db.getAgentsRunning().values()) {
			if (agent.getName().equals(aid)) {
				myAgent = agent;
				break;
			}
		}
		if (myAgent.equals(null)) {
			return Response.status(400).entity("Agent does not exist").build();
		}
		
		// prodji kroz ostale hostove i njima isto izbrisi
		String hostPath = "http://" + MASTERIP + ":8080/2020WAR/rest/server/hostStoppedAgent/";
		
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new AID(myAgent.getName(), myAgent.getHost(), myAgent.getType())), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println("DELETE HOST RET: " + ret);
		}
		catch (Exception e) {
			System.out.println("ERROR IN ADDING NEW RUNNING AGENT");
			return Response.status(400).build();
		}
		
		db.getAgentsRunning().remove(myAgent.getName());
		
		return Response.status(200).entity("OK").build();
	}
	
}
