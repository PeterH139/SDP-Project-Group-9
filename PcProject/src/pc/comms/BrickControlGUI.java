package pc.comms;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import lejos.pc.comm.NXTCommException;

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
			command = new RobotCommand.Kick(100);
			break;
		case KeyEvent.VK_1:
			command = new RobotCommand.PrepareCatcher();
			break;
		case KeyEvent.VK_2:
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
		try {
			brick.robotStop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent key) {
	}
	

	public static void main(String[] args) throws NXTCommException {
		BrickCommServer bcs = new BrickCommServer();
		bcs.guiConnect(BtInfo.group10);
		BrickControlGUI client = new BrickControlGUI(bcs);
		client.setVisible(true);
	}
}
