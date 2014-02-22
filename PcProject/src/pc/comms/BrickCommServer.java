package pc.comms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import reallejos.shared.RobotOpcode;

public class BrickCommServer {
	NXTComm comm;
	DataInputStream brickInput;
	DataOutputStream brickOutput;

	private ExecutorService executor;

	public BrickCommServer() {
		executor = Executors.newSingleThreadExecutor();
	}

	public void connect(NXTInfo brickInfo) throws NXTCommException {
		comm = NXTCommFactory.createNXTComm(brickInfo.protocol);
		if (!comm.open(brickInfo))
			return;
		brickInput = new DataInputStream(comm.getInputStream());
		brickOutput = new DataOutputStream(comm.getOutputStream());
	}

	public void close() {
		try {
			if (comm != null)
				comm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executes a command asynchronously. Returns immediately and is safe to
	 * call from any thread.
	 */
	public void execute(final RobotCommand.Command command) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					BrickCommServer.this.executeSync(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Executes a command synchronously. Never call this method from GUI or
	 * frame grabber thread!
	 */
	public void executeSync(RobotCommand.Command command) throws IOException {
		command.sendToBrick(brickOutput);
		brickOutput.flush();
	}

	// Legacy methods

	@Deprecated
	public void robotStop() throws IOException {
		brickOutput.writeInt(RobotOpcode.STOP);
		brickOutput.flush();
	}

	@Deprecated
	public void robotForwards() throws IOException {
		brickOutput.writeInt(RobotOpcode.FORWARDS);
		brickOutput.flush();
	}

	@Deprecated
	public void robotBackwards() throws IOException {
		brickOutput.writeInt(RobotOpcode.BACKWARDS);
		brickOutput.flush();
	}

	@Deprecated
	public void robotKick(int speed) throws IOException {
		brickOutput.writeInt(RobotOpcode.KICK);
		brickOutput.writeInt(speed);
		brickOutput.flush();
	}

	@Deprecated
	public void robotCatch() throws IOException {
		brickOutput.writeInt(RobotOpcode.CATCH);
		brickOutput.flush();
	}

	@Deprecated
	public void robotPrepCatch() throws IOException {
		brickOutput.writeInt(RobotOpcode.APPROACHING_BALL);
		brickOutput.flush();
	}

	@Deprecated
	public void robotRotate(boolean clockwise) throws IOException {
		brickOutput.writeInt(clockwise ? RobotOpcode.ROTATE_RIGHT
				: RobotOpcode.ROTATE_LEFT);
		brickOutput.flush();
	}

	@Deprecated
	public void robotRotateBy(int angle, double speed) throws IOException {
		brickOutput.writeInt(RobotOpcode.ROTATE_BY);
		brickOutput.writeInt(angle);
		brickOutput.writeDouble(speed);
		brickOutput.flush();
	}

	@Deprecated
	public void robotArcForwards(double arcRadius, int distance)
			throws IOException {
		brickOutput.writeInt(RobotOpcode.ARC_FORWARDS);
		brickOutput.writeDouble(arcRadius);
		brickOutput.writeInt(distance);
		brickOutput.flush();
	}

	@Deprecated
	public void robotTravel(int distance, int travelSpeed) throws IOException {
		brickOutput.writeInt(RobotOpcode.TRAVEL);
		brickOutput.writeInt(distance);
		brickOutput.writeInt(travelSpeed);
		brickOutput.flush();

	}
	
	public boolean robotTest() throws IOException {
		brickOutput.writeInt(RobotOpcode.TEST);
		brickOutput.flush();
		boolean robotReceived = brickInput.readBoolean();
		return robotReceived;
	}

	public boolean robotTestINT(int param) throws IOException {
		brickOutput.writeInt(RobotOpcode.TESTINT);
		brickOutput.writeInt(param);
		brickOutput.flush();
		boolean robotReceived = brickInput.readBoolean();
		return robotReceived;
	}

	public boolean robotTestDOUBLE(double param) throws IOException {
		brickOutput.writeInt(RobotOpcode.TESTDOUBLE);
		brickOutput.writeDouble(param);
		brickOutput.flush();
		boolean robotReceived = brickInput.readBoolean();
		return robotReceived;
	}

	public boolean robotTestINTANDDOUBLE(int paramInt, double paramDouble)
			throws IOException {
		brickOutput.writeInt(RobotOpcode.TESTINTANDDOUBLE);
		brickOutput.writeDouble(paramDouble);
		brickOutput.writeInt(paramInt);
		brickOutput.flush();
		boolean robotReceived = brickInput.readBoolean();
		return robotReceived;
	}
}
