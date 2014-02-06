package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Frame;
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

	public RobotRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
	}

	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay) {
		Position blueDef, blueAtk, yellowDef, yellowAtk;
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
			blueDef = searchColumn(pixels, debugOverlay, leftBuffer,
					dividers[0], true);
			yellowAtk = searchColumn(pixels, debugOverlay, dividers[0],
					dividers[1], false);
			blueAtk = searchColumn(pixels, debugOverlay, dividers[1],
					dividers[2], true);
			yellowDef = searchColumn(pixels, debugOverlay, dividers[2],
					frame.getWidth() - rightBuffer, false);
		} else {
			// In order, ltr: Yellow Defender, Blue Attacker, Yellow Attacker,
			// Blue Defender
			yellowDef = searchColumn(pixels, debugOverlay, leftBuffer,
					dividers[0], false);
			blueAtk = searchColumn(pixels, debugOverlay, dividers[0],
					dividers[1], true);
			yellowAtk = searchColumn(pixels, debugOverlay, dividers[1],
					dividers[2], false);
			blueDef = searchColumn(pixels, debugOverlay, dividers[2],
					frame.getWidth() - rightBuffer, true);
		}

		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect(blueDef.getX() - 2, blueDef.getY() - 2, 4, 4);
		debugGraphics.drawRect(blueAtk.getX() - 2, blueAtk.getY() - 2, 4, 4);
		debugGraphics.drawRect(yellowDef.getX() - 2, yellowDef.getY(), 4, 4);
		debugGraphics.drawRect(yellowAtk.getX() - 2, yellowAtk.getY(), 4, 4);

		// TODO: Using the previous position values and the time between frames,
		// calculate the velocities of the robots and the ball.

		// Calculate the rotation of each of the robots.
		float blueDefAngle, blueAtkAngle, yellowDefAngle, yellowAtkAngle;
		blueDefAngle = calculateAngle(pixels, debugOverlay, blueDef, 14);
		blueAtkAngle = calculateAngle(pixels, debugOverlay, blueAtk, 14);
		yellowDefAngle = calculateAngle(pixels, debugOverlay, yellowDef, 14);
		yellowAtkAngle = calculateAngle(pixels, debugOverlay, yellowAtk, 14);

		// TODO: Update the world state with the new values for position,
		// velocity and rotation.
		// **RELATIVE TO THE ORIGIN FOR POSITION**
		worldState.setBlueX(blueAtk.getX());
		worldState.setBlueY(blueAtk.getY());
		worldState.setBlueOrientation(blueAtkAngle);
		// worldState.setBlueXVelocity(blueAtkVel.getX());
		// worldState.setBlueYVelocity(blueAtkVel.getY());
		worldState.setYellowX(yellowDef.getX());
		worldState.setYellowY(yellowDef.getY());
		worldState.setYellowOrientation(yellowDefAngle);

		// #Dimitar TODO: further code changes needed! the robots need to be
		// correctly
		// identified based on the sections of the field they are in.
		// right now I assume that the yellow is our team and the
		// blue is the enemy team
		MovingObject attackerRobot = new MovingObject(yellowAtk.getX(),
				yellowAtk.getY(), yellowAtkAngle);
		MovingObject defenderRobot = new MovingObject(yellowDef.getX(),
				yellowDef.getY(), yellowDefAngle);

		MovingObject enemyAttackerRobot = new MovingObject(blueAtk.getX(),
				blueAtk.getY(), blueAtkAngle);
		MovingObject enemyDefenderRobot = new MovingObject(blueDef.getX(),
				blueDef.getY(), blueDefAngle);

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
	private Position searchColumn(PixelInfo[][] pixels,
			BufferedImage debugOverlay, int leftEdge, int rightEdge,
			boolean isBlue) {
		ArrayList<Position> points = new ArrayList<Position>();
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
		// Green plate bounds.
		Position maxX = new Position(0, 0);
		Position maxY = new Position(0, 0);
		Position minX = new Position(VideoStream.FRAME_WIDTH + 1,
				VideoStream.FRAME_WIDTH + 1);
		Position minY = new Position(VideoStream.FRAME_HEIGHT + 1,
				VideoStream.FRAME_HEIGHT + 1);
		for (Position p : greenPoints) {
			int x = p.getX();
			int y = p.getY();
			if (x < minX.getX())
				minX = p;
			if (x >= maxX.getX())
				maxX = p;
			if (y <= minY.getY())
				minY = p;
			if (y > maxY.getY())
				maxY = p;
		}
		// For Debugging
		debugOverlay.getGraphics().drawLine(minX.getX(), minX.getY(),
				minY.getX(), minY.getY());
		debugOverlay.getGraphics().drawLine(minX.getX(), minX.getY(),
				maxY.getX(), maxY.getY());
		debugOverlay.getGraphics().drawLine(maxX.getX(), maxX.getY(),
				minY.getX(), minY.getY());
		debugOverlay.getGraphics().drawLine(maxX.getX(), maxX.getY(),
				maxY.getX(), maxY.getY());

		// Find the yellow/blue coloured pixels within the plate bounds.

		for (int row = minY.getY(); row < maxY.getY(); row++) {
			for (int column = minX.getX(); column < maxX.getX(); column++) {
				if (pointInSquare(column, row, minX, minY, maxX, maxY)) {
					debugOverlay.setRGB(column, row, 0xFFFF0099);
				}
			}
		}

		// Find the grey circle within the plate bounds.
		// Calculate angles from those.

		for (int row = topBuffer; row < VideoStream.FRAME_HEIGHT - bottomBuffer; row++) {
			for (int column = leftEdge; column < rightEdge; column++) {
				if (pixels[column][row] != null) {
					if (vision.isColour(pixels[column][row], obj)) {
						points.add(new Position(column, row));
						if (this.pitchConstants.debugMode(obj)) {
							debugOverlay.setRGB(column, row, 0xFFFF0099);
						}
					}
				}
			}
		}

		return vision.calculatePosition(points);
	}

	private boolean pointInSquare(int x, int y, Position minX, Position minY,
			Position maxX, Position maxY) {
		return pointInTriangle(new Position(x, y), minY, maxX, maxY)
				&& pointInTriangle(new Position(x, y), minY, minX, maxY);
	}

	private int sign(Position p1, Position p2, Position p3) {
		return ((p1.getX() - p3.getX()) * (p2.getY() - p3.getY()))
				- ((p2.getX() - p3.getX()) * (p1.getY() - p3.getY()));
	}

	private boolean pointInTriangle(Position pt, Position v1, Position v2,
			Position v3) {
		boolean b1, b2, b3;

		b1 = sign(pt, v1, v2) < 0;
		b2 = sign(pt, v2, v3) < 0;
		b3 = sign(pt, v3, v1) < 0;

		return ((b1 == b2) && (b2 == b3));
	}

	/**
	 * Returns the angle a robot is facing relative to the horizontal axis.
	 * 
	 * @param frame
	 *            - The current frame of video
	 * @param debugOverlay
	 *            - The image for debugging
	 * @param pos
	 *            - The position of the object
	 * @param margin
	 *            - The radius of pixels that should be checked for the grey dot
	 * @return the heading of the robot
	 * 
	 * @author Peter Henderson (s1117205)
	 */
	private float calculateAngle(PixelInfo[][] pixels,
			BufferedImage debugOverlay, Position pos, int margin) {
		int cumulativeGreyX = 0;
		int cumulativeGreyY = 0;
		int numGreyPoints = 0;

		int startRow = pos.getY() - margin;
		int startColumn = pos.getX() - margin;
		int endRow = pos.getY() + margin;
		int endColumn = pos.getX() + margin;

		// Find the grey points in the circle of radius 'margin' close to the
		// position.
		for (int row = startRow; row < endRow; row++) {
			for (int column = startColumn; column < endColumn; column++) {
				int x2, y2, r2;
				x2 = (column - pos.getX()) * (column - pos.getX());
				y2 = (row - pos.getY()) * (row - pos.getY());
				r2 = margin * margin;
				boolean inBounds = (0 < row && row < VideoStream.FRAME_HEIGHT)
						&& (0 < column && column < VideoStream.FRAME_WIDTH);
				if (x2 + y2 <= r2 && inBounds && pixels[column][row] != null) {
					if (vision.isColour(pixels[column][row],
							PitchConstants.OBJECT_GREY)) {
						cumulativeGreyX += column;
						cumulativeGreyY += row;
						numGreyPoints++;
						if (this.pitchConstants
								.debugMode(PitchConstants.OBJECT_GREY)) {
							debugOverlay.setRGB(column, row, 0xFFFF0099);
						}
					}
				}
			}
		}

		// Find the mean and return the angle from there to pos
		if (numGreyPoints > 0) {
			double greyXMean = 1.0 * cumulativeGreyX / numGreyPoints;
			double greyYMean = 1.0 * cumulativeGreyY / numGreyPoints;

			// Debugging Code
			debugOverlay.getGraphics().drawOval(startColumn, startRow,
					2 * margin, 2 * margin);
			debugOverlay.getGraphics().drawRect((int) greyXMean - 2,
					(int) greyYMean - 2, 4, 4);

			float angle = (float) Math.toDegrees(Math.atan2(pos.getY()
					- greyYMean, pos.getX() - greyXMean));

			return (angle < 0) ? (angle + 360) : angle;
		} else {
			// System.err.println("Can't find any grey points for position: " +
			// pos.toString());
			return 0;
		}
	}

}
