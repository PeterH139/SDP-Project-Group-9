package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.comms.RobotCommand.TravelArc;
import pc.strategy.GeneralStrategy.RobotType;
import pc.strategy.TargetFollowerStrategy.Operation;
import pc.strategy.interfaces.Strategy;
import pc.world.WorldState;

public class AttackerStrategy extends GeneralStrategy {

	private BrickCommServer brick;
	private ControlThread controlThread;
	private boolean ballCaught = false;
	private boolean stopControlThread;

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

		if (ballX < leftCheck || ballX > rightCheck) {
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (controlThread) {
			controlThread.operation = Operation.ATKPREPARE_CATCH;
			if (!ballCaught) {
				double[] RotDistSpeed = new double[4];
				controlThread.operation = catchBall(RobotType.ATTACKER, RotDistSpeed);
				controlThread.radius = RotDistSpeed[0];
				controlThread.travelDist = (int) RotDistSpeed[1];
				controlThread.travelSpeed = (int) RotDistSpeed[2];
				controlThread.rotateBy = (int) RotDistSpeed[3];
				
			} else {
				double [] RotDist = new double[2];
				controlThread.operation = scoreGoal(RobotType.ATTACKER, RotDist);
				controlThread.rotateBy = (int) RotDist[0];
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
				while (!stopControlThread) {
					int travelDist, rotateBy, travelSpeed;
					Operation op;
					double radius;
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
						radius = this.radius;
					}

//					System.out.println("ballcaught: " + ballCaught + "op: " + op.toString() + " rotateBy: "
//							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case ATKCATCH:
						brick.executeSync(new RobotCommand.Catch());
						ballCaught = true;
						break;
					case ATKPREPARE_CATCH:
						brick.executeSync(new RobotCommand.PrepareCatcher());
						break;
					case ATKKICK:
						brick.executeSync(new RobotCommand.Kick(100));
						ballCaught = false;
						break;
					case ATKROTATE:
						brick.executeSync(new RobotCommand.Rotate(-rotateBy, Math.abs(rotateBy)));
						break;
					case ATKTRAVEL:
						brick.executeSync(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					case ATKARC_LEFT:
						brick.executeSync(new RobotCommand.TravelArc(radius, travelDist, travelSpeed));
						break;
					case ATKARC_RIGHT:
						brick.executeSync(new RobotCommand.TravelArc(-radius, travelDist, travelSpeed));
						break;
					}
					Thread.sleep(250); // TODO: Test lower values for this and
										// see where it breaks.
				}
			}  catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
