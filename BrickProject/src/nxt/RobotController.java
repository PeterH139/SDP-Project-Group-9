package nxt;

import nxt.brick.Keeper;
import nxt.brick.Movement;

public class RobotController {
	private Movement movementController;
	
	public RobotController() {
		movementController = new Keeper();
	}
	
	public Movement getMovementController() {
		return movementController;
	}
}
