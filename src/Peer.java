import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Peer {
	
	public Property prop;
	
	
	
	public Socket requestSocket;           
    public ObjectOutputStream out;  
    public ObjectInputStream in;
	public byte[] bitmap;
	public FileManager file;
	
	
	Peer(Property prop){
		this.prop = prop;
		createPeerDir();
		initialize();
	}
	
	public String get_peerId() {
		return prop.peerId;
	}

	public String get_hostName() {
		return prop.hostName;
	}

	public String get_port() {
		return prop.port;
	}
	
	public void initialize(){
		file = new FileManager(prop);
		
	}
	
	public void createPeerDir(){
		String dirName = "peer_"+prop.peerId;
		String currDir = System.getProperty("user.dir");
		File newDir = new File(currDir+"//"+dirName);
		if(!newDir.isDirectory())
			newDir.mkdir();
	}
	
//	@Override
//	public String toString(){
//		return (peerId+" "+hostName+" "+port+" "+hasFile);
//	}
}
