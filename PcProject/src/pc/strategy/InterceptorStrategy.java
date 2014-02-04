package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class InterceptorStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private boolean forward;

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
		int ballX1 = worldState.getBallX(), ballY1 = worldState
				.getBallY();
		try {
		wait(100);
		}
		catch (Exception e) {
			System.out.println("waiting error: " + e);
		}
		int ballX2 = worldState.getBallX(), ballY2 = worldState
				.getBallY();
		
		double slope = (ballY2 - ballY1)/ ((ballX2- ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;
		
		int targetY = (int) (slope * robotX + c);
		if (targetY > robotY)
		{
			forward = true;
		}
		else {
			forward = false;
		}
		
		if (robotX == 0 || targetY == 0 || robotY == 0
				|| robotO == 0
				|| Math.hypot(0, robotY - targetY) < 10) {
			worldState.setMoveR(0);
			synchronized (controlThread) {
				controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
			}
			return;
		}
//		double robotRad = Math.toRadians(robotO);
//		double targetRad = Math.atan2(targetY - robotY, robotX - robotX);
//
//		if (robotRad > Math.PI)
//			robotRad -= 2 * Math.PI;
//
//		double ang1 = targetRad - robotRad;
//		while (ang1 > Math.PI)
//			ang1 -= 2 * Math.PI;
//		while (ang1 < -Math.PI)
//			ang1 += 2 * Math.PI;
//		
		double dist = Math.hypot(robotX - robotX, robotY - targetY);

 
		synchronized (controlThread) {
			controlThread.rotateBy = 0;
			controlThread.travelDist = 0;
//			if (Math.abs(ang1) > Math.PI / 2) {
//				if (ang1 > 0) {
//				ang1 = (Math.PI / 2) - ang1; }
//				else {
//				ang1 = Math.abs(ang1) - (Math.PI / 2);
//				}
//				forward = false;
//			}
//			else {
//				forward = true;
//			}
//			if (Math.abs(ang1) > Math.PI / 16) {
//				controlThread.rotateBy = (int) Math.toDegrees(ang1 * 0.8);
//			}
//			else {
			if (forward == true) {
			 controlThread.rotateBy = 1;
			}
			else {
				controlThread.rotateBy = 0;
			}
				controlThread.travelDist = (int) (dist * 3);
//			}
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
					if (travelDist > 5) {
					if (rotateBy == 1) {
						brick.robotTravel(-travelDist); 
					}
					else if (rotateBy == 0) {
						brick.robotTravel(travelDist); 
					}
					}
					Thread.sleep(50);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}
