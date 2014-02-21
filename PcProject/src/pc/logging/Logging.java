package pc.logging;
import java.io.IOException;
import java.util.logging.*;
import java.util.Date;

public class Logging {
	private Logger mainLogger;
	private FileHandler mainFileHandler;
	
	/**
	 * Initiates the logger
	 * */
	public Logging(){
		mainLogger = Logger.getLogger("");
		Date d = new Date();
		try {
			mainFileHandler = new FileHandler(d.toString()+".txt");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mainLogger.addHandler(mainFileHandler);
		
	}
	
	public void Log(String msg){
		mainLogger.info(msg);
	}
}
