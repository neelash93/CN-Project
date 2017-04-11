import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Peer {
	
	public Property prop;
	public PeerState state;

	Peer(Property prop){
		this.prop = prop;
		this.state = new PeerState();
	}
	
	public String get_peerId() {
		return prop.peerId;
	}

	public String get_hostName() {
		return prop.hostName;
	}

	public int get_port() {
		return prop.port;
	}
	
//	@Override
//	public String toString(){
//		return (peerId+" "+hostName+" "+port+" "+hasFile);
//	}
}
