package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
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
		
		//Calculate the midpoint
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
		

		if (robotX <= 0.5 || targetY <= 0.5 || robotY <= 0.5
				|| robotO <= 0.5 || targetX < leftCheck || targetX > rightCheck) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}
		
		double robotToTargetAngle = calculateAngle(robotX, robotY, robotO, targetX,
				targetY);
		double robotToTargetDistance = Math.hypot(robotX - targetX, robotY - targetY);
		
		
		synchronized (this.controlThread) {
			if (Math.abs(robotToTargetAngle) > Math.PI / 35) {
				this.controlThread.operation = Operation.ROTATE;
				this.controlThread.rotateBy = (int) Math.toDegrees(robotToTargetAngle);
			} else if (robotToTargetDistance > 20) {
				this.controlThread.operation = Operation.TRAVEL;
				this.controlThread.travelDist = (int) (robotToTargetDistance * 3);
				this.controlThread.travelSpeed = (int) (robotToTargetDistance * 4);
			} else {
				this.controlThread.operation = Operation.DO_NOTHING;
			}

		}
	}
	
	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE,
	}

	private class ControlThread extends Thread {
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
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
					}

//					System.out.println("op: " + op.toString() + " rotateBy: "
//							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						
						break;
					case ROTATE:
						MarkingStrategy.this.brick.robotRotateBy(rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						MarkingStrategy.this.brick.robotTravel(travelDist, travelSpeed);
						break;
					default:
						break;
					}
					 
					//TODO Try lower values and see when it breaks
					//TODO Maybe this should be defined as a constant?
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
		return ang1;
	}
}

