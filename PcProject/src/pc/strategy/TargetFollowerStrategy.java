package pc.strategy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.vision.Vector2f;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.oldmodel.WorldState;

public class TargetFollowerStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();

	public TargetFollowerStrategy(BrickCommServer brick) {
		this.brick = brick;
		controlThread = new ControlThread();
	}

	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		float robotX = worldState.getAttackerRobot().x, robotY = worldState
				.getAttackerRobot().y;
		float robotO = worldState.getAttackerRobot().orientation_angle;
		float ballX = worldState.getBall().x, ballY = worldState.getBall().y;
		double dist = Math.hypot(ballX - robotX, ballY - robotY);

		if (dist < 10) {
			synchronized (controlThread) {
				controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
				controlThread.radius = Double.POSITIVE_INFINITY;
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		double ang1 = calculateAngle(robotX, robotY, robotO, ballX, ballY);
		System.out.println("dist: " + dist + " angle: " + ang1);

		double radius = Math.hypot(ballX - robotX, ballY - robotY) / 2;

		synchronized (controlThread) {
			if (Math.abs(dist) > 100) {
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
					controlThread.radius = radius;
				} else if (ang1 < 0) {
					if (ang1 < -90) {
						controlThread.operation = Operation.ARC_RIGHT;
					} else {
						controlThread.operation = Operation.ARC_LEFT;
					}
					controlThread.radius = radius;
				}

				controlThread.travelSpeed = (int) (dist / 2);
			} else {
				controlThread.operation = Operation.DO_NOTHING;
			}

			// else if (ang1 < -90 || ang1 > 0) { // || Intercepter Version
			// // ang1 > 0
			// controlThread.operation = Operation.ARC_RIGHT;
			// controlThread.radius = radius;
			// } else if (ang1 > 90 || ang1 < 0) { // || Intercepter Version
			// // ang1 < 0
			// controlThread.operation = Operation.ARC_LEFT;
			// controlThread.radius = radius;
			// }
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, ARC_LEFT, ARC_RIGHT,
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
					int travelDist, travelSpeed; // rotateBy,
					Operation op;
					double radius;
					synchronized (this) {
						op = this.operation;
						// rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						radius = this.radius;
					}

					System.out.println("op: " + op.toString() + " travelDist: "
							+ travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case TRAVEL:
						brick.robotTravel(travelDist, travelSpeed);
						break;
					// case ROTATE:
					// brick.robotRotateBy(rotateBy, travelSpeed);
					// break;
					case ARC_LEFT:
						brick.robotArcForwards(radius, travelDist, travelSpeed);
						break;
					case ARC_RIGHT:
						brick.robotArcForwards(-radius, travelDist, travelSpeed);
						break;
					}

					Thread.sleep(StrategyController.STRATEGY_TICK);
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
