package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.strategy.TargetFollowerStrategy.Operation;
import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

public class AttackerStrategy implements Strategy {

	private BrickCommServer brick;
	private ControlThread controlThread;

	private boolean ballCaught = false;

	public AttackerStrategy(BrickCommServer brick) {
		this.brick = brick;
		controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		controlThread.stop();
	}

	@Override
	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		System.out.println("Attacking");
		float robotX = worldState.getAttackerRobot().x, robotY = worldState
				.getAttackerRobot().y;
		float robotO = worldState.getAttackerRobot().orientation_angle;
		float targetX = worldState.getBall().x, targetY = worldState.getBall().y;
		int leftCheck, rightCheck;
		float goalX, goalY;
		int[] divs = worldState.dividers;
		if (worldState.weAreShootingRight) {
			leftCheck = divs[1];
			rightCheck = divs[2];
			goalX = 640;
			goalY = 220;
		} else {
			leftCheck = divs[0];
			rightCheck = divs[1];
			goalX = 0;
			goalY = 220;
		}

		if (targetX < leftCheck || targetX > rightCheck) {
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (controlThread) {
			controlThread.operation = Operation.PREPARE_CATCH;
			if (!ballCaught) {
				double ang1 = calculateAngle(robotX, robotY, robotO, targetX,
						targetY);
				double dist = Math.hypot(targetX - robotX, targetY - robotY);
				if (Math.abs(dist) > 30) {
					controlThread.travelSpeed = (int) (dist * 1.5);
					if (Math.abs(ang1) < 90) {
						controlThread.travelDist = (int) dist;
					} else {
						controlThread.travelDist = (int) -dist;
					}
					if (Math.abs(ang1) > 150 || Math.abs(ang1) < 10) {
						controlThread.operation = Operation.TRAVEL;
					} else if (ang1 > 0) {
						if (ang1 > 90) {
							controlThread.operation = Operation.ARC_LEFT;
						} else {
							controlThread.operation = Operation.ARC_RIGHT;
						}
						controlThread.radius = dist / 3;
					} else if (ang1 < 0) {
						if (ang1 < -90) {
							controlThread.operation = Operation.ARC_RIGHT;
						} else {
							controlThread.operation = Operation.ARC_LEFT;
						}
						controlThread.radius = dist * 3;

					}

				} else {
					controlThread.operation = Operation.CATCH;
				}
			} else {
				double ang1 = calculateAngle(robotX, robotY, robotO, goalX,
						goalY);
				//System.out.println("angle to goal: " + ang1);
				if (Math.abs(ang1) > 5) {
					controlThread.operation = Operation.ROTATE;
					controlThread.rotateBy = (int) ang1;
				} else {
					controlThread.operation = Operation.KICK;
				}
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, PREPARE_CATCH, CATCH, KICK, ARC_LEFT, ARC_RIGHT,
	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;
		public double radius = 0;

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

//					System.out.println("ballcaught: " + ballCaught + "op: " + op.toString() + " rotateBy: "
//							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case CATCH:
						brick.robotCatch();
						ballCaught = true;
						break;
					case PREPARE_CATCH:
						brick.robotPrepCatch();
						break;
					case KICK:
						brick.robotKick(100);
						ballCaught = false;
						break;
					case ROTATE:
						brick.robotRotateBy(-rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						brick.robotPrepCatch();
						brick.robotTravel(travelDist, travelSpeed);
						break;
					case ARC_LEFT:
						brick.robotPrepCatch();
						brick.robotArcForwards(radius, travelDist);
						break;
					case ARC_RIGHT:
						brick.robotPrepCatch();
						brick.robotArcForwards(-radius, travelDist);
						break;
					}
					Thread.sleep(250); // TODO: Test lower values for this and
										// see where it breaks.
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

		double ang1 = robotRad - targetRad;
		while (ang1 > Math.PI)
			ang1 -= 2 * Math.PI;
		while (ang1 < -Math.PI)
			ang1 += 2 * Math.PI;
		return Math.toDegrees(ang1);
	}
}
