package pc.vision;

/**
 * An exception thrown when the orientation of the robots can't be determined by
 * vision
 */
@SuppressWarnings("serial")
public class NoAngleException extends Exception {
	public NoAngleException() {
		super();
	}

	public NoAngleException(String message) {
		super(message);
	}
}
