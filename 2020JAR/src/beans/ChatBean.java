package beans;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ws.WSEndPoint;

@Stateless
@Path("/chat")
@LocalBean
public class ChatBean implements ChatLocal{

	@EJB
	WSEndPoint ws;
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "OK";
	}

	@Override
	public String post(String text) {
		// TODO Auto-generated method stub
		return null;
	}
}
