package pc.strategy;

import pc.strategy.interfaces.Strategy;
import pc.world.oldmodel.WorldState;
import pc.world.Pitch;

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

	public enum Operation {
		DO_NOTHING, ATKTRAVEL, ATKMOVEKICK, ATKROTATE, ATKPREPARE_CATCH, ATKCATCH, ATKKICK, ATKARC_LEFT, ATKARC_RIGHT, DEFTRAVEL, DEFROTATE, DEFPREPARE_CATCH, DEFCATCH, DEFKICK, ROTATENMOVE, DEFARC_LEFT, DEFARC_RIGHT
	}

	public Operation catchBall(RobotType robot, double[] RotDistSpeed) {
		scoringAttackerHasArrived = false;
		Operation toExecute = Operation.DO_NOTHING;
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
				if (Math.abs(leftCheck - ballX) < 20
						|| Math.abs(rightCheck - ballX) < 10) {
					isBallCatchable = false;
				}
			} else if (ballY < 80 && isAttacker) {
				targetY = ballY + 40;
				catchDist = 37.2;
				catchThresh = 15;
				if (Math.abs(leftCheck - ballX) < 20
						|| Math.abs(rightCheck - ballX) < 10) {
					isBallCatchable = false;
				}
			} else {
				attackerHasArrived = false;
			}
			if (!attackerHasArrived && isBallCatchable) {
				toExecute = travelTo(robot, ballX, targetY, catchThresh,
						RotDistSpeed);
				if (toExecute == Operation.DO_NOTHING) {
					attackerHasArrived = true;
				}
			} else if (isBallCatchable) {
				if (Math.abs(angToBall) > 2) {
					toExecute = Operation.ATKROTATE;
					RotDistSpeed[3] = isAttacker ? angToBall : angToBall / 3;
				} else if (Math.abs(distanceToBall) > catchDist) {
					toExecute = Operation.ATKTRAVEL;
					RotDistSpeed[1] = isAttacker ? distanceToBall
							: distanceToBall / 3;
					RotDistSpeed[2] = isAttacker ? Math.abs(distanceToBall)
							: Math.abs(distanceToBall) / 3;
				}
				RotDistSpeed[4] = isAttacker ? Math.abs(angToBall) : Math
						.abs(angToBall);
			}

			// defender's case
		} else {
			if (ballX < 87 && !isAttacker) {
				targetX = 120;
				catchDist = 37.2;
				catchThresh = 15;
				if (ballY < 135) {
					slope = 0.45;
					c -= 10;

				} else if (ballY > 287) {
					slope = -0.44;
					c += 10;
				}
				targetY = (float) (slope * targetX + c);
				if (Math.abs(defenderCheck - ballX) < 20) {
					catchDist = 200;
					catchThresh = 200;
				}
			} else if (ballX > 538 && !isAttacker) {
				targetX = 498;
				catchDist = 37.2;
				catchThresh = 15;
				if (ballY < 135) {
					slope = 0.43;
					c -= 20;
					targetY = (float) (slope * targetX - c);
				} else if (ballY > 287) {
					slope = -0.39;
					c += 20;
					targetY = (float) (slope * targetX + c);
				}
				if (Math.abs(defenderCheck - ballX) < 20) {
					catchDist = 200;
					catchThresh = 200;
				}
			} else {
				defenderHasArrived = false;
			}
			if (!defenderHasArrived) {
				toExecute = travelTo(robot, targetX, targetY, catchThresh,
						RotDistSpeed);
				System.out.println("dist: " + RotDistSpeed[1] + " slope: " + slope + " x: " + ballX + " y: " + ballY + " target y: " + targetY + " c: " + c);
				if (toExecute == Operation.DO_NOTHING) {
					defenderHasArrived = true;
				}
			} else {
				if (Math.abs(angToBall) > 2) {
					toExecute = Operation.DEFROTATE;
					RotDistSpeed[3] = isAttacker ? angToBall : angToBall / 3;
				} else if (Math.abs(distanceToBall) > catchDist) {
					toExecute = Operation.DEFTRAVEL;
					RotDistSpeed[1] = isAttacker ? distanceToBall
							: -distanceToBall / 3;
					RotDistSpeed[2] = isAttacker ? Math.abs(distanceToBall)
							: Math.abs(distanceToBall) / 3;
				}
				RotDistSpeed[4] = isAttacker ? Math.abs(angToBall) : Math
						.abs(angToBall);
			}
		}
		// TODO: implement some form of check for whether the ball is
		// against
		// the wall or not
		// if (Math.abs(distanceToBall) < catchDist
		// && Math.abs(angToBall) < 25) {
		// toExecute = Operation.DO_NOTHING;
		// }
		if (toExecute = Operation.DO_NOTHING && isBallCatchable) {
			toExecute = isAttacker? Operation.ATKCATCH: Operation.DEFCATCH;
		}
		return toExecute;
	}

	public Operation scoreGoal(RobotType robot, double[] RadDistSpeedRot) {
		attackerHasArrived = false;
		Operation toExecute = Operation.DO_NOTHING;
		float toTravelX;
		if ((Math.abs(enemyDefenderRobotX - rightCheck) < 50 || Math.abs(enemyDefenderRobotX - leftCheck) < 50) && (enemyDefenderRobotY < goalY[2] || enemyDefenderRobotY > goalY[0] ) ) {
			toTravelX = goalY[0] - 60;
		} else {
			toTravelX = goalY[1];
		}
		if (!scoringAttackerHasArrived) {
		toExecute = travelToNoArc(robot, (leftCheck + rightCheck) / 2,
				toTravelX, 20, RadDistSpeedRot);
		if (toExecute == Operation.DO_NOTHING) {
			scoringAttackerHasArrived = true;
		}
		}
		if (toExecute == Operation.DO_NOTHING) {
			float aimY = goalY[1];
			if (robot == RobotType.ATTACKER) {
				if (enemyDefenderRobotY > goalY[1]) {
					aimY = goalY[0];
				} else {
					aimY = goalY[2];
				}
				double ang1 = 0;
				if (Math.abs(enemyDefenderRobotX - rightCheck) < 50) {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, (rightCheck + 560) / 2, 60);
				} else if (Math.abs(enemyDefenderRobotX - leftCheck) < 50) {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, (leftCheck + 100) / 2, 60);
				} else {
					ang1 = calculateAngle(attackerRobotX, attackerRobotY,
							attackerOrientation, goalX, aimY);
				}
				// System.out.println("angle to goal: " + ang1);
				if (Math.abs(ang1) > 2) {
					toExecute = Operation.ATKROTATE;
					RadDistSpeedRot[3] = (int) ang1;
					RadDistSpeedRot[4] = (int) Math.abs(ang1) * 1.5;
				} else {
					toExecute = Operation.ATKMOVEKICK;
				}
			}

		}

		return toExecute;

	}

	public Operation travelToNoArc(RobotType robot, float travelToX,
			float travelToY, float distThresh, double[] DistSpeedRot) {
		Operation toExecute = Operation.DO_NOTHING;
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
					toExecute = isAttacker ? Operation.ATKROTATE
							: Operation.DEFROTATE;
					if (ang1 > 0) {
						ang1 = -(180 - ang1);
					} else {
						ang1 = -(-180 - ang1);
					}
					DistSpeedRot[3] = isAttacker ? ang1 : ang1 / 3;
					DistSpeedRot[4] = Math.abs(ang1);
				} else if (Math.abs(dist) > distThresh) {
					toExecute = isAttacker ? Operation.ATKTRAVEL
							: Operation.DEFTRAVEL;
					DistSpeedRot[1] = isAttacker ? -dist : -dist / 3;
					DistSpeedRot[2] = isAttacker ? Math.abs(dist) * 3 : 30;
				}
			} else {
				if (Math.abs(ang1) > 15) {
					toExecute = isAttacker ? Operation.ATKROTATE
							: Operation.DEFROTATE;
					DistSpeedRot[3] = isAttacker ? ang1 : ang1 / 3;
					DistSpeedRot[4] = Math.abs(ang1);
				} else if (Math.abs(dist) > distThresh) {
					toExecute = isAttacker ? Operation.ATKTRAVEL
							: Operation.DEFTRAVEL;
					DistSpeedRot[1] = isAttacker ? dist : dist / 3;
					DistSpeedRot[2] = isAttacker ? dist * 3 : dist;
				}
			}
		}
		return toExecute;

	}

	public Operation travelTo(RobotType robot, float travelToX,
			float travelToY, float distThresh, double[] RotDistSpeed) {

		Operation toExecute = Operation.DO_NOTHING;
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
			toExecute = isAttacker ? Operation.ATKROTATE : Operation.DEFROTATE;
			RotDistSpeed[3] = isAttacker ? ang1 : (ang1 / 3);
		} else if (!haveArrived) {
			RotDistSpeed[2] = isAttacker ? (int) (Math.abs(dist) * 1.5)
					: (int) (Math.abs(dist) / 3);
			if (Math.abs(ang1) < 90) {
				RotDistSpeed[1] = isAttacker ? (int) dist : (int) (dist / 3);
			} else {
				RotDistSpeed[1] = isAttacker ? (int) -dist : (int) -(dist / 3);
			}
			if (Math.abs(ang1) > 150 || Math.abs(ang1) < 10) {
				toExecute = isAttacker ? Operation.ATKTRAVEL
						: Operation.DEFTRAVEL;
			} else if (ang1 > 0) {
				if (ang1 > 90) {
					toExecute = isAttacker ? Operation.ATKARC_LEFT
							: Operation.DEFARC_LEFT;
				} else {
					toExecute = isAttacker ? Operation.ATKARC_RIGHT
							: Operation.DEFARC_RIGHT;
				}
				RotDistSpeed[0] = isAttacker ? dist / 3 : -(dist / 3);
			} else if (ang1 < 0) {
				if (ang1 < -90) {
					toExecute = isAttacker ? Operation.ATKARC_RIGHT
							: Operation.DEFARC_RIGHT;
				} else {
					toExecute = isAttacker ? Operation.ATKARC_LEFT
							: Operation.DEFARC_LEFT;
				}
				RotDistSpeed[0] = isAttacker ? dist * 3 : -(dist * 3);

			}

		}
		return toExecute;
	}

	public Operation passBall(RobotType passer, RobotType receiver,
			double[] RotDistSpeed) {
		defenderHasArrived = false;
		Operation toExecute = Operation.DO_NOTHING;
		if (!defenderHasArrived) {
			toExecute = travelToNoArc(passer, defenderResetX, defenderResetY,
					20, RotDistSpeed);
		}
		if (toExecute == Operation.DO_NOTHING) {
			defenderHasArrived = true;
			float targetY = 220;
			if (enemyAttackerRobotY < targetY) {
				targetY = enemyAttackerRobotY + 125;
			} else {
				targetY = enemyAttackerRobotY - 125;
			}
			double attackerAngle = calculateAngle(attackerRobotX,
					attackerRobotY, attackerOrientation, attackerRobotX,
					targetY);
			double angleToPass = calculateAngle(defenderRobotX, defenderRobotY,
					defenderOrientation, attackerRobotX, targetY);
			double dist = Math.hypot(attackerRobotX - attackerRobotX,
					attackerRobotY - targetY);

			toExecute = Operation.ROTATENMOVE;
			RotDistSpeed[2] = (int) (dist * 3);
			if (Math.abs(attackerAngle) > 15) {
				toExecute = Operation.ATKROTATE;
				RotDistSpeed[3] = -(int) attackerAngle;
			} else {

				if (Math.abs(angleToPass) > 10) {
					RotDistSpeed[3] = (int) angleToPass / 3;
				} else {
					RotDistSpeed[3] = 0;
				}
				if (Math.abs(dist) > 5) {
					RotDistSpeed[1] = (int) (dist);
				} else {
					RotDistSpeed[1] = 0;
					toExecute = Operation.DEFKICK;
				}

			}
		}
		return toExecute;
	}

	public Operation returnToOrigin(RobotType robot, double[] RotDistSpeed) {
		Operation toExecute = Operation.DO_NOTHING;
		boolean isAttacker = robot == RobotType.ATTACKER;

		toExecute = isAttacker ? travelToNoArc(robot, attackerResetX,
				attackerRobotY, 10, RotDistSpeed) : travelTo(robot,
				defenderResetX, defenderRobotY, 10, RotDistSpeed);

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
