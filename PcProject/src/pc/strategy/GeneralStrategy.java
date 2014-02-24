package pc.strategy;

import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

public class GeneralStrategy implements Strategy {

	protected ControlThread controlThread;
	protected float attackerRobotX;
	protected float attackerRobotY;
	protected float defenderRobotX;
	protected float defenderRobotY;
	protected float enemyAttackerRobotX;
	protected float enemyAttackerRobotY;
	protected float enemyDefenderRobotX;
	protected float enemyDefenderRobotY;
	protected float ballX;
	protected float ballY;
	protected float defenderOrientation;
	protected float attackerOrientation;
	protected int leftCheck;
	protected int rightCheck;
	protected int defenderCheck;
	protected float goalX;
	protected float goalY;
	protected int topY;
	protected int bottomY;

	@Override
	public void stopControlThread() {
		controlThread.stop();
	}

	@Override
	public void startControlThread() {
		controlThread.start();
	}

	private class ControlThread extends Thread {

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
		}
	}

	public enum RobotType {
		ATTACKER, DEFENDER
	}

	public enum Operation {
		DO_NOTHING, ATKTRAVEL, ATKROTATE, ATKPREPARE_CATCH, ATKCATCH, ATKKICK, ATKARC_LEFT, ATKARC_RIGHT,
		DEFTRAVEL, DEFROTATE, DEFPREPARE_CATCH, DEFCATCH, DEFKICK, ROTATENMOVE, DEFARC_LEFT, DEFARC_RIGHT
	}

	public Operation catchBall(RobotType robot, double[] RotDistSpeed) {

		Operation toExecute = Operation.DO_NOTHING;
		boolean isAttacker = robot == RobotType.ATTACKER;

		double ang1 = isAttacker? calculateAngle(attackerRobotX, attackerRobotY,
				attackerOrientation, ballX, ballY) : calculateAngle(defenderRobotX, defenderRobotY,
						defenderOrientation, ballX, ballY);
		double dist = isAttacker ? Math.hypot(ballX - attackerRobotX, ballY
				- attackerRobotY) : -((Math.hypot(ballX - defenderRobotX, ballY
				- defenderRobotY)) / 3);
		boolean shouldCatch = isAttacker? Math.abs(dist) < 32 : Math.abs(dist) < 32/3;
		if (!shouldCatch) {
			RotDistSpeed[2] = (int) (dist * 1.5);
			if (Math.abs(ang1) < 90) {
				RotDistSpeed[1] = (int) dist;
			} else {
				RotDistSpeed[1] = (int) -dist;
			}
			if (Math.abs(ang1) > 150 || Math.abs(ang1) < 10) {
				toExecute = isAttacker? Operation.ATKTRAVEL : Operation.DEFTRAVEL;
			} else if (ang1 > 0) {
				if (ang1 > 90) {
					toExecute = isAttacker? Operation.ATKARC_LEFT : Operation.DEFARC_LEFT;
				} else {
					toExecute = isAttacker? Operation.ATKARC_RIGHT : Operation.DEFARC_RIGHT;
				}
				RotDistSpeed[0] = dist / 3;
			} else if (ang1 < 0) {
				if (ang1 < -90) {
					toExecute = isAttacker? Operation.ATKARC_RIGHT : Operation.DEFARC_RIGHT;
				} else {
					toExecute = isAttacker? Operation.ATKARC_LEFT : Operation.DEFARC_LEFT;
				}
				RotDistSpeed[0] = dist * 3;

			}

		} else {
			toExecute = isAttacker? Operation.ATKCATCH : Operation.DEFCATCH;
		}

		return toExecute;
	}

	public Operation scoreGoal(RobotType robot, double[] RotDist) {
		Operation toExecute = Operation.DO_NOTHING;
		if (robot == RobotType.ATTACKER) {
			double ang1 = calculateAngle(attackerRobotX, attackerRobotY,
					attackerOrientation, goalX, goalY);
			// System.out.println("angle to goal: " + ang1);
			if (Math.abs(ang1) > 5) {
				toExecute = Operation.ATKROTATE;
				RotDist[0] = (int) ang1;
			} else {
				toExecute = Operation.ATKKICK;
			}
		}
		return toExecute;

	}

	@Override
	public void sendWorldState(WorldState worldState) {
		attackerRobotX = worldState.getAttackerRobot().x;
		attackerRobotY = worldState.getAttackerRobot().y;
		defenderRobotX = worldState.getDefenderRobot().x;
		defenderRobotY = worldState.getDefenderRobot().y;
		enemyAttackerRobotX = worldState.getEnemyAttackerRobot().x;
		enemyAttackerRobotY = worldState.getEnemyAttackerRobot().y;
		enemyDefenderRobotX = worldState.getEnemyDefenderRobot().x;
		enemyDefenderRobotY = worldState.getEnemyDefenderRobot().y;
		ballX = worldState.getBall().x;
		ballY = worldState.getBall().y;
		attackerOrientation = worldState.getAttackerRobot().orientation_angle;
		defenderOrientation = worldState.getAttackerRobot().orientation_angle;

		if (worldState.weAreShootingRight) {
			leftCheck = worldState.dividers[1];
			rightCheck = worldState.dividers[2];
			defenderCheck = worldState.dividers[0];
			goalX = 640;
			goalY = 220;
		} else {
			leftCheck = worldState.dividers[0];
			rightCheck = worldState.dividers[1];
			defenderCheck = worldState.dividers[2];
			goalX = 0;
			goalY = 220;
		}

	}

	public static double calculateAngle(float robotX, float robotY,
			float robotOrientation, float targetX, float targetY) {
		double robotRad = Math.toRadians(robotOrientation);
		double targetRad = Math.atan2(targetY - robotY, targetX - robotX);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang1 = robotRad - targetRad;
		while (ang1 > Math.PI)
			ang1 -= 2 * Math.PI;
		while (ang1 < -Math.PI)
			ang1 += 2 * Math.PI;
		return Math.toDegrees(ang1);
	}

}
