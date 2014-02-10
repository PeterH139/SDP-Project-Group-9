package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.PitchConstants;
import pc.vision.PixelInfo;
import pc.vision.Position;
import pc.vision.VideoStream;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.MovingObject;
import pc.world.WorldState;

public class RobotRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private SearchReturn bd, ya, ba, yd;
	public RobotRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
	}

	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay) {
		int leftBuffer = this.pitchConstants.getPitchLeft();
		int rightBuffer = frame.getWidth() - leftBuffer
				- this.pitchConstants.getPitchWidth();
		int[] dividers = this.pitchConstants.getDividers();

		// Detect the X,Y coords and rotations of the plates in each section,
		// also look for the points of the ball while looping over the frame to
		// save time
		
		boolean leftBlueFirst = false; // TODO: calculate this from the
										// appropriate location
		if (leftBlueFirst) {
			// In order, ltr: Blue Defender, Yellow Attacker, Blue Attacker,
			// Yellow Defender
			bd = searchColumn(pixels, debugOverlay, leftBuffer, 
					dividers[0], true);
			ya = searchColumn(pixels, debugOverlay, dividers[0], 
					dividers[1], false);
			ba = searchColumn(pixels, debugOverlay, dividers[1],
					dividers[2], true);
			yd = searchColumn(pixels, debugOverlay, dividers[2],
					frame.getWidth() - rightBuffer, false);
		} else {
			// In order, ltr: Yellow Defender, Blue Attacker, Yellow Attacker,
			// Blue Defender
			yd = searchColumn(pixels, debugOverlay, leftBuffer,
					dividers[0], false);
			ba = searchColumn(pixels, debugOverlay, dividers[0],
					dividers[1], true);
			ya = searchColumn(pixels, debugOverlay, dividers[1],
					dividers[2], false);
			bd = searchColumn(pixels, debugOverlay, dividers[2],
					frame.getWidth() - rightBuffer, true);
		}

		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect(bd.pos.getX() - 2, bd.pos.getY() - 2, 4, 4);
		debugGraphics.drawRect(ba.pos.getX() - 2, ba.pos.getY() - 2, 4, 4);
		debugGraphics.drawRect(yd.pos.getX() - 2, yd.pos.getY(), 4, 4);
		debugGraphics.drawRect(ya.pos.getX() - 2, ya.pos.getY(), 4, 4);

		// TODO: Using the previous position values and the time between frames,
		// calculate the velocities of the robots and the ball.

		// TODO: Update the world state with the new values for position,
		// velocity and rotation.
		// **RELATIVE TO THE ORIGIN FOR POSITION**
		worldState.setBlueX(ba.pos.getX());
		worldState.setBlueY(ba.pos.getY());
		worldState.setBlueOrientation(ba.angle);
		worldState.setYellowX(ya.pos.getX());
		worldState.setYellowY(ya.pos.getY());
		worldState.setYellowOrientation(ya.angle);

		// #Dimitar TODO: further code changes needed! the robots need to be
		// correctly
		// identified based on the sections of the field they are in.
		// right now I assume that the yellow is our team and the
		// blue is the enemy team
		MovingObject attackerRobot = new MovingObject(ya.pos.getX(),
				ya.pos.getY(), ya.angle);
		MovingObject defenderRobot = new MovingObject(yd.pos.getX(),
				yd.pos.getY(), yd.angle);
		MovingObject enemyAttackerRobot = new MovingObject(ba.pos.getX(),
				ba.pos.getY(), ba.angle);
		MovingObject enemyDefenderRobot = new MovingObject(bd.pos.getX(),
				bd.pos.getY(), bd.angle);

		worldState.SetAttackerRobot(attackerRobot);
		worldState.SetDefenderRobot(defenderRobot);
		worldState.SetEnemyAttackerRobot(enemyAttackerRobot);
		worldState.SetEnemyDefenderRobot(enemyDefenderRobot);
	}

	/**
	 * Searches a particular column for a plate and updates its points IN PLACE.
	 * Also searches for the pixels that make up the ball, the grey circles on
	 * the plate, and deals with setting the debugOverlay pixels as required.
	 * 
	 * @param colourPoints
	 *            - the ArrayList of points for the coloured section of the
	 *            plate
	 * @param ballPoints
	 *            - the ArrayList of points for the ball
	 * @param frame
	 *            - the current frame of video
	 * @param debugOverlay
	 *            - the image that will be overlayed for debugging
	 * @param leftEdge
	 *            - The x value of the left edge of the section
	 * @param rightEdge
	 *            - The x value of the right edge of the section
	 * @param isBlue
	 *            - True iff we are searching for a blue plate
	 * 
	 * @author Peter Henderson (s1117205)
	 */
	private SearchReturn searchColumn(PixelInfo[][] pixels,
			BufferedImage debugOverlay, int leftEdge, int rightEdge,
			boolean isBlue) {
		 
		ArrayList<Position> greenPoints = new ArrayList<Position>();
		int topBuffer = this.pitchConstants.getPitchTop();
		int bottomBuffer = VideoStream.FRAME_HEIGHT - topBuffer
				- this.pitchConstants.getPitchHeight();
		int obj = isBlue ? PitchConstants.OBJECT_BLUE
				: PitchConstants.OBJECT_YELLOW;

		// Find the green plate pixels
		for (int row = topBuffer; row < VideoStream.FRAME_HEIGHT - bottomBuffer; row++) {
			for (int column = leftEdge; column < rightEdge; column++) {
				if (pixels[column][row] != null) {
					if (vision.isColour(pixels[column][row],
							PitchConstants.OBJECT_GREEN)) {
						greenPoints.add(new Position(column, row));
						if (this.pitchConstants
								.debugMode(PitchConstants.OBJECT_GREEN)) {
							debugOverlay.setRGB(column, row, 0xFFFF0099);
						}
					}
				}
			}
		}	

		// Green Plate centroid
		Position greenPlate = vision.calculatePosition(greenPoints);
		int searchRadius = 15; // TODO: Determine if this is the best value.

		// For Debugging
		debugOverlay.getGraphics().drawOval(greenPlate.getX()-searchRadius, greenPlate.getY()-searchRadius, searchRadius*2, searchRadius*2);

		// Find the yellow/blue coloured pixels within the plate bounds.
		int cumulativeGreyX = 0, cumulativeGreyY = 0, numGreyPoints = 0;
		int gx = greenPlate.getX();
		int gy = greenPlate.getY();
		int r2 = searchRadius*searchRadius;
		int squareDist;
		ArrayList<Position> colourPoints = new ArrayList<Position>();
		
		for (int row = topBuffer; row < VideoStream.FRAME_HEIGHT - bottomBuffer; row++) {
			for (int column = leftEdge; column < rightEdge; column++) {
				squareDist = ((gx-column)*(gx-column)) + ((gy-row)*(gy-row));
				if (squareDist < r2){
					if (pixels[column][row] != null) {
						if (vision.isColour(pixels[column][row], obj)) {
							colourPoints.add(new Position(column, row));
							if (this.pitchConstants.debugMode(obj)) {
								debugOverlay.setRGB(column, row, 0xFFFF0099);
							}
						} else if (vision.isColour(pixels[column][row], PitchConstants.OBJECT_GREY)){
							cumulativeGreyX += column;
							cumulativeGreyY += row;
							numGreyPoints++;
							if (this.pitchConstants.debugMode(PitchConstants.OBJECT_GREY)) {
								debugOverlay.setRGB(column, row, 0xFFFF0099);
							}
						}
					}
				}
			}
		}
		
		Position pos = vision.calculatePosition(colourPoints);
		
		float returnAngle;
		if (numGreyPoints > 0) {
			double greyXMean = 1.0 * cumulativeGreyX / numGreyPoints;
			double greyYMean = 1.0 * cumulativeGreyY / numGreyPoints;

			// Debugging Code
			debugOverlay.getGraphics().drawRect((int) greyXMean - 2,
					(int) greyYMean - 2, 4, 4);

			float angle = (float) Math.toDegrees(Math.atan2(pos.getY()
					- greyYMean, pos.getX() - greyXMean));
			
			returnAngle = (angle < 0) ? (angle + 360) : angle;
		} else {
			returnAngle = 0.0f;
		}
		
		return new SearchReturn(returnAngle, pos);
	}

	private class SearchReturn{
		
		public float angle;
		public Position pos;
		
		public SearchReturn(float angle, Position pos){
			this.angle = angle;
			this.pos = pos;
		}
		
	}
	
}
