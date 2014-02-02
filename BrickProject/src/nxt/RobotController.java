package nxt;

import nxt.brick.Movement;
import nxt.brick.Striker;

public class RobotController {
	private Striker movementController;
	
	public RobotController() {
		movementController = new Striker();
	}
	
	public Striker getMovementController() {
		return movementController;
	}
}
