import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Communication {
	Server server;
	Socket requestConn[];           //socket connect to the server      //Can be merged in neighbour     //socket
    ObjectOutputStream out[];         //stream write to the socket		//same							//Socket
//    ObjectInputStream in[];  
	
	
	public Communication(Property prop){
		server = new Server(prop);   //Listens for Connections
		
		requestConn = new Socket[prop.indexMap.size()];
		out = new ObjectOutputStream[prop.indexMap.size()];
//		in = new ObjectInputStream[prop.indexMap.size()];
		
	}
}
