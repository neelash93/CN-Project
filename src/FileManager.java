import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

public class FileManager {
	public byte[] bitField;
	public int totalBytes;
	public Property prop;
	byte[][] fileParts;
	
	public FileManager(Property prop){
		//Initialize everything necessary for managing file parts
		this.prop = prop;
		this.totalBytes = (int)Math.ceil((double)prop.numberOfPieces/8);
		bitField = new byte[totalBytes+1];
		fileParts = new byte[prop.numberOfPieces][prop.pieceSize];
		
		//Create the Current Peers Directory
		createDir();
		
		if(prop.hasFile){
//			BigInteger temp = new BigInteger("0");
//			for(int i=0;i<prop.numberOfPieces;i++)
//				temp = temp.setBit(i);
			
			//Set bits to 1
			int tempNum = 0;
			for(int i=bitField.length-1;i>=0;i--){
				for(int j=0;j<=7;j++){
					if(tempNum++ < prop.numberOfPieces)
						bitField[i] |= 1<<j;
					else break;
				}
				if(tempNum >= prop.numberOfPieces) break;
			}
//			bitField = temp.toByteArray();
			//Read file into byte array
			try{
				File file = new File("peer_" + prop.peerId + "\\" + prop.fileName);
				if(!file.isFile())
					file = new File(prop.fileName);
	    	 	FileInputStream inputStream = new FileInputStream(file);
				for(int i=0;i<fileParts.length;i++){
					inputStream.read(fileParts[i++]);
				}
//				testFile();
			}
			catch(Exception e){
				e.printStackTrace();
				Log.addLog("Error Getting File");
			}
		}
	}
	
	public void createDir(){
		String dirName = "peer_"+prop.peerId;
		String currDir = System.getProperty("user.dir");
		File newDir = new File(currDir+"//"+dirName);
		if(!newDir.isDirectory())
			newDir.mkdir();
	}
	
	public void testFile(){
		//Test if file is being read properly
		try{
			File f = new File("peer_"+prop.peerId+"\\temp.txt");
			FileOutputStream os = new FileOutputStream(f);
			for (int i = 0; i < prop.numberOfPieces; i++) {
                os.write(fileParts[i]);
            }
			os.close();
		}
		catch(Exception e){
			System.out.println("Error writing File");
		}
	}
}
