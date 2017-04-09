import java.util.ArrayList;
import java.util.List;

public class CurrentClient {
	int index;
	List<Peer> allPeers;
	Property prop;
	public Communication comm;
	public CurrentClient(ArrayList<Peer> peers){
		this.allPeers = peers;
		prop = allPeers.get(index).prop;
		comm = new Communication(prop,allPeers);
	}
}
