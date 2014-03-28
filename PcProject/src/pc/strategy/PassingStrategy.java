package pc.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.GeneralStrategy.RobotType;
import pc.vision.PitchConstants;
import pc.vision.Position;
import pc.vision.Vector2f;
import pc.world.oldmodel.WorldState;

public class PassingStrategy extends GeneralStrategy {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private ControlThread controlThread;
	private boolean stopControlThread;
	protected boolean ballIsOnSlopeEdge;
	protected boolean ballIsOnSideEdge;
	protected boolean ballIsOnGoalLine;
	protected boolean ballIsOnDefCheck;
	protected boolean catcherIsUp = true;
	protected boolean affectBallCaught = true;
	protected boolean defenderHasArrived = false;
	protected boolean needReset = false;
	protected double distFromBall;
	protected int defenderDistFromGoal;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();

	public PassingStrategy(BrickCommServer attackerBrick,
			BrickCommServer defenderBrick) {
		this.attackerBrick = attackerBrick;
		this.defenderBrick = defenderBrick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		stopControlThread = true;
	}

	@Override
	public void startControlThread() {
		stopControlThread = false;
		this.controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		distFromBall = Math.hypot(ballX - defenderRobotX, ballY
				- defenderRobotY);
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3) {
			ballPositions.removeFirst();
		}
		Vector2f ball3FramesAgo = ballPositions.getFirst();
		float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;
		boolean ballIsMoving = Math.abs(ballX2 - ballX1) > 10
				|| Math.abs(ballY2 - ballY1) > 10;

		synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			float targetX = ballX;
			float targetY = ballY;
			int ballDistFromTop = (int) Math.abs(ballY - PitchConstants.getPitchOutlineTop());
			int ballDistFromBot = (int) Math.abs(ballY - PitchConstants.getPitchOutlineBottom());
			defenderDistFromGoal = 100;
			Position[] p = PitchConstants.getPitchOutline();
			if (!this.ballCaughtDefender) {
				if (leftCheck > defenderCheck) {					
					// we are shooting right
					defenderDistFromGoal = (int) Math.abs(defenderRobotX - p[7].getX());
					int[] topPointTopSlope = { p[0].getX(), p[0].getY() };
					int[] botPointTopSlope = { p[7].getX(), p[7].getY() };
					int[] topPointBotSlope = { p[6].getX(), p[6].getY() };
					int[] botPointBotSlope = { p[5].getX(), p[5].getY() };
					int ballDistFromGoalLine = (int) (Math
							.abs((/* x2-x1 */botPointTopSlope[0] - topPointBotSlope[0])
									* (/* y1-y0 */topPointBotSlope[1] - ballY)
									- (/* x1-x0 */topPointBotSlope[0] - ballX)
									* (/* y2-y1 */botPointTopSlope[1] - topPointBotSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointTopSlope[0] - topPointBotSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointTopSlope[1]
											- topPointBotSlope[1], 2)));
					int ballDistFromTopSlope = (int) (Math
							.abs((/* x2-x1 */botPointTopSlope[0] - topPointTopSlope[0])
									* (/* y1-y0 */topPointTopSlope[1] - ballY)
									- (/* x1-x0 */topPointTopSlope[0] - ballX)
									* (/* y2-y1 */botPointTopSlope[1] - topPointTopSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointTopSlope[0] - topPointTopSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointTopSlope[1]
											- topPointTopSlope[1], 2)));
					int ballDistFromBotSlope = (int) (Math
							.abs((/* x2-x1 */botPointBotSlope[0] - topPointBotSlope[0])
									* (/* y1-y0 */topPointBotSlope[1] - ballY)
									- (/* x1-x0 */topPointBotSlope[0] - ballX)
									* (/* y2-y1 */botPointBotSlope[1] - topPointBotSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointBotSlope[0] - topPointBotSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointBotSlope[1]
											- topPointBotSlope[1], 2)));
					if (ballDistFromBotSlope < 10 || ballDistFromTopSlope < 10) {
						ballIsOnSlopeEdge = true;
						targetX = ballX + 40;
					} else {
						ballIsOnSlopeEdge = false;
					}
					if (ballDistFromGoalLine < 10) {
						ballIsOnGoalLine = true;
					} else {
						ballIsOnGoalLine = false;
					}
					if (Math.abs(ballX - defenderCheck) < 20) {
						ballIsOnDefCheck = true;
						targetX = ballX - 40;
					} else {
						ballIsOnDefCheck = false;
					}
				} else {
					// we are shooting left
					defenderDistFromGoal = (int) Math.abs(defenderRobotX - p[1].getX());
					int[] topPointTopSlope = { p[1].getX(), p[1].getY() };
					int[] botPointTopSlope = { p[2].getX(),  p[2].getY() };
					int[] topPointBotSlope = { p[3].getX(), p[3].getY()};
					int[] botPointBotSlope = { p[4].getX(), p[4].getY() };
					int ballDistFromGoalLine = (int) (Math
							.abs((/* x2-x1 */botPointTopSlope[0] - topPointBotSlope[0])
									* (/* y1-y0 */topPointBotSlope[1] - ballY)
									- (/* x1-x0 */topPointBotSlope[0] - ballX)
									* (/* y2-y1 */botPointTopSlope[1] - topPointBotSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointTopSlope[0] - topPointBotSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointTopSlope[1]
											- topPointBotSlope[1], 2)));
					int ballDistFromTopSlope = (int) (Math
							.abs((/* x2-x1 */botPointTopSlope[0] - topPointTopSlope[0])
									* (/* y1-y0 */topPointTopSlope[1] - ballY)
									- (/* x1-x0 */topPointTopSlope[0] - ballX)
									* (/* y2-y1 */botPointTopSlope[1] - topPointTopSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointTopSlope[0] - topPointTopSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointTopSlope[1]
											- topPointTopSlope[1], 2)));
					int ballDistFromBotSlope = (int) (Math
							.abs((/* x2-x1 */botPointBotSlope[0] - topPointBotSlope[0])
									* (/* y1-y0 */topPointBotSlope[1] - ballY)
									- (/* x1-x0 */topPointBotSlope[0] - ballX)
									* (/* y2-y1 */botPointBotSlope[1] - topPointBotSlope[1])) / Math
							.sqrt(Math
									.pow((/* x2-x1 */botPointBotSlope[0] - topPointBotSlope[0]),
											2)
									+ Math.pow(/* y2-y1 */botPointBotSlope[1]
											- topPointBotSlope[1], 2)));
					if (ballDistFromBotSlope < 10 || ballDistFromTopSlope < 10) {
						ballIsOnSlopeEdge = true;
						targetX = ballX - 40;
					} else {
						ballIsOnSlopeEdge = false;
					}
						
					if (ballDistFromBotSlope < 10) {
						targetY = ballY - 10;
					} else if (ballDistFromTopSlope < 10) {
						targetY = ballY + 10;
					}
					if (ballDistFromGoalLine < 10) {
						ballIsOnGoalLine = true;
					} else {
						ballIsOnGoalLine = false;
					}
					if (Math.abs(ballX - defenderCheck) < 20) {
						ballIsOnDefCheck = true;
						targetX = ballX + 40;
					} else {
						ballIsOnDefCheck = false;
					}

				}
				if (ballDistFromTop < 10 || ballDistFromBot < 10) {
					ballIsOnSideEdge = true;
				} else {
					ballIsOnSideEdge = false;
				}
				if (ballDistFromBot < 10) {
					targetY = ballY - 40;
				}
				if (ballDistFromTop < 10) {
					targetY = ballY + 40;
				}
				if (ballDistFromBot < 10 && ballIsOnSlopeEdge) {
					targetY = ballY - 21;
				}
				if (ballDistFromTop < 10 && ballIsOnSlopeEdge) {
					targetY = ballY + 20;
				}
				double distanceToBall = Math.hypot(ballX - defenderRobotX,
						ballY - defenderRobotY);
				if (!ballIsOnSlopeEdge && !ballIsOnSideEdge
						&& !ballIsOnGoalLine && !ballIsOnDefCheck) {
					defenderHasArrived = false;
					if (!catcherIsUp) {
						this.controlThread.operation.op = Operation.Type.DEFKICK;
					} else {
						affectBallCaught = true;
						this.controlThread.operation = catchBall(RobotType.DEFENDER);
					}
				} else {
					if (catcherIsUp && !ballIsMoving && !ballIsOnDefCheck) {
						affectBallCaught = false;
						this.controlThread.operation.op = Operation.Type.DEFCATCH;
					} else {
						if (!defenderHasArrived) {
						if (ballIsOnSlopeEdge) {
								this.controlThread.operation = travelTo(
										RobotType.DEFENDER, targetX, targetY,
										15);
						}
						if (ballIsOnSideEdge) {
								this.controlThread.operation = travelTo(
										RobotType.DEFENDER, targetX, targetY,
										15);
						}
						if (ballIsOnSideEdge && ballIsOnSlopeEdge) {
							this.controlThread.operation = travelTo(
									RobotType.DEFENDER, targetX, targetY,
									22);
						}
						if (ballIsOnGoalLine) {
								this.controlThread.operation = travelTo(
										RobotType.DEFENDER, targetX, ballY, 40);						
						}
						if (ballIsOnDefCheck) {
							this.controlThread.operation = travelTo(
									RobotType.DEFENDER, targetX, ballY, 15);
						}
						if (ballIsOnDefCheck && ballIsOnSideEdge) {
							this.controlThread.operation.op = Operation.Type.DO_NOTHING;
						}
						if (this.controlThread.operation.op == Operation.Type.DO_NOTHING) {
							defenderHasArrived = true;
						}
						} else {
							double angToBall;
							if  (ballIsOnSideEdge && ballIsOnSlopeEdge) {
								angToBall = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, ballX, defenderRobotY);
							} else {
								angToBall = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, ballX, ballY);

							}
								if (Math.abs(angToBall) > 2) {
									this.controlThread.operation.op = Operation.Type.DEFROTATE;
									this.controlThread.operation.rotateBy = (int) (angToBall / 3);
									
								} else if (Math.abs(distanceToBall) > 40) {
									this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
									this.controlThread.operation.travelDistance = -(int) (distanceToBall / 3);
									this.controlThread.operation.travelSpeed = (int) (Math.abs(distanceToBall) / 3);
								} else if (ballIsOnDefCheck && Math.abs(distanceToBall) > 25) {
									this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
									this.controlThread.operation.travelDistance = -(int) (distanceToBall / 3);
									this.controlThread.operation.travelSpeed = (int) (Math.abs(distanceToBall) / 3);
								}
								this.controlThread.operation.rotateSpeed = (int) (Math.abs(angToBall));
							
						}
						
						if ((distanceToBall < 40 && !ballIsOnDefCheck) || this.controlThread.operation.op == Operation.Type.DO_NOTHING) {
							this.controlThread.operation.op = Operation.Type.DEFROTATE;
							if (ballIsOnSideEdge || ballIsOnSlopeEdge) {
								if (worldState.weAreShootingRight) {
									if (ballY < 220) {
										this.controlThread.operation.rotateBy = 100;
									} else {
										this.controlThread.operation.rotateBy = -100;
									}
								} else {
									if (ballY < 220) {
										this.controlThread.operation.rotateBy = -100;
									} else {
										this.controlThread.operation.rotateBy = 100;
									}
								}
							}
							if (ballIsOnGoalLine) {
								this.controlThread.operation.rotateBy = -(int) calculateAngle(
										defenderRobotX, defenderRobotY,
										defenderOrientation, defenderRobotX,
										defenderRobotY - 50) / 3;
							}
							if (ballIsOnDefCheck) {
								this.controlThread.operation.op = Operation.Type.DEFCATCH;
							}
							this.controlThread.operation.rotateSpeed = 150;
						} 
						
					}

				}
			} else {
				this.controlThread.operation = passBall(RobotType.DEFENDER,
						RobotType.ATTACKER);
			}
			// kicks if detected false catch
			if (ballCaughtDefender
					&& (Math.hypot(ballX - defenderRobotX, ballY
							- defenderRobotY) > 50)) {
				controlThread.operation.op = Operation.Type.DEFKICK;
			}
			if ((ballCaughtDefender && worldState.ballNotOnPitch))  {
				controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, worldState.dividers[1], (PitchConstants.getPitchOutlineBottom() - PitchConstants.getPitchOutlineTop()));
				controlThread.operation.rotateSpeed = 200;
			}
			if (needReset || (defenderDistFromGoal < 5 && (int) calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, worldState.dividers[1], (PitchConstants.getPitchOutlineBottom() - PitchConstants.getPitchOutlineTop())) > 45)) {
				needReset = true;
				controlThread.operation = travelToNoArc(RobotType.DEFENDER,
						defenderResetX, defenderResetY, 20);
				if (controlThread.operation.op == Operation.Type.DO_NOTHING) {
					needReset = false;
					defenderHasArrived = false;
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
				while (!stopControlThread) {
					int travelDist, rotateBy, travelSpeed, rotateSpeed;
					double radius;
					Operation.Type op;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.rotateBy;
						travelDist = this.operation.travelDistance;
						travelSpeed = this.operation.travelSpeed;
						rotateSpeed = this.operation.rotateSpeed;
						radius = this.operation.radius;
					}
					System.out.println("robot too close to goal: "  + (defenderDistFromGoal < 5) +  "Ball on slope Edge: "
							+ ballIsOnSlopeEdge + " ball is on side edge: "
							+ ballIsOnSideEdge + " Catcher is up: "
							+ catcherIsUp);
					System.out.println("ballCaught: " + ballCaughtDefender
							+ " op: " + op);
					switch (op) {
					case DO_NOTHING:
						break;
					case ATKROTATE:
						attackerBrick.executeSync(new RobotCommand.Rotate(
								rotateBy, Math.abs(rotateBy)));
						break;
					case ATKTRAVEL:
						attackerBrick.executeSync(new RobotCommand.Travel(
								travelDist, travelSpeed));
						break;
					case DEFCATCH:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							catcherIsUp = false;
							defenderBrick.execute(new RobotCommand.Catch());
							if (affectBallCaught || distFromBall < 32) {
								ballCaughtDefender = true;
							}
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case DEFKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							catcherIsUp = true;
							defenderBrick.execute(new RobotCommand.Kick(15));
							ballCaughtDefender = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case DEFROTATE:
						defenderBrick.executeSync(new RobotCommand.Rotate(
								rotateBy, Math.abs(rotateBy)));
						break;
					case DEFTRAVEL:
						defenderBrick.executeSync(new RobotCommand.Travel(
								travelDist, travelSpeed));
						break;
					case ROTATENMOVE:
						attackerBrick.execute(new RobotCommand.Travel(
								travelDist, travelSpeed));
						defenderBrick.execute(new RobotCommand.Rotate(rotateBy,
								Math.abs(rotateBy)));
						break;
					case DEFARC_LEFT:
						defenderBrick.executeSync(new RobotCommand.TravelArc(
								radius, travelDist, travelSpeed));
						break;
					case DEFARC_RIGHT:
						defenderBrick.executeSync(new RobotCommand.TravelArc(
								-radius, travelDist, travelSpeed));
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
