package pc.world;

public class WorldState {

	int ballX, ballY;
	int blueX, blueY;
	int greenX, greenY;
	int yellowX, yellowY;
	
	int robotTargetX, robotTargetY;
	double moveX, moveY, moveR;
	
	double ballXVelocity, ballYVelocity;
	double blueXVelocity, blueYVelocity, blueOrientation;
	double yellowXVelocity, yellowYVelocity, yellowOrientation;
	public int getBallX() {
		return ballX;
	}
	public void setBallX(int ballX) {
		this.ballX = ballX;
	}
	public int getBallY() {
		return ballY;
	}
	public void setBallY(int ballY) {
		this.ballY = ballY;
	}
	public int getBlueX() {
		return blueX;
	}
	public void setBlueX(int blueX) {
		this.blueX = blueX;
	}
	public int getBlueY() {
		return blueY;
	}
	public void setBlueY(int blueY) {
		this.blueY = blueY;
	}
	public int getGreenX() {
		return greenX;
	}
	public void setGreenX(int greenX) {
		this.greenX = greenX;
	}
	public int getGreenY() {
		return greenY;
	}
	public void setGreenY(int greenY) {
		this.greenY = greenY;
	}
	public int getYellowX() {
		return yellowX;
	}
	public void setYellowX(int yellowX) {
		this.yellowX = yellowX;
	}
	public int getYellowY() {
		return yellowY;
	}
	public void setYellowY(int yellowY) {
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
	 * Constructor, use it for inital world model initialization once
	 * the play field data has been assembled
	 */
	WorldState(Pitch field){
		this.playingField = field;
	}
	
	/** Constructor, use it for inital world model initialization once
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
	
	/**
	 * Returns the enemy defender robot object
	 * returns null if the model is locked 
	 */
	public MovingObject GetEnemyDefenderRobot(){
		return this.enemyDefenderRobot;
	}
	
	/**
	 * Returns the enemy attacker robot object
	 * returns null if the model is locked 
	 */
	public MovingObject GetEnemyAttackerRobot(){
		return this.enemyAttackerRobot;
	}
	
	/**
	 * Returns the defender robot object
	 * returns null if the model is locked 
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
	 * returns null if the model is locked 
	 */
	public MovingObject GetBall(){
		return this.ball;
	}
	
	 /** Returns the pitch object
	 * returns null if the model is locked 
	 */
	public Pitch GetPitch(){
		return this.playingField;		
	}
	
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

	
	//#endregion
	

}
