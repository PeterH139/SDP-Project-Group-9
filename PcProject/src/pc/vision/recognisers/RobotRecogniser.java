package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.PixelInfo;
import pc.vision.Position;
import pc.vision.Vector2f;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.StaticWorldState;
import pc.world.oldmodel.MovingObject;
import pc.world.oldmodel.WorldState;

public class RobotRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private DistortionFix distortionFix;
	private SearchReturn blueDef, yellowAtk, blueAtk, yellowDef;
	private SearchReturn blueDefPrev = new SearchReturn();
	private SearchReturn yellowDefPrev = new SearchReturn();
	private SearchReturn blueAtkPrev = new SearchReturn();
	private SearchReturn yellowAtkPrev = new SearchReturn();

	public RobotRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants, DistortionFix distortionFix) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
	}

	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay,
			StaticWorldState staticWorldState) {
		int leftBuffer = this.pitchConstants.getPitchLeft();
		int rightBuffer = frame.getWidth() - leftBuffer
				- this.pitchConstants.getPitchWidth();
		int[] dividers = this.pitchConstants.getDividers();

		boolean leftBlueFirst = !(worldState.weAreBlue ^ worldState.weAreShootingRight);
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

		if (blueAtk.pos.x == 0 && blueAtk.pos.y == 0) {
			blueAtk = blueAtkPrev;
		} else {
			Point2D.Double point = new Point2D.Double(blueAtk.pos.x,
					blueAtk.pos.y);
			distortionFix.barrelCorrect(point);
			blueAtk.pos.x = (float) point.x;
			blueAtk.pos.y = (float) point.y;
			heightCorrection(blueAtk.pos, 2450, 175);
		}

		if (blueDef.pos.x == 0 && blueDef.pos.y == 0) {
			blueDef = blueDefPrev;
		} else {
			Point2D.Double point = new Point2D.Double(blueDef.pos.x,
					blueDef.pos.y);
			distortionFix.barrelCorrect(point);
			blueDef.pos.x = (float) point.x;
			blueDef.pos.y = (float) point.y;
			heightCorrection(blueDef.pos, 2450, 175);
		}

		if (yellowAtk.pos.x == 0 && yellowAtk.pos.y == 0) {
			yellowAtk = yellowAtkPrev;
		} else {
			Point2D.Double point = new Point2D.Double(yellowAtk.pos.x,
					yellowAtk.pos.y);
			distortionFix.barrelCorrect(point);
			yellowAtk.pos.x = (float) point.x;
			yellowAtk.pos.y = (float) point.y;
			heightCorrection(yellowAtk.pos, 2450, 175);
		}

		if (yellowDef.pos.x == 0 && yellowDef.pos.y == 0) {
			yellowDef = yellowDefPrev;
		} else {
			Point2D.Double point = new Point2D.Double(yellowDef.pos.x,
					yellowDef.pos.y);
			distortionFix.barrelCorrect(point);
			yellowDef.pos.x = (float) point.x;
			yellowDef.pos.y = (float) point.y;
			heightCorrection(yellowDef.pos, 2450, 175);
		}

		// Update Histories
		blueDefPrev = blueDef;
		blueAtkPrev = blueAtk;
		yellowDefPrev = yellowDef;
		yellowAtkPrev = yellowAtk;

		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect((int) blueDef.pos.x - 2,
				(int) blueDef.pos.y - 2, 4, 4);
		debugGraphics.drawRect((int) blueAtk.pos.x - 2,
				(int) blueAtk.pos.y - 2, 4, 4);
		debugGraphics.drawRect((int) yellowDef.pos.x - 2,
				(int) yellowDef.pos.y, 4, 4);
		debugGraphics.drawRect((int) yellowAtk.pos.x - 2,
				(int) yellowAtk.pos.y, 4, 4);

		// TODO: Using the previous position values and the time between frames,
		// calculate the velocities of the robots and the ball.
		// #Peter: Should this be done in the new world model?

		// #Peter: Robots are now decided based on which colour plates we are
		// using.
		MovingObject attackerRobot, defenderRobot, enemyAttackerRobot, enemyDefenderRobot;
		if (worldState.weAreBlue) {
			attackerRobot = new MovingObject(blueAtk.pos.x, blueAtk.pos.y,
					blueAtk.angle);
			defenderRobot = new MovingObject(blueDef.pos.x, blueDef.pos.y,
					blueDef.angle);
			enemyAttackerRobot = new MovingObject(yellowAtk.pos.x,
					yellowAtk.pos.y, yellowAtk.angle);
			enemyDefenderRobot = new MovingObject(yellowDef.pos.x,
					yellowDef.pos.y, yellowDef.angle);
		} else {
			attackerRobot = new MovingObject(yellowAtk.pos.x, yellowAtk.pos.y,
					yellowAtk.angle);
			defenderRobot = new MovingObject(yellowDef.pos.x, yellowDef.pos.y,
					yellowDef.angle);
			enemyAttackerRobot = new MovingObject(blueAtk.pos.x, blueAtk.pos.y,
					blueAtk.angle);
			enemyDefenderRobot = new MovingObject(blueDef.pos.x, blueDef.pos.y,
					blueDef.angle);
		}

		worldState.setAttackerRobot(attackerRobot);
		worldState.setDefenderRobot(defenderRobot);
		worldState.setEnemyAttackerRobot(enemyAttackerRobot);
		worldState.setEnemyDefenderRobot(enemyDefenderRobot);
	}

	/**
	 * This method recalculates the position of an object based on its distance
	 * from the camera. It helps reduce the error caused by an object being
	 * detected higher than the surface of the pitch.
	 */

	public static void heightCorrection(Vector2f object, int cameraH,
			int objectH) {
		double x = 320 - object.x, y = 240 - object.y;
		double dist = Math.hypot(x, y);
		double angle = Math.atan2(y, x);
		// Subtract y from dist
		dist = dist - ((objectH * dist) / cameraH);

		object.x = 320 - (float) (dist * Math.cos(angle));
		object.y = 240 - (float) (dist * Math.sin(angle));

	}

	/**
	 * Searches a particular column for a plate. Also searches for the pixels
	 * that make up the ball, the grey circles on the plate, and deals with
	 * setting the debugOverlay pixels as required.
	 * 
	 * @param pixels
	 *            - the pixel information for the current frame of video
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
		int bottomBuffer = topBuffer + this.pitchConstants.getPitchHeight();
		int obj = isBlue ? PitchConstants.OBJECT_BLUE
				: PitchConstants.OBJECT_YELLOW;

		// Find the green plate pixels
		for (int row = topBuffer; row < bottomBuffer; row++) {
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
		Vector2f greenPlate = vision.calculatePosition(greenPoints);
		int searchRadius = 14;

		// For Debugging
		debugOverlay.getGraphics().drawOval((int) greenPlate.x - searchRadius,
				(int) greenPlate.y - searchRadius, searchRadius * 2,
				searchRadius * 2);

		// Find the yellow/blue coloured pixels within the plate bounds.
		int cumulativeGreyX = 0, cumulativeGreyY = 0, numGreyPoints = 0;
		int gx = (int) greenPlate.x;
		int gy = (int) greenPlate.y;
		int r2 = searchRadius * searchRadius;
		int squareDist;
		ArrayList<Position> colourPoints = new ArrayList<Position>();

		for (int row = topBuffer; row < bottomBuffer; row++) {
			for (int column = leftEdge; column < rightEdge; column++) {
				squareDist = ((gx - column) * (gx - column))
						+ ((gy - row) * (gy - row));
				if (squareDist < r2) {
					if (pixels[column][row] != null) {
						if (vision.isColour(pixels[column][row], obj)) {
							colourPoints.add(new Position(column, row));
							if (this.pitchConstants.debugMode(obj)) {
								debugOverlay.setRGB(column, row, 0xFFFF0099);
							}
						} else if (vision.isColour(pixels[column][row],
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
		}

		Vector2f pos = vision.calculatePosition(colourPoints);

		float returnAngle;
		if (numGreyPoints > 0) {
			float greyXMean = 1.0f * cumulativeGreyX / numGreyPoints;
			float greyYMean = 1.0f * cumulativeGreyY / numGreyPoints;

			// Debugging Code
			debugOverlay.getGraphics().drawRect((int) greyXMean - 2,
					(int) greyYMean - 2, 4, 4);

			float angle = (float) Math.toDegrees(Math.atan2(pos.y - greyYMean,
					pos.x - greyXMean));

			returnAngle = (angle < 0) ? (angle + 360) : angle;
		} else {
			returnAngle = 0.0f;
		}

		return new SearchReturn(returnAngle, pos);
	}

	private class SearchReturn {

		public float angle;
		public Vector2f pos;

		public SearchReturn() {
			this(0, new Vector2f(0, 0));
		}

		public SearchReturn(float angle, Vector2f pos) {
			this.angle = angle;
			this.pos = pos;
		}

	}

}
