package pc.strategy;

import pc.strategy.interfaces.Strategy;
import pc.world.oldmodel.WorldState;

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
	protected float[] goalY;
	protected int topY;
	protected int bottomY;
	protected float defenderResetX;
	protected float defenderResetY;
	protected float attackerResetX;
	protected float attackerResetY;
	protected boolean ballCaughtDefender;
	protected boolean ballCaughtAttacker;
	protected boolean attackerHasArrived;
	protected boolean defenderHasArrived;
	protected boolean isBallCatchable;
	protected boolean scoringAttackerHasArrived;

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

	public Operation catchBall(RobotType robot) {
		defenderHasArrived = false;
		scoringAttackerHasArrived = false;
		Operation toExecute = new Operation();
		boolean isAttacker = robot == RobotType.ATTACKER;
		isBallCatchable = true;
	

		double distanceToBall = isAttacker ? Math.hypot(ballX - attackerRobotX,
				ballY - attackerRobotY) : Math.hypot(ballX - defenderRobotX,
				ballY - defenderRobotY);
		double angToBall = isAttacker ? calculateAngle(attackerRobotX,
				attackerRobotY, attackerOrientation, ballX, ballY)
				: calculateAngle(defenderRobotX, defenderRobotY,
						defenderOrientation, ballX, ballY);
		double catchDist = 32;
		int catchThresh = 32;
		float targetY = ballY;
		float targetX = ballX;
		double slope = 0;
		float c = (float) (ballY - slope * ballX);
		// attacker's case
		if (isAttacker) {

			if (ballY > 345 && isAttacker) {
				targetY = ballY - 40;
				catchDist = 37.2;
				catchThresh = 15;
				if (Math.abs(leftCheck - ballX) < 15
						|| Math.abs(rightCheck - ballX) < 15) {
					isBallCatchable = false;
				}
			} else if (ballY < 80 && isAttacker) {
				targetY = ballY + 40;
				catchDist = 37.2;
				catchThresh = 15;
				if (Math.abs(leftCheck - ballX) < 15
						|| Math.abs(rightCheck - ballX) < 15) {
					isBallCatchable = false;
				}
			} else {
				attackerHasArrived = false;
			}
			if (!attackerHasArrived && isBallCatchable) {
				toExecute = travelTo(robot, ballX, targetY, catchThresh);
				if (toExecute.op == Operation.Type.DO_NOTHING) {
					attackerHasArrived = true;
				}
			} else if (isBallCatchable) {
				if (Math.abs(angToBall) > 2) {
					toExecute.op = Operation.Type.ATKROTATE;
					toExecute.rotateBy = (int) (isAttacker ? angToBall : angToBall / 3);
				} else if (Math.abs(distanceToBall) > catchDist) {
					toExecute.op = Operation.Type.ATKTRAVEL;
					toExecute.travelDistance = (int) (isAttacker ? distanceToBall
							: distanceToBall / 3);
					toExecute.travelSpeed = (int) (isAttacker ? Math.abs(distanceToBall)
							: Math.abs(distanceToBall) / 3);
				}
				toExecute.rotateSpeed = (int) (isAttacker ? Math.abs(angToBall) : Math
						.abs(angToBall));
			}

			// defender's case
		} else {
//			if (ballX < 87 && !isAttacker) {
//				targetX = 120;
//				catchDist = 37.2;
//				catchThresh = 15;
//				if (ballY < 135) {
//					slope = 0.45;
//					c -= 10;
//
//				} else if (ballY > 287) {
//					slope = -0.44;
//					c += 10;
//				}
//				targetY = (float) (slope * targetX + c);
//				if (Math.abs(defenderCheck - ballX) < 20) {
//					catchDist = 200;
//					catchThresh = 200;
//				}
//			} else if (ballX > 538 && !isAttacker) {
//				targetX = 498;
//				catchDist = 37.2;
//				catchThresh = 15;
//				if (ballY < 135) {
//					slope = 0.43;
//					c -= 20;
//					targetY = (float) (slope * targetX - c);
//				} else if (ballY > 287) {
//					slope = 0.39;
//					c += 20;
//					targetY = (float) (slope * targetX - c);
//				}
//				if (Math.abs(defenderCheck - ballX) < 20) {
//					catchDist = 200;
//					catchThresh = 200;
//				}
//			} else {
//				defenderHasArrived = false;
//			}
//			if (!defenderHasArrived) {
			if (Math.abs(defenderCheck - ballX) > 25) {
				toExecute = travelTo(robot, ballX, ballY, catchThresh);
			}
//				System.out.println("dist: " + RotDistSpeed[1] + " slope: "
//						+ slope + " x: " + ballX + " y: " + ballY
//						+ " target y: " + targetY + " c: " + c);
//				if (toExecute == Operation.DO_NOTHING) {
//					defenderHasArrived = true;
//				}
//			} else {
//				if (Math.abs(angToBall) > 2) {
//					toExecute = Operation.DEFROTATE;
//					RotDistSpeed[3] = isAttacker ? angToBall : angToBall / 3;
//				} else if (Math.abs(distanceToBall) > catchDist) {
//					toExecute = Operation.DEFTRAVEL;
//					RotDistSpeed[1] = isAttacker ? distanceToBall
//							: -distanceToBall / 3;
//					RotDistSpeed[2] = isAttacker ? Math.abs(distanceToBall)
//							: Math.abs(distanceToBall) / 3;
//				}
//				RotDistSpeed[4] = isAttacker ? Math.abs(angToBall) : Math
//						.abs(angToBall);
//			}
		}
		// TODO: implement some form of check for whether the ball is
		// against
		// the wall or not
		// if (Math.abs(distanceToBall) < catchDist
		// && Math.abs(angToBall) < 25) {
		// toExecute = Operation.DO_NOTHING;
		// }
		if (toExecute.op == Operation.Type.DO_NOTHING && isBallCatchable && Math.abs(defenderCheck - ballX) > 25) {
			toExecute.op = isAttacker? Operation.Type.ATKCATCH: Operation.Type.DEFCATCH;
		}
		return toExecute;
	}

	public Operation scoreGoal(RobotType robot) {
		attackerHasArrived = false;
		Operation toExecute = new Operation();
		float toTravelX;
		//System.out.println("enemyY: " + enemyDefenderRobotY + "goal[0], goal[2] :" + goalY[0] +" " + goalY[2]);
		if ((Math.abs(enemyDefenderRobotX - rightCheck) < 50 || Math
				.abs(enemyDefenderRobotX - leftCheck) < 50)
				&& (enemyDefenderRobotY < goalY[2] && enemyDefenderRobotY > goalY[0])) {
			toTravelX = goalY[0] - 60;
		} else {
			toTravelX = goalY[1];
		}
		if (!scoringAttackerHasArrived) {
			toExecute = travelToNoArc(robot, (leftCheck + rightCheck) / 2,
					toTravelX, 20);
			if (toExecute.op == Operation.Type.DO_NOTHING) {
				scoringAttackerHasArrived = true;
			}
		}
		if (toExecute.op == Operation.Type.DO_NOTHING) {
			float aimY = goalY[1];
			if (robot == RobotType.ATTACKER) {
				if (enemyDefenderRobotY > goalY[1]) {
					aimY = goalY[0];
				} else {
					aimY = goalY[2];
				}
				double ang1 = 0;
				if (Math.abs(enemyDefenderRobotX - rightCheck) < 50 && (enemyDefenderRobotY < goalY[2] && enemyDefenderRobotY > goalY[0])) {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, ((rightCheck + 540) / 2) - 25, 69);
				} else if (Math.abs(enemyDefenderRobotX - leftCheck) < 50 && (enemyDefenderRobotY < goalY[2] && enemyDefenderRobotY > goalY[0])) {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, ((leftCheck + 170) / 2) + 10, 69);
				} else {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, goalX, aimY);
				}
				// System.out.println("angle to goal: " + ang1);
				if (Math.abs(ang1) > 3) {
					toExecute.op = Operation.Type.ATKROTATE;
					toExecute.rotateBy = (int) ang1;
					toExecute.rotateSpeed = (int) (Math.abs(ang1) * 1.5);
				} else {
					toExecute.op = Operation.Type.ATKMOVEKICK;
				}
			}

		}

		return toExecute;

	}

	public Operation travelToNoArc(RobotType robot, float travelToX,
			float travelToY, float distThresh) {
		Operation toExecute = new Operation();
		boolean isAttacker = robot == RobotType.ATTACKER;
		double ang1 = isAttacker ? calculateAngle(attackerRobotX,
				attackerRobotY, attackerOrientation, travelToX, travelToY)
				: calculateAngle(defenderRobotX, defenderRobotY,
						defenderOrientation, travelToX, travelToY);
		double dist = isAttacker ? Math.hypot(travelToX - attackerRobotX,
				travelToY - attackerRobotY) : -((Math.hypot(travelToX
				- defenderRobotX, travelToY - defenderRobotY)));
		boolean haveArrived = (Math.abs(dist) < distThresh);
		if (!haveArrived) {
			if (Math.abs(ang1) > 90) {
				if (Math.abs(ang1) < 165) {
					toExecute.op = isAttacker ? Operation.Type.ATKROTATE
							: Operation.Type.DEFROTATE;
					if (ang1 > 0) {
						ang1 = -(180 - ang1);
					} else {
						ang1 = -(-180 - ang1);
					}
					toExecute.rotateBy = (int) (isAttacker ? ang1 : ang1 / 3);
					toExecute.rotateSpeed = (int) Math.abs(ang1);
				} else if (Math.abs(dist) > distThresh) {
					toExecute.op = isAttacker ? Operation.Type.ATKTRAVEL
							: Operation.Type.DEFTRAVEL;
					toExecute.travelDistance = (int) (isAttacker ? -dist : -dist / 3);
					toExecute.travelSpeed = (int) (isAttacker ? Math.abs(dist) * 3 : 30);
				}
			} else {
				if (Math.abs(ang1) > 15) {
					toExecute.op = isAttacker ? Operation.Type.ATKROTATE
							: Operation.Type.DEFROTATE;
					toExecute.rotateBy = (int) (isAttacker ? ang1 : ang1 / 3);
					toExecute.rotateSpeed = (int) Math.abs(ang1);
				} else if (Math.abs(dist) > distThresh) {
					toExecute.op = isAttacker ? Operation.Type.ATKTRAVEL
							: Operation.Type.DEFTRAVEL;
					toExecute.travelDistance = (int) (isAttacker ? dist : dist / 3);
					toExecute.travelSpeed = (int) (isAttacker ? dist * 3 : dist);
				}
			}
		}
		return toExecute;

	}

	public Operation travelTo(RobotType robot, float travelToX,
			float travelToY, float distThresh) {

		Operation toExecute = new Operation();
		boolean isAttacker = robot == RobotType.ATTACKER;
		double ang1 = isAttacker ? calculateAngle(attackerRobotX,
				attackerRobotY, attackerOrientation, travelToX, travelToY)
				: calculateAngle(defenderRobotX, defenderRobotY,
						defenderOrientation, travelToX, travelToY);
		double dist = isAttacker ? Math.hypot(travelToX - attackerRobotX,
				travelToY - attackerRobotY) : -((Math.hypot(travelToX
				- defenderRobotX, travelToY - defenderRobotY)));
		boolean haveArrived = (Math.abs(dist) < distThresh);
		if (Math.abs(ang1) > 30) {
			toExecute.op = isAttacker ? Operation.Type.ATKROTATE : Operation.Type.DEFROTATE;
			toExecute.rotateBy = (int) (isAttacker ? ang1 : (ang1 / 3));
			toExecute.rotateSpeed = Math.abs(toExecute.rotateBy);
		} else if (!haveArrived) {
			toExecute.travelSpeed = isAttacker ? (int) (Math.abs(dist) * 1.5)
					: (int) (Math.abs(dist) / 2);
			if (Math.abs(ang1) < 90) {
				toExecute.travelDistance = isAttacker ? (int) dist : (int) (dist / 3);
			} else {
				toExecute.travelDistance = isAttacker ? (int) -dist : (int) -(dist / 3);
			}
			if (Math.abs(ang1) > 150 || Math.abs(ang1) < 10) {
				toExecute.op = isAttacker ? Operation.Type.ATKTRAVEL
						: Operation.Type.DEFTRAVEL;
			} else if (ang1 > 0) {
				if (ang1 > 90) {
					toExecute.op = isAttacker ? Operation.Type.ATKARC_LEFT
							: Operation.Type.DEFARC_LEFT;
				} else {
					toExecute.op = isAttacker ? Operation.Type.ATKARC_RIGHT
							: Operation.Type.DEFARC_RIGHT;
				}
				toExecute.radius = isAttacker ? dist / 3 : -(dist / 3);
			} else if (ang1 < 0) {
				if (ang1 < -90) {
					toExecute.op = isAttacker ? Operation.Type.ATKARC_RIGHT
							: Operation.Type.DEFARC_RIGHT;
				} else {
					toExecute.op = isAttacker ? Operation.Type.ATKARC_LEFT
							: Operation.Type.DEFARC_LEFT;
				}
				toExecute.radius = isAttacker ? dist * 3 : -(dist * 3);
			}
		}
		return toExecute;
	}

	public Operation passBall(RobotType passer, RobotType receiver) {
		Operation toExecute = new Operation();
		if (!defenderHasArrived) {
			toExecute = travelToNoArc(passer, defenderResetX, defenderResetY,
					20);
			if (toExecute.op == Operation.Type.DO_NOTHING) {
			defenderHasArrived = true;
			}
		} else {
		
			float targetY = 220;
			if (enemyAttackerRobotY > 220) {
				targetY = 69;
			} else {
				targetY = 354;
			}
			float targetX = attackerRobotX;
			if (leftCheck > defenderCheck) {
				targetX = ((defenderCheck + leftCheck) / 2) - 18;
			} else {
				targetX = ((rightCheck + defenderCheck) / 2) + 18;
			}
			double attackerAngle = calculateAngle(attackerRobotX,
					attackerRobotY, attackerOrientation, (leftCheck + rightCheck) / 2,
					220);
			double angleToPass = calculateAngle(defenderRobotX, defenderRobotY,
					defenderOrientation, targetX, targetY);
			double dist = Math.hypot(attackerRobotX - ((leftCheck + rightCheck) / 2),
					attackerRobotY - 220);

			toExecute.op = Operation.Type.ROTATENMOVE;
			toExecute.travelSpeed = (int) (dist * 3);
			if (Math.abs(attackerAngle) > 15) {
				toExecute.op = Operation.Type.ATKROTATE;
				toExecute.rotateBy = -(int) attackerAngle;
			} else {

				if (Math.abs(angleToPass) > 5) {
					toExecute.rotateBy = (int) angleToPass / 3;
				} else {
					toExecute.rotateBy = 0;
				}
				if (Math.abs(dist) > 30) {
					toExecute.travelDistance = (int) (dist);
				} else if (Math.abs(dist) < 30 && Math.abs(angleToPass) < 5){
					toExecute.travelDistance = 0;
					toExecute.op = Operation.Type.DEFKICK;
				}

			}
		}
			return toExecute;
		}


	public Operation returnToOrigin(RobotType robot) {
		Operation toExecute = new Operation();
		boolean isAttacker = robot == RobotType.ATTACKER;

		toExecute = isAttacker ? travelToNoArc(robot, attackerResetX,
				attackerRobotY, 10) : travelTo(robot,
				defenderResetX, defenderRobotY, 10);

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
		defenderOrientation = worldState.getDefenderRobot().orientation_angle;

		if (worldState.weAreShootingRight) {
			leftCheck = worldState.dividers[1];
			rightCheck = worldState.dividers[2];
			defenderCheck = worldState.dividers[0];
			defenderResetX = (defenderCheck / 2) + 20;
			goalX = 640;
			goalY = worldState.rightGoal;
		} else {
			leftCheck = worldState.dividers[0];
			rightCheck = worldState.dividers[1];
			defenderCheck = worldState.dividers[2];
			defenderResetX = ((defenderCheck + 640) / 2) - 40;
			goalX = 0;
			goalY = worldState.leftGoal;
		}
		attackerResetX = (leftCheck + rightCheck) / 2;
		attackerResetY = 220;
		defenderResetY = 220;

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
