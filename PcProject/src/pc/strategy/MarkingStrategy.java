package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.strategy.TargetFollowerStrategy.Operation;
import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

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
public class MarkingStrategy implements Strategy {

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
		float robotX = worldState.getAttackerRobot().x;
		float robotY = worldState.getAttackerRobot().y;
		float robotO = worldState.getAttackerRobot().orientation_angle;
		float enemyDefenderRobotX = worldState.getEnemyDefenderRobot().x;
		float enemyDefenderRobotY = worldState.getEnemyDefenderRobot().y;
		float enemyAttackerRobotX = worldState.getEnemyAttackerRobot().x;
		float enemyAttackerRobotY = worldState.getEnemyAttackerRobot().y;

		// Calculate the midpoint
		float targetX = (enemyAttackerRobotX + enemyDefenderRobotX) / 2;
		float targetY = (enemyAttackerRobotY + enemyDefenderRobotY) / 2;

		int[] divs = worldState.dividers;
		int leftCheck, rightCheck;

		if (worldState.weAreShootingRight) {
			leftCheck = divs[1];
			rightCheck = divs[2];
		} else {
			leftCheck = divs[0];
			rightCheck = divs[1];
		}

		if (robotX <= 0.5 || targetY <= 0.5 || robotY <= 0.5 || robotO <= 0.5
				|| targetX < leftCheck || targetX > rightCheck) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		double robotToTargetAngle = calculateAngle(robotX, robotY, robotO,
				targetX, targetY);
		double robotToTargetDistance = Math.hypot(targetX - robotX, targetY
				- robotY);

		synchronized (this.controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			
			if (Math.abs(robotToTargetDistance) > 25) {
				if (Math.abs(robotToTargetAngle) < 90) {
					controlThread.travelDist = (int) robotToTargetDistance;
				} else {
					controlThread.travelDist = (int) -robotToTargetDistance;
				}
				if (Math.abs(robotToTargetAngle) > 150
						|| Math.abs(robotToTargetAngle) < 10) {
					controlThread.operation = Operation.TRAVEL;
				} else if (robotToTargetAngle > 0) {
					controlThread.operation = Operation.ARC_RIGHT;
					if (robotToTargetAngle > 90) {
						controlThread.operation = Operation.ARC_LEFT;
					}
					controlThread.radius = robotToTargetDistance * 5;
				} else if (robotToTargetAngle < 0) {
					controlThread.operation = Operation.ARC_LEFT;
					if (robotToTargetAngle < -90) {
						controlThread.operation = Operation.ARC_RIGHT;
					}
					controlThread.radius = robotToTargetDistance *5;
				}

				controlThread.travelSpeed = (int) (200);
			} else {
				controlThread.operation = Operation.DO_NOTHING;
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, ARC_LEFT, ARC_RIGHT,
	}

	private class ControlThread extends Thread {
		public double radius = 0;
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;

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
					case TRAVEL:
						MarkingStrategy.this.brick.robotTravel(travelDist,
								travelSpeed);
						break;
					case ARC_LEFT:
						brick.robotArcForwards(radius, travelDist);
						break;
					case ARC_RIGHT:
						brick.robotArcForwards(-radius, travelDist);
						break;
					default:
						break;
					}

					// TODO Try lower values and see when it breaks
					// TODO Maybe this should be defined as a constant?
					Thread.sleep(250);
				}
			} catch (IOException e) {
				e.printStackTrace();
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
