import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class DetermineOptimisticNeighborTask extends TimerTask {

	private CurrentClient client;

	public DetermineOptimisticNeighborTask(CurrentClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		this.client.optimisticNeighbor = this.determineOptimisticNeighbor();
	}

	public int determineOptimisticNeighbor() {

		List<Peer> allPeers = this.client.allPeers;
		// Calculate from the available interested neighbors
		List<Integer> values = new ArrayList<>();

		for (int i = 0; i < allPeers.size(); i++) {
			if (allPeers.get(i).state.interested) {
				values.add(i);
			}
		}

		int randomNeighbor = 0;

		// Choose a random neighbor from the list
		if (values.size() > 0) {
			randomNeighbor = values.get((int) (Math.random() * values.size()));
			this.client.sendUnchoke(randomNeighbor);
		}

		return randomNeighbor;
	}

}