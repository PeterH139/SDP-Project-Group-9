package pc.strategy;

import java.io.IOException;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class AttackerStrategy implements WorldStateReceiver {

	private BrickCommServer brick;
	private ControlThread controlThread;

	private boolean ballCaught = false;

	public AttackerStrategy(BrickCommServer brick) {
		this.brick = brick;
		controlThread = new ControlThread();
	}

	public void startControlThread() {
		controlThread.start();
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		float robotX = worldState.GetAttackerRobot().x, robotY = worldState
				.GetAttackerRobot().y;
		float robotO = worldState.GetAttackerRobot().orientation_angle;
		float targetX = worldState.getBallX(), targetY = worldState.getBallY();
		float goalX = 65, goalY = 220;
		if (targetX == 0 || targetY == 0 || robotX == 0 || robotY == 0
				|| robotO == 0) {
			worldState.setMoveR(0);
			synchronized (controlThread) {
				controlThread.operation = Operation.DO_NOTHING;
			}
			return;
		}

		synchronized (controlThread) {
			controlThread.operation = Operation.DO_NOTHING;
			if (!ballCaught) {
				double ang1 = calculateAngle(robotX, robotY, robotO, targetX,
						targetY);
				double dist = Math.hypot(robotX - targetX, robotY - targetY);
				if (Math.abs(ang1) > Math.PI / 20) {
					controlThread.operation = Operation.ROTATE;
					controlThread.rotateBy = (int) Math.toDegrees(ang1);
				} else {
					if (dist > 30) {
						controlThread.operation = Operation.TRAVEL;
						controlThread.travelDist = (int) (dist * 3);
						controlThread.travelSpeed = (int) (dist);
					} else {
						controlThread.operation = Operation.CATCH;
					}
				}
			} else {
				double ang1 = calculateAngle(robotX, robotY, robotO, goalX,
						goalY);
				if (Math.abs(ang1) > Math.PI / 20) {
					controlThread.operation = Operation.ROTATE;
					controlThread.rotateBy = (int) Math.toDegrees(ang1);
				} else {
					controlThread.operation = Operation.KICK;
					//ballCaught = faspeedlse;
				}
			}
		}
	}

	public enum Operation {
		DO_NOTHING, TRAVEL, ROTATE, PREPARE_CATCH, CATCH, KICK,
	}

	private class ControlThread extends Thread {
		public Operation operation = Operation.DO_NOTHING;
		public int rotateBy = 0;
		public int travelDist = 0;
		public int travelSpeed = 0;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					int travelDist, rotateBy, travelSpeed;
					Operation op;
					synchronized (this) {
						op = this.operation;
						rotateBy = this.rotateBy;
						travelDist = this.travelDist;
						travelSpeed = this.travelSpeed;
					}

					System.out.println("op: " + op.toString() + " rotateBy: "
							+ rotateBy + " travelDist: " + travelDist);

					switch (op) {
					case DO_NOTHING:
						
						break;
					case CATCH:
						brick.robotCatch();
						ballCaught = true;
						break;
					case PREPARE_CATCH:
						brick.robotPrepCatch();
						break;
					case KICK:
						brick.robotKick(600);
						ballCaught = false;
						break;
					case ROTATE:
						brick.robotRotateBy(rotateBy, Math.abs(rotateBy));
						break;
					case TRAVEL:
						brick.robotPrepCatch();
						brick.robotTravel(travelDist, travelSpeed);
						break;
					}
					Thread.sleep(500);
				}

				//
				// while (ballCaught)
				// {
				// brick.robotRotateBy(60);
				// brick.robotKick(700);
				// ballCaught = false;
				// }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public static double calculateAngle(float robotX, float robotY,
			float robotOrientation, float targetX, float targetY) {
		double robotRad = Math.toRadians(robotOrientation);
		double targetRad = Math.atan2(targetY - robotY, targetX - robotX);

		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;

		double ang1 = targetRad - robotRad;
		while (ang1 > Math.PI)
			ang1 -= 2 * Math.PI;
		while (ang1 < -Math.PI)
			ang1 += 2 * Math.PI;
		return ang1;
	}
}
