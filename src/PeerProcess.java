import java.io.*;
import java.util.*;

class PeerProcess{
	static String peerId;
	final static String cfgFile = "PeerInfo.cfg";
	final static String commonCfg = "Common.cfg";
	
	public static void main(String args[]) throws FileNotFoundException {
		peerId = args[0];
		
		Property prop = parseCommonCfg();
		ArrayList<Peer> peers = parsePeerInfo(prop);
		System.out.println("Reaches here");
		CurrentClient c = new CurrentClient(Property.indexMap.get(peerId), peers);
		
	}
	
	private static ArrayList<Peer> parsePeerInfo(Property prop){
		ArrayList<Peer> peer = new ArrayList<>();
		int i = 0;
		try {
			BufferedReader cfg = new BufferedReader(new FileReader(cfgFile));
			String line = cfg.readLine();
			while(line != null){
				String items[] = line.split("\\s+");
				prop.indexMap.put(items[0], i++);
				Property t = new Property(prop);
				t.addPeerProp(items);
				System.out.println(t.indexMap+"  "+t.hostName+"   "+t.peerId);
				Peer p = new Peer(t);
				peer.add(p);
				line = cfg.readLine();
			}
			cfg.close();
		}
		catch(Exception e){
			System.out.println(e);
			System.exit(1);
		}
		return peer;
	}
	
	private static Property parseCommonCfg() {
		try{
			BufferedReader comm = new BufferedReader(new FileReader(commonCfg));
			String line = comm.readLine();
			String[] arr = new String[6];
			int i=0;
			while(line != null && !line.equals("")){
				arr[i++] = line.split("\\s+")[1];
				line = comm.readLine();
			}
			return new Property(arr);
		}
		catch(Exception e){
			System.out.println(e);
			System.exit(1);
		}
		return null;
	}
	
	

}

