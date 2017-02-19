import java.io.*;
import java.util.*;
//import java.text.ParseException;

public class Temp{
	public static void main(String args[]) throws IOException, FileNotFoundException {

        
        final String configFile = (args.length == 0 ? "PeerInfo.cfg" : args[0]);
        List<Peer> _peers = new ArrayList<Peer>();
        try {
        	BufferedReader cfg = new BufferedReader(new FileReader(configFile));
            String line = cfg.readLine();
            while(line != null){
            	String[] peer_prop = line.split("\\s+");
            	Peer p = new Peer(peer_prop[0],peer_prop[1],peer_prop[2],peer_prop[3]);
            	System.out.println(p);
            	System.out.println("");
            	line = cfg.readLine();
            }
            cfg.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
	}
}
