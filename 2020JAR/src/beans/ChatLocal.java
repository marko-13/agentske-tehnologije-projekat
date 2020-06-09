package beans;

import javax.ejb.Local;

@Local
public interface ChatLocal {
	
	public String post(String text);

}
