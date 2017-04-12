import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TimerTask;


public class CurrentClient {

	int index;
	boolean allFilesReceived;
	int optimisticNeighbor;
	List<Peer> allPeers;
	List<Integer> preferredPeers;
	int allParts = 0;
	
	Property prop;
	public static Communication comm;
	public FileManager fileManager;
	public MessageBuilder messageBuilder;
	Log log;
	Scheduler scheduler;
	
	public CurrentClient(int index, ArrayList<Peer> peers) {
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
		fileManager = new FileManager(prop);
		log = new Log(prop.peerId);
		comm = new Communication(prop, allPeers);
		scheduler = new Scheduler();
		preferredPeers = new ArrayList<>(prop.prefferedNeighbours);
		messageBuilder = new MessageBuilder();
		
		process();
	}

	public void process() {
		try {
			comm.startServer(Integer.parseInt(prop.peerId), prop.hostName, prop.port);
			comm.initOPStreams();
			boolean firstTime = false;
			while (!allFilesReceived) {
				// setup connection
				setUpConnections();
				
				//setup scheduler
				if (!firstTime) {
					DeterminePreferredPeerTask determinePreferredPeerTask = new DeterminePreferredPeerTask(this);
					scheduler.schedule(determinePreferredPeerTask, prop.unchokingInterval);

					DetermineOptimisticPeerTask determineOptimisticPeerTask = new DetermineOptimisticPeerTask(this);
					scheduler.schedule(determineOptimisticPeerTask, prop.optUnchokingInterval);
					
					firstTime = true;
				}
				
				processReceivedMessages();
			}
		}

		finally {
			comm.closeConnections(); // to close all connections
		}
		
		writeFile();
		System.exit(0);
	}

	public void processReceivedMessages() {
		int peerIndex = -1;
		List<Message> recievedMessages = comm.getRecievedMessages();
		List <Message> processedMessages =new ArrayList<Message>();
		
		synchronized(recievedMessages) {
			for(Message msg : recievedMessages) {
				peerIndex=comm.connectionOrderMap.getOrDefault(msg.getClientId(),-1);
				
				if(msg.getType()!=MessageType.HANDSHAKE){
					if(peerIndex == -1) continue;
					peerIndex = prop.getIndex(peerIndex);
				}
				
				if(peerIndex!=-1 ) {
					if(msg.getType()!=MessageType.BITFIELD && !allPeers.get(peerIndex).state.hasBitfieldReceived && allPeers.get(peerIndex).state.hasHandshakeReceived) {
						continue;
					}
				}

				processMessage(msg,processedMessages, peerIndex);
			}

			for(Message msg : processedMessages){
				recievedMessages.remove(msg);
			}
		}
	}

	public void processMessage(Message msg,List<Message>processedMessages, int peerIndex) {

		if(msg.getType() == MessageType.HANDSHAKE) {
			comm.connectionOrderMap.put(msg.getClientId(),msg.getLength());
			peerIndex=prop.getIndex(msg.getLength());
			allPeers.get(peerIndex).state.hasHandshakeReceived=true;
		} 
		else if(msg.getType() == MessageType.BITFIELD) {
			allPeers.get(peerIndex).state.bitField=msg.getPayload();

			allPeers.get(peerIndex).state.hasBitfieldReceived=true;
			if(ifFilePartsNeeded(allPeers.get(peerIndex))) {
				sendInterested(peerIndex);
			} else {
				sendNotInterested(peerIndex);
			}
		} 
		else if(msg.getType() == MessageType.INTERESTED) {
			allPeers.get(peerIndex).state.interested=true;
			Log.addLog("Peer " +prop.peerId+ " recieved the interested message from peer "+allPeers.get(peerIndex).prop.peerId+ "." + '\n');
		} 
		else if(msg.getType() == MessageType.NOT_INTERESTED) {
			allPeers.get(peerIndex).state.interested=false;
			Log.addLog("Peer "+ prop.peerId+ " recieved the 'not interested' message from peer "+allPeers.get(peerIndex).prop.peerId+ "." + '\n');
		}
		else if(msg.getType() == MessageType.HAVE){
			int bitIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue();
			
			BigInteger bits = new BigInteger(allPeers.get(peerIndex).state.bitField);
			bits = bits.setBit(bitIndex);
			allPeers.get(peerIndex).state.bitField = bits.toByteArray();

			boolean peerGetsFile = checkHasFile(bits); 
		
			//update allPeers
			allPeers.get(peerIndex).prop.hasFile = peerGetsFile;

			//check if all peers received the file
			allFilesReceived = checkAllReceived();
			
			Log.addLog("Peer "+ prop.peerId+" received the 'have' message from peer "+allPeers.get(peerIndex).prop.peerId+" for the piece :"+bitIndex+ "." + '\n');
			BigInteger selfbits = new BigInteger(fileManager.bitField);
			if(!selfbits.testBit(bitIndex)) {
				sendInterested(peerIndex);
			}
		} 
		else if(msg.getType() == MessageType.PIECE) {
			//Update File Parts
			fileManager.fileParts[allPeers.get(peerIndex).state.lastRequestedPart] = msg.getPayload();
			allPeers.get(peerIndex).state.isWaitingForPiece = false;

			BigInteger bitsSelf = new BigInteger(fileManager.bitField);

			bitsSelf = bitsSelf.setBit(allPeers.get(peerIndex).state.lastRequestedPart);

			allPeers.get(peerIndex).prop.partsRecieved += prop.pieceSize;

			fileManager.bitField = bitsSelf.toByteArray();

			Log.addLog("Peer " + prop.peerId + " has downloaded the piece " + allPeers.get(peerIndex).state.lastRequestedPart
					+ " from " + allPeers.get(peerIndex).get_peerId() + ". Now the number of pieces it has is " + (++allParts) + "." + '\n');

			//Update peerFileInfo
			boolean peerGetsFile = checkHasFile(bitsSelf);
			allPeers.get(prop.getOwnIndex()).prop.hasFile = peerGetsFile; 
			
			if(peerGetsFile) 
			    Log.addLog("Peer " + prop.peerId + " has downloaded the complete file." + '\n');

			prop.hasFile = peerGetsFile;
			
			//Send have message to others
			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i != index) // or prop.getOwnIndex()
					sendHave(i,allPeers.get(peerIndex).state.lastRequestedPart);
			}

			allPeers.get(peerIndex).state.lastRequestedPart = -1;

			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i == index) // or prop.getOwnIndex()
					continue;
				boolean hasInterest = false;
				if(allPeers.get(i).state.bitField != null){
					hasInterest = ifFilePartsNeeded(allPeers.get(i));
				}
				if(hasInterest == false)
					sendNotInterested(i);
			}

			allFilesReceived = checkAllReceived();
		}
		else if(msg.getType() == MessageType.REQUEST){
			ByteBuffer buffer = ByteBuffer.wrap(msg.getPayload());
			int pieceNumber = buffer.getInt();

			int partIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue(); 
			sendFileParts(peerIndex, partIndex);

		}
		else if(msg.getType() == MessageType.CHOKE){
			allPeers.get(peerIndex).state.choked = true;
			Log.addLog("Peer " + allPeers.get(index).prop.peerId + " is choked by  " + allPeers.get(peerIndex).prop.peerId+ "." + '\n');

		}
		else if(msg.getType() == MessageType.UNCHOKE){
			allPeers.get(peerIndex).state.choked = false;
			Log.addLog("Peer " + allPeers.get(index).prop.peerId + " is unchoked by  " + allPeers.get(peerIndex).prop.peerId+ "." + '\n');

		}
		else Log.addLog("Wrong Message depicted " + msg.getType());

		processedMessages.add(msg);
	}

	public void setUpConnections() {

		for (int i = 0; i < allPeers.size(); i++) {
			if (i != index) {
				if (!allPeers.get(i).state.hasHandshakeSent && allPeers.get(i).state.hasMadeConnection) {
					sendHandShake(i, prop.peerId);
					allPeers.get(i).state.hasHandshakeSent = true;
					Log.addLog("Peer " + prop.peerId + " is connected from Peer " + allPeers.get(i).prop.peerId+ "." + '\n');
				}

				if (!allPeers.get(i).state.hasBitfieldSent && allPeers.get(i).state.hasMadeConnection
						&& allPeers.get(i).state.hasHandshakeReceived) {
					sendBitfield(i);
					allPeers.get(i).state.hasBitfieldSent = true;
				}

				// Request a piece at random
				if (allPeers.get(i).state.hasBitfieldReceived && allPeers.get(i).state.hasHandshakeReceived && allPeers.get(i).state.choked == false
						&& allPeers.get(i).state.isWaitingForPiece == false) {
					allPeers.get(i).state.isWaitingForPiece = true;
					int latestRequestedPiece = generateRandomPart(allPeers.get(i));
					allPeers.get(i).state.lastRequestedPart = latestRequestedPiece; //Change name to LatestRequestedPiece

					if (latestRequestedPiece != -1 && latestRequestedPiece < prop.numberOfPieces) {
						sendRequest(i, latestRequestedPiece);
					}
				}
			}
		}
	}

	public void sendMessage(byte[] msg, int socketIndex) {
		try {
			comm.out[socketIndex].writeObject(msg);
			comm.out[socketIndex].flush();
		} catch(IOException e){
			System.err.println("Sending message failed");
			e.printStackTrace();
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

	public void sendHandShake(int index, String peerId) {
		byte[] handshake = messageBuilder.createHandshake(Integer.parseInt(peerId));
		sendMessage(handshake, index);
		Log.addLog("Peer " + peerId + " makes a connection to Peer " + allPeers.get(index).prop.peerId+ "." + '\n');
	}

	public void sendUnchoke(int index) {
		byte[] message = messageBuilder.createUnchoke(index);
		sendMessage(message, index);
	}

	public void sendChoke(int index) {
		byte[] message = messageBuilder.createChoke(index);
		sendMessage(message, index);
	}

	public void sendInterested(int index){
		byte[] message = messageBuilder.createInterested(index);
		sendMessage(message, index);

	}

	public void sendFileParts(int index,int partNumber){
		byte[] message = messageBuilder.createFilePiece(prop.pieceSize,fileManager.fileParts,partNumber);
		sendMessage(message, index);
	}

	public void sendNotInterested(int index){
		byte[] message = messageBuilder.createNotInterested(index);
		sendMessage(message, index);
	}

	public void sendHave(int index, int pieceNumber) {
		byte[] message = messageBuilder.createHave(index, pieceNumber);
		sendMessage(message, index);
	}

	public void writeFile()  {
		try {
			FileOutputStream os = new FileOutputStream("peer_" + prop.peerId + "//" + prop.fileName);
			for (int i = 0; i < prop.numberOfPieces; i++) {
					os.write(fileManager.fileParts[i]);
			}
			os.close();
		} catch (Exception e) {
			System.exit(0);
		}
	}

	public boolean ifFilePartsNeeded(Peer peer) {

		BitSet incomingbits = new BitSet();
		for (int i = 0; i < peer.state.bitField.length * 8; i++) {
			if ((peer.state.bitField[peer.state.bitField.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				incomingbits.set(i);
			}
		}
		BitSet selfbits = new BitSet();
		for (int i = 0; i < fileManager.bitField.length * 8; i++) {
			if ((fileManager.bitField[fileManager.bitField.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				selfbits.set(i);
			}
		}
		
		// to get the interesting incoming set bits 
		incomingbits.andNot(selfbits);

		// converting incomingbits bitset to long value
		long value = 0L;
		for (int i = 0; i < incomingbits.length(); ++i) {
			value += incomingbits.get(i) ? (1L << i) : 0L;
		}

		if (value > 0) {
			return true;
		}

		return false;
	}

	private int generateRandomPart(Peer peer) {

		BitSet incomingbits = new BitSet();
		for (int i = 0; i < peer.state.bitField.length * 8; i++) {
			if ((peer.state.bitField[peer.state.bitField.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				incomingbits.set(i);
			}
		}

		BitSet selfbits = new BitSet();
		for (int i = 0; i < fileManager.bitField.length * 8; i++) {
			if ((fileManager.bitField[fileManager.bitField.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
				selfbits.set(i);
			}
		}

		// to get the interesting incoming set bits
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
			int a = (int) (Math.random() * (j-1));
			return val[a];
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
		boolean result = prop.hasFile || allPeers.get(index).prop.hasFile;
		for(int i=0;i<allPeers.size();i++){
			if(i != index)
				result = result && allPeers.get(i).prop.hasFile;
		}
		return result;
	}
}
