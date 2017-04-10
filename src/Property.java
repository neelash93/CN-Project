import java.util.HashMap;

public class Property {
	
	//Peer properties
	public String peerId;
	public String hostName;
	public int port;
	public boolean hasFile;
	
	//Common properties
	int prefferedNeighbours; 
	int unchokingInterval;
	int optUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	int numberOfPieces;
	public static HashMap<String, Integer> indexMap = new HashMap<>();
	
	public Property(String[] arr) {
		int i=0;
		this.prefferedNeighbours = Integer.parseInt(arr[i++]);
		this.unchokingInterval = Integer.parseInt(arr[i++]);
		this.optUnchokingInterval = Integer.parseInt(arr[i++]);
		this.fileName = arr[i++];
		this.fileSize = Integer.parseInt(arr[i++]);
		this.pieceSize = Integer.parseInt(arr[i++]);
		
		this.numberOfPieces = (int)Math.ceil((double)fileSize/pieceSize);
	}
	
	public Property(Property prop){
		this.prefferedNeighbours = prop.prefferedNeighbours;
		this.unchokingInterval = prop.unchokingInterval;
		this.optUnchokingInterval = prop.optUnchokingInterval;
		this.fileName = prop.fileName;
		this.fileSize = prop.fileSize;
		this.pieceSize = prop.pieceSize;
		
		this.numberOfPieces = prop.numberOfPieces;
		
	}
	
	public int getPrefferedNeighbours() {
		return prefferedNeighbours;
	}

	public int getUnchokingInterval() {
		return unchokingInterval;
	}

	public int getOptUnchokingInterval() {
		return optUnchokingInterval;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getPieceSize() {
		return pieceSize;
	}

	public int getNumberOfPieces() {
		return numberOfPieces;
	}
	public int getIndex(int peerId) {
		return indexMap.get(String.valueOf(peerId));
	}

	public int getOwnIndex(){
//		System.out.println(indexMap);
		
		return indexMap.get(peerId);
		
	}
	
	public void addPeerProp(String arr[]){
//		System.out.println("before"+this.peerId);
		this.peerId = arr[0];
//		System.out.println("after"+this.peerId);
		this.hostName = arr[1];
		this.port = Integer.parseInt(arr[2]);
		this.hasFile = arr[3].equals("1") ? true : false;
	}

	@Override
	public String toString(){
		return new String("PrefferedNeighbours: "+prefferedNeighbours+ "\nUnchoking Interval: "+unchokingInterval+"\nOptimisticUnchokingInterval: "+optUnchokingInterval+"\nFileName: "+fileName+"\nFileSize: "+fileSize+"\nPieceSize: "+pieceSize+"\nNumberOfPieces: "+numberOfPieces);
	}
	
	
	
}
