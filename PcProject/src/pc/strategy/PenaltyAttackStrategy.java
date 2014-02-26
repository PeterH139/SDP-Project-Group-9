package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;


/**
 * This is responsible for taking penalties
 * @author Daniel, Shova, Scott
 *
 */
//FIXME Work out why it often shoots straight ahead
public class PenaltyAttackStrategy implements WorldStateReceiver,Strategy {
	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private int goalX, goalY;
	private boolean ballCaught = false;

	public PenaltyAttackStrategy(BrickCommServer brick) {
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
		float opponentRobotY = worldState.getEnemyDefenderRobot().y;
		float targetX = worldState.getBall().x, targetY = worldState.getBall().y;
		int[] divs = worldState.dividers;
		int leftCheck, rightCheck;
		
		if (worldState.weAreShootingRight) {
			leftCheck = divs[1];
			rightCheck = divs[2];
			this.goalX = 640;
			this.goalY = 220;
		} else {
			leftCheck = divs[0];
			rightCheck = divs[1];
			this.goalX = 0;
			this.goalY = 220;
		}
		
		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0 || targetX < leftCheck || targetX > rightCheck) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}
		
		
		//TODO Pull stuff out of synchronized
		synchronized (this.controlThread) {
			this.controlThread.operation = Operation.DO_NOTHING;
			
			if (!this.ballCaught) {
				//If we don't have the ball, then go to it, otherwise kick
				
				// TODO HERE CHECK WHERE THE OPPONENT ROBOT IS
				double robotToBallAngle = calculateAngle(robotX, robotY, robotO, targetX,
						targetY);
				double robotToBallDistance = Math.hypot(robotX - targetX, robotY - targetY);
				
				if (Math.abs(robotToBallAngle) > Math.PI / 10) {
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
						
						if (Math.abs(idealAngle) > Math.PI / 25) {
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
						PenaltyAttackStrategy.this.brick.robotCatch();
						PenaltyAttackStrategy.this.ballCaught = true;
						break;
					case KICK:
						PenaltyAttackStrategy.this.brick.robotKick(100);
						PenaltyAttackStrategy.this.ballCaught = false;
						break;
					case ROTATE:
						PenaltyAttackStrategy.this.brick.robotRotateBy(rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						PenaltyAttackStrategy.this.brick.robotTravel(travelDist, travelSpeed);
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
	
	public double calculateIdealAngle(float robotX, float robotY,
			float robotOrientation, float opponentRobotY) {
		double robotRad = Math.toRadians(robotOrientation);
		double targetRad = 0.0;
		
		//TODO Tweak the 2 ints below
		//The distance from the center to aim when shooting left or right
		int distanceFromCenter = 60;
		
		//The distance from the center to classify the enemy as left or right of the center
		// ie if it is 30, the enemy robot must be be between center + 30 or center - 30
		// for it to be called out of the cneter
		int centerThreshold = 30;
		
		
		if (opponentRobotY > this.goalY + centerThreshold) {
			//if opponent defender on the left side, then turn right
			double rightY = this.goalY - distanceFromCenter;
			double rightX = this.goalX;
			targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
					
		}
		else if (opponentRobotY < this.goalY - centerThreshold) {
			//if opponent defender on the left side, then turn left
			double rightY = this.goalY + distanceFromCenter;
			double rightX = this.goalX;
			targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
					
		}
		else {
			double random = Math.random();
			if (random < 0.5) {
				double rightY = this.goalY - distanceFromCenter;
				double rightX = this.goalX;
				targetRad = Math.atan2(rightY - robotY, rightX - robotX); 
			}
			else {
				
				double rightY = this.goalY + distanceFromCenter;
				double rightX = this.goalX;
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