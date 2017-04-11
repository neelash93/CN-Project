import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TimerTask;
import java.util.Comparator;

class PeerWithDownloadRate {
	int peerId;
	double downloadRate;

	public PeerWithDownloadRate(int peerId, double downloadRate) {
		super();
		this.peerId = peerId;
		this.downloadRate = downloadRate;
	}
}

public class DeterminePreferredPeerTask extends TimerTask {

	private CurrentClient client;
	public int preferredSize;

	public DeterminePreferredPeerTask(CurrentClient client) {
		this.client = client;
		this.preferredSize = client.prop.prefferedNeighbours;
	}

	@Override
	public void run() {
		this.determinePreferredPeers();
	}

	public void determinePreferredPeers() {

		List<Peer> allPeers = this.client.allPeers; // to maintain list of all peers
		List<Double> downloads = new ArrayList<>(); // to maintain download rates of all peers
		for (Peer peer : allPeers) {
			downloads.add(downloadSpeedCalc(peer));
		}

		// Priority Queue with peer id, download rate
		// Used to resolve tie in download rates randomly
		PriorityQueue<PeerWithDownloadRate> priorityQueue = new PriorityQueue(new Comparator<PeerWithDownloadRate>() {
			public int compare(PeerWithDownloadRate rate1,PeerWithDownloadRate rate2) {
				return (int)(rate1.downloadRate - rate2.downloadRate);
			}
		});	

		for(int i=0; i < downloads.size(); i++) {
			priorityQueue.add(new PeerWithDownloadRate(i, downloads.get(i)));
		}

		// Store peers with top download rates from priority queue
		List<Integer> topDownloadRatePeers = new ArrayList<>();
		for(int i=0; i < client.prop.prefferedNeighbours; i++) {
			topDownloadRatePeers.add(priorityQueue.remove().peerId);
		}

		Log.addLog("Peer " + client.prop.peerId + " has the preferred neighbors " + Arrays.toString(topDownloadRatePeers.toArray()) + '\n');
		
		// to check whether to send choke or unchoke message
		boolean exists = false;
		// traverse through all peers and check if any peer is optimistically choked 
		for (int i = 0; i < topDownloadRatePeers.size(); i++) { 
			for (int j = 0; j < allPeers.size(); j++) { 
				if (topDownloadRatePeers.get(i)== j) {
					exists = true; // if peer exists then send unchoke message
					break;
				}
			}

			if (i != this.client.index) {
				// if this peer exists then unchoke it
				if (exists) { 
					this.client.sendUnchoke(i); 
					exists = false;
				} else {
					// choke the peer id it does not exist
					this.client.sendChoke(i); 
				}
			}
		}
	}

	// calculates the downloading speed
	public double downloadSpeedCalc(Peer peer) {
		// to check if peer is connected and interested 
		if (peer.state.hasMadeConnection && peer.state.interested && peer.prop.getOwnIndex() != this.client.prop.getOwnIndex()) {
			// calculate downloading rate in the last unchoking interval
			int val = peer.prop.partsRecieved/this.client.prop.unchokingInterval;
			peer.prop.partsRecieved = 0;
			return val;
		} else {
			return 0;
		}
	}

}
