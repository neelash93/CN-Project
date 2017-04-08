import java.io.*;
import java.util.*;

class PeerProcess{
	static int index;
	static int peerId;
	final static String cfgFile = "PeerInfo.cfg";
	final static String commonCfg = "Common.cfg";
	
	public static void main(String args[]) throws FileNotFoundException {
//		peerId = Integer.parseInt(args[1]);
		
		Property prop = parseCommonCfg();
		ArrayList<Peer> peers = parsePeerInfo(prop);
		
		CurrentClient c = new CurrentClient(index, peers);
		
	}
	
	private static ArrayList<Peer> parsePeerInfo(Property prop){
		ArrayList<Peer> peer = new ArrayList<>();
		int i = 0;
		try {
			BufferedReader cfg = new BufferedReader(new FileReader(cfgFile));
			String line = cfg.readLine();
			while(line != null){
				String items[] = line.split("\\s+");
				Peer p = new Peer(items[0],items[1],items[2],items[3], prop);
				if(Integer.parseInt(p.get_peerId()) == peerId)
					index = i;
				i++;
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
				System.out.println(line + "  "+i);
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
	
	private HashMap<String,Thread>requestPool=new HashMap<>();

	Server server;
	PeerProcess(String ipAddress,String port,String peerName){


	}
	

}

