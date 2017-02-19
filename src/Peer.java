
public class Peer {
	String _peerId;
	String _hostName;
	String _port;
	boolean _hasFile;
	
	Peer(String peerId, String host, String port, String hasFile){
		_peerId = peerId;
		_hostName = host;
		_port = port;
		_hasFile = hasFile.equals("1") ? true : false;
	}
	
	@Override
	public String toString(){
		return (_peerId+" "+_hostName+" "+_port+" "+_hasFile);
	}
}
