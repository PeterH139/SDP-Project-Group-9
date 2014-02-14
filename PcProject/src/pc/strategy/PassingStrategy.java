package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.PitchConstants;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class PassingStrategy implements WorldStateReceiver {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private PitchConstants pitchConstants;
	private ControlThread controlThread;

	private boolean ballCaught = false;
	private boolean ballDefender = false;
	private boolean ballAttacker = false;

	public PassingStrategy(BrickCommServer attackerBrick,
			BrickCommServer defenderBrick, PitchConstants pitchConstants) {
		this.attackerBrick = attackerBrick;
		this.defenderBrick = defenderBrick;
		this.pitchConstants = pitchConstants;
		controlThread = new ControlThread();
	}

	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		float attackerRobotX = worldState.getAttackerRobot().x, attackerRobotY = worldState
				.getAttackerRobot().y;
		float defenderRobotX = worldState.getDefenderRobot().x, defenderRobotY = worldState
				.getDefenderRobot().y;
		float attackerRobotO = worldState.getAttackerRobot().orientation_angle;
		float defenderRobotO = worldState.getDefenderRobot().orientation_angle;
		float ballX = worldState.getBall().x, ballY = worldState.getBall().y;
		int leftCheck, rightCheck, defenderCheck;
		int[] divs = pitchConstants.getDividers();
		leftCheck = (worldState.weAreShootingRight) ? divs[1] : divs[0];
		rightCheck = (worldState.weAreShootingRight) ? divs[2] : divs[1];
		defenderCheck = (worldState.weAreShootingRight) ? divs[0] : divs[2];
		// float goalX = 65;
		float goalX = 559, goalY = 220;
		System.out.println(ballX);
		if (ballX == 0 || ballY == 0 || attackerRobotX == 0
				|| attackerRobotY == 0 || attackerRobotO == 0
				|| defenderRobotX == 0 || defenderRobotY == 0
				|| defenderRobotO == 0) {
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}
	
		synchronized (controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			if ((worldState.weAreShootingRight && ballX < defenderCheck) || (!worldState.weAreShootingRight && ballX > defenderCheck)) {
				ballAttacker = false;
				ballDefender = true;
			} else if (ballX > leftCheck && ballX < rightCheck) {
				ballAttacker = true;
				ballDefender = false;
			} else {
				ballAttacker = false;
				ballDefender = false;
			}
			System.out.println(ballX);
			if (ballAttacker) {
				if (!ballCaught) {
					double ang1 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, ballX, ballY);
					double dist = Math.hypot(attackerRobotX - ballX,
							attackerRobotY - ballY);
					if (Math.abs(ang1) > Math.PI / 32) {
						controlThread.operation = Operation.ATKROTATE;
						controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						if (dist > 20) {
							controlThread.operation = Operation.ATKTRAVEL;
							controlThread.travelDist = (int) (dist * 3);
							controlThread.travelSpeed = (int) (dist * 1.5);
						} else {
							controlThread.operation = Operation.ATKCATCH;
						}
					}
				} else {
					double ang1 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, goalX, goalY);
					if (Math.abs(ang1) > Math.PI / 32) {
						controlThread.operation = Operation.ATKROTATE;
						controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						controlThread.operation = Operation.ATKKICK;
					}
				}
			} else if (ballDefender) {
				if (!ballCaught) {
					double ang1 = calculateAngle(defenderRobotX,
							defenderRobotY, defenderRobotO, ballX, ballY);
					double dist = Math.hypot(defenderRobotX - ballX,
							defenderRobotY - ballY);
					if (Math.abs(ang1) > Math.PI / 32) {
						controlThread.operation = Operation.DEFROTATE;
						controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						if (dist > 30) {
							controlThread.operation = Operation.DEFTRAVEL;
							controlThread.travelDist = (int) (dist * 3);
							controlThread.travelSpeed = (int) (dist * 1.5);
						} else {
							controlThread.operation = Operation.DEFCATCH;
						}
					}
				} else {
					double ang1 = calculateAngle(defenderRobotX,
							defenderRobotY, defenderRobotO, attackerRobotX,
							attackerRobotY);
					if (Math.abs(ang1) > Math.PI / 32) {
						controlThread.operation = Operation.DEFROTATE;
						controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						controlThread.operation = Operation.DEFKICK;
					}
				}
			}
		}
	}

	public enum Operation {
		DO_NOTHING, ATKTRAVEL, ATKROTATE, ATKPREPARE_CATCH, ATKCATCH, ATKKICK, DEFTRAVEL, DEFROTATE, DEFPREPARE_CATCH, DEFCATCH, DEFKICK,
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

					System.out.println("op: " + op.toString() + " rotateBy: "
							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:

						break;
					case ATKCATCH:
						attackerBrick.robotCatch();
						ballCaught = true;
						break;
					case ATKPREPARE_CATCH:
						attackerBrick.robotPrepCatch();
						break;
					case ATKKICK:
						attackerBrick.robotKick(10000);
						ballCaught = false;
						break;
					case ATKROTATE:
						attackerBrick.robotRotateBy(rotateBy,
								Math.abs(rotateBy));
						break;
					case ATKTRAVEL:
						attackerBrick.robotPrepCatch();
						attackerBrick.robotTravel(travelDist, travelSpeed);
						break;
					case DEFCATCH:
						defenderBrick.robotCatch();
						ballCaught = true;
						break;
					case DEFPREPARE_CATCH:
						defenderBrick.robotPrepCatch();
						break;
					case DEFKICK:
						defenderBrick.robotKick(5000);
						ballCaught = false;
					case DEFROTATE:
						defenderBrick.robotRotateBy(rotateBy / 3,
								Math.abs(rotateBy) / 2);
						break;
					case DEFTRAVEL:
						defenderBrick.robotPrepCatch();
						defenderBrick.robotTravel(-travelDist / 3, travelSpeed / 3);
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