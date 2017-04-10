import java.util.TimerTask;

public class DetermineOptimisticNeighborTask extends TimerTask {

	private CurrentClient client;

	public DetermineOptimisticNeighborTask(CurrentClient client) {
		this.client = client;
	}

	@Override
	public void run() {
		this.client.optimisticNeighbor = this.client.determineOptimisticNeighbor();
	}
}