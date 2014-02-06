package pc.strategy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.vision.Position;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class InterceptorStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Position> ballPositions = new ArrayDeque<Position>();
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
		int robotX = worldState.getYellowX(), robotY = worldState.getYellowY();
		double robotO = worldState.getYellowOrientation();
		ballPositions.addLast(new Position(worldState.getBallX(), worldState
				.getBallY()));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		Position ball5FramesAgo = ballPositions.getFirst();
		int ballX1 = ball5FramesAgo.getX(), ballY1 = ball5FramesAgo.getY();

		int ballX2 = worldState.getBallX(), ballY2 = worldState.getBallY();

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
		
		int dist;
		int rotateBy = 0;
		if (targetY > tempBottomY) {
			targetY = tempBottomY - 15;
		}
		else if (targetY < tempTopY) {
			targetY = tempTopY + 15;
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
		if (targetX < robotX) {
			// moves robot to center of pitch
			dist = (tempBottomY - tempTopY) - robotY;
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
						brick.robotRotateBy(rotateBy);
					} else if (travelDist != 0) {
						brick.robotTravel(travelDist);
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
