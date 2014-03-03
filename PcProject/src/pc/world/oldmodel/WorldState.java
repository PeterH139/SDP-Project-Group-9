/**
 * Name: WorldState.java
 * Description: This class represents a the playing field
 * Most of the methods and properties are kept for legacy purposes
 * The robots and ball are represented by MovingObject classes
 */
package pc.world.oldmodel;

import pc.world.Pitch;

public class WorldState {
	
	//XXX: Temporary for testing.
	public int[] dividers; 
	public float[] leftGoal;
	public float[] rightGoal;
	public boolean attackerNotOnPitch, enemyAttackerNotOnPitch, defenderNotOnPitch, enemyDefenderNotOnPitch;

	// #region new world model representation
	// TODO: convert x/y to mm values
	// TODO: center to be the center of the field
	// Our team robots
	// Defender
	private MovingObject defenderRobot;
	// Attacker
	private MovingObject attackerRobot;

	// Enemy team robots
	// Defender
	private MovingObject enemyDefenderRobot;
	// Attacker
	private MovingObject enemyAttackerRobot;

	// Ball
	private MovingObject ball;

	// Pitch
	private Pitch playingField;

	// #Peter: added these for use in the GUI.
	// Flags
	public boolean weAreBlue, weAreShootingRight;

	/**
	 * Added for legacy purposes
	 * */
	public WorldState() {
	}

	/**
	 * Constructor, use it for initial world model initialization once the play
	 * field data has been assembled
	 */
	public WorldState(Pitch field) {
		this.playingField = field;
	}

	/**
	 * Constructor, use it for initial world model initialization once the
	 * playing field data has been assembled
	 */
	public WorldState(Pitch field, MovingObject defenderRobot,
			MovingObject attackerRobot, MovingObject enemyDefenderRobot,
			MovingObject enemyAttackerRobot, MovingObject ball) {
		this.playingField = field;
		this.defenderRobot = defenderRobot;
		this.attackerRobot = attackerRobot;
		this.enemyAttackerRobot = enemyAttackerRobot;
		this.enemyDefenderRobot = enemyDefenderRobot;
		this.ball = ball;
	}

	// get methods
	/**
	 * Returns the enemy defender robot object
	 */
	public MovingObject getEnemyDefenderRobot() {
		return this.enemyDefenderRobot;
	}

	/**
	 * Returns the enemy attacker robot object
	 */
	public MovingObject getEnemyAttackerRobot() {
		return this.enemyAttackerRobot;
	}

	/**
	 * Returns the defender robot object
	 */
	public MovingObject getDefenderRobot() {
		return this.defenderRobot;
	}

	/**
	 * Returns the attacker robot object returns null if the model is locked
	 */
	public MovingObject getAttackerRobot() {
		return this.attackerRobot;
	}

	/**
	 * Returns the ball object
	 */
	public MovingObject getBall() {
		return this.ball;
	}

	/**
	 * Returns the pitch object
	 */
	public Pitch getPitch() {
		return this.playingField;
	}

	public boolean getPossession() {
		if (Math.abs(attackerRobot.x - ball.x) < 50
				&& Math.abs(attackerRobot.y - ball.y) < 50) {
			return true;
		} else if (Math.abs(defenderRobot.x - ball.x) < 50
				&& Math.abs(defenderRobot.y - ball.y) < 50) {
			return true;
		}
		return false;
	}

	// update methods
	/**
	 * Updates the field with data for moving objects: the robots and the ball
	 */
	public void updateField(MovingObject enemyAttackerRobot,
			MovingObject enemyDefenderRobot, MovingObject attackerRobot,
			MovingObject defenderRobot, MovingObject ball) {
		// the actual update
		this.defenderRobot = defenderRobot;
		this.attackerRobot = attackerRobot;
		this.enemyAttackerRobot = enemyAttackerRobot;
		this.enemyDefenderRobot = enemyDefenderRobot;
		this.ball = ball;
	}

	/**
	 * Updates the ball object
	 * */
	public void setBall(MovingObject ball) {
		this.ball = ball;
	}

	/**
	 * Updates the enemy attacker robot object
	 * */
	public void setEnemyAttackerRobot(MovingObject enemyAttackerRobot) {
		this.enemyAttackerRobot = enemyAttackerRobot;
	}

	/**
	 * Updates the enemy defender robot object
	 * */
	public void setEnemyDefenderRobot(MovingObject enemyDefenderRobot) {
		this.enemyDefenderRobot = enemyDefenderRobot;
	}

	/**
	 * Updates the attacker robot object
	 * */
	public void setAttackerRobot(MovingObject attackerRobot) {
		this.attackerRobot = attackerRobot;
	}

	/**
	 * Updates the defender robot object
	 * */
	public void setDefenderRobot(MovingObject defenderRobot) {
		this.defenderRobot = defenderRobot;
	}

	// #endregion

}
