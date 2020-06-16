package agents;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import beans.DBBean;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Host;
import model.Message;
import model.Performative;
import ws.WSEndPoint;

@Stateful
public class Master extends Agent{

	@EJB
	WSEndPoint ws;
	
	@EJB
	DBBean db;
	
	public Master() {
		super();
	}
	
	public Master(AID a) {
		// TODO Auto-generated constructor stub
		this.id = a;
	}

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("MASTER AGENT HANDLE MESSAGE");
		
		if (message.getPerformative() == Performative.REQUEST) {
			
			// PROSLEDI NA ODGOVARAJUCI WEBSOCKET PORUKU SA REZULTATOM
			for (Host h : db.getHosts().values()) {
				
				if (message.getSender().getHost().getAddress().equals(h.getAddress())) {
					
					String hostPath = "http://" + h.getAddress() + ":8080/2020WAR/rest/server/prediction/" + db.getPredictionVal();

					try {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target(hostPath);
						Response res = target.request(MediaType.APPLICATION_JSON).get();
						String ret = res.readEntity(String.class);
						System.out.println("INFORMED NODE ABOUT RESULT: " + ret);
					}
					catch (Exception e) {
						System.out.println("ERROR IN INFORMING NODE ABOUT RESULT");
						return;
					}
				}
			}
		}
	}
}
