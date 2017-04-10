import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TimerTask;


public class CurrentClient {

	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public FileManager fileManager;
	boolean allFilesReceived;
	int optimisticNeighbor;
	public MessageBuilder messageBuilder;
	Log l;
	Scheduler scheduler;
	List<Integer> preferredPeers;

	public CurrentClient(int index, ArrayList<Peer> peers) {
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
		l = new Log(prop.peerId);
		fileManager = new FileManager(prop);
		comm = new Communication(prop, allPeers);
		scheduler = new Scheduler();
		preferredPeers = new ArrayList<>();
	}

	public void process() {
		int iteration = 0;
		while (!allFilesReceived) {
			setUpConnections();
			//setup timer
			if (iteration++ == 1) {

		if (iteration++ == 1) {
			DeterminePreferredNeighborTask determinePreferredNeighborTask = new DeterminePreferredNeighborTask(this);
			scheduler.schedule(determinePreferredNeighborTask, prop.unchokingInterval);

			DetermineOptimisticNeighborTask determineOptimisticNeighborTask = new DetermineOptimisticNeighborTask(this);
			scheduler.schedule(determineOptimisticNeighborTask, prop.optUnchokingInterval);
		}


			/*
			 * timer1.scheduleAtFixedRate(new TimerTask() {
			 *
			 * @Override public void run(){ determinePreferredNeighbors(); }
			 * },0, peerProcess.unchokingInterval * 1000);
			 *
			 * timer2.scheduleAtFixedRate(new TimerTask() {
			 *
			 * @Override public void run(){ int temp = optimisticNeighbor;
			 * optimisticNeighbor = determineOptimisticNeighbor(); if (temp !=
			 * optimisticNeighbor) logger.info("Peer " + peerId +
			 * " has the optimistically unchoked neighbor " + optimisticNeighbor
			 * + '\n'); } },0, peerProcess.optimisticUnchokingInterval * 1000);
			 */

			}


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

				if(peerIndex!=-1) {
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
		}
		else if(msg.getType()==MessageType.BITFIELD){
			allPeers.get(peerIndex).state.bitmap=msg.getPayload();
			allPeers.get(peerIndex).state.hasBitfieldReceived=true;
			if(checkIfNeedPieces(allPeers.get(peerIndex))){
//				sendInterested(peerIndex);
			}else{
//				sendNotInterested(peerIndex);
			}
		}
		else if(msg.getType()==MessageType.INTERESTED){
			allPeers.get(peerIndex).state.interested=true;
		}
		else if(msg.getType()==MessageType.NOT_INTERESTED){
			allPeers.get(peerIndex).state.interested=false;
		}
		else if(msg.getType()==MessageType.HAVE){
//			ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.payload);
//            BigInteger tempField = new BigInteger(neighbors[messageIndex].bitmap);
//            int thisIndex = buffer.getInt();
//            tempField = tempField.setBit(thisIndex); //update with sent 'have' index
//            neighbors[messageIndex].bitmap = tempField.toByteArray();
			
			 int bitIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue();
			 BigInteger bits = new BigInteger(allPeers.get(peerIndex).state.bitmap);
			 bits.setBit(bitIndex);
			 
			 boolean peerGetsFile = checkHasFile(bits); //Seperated into function as its used in other places as well.
			 
			 
			 //Should be done without BigInteger
//			 for(int i=0;i<prop.numberOfPieces;i++){
//				 if(!bits.testBit(i)){
//					 peerGetsFile = false;
//					 break;
//				 }
//			 }
			 
			 //update allPeers
			 allPeers.get(peerIndex).prop.hasFile = peerGetsFile;
			 
			 //check if everyone got File
			 allFilesReceived = checkAllReceived();
//			 for(Peer p : allPeers){
//				 allFilesReceived = allFilesReceived & p.prop.hasFile;
//			 }
			 

		}
		else if(msg.getType() == MessageType.PIECE){
			//Update FileParts
			fileManager.fileParts[allPeers.get(peerIndex).state.pieceNumber] = msg.getPayload();
			BigInteger bitsSelf = new BigInteger(fileManager.bitField);
			bitsSelf.setBit(allPeers.get(peerIndex).state.pieceNumber);
			allPeers.get(peerIndex).prop.partsRecieved += prop.pieceSize;
			allPeers.get(peerIndex).state.isWaitingForPiece = false;
			
			//Update peerFileInfo
			boolean peerGetsFile = checkHasFile(bitsSelf);
			allPeers.get(prop.getOwnIndex()).prop.hasFile = peerGetsFile; //Could also do prop.hasFile = ... But could conflict if same prop object is not passed to CurrClient and allPeers.get(ownIndex)
			
			
			//Send have msg to others
			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i == index) // or prop.getOwnIndex()
					continue;
				//sendHave(i,allPeers.get(peerIndex).state.pieceNumber);
			}
			allPeers.get(peerIndex).state.pieceNumber = -1;
			
			
			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i == index) // or prop.getOwnIndex()
					continue;
				boolean hasInterest;
				if(allPeers.get(i).state.bitmap != null){
					hasInterest = checkIfNeedPieces(allPeers.get(i));
				}
				
				//if(hasInterest == false)
					//sendNotInterested(i);
			}
			
			allFilesReceived = checkAllReceived();
			
			
			
		}
		else if(msg.getType() == MessageType.REQUEST){
//			ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.payload);
//            //I don't think we need an if statement here, they shouldnt be sending
//            //If they are choked
//            //if (!neighbors[messageIndex].choked)    //Uncomment to run legit m,m.
//            int pieceNumber = buffer.getInt();
//            sendFilePiece(messageIndex, pieceNumber);
            
			int partIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue();  //Alternative for above code;
//			sendFileParts(peerIndex, partIndex);
            
		}
		else if(msg.getType() == MessageType.CHOKE){
			allPeers.get(peerIndex).state.choked = true;
		}
		else if(msg.getType() == MessageType.UNCHOKE){
			allPeers.get(peerIndex).state.choked = false;
		}
		else l.log("Illegal Message Type Found : " + msg.getType());
		
	}

	public void setUpConnections() {

		for (int i = 0; i < allPeers.size(); i++) {
			if (i != index) {
				if (!allPeers.get(i).state.hasHandshakeSent && allPeers.get(i).state.hasMadeConnection) {
					sendHandShake(i, prop.peerId);
					allPeers.get(i).state.hasHandshakeSent = true;
					System.out.println(
							"Peer " + prop.peerId + " is connected from Peer " + allPeers.get(i).prop.peerId + '\n');
					// Logger
				}
			}

			// if we havent sent the bitfield and we are connected and we have
			// received the handshake:
			if (!allPeers.get(i).state.hasBitfieldSent && allPeers.get(i).state.hasMadeConnection
					&& allPeers.get(i).state.hasHandshakeReceived) {
				// Send the bitfield
				sendBitfield(i);
				allPeers.get(i).state.hasBitfieldSent = true;
				System.out.println("Sent bitfield to host " + allPeers.get(i).prop.hostName + "on port "
						+ allPeers.get(i).prop.port + '\n');
				// logger.info("Sent bitfield to host " + neighbors[i].hostName
				// + " on port " + neighbors[i].portNumber + '\n');
			}

			// Request a piece at random if we are able to:
			if (allPeers.get(i).state.hasBitfieldReceived && allPeers.get(i).state.choked == false
					&& allPeers.get(i).state.isWaitingForPiece == false) {

				allPeers.get(i).state.isWaitingForPiece = true;
				int requestedPieceNumber = getRandomPiece(allPeers.get(i));
				allPeers.get(i).state.pieceNumber = requestedPieceNumber; //Change name to LatestRequestedPiece

				// Call the method to send the request:
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

	// send a message to the output stream
	public void sendMessage(byte[] msg, int socketIndex) {
		try {
			// stream write the message
			comm.out[socketIndex].writeObject(msg);
			comm.out[socketIndex].flush();
		} catch(IOException e){

			System.err.println("Error sending message.");
			e.printStackTrace();
		}
	}

	public void sendHandShake(int index, String peerId) {
		byte[] handshake = messageBuilder.createHandshake(Integer.parseInt(peerId));
		sendMessage(handshake, index);
	}

	public void sendUnchoke(int index) {
		// Send a choke message to non-preferred peers
		byte[] message = messageBuilder.createUnchoke(index);
		sendMessage(message, index);
	}

	 public void sendChoke(int index) {
	      byte[] message = messageBuilder.createChoke(index);
			sendMessage(message, index);
	 }
	 
	public boolean checkIfNeedPieces(Peer peer) {

		// BigInteger incomingBitfieldInt = new BigInteger(neighbor.bitmap);
		// BigInteger selfBitfieldInt = new BigInteger(bitfield);

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

		long value = 0L;
		for (int i = 0; i < incomingbits.length(); ++i) {
			value += incomingbits.get(i) ? (1L << i) : 0L;
		}

		if (value > 0) {
			return true;
		}

		return false;
		// Check the bits of the bitfield to see if the incoming bitfield has
		// any bits that we don't
		// Example:
		// 00000010 (own bitfield)
		// 00001111 (incoming bitfield)
		// AND =
		// 00000010
		// NOT =
		// 11111101
		// 00001111 (Now we And it with the incoming bitfield again)
		// AND =
		// 00001101 We should be left with the bits that we dont have
		// If it is greater than 0 then we need pieces from the sender:
		// if
		// (incomingBitfieldInt.and(selfBitfieldInt.and(incomingBitfieldInt).not()).doubleValue()
		// > 0) {
		// return true;
		// }
		// return false;
	}

	private int getRandomPiece(Peer peer) {
		// BigInteger incomingBitfieldInt = new BigInteger(neighbor.bitmap);
		// BigInteger selfBitfieldInt = new BigInteger(bitfield);

		// BigInteger interestingBits =
		// incomingBitfieldInt.and(selfBitfieldInt.and(incomingBitfieldInt).not());

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
			if (incomingbits.get(i)) {
				val[j++] = i;
				exists = true;
			}
		}

		// Choose a random value from the available bits
		if (exists) {
			int a = (int) (Math.random() * j);
			return a;
		} else {
			return -1;
		}
	}
	
	public boolean checkHasFile(BigInteger bits){
		for(int i=0;i<prop.numberOfPieces;i++){
			if(!bits.testBit(i))
				return false;
		}
		return true;
	}
	
	public boolean checkAllReceived(){
		for(int i=0;i<allPeers.size();i++){
			if(!allPeers.get(i).prop.hasFile)
				return false;
		}
		return true;
	}

}
