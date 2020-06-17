package agents;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import model.Performative;

@Stateful
public class Collector extends Agent{

	@EJB
	DBBean db;
	
	public Collector(AID a) {
		// TODO Auto-generated constructor stub
		this.id = a;
	}
	
	public Collector() {
		super();
	}

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("COLLECTOR AGENT HANDLE MESSAGE");
		Path pathToFile = Paths.get("D:/Desktop/50_Startups.csv");
		String all_lines = "";
		
		// AKO JE REQUEST ONDA MORAS DA VRATIS CSV PODATKE
		if (message.getPerformative() == Performative.REQUEST) {
            System.out.println("CSV FILE");

            // CHECK IP
            InetAddress ip = null;
    		try {
    			ip = InetAddress.getLocalHost();
    			
    		} catch (UnknownHostException e) {
    			e.printStackTrace();
    			return;
    		}
    		
			// READ INFORMATION FROM CSV FILE
			try (BufferedReader br = Files.newBufferedReader(pathToFile,
	                StandardCharsets.US_ASCII)) {

	            // read the first line from the text file
	            String line = br.readLine();

	            line = br.readLine();
	            // loop until all lines are read
	            while (line != null) {

	                // use string.split to load a string array with the values from
	                // each line of
	                // the file, using a comma as the delimiter
	                String[] attributes = line.split(",");
		            //System.out.println(line);
		            all_lines += line;

	                // read next line before looping
	                // if end of file reached, line would be null
	                line = br.readLine();
	            }

	        } catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
			
			// AKO NIJE NA MASTERU SALJI REST SA PODACIMA
			if (!ip.getHostAddress().equals("192.168.1.9")) {
				// u rest endpointu ce se poslati taj string i podaci ce se sacuvati u dbbean
				// svi podaci se cuvaju samo na masteru
				for (Host h : db.getHosts().values()) {
					if (h.getAddress().equals("192.168.1.9")) {
						String hostPath = "http://" + "192.168.1.9" + ":8080/2020WAR/rest/server/csvData/";

						try {
							ResteasyClient client = new ResteasyClientBuilder().build();
							ResteasyWebTarget target = client.target(hostPath);
							Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity((new String(all_lines)), MediaType.APPLICATION_JSON));
							String ret = res.readEntity(String.class);
							System.out.println("INFORMED MASTER ABOUT NEW CSV DATA: " + ret);
						}
						catch (Exception e) {
							System.out.println("ERROR IN COLLECTOR AGENT SENDING INFORMATION TO MASTER");
							return;
						}
					}
					
					
				}
			}
			// AKO JE NA MASTERU SACUVAJ PODATKE U DBBEAN
			else {
				System.out.println("HELLO FROM MASTER COLLECTOR AGENT REQUEST");

				Context ctx = null;
				DBBean db = null;
				try {
					ctx = new InitialContext();
				} catch (NamingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					db = (DBBean) ctx.lookup(DBBean.LOOKUP);
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String temp_lines = db.getCsvData();
				System.out.println("TEMP LINES: " + temp_lines);
				db.setCsvData(temp_lines + all_lines);
				System.out.println(all_lines);
				
			}
		}
		// INFORM CE SAMO RADITI NEKI ISPIS
		else if (message.getPerformative() == Performative.INFORM) {
			
		}
		// ISPIS DA JE ZA OVOG AGENTA TAJ PERFORMATIV NEPODRZAN
		else {
			System.out.println("UNSUPPORTED PERFORMATIVE FOR SELECTED AGENT");
		}
		
	}
}
