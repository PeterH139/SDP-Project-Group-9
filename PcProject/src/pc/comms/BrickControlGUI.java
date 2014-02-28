package pc.comms;

import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

@SuppressWarnings("serial")
public class BrickControlGUI extends JFrame implements KeyListener {
	private BrickCommServer brick;

	public BrickControlGUI(final BrickCommServer brick) {
		this.brick = brick;

		setTitle("Robot controller");
		setSize(400, 200);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				brick.close();
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
		RobotCommand.Command command = null;
		switch (key.getKeyCode()) {
		case KeyEvent.VK_UP:
			command = new RobotCommand.Travel(10000, 100);
			break;
		case KeyEvent.VK_DOWN:
			command = new RobotCommand.Travel(-10000, 100);
			break;
		case KeyEvent.VK_SPACE:
			command = new RobotCommand.Kick(30);
			break;
		case KeyEvent.VK_1:
			command = new RobotCommand.Catch();
			break;
		case KeyEvent.VK_LEFT:
			command = new RobotCommand.Rotate(-45, 45);
			break;
		case KeyEvent.VK_RIGHT:
			command = new RobotCommand.Rotate(45, 45);
			break;
		}
		if (command != null) {
			brick.execute(command);
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		brick.execute(new RobotCommand.Stop());
	}

	@Override
	public void keyTyped(KeyEvent key) {
	}

	public static void guiConnect(final BrickCommServer brick,
			final NXTInfo brickInfo) throws NXTCommException {
		new GUIConnect(brick, brickInfo);
	}

	public static void main(String[] args) throws NXTCommException {
		BrickCommServer bcs = new BrickCommServer();
		BrickControlGUI.guiConnect(bcs, BtInfo.MEOW);
		BrickControlGUI client = new BrickControlGUI(bcs);
		client.setVisible(true);
	}

	private static class GUIConnect {
		private JDialog window;
		private NXTCommException exception;

		public GUIConnect(final BrickCommServer brick, final NXTInfo brickInfo)
				throws NXTCommException {
			window = new JDialog(null, "Connecting",
					ModalityType.APPLICATION_MODAL);
			window.setSize(400, 150);
			window.setResizable(false);
			window.setLocationRelativeTo(null);
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			String protocol = brickInfo.protocol == NXTCommFactory.BLUETOOTH ? "Bluetooth"
					: "USB";
			JLabel label = new JLabel("Connecting to " + brickInfo.name
					+ " via " + protocol, JLabel.CENTER);
			window.add(label);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							try {
								brick.connect(brickInfo);
								break;
							} catch (NXTCommException e) {
								
								int result = JOptionPane.showConfirmDialog(
										window,
										"Connection failed. Retry?\n\n"
												+ e.getMessage() + "\n\n"
												+ e.getCause(),
										"Connection failed",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.ERROR_MESSAGE);
								if (result == JOptionPane.YES_OPTION)
									continue;
								
								exception = e;
								return;
							}
						}
					} finally {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								window.dispose();
							}
						});
					}
				}
			}).start();

			window.setVisible(true);
			if (exception != null)
				throw exception;
		}
	}
}
