package pc.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormat extends Formatter {

	@Override
	public String format(LogRecord arg0) {
		// TODO Auto-generated method stub
		// TODO: implement custom logging format here
		
		return arg0.getMessage();
	}

}
