import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
	public void schedule(TimerTask task, int seconds) {
		new Timer().scheduleAtFixedRate(task, 0, seconds * 1000);
	}
}