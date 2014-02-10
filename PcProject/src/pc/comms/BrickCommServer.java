package pc.comms;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import reallejos.shared.RobotOpcode;

public class BrickCommServer {
	NXTComm comm;
	DataInputStream brickInput;
	DataOutputStream brickOutput;

	public BrickCommServer() {
	}

	public void connect(NXTInfo brickInfo) throws NXTCommException {
		comm = NXTCommFactory.createNXTComm(brickInfo.protocol);
		if (!comm.open(brickInfo))
			return;
		brickInput = new DataInputStream(comm.getInputStream());
		brickOutput = new DataOutputStream(comm.getOutputStream());
	}

	public void guiConnect(NXTInfo brickInfo) throws NXTCommException {
		JFrame window = new JFrame("Connecting");
		window.setSize(400, 150);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		String protocol = brickInfo.protocol == NXTCommFactory.BLUETOOTH ? "Bluetooth"
				: "USB";
		JLabel label = new JLabel("Connecting to " + brickInfo.name + " via "
				+ protocol, JLabel.CENTER);
		window.add(label);
		window.setVisible(true);
		try {
			while (true) {
				try {
					connect(brickInfo);
					break;
				} catch (NXTCommException e) {
					int result = JOptionPane.showConfirmDialog(window,
							"Connection failed. Retry?\n\n" + e.getMessage()
									+ "\n\n" + e.getCause(),
							"Connection failed", JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE);
					if (result == JOptionPane.YES_OPTION)
						continue;
					throw e;
				}
			}
		} finally {
			window.setVisible(false);
			window.dispose();
		}
	}

	public void close() {
		try {
			if (comm != null)
				comm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void robotStop() throws IOException {
		brickOutput.writeInt(RobotOpcode.STOP);
		brickOutput.flush();
	}

	public void robotForwards() throws IOException {
		brickOutput.writeInt(RobotOpcode.FORWARDS);
		brickOutput.flush();
	}

	public void robotBackwards() throws IOException {
		brickOutput.writeInt(RobotOpcode.BACKWARDS);
		brickOutput.flush();
	}

	public void robotKick(int speed) throws IOException {
		brickOutput.writeInt(RobotOpcode.KICK);
		brickOutput.writeInt(speed);
		brickOutput.flush();
	}

	public void robotCatch() throws IOException {
		brickOutput.writeInt(RobotOpcode.CATCH);
		brickOutput.flush();
	}
	public void robotPrepCatch() throws IOException {
		brickOutput.writeInt(RobotOpcode.APPROACHING_BALL);
		brickOutput.flush();
	}

	public void robotRotate(boolean clockwise) throws IOException {
		brickOutput.writeInt(clockwise ? RobotOpcode.ROTATE_RIGHT
				: RobotOpcode.ROTATE_LEFT);
		brickOutput.flush();
	}

	public void robotRotateBy(int angle) throws IOException {
		brickOutput.writeInt(RobotOpcode.ROTATE_BY);
		brickOutput.writeInt(angle);
		brickOutput.flush();
	}
	
	public void robotWheelSpeed(double speed) throws IOException {
		brickOutput.writeInt(RobotOpcode.WHEEL_SPEED);
		brickOutput.writeDouble(speed);
		brickOutput.flush();
	}

	public void robotArcForwards(double arcRadius, int distance)
			throws IOException {
		brickOutput.writeInt(RobotOpcode.ARC_FORWARDS);
		brickOutput.writeDouble(arcRadius);
		brickOutput.writeInt(distance);
		brickOutput.flush();
	}

	public void robotTravel(int distance, int travelSpeed) throws IOException {
		brickOutput.writeInt(RobotOpcode.TRAVEL);
		brickOutput.writeInt(distance);
		brickOutput.writeInt(travelSpeed);
		brickOutput.flush();
		//brickInput.readBoolean();
	}
	
	public boolean robotTest() throws IOException {
		brickOutput.writeInt(RobotOpcode.TEST);
		brickOutput.flush();
		boolean robotReceived = brickInput.readBoolean();
		return robotReceived;
	}

	public static void main(String[] args) throws NXTCommException {
		BrickCommServer bcs = new BrickCommServer();
		bcs.guiConnect(BtInfo.group10);
		GUIClient client = bcs.new GUIClient();
		client.setVisible(true);
	}

	@SuppressWarnings("serial")
	public class GUIClient extends JFrame implements KeyListener {
		public GUIClient() {
			setTitle("Robot controller");
			setSize(400, 200);
			setResizable(false);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					BrickCommServer.this.close();
				}
			});

			JLabel label = new JLabel("Use arrow keys to control the robot.",
					JLabel.CENTER);
			label.setFocusable(true);
			add(label);

			label.addKeyListener(this);
		}

		@Override
		public void keyPressed(KeyEvent key) {
			try {
				switch (key.getKeyCode()) {
				case KeyEvent.VK_UP:
					BrickCommServer.this.robotForwards();
					break;
				case KeyEvent.VK_DOWN:
					BrickCommServer.this.robotBackwards();
					break;
				case KeyEvent.VK_SPACE:
					BrickCommServer.this.robotKick(600);
					break;
				case KeyEvent.VK_1:
					BrickCommServer.this.robotPrepCatch();
					break;
				case KeyEvent.VK_2:
					BrickCommServer.this.robotCatch();
					break;
				case KeyEvent.VK_LEFT:
					BrickCommServer.this.robotRotate(false);
					break;
				case KeyEvent.VK_RIGHT:
					BrickCommServer.this.robotRotate(true);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void keyReleased(KeyEvent key) {
			try {
				BrickCommServer.this.robotStop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}
	}
}
