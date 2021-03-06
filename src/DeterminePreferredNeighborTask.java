import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

public class DeterminePreferredNeighborTask extends TimerTask {

	private CurrentClient client;

	public DeterminePreferredNeighborTask(CurrentClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		this.determinePreferredNeighbors();
	}

	public void determinePreferredNeighbors() {
		// TODO Auto-generated method stub

		List<Peer> allPeers = this.client.allPeers;

		boolean[] chosen = new boolean[this.client.allPeers.size()];

		int topctr = 0; // Used to determine how full preferredNeighbors is
		int startIndex = 0; // Used to break ties that don't fit into
							// preferredNeighbors
		int tieLength = 1;
		int randomIndex;

		List<Double> downloads = new ArrayList<>();

		for (Peer peer : allPeers) {
			downloads.add(calculateDownloadRate(peer));
		}

		Collections.sort(downloads);
		Collections.reverse(downloads);

		List<Integer> preferredNeighbors = this.client.preferredPeers;
		List<Double> topRates = downloads.subList(0, preferredNeighbors.size());

		if ((downloads.size() > preferredNeighbors.size())
				&& (downloads.get(preferredNeighbors.size() - 1) == downloads.get(preferredNeighbors.size()))) { // If
			// there is a tie that doesn't fit
			for (int i = preferredNeighbors.size() - 1; i > 0; i--) { // Determine where it starts
				if ((i != 1) && (downloads.get(i) == downloads.get(i - 1))) {
					startIndex = i - 1;
				} else {
					break;
				}
			}

			for (int i = startIndex; i < downloads.size(); i++) { // Then determine how long is it
				if ((i != downloads.size() - 1) && (downloads.get(i) == downloads.get(i + 1))) {
					tieLength += 1;
				}
			}
		}

		for (int i = 0; i < preferredNeighbors.size(); i++) { // Fill the preferredNeigh
																// preferredNeighbors
																// array with
																// proper
																// indices
			for (int j = 0; j < downloads.size(); j++) {
				if ((tieLength == 1 || i < startIndex) && (topRates.get(i) == downloads.get(j)) && !chosen[j]) {
					// If there wasn't a tie that didn't fit or it hasn't been reached yet
					preferredNeighbors.set(i, j); // And this value is the correct one, take the index of downloads[]
					chosen[j] = true; // Mark as chosen
					break;
				}
			}

			if (i >= startIndex && tieLength != 0) { // If there is a tie that doesn't fit and it has been reached
				while (true) { // Randomly probe for a proper value
					randomIndex = (int) (Math.random() * downloads.size()); // Select a random integer in the range [0, downloads.length)
	
					if (downloads.get(randomIndex) == topRates.get(i) && !chosen[randomIndex]) {
						preferredNeighbors.set(i, randomIndex);
						chosen[randomIndex] = true; // Mark as chosen
						break;
					}
				}
			}
		}
		
		//logger.info("Peer " + peerId + " has the preferred neighbors " + Arrays.toString(preferredNeighbors) + '\n');

		boolean found = false; // Determine whether to send choke or unchoke message
		for (int i = 0; i < preferredNeighbors.size(); i++) { // Loop through all neighbors
			for (int j = 0; j < allPeers.size(); j++) { // Check if neighbor i is preferred or optimistically unchoked
				if (preferredNeighbors.get(i) == j) {
					found = true; // If so, mark true
					break;
				}
			}

			if (i != this.client.index) {
				if (found) { // If this neighbor was found
					this.client.sendUnchoke(i); // Unchoke it
					found = false; // And mark found as false for next neighbor
				} else
					this.client.sendChoke(i); // Otherwise choke it
			}
		}

	}
	
	public double calculateDownloadRate(Peer peer) {
		//System.out.println("Calculate download rate: peer, madeConnection, interested, ownIndex: " + neighbors[peer].madeConnection + "," + neighbors[peer].interested);
	      if (peer.state.hasMadeConnection && peer.state.interested && peer.prop.getOwnIndex() != this.client.prop.getOwnIndex()) { //Check if connection was made and peer is interested
	        //Calculate download rate
	        int val = this.client.prop.partsRecieved/this.client.prop.unchokingInterval;
	        this.client.prop.partsRecieved = 0;
	        return val;
	      }
	      else {
	        return 0;
	      }
	}

}
