package pc.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.GeneralStrategy.RobotType;
import pc.strategy.Operation.Type;
import pc.vision.PitchConstants;
import pc.vision.Vector2f;
import pc.world.oldmodel.WorldState;

/** Manages the strategy for the defender robot to intercept
 * an incoming ball. If the ball is moving away from the robot then
 * the robot will move to the centre of the goal.
 */
public class DefenderStrategy extends GeneralStrategy {
	private static final int defenderOffset = 0; // Used to properly centre the robot at the target Y position.
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	private boolean haveReset = false;
	protected boolean catcherIsUp = true;

	public DefenderStrategy(BrickCommServer brick) {
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
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		double slope = (enemyAttackerRobotY - ballY) / ((enemyAttackerRobotX - ballX) + 0.0001);
		double c = ballY - slope * ballX;
		boolean noBallMovement =  Math.abs(enemyAttackerRobotX - ballX) < 10;
		int targetY = (int) (slope * defenderRobotX + c);
		double enemyAngleToUs = calculateAngle(enemyAttackerRobotX, enemyDefenderRobotY, enemyAttackerOrientation, defenderRobotX, defenderRobotY);
		double ang1 = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderRobotX, defenderRobotY - 50);
		ang1 = ang1/3;
		float dist;
		if (noBallMovement) {
			targetY = (int) ballY;
		}
		if (worldState.ballNotOnPitch ) {
			// Get equation of line through enemyAttacker along its orientation
			double enemyAngleToHorizontal = calculateAngle(enemyAttackerRobotX, enemyAttackerRobotY, enemyAttackerOrientation, defenderRobotX, enemyAttackerRobotY);
			double m = enemyAngleToHorizontal / 180;
			double n = enemyAttackerRobotY - m * enemyAttackerRobotX;
			// Find intersection with defenderX
			targetY = (int) (m * defenderRobotX + n);
		}
		if (targetY > ourGoalEdges[2]) {
			targetY = (int) ourGoalEdges[2];
		} else if (targetY < ourGoalEdges[0]) {
			targetY = (int) ourGoalEdges[0];
		}
		
		// Correct for defender plate not being in centre of robot
		targetY += defenderOffset;
		
		dist = targetY - defenderRobotY;
	
		if (Math.abs(ang1) < 5) {
			ang1 = 0;
		} else {
			dist = 0;
		}
		
		synchronized (controlThread) {
			if (catcherIsUp) {
				controlThread.operation.op = Operation.Type.DEFCATCH;
				catcherIsUp = false;
			} else {
			if (Math.abs(defenderRobotX - defenderCheck) < 40 && !haveReset) {
				controlThread.operation = travelToNoArc(RobotType.DEFENDER,
						defenderResetX, defenderResetY, 20);
				if (controlThread.operation.op == Operation.Type.DO_NOTHING) {
					haveReset = true;
				}
			} else {
				haveReset = false;
			controlThread.operation.rotateBy = (int) ang1;
			controlThread.operation.travelDistance = (int) (dist * 0.8);
			if (Math.abs(controlThread.operation.rotateBy) > 3) {
				controlThread.operation.op = Operation.Type.DEFROTATE;
			} else {
				controlThread.operation.op = Operation.Type.DEFTRAVEL;
				}	
			}

		}
		}
		
	}
	private class ControlThread extends Thread {
		public Operation operation = new Operation();
		private long lastKickerEventTime = 0;
		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					Operation.Type op;
					int rotateBy, travelDist;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.rotateBy;
						travelDist = this.operation.travelDistance;
					}
//					System.out.println("operation: " + op + " rotateBy: "
//							 + rotateBy + " travelDist: " + travelDist);
					switch (op) {
					case DEFCATCH :
						brick.execute(new RobotCommand.Catch());
						break;
					case DEFROTATE:
						if (rotateBy != 0) {
						brick.executeSync(new RobotCommand.Rotate(
								rotateBy, Math.abs(rotateBy)));
						}
						break;
					case DEFTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Travel(
									travelDist / 3,
									Math.abs(travelDist) * 3 + 25));
						}
						break;
					case DEFKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
						//	brick.execute(new RobotCommand.Kick(30));
							ballCaughtDefender = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					default:
						break;
					}
					Thread.sleep(StrategyController.STRATEGY_TICK);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
