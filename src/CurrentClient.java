import java.util.ArrayList;
import java.util.List;

public class CurrentClient {
	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public FileManager file;
	Log l;
	
	public CurrentClient(int index, ArrayList<Peer> peers){
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
		l = new Log(prop.peerId);
		file = new FileManager(prop);
		comm = new Communication(prop,allPeers);
		
	}
}
