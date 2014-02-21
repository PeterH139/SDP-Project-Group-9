package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

public class PassingStrategy implements Strategy {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private ControlThread controlThread;

	private boolean ballCaught = false;
	private boolean ballDefender = false;
	private boolean ballAttacker = false;

	public PassingStrategy(BrickCommServer attackerBrick,
			BrickCommServer defenderBrick) {
		this.attackerBrick = attackerBrick;
		this.defenderBrick = defenderBrick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		this.controlThread.stop();
	}

	@Override
	public void startControlThread() {
		this.controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		System.out.println("Passing");
		float attackerRobotX = worldState.getAttackerRobot().x, attackerRobotY = worldState
				.getAttackerRobot().y;
		float defenderRobotX = worldState.getDefenderRobot().x, defenderRobotY = worldState
				.getDefenderRobot().y;
		float enemyAttackerY = worldState.getEnemyAttackerRobot().y;
		float attackerRobotO = worldState.getAttackerRobot().orientation_angle;
		float defenderRobotO = worldState.getDefenderRobot().orientation_angle;
		float ballX = worldState.getBall().x, ballY = worldState.getBall().y;
		int leftCheck, rightCheck, defenderCheck;
		int[] divs = worldState.dividers;
		leftCheck = (worldState.weAreShootingRight) ? divs[1] : divs[0];
		rightCheck = (worldState.weAreShootingRight) ? divs[2] : divs[1];
		defenderCheck = (worldState.weAreShootingRight) ? divs[0] : divs[2];
		// float goalX = 65;
		float goalX = 559, goalY = 220;
		if (ballX == 0 || ballY == 0 || attackerRobotX == 0
				|| attackerRobotY == 0 || attackerRobotO == 0
				|| defenderRobotX == 0 || defenderRobotY == 0
				|| defenderRobotO == 0) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (this.controlThread) {
			this.controlThread.operation = Operation.DO_NOTHING;
			if ((worldState.weAreShootingRight && ballX < defenderCheck)
					|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
				this.ballAttacker = false;
				this.ballDefender = true;
			} else if (ballX > leftCheck && ballX < rightCheck) {
				this.ballAttacker = true;
				this.ballDefender = false;
			} else {
				this.ballAttacker = false;
				this.ballDefender = false;
			}

			if (this.ballAttacker) {
				if (!this.ballCaught) {
					double ang1 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, ballX, ballY);
					double dist = Math.hypot(attackerRobotX - ballX,
							attackerRobotY - ballY);
					if (Math.abs(ang1) > Math.PI / 32) {
						this.controlThread.operation = Operation.ATKROTATE;
						this.controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						if (dist > 30) {
							this.controlThread.operation = Operation.ATKTRAVEL;
							this.controlThread.travelDist = (int) (dist * 3);
							this.controlThread.travelSpeed = (int) (dist * 1.5);
						} else {
							this.controlThread.operation = Operation.ATKCATCH;
						}
					}
				} else {
					double ang1 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, goalX, goalY);
					if (Math.abs(ang1) > Math.PI / 32) {
						this.controlThread.operation = Operation.ATKROTATE;
						this.controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						this.controlThread.operation = Operation.ATKKICK;
					}
				}
			} else if (this.ballDefender) {
				if (!this.ballCaught) {
					double ang1 = calculateAngle(defenderRobotX,
							defenderRobotY, defenderRobotO, ballX, ballY);
					//ang1 = (ang1 > 0) ? (ang1 + Math.toRadians(15)) : ang1 - Math.toRadians(15);
					double dist = Math.hypot(defenderRobotX - ballX,
							defenderRobotY - ballY);
					if ((Math.abs(ang1) < (Math.PI / 12)) && dist < 36) { 
						this.controlThread.operation = Operation.DEFCATCH;
					}
					else if (Math.abs(ang1) > Math.PI / 32) {
						this.controlThread.operation = Operation.DEFROTATE;
						this.controlThread.rotateBy = -(int) Math.toDegrees(ang1);
					} else {
						if (dist > 32) {
							this.controlThread.operation = Operation.DEFTRAVEL;
							this.controlThread.travelDist = (int) (dist * 3);
							this.controlThread.travelSpeed = (int) (dist);
						} 
					}
				} else {
					float targetY = 220;
					if (enemyAttackerY < 220) {
						targetY = enemyAttackerY + 150;
					} else {
						targetY = enemyAttackerY - 150;
					}
					double ang1 = calculateAngle(defenderRobotX,
							defenderRobotY, defenderRobotO, attackerRobotX,
							targetY);
					double ang2 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, attackerRobotX,
							targetY);
					double dist = Math.hypot(0, attackerRobotY - targetY);
					
						this.controlThread.operation = Operation.ROTATENMOVE;
						this.controlThread.travelSpeed = (int) (dist * 3);
						if (Math.abs(ang2) > Math.PI / 16) {
						this.controlThread.operation = Operation.ATKROTATE;
						this.controlThread.rotateBy= (int) Math.toDegrees(ang2);
						} else {
						
						if (Math.abs(ang1) > Math.PI / 32) {
							this.controlThread.rotateBy = -(int) Math.toDegrees(ang1);
							} else {
								this.controlThread.rotateBy = 0;	
							}
						if (Math.abs(dist) > 5) {
							this.controlThread.travelDist = (int) (dist * 3);
						} else {
						this.controlThread.travelDist = 0;
						this.controlThread.operation = Operation.DEFKICK;
						}
						}
				}
			}
		}
	}

	public enum Operation {
		DO_NOTHING, ATKTRAVEL, ATKROTATE, ATKPREPARE_CATCH, ATKCATCH, ATKKICK, DEFTRAVEL, DEFROTATE, DEFPREPARE_CATCH, DEFCATCH, DEFKICK, ROTATENMOVE
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

//					System.out.println("ballCaught: " + ballCaught + " op: "
//							+ op.toString() + " rotateBy: " + rotateBy
//							+ " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:

						break;
					case ATKCATCH:
						PassingStrategy.this.attackerBrick.robotCatch();
						PassingStrategy.this.ballCaught = true;
						break;
					case ATKPREPARE_CATCH:
						PassingStrategy.this.attackerBrick.robotPrepCatch();
						break;
					case ATKKICK:
						PassingStrategy.this.attackerBrick.robotKick(100);
						PassingStrategy.this.ballCaught = false;
						break;
					case ATKROTATE:
						PassingStrategy.this.attackerBrick.robotRotateBy(rotateBy,
								Math.abs(rotateBy));
						break;
					case ATKTRAVEL:
						PassingStrategy.this.attackerBrick.robotPrepCatch();
						PassingStrategy.this.attackerBrick.robotTravel(travelDist, travelSpeed);
						break;
					case DEFCATCH:
						PassingStrategy.this.defenderBrick.robotCatch();
						PassingStrategy.this.ballCaught = true;
						break;
					case DEFPREPARE_CATCH:
						PassingStrategy.this.defenderBrick.robotPrepCatch();
						break;
					case DEFKICK:
						//TODO The power in here was changed when speed became a percentage
						PassingStrategy.this.defenderBrick.robotKick(50);
						PassingStrategy.this.ballCaught = false;
						break;
					case DEFROTATE:
						PassingStrategy.this.defenderBrick.robotRotateBy(rotateBy / 3,
								Math.abs(rotateBy) / 3);
						break;
					case DEFTRAVEL:
						PassingStrategy.this.defenderBrick.robotPrepCatch();
						PassingStrategy.this.defenderBrick.robotTravel(-travelDist / 3,
								travelSpeed / 3);
						break;
					case ROTATENMOVE:
						PassingStrategy.this.defenderBrick.robotRotateBy(rotateBy / 3, Math.abs(rotateBy) / 3);
						PassingStrategy.this.attackerBrick.robotTravel(travelDist, travelSpeed);
						break;
					default:
						
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
}
