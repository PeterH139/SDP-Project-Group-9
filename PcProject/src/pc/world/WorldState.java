/**
 * Name: WorldState.java
 * Description: This class represents a the playing field
 * Most of the methods and properties are kept for legacy purposes
 * The robots and ball are represented by MovingObject classes
 */
package pc.world;

public class WorldState {

	float ballX, ballY;
	float blueX, blueY;
	float greenX, greenY;
	float yellowX, yellowY;
	
	int robotTargetX, robotTargetY;
	double moveX, moveY, moveR;
	
	double ballXVelocity, ballYVelocity;
	double blueXVelocity, blueYVelocity, blueOrientation;
	double yellowXVelocity, yellowYVelocity, yellowOrientation;	
	public float getBallX() {
		return ballX;
	}
	public void setBallX(float ballX) {
		this.ballX = ballX;
	}
	public float getBallY() {
		return ballY;
	}
	public void setBallY(float ballY) {
		this.ballY = ballY;
	}
	public float getBlueX() {
		return blueX;
	}
	public void setBlueX(float blueX) {
		this.blueX = blueX;
	}
	public float getBlueY() {
		return blueY;
	}
	public void setBlueY(float blueY) {
		this.blueY = blueY;
	}
	public float getGreenX() {
		return greenX;
	}
	public void setGreenX(float greenX) {
		this.greenX = greenX;
	}
	public float getGreenY() {
		return greenY;
	}
	public void setGreenY(float greenY) {
		this.greenY = greenY;
	}
	public float getYellowX() {
		return yellowX;
	}
	public void setYellowX(float yellowX) {
		this.yellowX = yellowX;
	}
	public float getYellowY() {
		return yellowY;
	}
	public void setYellowY(float yellowY) {
		this.yellowY = yellowY;
	}
	public double getBallXVelocity() {
		return ballXVelocity;
	}
	public void setBallXVelocity(double ballXVelocity) {
		this.ballXVelocity = ballXVelocity;
	}
	public double getBallYVelocity() {
		return ballYVelocity;
	}
	public void setBallYVelocity(double ballYVelocity) {
		this.ballYVelocity = ballYVelocity;
	}
	public double getBlueXVelocity() {
		return blueXVelocity;
	}
	public void setBlueXVelocity(double blueXVelocity) {
		this.blueXVelocity = blueXVelocity;
	}
	public double getBlueYVelocity() {
		return blueYVelocity;
	}
	public void setBlueYVelocity(double blueYVelocity) {
		this.blueYVelocity = blueYVelocity;
	}
	public double getBlueOrientation() {
		return blueOrientation;
	}
	public void setBlueOrientation(double blueOrientation) {
		this.blueOrientation = blueOrientation;
	}
	public double getYellowXVelocity() {
		return yellowXVelocity;
	}
	public void setYellowXVelocity(double yellowXVelocity) {
		this.yellowXVelocity = yellowXVelocity;
	}
	public double getYellowYVelocity() {
		return yellowYVelocity;
	}
	public void setYellowYVelocity(double yellowYVelocity) {
		this.yellowYVelocity = yellowYVelocity;
	}
	public double getYellowOrientation() {
		return yellowOrientation;
	}
	public void setYellowOrientation(double yellowOrientation) {
		this.yellowOrientation = yellowOrientation;
	}
	public int getRobotTargetX() {
		return robotTargetX;
	}
	public void setRobotTargetX(int robotTargetX) {
		this.robotTargetX = robotTargetX;
	}
	public int getRobotTargetY() {
		return robotTargetY;
	}
	public void setRobotTargetY(int robotTargetY) {
		this.robotTargetY = robotTargetY;
	}
	public double getMoveX() {
		return moveX;
	}
	public void setMoveX(double moveX) {
		this.moveX = moveX;
	}
	public double getMoveY() {
		return moveY;
	}
	public void setMoveY(double moveY) {
		this.moveY = moveY;
	}
	public double getMoveR() {
		return moveR;
	}
	public void setMoveR(double moveR) {
		this.moveR = moveR;
	}
	//#region new world model representation
	//TODO: convert x/y to mm values
	//TODO: center to be the center of the field
	//Our team robots
	//Defender
	private MovingObject defenderRobot;
	//Attacker
	private MovingObject attackerRobot;
	
	//Enemy team robots
	//Defender
	private MovingObject enemyDefenderRobot;
	//Attacker
	private MovingObject enemyAttackerRobot;
	
	//Ball
	private MovingObject ball;
	
	//Pitch
	private Pitch playingField;
	
	/**
	 * Added for legacy purposes
	 * */
	public WorldState(){	
		
	}
	
	/**
	 * Constructor, use it for initial world model initialization once
	 * the play field data has been assembled
	 */
	WorldState(Pitch field){
		this.playingField = field;
	}
	
	/** Constructor, use it for initial world model initialization once
	 * the playing field data has been assembled
	 */
	WorldState(Pitch field, MovingObject defenderRobot, MovingObject attackerRobot, MovingObject enemyDefenderRobot, MovingObject enemyAttackerRobot, MovingObject ball){
		this.playingField = field;
		this.defenderRobot = defenderRobot;
		this.attackerRobot = attackerRobot;
		this.enemyAttackerRobot = enemyAttackerRobot;
		this.enemyDefenderRobot = enemyDefenderRobot;
		this.ball = ball;
	}
	//get methods
	/**
	 * Returns the enemy defender robot object
	 */
	public MovingObject GetEnemyDefenderRobot(){
		return this.enemyDefenderRobot;
	}
	
	/**
	 * Returns the enemy attacker robot object
	 */
	public MovingObject GetEnemyAttackerRobot(){
		return this.enemyAttackerRobot;
	}
	
	/**
	 * Returns the defender robot object
	 */
	public MovingObject GetDefenderRobot(){
		return this.defenderRobot;
	}
	
	/**
	 * Returns the attacker robot object
	 * returns null if the model is locked 
	 */
	public MovingObject GetAttackerRobot(){
		return this.attackerRobot;
	}
	
	/**
	 * Returns the ball object
	 */
	public MovingObject GetBall(){
		return this.ball;
	}
	
	 /** Returns the pitch object
	 */
	public Pitch GetPitch(){
		return this.playingField;		
	}
	
	public boolean GetPossession() {
		if (Math.abs(attackerRobot.x - ball.x) < 50 && Math.abs(attackerRobot.y - ball.y) < 50) {
			return true;
		} else if (Math.abs(defenderRobot.x - ball.x) < 50 && Math.abs(defenderRobot.y - ball.y) < 50) {
			return true;
		}
		return false;
	}
	
	//update methods
	/**
	 * Updates the field with data for moving objects: the robots and the ball
	 */
	public void UpdateField(MovingObject enemyAttackerRobot, MovingObject enemyDefenderRobot, MovingObject attackerRobot, MovingObject defenderRobot, MovingObject ball){
		//the actual update
		this.defenderRobot = defenderRobot;
		this.attackerRobot = attackerRobot;
		this.enemyAttackerRobot = enemyAttackerRobot;
		this.enemyDefenderRobot = enemyDefenderRobot;
		this.ball = ball;
	}
	/**
	 * Updates the ball object
	 * */
    public void SetBall(MovingObject ball){
    	this.ball = ball;    	
    }
    /**
	 * Updates the enemy attacker robot object
	 * */
    public void SetEnemyAttackerRobot(MovingObject enemyAttackerRobot){
    	this.enemyAttackerRobot = enemyAttackerRobot;    	
    }
    /**
	 * Updates the enemy defender robot object
	 * */
    public void SetEnemyDefenderRobot(MovingObject enemyDefenderRobot){
    	this.enemyDefenderRobot = enemyDefenderRobot;    	
    }
    /**
	 * Updates the attacker robot object
	 * */
    public void SetAttackerRobot(MovingObject attackerRobot){
    	this.attackerRobot = attackerRobot;    	
    }
    /**
	 * Updates the defender robot object
	 * */
    public void SetDefenderRobot(MovingObject defenderRobot){
    	this.defenderRobot = defenderRobot;    	
    }
    
	
	//#endregion
	

}
