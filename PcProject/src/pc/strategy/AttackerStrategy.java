package pc.strategy;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.Operation;
import pc.vision.PitchConstants;
import pc.world.oldmodel.WorldState;

public class AttackerStrategy extends GeneralStrategy {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private boolean stopControlThread;
	private boolean ballInEnemyAttackerArea = false;
	private boolean justCaught = true;
	private boolean fromSide = false;

	public AttackerStrategy(BrickCommServer brick) {
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

		if (worldState.weAreShootingRight && ballX > defenderCheck
				&& ballX < leftCheck || !worldState.weAreShootingRight
				&& ballX < defenderCheck && ballX > rightCheck) {
			this.ballInEnemyAttackerArea = true;
		} else {
			this.ballInEnemyAttackerArea = false;
		}
		if ((ballX < leftCheck || ballX > rightCheck)
				&& !ballInEnemyAttackerArea) {
			synchronized (controlThread) {
				controlThread.operation.op = Operation.Type.DO_NOTHING;
			}
			return;
		}
		if (Math.abs(ballY - PitchConstants.getPitchOutlineTop()) < 10 || Math.abs(ballY - PitchConstants.getPitchOutlineBottom()) < 10 ) {
			fromSide = true;
		} else {
			fromSide = false;
		}
		synchronized (controlThread) {
			if (ballInEnemyAttackerArea) { 
				controlThread.operation = returnToOrigin(RobotType.ATTACKER);
			} else {
				if (!ballCaughtAttacker) {
					controlThread.operation = catchBall(RobotType.ATTACKER);
					justCaught = true;
				} else {
					controlThread.operation = scoreGoal(RobotType.ATTACKER);
					if (justCaught && fromSide) {
						controlThread.operation.op = Operation.Type.ATKROTATE;
						controlThread.operation.rotateBy = (int) calculateAngle(attackerRobotX, attackerRobotY, attackerOrientation, leftCheck, attackerRobotY);
						controlThread.operation.rotateSpeed = 50;
						if (Math.abs(controlThread.operation.rotateBy) < 10) {
							controlThread.operation.op = Operation.Type.DO_NOTHING;
						}
						if (controlThread.operation.op == Operation.Type.DO_NOTHING) {
							justCaught = false;
						}
					}
				}
				// kicks if detected false catch
				if (ballCaughtAttacker
						&& (Math.hypot(ballX - attackerRobotX, ballY
								- attackerRobotY) > 60) && !worldState.ballNotOnPitch) {
					controlThread.operation.op = Operation.Type.ATKKICK;
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
//				Operation.Type prevOp = null;
				while (!stopControlThread) {
					int travelDist, rotateBy, rotateSpeed, travelSpeed;
					Operation.Type op;
					double radius;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.rotateBy;
						rotateSpeed = this.operation.rotateSpeed;
						travelDist = this.operation.travelDistance;
						travelSpeed = this.operation.travelSpeed;
						radius = this.operation.radius;
					}

//					if (prevOp != null) {
//						if (!op.equals(prevOp)){
//							System.out.println("justCaught: " + justCaught + " op: " + op.toString() + " rotateBy: "
//								+ rotateBy + " travelDist: " + travelDist
//								+ "radius: " + radius);
//						}
//					}
//					prevOp = op;

					switch (op) {
					case DO_NOTHING:
						break;
					case ATKCATCH:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							brick.execute(new RobotCommand.Catch());
							ballCaughtAttacker = true;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKMOVEKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							brick.execute(new RobotCommand.Travel(100, 10000));
							brick.execute(new RobotCommand.Kick(100));
							ballCaughtAttacker = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							brick.execute(new RobotCommand.Kick(100));
							ballCaughtAttacker = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKCONFUSEKICKRIGHT:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							brick.execute(new RobotCommand.Rotate(75, 100, false));
							brick.execute(new RobotCommand.Rotate(-105, 1000, false));
							brick.execute(new RobotCommand.Travel(100, 10000));
							brick.execute(new RobotCommand.Kick(100));
							ballCaughtAttacker = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKCONFUSEKICKLEFT:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							brick.execute(new RobotCommand.Rotate(-75, 100, false));
							brick.execute(new RobotCommand.Rotate(105, 1000, false));
							brick.execute(new RobotCommand.Travel(100, 10000));
							brick.execute(new RobotCommand.Kick(100));
							ballCaughtAttacker = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKROTATE:
						brick.execute(new RobotCommand.Rotate(-rotateBy,
								rotateSpeed));
						break;
					case ATKTRAVEL:
						brick.execute(new RobotCommand.Travel(travelDist,
								travelSpeed));
						break;
					case ATKARC_LEFT:
						brick.execute(new RobotCommand.TravelArc(radius,
								travelDist, travelSpeed));
						break;
					case ATKARC_RIGHT:
						brick.execute(new RobotCommand.TravelArc(-radius,
								travelDist, travelSpeed));
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
