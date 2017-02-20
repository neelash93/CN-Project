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
    private byte[] messagelength;
    private byte[] payload;

    public int getLength() {
        return length;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getMessagelength() {
        return messagelength;
    }

    public byte[] getPayload() {
        return payload;
    }

    public Message(int length, byte[] payload, MessageType mtype) {
        this.length = length;
        this.messagelength = ByteBuffer.allocate(4).putInt(length).array();
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

        messageBuffer.put(messagelength);
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

    byte[] createBitField(int length, byte[] payload, MessageType mtype) {
        Message message = new Message(length, payload, mtype);
        return message.getMessageBytes();
    }

    byte[] createIntereted(int length, byte[] payload, MessageType mtype) {
        Message message = new Message(length, payload, mtype);
        return message.getMessageBytes();
    }


    byte[] createNotIntereted(int length, byte[] payload, MessageType mtype) {
        Message message = new Message(length, payload, mtype);
        return message.getMessageBytes();
    }
}
