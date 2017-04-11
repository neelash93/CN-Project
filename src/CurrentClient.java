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
	List<Peer> allPeers;
	Property prop;
//	public Communication comm;
	public static Communication comm1;
	public FileManager fileManager;
	boolean allFilesReceived;
	int optimisticNeighbor;
	public MessageBuilder messageBuilder;
	Log l;
	Scheduler scheduler;
	List<Integer> preferredPeers;
	static int counter_test = 0;
	int total=0;
	public CurrentClient(int index, ArrayList<Peer> peers) {
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
		l = new Log(prop.peerId);
		fileManager = new FileManager(prop);
		//Trying to use new Communication specifics everywhere instead of old comm object
//		comm = new Communication(prop, allPeers);
		comm1 = new Communication(prop, allPeers);
		
		scheduler = new Scheduler();
		preferredPeers = new ArrayList<>(prop.prefferedNeighbours);
		messageBuilder = new MessageBuilder();
		System.out.println("Reaching before Process");
		process();
	}

	public void process() {
		try{
			comm1.startServer(Integer.parseInt(prop.peerId), prop.hostName, prop.port);
			comm1.initOPStreams();
			int iteration = 0;
		while (!allFilesReceived) {
//			System.out.println("Reaches main while loop");
			setUpConnections();
			//setup timer

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

//			}
		
			processReceivedMessages();
			
		}
		}
//		catch (ConnectException e) {
//            Log.addLog("Connection refused. You need to initiate a server first.");
//        }
//        //catch ( ClassNotFoundException e ) {
//            //System.err.println("Class not found");
//        //}
//        catch(UnknownHostException unknownHost) {
//            Log.addLog("You are trying to connect to an unknown host!");
//        }
//        catch(IOException ioException) {
//            Log.addLog("IOException: See console for more details.");
//            ioException.printStackTrace();
//        }
        //catch (InterruptedException e) {
            //System.err.println("Interrupted thread execution.");
        //}
        finally {
            //Close connections
        	comm1.closeConnections();
        }
		System.out.println("DONE WITH EVERYTHING. Next step assemble File");
		assembleFilePieces();
		System.exit(0);
		}

	public void processReceivedMessages(){
		int peerIndex = -1;

//		List<Message> recievedMessages = comm.getRecievedMessages();
		List<Message> recievedMessages = comm1.getRecievedMessages();
//		if(!recievedMessages.isEmpty())
//		System.out.println("processRecieved Called, Msgs are : \n"+recievedMessages);
//		for(Message m : recievedMessages){
//			System.out.println(m.getType());
//		}
		List <Message> processedMessages =new ArrayList<Message>();
		synchronized(recievedMessages){
			for(Message msg:recievedMessages){
				System.out.println(msg.getType());
//				peerIndex=comm.connectionOrderMap.getOrDefault(msg.getClientId(),-1);
				peerIndex=comm1.connectionOrderMap.getOrDefault(msg.getClientId(),-1);
				
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
					if(peerIndex == -1) continue;
					peerIndex = prop.getIndex(peerIndex);
				}
				if(peerIndex!=-1 ) {
					if(msg.getType()!=MessageType.BITFIELD&&!allPeers.get(peerIndex).state.hasBitfieldReceived&&allPeers.get(peerIndex).state.hasHandshakeReceived){
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

	public void processMessage(Message msg,List<Message>processedMessages, int peerIndex){
		System.out.println("Message received from :"+peerIndex);
		System.out.println("Type of message"+msg.getType());
		if(msg.getType()==MessageType.HANDSHAKE){
//			comm.connectionOrderMap.put(msg.getClientId(),msg.getLength());
			comm1.connectionOrderMap.put(msg.getClientId(),msg.getLength());

			peerIndex=prop.getIndex(msg.getLength());
			allPeers.get(peerIndex).state.hasHandshakeReceived=true;
		}
		else if(msg.getType()==MessageType.BITFIELD){
			allPeers.get(peerIndex).state.bitmap=msg.getPayload();
			allPeers.get(peerIndex).state.hasBitfieldReceived=true;
			if(checkIfNeedPieces(allPeers.get(peerIndex))){
				sendInterested(peerIndex);
			}else{
				sendNotInterested(peerIndex);
			}
			Log.addLog("Recieved Bitfield from peer "+allPeers.get(peerIndex).prop.peerId+". Bitfield is : "+allPeers.get(peerIndex).state.bitmap);
		}
		else if(msg.getType()==MessageType.INTERESTED){
			allPeers.get(peerIndex).state.interested=true;
			Log.addLog("Recieved Interedted Message from peer "+allPeers.get(peerIndex).prop.peerId);
		}
		else if(msg.getType()==MessageType.NOT_INTERESTED){
			allPeers.get(peerIndex).state.interested=false;
			Log.addLog("Recieved UnInteredted Message from peer "+allPeers.get(peerIndex).prop.peerId);

		}
		else if(msg.getType()==MessageType.HAVE){
			ByteBuffer buffer = ByteBuffer.wrap(msg.getPayload());
//            BigInteger tempField = new BigInteger(neighbors[messageIndex].bitmap);
            int thisIndex = buffer.getInt();
//            tempField = tempField.setBit(thisIndex); //update with sent 'have' index
//            neighbors[messageIndex].bitmap = tempField.toByteArray();
			
			 int bitIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue();
			 
			 
			 BigInteger bits = new BigInteger(allPeers.get(peerIndex).state.bitmap);
			 bits = bits.setBit(bitIndex);
			 allPeers.get(peerIndex).state.bitmap = bits.toByteArray();
			 
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
			 Log.addLog("Have message recieved from peer "+allPeers.get(peerIndex).prop.peerId+". With piece index :"+bitIndex+"  //check "+thisIndex);
			 BigInteger selfbits = new BigInteger(fileManager.bitField);
			 Log.addLog("Testing Self Bitfield inside Have message:"+selfbits);
			 if(!selfbits.testBit(bitIndex)){
				 Log.addLog("HAVE : Need to set in self bitfield");
				 sendInterested(peerIndex);
			 }

		}
		else if(msg.getType() == MessageType.PIECE){
			//Update FileParts
			Log.addLog("PIECE : piece to set at - "+allPeers.get(peerIndex).state.pieceNumber);
			fileManager.fileParts[allPeers.get(peerIndex).state.pieceNumber] = msg.getPayload();
			allPeers.get(peerIndex).state.isWaitingForPiece = false;
			
			BigInteger bitsSelf = new BigInteger(fileManager.bitField);
			Log.addLog("PIECE : Self BitField before "+ bitsSelf);
			
			bitsSelf = bitsSelf.setBit(allPeers.get(peerIndex).state.pieceNumber);
			Log.addLog("PIECE : Self BitField after "+ bitsSelf);
			
			allPeers.get(peerIndex).prop.partsRecieved += prop.pieceSize;
			
			fileManager.bitField = bitsSelf.toByteArray();
			System.out.println("****Updated bitfield"+new BigInteger(fileManager.bitField));
			
			Log.addLog("Peer " + allPeers.get(prop.getOwnIndex()) + " has downloaded the piece " + allPeers.get(peerIndex).state.pieceNumber
            + " from " + allPeers.get(peerIndex).get_peerId() + ". Now the number of pieces it has is " + (++total) + "." + '\n');
			//Update peerFileInfo
			boolean peerGetsFile = checkHasFile(bitsSelf);
			allPeers.get(prop.getOwnIndex()).prop.hasFile = peerGetsFile; //Could also do prop.hasFile = ... But could conflict if same prop object is not passed to CurrClient and allPeers.get(ownIndex)
			prop.hasFile = peerGetsFile; //Just for debug purposes
			if(peerGetsFile)
				Log.addLog("THIS CLIENT HAS RECIEVED THE FILE");
			
			//Send have msg to others
			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i != index) // or prop.getOwnIndex()
					sendHave(i,allPeers.get(peerIndex).state.pieceNumber);
			}
			allPeers.get(peerIndex).state.pieceNumber = -1;
			
			
			for(Peer p : allPeers){
				int i = p.prop.getOwnIndex();
				if(i == index) // or prop.getOwnIndex()
					continue;
				boolean hasInterest = false;
				if(allPeers.get(i).state.bitmap != null){
					hasInterest = checkIfNeedPieces(allPeers.get(i));
				}
				if(hasInterest == false)
					sendNotInterested(i);
			}
			
			allFilesReceived = checkAllReceived();
		}
		else if(msg.getType() == MessageType.REQUEST){
			ByteBuffer buffer = ByteBuffer.wrap(msg.getPayload());
//            //I don't think we need an if statement here, they shouldnt be sending
//            //If they are choked
//            //if (!neighbors[messageIndex].choked)    //Uncomment to run legit m,m.
            int pieceNumber = buffer.getInt();
//           sendFilePiece(messageIndex, pieceNumber);
            
			int partIndex = new BigInteger(Arrays.copyOfRange(msg.getPayload(), 0, 4)).intValue();  //Alternative for above code;
			Log.addLog("REQUEST : Sending to peer "+allPeers.get(peerIndex).prop.peerId+", Piece number "+partIndex+"  //check "+pieceNumber);
			sendFileParts(peerIndex, partIndex);
            
		}
		else if(msg.getType() == MessageType.CHOKE){
			allPeers.get(peerIndex).state.choked = true;
            Log.addLog("Peer " + allPeers.get(index).prop.peerId + " is choked by  " + allPeers.get(peerIndex).prop.peerId + '\n');

		}
		else if(msg.getType() == MessageType.UNCHOKE){
			allPeers.get(peerIndex).state.choked = false;
            Log.addLog("Peer " + allPeers.get(index).prop.peerId + " is unchoked by  " + allPeers.get(peerIndex).prop.peerId + '\n');
//			if(counter_test++ > ){
//				assembleFilePieces();
//				System.exit(0);
//			}
		}
		else Log.addLog("Illegal Message Type Found : " + msg.getType());
		
		processedMessages.add(msg);
	}

	public void setUpConnections() {

		for (int i = 0; i < allPeers.size(); i++) {
			if (i != index) {
				if (!allPeers.get(i).state.hasHandshakeSent && allPeers.get(i).state.hasMadeConnection) {
					sendHandShake(i, prop.peerId);
					allPeers.get(i).state.hasHandshakeSent = true;
					Log.addLog(
							"Peer " + prop.peerId + " is connected from Peer " + allPeers.get(i).prop.peerId + '\n');
					// Logger
				}

			// if we havent sent the bitfield and we are connected and we have
			// received the handshake:
			if (!allPeers.get(i).state.hasBitfieldSent && allPeers.get(i).state.hasMadeConnection
					&& allPeers.get(i).state.hasHandshakeReceived) {
				// Send the bitfield
				sendBitfield(i);
				allPeers.get(i).state.hasBitfieldSent = true;
				Log.addLog("Sent bitfield to host " + allPeers.get(i).prop.hostName + "on port "
						+ allPeers.get(i).prop.port + '\n');
				// + " on port " + neighbors[i].portNumber + '\n');
			}

			// Request a piece at random if we are able to:
			
			if (allPeers.get(i).state.hasBitfieldReceived && allPeers.get(i).state.hasHandshakeReceived && allPeers.get(i).state.choked == false
					&& allPeers.get(i).state.isWaitingForPiece == false) {
				System.out.println("Searching for random Piece");
				allPeers.get(i).state.isWaitingForPiece = true;
				int requestedPieceNumber = getRandomPiece(allPeers.get(i));
				Log.addLog("Requested Piece Number : "+requestedPieceNumber);
				allPeers.get(i).state.pieceNumber = requestedPieceNumber; //Change name to LatestRequestedPiece
				
				
				// Call the method to send the request:
				if (requestedPieceNumber != -1 && requestedPieceNumber < prop.numberOfPieces) {
					sendRequest(i, requestedPieceNumber);
				}
				else System.out.println("REQUESTED PIECE IS -1");

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
//			comm.out[socketIndex].writeObject(msg);
//			comm.out[socketIndex].flush();
			comm1.out[socketIndex].writeObject(msg);
			comm1.out[socketIndex].flush();
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
		counter_test++;
		// Send a choke message to non-preferred peers
		byte[] message = messageBuilder.createUnchoke(index);
		sendMessage(message, index);
		System.out.println("Sent Unchoke" + counter_test);
//		if(counter_test > 5){
//			assembleFilePieces();
//			System.exit(0);
//		}
		
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
	    	//send a have message to a given index
	        byte[] message = messageBuilder.createHave(index, pieceNumber);
		      sendMessage(message, index);
	    }
	 
	public boolean checkIfNeedPieces(Peer peer) {

//		 BigInteger incomingBitfieldInt = new BigInteger(peer.state.bitmap);
//		 BigInteger selfBitfieldInt = new BigInteger(fileManager.bitField);
//		 if (incomingBitfieldInt.and(selfBitfieldInt.and(incomingBitfieldInt).not()).doubleValue() > 0) {   
//			 return true;
//	        }
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
//		 BigInteger incomingBitfieldInt = new BigInteger(peer.state.bitmap);
//		 BigInteger selfBitfieldInt = new BigInteger(fileManager.bitField);
//
//		 BigInteger interestingBits = incomingBitfieldInt.and(selfBitfieldInt.and(incomingBitfieldInt).not());
//		 
//		 int[] values = new int[interestingBits.bitLength()];
//	        int j = 0;
//	        boolean exists = false;
//	        for (int i = 0; i < interestingBits.bitLength(); i++) {
//	            if (interestingBits.testBit(i)) {
//	                values[j++] = i;
//	                exists = true;
//
////	                System.out.println("Has bit: " + i);
//	            }
//	        }
//	        System.out.println("Random VAlues are");
//	        for(int i=0;i<values.length;i++){
//	        	System.out.println(values[i]);
//	        }
//	        
//	        Log.addLog("Interesting Bits are : "+interestingBits);
//	        Random rng = new Random();
//	        //Choose a random value from the available bits
//	        if (exists) {
//	            return values[rng.nextInt(j)];
//	        } else {
//	            return -1;
//	        }
		 
		 
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
		System.out.println("HAS RECIEVED ENTIRE FILE");
		return true;
	}
	
	public boolean checkAllReceived(){
		boolean result = prop.hasFile || allPeers.get(index).prop.hasFile;
		if(result){
			System.out.println("THis peer "+allPeers.get(index).prop.peerId+" has Recieved the entire file");
		}
		for(int i=0;i<allPeers.size();i++){
			if(i != index)
				result = result && allPeers.get(i).prop.hasFile;
		}
		return result;
	}
	
	public void assembleFilePieces()  {
        try {
            FileOutputStream os = new FileOutputStream("peer_" + prop.peerId + "//" + prop.fileName);
            for (int i = 0; i < prop.numberOfPieces; i++) {
//                if (i+1 == prop.numberOfPieces)
//                    os.write(trim(fileManager.fileParts[i]));
//                else
                    os.write(fileManager.fileParts[i]);
            }
            os.close();
        } catch (Exception e) {
            //logger.info("Error assembling file pieces");
            System.exit(0);
       }
   }
   
   public byte[] trim(byte[] data) {
        int x = data.length-1;
        
        while (x >= 0 && data[x] == 0)
            --x;
        
        return Arrays.copyOf(data, x + 1);
    }

}
