package pc.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.vision.Vector2f;
import pc.world.oldmodel.WorldState;

/* This is a class that manages the strategy for the defender robot to intercept
 * an incoming ball. If the ball is moving away from the robot then
 * the robot will move to the centre of the goal.
 */
public class InterceptorStrategy extends GeneralStrategy {
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();

	public InterceptorStrategy(BrickCommServer brick) {
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
		boolean ballInAttackerArea = false;
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		if (ballX > leftCheck && ballX < rightCheck) {
			ballInAttackerArea = true;
		}

		Vector2f ball3FramesAgo = ballPositions.getFirst();
		float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

		double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;
		boolean ballMovement = Math.abs(ballX2 - ballX1) < 10;
		int targetY = (int) (slope * defenderRobotX + c);
		double ang1 = calculateAngle(defenderRobotX, defenderRobotY,
				defenderOrientation, defenderRobotX, defenderRobotY - 50);
		ang1 = ang1 / 3;
		float dist;
		if (ballMovement) {
			targetY = (int) ballY;
		}
		if (targetY > worldState.rightGoal[2]) {
			targetY = (int) worldState.rightGoal[2];
		} else if (targetY < worldState.rightGoal[0]) {
			targetY = (int) worldState.rightGoal[0];
		}

		dist = targetY - defenderRobotY;

		synchronized (controlThread) {
			if (ballInAttackerArea) {
				controlThread.operation = travelToNoArc(RobotType.DEFENDER,
						defenderResetX, defenderResetY, 20);
			} else {
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
					Thread.sleep(StrategyController.STRATEGY_TICK); // TODO: Test lower values for this and
										// see where it breaks.
				}
				// } catch (IOException e) {
				// e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
