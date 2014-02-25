package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.world.WorldState;

public class PenaltyDefenderStrategy extends GeneralStrategy {
	private BrickCommServer brick;
	private ControlThread controlThread;
	private boolean stopControlThread;

	public PenaltyDefenderStrategy(BrickCommServer brick) {
		this.brick = brick;
		controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		stopControlThread = true;
	}

	@Override
	public void startControlThread() {
		stopControlThread = false;
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		float enemyAttackerOrientation = worldState.getEnemyAttackerRobot().orientation_angle;
		float enemyAttackerRobotX = worldState.getEnemyDefenderRobot().x;
		float enemyAttackerRobotY = worldState.getEnemyAttackerRobot().y;

		if (ballX < leftCheck || ballX > rightCheck) {
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		double enemyToGoalAngle = calculateAngle(enemyAttackerRobotX,
				enemyAttackerRobotY, enemyAttackerOrientation, goalX, goalY);
		double enemyToGoalDistance = (int) Math.hypot(enemyAttackerRobotX
				- goalX, enemyAttackerRobotY - goalY);
		int distOnGoal = (int) (-enemyToGoalDistance * Math
				.tan(enemyToGoalAngle));
		int dist = (int) ((defenderRobotY - goalY) + distOnGoal);

		synchronized (controlThread) {
			// Tests whether the opponents attacker has possession
			if (Math.abs(enemyAttackerRobotX - ballX) < Math
					.abs(enemyAttackerRobotX - 40)
					&& Math.abs(enemyAttackerRobotY - ballY) < Math
							.abs(enemyAttackerRobotY - 40)) {
				controlThread.operation = Operation.DEFTRAVEL;
				controlThread.travelDist = dist;

				// close to the origin and the opponent doesn't have possession
				// so reset orientation
			} else if (Math.abs(defenderRobotX - goalX) < Math
					.abs(defenderRobotX - 40)
					&& Math.abs(enemyAttackerRobotY - goalY) < Math
							.abs(enemyAttackerRobotY - 40)) {
				controlThread.operation = Operation.DEFROTATE;
				controlThread.rotateBy = (int) (defenderOrientation - 90);

				// go to origin if the opponent doesn't have the ball
			} else {
				double[] rotDistSpeed = new double[3];
				controlThread.operation = returnToOrigin(RobotType.DEFENDER,
						rotDistSpeed);
				controlThread.rotateBy = (int) rotDistSpeed[0];
				controlThread.travelDist = (int) rotDistSpeed[1];
				controlThread.travelSpeed = (int) rotDistSpeed[2];
			}
		}
	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int travelDist = 0;
		public int travelSpeed = 250;
		public int rotateBy = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (!stopControlThread) {
					int travelDist, rotateBy, travelSpeed;
					Operation op;
					synchronized (this) {
						op = this.operation;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						rotateBy = this.rotateBy;
					}

					switch (op) {
					case DO_NOTHING:
						break;
					case DEFTRAVEL:
						brick.executeSync(new RobotCommand.PrepareCatcher());
						brick.executeSync(new RobotCommand.Travel(travelDist,
								travelSpeed));
						break;
					case DEFROTATE:
						brick.executeSync(new RobotCommand.Rotate(-rotateBy,
								Math.abs(rotateBy) * 3));
						break;
					default:
						break;
					}
					Thread.sleep(250); // TODO: Test lower values for this and
										// see where it breaks.
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
