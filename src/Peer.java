
public class Peer {
	String _peerId;
	String _hostName;
	String _port;
	boolean _hasFile;
	
	Peer(String peerId, String host, String port, String hasfile){
		_peerId = peerId;
		_hostName = host;
		_port = port;
		_hasFile = hasfile.equals("1") ? true : false;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(_peerId+" "+_hostName+" "+_port+" "+_hasFile);
		return new String(sb);
	}
}
