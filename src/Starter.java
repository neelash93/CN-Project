import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class PeerProcess{
	
	
	private HashMap<String,Thread>requestPool=new HashMap<>();

	Server server;
	PeerProcess(String ipAddress,String port,String peerName){
		server=new Server(ipAddress,port,peerName);
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
		PeerProcess peerProcess=new PeerProcess("localhost","8010","My server");
		peerProcess.createServer();
		peerProcess.createRequest("192.168.0.30", 8010);
	



	}



}

