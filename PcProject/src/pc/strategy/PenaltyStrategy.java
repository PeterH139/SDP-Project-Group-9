package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class PenaltyStrategy implements WorldStateReceiver {
	
	private BrickCommServer brick;
	private ControlThread controlThread;

	private boolean ballCaught = false;

	public PenaltyStrategy(BrickCommServer brick) {
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
		float opponentRobotX = worldState.getEnemyDefenderRobot().x, opponentRobotY = worldState
				.getEnemyDefenderRobot().y;
		float opponentRobotO = worldState.getEnemyDefenderRobot().orientation_angle;
		float targetX = worldState.getBall().x, targetY = worldState.getBall().y;
		float goalX = 63, goalY = 212;
		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0) {
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}
		
		
		
		synchronized (controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			if (!ballCaught) {
				
				// HERE CHECK WHERE THE OPPONENT ROBOT IS
				double ang1 = calculateAngle(robotX, robotY, robotO, targetX,
						targetY);
				double dist = Math.hypot(robotX - targetX, robotY - targetY);
				if (Math.abs(ang1) > Math.PI / 20) {
					controlThread.operation = Operation.ROTATE;
					controlThread.rotateBy = (int) Math.toDegrees(ang1);
				} else {
					if (dist > 30) {
						
						// HERE CHECK WHERE THE OPPONENT ROBOT IS
						controlThread.operation = Operation.TRAVEL;
						controlThread.travelDist = (int) (dist * 3);
						controlThread.travelSpeed = (int) (dist * 1.5);
					} else {
						double ang2 = calculateIdealAngle(robotX, robotY, robotO, opponentRobotY);
						if (Math.abs(ang2) > Math.PI / 20) {
							controlThread.operation = Operation.ROTATE;
							controlThread.rotateBy = (int) Math.toDegrees(ang2);
						} else {
							controlThread.operation = Operation.CATCH;
						}
					}
				}
			} else {
				
				controlThread.operation = Operation.KICK;
				
				
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, PREPARE_CATCH, CATCH, KICK,
	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;

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
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
					}

//					System.out.println("op: " + op.toString() + " rotateBy: "
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
						brick.robotKick(10000);
						ballCaught = false;
						break;
					case ROTATE:
						brick.robotRotateBy(rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						brick.robotPrepCatch();
						brick.robotTravel(travelDist, travelSpeed);
						break;
					}
					Thread.sleep(250);
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
	
	public static double calculateIdealAngle(float robotX, float robotY,
			float robotOrientation, float opponentRobotY) {
		double robotRad = Math.toRadians(robotOrientation);
		double targetRad = 0.0;
		if (opponentRobotY > 245) {
			//if opponent defender on the left side, then turn right
			double rightY = ((212 + 134)/2);
			double rightX = 63.0;
			targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
					
		}
		else if (opponentRobotY < 179) {
			//if opponent defender on the left side, then turn left
			double rightY = ((212 + 134)/2);
			double rightX = 63.0;
			targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
					
		}
		else {
			double random = Math.random();
			if (random < 0.5) {
				double rightY = ((212 + 134)/2);
				double rightX = 63.0;
				targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
			}
			else {
				
				double rightY = ((212 + 134)/2);
				double rightX = 63.0;
				targetRad = Math.atan2(rightY - robotY, rightX - robotX); 				
			}			
		}
		// double targetRad = Math.atan2(targetY - robotY, targetX - robotX);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang2 = targetRad - robotRad;
		while (ang2 > Math.PI)
			ang2 -= 2 * Math.PI;
		while (ang2 < -Math.PI)
			ang2 += 2 * Math.PI;
		return ang2;
	}
	
}