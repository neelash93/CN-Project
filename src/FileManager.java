import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class FileManager {
	byte[] bitField;
	int totalBytes;
	Property prop;
	byte[][] fileParts;
	
	public FileManager(Property prop){
		this.prop = prop;
		this.totalBytes = (int)Math.ceil((double)prop.numberOfPieces/8);
		bitField = new byte[totalBytes+1];
		fileParts = new byte[prop.numberOfPieces][prop.pieceSize];
		if(prop.hasFile){
			Arrays.fill( bitField, (byte) 1 );
			try{
				File file = new File("peer_" + prop.peerId + "\\" + prop.fileName);
	    	 	FileInputStream inputStream = new FileInputStream(file);
				for(int i=0;i<fileParts.length;i++){
					inputStream.read(fileParts[i++]);  //Check with a huge file. Out of memory errors
				}
//				testFile();
				
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println("Error Getting File");
			}
		}
	}
	
	public void testFile(){
		try{
			File f = new File("peer_"+prop.peerId+"\\temp.txt");
			FileOutputStream os = new FileOutputStream(f);
			for (int i = 0; i < prop.numberOfPieces; i++) {
                if (i+1 == prop.numberOfPieces)
                    os.write(trim(fileParts[i]));
                else
                    os.write(fileParts[i]);
            }
			os.close();
		}
		catch(Exception e){
			System.out.println("Error writing File");
		}
	}
	
	private static byte[] trim(byte[] data) {
        int x = data.length-1;

        while (x >= 0 && data[x] == 0)
            --x;

        return Arrays.copyOf(data, x + 1);
    }
}
