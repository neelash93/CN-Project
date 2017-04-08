import java.util.ArrayList;

public class CurrentClient {
	int index;
	ArrayList<Peer> allPeers;
	Property prop;
	public Communication comm;
	public CurrentClient(ArrayList<Peer> peers){
		this.allPeers = peers;
		prop = allPeers.get(index).prop;
		comm = new Communication(prop);
	}
}
