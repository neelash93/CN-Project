import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class DetermineOptimisticPeerTask extends TimerTask {

	private CurrentClient client;

	public DetermineOptimisticPeerTask(CurrentClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		int optimisticPeer = this.client.optimisticNeighbor;
		this.client.optimisticNeighbor = this.determineOptimisticPeer();
		if(optimisticPeer != this.client.optimisticNeighbor) {
			Log.addLog("Peer " + this.client.prop.peerId + " has the optimistically unchoked neighbor " + this.client.allPeers.get(this.client.optimisticNeighbor).prop.peerId+ "." + '\n');
		}
	}

	public int determineOptimisticPeer() {

		List<Peer> allPeers = this.client.allPeers; // maintains the list of all peers
		// Calculate the optimistic peer from available interested peers
		List<Integer> values = new ArrayList<>();

		for (int i = 0; i < allPeers.size(); i++) {
			if (allPeers.get(i).state.interested) {
				values.add(i);
			}
		}

		// choose a random peer and unchoke it
		int randomPeer = 0;
		if (values.size() > 0) {
			randomPeer = values.get((int) (Math.random() * values.size()));
			this.client.sendUnchoke(randomPeer);
		}		
		
		return randomPeer;
	}
}