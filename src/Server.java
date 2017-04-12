	import java.net.*;
	import java.io.*;
	import java.nio.*;
	import java.nio.channels.*;
	import java.nio.charset.Charset;
	import java.util.*;

	public class Server {
		//List where messages are received (synchronized)
	    public List<Message> messagesFromPeers;

	    private int peerId;
	    private String hostName;
	    private int port; //The listener will be listening on this port number
	    
	    public Server(int peerId, String hostName, int portNumber) {
	        this.peerId = peerId;
	        this.hostName = hostName;
	        this.port = portNumber;

	        messagesFromPeers = Collections.synchronizedList(new ArrayList<Message>());
	        new Listener().start();
	    }

	    private class Listener extends Thread {
	    	public void run() {
	            System.out.println("Server Running. Listening for Incoming Connections\n");
	            int receivePeerOrder = 0;
	            ServerSocket listener = null;
	            try {
	                while (true) {
	                    try {
	                        listener = new ServerSocket(port);
	                        new Thread(new ServerRequestHandler(listener.accept(), receivePeerOrder , messagesFromPeers)).start();
	                        receivePeerOrder++;
	                    } catch (SocketException e) {
	                        Thread.sleep(50);
	                    }
	                    finally {
	                        listener.close();
	                    }
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
}
