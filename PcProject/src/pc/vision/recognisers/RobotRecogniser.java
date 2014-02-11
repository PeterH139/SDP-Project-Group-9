package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.PitchConstants;
import pc.vision.PixelInfo;
import pc.vision.Position;
import pc.vision.Vector2f;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.MovingObject;
import pc.world.WorldState;

public class RobotRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private SearchReturn blueDef, yellowAtk, blueAtk, yellowDef;
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

		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect((int)blueDef.pos.x - 2, (int)blueDef.pos.y - 2, 4, 4);
		debugGraphics.drawRect((int)blueAtk.pos.x - 2, (int)blueAtk.pos.y - 2, 4, 4);
		debugGraphics.drawRect((int)yellowDef.pos.x - 2, (int)yellowDef.pos.y, 4, 4);
		debugGraphics.drawRect((int)yellowAtk.pos.x - 2, (int)yellowAtk.pos.y, 4, 4);

		// TODO: Using the previous position values and the time between frames,
		// calculate the velocities of the robots and the ball.
		// #Peter: Should this be done in the new world model?

		// #Dimitar TODO: further code changes needed! the robots need to be
		// correctly
		// identified based on the sections of the field they are in.
		// right now I assume that the yellow is our team and the
		// blue is the enemy team
		MovingObject attackerRobot = new MovingObject(blueAtk.pos.x,
				blueAtk.pos.y, blueAtk.angle);
		MovingObject defenderRobot = new MovingObject(yellowDef.pos.x,
				yellowDef.pos.y, yellowDef.angle);
		MovingObject enemyAttackerRobot = new MovingObject(blueAtk.pos.x,
				blueAtk.pos.y, blueAtk.angle);
		MovingObject enemyDefenderRobot = new MovingObject(blueDef.pos.x,
				blueDef.pos.y, blueDef.angle);

		worldState.setAttackerRobot(attackerRobot);
		worldState.setDefenderRobot(defenderRobot);
		worldState.setEnemyAttackerRobot(enemyAttackerRobot);
		worldState.setEnemyDefenderRobot(enemyDefenderRobot);
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
		int searchRadius = 14; // TODO: Determine if this is the best value.

		// For Debugging
		debugOverlay.getGraphics().drawOval((int)greenPlate.x-searchRadius, (int)greenPlate.y-searchRadius, searchRadius*2, searchRadius*2);

		// Find the yellow/blue coloured pixels within the plate bounds.
		int cumulativeGreyX = 0, cumulativeGreyY = 0, numGreyPoints = 0;
		int gx = (int)greenPlate.x;
		int gy = (int)greenPlate.y;
		int r2 = searchRadius*searchRadius;
		int squareDist;
		ArrayList<Position> colourPoints = new ArrayList<Position>();
		
		for (int row = topBuffer; row < bottomBuffer; row++) {
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
		
		Vector2f pos = vision.calculatePosition(colourPoints);
		
		float returnAngle;
		if (numGreyPoints > 0) {
			float greyXMean = 1.0f * cumulativeGreyX / numGreyPoints;
			float greyYMean = 1.0f * cumulativeGreyY / numGreyPoints;

			// Debugging Code
			debugOverlay.getGraphics().drawRect((int) greyXMean - 2,
					(int) greyYMean - 2, 4, 4);

			float angle = (float) Math.toDegrees(Math.atan2(pos.y
					- greyYMean, pos.x - greyXMean));
			
			returnAngle = (angle < 0) ? (angle + 360) : angle;
		} else {
			returnAngle = 0.0f;
		}
		
		return new SearchReturn(returnAngle, pos);
	}

	private class SearchReturn{
		
		public float angle;
		public Vector2f pos;
		
		public SearchReturn(float angle, Vector2f pos){
			this.angle = angle;
			this.pos = pos;
		}
		
	}
	
}
