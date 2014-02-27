package pc.strategy;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
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
			if (!ballCaught) {
				double[] RadDistSpeedRot = new double[4];
				controlThread.operation = catchBall(RobotType.ATTACKER,
						RadDistSpeedRot);
				controlThread.radius = RadDistSpeedRot[0];
				controlThread.travelDist = (int) RadDistSpeedRot[1];
				controlThread.travelSpeed = (int) RadDistSpeedRot[2];
				controlThread.rotateBy = (int) RadDistSpeedRot[3];

			} else {
				double[] RotDist = new double[2];
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

		private long lastKickerEventTime = 0;

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

					// System.out.println("ballcaught: " + ballCaught + "op: " +
					// op.toString() + " rotateBy: "
					// + rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case ATKCATCH:
						if (System.currentTimeMillis() - lastKickerEventTime > 500) {
							brick.execute(new RobotCommand.Catch());
							ballCaught = true;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKKICK:
						if (System.currentTimeMillis() - lastKickerEventTime > 500) {
							brick.execute(new RobotCommand.Kick(100));
							ballCaught = false;
							lastKickerEventTime = System.currentTimeMillis();
						}
						break;
					case ATKROTATE:
						brick.execute(new RobotCommand.Rotate(-rotateBy, Math
								.abs(rotateBy)));
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
					Thread.sleep(250); // TODO: Test lower values for this and
										// see where it breaks.
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
