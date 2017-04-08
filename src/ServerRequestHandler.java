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


		private Message message;    //message received from the client
		private Socket connection;
		private ObjectInputStream in;   //stream read from the socket
		private int clientId;     //The index number of the client
        private List<Message>receivedMessages;
		ReentrantLock lock;
		private static final HashMap<Integer, MessageType> intToMessageType;

		static {
			intToMessageType = new HashMap<Integer, MessageType>();
			for (MessageType type : MessageType.values()) {
				intToMessageType.put(type.getValue(), type);
			}
		}


		public ServerRequestHandler(Socket connection, int clientId,List<Message>receivedMessages) {
			this.connection = connection;
			this.clientId = clientId;
			this.receivedMessages=receivedMessages;
			this.lock=new ReentrantLock();

		}

		public void run() {
			try{
				//initialize Input and Output streams
				in = new ObjectInputStream(connection.getInputStream());

				try{

					while(true)
					{
						//receive the message sent from the client
						byte[] temp = (byte[])in.readObject();
                        /*for (int i = 0; i < temp.length; i++) {
                            System.out.println(i + " : " + temp[i]);
                        }*/
						message = convertToMessage(temp, clientId);
						//System.out.println("Placing message in array: " + message.toString());


						try{
							lock.lock();
							receivedMessages.add(message);

						}catch(Exception e){
							System.out.println("failed to add message to the messageList");
						}finally {
							lock.unlock();
						}

						//show the message to the user
						//System.out.println("Received message: " + message.toString() + " from client " + clientId);
                        /*
                         *
                         * TODO: Add this to the logger somehow
                         *
                         */
					}
				}
				catch(ClassNotFoundException e){
					e.printStackTrace();
					System.out.println("Data received in unknown format");
				}
			}
			catch(IOException e){
				e.printStackTrace();
				System.out.println("Disconnect with Client " + clientId);
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
			//Check for handshake:

			if (data.length >= 18) {
				byte[] bytes = Arrays.copyOfRange(data, 0, 18);
				String header = new String(bytes, Charset.forName("UTF-8"));
				if (header.equals("P2PFILESHARINGPROJ")) {
					int newPeerID = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, 28, 32)).getInt(0);
					return new Message(newPeerID, MessageType.HANDSHAKE, null, clientID);
				}
			}

			//Converts the incoming bytes into the actual Message with its associated clientId
			int length = ByteBuffer.allocate(4).put(Arrays.copyOfRange(data, 0, 4)).getInt(0);
			byte[] payload = Arrays.copyOfRange(data, 5, data.length);



			return new Message(length, fromInt(data[4]), payload, clientID);
		}

		public static MessageType fromInt(int i) {
			MessageType type = intToMessageType.get(Integer.valueOf(i));
			return type;
		}

	}


