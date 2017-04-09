/**
 * Created by Prateek
 */
public class PeerState {
    public byte[] bitmap;

    public boolean choked;
    public boolean interested;

    public boolean isWaitingForPiece;
    public boolean hasConnectionRefused ;
    public boolean hasBitfieldReceived ;
    public boolean hasHandshakeSent ;
    public boolean hasHandshakeReceived ;
    public boolean hasFile ;
    public boolean hasBitfieldSent ;
    public boolean hasMadeConnection;

    public int pieceNumber ;

    public PeerState() {
        this.pieceNumber=-1;
        this.choked=true;

    }

}
