package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.PitchConstants;
import pc.vision.Position;
import pc.vision.Velocity;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.WorldState;
import pc.world.*;

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
	public void processFrame(BufferedImage frame, Graphics2D debugGraphics,
			BufferedImage debugOverlay) {
		ArrayList<Position> baPoints = new ArrayList<Position>(); // Blue
																	// Attacker
		ArrayList<Position> bdPoints = new ArrayList<Position>(); // Blue
																	// Defender
		ArrayList<Position> yaPoints = new ArrayList<Position>(); // Yellow
																	// Attacker
		ArrayList<Position> ydPoints = new ArrayList<Position>(); // Yellow
																	// Defender
		ArrayList<Position> ballPointsUnused = new ArrayList<Position>();

		int leftBuffer = this.pitchConstants.getLeftBuffer();
		int rightBuffer = this.pitchConstants.getRightBuffer();
		int[] dividers = this.pitchConstants.getDividers();

		// Detect the X,Y coords and rotations of the plates in each section,
		// also look for the points of the ball while looping over the frame to
		// save time
		boolean leftBlueFirst = true; // TODO: calculate this from the
										// appropriate location
		if (leftBlueFirst) {
			// In order, ltr: Blue Defender, Yellow Attacker, Blue Attacker,
			// Yellow Defender
			vision.searchColumn(bdPoints, ballPointsUnused, frame,
					debugOverlay, leftBuffer, dividers[0], true);
			vision.searchColumn(yaPoints, ballPointsUnused, frame,
					debugOverlay, dividers[0], dividers[1], false);
			vision.searchColumn(baPoints, ballPointsUnused, frame,
					debugOverlay, dividers[1], dividers[2], true);
			vision.searchColumn(ydPoints, ballPointsUnused, frame,
					debugOverlay, dividers[2], frame.getWidth() - rightBuffer,
					false);
		} else {
			// In order, ltr: Yellow Defender, Blue Attacker, Yellow Attacker,
			// Blue Defender
			vision.searchColumn(ydPoints, ballPointsUnused, frame,
					debugOverlay, leftBuffer, dividers[0], false);
			vision.searchColumn(baPoints, ballPointsUnused, frame,
					debugOverlay, dividers[0], dividers[1], true);
			vision.searchColumn(yaPoints, ballPointsUnused, frame,
					debugOverlay, dividers[1], dividers[2], false);
			vision.searchColumn(bdPoints, ballPointsUnused, frame,
					debugOverlay, dividers[2], frame.getWidth() - rightBuffer,
					true);
		}

		// Calculate the mean position of the points for each robot and the
		// ball.
		Position blueDef, blueAtk, yellowDef, yellowAtk;
		blueDef = vision.calculatePosition(bdPoints);
		blueAtk = vision.calculatePosition(baPoints);
		yellowDef = vision.calculatePosition(ydPoints);
		yellowAtk = vision.calculatePosition(yaPoints);
		// Debugging Graphics
		debugGraphics.setColor(Color.CYAN);
		debugGraphics.drawRect(blueDef.getX() - 2, blueDef.getY() - 2, 4, 4);
		debugGraphics.drawRect(blueAtk.getX() - 2, blueAtk.getY() - 2, 4, 4);
		debugGraphics.drawRect(yellowDef.getX() - 2, yellowDef.getY(), 4, 4);
		debugGraphics.drawRect(yellowAtk.getX() - 2, yellowAtk.getY(), 4, 4);

		// TODO: Using the previous position values and the time between frames,
		// calculate the velocities of the robots and the ball.
		// Can be completed once Dimitar has implemented the world model section
		// - Peter
		// **Temp code below**
		/*Velocity blueDefVel, blueAtkVel, yellowDefVel, yellowAtkVel, ballVel;
		float xdiff = blueAtk.getX() - worldState.getBlueX();
		float ydiff = blueAtk.getY() - worldState.getBlueY();
		blueAtkVel = new Velocity(xdiff / delta, ydiff / delta);*/
		// System.out.println("x: "+ blueAtkVel.getX() + " y: " +
		// blueAtkVel.getY());

		// Calculate the rotation of each of the robots.
		float blueDefAngle, blueAtkAngle, yellowDefAngle, yellowAtkAngle;
		blueDefAngle = vision.calculateAngle(frame, debugOverlay, blueDef, 14);
		blueAtkAngle = vision.calculateAngle(frame, debugOverlay, blueAtk, 14);
		yellowDefAngle = vision.calculateAngle(frame, debugOverlay, yellowDef, 14);
		yellowAtkAngle = vision.calculateAngle(frame, debugOverlay, yellowAtk, 14);
		// TODO: Update the world state with the new values for position,
		// velocity and rotation.
		// **RELATIVE TO THE ORIGIN FOR POSITION**
		// The following code is temporary until Dimitar finished the world
		// model section - Peter
		
		worldState.setBlueX(blueAtk.getX());
		worldState.setBlueY(blueAtk.getY());
		worldState.setBlueOrientation(blueAtkAngle);
		//worldState.setBlueXVelocity(blueAtkVel.getX());
		//worldState.setBlueYVelocity(blueAtkVel.getY());
		worldState.setYellowX(yellowDef.getX());
		worldState.setYellowY(yellowDef.getY());
		worldState.setYellowOrientation(yellowDefAngle);
		// #Dimitar TODO: further code changes needed! the robots need to be correctly 
		//identified based on the sections of the field they are in.
		//right now I assume that the yellow is our team and the
		//blue is the enemy team
		MovingObject attackerRobot = new MovingObject(yellowAtk.getX(),yellowAtk.getY(),yellowAtkAngle);
		MovingObject defenderRobot = new MovingObject(yellowDef.getX(),yellowDef.getY(),yellowDefAngle);
		
		MovingObject enemyAttackerRobot = new MovingObject(blueAtk.getX(),blueAtk.getY(),blueAtkAngle);
		MovingObject enemyDefenderRobot = new MovingObject(blueDef.getX(),blueDef.getY(),blueDefAngle);
		
		worldState.SetAttackerRobot(attackerRobot);
		worldState.SetDefenderRobot(defenderRobot);
		worldState.SetEnemyAttackerRobot(enemyAttackerRobot);
		worldState.SetEnemyDefenderRobot(enemyDefenderRobot);
	}

}
