
import java.util.*;

public class Server {

	public  List<Message> messagesFromPeers;

	
	Property prop;



	public Server(Property prop) {
		this.prop = prop;
		messagesFromPeers = new ArrayList<Message>();
		new Thread(new ServerListener(prop.port,messagesFromPeers)).start();
	}
}