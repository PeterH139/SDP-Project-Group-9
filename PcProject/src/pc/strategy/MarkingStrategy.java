package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.GeneralStrategy.Operation;
import pc.strategy.interfaces.Strategy;
import pc.world.oldmodel.WorldState;

/**
 * This is a strategy to "mark" the enemy attacker when their defender has the
 * ball, this will hopefully allow us to block any passes made by the enemy
 * defender to their attacker
 * 
 * It currently aims for the midpoint between the two robots
 * 
 * @author Daniel
 * 
 */
public class MarkingStrategy extends GeneralStrategy {

	private BrickCommServer brick;
	private ControlThread controlThread;

	public MarkingStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		this.controlThread.stop();
	}

	@Override
	public void startControlThread() {
		this.controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
//		System.out.println("Marking");
		float robotX = worldState.getAttackerRobot().x;
		float robotY = worldState.getAttackerRobot().y;
		float robotO = worldState.getAttackerRobot().orientation_angle;
		float enemyDefenderRobotY = worldState.getEnemyDefenderRobot().y;
		float enemyAttackerRobotY = worldState.getEnemyAttackerRobot().y;

		int[] divs = worldState.dividers;
		int leftCheck, rightCheck;

		if (worldState.weAreShootingRight) {
			leftCheck = divs[1];
			rightCheck = divs[2];

			// targetX = (2 * leftCheck + rightCheck) / 3;
		} else {
			leftCheck = divs[0];
			rightCheck = divs[1];

			// targetX = (leftCheck + 2 * rightCheck) / 3;
		}
		float targetX = (leftCheck + rightCheck) / 2;
		float targetY = (enemyAttackerRobotY + enemyDefenderRobotY) / 2;

		double robotToTargetAngle = calculateAngle(robotX, robotY, robotO,
				targetX, targetY);
		double robotToTargetDistance = Math.hypot(targetX - robotX, targetY
				- robotY);

		synchronized (this.controlThread) {
			this.controlThread.operation = Operation.DO_NOTHING;

			if (Math.abs(robotToTargetDistance) > 15) {
				
				//Decide if we'll go forward or backwards
				if (Math.abs(robotToTargetAngle) < 90) {
					this.controlThread.travelDist = (int) robotToTargetDistance;
				} else {
					this.controlThread.travelDist = (int) -robotToTargetDistance;
				}
				
				//Decide if we need to go in a straight line or arc to the target
				if (Math.abs(robotToTargetAngle) > 45) {
					controlThread.operation= Operation.ROTATE;
					controlThread.rotateBy = (int) -robotToTargetAngle;
				}
				else if (Math.abs(robotToTargetAngle) > 150 || Math.abs(robotToTargetAngle) < 10) {
					//Go in a straight line
					this.controlThread.operation = Operation.TRAVEL;
					//System.out.println("Straight line: " + targetY);
				} //Now we decide if we need to go left or right 
				else if (robotToTargetAngle > 10) {
					this.controlThread.operation = Operation.ARC_LEFT;
					if (robotToTargetAngle > 90) {
						//We're going backwards, so reverse the direction
						this.controlThread.operation = Operation.ARC_RIGHT;
					}
					this.controlThread.radius = robotToTargetDistance * 10;
				} else if (robotToTargetAngle < 10) {
					this.controlThread.operation = Operation.ARC_RIGHT;
					if (robotToTargetAngle < -90) {
						//We're going backwards, so reverse the direction
						this.controlThread.operation = Operation.ARC_LEFT;
					}
					this.controlThread.radius = robotToTargetDistance * 10;
				}

				this.controlThread.travelSpeed = (int) (200);
			} else {
				if (robotO >= 0 && robotO <= 85) {
					this.controlThread.operation = Operation.ROTATE;
					this.controlThread.rotateBy = (int) (90 - robotO);
				//	System.out.println("Rotating to align");
				} else if (robotO >= 95 && robotO <= 179) {
					this.controlThread.operation = Operation.ROTATE;
					this.controlThread.rotateBy = (int) -(90 - robotO);
				//	System.out.println("Rotating to align");
				} else if (robotO >= 180 && robotO <= 265) {
					this.controlThread.operation = Operation.ROTATE;
					this.controlThread.rotateBy = (int) (270 - robotO);
				//	System.out.println("Rotating to align");
				} else if (robotO >= 275 && robotO <= 360) {
					this.controlThread.operation = Operation.ROTATE;
					this.controlThread.rotateBy = (int) -(270 - robotO);
				//	System.out.println("Rotating to align");
				} else {
					this.controlThread.operation = Operation.DO_NOTHING;
				}
			}
			if (ballCaughtAttacker && (Math.hypot(ballX - attackerRobotX, ballY - attackerRobotY) > 45)) {
				controlThread.operation = Operation.ATKKICK;
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, ARC_LEFT, ARC_RIGHT, ATKKICK
	}

	private class ControlThread extends Thread {
		public double radius = 0;
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;
		
		private long lastKickerEventTime = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int travelDist, rotateBy, travelSpeed;
					Operation op;
					double radius;
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						radius = this.radius;
					}


					switch (op) {
					case DO_NOTHING:

						break;
					case ATKKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 500) {
							brick.execute(new RobotCommand.Kick(100));
							ballCaughtAttacker = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case TRAVEL:
						brick.executeSync(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					case ARC_LEFT:
						brick.executeSync(new RobotCommand.TravelArc(radius, travelDist, travelSpeed));
						break;
					case ARC_RIGHT:
						brick.executeSync(new RobotCommand.TravelArc(-radius, travelDist, travelSpeed));
						break;
					case ROTATE:
						brick.executeSync(new RobotCommand.Rotate(-rotateBy, Math.abs(rotateBy)));
						break;
					default:
						break;
					}

					// TODO Try lower values and see when it breaks
					// TODO Maybe this should be defined as a constant?
					Thread.sleep(250);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public static double calculateAngle(float robotX, float robotY,
			float robotOrientation, float targetX, float targetY) {
		double robotRad = Math.toRadians(robotOrientation);
		double targetRad = Math.atan2(targetY - robotY, targetX - robotX);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang1 = targetRad - robotRad;
		while (ang1 > Math.PI)
			ang1 -= 2 * Math.PI;
		while (ang1 < -Math.PI)
			ang1 += 2 * Math.PI;
		return Math.toDegrees(ang1);
	}
}
