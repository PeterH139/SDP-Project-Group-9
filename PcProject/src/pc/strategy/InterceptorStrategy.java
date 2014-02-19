package pc.strategy;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.vision.Vector2f;
import pc.world.WorldState;

/* This is a class that manages the strategy for the defender robot to intercept
 * an incoming ball. If the ball is moving away from the robot then
 * the robot will move to the centre of the goal.
 */
public class InterceptorStrategy implements Strategy {
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	private int tempBottomY = 315;
	private int tempTopY = 157;

	public InterceptorStrategy(BrickCommServer brick) {
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
		float robotX = worldState.getDefenderRobot().x;
		float robotY = worldState.getDefenderRobot().y;
		double robotO = worldState.getDefenderRobot().orientation_angle;
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 2)
			ballPositions.removeFirst();

		Vector2f ball2FramesAgo = ballPositions.getFirst();
		float ballX1 = ball2FramesAgo.x, ballY1 = ball2FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

		double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;
		boolean ballMovement =  Math.abs(ballX2 - ballX1) < 10;
		int targetY = (int) (slope * robotX + c);
		int targetX = (int) ((targetY + c) / slope);

		if (robotX <= 0.5 || targetY <= 0.5 || robotY <= 0.5 /*|| ballMovement */
				|| robotO <= 0.5 || Math.hypot(0, robotY - targetY) < 10) {
			synchronized (controlThread) {
				//controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
			}
			return;
		}
		double robotRad = Math.toRadians(robotO);
		
		
		float dist;
		int rotateBy = 0;
		if (targetY > tempBottomY) {
			targetY = tempBottomY;
		} else if (targetY < tempTopY) {
			targetY = tempTopY;
		}
		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;
		System.out.println("RobotO: " + robotO);
		if (robotO > 180) {
			rotateBy = (int) (270 - robotO) / 3;
			dist = targetY - robotY;
		} else {
			rotateBy = (int) (robotO - 90) / 3;
			dist = robotY - targetY;
		}
		if (Math.abs(rotateBy) < 10) {
			rotateBy = 0;
		} else {
			dist = 0;
		}
		if (!ballMovement) {
			dist = 0;
		}
		if (dist > 0) {
			rotateBy = 0;
		}
		System.out.println("distance: " + dist);
		synchronized (controlThread) {
			controlThread.rotateBy = rotateBy;
			controlThread.travelDist = (int) (dist * 0.8);

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
						brick.robotRotateBy(rotateBy, Math.abs(rotateBy / 3));
					} else if (travelDist != 0) {
						brick.robotTravel(travelDist,
								(int) (Math.abs(travelDist) * 4));
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
		



	
}
