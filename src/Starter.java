import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class PeerProcess{
	private int sPort = 8010;
	private HashMap<String,Thread> connectionPool=new HashMap<>();
	private HashMap<String,Thread> requestPool=new HashMap<>();

	public void createRequest(String peerIp,int peerPort)throws IOException{
		Client client = new Client(peerIp,peerPort);
		Thread request=new Thread(client);
		requestPool.put(peerIp, request);
		request.start();
	}
	
	public void createServer(String peerIp,String peerName) throws IOException{
		ServerSocket listener = new ServerSocket(sPort);
		try {
			while(true) {
				Thread requestHandlerThread=new Thread(new Server(listener.accept(),peerName));
				requestHandlerThread.start();
				connectionPool.put(peerIp, requestHandlerThread);
				System.out.println("Client "  + peerIp + " is connected!");
			}
		} finally {
			listener.close();
		}
	}
}


public class Starter {
	public static void main(String args[]) throws IOException{
		int  sPort=8000;
		PeerProcess peerProcess=new PeerProcess();
		peerProcess.createServer("localhost","My server 2");
		peerProcess.createRequest("192.168.0.17", 8010);
	}
}

