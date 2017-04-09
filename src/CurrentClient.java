import java.util.ArrayList;
import java.util.List;

public class CurrentClient {
	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public FileManager file;
	public CurrentClient(int index, ArrayList<Peer> peers){
		this.allPeers = peers;
		this.index = index;
		prop = allPeers.get(index).prop;
//		System.out.println("Index"+index);
		file = new FileManager(prop);
		comm = new Communication(prop,allPeers);
		
	}
}
