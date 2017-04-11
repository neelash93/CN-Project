import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class Communication {
	HashMap<Integer,Integer> connectionOrderMap;
	Server server;
	Socket requestConn[];           //socket connect to the server      //Can be merged in neighbour     //socket
    ObjectOutputStream out[];        //stream write to the socket		//same							//Socket
    List<Peer> allPeers;
    Property prop;
    static ServerListener1 serverListener;
    
    
    //    ObjectInputStream in[];
	
	public Communication(Property prop, List<Peer> allPeers){
//		server = new Server(prop);   //Listens for Connections
		connectionOrderMap=new HashMap<>();
		requestConn = new Socket[prop.indexMap.size()];
		out = new ObjectOutputStream[prop.indexMap.size()];
		this.allPeers = allPeers;
		this.prop = prop;
//		in = new ObjectInputStream[prop.indexMap.size()];
		
		
		
		
//		int connectedPeers=1;
//		while(connectedPeers<=prop.indexMap.size()){
//			int j=1;
//			for(int i=0;i<prop.indexMap.size();i++){
////				System.out.println("Own Indexw);
//				if(i!=prop.getOwnIndex()&& ! allPeers.get(i).state.hasMadeConnection){
//					System.out.println("Connecting to peer:"+allPeers.get(i).get_hostName()+" on port "+ allPeers.get(i).get_port());
//				
//
//				try{
//					requestConn[i]=new Socket(allPeers.get(i).get_hostName(), allPeers.get(i).get_port());
//					if(requestConn[i].isConnected()){
//						System.out.println("Connected to Peer "+i+"  "+(j++));
//						connectedPeers++;
//						allPeers.get(i).state.hasMadeConnection=true;
//					}
//
//				}catch(ConnectException e){
//					allPeers.get(i).state.hasConnectionRefused=true;
//				}catch(IOException e){
//					allPeers.get(i).state.hasConnectionRefused=true;
//				}
//				
//				}
//
//			}
//		}
//
//		if(connectedPeers!=prop.indexMap.size()){
//			try{
//				System.out.println("Wating to reconnect to peers");
//				Thread.sleep(10000);
//			}catch(Exception e) {
//				System.out.println("Client thread stopped");
//			}
//		}


	}
	
	public void startServer(int peerId, String hostName, int port){
		serverListener = new ServerListener1(peerId, hostName, port);
		
        int connectionsLeft = allPeers.size()-1;
            while (connectionsLeft > 0) {
                System.out.println();

                for (int i = 0; i < allPeers.size(); i++) {
                    if (i != prop.getOwnIndex() && !allPeers.get(i).state.hasMadeConnection) {
                        System.out.println("Attempting to connect to " + allPeers.get(i).prop.hostName +
                            " on port " + allPeers.get(i).prop.port);

                        try {
                            //create a socket to connect to the server
                            requestConn[i] = new Socket(allPeers.get(i).prop.hostName, allPeers.get(i).prop.port);

                            if (requestConn[i].isConnected()) {
                            //logger.info("Connected to " + allPeers.get(i).hostName +
                            //    " in port " + allPeers.get(i).portNumber + '\n');

                            connectionsLeft--;
                            allPeers.get(i).state.hasMadeConnection = true;
                            //madeConnection[i] = true;
                            }
                        } catch (ConnectException e) {
                            allPeers.get(i).state.hasConnectionRefused = true;
                        } catch (IOException e) {
                            allPeers.get(i).state.hasConnectionRefused = true;
                        }
                    }
                }
                System.out.println();
                for (int i = 0; i < allPeers.size(); i++) {
                    if (allPeers.get(i).state.hasConnectionRefused) {
                        //System.out.println("Connection refused for " + allPeers.get(i).hostName +
                        //    " on port " + allPeers.get(i).portNumber + '\n');

                    }
                }
                if (connectionsLeft > 0) {
                //Wait four seconds before attempting to reconnect
                    try {
                    System.out.println("Waiting to reconnect..");
                    Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
            }
            
		}

//		if(connectedPeers!=prop.indexMap.size()){
//			try{
//				System.out.println("Wating to reconnect to peers");
//				Thread.sleep(10000);
//			}catch(Exception e) {
//				System.out.println("Client thread stopped");
//			}
//		}
//		try{
//		for (int i = 0; i < allPeers.size(); i++) {
//            if (prop.getOwnIndex() != i && allPeers.get(i).state.hasMadeConnection) {
//                out[i] = new ObjectOutputStream(requestConn[i].getOutputStream());
//                out[i].flush();
//            }
//        }
//		}
//		catch(IOException e){
//			System.out.println(e);
//		}
//	}
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
//		return server.messagesFromPeers;
		return serverListener.receivedMessages;
	}
}
