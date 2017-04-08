/* To compile, run the following command:
 * javac Client.java Message.java Neighbor.java ServerListener.java PeerProcess.java
 */

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;

public class Server {

	public  List<Message> messagesFromPeers;

//	private int peerId;
//	private String hostName;
//	private int portNumber; //The listener will be listening on this port number
	
	Property prop;



	public Server(Property prop) {
		this.prop = prop;
//		this.hostName = hostName;
//		this.portNumber = portNumber;
		messagesFromPeers = new ArrayList<Message>();
		new Thread(new ServerListener(prop.port,messagesFromPeers)).start();
	}
}