
public class Property {
	int prefferedNeighbours; 
	int unchokingInterval;
	int optUnchokingInterval;
	String fileName;
	int fileSize;
	int pieceSize;
	int numberOfPieces;
	
	public Property(String[] arr) {
		int i=0;
		this.prefferedNeighbours = Integer.parseInt(arr[i++]);
		this.unchokingInterval = Integer.parseInt(arr[i++]);
		this.optUnchokingInterval = Integer.parseInt(arr[i++]);
		this.fileName = arr[i++];
		this.fileSize = Integer.parseInt(arr[i++]);
		this.pieceSize = Integer.parseInt(arr[i++]);
		
		this.numberOfPieces = (int)Math.ceil(fileSize/pieceSize);

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

	@Override
	public String toString(){
		return new String("PrefferedNeighbours: "+prefferedNeighbours+ "\nUnchoking Interval: "+unchokingInterval+"\nOptimisticUnchokingInterval: "+optUnchokingInterval+"\nFileName: "+fileName+"\nFileSize: "+fileSize+"\nPieceSize: "+pieceSize+"\nNumberOfPieces: "+numberOfPieces);
	}
	
	
	
}
