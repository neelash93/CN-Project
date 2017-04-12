import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class Communication {
	HashMap<Integer,Integer> connectionOrderMap;
	Socket requestConn[];           //Socket for all Peers
    ObjectOutputStream out[];        //for outgoing Connections
    List<Peer> allPeers;
    Property prop;
    static Server serverListener;
    
	public Communication(Property prop, List<Peer> allPeers){
		connectionOrderMap=new HashMap<>();
		requestConn = new Socket[prop.indexMap.size()];
		out = new ObjectOutputStream[prop.indexMap.size()];
		this.allPeers = allPeers;
		this.prop = prop;
	}
	
	public void startServer(int peerId, String hostName, int port){
		serverListener = new Server(peerId, hostName, port);
		
        int allConnections = 0;
            while (allConnections < allPeers.size() - 1) {
                System.out.println();

                for (int i = 0; i < allPeers.size(); i++) {
                    if (i != prop.getOwnIndex() && !allPeers.get(i).state.hasMadeConnection) {
                        System.out.println("Attempting to connect to " + allPeers.get(i).prop.hostName +
                            " on port " + allPeers.get(i).prop.port);

                        try {
                            //create a socket to connect to all other Peers
                            requestConn[i] = new Socket(allPeers.get(i).prop.hostName, allPeers.get(i).prop.port);

                            if (requestConn[i].isConnected()) {
                            Log.addLog("Connected to " + allPeers.get(i).prop.hostName +" in port " + allPeers.get(i).prop.port + '\n');

                            allConnections++;
                            allPeers.get(i).state.hasMadeConnection = true;
                            }
                        } catch (Exception e) {
                            allPeers.get(i).state.hasConnectionRefused = true;
                        }
                    }
                }
          
                if (allConnections < allPeers.size() - 1) {
                    try {
                    Thread.sleep(1500);
                    } catch (Exception e) {
                    	System.out.println(e);
                    }
                }
          }
            
	}

	public void initOPStreams(){
		try{
			for (int i = 0; i < allPeers.size(); i++) {
	            if (prop.getOwnIndex() != i && allPeers.get(i).state.hasMadeConnection) {
	                out[i] = new ObjectOutputStream(requestConn[i].getOutputStream());
	                out[i].flush();
	            }
	        }
			}
			catch(IOException e){
				System.out.println(e);
			}
	}
	
	
	public void closeConnections(){
		try {
            for (int i = 0; i < allPeers.size(); i++) {
                if (i != prop.getOwnIndex()) {
                    out[i].close();
                    requestConn[i].close();
                }
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
	}

	public List<Message> getRecievedMessages(){
		return serverListener.messagesFromPeers;
	}
}
