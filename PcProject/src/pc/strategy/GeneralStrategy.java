package pc.strategy;

import pc.prediction.Calculations;
import pc.strategy.Operation.Type;
import pc.strategy.interfaces.Strategy;
import pc.vision.PitchConstants;
import pc.world.oldmodel.WorldState;

public class GeneralStrategy implements Strategy {

	private static final int ATTACKER_SPEED_CONSTANT = 50;
	private static final int DEFENDER_SPEED_CONSTANT = 0;

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
	protected float enemyAttackerOrientation;
	protected int leftCheck;
	protected int rightCheck;
	protected int defenderCheck;
	protected int topOfPitch;
	protected int botOfPitch;
	protected float goalX;
	protected float ourGoalX;
	protected float[] goalY;
	protected float[] ourGoalY;
	protected float[] ourGoalEdges = new float[3];
	protected int topY;
	protected int bottomY;
	protected float defenderResetX;
	protected float defenderResetY;
	protected float attackerResetX;
	protected float attackerResetY;
	protected boolean ballCaughtDefender;
	protected boolean ballCaughtAttacker;
	protected boolean attackerHasArrived;
	protected boolean passingAttackerHasArrived;
	protected boolean defenderHasArrived;
	protected boolean isBallCatchable;
	protected boolean scoringAttackerHasArrived;
	protected boolean enemyDefenderNotOnPitch;
	protected boolean attackerNotOnPitch;
	private int BOUNCE_SHOT_DISTANCE = 50;

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
		int ballDistFromTop = (int) Math.abs(ballY
				- PitchConstants.getPitchOutlineTop());
		int ballDistFromBot = (int) Math.abs(ballY
				- PitchConstants.getPitchOutlineBottom());
		// attacker's case
		if (isAttacker) {
			if (ballDistFromBot < 20) {
				targetY = ballY - 40;
				catchDist = 35;
				catchThresh = 15;
				if (Math.abs(leftCheck - ballX) < 15
						|| Math.abs(rightCheck - ballX) < 15) {
					isBallCatchable = false;
				}
			} else if (ballDistFromTop < 20) {
				targetY = ballY + 40;
				catchDist = 35;
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
					toExecute.rotateBy = (int) (isAttacker ? angToBall
							: angToBall / 3);
				} else if (Math.abs(distanceToBall) > catchDist) {
					toExecute.op = Operation.Type.ATKTRAVEL;
					toExecute.travelDistance = (int) (isAttacker ? distanceToBall
							: distanceToBall / 3);
					toExecute.travelSpeed = (int) (isAttacker ? Math
							.abs(distanceToBall) : Math.abs(distanceToBall) / 3);
				}
				toExecute.rotateSpeed = (int) (isAttacker ? Math.abs(angToBall)
						: Math.abs(angToBall));
			}

			// defender's case
		} else {
			passingAttackerHasArrived = false;
			if (Math.abs(defenderCheck - ballX) > 20
					&& toExecute.op == Operation.Type.DO_NOTHING) {
				toExecute = travelTo(robot, ballX, ballY, catchThresh - 2);
			}
		}
		if (toExecute.op == Operation.Type.DO_NOTHING && isBallCatchable
				&& Math.abs(defenderCheck - ballX) > 25) {
			toExecute.op = isAttacker ? Operation.Type.ATKCATCH
					: Operation.Type.DEFCATCH;
		}
		return toExecute;
	}

	public Operation scoreGoal(RobotType robot) {
		attackerHasArrived = false;
		Operation toExecute = new Operation();
		float toTravelY;
		boolean isInCenter;
		int distanceFromTop = (int) Math.abs(attackerRobotY
				- PitchConstants.getPitchOutlineTop());
		int distanceFromBot = (int) Math.abs(attackerRobotY
				- PitchConstants.getPitchOutlineBottom());
		if ((Math.abs(enemyDefenderRobotX - rightCheck) < BOUNCE_SHOT_DISTANCE || Math
				.abs(enemyDefenderRobotX - leftCheck) < BOUNCE_SHOT_DISTANCE)
				&& (enemyDefenderRobotY < goalY[2] + 15 && enemyDefenderRobotY > goalY[0] - 15)) {
			isInCenter = false;
			if (distanceFromBot > distanceFromTop) {
				toTravelY = goalY[0] - 60;
			} else {
				toTravelY = goalY[2] + 60;
			}
		} else {
			toTravelY = goalY[1];
			isInCenter = true;
		}

		// If bounce shots aren't enabled, always go to the centre.
		if (!StrategyController.bounceShotEnabled) {
			toTravelY = goalY[1];
		}
		if (!scoringAttackerHasArrived) {
			toExecute = travelToNoArc(robot, (leftCheck + rightCheck) / 2,
					toTravelY, 30);
			if (toExecute.op == Operation.Type.DO_NOTHING) {
				scoringAttackerHasArrived = true;
			}
		}
		if (toExecute.op == Operation.Type.DO_NOTHING) {
			float aimY = goalY[1];
			if (robot == RobotType.ATTACKER) {
				// Determine which side of the goal we should shoot at, and
				// which way
				// we should fake shot.
				if (enemyDefenderRobotY > goalY[1]) {
					aimY = goalY[0];
					toExecute.op = (goalX == 640) ? Type.ATKCONFUSEKICKRIGHT
							: Type.ATKCONFUSEKICKLEFT;
				} else {
					aimY = goalY[2];
					toExecute.op = (goalX == 640) ? Type.ATKCONFUSEKICKLEFT
							: Type.ATKCONFUSEKICKRIGHT;
				}

				// If the enemy defender is not on pitch, or we don't want you
				// to do it, then don't fake shot.
				if (enemyDefenderNotOnPitch
						|| !StrategyController.confusionEnabled) {
					toExecute.op = Type.ATKMOVEKICK;
				}

				// Straight forward case
				double ang1 = calculateAngle(attackerRobotX, attackerRobotY,
						attackerOrientation, goalX, aimY);

				// Cases for when the defending robot is close to the line and
				// we need to try
				// a bounce shot against the wall. If we are doing a bounce shot
				// there is no need to fake.
				if (StrategyController.bounceShotEnabled && !isInCenter) {
					float goalTarget = goalY[1];
					boolean doBounce = false;
					if (distanceFromBot > distanceFromTop) {
						if (Math.abs(enemyDefenderRobotX - rightCheck) < BOUNCE_SHOT_DISTANCE
								 && (enemyDefenderRobotY > goalY[0])) {
							goalTarget = goalY[0];
							doBounce = true;
						} else if (Math.abs(enemyDefenderRobotX - leftCheck) < BOUNCE_SHOT_DISTANCE
								&& (enemyDefenderRobotY > goalY[0])) {
							goalTarget = goalY[0];
							doBounce = true;
						}
					} else {
						if (Math.abs(enemyDefenderRobotX - rightCheck) < BOUNCE_SHOT_DISTANCE
								 && (enemyDefenderRobotY < goalY[2])) {
							goalTarget = goalY[2];
							doBounce = true;
						} else if (Math.abs(enemyDefenderRobotX - leftCheck) < BOUNCE_SHOT_DISTANCE
								&& (enemyDefenderRobotY < goalY[2])) {
							goalTarget = goalY[2];
							doBounce = true;
						}
					}
					
					if (doBounce){
						ang1 = Calculations.GetBounceAngle(attackerRobotX,
								attackerRobotY, attackerOrientation, goalX,
								goalTarget, 0);
						toExecute.op = Operation.Type.ATKMOVEKICK;
					//	System.out.println("ang1: " + ang1);
					}
				}
				
				// Check we are pointing in the correct direction to score.
				// 2 degree threshold seems to work best.
				if (Math.abs(ang1) > 2) {
					toExecute.op = Operation.Type.ATKROTATE;
					toExecute.rotateBy = (int) ang1;
					toExecute.rotateSpeed = (int) (Math.abs(ang1) * 1.5);
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
					toExecute.travelDistance = (int) (isAttacker ? -dist
							: -dist / 3);
					toExecute.travelSpeed = (int) (isAttacker ? Math.abs(dist) * 3
							: 30);
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
					toExecute.travelDistance = (int) (isAttacker ? dist
							: dist / 3);
					toExecute.travelSpeed = (int) (isAttacker ? dist * 3 : dist);
				}
			}
		}
		return toExecute;

	}

	public Operation travelToNoArcNoReverse(RobotType robot, float travelToX,
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
			if (Math.abs(ang1) > 10) {
				toExecute.op = isAttacker ? Operation.Type.ATKROTATE
						: Operation.Type.DEFROTATE;
				toExecute.rotateBy = (int) (isAttacker ? ang1 : ang1 / 3);
				toExecute.rotateSpeed = (int) Math.abs(ang1) + 15;
			} else if (Math.abs(dist) > distThresh) {
				toExecute.op = isAttacker ? Operation.Type.ATKTRAVEL
						: Operation.Type.DEFTRAVEL;
				toExecute.travelDistance = (int) (isAttacker ? dist : dist / 3);
				toExecute.travelSpeed = (int) (isAttacker ? dist * 3
						: dist * 0.6);
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
			toExecute.op = isAttacker ? Operation.Type.ATKROTATE
					: Operation.Type.DEFROTATE;
			toExecute.rotateBy = (int) (isAttacker ? ang1 : (ang1 / 3));
			toExecute.rotateSpeed = Math.abs(toExecute.rotateBy);
		} else if (!haveArrived) {
			toExecute.travelSpeed = isAttacker ? (int) (Math.abs(dist) * 1.5)
					+ ATTACKER_SPEED_CONSTANT : (int) (Math.abs(dist) / 2.5)
					+ DEFENDER_SPEED_CONSTANT;
			if (Math.abs(ang1) < 90) {
				toExecute.travelDistance = isAttacker ? (int) dist
						: (int) (dist / 3);
			} else {
				toExecute.travelDistance = isAttacker ? (int) -dist
						: (int) -(dist / 3);
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
				toExecute.radius = isAttacker ? dist * 3 : -(dist * 3);
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
			int bounceDirection = 0;
			if (enemyAttackerRobotY > ((topOfPitch + botOfPitch) / 2)) {
				targetY = topOfPitch;
				bounceDirection = 1;
			} else {
				targetY = botOfPitch;
				bounceDirection = -1;
			}
			float targetX = attackerRobotX;
			if (leftCheck > defenderCheck) {
				targetX = ((defenderCheck + leftCheck) / 2);
			} else {
				targetX = ((rightCheck + defenderCheck) / 2);
			}
			double attackerAngle =  
									  calculateAngle(attackerRobotX,
									  attackerRobotY, attackerOrientation,
									  attackerResetX, attackerResetY);
									 
			double angleToPass = Calculations.GetBounceAngle(defenderRobotX,
					defenderRobotY, defenderOrientation, attackerRobotX,
					attackerRobotY, bounceDirection);
			double dist = 
							  Math.hypot(attackerRobotX - attackerResetX,
							  attackerRobotY - attackerResetY);
							 
			double attackerAngleToBall = calculateAngle(attackerRobotX,
					attackerRobotY, attackerOrientation, targetX, targetY);
			if (attackerNotOnPitch) {
				toExecute.op = Operation.Type.DEFROTATE;
				if (Math.abs(angleToPass) > 5) {
					toExecute.rotateBy = (int) angleToPass / 3;
				} else
					toExecute.op = Operation.Type.DEFKICKSTRONG;
			} else {
				toExecute.op = Operation.Type.ROTATENMOVE;
				toExecute.travelSpeed = (int) (dist * 3);
				if (Math.abs(attackerAngle) > 15 && !passingAttackerHasArrived) {
					toExecute.op = Operation.Type.ATKROTATE;
					toExecute.rotateBy = -(int) attackerAngle;
				} else {

					if (Math.abs(angleToPass) > 5) {
						toExecute.rotateBy = (int) angleToPass / 3;
					} else {
						toExecute.rotateBy = 0;
					}
					if (Math.abs(dist) > 30 && !passingAttackerHasArrived) {
						toExecute.travelDistance = (int) (dist);
					} else if (Math.abs(dist) < 30 || passingAttackerHasArrived) {
						passingAttackerHasArrived = true;
						if (Math.abs(attackerAngleToBall) > 10) {
							toExecute.op = Operation.Type.ATKROTATE;
							toExecute.rotateBy = -(int) attackerAngleToBall;
						} else if (Math.abs(angleToPass) < 3) {
							toExecute.travelDistance = 0;
							toExecute.op = Operation.Type.DEFKICK;
						}
					}

				}
			}
		}
		return toExecute;
	}

	public Operation returnToOrigin(RobotType robot) {
		Operation toExecute = new Operation();
		boolean isAttacker = robot == RobotType.ATTACKER;

		toExecute = isAttacker ? travelToNoArc(robot, attackerResetX,
				attackerResetY, 10) : travelToNoArc(robot, defenderResetX,
				defenderRobotY, 10);

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
		enemyAttackerOrientation = worldState.getEnemyAttackerRobot().orientation_angle;
		enemyDefenderNotOnPitch = worldState.enemyDefenderNotOnPitch;
		topOfPitch = PitchConstants.getPitchOutlineTop();
		botOfPitch = PitchConstants.getPitchOutlineBottom();
		attackerNotOnPitch = worldState.attackerNotOnPitch;
		if (worldState.weAreShootingRight) {
			leftCheck = worldState.dividers[1];
			rightCheck = worldState.dividers[2];
			defenderCheck = worldState.dividers[0];
			defenderResetX = ((defenderCheck - PitchConstants.getPitchOutline()[7].getX()) / 2) + 20;
			attackerResetX = ((leftCheck + rightCheck) / 2) + 15;
			goalX = PitchConstants.getPitchOutline()[2].getX();
			ourGoalX = PitchConstants.getPitchOutline()[7].getX();
			goalY = worldState.rightGoal;
			ourGoalEdges[0] = PitchConstants.getPitchOutline()[7].getY();
			ourGoalEdges[1] = worldState.leftGoal[1];
			ourGoalEdges[2] = PitchConstants.getPitchOutline()[6].getY();
			ourGoalY = worldState.leftGoal;
		} else {
			leftCheck = worldState.dividers[0];
			rightCheck = worldState.dividers[1];
			defenderCheck = worldState.dividers[2];
			defenderResetX = ((PitchConstants.getPitchOutline()[2].getX() - defenderCheck) / 2) + defenderCheck - 20;
			attackerResetX = ((leftCheck + rightCheck) / 2) - 15;
			goalX = PitchConstants.getPitchOutline()[7].getX();
			ourGoalX = PitchConstants.getPitchOutline()[2].getX();
			goalY = worldState.leftGoal;
			ourGoalEdges[0] = PitchConstants.getPitchOutline()[2].getY();
			ourGoalEdges[1] = worldState.rightGoal[1];
			ourGoalEdges[2] = PitchConstants.getPitchOutline()[3].getY();
			ourGoalY = worldState.rightGoal;
		}
		attackerResetY = (PitchConstants.getPitchOutlineBottom() + PitchConstants.getPitchOutlineTop())/2;
		defenderResetY = (PitchConstants.getPitchOutlineBottom() + PitchConstants.getPitchOutlineTop())/2;

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
