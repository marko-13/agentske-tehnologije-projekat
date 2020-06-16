package agents;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.DBBean;
import model.ACLMessage;
import model.AID;
import model.Agent;
import model.Performative;
import ws.WSEndPoint;

@Stateful
public class Predictor extends Agent{

	@EJB
	DBBean db;
	
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
			
			// PREDIKCIJA SE MOZE IZVRSITI SAMO NA MASTERU
			try {
				FileWriter pw = new FileWriter("D:/Desktop/50_StartupsPOM.csv",true);
				
				int brojac = 1;
				for (String s : db.getCsvData().split(",")) {
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
					
				}
				
				pw.flush();
				pw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} 
			try {
				Process p = Runtime.getRuntime().exec("python yourapp.py");
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String ret = in.readLine();
				System.out.println("\n\n\nvalue from python is : " + ret);
				
				db.setPredictionVal(100);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			// KAD OVO SVE PRODJE ONDA MORAS POSLATI SVIM HOSTOVIMA OVU VREDNOST I STAVITI JE U SVAKI DBBEAN
			// IPAK NE MORA JER UVEK SA MASTERA VUCES
			
		}
		
		else if (message.getPerformative() == Performative.INFORM) {
			// URADI NEKI INFORM
		}
		
	}
}
