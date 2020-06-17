package agents;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.LocalBean;
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
import ws.WSEndPoint;

@Stateful
public class Predictor extends Agent{
	
	public Predictor(AID a) {
		// TODO Auto-generated constructor stub
		this.id = a;
	}
	
	public Predictor() {
		super();
	}

	@Override
	public void handleMssage(ACLMessage message) {
		// TODO Auto-generated method stub
		System.out.println("PREDICTOR AGENT HANDLE MESSAGE");
		
		if (message.getPerformative() == Performative.REQUEST) {
			
			int brojac = 1;
			/*for (String s : db.getCsvData().split(",")) {
				if ((brojac % 5) == 0) {
					// peta rec
					pw.append(s);
					pw.append("\n");
					brojac = 1;
				}
				else if ((brojac % 4) == 0) {
					// cetvrta rec
					pw.append(s);
					pw.append(",");
					brojac ++;
				}
				else if ((brojac % 3) == 0) {
					// treca rec
					pw.append(s);
					pw.append(",");
					brojac ++;
				}
				else if ((brojac % 2) == 0) {
					// druga rec
					pw.append(s);
					pw.append(",");
					brojac ++;
				}
				else if ((brojac % 1) == 0 ) {
					// prva rec
					pw.append(s);
					pw.append(",");
					brojac ++;
				}
				
			}*/ 
			String params = message.getLanguage();
			String[] paramsSplit = params.split("!");
			
			if (Double.parseDouble(paramsSplit[0]) >= 1) {
				paramsSplit[0] = String.valueOf(1);
			}
			else {
				paramsSplit[0] = String.valueOf(0);
			}
				
				// MORACE PREKO RESTA DA SE POSALJE VREDNOST PREDIKCIJE
			String myValue = "";
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
				//RESTEASY POZIV ZA FLASK SERVER
			HttpURLConnection conn = null;
	        DataOutputStream os = null;
	        try{
	            URL url = new URL("http://192.168.1.9:5000/pred/1/"+ paramsSplit[1] + "/" + paramsSplit[2] + "/" + paramsSplit[3] + "/"); //important to add the trailing slash after add
	            String[] inputData2 = {"{\"x\": 5, \"y\": 8, \"text\":\"random text\"}",
	                    "{\"x\":5, \"y\":14, \"text\":\"testing\"}"};
	            String[] inputData = {"{\"data\": \"" + db.getCsvData() + "\"}"};
	            for(String input: inputData){
	                byte[] postData = input.getBytes(StandardCharsets.UTF_8);
	                conn = (HttpURLConnection) url.openConnection();
	                conn.setDoOutput(true);
	                conn.setRequestMethod("POST");
	                conn.setRequestProperty("Content-Type", "application/json");
	                conn.setRequestProperty( "charset", "utf-8");
	                conn.setRequestProperty("Content-Length", Integer.toString(input.length()));
	                os = new DataOutputStream(conn.getOutputStream());
	                os.write(postData);
	                os.flush();

	                if (conn.getResponseCode() != 200) {
	                    throw new RuntimeException("Failed : HTTP error code : "
	                            + conn.getResponseCode());
	                }

	                BufferedReader br = new BufferedReader(new InputStreamReader(
	                        (conn.getInputStream())));

	                String output;
	                System.out.println("Output from Server .... \n");
	                while ((output = br.readLine()) != null) {
	                    System.out.println(output);
	                    myValue = output;
	                }
	                conn.disconnect();
	            }
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }catch (IOException e){
	        e.printStackTrace();
	    }finally
	        {
	            if(conn != null)
	            {
	                conn.disconnect();
	            }
	        }
	        
	        db.setPredictionVal(Double.parseDouble(myValue));
	    
			// OVO JE SAMO NA MASTERU
	        // TREBA PREZENTOVATI REZULTAT NEKAKO
			/*String hostPath = "http://" + "192.168.1" + ":5000/pred/1/1/1/1/";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new String(db.getCsvData()), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println("PREDICTOR RET: " + ret);*/
			
			
			// KAD OVO SVE PRODJE ONDA MORAS POSLATI SVIM HOSTOVIMA OVU VREDNOST I STAVITI JE U SVAKI DBBEAN
			// IPAK NE MORA JER UVEK SA MASTERA VUCES
			
		}
		
		else if (message.getPerformative() == Performative.INFORM) {
			// URADI NEKI INFORM
		}
		
	}
}
