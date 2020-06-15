package beans;
 
import java.io.Serializable;
 
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
 
import model.ACLMessage;
 
public class JMSBuilder {
   
    public static final String FACTORY = "java:jboss/exported/jms/RemoteConnectionFactory";
    public static final String MDB_CONSUMER_QUEUE = "java:jboss/exported/jms/queue/mojQueue";
    public static final String USERNAME = "guest";
    public static final String PASSWORD = "guest.guest.1";
   
    public static boolean sendACL(ACLMessage msg) {
        try {
            Context context = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) context.lookup(JMSBuilder.FACTORY);
            System.out.println(cf);
            final Queue queue = (Queue) context.lookup(JMSBuilder.MDB_CONSUMER_QUEUE);
            context.close();
            Connection connection = cf.createConnection(JMSBuilder.USERNAME, JMSBuilder.PASSWORD);
            final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            ObjectMessage tmsg = session.createObjectMessage((Serializable) msg);
            //tmsg.setJMSDeliveryTime(12000);
            MessageProducer producer = session.createProducer(queue);
            producer.setTimeToLive(12000);
            producer.send(tmsg);
            producer.close();
            connection.stop();
            connection.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
       
       
    }
 
}