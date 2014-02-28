package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.GeneralStrategy.Operation;
import pc.world.WorldState;

public class PassingStrategy extends GeneralStrategy {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private ControlThread controlThread;
	private boolean stopControlThread;

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
		if (ballX == 0 || ballY == 0 || attackerRobotX == 0
				|| attackerRobotY == 0 || attackerOrientation == 0
				|| defenderRobotX == 0 || defenderRobotY == 0
				|| defenderOrientation == 0) {
			synchronized (this.controlThread) {
				this.controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (this.controlThread) {
			this.controlThread.operation = Operation.DO_NOTHING;

			if (!this.ballCaughtDefender) {
				double[] RotDistSpeed = new double[4];
				this.controlThread.operation = catchBall(RobotType.DEFENDER,
						RotDistSpeed);
				this.controlThread.radius = RotDistSpeed[0];
				this.controlThread.travelDist = (int) RotDistSpeed[1];
				this.controlThread.travelSpeed = (int) RotDistSpeed[2];
				this.controlThread.rotateBy = (int) RotDistSpeed[3];
				this.controlThread.rotateSpeed = (int) Math.abs(RotDistSpeed[3]);

			} else {
				double[] RadDistSpeedRot = new double[5];
				this.controlThread.operation = passBall(RobotType.DEFENDER,
						RobotType.ATTACKER, RadDistSpeedRot);
				controlThread.radius = (int) RadDistSpeedRot[0];
				controlThread.travelDist = (int) RadDistSpeedRot[1];
				controlThread.travelSpeed = (int) RadDistSpeedRot[2];
				controlThread.rotateBy = (int) RadDistSpeedRot[3];
				controlThread.rotateSpeed = (int) RadDistSpeedRot[4];
			}
			// kicks if detected false catch
			if (ballCaughtDefender && (Math.hypot(ballX - defenderRobotX, ballY - defenderRobotY) > 45)) {
				controlThread.operation = Operation.DEFKICK;
			}
		}

	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;
		public int rotateSpeed = 0;
		public double radius = 0;

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
					Operation op;
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						rotateSpeed = this.rotateSpeed;
						radius = this.radius;
					}

//					System.out.println("ballCaught: " + ballCaughtDefender + " op: "
//							+ op.toString() + " rotateBy: " + rotateBy
//							+ " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:

						break;
					case ATKROTATE:
						attackerBrick.executeSync(new RobotCommand.Rotate(rotateBy, Math.abs(rotateBy)));
						break;
					case ATKTRAVEL:
						attackerBrick.executeSync(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					case DEFCATCH:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							defenderBrick.execute(new RobotCommand.Catch());
							ballCaughtDefender = true;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case DEFKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 1000) {
							defenderBrick.execute(new RobotCommand.Kick(30));
							ballCaughtDefender = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case DEFROTATE:
						defenderBrick.executeSync(new RobotCommand.Rotate(rotateBy, Math.abs(rotateBy)));
						break;
					case DEFTRAVEL:
						defenderBrick.executeSync(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					case ROTATENMOVE:
						attackerBrick.execute(new RobotCommand.Travel(travelDist, travelSpeed));
						defenderBrick.execute(new RobotCommand.Rotate(rotateBy, Math.abs(rotateBy)));
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
					Thread.sleep(250);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
