import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.locks.ReentrantLock;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

 class ServerRequestHandler implements Runnable {


	private Message message;    //Received Message
	private Socket connection;
	private ObjectInputStream in;
	private int receiveOrderPeer;
    private List<Message>messagesFromPeers;
	ReentrantLock lock;
	private static final HashMap<Integer, MessageType> intToMessageType;

	static {
		intToMessageType = new HashMap<Integer, MessageType>();
		for (MessageType type : MessageType.values()) {
			intToMessageType.put(type.getValue(), type);
		}
	}

	public ServerRequestHandler(Socket connection, int clientId, List<Message>receivedMessages) {
		this.connection = connection;
		this.receiveOrderPeer = clientId;
		this.messagesFromPeers=receivedMessages;
		this.lock=new ReentrantLock();
	}

	public void run() {
		try{
			in = new ObjectInputStream(connection.getInputStream());
			try{

				while(true)
				{
					//Retrieve msgs sent to this peer
					try{
					byte[] temp = (byte[])in.readObject();
					message = convertToMessage(temp, receiveOrderPeer);
					}
					catch(EOFException e){
//							break;
					}try{
						lock.lock();
						messagesFromPeers.add(message);

					}catch(Exception e){
						System.out.println("Message not added to Received List");
					}finally {
						lock.unlock();
					}
				}
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
				System.out.println("Unknown Data received");
			}
		}
		catch(IOException e){
			e.printStackTrace();
			System.out.println("Client " + receiveOrderPeer+" got Disconnected");
		}
		finally{
			try{
				in.close();
				connection.close();
			}
			catch(IOException e){
				System.out.println(e);
			}
		}
	}


	private Message convertToMessage(byte[] data, int clientID) {
		//If Handshake:
		if (data.length >= 18) {
			byte[] bytes = Arrays.copyOfRange(data, 0, 18);
			String header = new String(bytes, Charset.forName("UTF-8"));
			if (header.equals("P2PFILESHARINGPROJ")) {
				int newPeerID = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, 28, 32)).getInt(0);
				return new Message(newPeerID, MessageType.HANDSHAKE, null, clientID);
			}
		}
		//otherwise
		int length = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, 0, 4)).getInt(0);
		byte[] payload = Arrays.copyOfRange(data, 5, data.length);
		return new Message(length, fromInt(data[4]), payload, clientID);
	}

	public static MessageType fromInt(int i) {
		MessageType type = intToMessageType.get(Integer.valueOf(i));
		return type;
	}
	
}