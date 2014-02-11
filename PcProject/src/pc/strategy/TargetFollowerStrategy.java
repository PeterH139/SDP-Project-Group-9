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
		
		Vector2f ball5FramesAgo = ballPositions.getFirst();
		float ballX1 = ball5FramesAgo.x, ballY1 = ball5FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

		float slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001f);
		float c = ballY1 - slope * ballX1;
		float targetY = slope * robotX + c;
		float targetX = (targetY + c) / slope; 

		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0
				|| Math.hypot(robotX - targetX, robotY - targetY) < 10) {
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

		double ang1 = calculateAngle(robotX, robotY, robotO, targetX,
				targetY);

		double dist = Math.hypot(targetX - robotX, targetY - robotY);

		double radius = Math.hypot(ballX1 - robotX, ballY1 - robotY) / 2;

		synchronized (controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			if (Math.abs(ang1) > Math.PI / 16) {
				if (ang1 > 150) {
					controlThread.operation = Operation.ARC_LEFT;
				} else if (ang1 < -150) {
					controlThread.operation = Operation.ARC_RIGHT;
				} else {
					controlThread.operation = Operation.ROTATE;
					controlThread.rotateBy = (int) Math.toDegrees(ang1) - 90;  // negating 90 to account for the fact the robot with be perpendicular to the ball
				}
				controlThread.radius = radius;
				controlThread.travelDist = (int) (-dist * 3);
				controlThread.travelSpeed = (int) (dist * 2);
			} else {
				if (ang1 > 0) {
					controlThread.operation = Operation.ARC_RIGHT;
				} else if (ang1 < 0) {
					controlThread.operation = Operation.ARC_LEFT;
				}
				if (dist > 40) {
					controlThread.operation = Operation.TRAVEL;
					controlThread.travelDist = (int) (dist * 3);
					controlThread.travelSpeed = (int) (dist * 2);
				}
				controlThread.radius = radius;
				controlThread.travelDist = (int) (dist * 3);
			}
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

					System.out.println("op: " + op.toString() + " rotateBy: "
							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case TRAVEL:
						brick.robotTravel(travelDist, travelSpeed);
						break;
					case ROTATE:
						brick.robotRotateBy(rotateBy,travelSpeed);
						break;
					case ARC_LEFT:
						brick.robotArcForwards(radius, travelDist);
						break;
					case ARC_RIGHT:	
						brick.robotArcForwards(-radius, travelDist);
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
		return ang1;
	}
}
