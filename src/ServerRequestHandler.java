import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ServerRequestHandler implements Runnable{
	private String message;    //message received from the client
	private String MESSAGE;    //uppercase message send to the client
	private Socket connection;
	private ObjectInputStream in;	//stream read from the socket
	private ObjectOutputStream out;    //stream write to the socket
	private String peerName;		//The index number of the client

	public ServerRequestHandler(Socket connection, String peerName) {
		this.connection = connection;
		this.peerName = peerName;
	}
	
	

	public void run() {
		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{	System.out.println("fgdfgdfgd");
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + peerName);
					//Capitalize all letters in the message
					MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					sendMessage(MESSAGE);
				}
			}
			catch(ClassNotFoundException classnot){
				System.err.println("Data received in unknown format");
			}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + peerName);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + peerName);
			}
		}
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg + " to Client " + peerName);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

}
