import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Peer {
	private String peerId;
	private String hostName;
	private String port;
	public boolean hasFile;
	public Property prop;
	public Socket requestSocket;           
    public ObjectOutputStream out;  
    public ObjectInputStream in;
	public byte[] bitmap;
	
	Peer(String peerId, String host, String port, String hasFile, Property prop){
		this.peerId = peerId;
		this.hostName = host;
		this.port = port;
		this.hasFile = hasFile.equals("1") ? true : false;
		this.prop = prop;
		
		createPeerDir();
		
	}
	
	public String get_peerId() {
		return peerId;
	}

	public String get_hostName() {
		return hostName;
	}

	public String get_port() {
		return port;
	}
	
	public void createPeerDir(){
		String dirName = "peer_"+peerId;
		String currDir = System.getProperty("user.dir");
		File newDir = new File(currDir+"//"+dirName);
		if(!newDir.isDirectory())
			newDir.mkdir();
	}
	
	@Override
	public String toString(){
		return (peerId+" "+hostName+" "+port+" "+hasFile);
	}
}
