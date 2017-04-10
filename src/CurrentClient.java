import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class CurrentClient {

	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public FileManager fileManager;
	boolean allFilesReceived;
	public MessageBuilder messageBuilder;
	public CurrentClient(int index, ArrayList<Peer> peers){
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
		//		System.out.println("Index"+index);
		fileManager = new FileManager(prop);
		comm = new Communication(prop,allPeers);

	}

	public void process() {
		while(!allFilesReceived) {
			setUpConnections();
			//setup timer
			processReceivedMessages();







		}
	}

	public void processReceivedMessages(){
		int peerIndex = -1;

		List<Message> recievedMessages = comm.getRecievedMessages();
		List <Message> processedMessages =new ArrayList<Message>();
		synchronized(recievedMessages){
			for(Message msg:recievedMessages){
				peerIndex=comm.connectionOrderMap.getOrDefault(msg.getClientId(),-1);
//				if(peerIndex!=-1&& msg.getType()!=MessageType.HANDSHAKE){
//					continue;
//				}
//
//				if(msg.getType()!=MessageType.HANDSHAKE){
//					peerIndex = prop.getIndex(peerIndex);
//				}
//
				//Equivalent block
				if(msg.getType()!=MessageType.HANDSHAKE){
					if(peerIndex != -1) continue;
					peerIndex = prop.getIndex(peerIndex);
				}

				if(peerIndex!=-1){
					if(msg.getType()!=MessageType.BITFIELD&&!allPeers.get(peerIndex).state.hasBitfieldReceived&&allPeers.get(peerIndex).state.hasHandshakeReceived){
					   continue;
					}
				}

				processMessage(msg,processedMessages, peerIndex);
			}
		}
	}



	public void processMessage(Message msg,List<Message>processedMessges, int peerIndex){
		if(msg.getType()==MessageType.HANDSHAKE){
			comm.connectionOrderMap.put(msg.getClientId(),msg.getLength());
			peerIndex=prop.getIndex(msg.getLength());
			allPeers.get(peerIndex).state.hasHandshakeReceived=true;
		}else if(msg.getType()==MessageType.BITFIELD){
			allPeers.get(peerIndex).state.bitmap=msg.getPayload();
			allPeers.get(peerIndex).state.hasBitfieldReceived=true;
			if(checkifNeedPieces(allPeers.get(peerIndex))){
				sendInterested(peerIndex);
			}else{
				sendNotInterested(peerIndex);
			}
		}else if(msg.getType()==MessageType.INTERESTED){
			allPeers.get(peerIndex).state.interested=true;
		}else if(msg.getType()==MessageType.NOT_INTERESTED){
			allPeers.get(peerIndex).state.interested=false;
		}else if(msg.getType()==MessageType.HAVE){
			


		}


	}

	public void setUpConnections() {

		for(int i = 0; i < allPeers.size(); i++) {
			if(i != index) {
				if(!allPeers.get(i).state.hasHandshakeSent && allPeers.get(i).state.hasMadeConnection) {
					sendHandShake(allPeers.get(i).prop.peerId);
					allPeers.get(i).state.hasHandshakeSent = true;
					System.out.println("Peer "+ prop.peerId + " is connected from Peer "+ allPeers.get(i).prop.peerId + '\n');
					// Logger
				}
			}

			//if we havent sent the bitfield and we are connected and we have received the handshake:
			if (!allPeers.get(i).state.hasBitfieldSent && allPeers.get(i).state.hasMadeConnection && allPeers.get(i).state.hasHandshakeReceived) {
				//Send the bitfield
				sendBitfield(i);
				allPeers.get(i).state.hasBitfieldSent = true;
				System.out.println("Sent bitfield to host " + allPeers.get(i).prop.hostName + "on port " + allPeers.get(i).prop.port + '\n');
				//logger.info("Sent bitfield to host " + neighbors[i].hostName + " on port " + neighbors[i].portNumber + '\n');
			}

			//Request a piece at random if we are able to:
			if (allPeers.get(i).state.hasBitfieldReceived && 
					allPeers.get(i).state.choked == false &&
					allPeers.get(i).state.isWaitingForPiece == false) {

				allPeers.get(i).state.isWaitingForPiece = true;
				int requestedPieceNumber = getRandomPiece(allPeers.get(i));
				allPeers.get(i).state.pieceNumber = requestedPieceNumber;

				//Call the method to send the request:
				if (requestedPieceNumber != -1) {
					sendRequest(i, requestedPieceNumber);
				}
			}
		}

	}

	private void sendRequest(int index, int pieceNumber) {    
		byte[] pieceMessage = messageBuilder.createRequest(index, pieceNumber);
		sendMessage(pieceMessage, index);
	}

	private void sendBitfield(int index) {        
		byte[] bitfieldMessage = messageBuilder.createBitfield(fileManager.bitField);
		sendMessage(bitfieldMessage, index);
	}

	//send a message to the output stream
	public void sendMessage(byte[] msg, int socketIndex)
	{
		try {
			//stream write the message
			comm.out[socketIndex].writeObject(msg);
			comm.out[socketIndex].flush();
		}
		catch(IOException e){
			System.err.println("Error sending message.");
			e.printStackTrace();
		}
	}

	public void sendHandShake(String peerId) {
		byte[] handshake = messageBuilder.createHandshake(Integer.parseInt(peerId));
		sendMessage(handshake, Integer.parseInt(peerId));
	}

	private int getRandomPiece(Peer peer) {
		//BigInteger incomingBitfieldInt = new BigInteger(neighbor.bitmap);
		//BigInteger selfBitfieldInt = new BigInteger(bitfield);

		//BigInteger interestingBits = incomingBitfieldInt.and(selfBitfieldInt.and(incomingBitfieldInt).not());

		BitSet incomingbits = new BitSet();
		for (int i = 0; i < peer.state.bitmap.length * 8; i++) {
			if ((peer.state.bitmap[peer.state.bitmap.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				incomingbits.set(i);
			}
		}

		BitSet selfbits = new BitSet();
		for (int i = 0; i < fileManager.bitField.length * 8; i++) {
			if ((fileManager.bitField[fileManager.bitField.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				selfbits.set(i);
			}
		}

		incomingbits.andNot(selfbits);
		int j = 0;
		boolean exists = false;
		int[] val = new int[incomingbits.length()];


		
		for (int i = 0; i < incomingbits.length(); i++) {
			if(incomingbits.get(i)) {
				val[j++] = i; 
				exists = true;
			}
		}
    
		//Choose a random value from the available bits
		if (exists) {
			int a = (int) (Math.random() * j);
			return a;
		} else {
			return -1;
		}
	}
}
