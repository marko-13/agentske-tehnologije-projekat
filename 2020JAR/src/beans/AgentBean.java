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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.AID;
import model.Agent;
import model.AgentType;
import model.Host;
import ws.WSEndPoint;

@Stateless
@Path("/agents")
@LocalBean
public class AgentBean{

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


	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<AgentType> getAgentTypes() {
		
		// MODUL AGENTA JE IP ADRESA HOSTA
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		AgentType at = new AgentType("Test agent 1", ip.getHostAddress());
		db.getAgentTypes().put(at.getName()+at.getModule(), at);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET AGENT TYPES ENDPOINT");
	
		return db.getAgentTypes().values();
	}
	
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Agent> getRunningAgents() {
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET AGENT TYPES ENDPOINT");
		
		return db.getAgentsRunning().values();
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	public Response startAgent(@PathParam("type")String type, @PathParam("name")String name) {
		
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO PUT AGENT ENDPOINT");
		// URADITI
		// Ako vec postoji agent sa takvim imenom vrati error
		for (Agent agent : db.getAgentsRunning().values()) {
			if (agent.getAid().getName().equals(name)) {
				return Response.status(400).entity("Naming error").build();
			}
		}
		
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
			if (host.getAddress().equals(ip.getHostAddress())) {
				myHost = host;
			}
		}
		if (myHost.equals(null)) {
			return Response.status(400).entity("IP error").build();
		}
		
		// prodji kroz sve hostove i posalji im novog agenta koji je pokrenut
		
		
		Agent newAgent = new Agent(new AID(name, myHost, myAgentType));
		db.getAgentsRunning().put(name, newAgent);
		
		return Response.status(200).entity(new Agent()).build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	public Response stopAgent(@PathParam("aid")String aid) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO PUT AGENT ENDPOINT");
		// URADITI
		Agent myAgent = null;
		for (Agent agent : db.getAgentsRunning().values()) {
			if (agent.getAid().getName().equals(aid)) {
				myAgent = agent;
				break;
			}
		}
		if (myAgent.equals(null)) {
			return Response.status(400).entity("Agent does not exist").build();
		}
		
		// prodji kroz ostale hostove i njima isto izbrisi
		
		db.getAgentsRunning().remove(myAgent);
		
		return Response.status(200).entity("OK").build();
	}
	
}
