import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
	private final static Logger logger = Logger.getLogger(Log.class.getName());
    private FileHandler handler = null;

    public Log(String peerId){
        try {
            handler = new FileHandler(System.getProperty("user.dir")+"\\peer_"+peerId+"\\log_peer_"+peerId+".log");
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogFormat format = new LogFormat();
        handler.setFormatter(format);
        logger.addHandler(handler);
    }
    

    public static void addLog(String msg, char type) {
        if(type == 'i')
    		logger.info(msg);
        else if(type == 's')
        	logger.severe(msg);
        else if(type == 'f')
        	logger.fine(msg); //won't show because to high level of logging
    }
    
    public static void addLog(String msg){
    	addLog(msg,'i');
    }
}


class LogFormat extends Formatter {
    // Create a DateFormat to format the logger timestamp.
    private static final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

    public String format(LogRecord record) {
        StringBuilder str = new StringBuilder();
        str.append(df.format(new Date(record.getMillis()))).append(" - ");
        str.append("[").append(record.getLevel()).append("] - ");
        str.append(formatMessage(record));
//        str.append("\r\n"); //for windows
        str.append("\n"); //for Linux
        return str.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
