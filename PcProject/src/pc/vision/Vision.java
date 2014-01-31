package pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.VisionDebugReceiver;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

/**
 * The main class for showing the video feed and processing the video data.
 * Identifies ball and robot locations, and robot orientations.
 * 
 */
public class Vision implements VideoReceiver {

	// Variables used in processing video
	private final PitchConstants pitchConstants;
	private final WorldState worldState;
	private ArrayList<VisionDebugReceiver> visionDebugReceivers = new ArrayList<VisionDebugReceiver>();
	private ArrayList<WorldStateReceiver> worldStateReceivers = new ArrayList<WorldStateReceiver>();

	private final int YELLOW_T = 0;
	private final int BLUE_T = 1;
	private final int BALL = 2;
	private final int GREY_CIRCLE = 3;

	public Vision(WorldState worldState, PitchConstants pitchConstants) {
		// Set the state fields.
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
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

	/**
	 * Processes an input image, extracting the ball and robot positions and
	 * robot orientations from it, and then displays the image (with some
	 * additional graphics layered on top for debugging) in the vision frame.
	 * 
	 * @param frame
	 *  	      The image to process and then show.
	 * @param delta
	 *            The time between frames in seconds
	 * @param counter
	 *            The index of the current frame
	 */
	public void sendFrame(BufferedImage frame, float delta, int counter) {
		BufferedImage debugOverlay = new BufferedImage(frame.getWidth(),
				frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics debugGraphics = debugOverlay.getGraphics();

		ArrayList<Position> ballPoints = new ArrayList<Position>();
		
		ArrayList<Position> baPoints = new ArrayList<Position>(); // Blue Attacker
		ArrayList<Position> bdPoints = new ArrayList<Position>(); // Blue Defender
		ArrayList<Position> yaPoints = new ArrayList<Position>(); // Yellow Attacker
		ArrayList<Position> ydPoints = new ArrayList<Position>(); // Yellow Defender
		
		int leftBuffer = this.pitchConstants.getLeftBuffer();
		int rightBuffer = this.pitchConstants.getRightBuffer();
		int[] dividers = this.pitchConstants.getDividers();
		
		// Detect the X,Y coords and rotations of the plates in each section,
		// also look for the points of the ball while looping over the frame to save time
		boolean leftBlueFirst = false; //TODO: calculate this from the appropriate location
		if (leftBlueFirst) {
			//In order, ltr: Blue Defender, Yellow Attacker, Blue Attacker, Yellow Defender
			searchColumn(bdPoints, ballPoints, frame, debugOverlay, leftBuffer, dividers[0], true);
			searchColumn(yaPoints, ballPoints, frame, debugOverlay, dividers[0], dividers[1], false);
			searchColumn(baPoints, ballPoints, frame, debugOverlay, dividers[1], dividers[2], true);
			searchColumn(ydPoints, ballPoints, frame, debugOverlay, dividers[2], frame.getWidth()-rightBuffer, false);
		} else {
			//In order, ltr: Yellow Defender, Blue Attacker, Yellow Attacker, Blue Defender
			searchColumn(ydPoints, ballPoints, frame, debugOverlay, leftBuffer, dividers[0], false);
			searchColumn(baPoints, ballPoints, frame, debugOverlay, dividers[0], dividers[1], true);
			searchColumn(yaPoints, ballPoints, frame, debugOverlay, dividers[1], dividers[2], false);
			searchColumn(bdPoints, ballPoints, frame, debugOverlay, dividers[2], frame.getWidth()-rightBuffer, true);
		}
		
		// Calculate the mean position of the points for each robot and the ball.
		Position blueDef, blueAtk, yellowDef, yellowAtk, ball;
		blueDef = calculatePosition(bdPoints);
		blueAtk = calculatePosition(baPoints);
		yellowDef = calculatePosition(ydPoints);
		yellowAtk = calculatePosition(yaPoints);
		ball = calculatePosition(ballPoints);
		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect(blueDef.getX()-2, blueDef.getY()-2, 4, 4);
		debugGraphics.drawRect(blueAtk.getX()-2, blueAtk.getY()-2, 4, 4);
		debugGraphics.drawRect(yellowDef.getX()-2, yellowDef.getY(), 4, 4);
		debugGraphics.drawRect(yellowAtk.getX()-2, yellowAtk.getY(), 4, 4);		
		
		// TODO: Using the previous position values and the time between frames, 
		// calculate the velocities of the robots and the ball.
		// Can be completed once Dimitar has implemented the world model section - Peter
		// **Temp code below**
		Velocity blueDefVel, blueAtkVel, yellowDefVel, yellowAtkVel, ballVel;
		float xdiff = blueAtk.getX() - worldState.getBlueX();
		float ydiff = blueAtk.getY() - worldState.getBlueY();
		blueAtkVel = new Velocity(xdiff/delta, ydiff/delta);
		System.out.println("x: "+ blueAtkVel.getX() + " y: " + blueAtkVel.getY());
		
		// Calculate the rotation of each of the robots.
		float blueDefAngle, blueAtkAngle, yellowDefAngle, yellowAtkAngle;
		blueDefAngle = calculateAngle(frame, debugOverlay, blueDef, 15);
		blueAtkAngle = calculateAngle(frame, debugOverlay, blueAtk, 15);
		yellowDefAngle = calculateAngle(frame, debugOverlay, yellowDef, 15);
		yellowAtkAngle = calculateAngle(frame, debugOverlay, yellowAtk, 15);
		
		// TODO: Update the world state with the new values for position, velocity and rotation.
		// **RELATIVE TO THE ORIGIN FOR POSITION**
		// The following code is temporary until Dimitar finished the world model section - Peter
		worldState.setBallX(ball.getX());
		worldState.setBallY(ball.getY());
		worldState.setBlueX(blueAtk.getX());
		worldState.setBlueY(blueAtk.getY());
		worldState.setBlueOrientation(blueAtkAngle);
		worldState.setBlueXVelocity(blueAtkVel.getX());
		worldState.setBlueYVelocity(blueAtkVel.getY());
		worldState.setYellowX(yellowAtk.getX());
		worldState.setYellowY(yellowAtk.getY());
		worldState.setYellowOrientation(yellowAtkAngle);

		// Only display these markers in non-debug mode.
		boolean anyDebug = false;
		for (int i = 0; i < 5; ++i) {
			if (this.pitchConstants.debugMode(i)) {
				anyDebug = true;
				break;
			}
		}

		if (!anyDebug) {
			debugGraphics.setColor(Color.red);
			debugGraphics.drawLine(0, this.worldState.getBallY(), 640,
					this.worldState.getBallY());
			debugGraphics.drawLine(this.worldState.getBallX(), 0,
					this.worldState.getBallX(), 480);
			debugGraphics.setColor(Color.white);
		}
		
		for (VisionDebugReceiver receiver : this.visionDebugReceivers)
			receiver.sendDebugOverlay(debugOverlay);
		for (WorldStateReceiver receiver : this.worldStateReceivers)
			receiver.sendWorldState(this.worldState);

	}

	/**
	 * Returns the angle a robot is facing relative to the horizontal axis. 
	 * 	
	 * @param frame - The current frame of video
	 * @param debugOverlay - The image for debugging
	 * @param pos - The position of the object
	 * @param margin - The radius of pixels that should be checked for the grey dot
	 * @return the heading of the robot
	 * 
	 * @author Peter Henderson (s1117205)
	 */
	private float calculateAngle(BufferedImage frame, BufferedImage debugOverlay, Position pos, int margin){
		int cumulativeGreyX = 0;
		int cumulativeGreyY = 0;
		int numGreyPoints = 0;
		float hsbvals[] = new float[3];
		
		int startRow = pos.getY() - margin;
		int startColumn = pos.getX() - margin;
		int endRow = pos.getY() + margin;
		int endColumn = pos.getX() + margin;
		
		// Find the grey points in the circle of radius 'margin' close to the position.
		for (int row = startRow; row < endRow; row++){
			for (int column = startColumn; column < endColumn; column++){
				int x2,y2,r2;
				x2 = (column-pos.getX())*(column-pos.getX());
				y2 = (row-pos.getY())*(row-pos.getY());
				r2 = margin*margin;
				boolean inBounds = (0 < row && row < frame.getHeight()) &&
						(0 < column && column < frame.getWidth());
				if (x2 + y2 <= r2 && inBounds){
					Color c = new Color(frame.getRGB(column, row));
					Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
					if (isColour(c, hsbvals, this.GREY_CIRCLE)){
						cumulativeGreyX += column;
						cumulativeGreyY += row;
						numGreyPoints++;
						if (this.pitchConstants.debugMode(PitchConstants.GREY)){
							debugOverlay.setRGB(column, row, 0xFFFF0099);
						}
					}
				}
			}
		}
		
		// Find the mean and return the angle from there to pos
		if (numGreyPoints > 0){
			int greyXMean = cumulativeGreyX / numGreyPoints;
			int greyYMean = cumulativeGreyY / numGreyPoints;
			
			// Debugging Code
			debugOverlay.getGraphics().drawOval(startColumn, startRow, 2*margin, 2*margin);
			debugOverlay.getGraphics().drawRect(greyXMean-2,greyYMean-2, 4, 4);
			
			float angle = (float) Math.toDegrees(Math.atan2(pos.getY()-greyYMean, pos.getX()-greyXMean));
			
			return (angle < 0) ? (angle+360) : angle; 
		} else {
			System.err.println("Can't find any grey points for position: " + pos.toString());
			return 0;
		}		
	}
	
	/**
	 * Returns the mean position of a list of points.
	 * 
	 * @param points
	 * @return the mean position of the points
	 * 
	 * @author Peter Henderson (s1117205)
	 */
	private Position calculatePosition(ArrayList<Position> points) {
		if (points.size() < 10){
			return new Position(0,0);
		} else {
			int xsum = 0;
			int ysum = 0;
			for (Position p : points){
				xsum += p.getX();
				ysum += p.getY();
			}
			int xmean = xsum / points.size();
			int ymean = ysum / points.size();
			return new Position (xmean, ymean);
		}
	}

	/**
	 * Searches a particular column for a plate and updates its points IN PLACE.
	 * Also searches for the pixels that make up the ball, the grey circles on the plate, 
	 * and deals with setting the debugOverlay pixels as required.
	 * 
	 * @param colourPoints - the ArrayList of points for the coloured section of the plate
	 * @param ballPoints - the ArrayList of points for the ball
	 * @param frame - the current frame of video
	 * @param debugOverlay - the image that will be overlayed for debugging
	 * @param leftEdge - The x value of the left edge of the section
	 * @param rightEdge - The x value of the right edge of the section
	 * @param isBlue - True iff we are searching for a blue plate
	 * 
	 * @author Peter Henderson (s1117205)
	 */
	private void searchColumn(ArrayList<Position> colourPoints, ArrayList<Position> ballPoints,
			BufferedImage frame, BufferedImage debugOverlay, 
			int leftEdge, int rightEdge, boolean isBlue) {
		
		int topBuffer = this.pitchConstants.getTopBuffer();
		int bottomBuffer = this.pitchConstants.getBottomBuffer();
		int obj = isBlue ? this.BLUE_T : this.YELLOW_T;
		int deb = isBlue ? PitchConstants.BLUE : PitchConstants.YELLOW;
		
		for (int row = topBuffer; row < frame.getHeight() - bottomBuffer; row++){
			for (int column = leftEdge; column < rightEdge; column++){
				Color c = new Color(frame.getRGB(column, row));
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
				
				if (isColour(c, hsbvals, obj)){
					colourPoints.add(new Position(column,row));
					if (this.pitchConstants.debugMode(deb)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				} else if (isColour(c, hsbvals, this.BALL)){
					ballPoints.add(new Position(column,row));
					if (this.pitchConstants.debugMode(PitchConstants.BALL)) {
						debugOverlay.setRGB(column, row, 0xFF000000);
					}
				}
			}
		}
	}

	/**
	 * Tests if an integer value is within bounds, or outside bounds if the
	 * range is inverted
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
	private boolean checkBounds(int value, int lower, int upper,
			boolean inverted) {
		if (!inverted)
			return (lower <= value && value <= upper);
		else
			return (upper <= value || value <= lower);
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
	private static boolean checkBounds(float value, float lower, float upper,
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
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * @param object
	 *            Indication which object we're looking for.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	private boolean isColour(Color colour, float[] hsbvals, int object) {
		int objectIdx = -1;

		switch (object) {
		case BLUE_T:
			objectIdx = PitchConstants.BLUE;
			break;
		case YELLOW_T:
			objectIdx = PitchConstants.YELLOW;
			break;
		case BALL:
			objectIdx = PitchConstants.BALL;
			break;
		case GREY_CIRCLE:
			objectIdx = PitchConstants.GREY;
		}

		return checkBounds(colour.getRed(),
				this.pitchConstants.getRedLower(objectIdx),
				this.pitchConstants.getRedUpper(objectIdx),
				this.pitchConstants.isRedInverted(objectIdx))
				&& checkBounds(colour.getGreen(),
						this.pitchConstants.getGreenLower(objectIdx),
						this.pitchConstants.getGreenUpper(objectIdx),
						this.pitchConstants.isGreenInverted(objectIdx))
				&& checkBounds(colour.getBlue(),
						this.pitchConstants.getBlueLower(objectIdx),
						this.pitchConstants.getBlueUpper(objectIdx),
						this.pitchConstants.isBlueInverted(objectIdx))
				&& checkBounds(hsbvals[0],
						this.pitchConstants.getHueLower(objectIdx),
						this.pitchConstants.getHueUpper(objectIdx),
						this.pitchConstants.isHueInverted(objectIdx))
				&& checkBounds(hsbvals[1],
						this.pitchConstants.getSaturationLower(objectIdx),
						this.pitchConstants.getSaturationUpper(objectIdx),
						this.pitchConstants.isSaturationInverted(objectIdx))
				&& checkBounds(hsbvals[2],
						this.pitchConstants.getValueLower(objectIdx),
						this.pitchConstants.getValueUpper(objectIdx),
						this.pitchConstants.isValueInverted(objectIdx));
	}
}
