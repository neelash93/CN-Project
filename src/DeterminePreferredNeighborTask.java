import java.util.TimerTask;

public class DeterminePreferredNeighborTask extends TimerTask{

	private CurrentClient client;
	
	public DeterminePreferredNeighborTask(CurrentClient client) {
		this.client = client;
	}
	
    @Override
    public void run(){
       this.client.determinePreferredNeighbors();
    }
}
