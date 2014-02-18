package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class PenaltyStrategy implements WorldStateReceiver,Strategy {
	
	private BrickCommServer brick;
	private ControlThread controlThread;

	private boolean ballCaught = false;

	public PenaltyStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void startControlThread() {
		this.controlThread.start();
	}
	
	@Override
	public void stopControlThread() {
		this.controlThread.stop();
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
		//TODO Goals shouldn't be hardcoded
		float goalX = 63, goalY = 212;
		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}
		
		
		
		synchronized (this.controlThread) {
			this.controlThread.operation = Operation.DO_NOTHING;
			
			if (!this.ballCaught) {
				//If we don't have the ball, then go to it, otherwise kick
				
				// TODO HERE CHECK WHERE THE OPPONENT ROBOT IS
				double robotToBallAngle = calculateAngle(robotX, robotY, robotO, targetX,
						targetY);
				double robotToBallDistance = Math.hypot(robotX - targetX, robotY - targetY);
				
				if (Math.abs(robotToBallAngle) > Math.PI / 20) {
					//If we're not facing the ball, then turn to face it.
					this.controlThread.operation = Operation.ROTATE;
					this.controlThread.rotateBy = (int) Math.toDegrees(robotToBallAngle);
				} else {
					//We're facing the ball, so we either need to go to it, or
					//catch it if we're not close enough
					
					if (robotToBallDistance > 30) {
						//We're too far away from the ball, so move closer
						
						// TODO HERE CHECK WHERE THE OPPONENT ROBOT IS
						this.controlThread.operation = Operation.TRAVEL;
						this.controlThread.travelDist = (int) (robotToBallDistance * 3);
						this.controlThread.travelSpeed = (int) (robotToBallDistance * 1.5);
					} else {
						//We're close enough to the ball, so rotate to the angle that we want to
						//be at to shoot, then catch the ball
						
						double idealAngle = calculateIdealAngle(robotX, robotY, robotO, opponentRobotY);
						
						if (Math.abs(idealAngle) > Math.PI / 20) {
							//We're not aiming where we want to, so rotate
							this.controlThread.operation = Operation.ROTATE;
							this.controlThread.rotateBy = (int) Math.toDegrees(idealAngle);
						} else {
							//We're aiming where we want to, so get the ball
							this.controlThread.operation = Operation.CATCH;
						}
					}
				}
			} else {
				//We have the ball, so kick!
				this.controlThread.operation = Operation.KICK;
				
				
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
						PenaltyStrategy.this.brick.robotCatch();
						PenaltyStrategy.this.ballCaught = true;
						break;
					case PREPARE_CATCH:
						PenaltyStrategy.this.brick.robotPrepCatch();
						break;
					case KICK:
						PenaltyStrategy.this.brick.robotKick(10000);
						PenaltyStrategy.this.ballCaught = false;
						break;
					case ROTATE:
						PenaltyStrategy.this.brick.robotRotateBy(rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						PenaltyStrategy.this.brick.robotPrepCatch();
						PenaltyStrategy.this.brick.robotTravel(travelDist, travelSpeed);
						break;
					default:
						break;
					}
					 
					//TODO Try lower values and see when it breaks
					//TODO Maybe this should be defined as a constant?
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