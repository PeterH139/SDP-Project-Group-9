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
public class InterceptorStrategy extends GeneralStrategy {
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
		super.sendWorldState(worldState);
		System.out.println("Intercepting");
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		Vector2f ball3FramesAgo = ballPositions.getFirst();
		float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

		double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;
		boolean ballMovement =  Math.abs(ballX2 - ballX1) < 10;
		int targetY = (int) (slope * defenderRobotX + c);

		if (defenderRobotX <= 0.5 || targetY <= 0.5 || defenderRobotY <= 0.5 /*|| ballMovement */
				|| defenderOrientation <= 0.5 || Math.hypot(0, defenderRobotY - targetY) < 10) {
			synchronized (controlThread) {
				//controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
			}
			return;
		}
		double robotRad = Math.toRadians(defenderOrientation);
		
		
		float dist;
		int rotateBy = 0;
		if (targetY > tempBottomY) {
			targetY = tempBottomY;
		} else if (targetY < tempTopY) {
			targetY = tempTopY;
		}
		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;
		if (defenderOrientation > 180) {
			rotateBy = (int) (270 - defenderOrientation) / 3;
			dist = targetY - defenderOrientation;
		} else {
			rotateBy = (int) (defenderOrientation - 90) / 3;
			dist = defenderOrientation - targetY;
		}
		if (Math.abs(rotateBy) < 45) {
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
								(int) (Math.abs(travelDist) * 2));
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
