package nxt;

import nxt.brick.Movement;
import nxt.brick.Striker;

public class RobotController {
	private Movement movementController;
	
	public RobotController() {
		movementController = new Striker();
	}
	
	public Movement getMovementController() {
		return movementController;
	}
}
