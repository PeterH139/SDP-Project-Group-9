package pc.strategy;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.world.WorldState;

public class ResetStrategy extends GeneralStrategy{

	private BrickCommServer brick;
	private ControlThread controlThread;
	private boolean stopControlThread;
	private RobotType robotType;

	public ResetStrategy(BrickCommServer brick, boolean isAttacker){
		this.brick = brick;
		this.robotType = isAttacker ? RobotType.ATTACKER : RobotType.DEFENDER;
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
		double[] rotDistSpeed = new double[4];
		synchronized(controlThread){
			controlThread.operation = returnToOrigin(robotType, rotDistSpeed);
			controlThread.radius = rotDistSpeed[0];
			controlThread.travelDist = (int) rotDistSpeed[1];
			controlThread.travelSpeed = (int) rotDistSpeed[2];
			controlThread.rotateBy = (int) rotDistSpeed[3];
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

//					System.out.println("op: " + op.toString() + " rotateBy: "
//							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						break;
					case ATKROTATE:
						brick.execute(new RobotCommand.Rotate(-rotateBy, Math.abs(rotateBy)));
						break;
					case ATKTRAVEL:
						brick.execute(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					case ATKARC_LEFT:
						brick.execute(new RobotCommand.TravelArc(radius, travelDist, travelSpeed));
						break;
					case ATKARC_RIGHT:
						brick.execute(new RobotCommand.TravelArc(-radius, travelDist, travelSpeed));
						break;
					case DEFARC_LEFT:
						brick.execute(new RobotCommand.TravelArc(radius, travelDist, travelSpeed));
						break;
					case DEFARC_RIGHT:
						brick.execute(new RobotCommand.TravelArc(-radius, travelDist, travelSpeed));
						break;
					case DEFROTATE:
						brick.execute(new RobotCommand.Rotate(rotateBy, Math.abs(rotateBy)));
						break;
					case DEFTRAVEL:
						brick.execute(new RobotCommand.Travel(travelDist, travelSpeed));
						break;
					default:
						break;
					}
					Thread.sleep(250); // TODO: Test lower values for this and
										// see where it breaks.
				}
			}  catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
	
}
