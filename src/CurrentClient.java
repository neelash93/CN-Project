import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
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
		}

		if (iteration++ == 1) {
			DeterminePreferredNeighborTask determinePreferredNeighborTask = new DeterminePreferredNeighborTask(this);
			scheduler.schedule(determinePreferredNeighborTask, prop.unchokingInterval);

			DetermineOptimisticNeighborTask determineOptimisticNeighborTask = new DetermineOptimisticNeighborTask(this);
			scheduler.schedule(determineOptimisticNeighborTask, prop.optUnchokingInterval);
		}
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
				allPeers.get(i).state.pieceNumber = requestedPieceNumber;

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
		} catch (IOException ioException) {
			System.err.println("Error sending message.");
			ioException.printStackTrace();
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

}
