package pc.vision.recognisers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
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
import pc.vision.interfaces.PitchViewProvider;
import pc.world.DirectedPoint;
import pc.world.DynamicWorldState;
import pc.world.Pitch;
import pc.world.RobotModel;
import pc.world.StaticWorldState;
import pc.world.oldmodel.MovingObject;
import pc.world.oldmodel.WorldState;

public class RobotRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private DistortionFix distortionFix;
	private Pitch pitch;
	private SearchReturn blueDef = new SearchReturn();
	private SearchReturn yellowAtk = new SearchReturn();
	private SearchReturn blueAtk= new SearchReturn();
	private SearchReturn yellowDef= new SearchReturn();
	private SearchReturn blueDefPrev = new SearchReturn(),
			yellowDefPrev = new SearchReturn(),
			blueAtkPrev = new SearchReturn(),
			yellowAtkPrev = new SearchReturn();
	private boolean blueAtkNotOnPitch, blueDefNotOnPitch,yellowAtkNotOnPitch,yellowDefNotOnPitch;
	
	public RobotRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants, DistortionFix distortionFix,
			Pitch pitch) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
		this.pitch = pitch;
	}

	private static void drawRobotPos(Graphics2D debugGraphics, SearchReturn pos) {
		if (pos.pos.x == 0 && pos.pos.y == 0)
			return;
		Graphics2D g = (Graphics2D) debugGraphics.create();
		g.translate(pos.pos.x, pos.pos.y);
		g.rotate(Math.toRadians(pos.angle + 90));
		g.setColor(Color.RED);
		g.drawOval(-2, -2, 5, 5);
		g.drawLine(0, -15, 0, -2);
		// Draw an arrow
		int[] xPoints = { -3, 3, 0 };
		int[] yPoints = { 0, 0, -7 };
		g.translate(0, -14);
		g.fillPolygon(xPoints, yPoints, xPoints.length);
		g.dispose();
	}
	
	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay,
			StaticWorldState result) {
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
		drawRobotPos(debugGraphics, yellowAtk);
		drawRobotPos(debugGraphics, yellowDef);
		drawRobotPos(debugGraphics, blueAtk);
		drawRobotPos(debugGraphics, blueDef);

		// Reset to default
		blueAtkNotOnPitch = false;
		blueDefNotOnPitch = false;
		yellowAtkNotOnPitch = false;
		yellowDefNotOnPitch = false;
		
		// Determine if the plates are on the pitch or not.
		if (blueAtk.pos.x == 0 && blueAtk.pos.y == 0) {
			blueAtk = blueAtkPrev;
			blueAtkNotOnPitch = true;
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
			blueDefNotOnPitch = true;
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
			yellowAtkNotOnPitch = true;
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
			yellowDefNotOnPitch = true;
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
			worldState.attackerNotOnPitch = blueAtkNotOnPitch;
			worldState.defenderNotOnPitch = blueDefNotOnPitch;
			worldState.enemyAttackerNotOnPitch = yellowAtkNotOnPitch;
			worldState.enemyDefenderNotOnPitch = yellowDefNotOnPitch;
		} else {
			attackerRobot = new MovingObject(yellowAtk.pos.x, yellowAtk.pos.y,
					yellowAtk.angle);
			defenderRobot = new MovingObject(yellowDef.pos.x, yellowDef.pos.y,
					yellowDef.angle);
			enemyAttackerRobot = new MovingObject(blueAtk.pos.x, blueAtk.pos.y,
					blueAtk.angle);
			enemyDefenderRobot = new MovingObject(blueDef.pos.x, blueDef.pos.y,
					blueDef.angle);
			worldState.attackerNotOnPitch = yellowAtkNotOnPitch;
			worldState.defenderNotOnPitch = yellowDefNotOnPitch;
			worldState.enemyAttackerNotOnPitch = blueAtkNotOnPitch;
			worldState.enemyDefenderNotOnPitch = blueDefNotOnPitch;
		}

		worldState.setAttackerRobot(attackerRobot);
		worldState.setDefenderRobot(defenderRobot);
		worldState.setEnemyAttackerRobot(enemyAttackerRobot);
		worldState.setEnemyDefenderRobot(enemyDefenderRobot);

		result.setAttacker(movingObjectToPoint(attackerRobot));
		result.setDefender(movingObjectToPoint(defenderRobot));
		result.setEnemyAttacker(movingObjectToPoint(enemyAttackerRobot));
		result.setEnemyDefender(movingObjectToPoint(enemyDefenderRobot));
	}

	private DirectedPoint movingObjectToPoint(MovingObject movObj) {
		Point2D pt = movObj.asPoint();
		pitch.framePointToModel(pt);
		return new DirectedPoint((int) pt.getX(), (int) pt.getY(),
				Math.toRadians(movObj.orientation_angle));
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

	public static class ViewProvider implements PitchViewProvider {
		private DynamicWorldState dynamicWorldState;
		private Pitch pitch;

		private static final Stroke EXTENTS_STROKE = new BasicStroke(1,
				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1,
				new float[] { 10 }, 10);

		public ViewProvider(DynamicWorldState dynamicWorldState, Pitch pitch) {
			this.dynamicWorldState = dynamicWorldState;
			this.pitch = pitch;
		}

		private void drawRobot(DirectedPoint pos, Color color,
				Graphics2D graphics, RobotModel robotModel) {
			if (pos == null)
				return;
			Graphics2D g = (Graphics2D) graphics.create();
			g.setColor(color);
			g.translate(pos.getX(), pos.getY());
			g.rotate(pos.getDirection() + Math.PI / 2);

			g.setStroke(new BasicStroke(3));
			Rectangle r = robotModel.getPlate();
			g.drawRect(r.x, r.y, r.width, r.height);

			g.translate(r.getCenterX(), r.getCenterY());

			r = robotModel.getCatcher();
			g.setColor(Color.WHITE);
			g.drawRect(r.x, r.y, r.width, r.height);

			g.setStroke(EXTENTS_STROKE);
			r = robotModel.getExtents();
			g.drawRect(r.x, r.y, r.width, r.height);

			g.dispose();
		}

		@Override
		public void drawOnPitch(Graphics2D g) {
			drawRobot(dynamicWorldState.getAttacker(), Color.GREEN, g,
					RobotModel.GENERIC_ROBOT);
			drawRobot(dynamicWorldState.getDefender(), Color.YELLOW, g,
					RobotModel.GENERIC_ROBOT);
			drawRobot(dynamicWorldState.getEnemyAttacker(), Color.DARK_GRAY, g,
					RobotModel.GENERIC_ROBOT);
			drawRobot(dynamicWorldState.getEnemyDefender(), Color.DARK_GRAY, g,
					RobotModel.GENERIC_ROBOT);
		}
	}
}
