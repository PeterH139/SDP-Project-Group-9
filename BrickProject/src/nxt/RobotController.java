package nxt;

import nxt.brick.Keeper;
import nxt.brick.Movement;
import nxt.brick.Striker;

public class RobotController {
	private Movement movementController;
	
	public RobotController(boolean isKeeper) {
		
		if (isKeeper) {
			movementController = new Keeper();
		} else {
			movementController = new Striker();
		}
	}
	
	public Movement getMovementController() {
		return movementController;
	}
}
