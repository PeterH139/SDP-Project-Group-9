package pc.comms;

import java.io.DataOutputStream;
import java.io.IOException;

import reallejos.shared.RobotOpcode;

public class RobotCommand {
	private RobotCommand() {
	}

	public interface Command {
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException;
	}

	private static abstract class GenericCommand implements Command {
		protected abstract int getOpcode();

		@Override
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException {
			outputStream.writeInt(getOpcode());
		}
	}
	
	// Classes below represent every possible brick command

	public static class Stop extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.STOP;
		}
	}

	public static class Forwards extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.FORWARDS;
		}
	}

	public static class Backwards extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.BACKWARDS;
		}
	}

	public static class Kick extends GenericCommand {
		private int speed;

		public Kick(int speed) {
			this.speed = speed;
		}

		@Override
		protected int getOpcode() {
			return RobotOpcode.KICK;
		}

		@Override
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException {
			super.sendToBrick(outputStream);
			outputStream.writeInt(speed);
		}
	}

	public static class Catch extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.CATCH;
		}
	}


	public static class RotateLeft extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.ROTATE_LEFT;
		}
	}

	public static class RotateRight extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.ROTATE_RIGHT;
		}
	}

	public static class Rotate extends GenericCommand {
		private int angle;
		private double speed; // Is double necessary here?

		public Rotate(int angle, double speed) {
			this.angle = angle;
			this.speed = speed;
		}

		@Override
		protected int getOpcode() {
			return RobotOpcode.ROTATE_BY;
		}

		@Override
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException {
			super.sendToBrick(outputStream);
			outputStream.writeInt(angle);
			outputStream.writeDouble(speed);
		}
	}

	public static class TravelArc extends GenericCommand {
		private double arcRadius;
		private int distance;
		private int speed;

		public TravelArc(double arcRadius, int distance, int speed) {
			this.arcRadius = arcRadius;
			this.distance = distance;
			this.speed = speed;
		}

		@Override
		protected int getOpcode() {
			return RobotOpcode.ARC_FORWARDS;
		}

		@Override
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException {
			super.sendToBrick(outputStream);
			outputStream.writeDouble(arcRadius);
			outputStream.writeInt(distance);
			outputStream.writeInt(speed);
		}
	}

	public static class Travel extends GenericCommand {
		private int distance;
		private int travelSpeed;
		
		public Travel(int distance, int travelSpeed) {
			this.distance = distance;
			this.travelSpeed = travelSpeed;
		}
		
		@Override
		protected int getOpcode() {
			return RobotOpcode.TRAVEL;
		}
		
		@Override
		public void sendToBrick(DataOutputStream outputStream)
				throws IOException {
			super.sendToBrick(outputStream);
			outputStream.writeInt(distance);
			outputStream.writeInt(travelSpeed);
		}
	}
	
	public static class ResetCatcher extends GenericCommand {
		@Override
		protected int getOpcode() {
			return RobotOpcode.RESET_CATCHER;
		}
	}
}
