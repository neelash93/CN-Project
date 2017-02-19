import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class PeerProcess{
	
	
	private HashMap<String,Thread>requestPool=new HashMap<>();

	Server server;
	PeerProcess(String port,String ipAddress,String peerName){
		server=new Server(port,ipAddress,peerName);
	}
	
	public void createServer(){
		Thread serverThread=new Thread(server);
		serverThread.start();
	} 
	public void createRequest(String peerIp,int peerPort)throws IOException{
		Client client = new Client(peerIp,peerPort);
		Thread request=new Thread(client);
		request.start();
		requestPool.put(peerIp, request);
	}
	
	

}

public class Starter {



	public static void main(String args[]) throws IOException{
		int  sPort=8000;
		PeerProcess peerProcess=new PeerProcess("localhost","My server","8010");
		peerProcess.createRequest("192.168.0.30", 8010);
	



	}



}

