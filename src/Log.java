import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	private final Logger logger = Logger.getLogger(Log.class.getName());
    private FileHandler handler = null;

    public Log(String peerId){
        try {
            handler = new FileHandler("peer_"+peerId+"\\log.log");
        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }
    

    public void log(String msg, char type) {
        if(type == 'i')
    		logger.info(msg);
        else if(type == 's')
        	logger.severe(msg);
        else if(type == 'f')
        	logger.fine(msg); //won't show because to high level of logging
    }
    
    public void log(String msg){
    	log(msg,'i');
    }
}
