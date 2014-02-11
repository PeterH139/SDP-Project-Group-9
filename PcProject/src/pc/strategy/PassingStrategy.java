package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class PassingStrategy implements WorldStateReceiver {

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
		controlThread = new ControlThread();
	}

	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		float attackerRobotX = worldState.GetAttackerRobot().x, attackerRobotY = worldState
				.GetAttackerRobot().y;
		float defenderRobotX = worldState.GetDefenderRobot().x, defenderRobotY = worldState
				.GetDefenderRobot().y;
		float attackerRobotO = worldState.GetAttackerRobot().orientation_angle;
		float defenderRobotO = worldState.GetDefenderRobot().orientation_angle;
		float ballX = worldState.GetBall().x, ballY = worldState.GetBall().y;
		int leftCheck, rightCheck, defenderCheck;
		leftCheck = (worldState.weAreShootingRight) ? worldState.dividers[1]
				: worldState.dividers[0];
		rightCheck = (worldState.weAreShootingRight) ? worldState.dividers[2]
				: worldState.dividers[1];
		defenderCheck = (worldState.weAreShootingRight) ? worldState.dividers[0]
				: worldState.dividers[2];
		float goalX = 65, goalY = 220;
		if (ballX == 0 || ballY == 0 || attackerRobotX == 0
				|| attackerRobotY == 0 || attackerRobotO == 0
				|| defenderRobotX == 0 || defenderRobotY == 0
				|| defenderRobotO == 0) {
			worldState.setMoveR(0);
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			if (ballX < defenderCheck || ballX > defenderCheck) {
				ballAttacker = false;
				ballDefender = true;
			} else if (ballX > leftCheck && ballX < rightCheck) {
				ballAttacker = true;
				ballDefender = false;
			} else {
				ballAttacker = false;
				ballDefender = false;
			}
			if (ballAttacker) {
				if (!ballCaught) {
					double ang1 = calculateAngle(attackerRobotX,
							attackerRobotY, attackerRobotO, ballX, ballY);
					double dist = Math.hypot(attackerRobotX - ballX,
							attackerRobotY - ballY);
					if (Math.abs(ang1) > Math.PI / 20) {
						controlThread.operation = Operation.ATKROTATE;
						controlThread.rotateBy = (int) Math.toDegrees(ang1);
					} else {
						if (dist > 30) {
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
					if (Math.abs(ang1) > Math.PI / 20) {
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
								Math.abs(rotateBy));
						break;
					case DEFTRAVEL:
						defenderBrick.robotPrepCatch();
						defenderBrick.robotTravel(travelDist / 3, travelSpeed);
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
