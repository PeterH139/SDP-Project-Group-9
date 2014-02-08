package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class TargetFollowerStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;

	public TargetFollowerStrategy(BrickCommServer brick) {
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
		int targetX = worldState.getRobotTargetX(), targetY = worldState.getRobotTargetY();

		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0
				|| Math.hypot(robotX - targetX, robotY - targetY) < 10) {
			worldState.setMoveR(0);
			synchronized (controlThread) {
				controlThread.rotateBy  = 0;
				controlThread.travelDist = 0;
				controlThread.radius = Double.POSITIVE_INFINITY;
			}
			return;
		}

		double robotRad = Math.toRadians(robotO);
		double targetRad = Math.atan2(targetY - robotY, targetX - robotX);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang1 = targetRad - robotRad;
		while (ang1 > Math.PI)
			ang1 -= 2 * Math.PI;
		while (ang1 < -Math.PI)
			ang1 += 2 * Math.PI;
		
		double dist = Math.hypot(robotX - targetX, robotY - targetY);

		double radius = Math.hypot(robotX - targetX, robotY - targetY)
				/ (2 * Math.sin(ang1));
		
		/* Potential alteration for keeper strategy
		 * 
		 * Take the radius from when the Striker is shooting.
		 * radius = Math.hypot(robotX - targetX, robotY - targetY) / 2;
		 * 
		 * Upon shooting move round this arc in order to close the angle down.
		 * 
		 */
		
		worldState.setMoveR(radius);
		worldState.setMoveX(robotX + radius * Math.cos(robotRad + Math.PI / 2));
		worldState.setMoveY(robotY + radius * Math.sin(robotRad + Math.PI / 2));
		System.out.println(Math.toDegrees(ang1));
 
		synchronized (controlThread) {
			controlThread.rotateBy = 0;
			controlThread.travelDist = 0;
			controlThread.radius = -radius;

			if (Math.abs(ang1) > Math.PI / 16) {
				controlThread.rotateBy =  (int) Math.toDegrees(-ang1 * 0.8);
			}
			else {
				controlThread.travelDist = (int) (-dist * 3);
			}
		}
	}

	private class ControlThread extends Thread {
		public int rotateBy = 0;
		public double radius = Double.POSITIVE_INFINITY;
		public int travelDist = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int travelDist, rotateBy;
					double radius, speed;
					synchronized (this) {
						rotateBy = this.rotateBy;
						radius = this.radius;
						travelDist = this.travelDist;
					}
					// Currently just dummy values: needs calibration 
					speed = (travelDist < 50) ? 50 : 5 * travelDist; 
					brick.robotWheelSpeed(speed);
					if (rotateBy < 45) {
						brick.robotArcForwards(radius,travelDist);
					} else if( rotateBy >= 45){
						brick.robotRotateBy(rotateBy);
						brick.robotTravel(travelDist);
					}
					Thread.sleep(300);
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
