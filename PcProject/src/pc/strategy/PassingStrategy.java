package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

public class PassingStrategy extends GeneralStrategy {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private ControlThread controlThread;
	private boolean ballCaught;

	public PassingStrategy(BrickCommServer attackerBrick,
			BrickCommServer defenderBrick) {
		this.attackerBrick = attackerBrick;
		this.defenderBrick = defenderBrick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		this.controlThread.stop();
	}

	@Override
	public void startControlThread() {
		this.controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		System.out.println("Passing");
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

			if (!this.ballCaught) {
				double[] RotDistSpeed = new double[3];
				this.controlThread.operation = catchBall(RobotType.DEFENDER,
						RotDistSpeed);
				this.controlThread.travelDist = (int) RotDistSpeed[1];
				this.controlThread.travelSpeed = (int) RotDistSpeed[2];
				this.controlThread.radius = RotDistSpeed[0];

			} else {
				double[] RotDistSpeed = new double[3];
				this.controlThread.operation = passBall(RobotType.DEFENDER,
						RobotType.ATTACKER, RotDistSpeed);
				this.controlThread.travelDist = (int) RotDistSpeed[1];
				this.controlThread.travelSpeed = (int) RotDistSpeed[2];
				this.controlThread.rotateBy = (int) RotDistSpeed[0];
			}
		}

	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;
		public double radius = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int travelDist, rotateBy, travelSpeed;
					double radius;
					Operation op;
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						radius = this.radius;
					}

					System.out.println("ballCaught: " + ballCaught + " op: "
							+ op.toString() + " rotateBy: " + rotateBy
							+ " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:

						break;
					case ATKROTATE:
						PassingStrategy.this.attackerBrick.robotRotateBy(
								rotateBy, Math.abs(rotateBy));
						break;
					case ATKTRAVEL:
						PassingStrategy.this.attackerBrick.robotPrepCatch();
						PassingStrategy.this.attackerBrick.robotTravel(
								travelDist, travelSpeed);
						break;
					case DEFCATCH:
						PassingStrategy.this.defenderBrick.robotCatch();
						PassingStrategy.this.ballCaught = true;
						break;
					case DEFPREPARE_CATCH:
						PassingStrategy.this.defenderBrick.robotPrepCatch();
						break;
					case DEFKICK:
						// TODO The power in here was changed when speed became
						// a percentage
						PassingStrategy.this.defenderBrick.robotKick(50);
						PassingStrategy.this.ballCaught = false;
						break;
					case DEFROTATE:
						PassingStrategy.this.defenderBrick.robotRotateBy(
								rotateBy / 3, Math.abs(rotateBy) / 3);
						break;
					case DEFTRAVEL:
						PassingStrategy.this.defenderBrick.robotPrepCatch();
						PassingStrategy.this.defenderBrick.robotTravel(
								travelDist, travelSpeed);
						break;
					case ROTATENMOVE:
						PassingStrategy.this.defenderBrick.robotRotateBy(
								rotateBy / 3, Math.abs(rotateBy) / 3);
						PassingStrategy.this.attackerBrick.robotTravel(
								travelDist, travelSpeed);
						break;
					case DEFARC_LEFT:
						defenderBrick
								.executeSync(new RobotCommand.PrepareCatcher());
						defenderBrick.executeSync(new RobotCommand.TravelArc(
								radius, travelDist, travelSpeed));
						break;
					case DEFARC_RIGHT:
						defenderBrick
								.executeSync(new RobotCommand.PrepareCatcher());
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
