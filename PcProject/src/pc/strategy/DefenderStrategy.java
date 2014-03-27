package pc.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.vision.Vector2f;
import pc.world.oldmodel.WorldState;

/** Manages the strategy for the defender robot to intercept
 * an incoming ball. If the ball is moving away from the robot then
 * the robot will move to the centre of the goal.
 */
public class DefenderStrategy extends GeneralStrategy {
	private static final int defenderOffset = 20; // Used to properly centre the robot at the target Y position.
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();

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

		if (defenderRobotX <= 0.5 || targetY <= 0.5 || defenderRobotY <= 0.5 /*|| ballMovement */
				|| defenderOrientation <= 0.5 || Math.hypot(0, defenderRobotY - targetY) < 10) {
			synchronized (controlThread) {
				controlThread.rotateBy = 0;
				controlThread.travelDist = 0;
			}
			return;
		}
		double ang1 = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderRobotX, defenderRobotY - 50);
		ang1 = ang1/3;
		float dist;
		if (noBallMovement) {
			targetY = (int) ballY;
		}
		if (targetY > worldState.rightGoal[2]) {
			targetY = (int) worldState.rightGoal[2];
		} else if (targetY < worldState.rightGoal[0]) {
			targetY = (int) worldState.rightGoal[0];
		}
		
		// Correct for defender plate not being in centre of robot
		targetY += defenderOffset;
		
		dist = targetY - defenderRobotY;
	
		if (Math.abs(ang1) < 3) {
			ang1 = 0;
		} else {
			dist = 0;
		}
		
		synchronized (controlThread) {
			controlThread.rotateBy = (int) ang1;
			controlThread.travelDist = (int) (dist * 0.8);
		}
	}

	private class ControlThread extends Thread {
		public int rotateBy = 0;
		public int travelDist = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int rotateBy, travelDist;
					synchronized (this) {
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
					}
					if (rotateBy != 0) {
						brick.execute(new RobotCommand.Rotate(rotateBy, Math.abs(rotateBy)));
					} else if (travelDist != 0) {
						brick.execute(new RobotCommand.Travel(travelDist / 3, Math.abs(travelDist) * 3 + 100));
					}
					Thread.sleep(StrategyController.STRATEGY_TICK);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
