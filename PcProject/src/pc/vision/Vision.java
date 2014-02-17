package pc.vision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.interfaces.ObjectRecogniser;
import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.VisionDebugReceiver;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

/**
 * The main class for showing the video feed and processing the video data.
 * Identifies ball and robot locations, and robot orientations.
 * 
 * @author Peter Henderson (s1117205)
 */
public class Vision implements VideoReceiver {

	// Variables used in processing video
	private final PitchConstants pitchConstants;
	private final WorldState worldState;
	private ArrayList<VisionDebugReceiver> visionDebugReceivers = new ArrayList<VisionDebugReceiver>();
	private ArrayList<WorldStateReceiver> worldStateReceivers = new ArrayList<WorldStateReceiver>();
	private ArrayList<ObjectRecogniser> recognisers = new ArrayList<ObjectRecogniser>();

	public Vision(WorldState worldState, PitchConstants pitchConstants) {
		// Set the state fields.
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		worldState.dividers = pitchConstants.getDividers();
	}

	public WorldState getWorldState() {
		return this.worldState;
	}

	/**
	 * Registers an object to receive the debug overlay from the vision system
	 * 
	 * @param receiver
	 *            The object being registered
	 */
	public void addVisionDebugReceiver(VisionDebugReceiver receiver) {
		this.visionDebugReceivers.add(receiver);
	}

	/**
	 * Registers an object to receive the world state from the vision system
	 * 
	 * @param receiver
	 *            The object being registered
	 */
	public void addWorldStateReceiver(WorldStateReceiver receiver) {
		this.worldStateReceivers.add(receiver);
	}
	
	public void removeWorldStateReciver(WorldStateReceiver reciver){
		this.worldStateReceivers.remove(reciver);
	}

	public void addRecogniser(ObjectRecogniser recogniser) {
		this.recognisers.add(recogniser);
	}

	/**
	 * Processes an input image, extracting the ball and robot positions and
	 * robot orientations from it, and then displays the image (with some
	 * additional graphics layered on top for debugging) in the vision frame.
	 * 
	 * @param frame
	 *            The image to process and then show.
	 * @param delta
	 *            The time between frames in seconds
	 * @param counter
	 *            The index of the current frame
	 */
	public void sendFrame(BufferedImage frame, float delta, int counter) {
		BufferedImage debugOverlay = new BufferedImage(frame.getWidth(),
				frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D debugGraphics = (Graphics2D) debugOverlay.getGraphics();
		
		int top = pitchConstants.getPitchTop();
		int left = pitchConstants.getPitchLeft();
		int right = left + pitchConstants.getPitchWidth();
		int bottom = top + pitchConstants.getPitchHeight();
		PixelInfo[][] pixels = new PixelInfo[VideoStream.FRAME_WIDTH][VideoStream.FRAME_HEIGHT];
		for (int row = top; row < bottom; row++){
			for (int column = left; column < right; column++){
				Color c = new Color(frame.getRGB(column, row));
				PixelInfo p = new PixelInfo(c);
				pixels[column][row] = p;
			}
		}
		
		for (ObjectRecogniser recogniser : recognisers) 
			recogniser.processFrame(pixels, frame, debugGraphics, debugOverlay);
		for (VisionDebugReceiver receiver : this.visionDebugReceivers)
			receiver.sendDebugOverlay(debugOverlay);
		for (WorldStateReceiver receiver : this.worldStateReceivers)
			receiver.sendWorldState(this.worldState);

	}

	/**
	 * Returns the mean position of a list of points.
	 * 
	 * @param points
	 * @return the mean position of the points
	 */
	public Vector2f calculatePosition(ArrayList<Position> points) {
		if (points.size() < 10) {
			return new Vector2f(0, 0);
		} else {
			int xsum = 0;
			int ysum = 0;
			for (Position p : points) {
				xsum += p.getX();
				ysum += p.getY();
			}
			float xmean = 1.0f * xsum / points.size();
			float ymean = 1.0f * ysum / points.size();
			return new Vector2f(xmean, ymean);
		}
	}

	/**
	 * Tests if a floating point value is within bounds, or outside bounds if
	 * the range is inverted
	 * 
	 * @param value
	 *            The value to check
	 * @param lower
	 *            The lower bound
	 * @param upper
	 *            The upper bound
	 * @param inverted
	 *            true if the range is inverted, false otherwise
	 * @return true if the value is within bounds, false otherwise
	 */
	public static boolean checkBounds(float value, float lower, float upper,
			boolean inverted) {
		if (!inverted)
			return (lower <= value && value <= upper);
		else
			return (upper <= value || value <= lower);
	}

	/**
	 * Determines if a pixel is part of the object specified, based on input RGB
	 * colours and hsv values.
	 * 
	 * @param pixel
	 *            The pixel info for a particular pixel
	 * @param colourId
	 *            Indication which object we're looking for. Taken from PitchConstants.
	 *            eg. PitchConstants.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	public boolean isColour(PixelInfo pixel, int colourId) {
		float[] colourValues = { pixel.r, pixel.g,
				pixel.b, pixel.h, pixel.s, pixel.v, };

		for (int ch = 0; ch < PitchConstants.NUM_CHANNELS; ch++)
			if (!Vision.checkBounds(colourValues[ch],
					this.pitchConstants.getLowerThreshold(colourId, ch),
					this.pitchConstants.getUpperThreshold(colourId, ch),
					this.pitchConstants.isThresholdInverted(colourId, ch)))
				return false;
		
		return true;
	}
}
