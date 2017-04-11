public class PeerState {
	//All Peers file state
    public byte[] bitField;
    
    //All Peers connection status
    public boolean choked;
    public boolean interested;

    public boolean hasBitfieldReceived;
    public boolean hasBitfieldSent;
    
    public boolean hasHandshakeSent;
    public boolean hasHandshakeReceived;
    
    public boolean hasMadeConnection;
    public boolean hasConnectionRefused;
    
    //Peer's Piece Information
    public int lastRequestedPart;
    public boolean isWaitingForPiece;

    public PeerState() {
        this.lastRequestedPart=-1;
        this.choked=true;
    }
}
