package pc.strategy;

import pc.comms.BrickCommServer;
import pc.comms.RobotCommand;
import pc.world.oldmodel.WorldState;

public class PassingStrategy extends GeneralStrategy {

	private BrickCommServer attackerBrick;
	private BrickCommServer defenderBrick;
	private ControlThread controlThread;
	private boolean stopControlThread;
	private int numberOfLostBalls = 0;
	
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
		synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			if (!this.ballCaughtDefender) {
				this.controlThread.operation = catchBall(RobotType.DEFENDER);

			} else {
				this.controlThread.operation = passBall(RobotType.DEFENDER,
						RobotType.ATTACKER);
			}
			// kicks if detected false catch
			if (ballCaughtDefender && (Math.hypot(ballX - defenderRobotX, ballY - defenderRobotY) > 50)) {
				controlThread.operation.op = Operation.Type.DEFKICK;
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
							defenderBrick.execute(new RobotCommand.Kick(15));
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

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
