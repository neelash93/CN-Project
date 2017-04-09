import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class CurrentClient {
	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public FileManager fileManager;
	boolean allFilesReceived;
	public MessageBuilder message;
	public CurrentClient(int index, ArrayList<Peer> peers){
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
//		System.out.println("Index"+index);
		file = new FileManager(prop);
		comm = new Communication(prop,allPeers);
		
	}
	
	public void process() {
		while(!allFilesReceived) {
			setUpConnections();
			
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
	
	private static void sendBitfield(int index) {
		/*
        //create bitfield message and send it to peers
        Message bitfieldMessage = new Message(bitfield.length, (byte)Message.bitfield, bitfield);
        //System.out.println("sending bitfield: " + bitfield.toString());
        sendMessage(bitfieldMessage.getMessageBytes(), index);
        */
    }
	
    //send a message to the output stream
	public void sendMessage(byte[] msg, int socketIndex)
    {
        try {
            //stream write the message
            comm.out[socketIndex].writeObject(msg);
            comm.out[socketIndex].flush();
        }
        catch(IOException ioException){
        	System.err.println("Error sending message.");
            ioException.printStackTrace();
        }
    }

	public void sendHandShake(String peerId) {
		byte[] handshake = message.createHandshake(Integer.parseInt(peerId));
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
        for (int i = 0; i < peer.bitfield.length * 8; i++) {
            if ((bitfield[bitfield.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                  selfbits.set(i);
            }
       }
        
       incomingbits.andNot(selfbits);
       int fromIndex = 0;
       int j = 0;
       boolean exists = false;
       int[] val = new int[incomingbits.length()];
       
       while(fromIndex < incomingbits.length()) {
    	   val[j++] = incomingbits.nextSetBit(fromIndex);
    	   fromIndex = val[j];
    	   exists = true;
       }
       
/*  
       for (int i = 0; i < incomingbits.length(); i++) {
    	   if(incomingbits.get(i)) {
    		   val[j++] = i; 
    		   exists = true;
    	   }
       }
       

        int[] values = new int[interestingBits.bitLength()];
        int j = 0;
        boolean exists = false;
        for (int i = 0; i < interestingBits.bitLength(); i++) {
            if (interestingBits.testBit(i)) {
                values[j++] = i;
                exists = true;

//                System.out.println("Has bit: " + i);
            }    
        }
  */      
        //Choose a random value from the available bits
        if (exists) {
            return val[rng.nextInt(j)];
        } else {
            return -1;
        }
    }
}
