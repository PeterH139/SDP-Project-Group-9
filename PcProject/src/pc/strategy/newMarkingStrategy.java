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
public class newMarkingStrategy extends GeneralStrategy {
	private static final int attackerOffset = 0; // Used to properly centre the robot at the target Y position.
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	protected boolean catcherIsUp = true;
	private boolean needReset = false;

	public newMarkingStrategy(BrickCommServer brick) {
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

		double slope = (enemyDefenderRobotY - ballY) / ((enemyDefenderRobotX - ballX) + 0.0001);
		double c = ballY - slope * ballX;
		boolean noBallMovement =  Math.abs(enemyDefenderRobotX - ballX) < 10;
		int targetY = (int) (slope * attackerRobotX + c);
		double ang1 = calculateAngle(attackerRobotX, attackerRobotY, attackerOrientation, attackerRobotX, attackerRobotY - 50);
		float dist;
		
		if (targetY > PitchConstants.getPitchOutline()[5].getY()) {
			targetY = PitchConstants.getPitchOutline()[5].getY() - 50;
		} else if (targetY <  PitchConstants.getPitchOutline()[0].getY()) {
			targetY =  PitchConstants.getPitchOutline()[0].getY() + 50;
		}
		
		// Correct for defender plate not being in centre of robot
		targetY += attackerOffset;
		
		dist = attackerRobotY - targetY;
	
		if (Math.abs(ang1) < 5) {
			ang1 = 0;
		} else {
			dist = 0;
		}
		
		synchronized (controlThread) {
			if ((Math.abs(attackerRobotX - leftCheck) < 40 || Math.abs(attackerRobotX - rightCheck) < 40)  || needReset) {
				controlThread.operation = travelToNoArc(RobotType.ATTACKER,
						attackerResetX, attackerResetY, 20);
				needReset = true;
				if (controlThread.operation.op == Operation.Type.DO_NOTHING) {
					needReset = false;
				}
			} else {
			controlThread.operation.rotateBy = (int) ang1;
			controlThread.operation.travelDistance = (int) (dist * 0.8);
			if (Math.abs(controlThread.operation.rotateBy) > 3) {
				controlThread.operation.op = Operation.Type.ATKROTATE;
			} else {
				controlThread.operation.op = Operation.Type.ATKTRAVEL;
				}	
			}

		}
		
		
	}
	private class ControlThread extends Thread {
		public Operation operation = new Operation();
		private ControlThread controlThread;
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
					case ATKROTATE:
						if (rotateBy != 0) {
						brick.executeSync(new RobotCommand.Rotate(
								-rotateBy, Math.abs(rotateBy)));
						}
						break;
					case ATKTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Travel(
									travelDist,
									Math.abs(travelDist) * 10 + 25));
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
