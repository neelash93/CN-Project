/**
 * Created by Prateek on 2/19/2017.
 */


import java.nio.ByteBuffer;


enum MessageType {
    CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3), HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), HANDSHAKE(8);
    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

class Message {
    private int length;
    private MessageType type;
    private byte[] messageLength;
    private byte[] payload;
    private int clientId;
    public int getLength() {
        return length;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getMessageLength() {
        return messageLength;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Message(int length,  MessageType mType, byte[] payload, int clientId) {
        this(length,  payload, mType);
        this.clientId = clientId;
    }


    public Message(int length, MessageType type, byte[] payload) {
        this.length = length;
        this.messageLength = ByteBuffer.allocate(4).putInt(length).array();
        this.type = type;

        if (hasPayload(type)) {
            this.payload = new byte[length];
            this.payload = payload;
        }
        else {
            this.payload = null;
        }

    }

    public Message(int length, byte[] payload, MessageType mtype) {
        this.length = length;
        this.messageLength = ByteBuffer.allocate(4).putInt(length).array();
        this.type = mtype;

        if (hasPayload((mtype))) {
            this.payload = new byte[length];
            this.payload = payload;
        } else {
            this.payload = null;
        }

    }

    public boolean hasPayload(MessageType mtype) {
        return (mtype == MessageType.HAVE || mtype == MessageType.BITFIELD
                || mtype == MessageType.REQUEST || mtype == MessageType.PIECE);
    }

    public byte[] getMessageBytes() {
        ByteBuffer messageBuffer = ByteBuffer.allocate(5 + length);

        messageBuffer.put(messageLength);
        messageBuffer.putInt(type.getValue());

        if (payload != null) {
            messageBuffer.put(payload);
        }

        return messageBuffer.array();
    }

}

public class MessageBuilder {


    byte[] createHandshake(int peerId) {
        byte[] handshakeHeader = new byte[18];
        try {
            handshakeHeader = "P2PFILESHARINGPROJ".getBytes("UTF-8");
        } catch (Exception e) {

        }

        byte[] zeroBits = new byte[10];
        byte[] peeridArray = ByteBuffer.allocate(4).putInt(peerId).array();

        ByteBuffer handshakeBuffer = ByteBuffer.allocate(32);

        handshakeBuffer.put(handshakeHeader);
        handshakeBuffer.put(zeroBits);
        handshakeBuffer.put(peeridArray);

        byte[] handshake = handshakeBuffer.array();
        return handshake;
    }

    public byte[] createBitfield(byte[] bitfield) {
        //create bitfield message and send it to peers
        Message message = new Message(bitfield.length, MessageType.BITFIELD, bitfield);
        //System.out.println("sending bitfield: " + bitfield.toString());
        return message.getMessageBytes();

    }

    public  byte[] createInterested(int peerId) {
        Message message = new Message(0,MessageType.INTERESTED, null);
        return message.getMessageBytes();
    }

    public byte[]  createNotInterested(int peerId) {
        Message message = new Message(0,MessageType.NOT_INTERESTED, null);
        return message.getMessageBytes();
    }

    public byte[] createHave(int index, int pieceNumber) {
        byte[] pieceIndex = ByteBuffer.allocate(4).putInt(pieceNumber).array();
        Message message = new Message(4,MessageType.HAVE,pieceIndex);
        return message.getMessageBytes();
    }

    public byte[] createFilePiece(int pieceSize,byte filePieces[][] ,int pieceNumber) {
        //Send file piece to a given server
        Message message = new Message(pieceSize, MessageType.PIECE, filePieces[pieceNumber]);
        return message.getMessageBytes();
    }

    public byte[] createChoke(int index) {
        //Send a choke message to non-preferred peers
        Message message = new Message(0,MessageType.CHOKE, null);
        return message.getMessageBytes();
    }

    public byte[] sendUnchoke(int index) {
        //Send a choke message to non-preferred peers
        Message message = new Message(0,MessageType.UNCHOKE, null);
        return message.getMessageBytes();
    }

    private byte[] sendRequest(int index, int pieceNumber) {
        //send a request message to a given inde
        byte[] pieceIndex = ByteBuffer.allocate(4).putInt(pieceNumber).array();
        Message message = new Message(4,MessageType.REQUEST,pieceIndex);

        //logger.info("Sending request " + pieceNumber + " to server " + index + '\n');
        return message.getMessageBytes();

    }
}
