import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

public class Communication {
	Server server;
	Socket requestConn[];           //socket connect to the server      //Can be merged in neighbour     //socket
    ObjectOutputStream out[];         //stream write to the socket		//same							//Socket
//    ObjectInputStream in[];  
	
	
	public Communication(Property prop, List<Peer> allPeers){
		server = new Server(prop);   //Listens for Connections
		
		requestConn = new Socket[prop.indexMap.size()];
		out = new ObjectOutputStream[prop.indexMap.size()];
//		in = new ObjectInputStream[prop.indexMap.size()];
		int connectedPeers=1;
		while(connectedPeers<=prop.indexMap.size()){
			int j=1;
			for(int i=0;i<prop.indexMap.size();i++){
				if(i!=prop.getOwnIndex()&& ! allPeers.get(i).state.hasMadeConnection){
					System.out.println("Connecting to peer:"+allPeers.get(i).get_hostName()+" on port "+ allPeers.get(i).get_port());
				}

				try{
					requestConn[i]=new Socket(allPeers.get(i).get_hostName(), allPeers.get(i).get_port());
					if(requestConn[i].isConnected()){
						System.out.println("Connected to Peer"+j++);
						connectedPeers++;
						allPeers.get(i).state.hasMadeConnection=true;
					}

				}catch(ConnectException e){
					allPeers.get(i).state.hasConnectionRefused=true;
				}catch(IOException e){
					allPeers.get(i).state.hasConnectionRefused=true;
				}

			}
		}

		if(connectedPeers!=connectedPeers){
			try{
				System.out.println("Wating to reconnect to peers");
				Thread.sleep(10000);
			}catch(Exception e) {
				System.out.println("Client thread stopped");
			}
		}


	}
}
