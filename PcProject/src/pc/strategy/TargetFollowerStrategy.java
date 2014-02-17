package pc.strategy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.vision.Vector2f;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

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
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 2)
			ballPositions.removeFirst();

		Vector2f ball2FramesAgo = ballPositions.getFirst();
		float ballX1 = ball2FramesAgo.x, ballY1 = ball2FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;
		;

		float slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001f);
		float c = ballY1 - slope * ballX1;
		float targetY = slope * robotX + c;
		float targetX = (targetY + c) / slope;
		
//		System.out.println("targetX = " + ballX2 + " targetY = " + ballY2);
//		System.out.println("robotX = " + robotX + " robotY = " + robotY);
		double dist = Math.hypot(robotX - ballX2, robotY - ballY2);

		System.out.println("dist: " + dist);
		if (dist < 10) {
			synchronized (controlThread) {
				controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
				controlThread.radius = Double.POSITIVE_INFINITY;
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		double robotRad = Math.toRadians(robotO);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang1 = calculateAngle(robotX, robotY, robotO, targetX, targetY);
		
		//double dist = Math.hypot(targetX - robotX, targetY - robotY);
		// for testing
		
		// double radius = Math.hypot(ballX1 - robotX, ballY1 - robotY) / 2;
		// To be used for initial testing
		double radius = Math.hypot(ballX2 - robotX, ballY2 - robotY) / 2;

		synchronized (controlThread) {;
			controlThread.operation = Operation.DO_NOTHING;
			// if(!worldState.getPossession()) {
			// controlThread.operation = Operation.RESET_ORIENTATION;
			// controlThread.rotateBy = (int) Math.toDegrees(robotO - 90);
			// }
			if (Math.abs(dist) > 10) {
				if (Math.abs(ang1) > 10) {
					if (ang1 > 150) {
						controlThread.operation = Operation.ARC_LEFT;
					} else if (ang1 < -150) {
						controlThread.operation = Operation.ARC_RIGHT;
					}
					controlThread.radius = radius;
					controlThread.travelDist = (int) (-dist);
					controlThread.travelSpeed = (int) (dist / 5);
				} else {
					if (ang1 > 0) {
						controlThread.operation = Operation.ARC_RIGHT;
					} else if (ang1 < 0) {
						controlThread.operation = Operation.ARC_LEFT;
					}
					if (dist > 40) {
						controlThread.operation = Operation.TRAVEL;
						controlThread.travelDist = (int) (-dist);
						controlThread.travelSpeed = (int) (dist / 5);
					}
					controlThread.radius = radius;
					controlThread.travelDist = (int) (-dist);
				}
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, ARC_LEFT, ARC_RIGHT, RESET_ORIENTATION,
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

					System.out.println("op: " + op.toString() + " travelDist: "
							+ travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case TRAVEL:
						brick.robotTravel(travelDist, travelSpeed);
						break;
					case ROTATE:
						brick.robotRotateBy(rotateBy, travelSpeed);
						break;
					case ARC_LEFT:
						brick.robotArcForwards(radius, travelDist);
						break;
					case ARC_RIGHT:
						brick.robotArcForwards(-radius, travelDist);
						break;
					case RESET_ORIENTATION:
						brick.robotRotateBy(rotateBy, travelSpeed);
						break;
					}

					Thread.sleep(1000);
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
