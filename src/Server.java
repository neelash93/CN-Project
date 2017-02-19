import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

class Server implements Runnable {
	private String port;
	private String ipAddress;
	private String peerName;
	private HashMap<String,Thread> connectionPool=new HashMap<>();
	
		
		public Server(String ipAdress,String port ,String peerName) {
			// TODO Auto-generated constructor stub,
			this.port=port;
			this.ipAddress=ipAddress;
			this.peerName=peerName;
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				createServerRequestHandler();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void createServerRequestHandler() throws NumberFormatException, IOException{
			ServerSocket listener = new ServerSocket(Integer.parseInt(port));
			
			try {
				while(true) {
					Thread requestHandlerThread=new Thread(new ServerRequestHandler(listener.accept(),peerName));
					requestHandlerThread.start();
					connectionPool.put(ipAddress, requestHandlerThread);
					System.out.println("Client "  + ipAddress + " is connected!");
				}
			} finally {
				listener.close();
			}
			
		}
	 
	
}
