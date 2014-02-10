package pc.strategy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.vision.Position;
import pc.vision.Vector2f;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

/* This is a class that manages the strategy for the defender robot to intercept
 * an incoming ball. If the ball is moving away from the robot then
 * the robot will move to the centre of the goal.
 */
public class InterceptorStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	private int tempBottomY = 365;
	private int tempTopY = 103;

	public InterceptorStrategy(BrickCommServer brick) {
		this.brick = brick;
		controlThread = new ControlThread();
	}

	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		float robotX = worldState.getYellowX(), robotY = worldState.getYellowY();
		double robotO = worldState.getYellowOrientation();
		ballPositions.addLast(new Vector2f(worldState.getBallX(), worldState
				.getBallY()));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		Vector2f ball5FramesAgo = ballPositions.getFirst();
		float ballX1 = ball5FramesAgo.x, ballY1 = ball5FramesAgo.y;
		float ballX2 = worldState.getBallX(), ballY2 = worldState.getBallY();

		double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;

		int targetY = (int) (slope * robotX + c);
		int targetX = (int) ((targetY + c) / slope); 
		//targetY = ballY2;
		System.out.println(targetY);
		System.out.println(robotY);

		if (robotX == 0 || targetY == 0 || robotY == 0 || robotO == 0
				|| Math.hypot(0, robotY - targetY) < 10) {
			synchronized (controlThread) {
				controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
			}
			return;
		}
		double robotRad = Math.toRadians(robotO);
		//
		// if (robotRad > Math.PI)
		// robotRad -= 2 * Math.PI;
		//
		// while (ang1 > Math.PI)
		// ang1 -= 2 * Math.PI;
		// while (ang1 < -Math.PI)
		// ang1 ang1 += 2 * Math.PI;
		//
		
		float dist;
		int rotateBy = 0;
		if (targetY > tempBottomY) {
			targetY = tempBottomY - 15;
		}
		else if (targetY < tempTopY) {
			targetY = tempTopY + 15;
		}
		if (targetX > robotX) {
			targetY = tempBottomY - tempTopY;
		}
		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;
		if (robotRad > 0) {
			rotateBy = -(int) Math.toDegrees(Math.PI / 2 - robotRad);
			dist = targetY - robotY;
		} else {
			rotateBy = -(int) Math.toDegrees(-Math.PI / 2 - robotRad);
			dist = robotY - targetY;
		}
		if (Math.abs(rotateBy) < 20) {
			rotateBy = 0;
		}
		else {
			dist = 0;
		}
		
		
		synchronized (controlThread) {
			controlThread.rotateBy = rotateBy;
			controlThread.travelDist = (int) (dist * 3);

		}
	}

	private class ControlThread extends Thread {
		public int rotateBy = 0;
		public int travelDist = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int rotateBy, travelDist;
					synchronized (this) {
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
					}
					if (rotateBy != 0) {
						brick.robotRotateBy(rotateBy, rotateBy / 3);
					} else if (travelDist != 0) {
						brick.robotTravel(travelDist,25);
					}
					Thread.sleep(400);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
