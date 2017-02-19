import java.io.*;
import java.util.*;

public class ParseCFG {
	public static void main(String args[]) throws IOException, FileNotFoundException {
		String peer_cfg = "PeerInfo.cfg";
		List<Peer> peers = new ArrayList<Peer>();
		try {
			BufferedReader cfg = new BufferedReader(new FileReader(peer_cfg));
			String line = cfg.readLine();
			while(line != null){
				String items[] = line.split("\\s+");
				Peer p = new Peer(items[0],items[1],items[2],items[3]);
				System.out.println(p);
				peers.add(p);
				line = cfg.readLine();
			}
			cfg.close();
		}
		catch(Exception e){
			System.out.println(e);
			System.exit(1);
		}
	}
}
