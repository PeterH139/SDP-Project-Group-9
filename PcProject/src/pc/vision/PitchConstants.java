package pc.vision;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Scanner;

/**
 * A class that holds the constants for various values about the pitch, such as
 * thresholding values and dimension variables.
 * 
 * @author Alex Adams (s1046358), Peter Henderson (s1117205)
 */
public class PitchConstants extends Observable {
	/** The number of objects there are thresholds for */
	public static final int NUM_OBJECTS = 5;

	/** The threshold index associated with the ball */
	public static final int OBJECT_BALL = 0;
	/** The threshold index associated with the blue robot */
	public static final int OBJECT_BLUE = 1;
	/** The threshold index associated with the yellow robot */
	public static final int OBJECT_YELLOW = 2;
	/** The threshold index associated with the grey circles */
	public static final int OBJECT_GREY = 3;
	/** The threshold index associated with the green plate */
	public static final int OBJECT_GREEN = 4;
	/** Names of threshold objects */
	public static final String[] OBJECT_NAMES = { "Ball", "Blue plate",
			"Yellow plate", "Grey dot on the plate", "Green plate" };

	/** The minimum value for the red, green, and blue colour components */
	public static final int RGBMIN = 0;
	/** The maximum value for the red, green, and blue colour components */
	public static final int RGBMAX = 255;
	/** The minimum value for the hue, saturation, and value colour components */
	public static final float HSVMIN = 0.0f;
	/** The maximum value for the hue, saturation, and value colour components */
	public static final float HSVMAX = 1.0f;

	public static final int NUM_CHANNELS = 6;
	
	public static final int CHANNEL_RED = 0;
	
	public static final int CHANNEL_GREEN = 1;
	
	public static final int CHANNEL_BLUE = 2;
	
	public static final int CHANNEL_HUE = 3;
	
	public static final int CHANNEL_SATURATION = 4;
	
	public static final int CHANNEL_BRIGHTNESS = 5;

	// The pitch number. 0 is the main pitch, 1 is the side pitch
	private int pitchNum;

	// Threshold upper and lower values
	private float[][] lowerThreshold = new float[NUM_OBJECTS][NUM_CHANNELS];
	private float[][] upperThreshold = new float[NUM_OBJECTS][NUM_CHANNELS];
	private boolean[][] thresholdInverted = new boolean[NUM_OBJECTS][NUM_CHANNELS];
	// Debug
	private boolean[] debug = new boolean[NUM_OBJECTS];

	// Pitch dimensions
	// When scanning the pitch we look at pixels starting from 0 + topBuffer and
	// 0 + leftBuffer, and then scan to pixels at 480 - bottomBuffer and 640 -
	// rightBuffer.
	private Rectangle pitchBounds = new Rectangle();

	// Holds the x values of the pitch divisions. Used when detecting the plates
	// on the board.
	private int[] dividers = new int[3];

	public int[] getDividers() {
		return this.dividers;
	}

	public void setDividers(int[] dividers) {
		if (dividers.length != 3) {
			System.err.println("Dividers array not the right size to set!");
		} else {
			if (!Arrays.equals(this.dividers, dividers)) {
				this.dividers = dividers;
				setChanged();
			}
			notifyObservers();
		}
	}

	/**
	 * Default constructor.
	 * 
	 * @param pitchNum
	 *            The pitch that we are on.
	 */
	public PitchConstants(int pitchNum) {
		// Just call the setPitchNum method to load in the constants
		setPitchNum(pitchNum);
	}

	public float getLowerThreshold(int object, int channel) {
		return lowerThreshold[object][channel];
	}

	public float getUpperThreshold(int object, int channel) {
		return upperThreshold[object][channel];
	}

	public boolean isThresholdInverted(int object, int channel) {
		return thresholdInverted[object][channel];
	}

	public void setThresholds(int object, int channel, float lower, float upper) {
		if (lower != lowerThreshold[object][channel]
				|| upper != upperThreshold[object][channel]) {
			lowerThreshold[object][channel] = lower;
			upperThreshold[object][channel] = upper;
			setChanged();
			notifyObservers();
		}
	}

	public void setThresholdInverted(int object, int channel, boolean inverted) {
		if (inverted != thresholdInverted[object][channel]) {
			thresholdInverted[object][channel] = inverted;
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Gets the width of the pitch
	 * 
	 * @return the width of the pitch in pixels
	 */
	public int getPitchWidth() {
		return this.pitchBounds.width;
	}

	/**
	 * Gets the height of the pitch
	 * 
	 * @return the height of the pitch in pixels
	 */
	public int getPitchHeight() {
		return this.pitchBounds.height;
	}

	public int getPitchLeft() {
		return this.pitchBounds.x;
	}

	public int getPitchTop() {
		return this.pitchBounds.y;
	}

	public Rectangle getPitchBounds() {
		return new Rectangle(this.pitchBounds);
	}

	public void setPitchBounds(Rectangle bounds) {
		if (bounds != null && !this.pitchBounds.equals(bounds)) {
			this.pitchBounds = bounds;
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Tests whether debug mode is enabled for the threshold set i refers to
	 * 
	 * @param i
	 *            One of: BALL, BLUE, YELLOW, GREY, GREEN - other values will
	 *            cause an ArrayIndexOutOfBoundsException
	 * @return true if debug mode is enabled, false otherwise
	 */
	public boolean debugMode(int i) {
		return this.debug[i];
	}

	/**
	 * Enables or disables debug mode for the threshold set i refers to. This
	 * method permits multiple debug modes to be enabled
	 * 
	 * @param i
	 *            One of: BALL, BLUE, YELLOW, GREY, GREEN - other values will
	 *            cause an ArrayIndexOutOfBoundsException
	 * @param debug
	 *            A boolean value to enable debug mode if true, and disable
	 *            otherwise
	 */
	public void setDebugMode(int i, boolean debug) {
		if (this.debug[i] != debug) {
			this.debug[i] = debug;
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Enables or disables debug mode for the threshold set i refers to. This
	 * method permits multiple debug modes to be enabled only if allowMultiple
	 * is set to true.
	 * 
	 * @param i
	 *            One of: BALL, BLUE, YELLOW, GREY, GREEN - other values will
	 *            cause an ArrayIndexOutOfBoundsException
	 * @param debug
	 *            A boolean value to enable debug mode if true, and disable
	 *            otherwise
	 * @param allowMultiple
	 *            A boolean value specifying whether to allow multiple debug
	 *            modes to be set
	 */
	public void setDebugMode(int i, boolean debug, boolean allowMultiple) {
		if (allowMultiple)
			setDebugMode(i, debug);
		else {
			for (int j = 0; j < 5; ++j)
				setDebugMode(j, (i == j) && debug);
		}
	}

	/**
	 * Gets the current pitch number
	 * 
	 * @return The pitch number
	 */
	public int getPitchNum() {
		return this.pitchNum;
	}

	/**
	 * Sets a new pitch number, loading in constants from the corresponding
	 * file.
	 * 
	 * @param newPitchNum
	 *            The pitch number to use.
	 */
	public void setPitchNum(int newPitchNum) {
		assert (newPitchNum == 0 || newPitchNum == 1) : "Invalid pitch number";
		this.pitchNum = newPitchNum;

		loadConstants(System.getProperty("user.dir") + "/constants/pitch"
				+ this.pitchNum);

		setChanged();
		notifyObservers();
	}

	public void saveConstants(int pitchNumber) {
		saveConstants(String.valueOf(pitchNumber));
	}

	/**
	 * Save the constants to a file.
	 * 
	 * @param fileName
	 *            The file to save the constants to
	 */
	public void saveConstants(String fileName) {
		try {
			// Update the pitch dimensions file
			FileWriter pitchDimFile = new FileWriter(new File("constants/pitch"
					+ this.pitchNum + "Dimensions"));
			pitchDimFile.write(String.valueOf(getPitchTop()) + "\n");
			pitchDimFile.write(String.valueOf(getPitchHeight()) + "\n");
			pitchDimFile.write(String.valueOf(getPitchLeft()) + "\n");
			pitchDimFile.write(String.valueOf(getPitchWidth()) + "\n");
			pitchDimFile.write(String.valueOf(this.dividers[0]) + "\n");
			pitchDimFile.write(String.valueOf(this.dividers[1]) + "\n");
			pitchDimFile.write(String.valueOf(this.dividers[2]) + "\n");
			pitchDimFile.close();

			FileWriter pitchFile = new FileWriter(new File("constants/pitch"
					+ this.pitchNum));

			// Iterate over the ball, blue robot, yellow robot, grey circles,
			// and green plates in the order they're defined above.
			for (int i = 0; i < NUM_OBJECTS; ++i) {
				for (int ch = 0; ch < NUM_CHANNELS; ch++) {
					pitchFile.write(String.valueOf(getLowerThreshold(i, ch))
							+ "\n");
					pitchFile.write(String.valueOf(getUpperThreshold(i, ch))
							+ "\n");
					pitchFile.write(String.valueOf(isThresholdInverted(i, ch))
							+ "\n");
				}
			}
			pitchFile.close();

			System.out.println("Wrote successfully!");
		} catch (IOException e) {
			System.err.println("Cannot save constants file " + fileName + ":");
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Load in the constants from a file. Note that this assumes that the
	 * constants file is well formed.
	 * 
	 * @param fileName
	 *            The file to load the constants from.
	 */
	private void loadConstants(String fileName) {
		Scanner scannerDim;

		try {
			scannerDim = new Scanner(new File(fileName + "Dimensions"));
			assert (scannerDim != null);

			// Pitch Dimensions
			this.pitchBounds.y = scannerDim.nextInt();
			this.pitchBounds.height = scannerDim.nextInt();
			this.pitchBounds.x = scannerDim.nextInt();
			this.pitchBounds.width = scannerDim.nextInt();

			this.dividers[0] = scannerDim.nextInt();
			this.dividers[1] = scannerDim.nextInt();
			this.dividers[2] = scannerDim.nextInt();

			scannerDim.close();
		} catch (Exception e) {
			System.err.println("Cannot load pitch dimensions file " + fileName
					+ "Dimensions:");
			System.err.println(e.getMessage());
			loadDefaultConstants();
			return;
		}

		Scanner scanner;

		try {
			scanner = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.err.println("Cannot load thresholds file " + fileName + ":");
			System.err.println(e.getMessage());
			loadDefaultConstants();
			return;
		}

		assert (scanner != null);

		// Iterate over the ball, blue robot, yellow robot, grey circles, and
		// green plates in the order they're defined above.
		for (int i = 0; i < NUM_OBJECTS; ++i) {
			for (int ch = 0; ch < NUM_CHANNELS; ch++) {
				this.lowerThreshold[i][ch] = scanner.nextFloat();
				this.upperThreshold[i][ch] = scanner.nextFloat();
				this.thresholdInverted[i][ch] = scanner.nextBoolean();
			}
		}

		scanner.close();
	}

	/**
	 * Loads default values for the constants, used when loading from a file
	 * fails.
	 */
	private void loadDefaultConstants() {
		// Iterate over the ball, blue robot, yellow robot, grey circles, and
		// green plates in the order they're defined above.
		for (int i = 0; i < NUM_OBJECTS; ++i) {
			for (int ch = CHANNEL_RED; ch <= CHANNEL_BLUE; ch++) {
				this.lowerThreshold[i][ch] = RGBMIN;
				this.upperThreshold[i][ch] = RGBMAX;
				this.thresholdInverted[i][ch] = false;
			}
			for (int ch = CHANNEL_HUE; ch <= CHANNEL_BRIGHTNESS; ch++) {
				this.lowerThreshold[i][ch] = HSVMIN;
				this.upperThreshold[i][ch] = HSVMAX;
				this.thresholdInverted[i][ch] = false;
			}
		}

		// Pitch Dimensions
		this.pitchBounds.setBounds(40, 40, 600, 400);

		this.dividers[0] = 70;
		this.dividers[1] = 120;
		this.dividers[2] = 170;
	}
}
