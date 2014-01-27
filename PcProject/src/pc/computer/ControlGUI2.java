package pc.computer;

// UI imports
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

//import movement.RobotMover;
//import strategy.calculations.GoalInfo;
//import strategy.planning.DribbleBall5;
//import strategy.planning.MainPlanner;
//import strategy.planning.PenaltyDefense;
//import strategy.planning.Strategy;
//import strategy.planning.StrategyInterface;
//import utility.SafeSleep;
import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.VideoStream;
import pc.vision.Vision;
import pc.vision.gui.VisionGUI;
//import world.state.RobotType;
//import world.state.WorldState;
import au.edu.jcu.v4l4j.V4L4JConstants;

//import communication.BluetoothCommunication;
//import communication.BluetoothRobot;
//import communication.DeviceInfo;
//import communication.RobotController;

// testing changes to this file.

@SuppressWarnings("serial")
public class ControlGUI2 extends JFrame {
	// GUI elements
	private final JPanel startStopQuitPanel = new JPanel();
	private final JPanel optionsPanel = new JPanel();
	private final JPanel simpleMovePanel = new JPanel();
	private final JPanel complexMovePanel = new JPanel();
	private final JPanel moveTargetPanel = new JPanel();
	private final JPanel moveTargetOptionsPanel = new JPanel();
	// General control buttons
	private final JButton startButton = new JButton("Start");
	private final JButton resetButton = new JButton("Reset");
	private final JButton quitButton = new JButton("Quit");
	private final JButton forceQuitButton = new JButton("Force quit");
	private final JButton stopButton = new JButton("Stop");
	private final JButton stratStartButton = new JButton("Strat Start");
	private final JButton penaltyAtkButton = new JButton("Penalty Attack");
	private final JButton penaltyDefButton = new JButton("Penalty Defend");
	private final JButton moveNoCollTarget = new JButton("Move while avoiding just opponent");
	private final JButton moveNoCollOppTarget = new JButton("Move while avoiding all obstacles");
	// Basic movement
	private final JButton forwardButton = new JButton("Forward");
	private final JButton backwardButton = new JButton("Backward");
	private final JButton leftButton = new JButton("Left");
	private final JButton rightButton = new JButton("Right");
	// Kick
	private final JButton kickButton = new JButton("Kick");
	private final JButton dribblerStart = new JButton("DribblerStart");
	private final JButton dribblerStop = new JButton("DribblerStop");
	// Other movement
	private final JButton rotateButton = new JButton("Rotate");
	private final JButton moveButton = new JButton("Move");
	private final JButton moveToButton = new JButton("Move To");
	private final JButton rotateAndMoveButton = new JButton("Rotate & Move");
	private final JButton dribbleButton = new JButton("Dribble");

	// OPcode fields
	private final JLabel op1label = new JLabel("Option 1: ");
	private final JLabel op2label = new JLabel("Option 2: ");
	private final JLabel op3label = new JLabel("Option 3: ");
	private final JLabel op4label = new JLabel("Move to (x label): ");
	private final JLabel op5label = new JLabel("Move to (y label): ");
	private final JTextField op1field = new JTextField();
	private final JTextField op2field = new JTextField();
	private final JTextField op3field = new JTextField();
	public static JTextField op4field = new JTextField();
	public static JTextField op5field = new JTextField();

	//private DribbleBall5 dribbleBall = new DribbleBall5();
	private DribbleBallThread dribbleThread;

	//private WorldState worldState;

	private Thread strategyThread;
	//private StrategyInterface strategy;

	//private final RobotController robot;
	//private RobotMover mover;

	public static void main(String[] args) throws IOException {
		// Make the GUI pretty
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Default to main pitch
		PitchConstants pitchConstants = new PitchConstants(0);
		//GoalInfo goalInfo = new GoalInfo(pitchConstants);
		//WorldState worldState = new WorldState(goalInfo);

		// Default values for the main vision window
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 80;

		try {
			VideoStream vStream = new VideoStream(videoDevice, width, height, channel, videoStandard, compressionQuality);

			DistortionFix distortionFix = new DistortionFix(pitchConstants);

			// Create a new Vision object to serve the main vision window
			//Vision vision = new Vision(worldState, pitchConstants);
			Vision vision = new Vision(pitchConstants);

			// Create the Control GUI for threshold setting/etc
			//VisionGUI gui = new VisionGUI(width, height, worldState, pitchConstants, vStream, distortionFix);
			VisionGUI gui = new VisionGUI(width, height, pitchConstants, vStream, distortionFix);

			vStream.addReceiver(distortionFix);
			distortionFix.addReceiver(gui);
			distortionFix.addReceiver(vision);
			vision.addVisionDebugReceiver(gui);
			vision.addWorldStateReceiver(gui);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Sets up the communication
		//BluetoothCommunication comms = new BluetoothCommunication(DeviceInfo.NXT_NAME, DeviceInfo.NXT_MAC_ADDRESS);
		// Sets up robot
		//BluetoothRobot robot = new BluetoothRobot(RobotType.Us, comms);

		// Sets up the GUI
		//ControlGUI2 gui = new ControlGUI2(worldState, robot);
		ControlGUI2 gui = new ControlGUI2();
		gui.setVisible(true);

		//robot.connect();

//		while (!robot.isConnected()) {
//			// Reduce CPU cost
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//				System.exit(1);
//			}
//		}

		System.out.println("Robot ready!");

	}

	private void startMainPlanner() {
		assert (strategyThread == null || !strategyThread.isAlive()) : "Strategy is already running";
		//strategy = new MainPlanner(worldState, mover);
		//strategyThread = new Thread(strategy);
		//strategyThread.start();
	}

	private void cleanQuit() {
		//robot.clearBuff();
		//if (robot.isConnected())
		//	robot.disconnect();
		System.exit(0);
	}

	//public ControlGUI2(final WorldState worldState, final RobotController robot) {
	public ControlGUI2() {
		//this.worldState = worldState;
		//this.robot = robot;
		//this.mover = new RobotMover(worldState, robot);
		//this.mover.start();

		this.setTitle("Group 4 control GUI");

		op1field.setColumns(6);
		op2field.setColumns(6);
		op3field.setColumns(6);
		op1field.setText("0");
		op2field.setText("0");
		op3field.setText("0");
		// Auto-generated GUI code (made more readable)
		GridBagLayout gridBagLayout = new GridBagLayout();
		this.getContentPane().setLayout(gridBagLayout);

		GridBagConstraints gbc_startStopQuitPanel = new GridBagConstraints();
		gbc_startStopQuitPanel.anchor = GridBagConstraints.NORTH;
		gbc_startStopQuitPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_startStopQuitPanel.insets = new Insets(0, 0, 5, 0);
		gbc_startStopQuitPanel.gridx = 0;
		gbc_startStopQuitPanel.gridy = 0;
		this.getContentPane().add(startStopQuitPanel, gbc_startStopQuitPanel);
		startStopQuitPanel.add(startButton);
		startStopQuitPanel.add(stopButton);
		startStopQuitPanel.add(resetButton);
		startStopQuitPanel.add(quitButton);
		startStopQuitPanel.add(forceQuitButton);
		startStopQuitPanel.add(stratStartButton);
		startStopQuitPanel.add(penaltyAtkButton);
		startStopQuitPanel.add(penaltyDefButton);

		GridBagConstraints gbc_simpleMoveTestPanel = new GridBagConstraints();
		gbc_simpleMoveTestPanel.anchor = GridBagConstraints.NORTH;
		gbc_simpleMoveTestPanel.fill = GridBagConstraints.VERTICAL;
		gbc_simpleMoveTestPanel.insets = new Insets(0, 0, 5, 0);
		gbc_simpleMoveTestPanel.gridx = 0;
		gbc_simpleMoveTestPanel.gridy = 1;
		// gbc_simpleMoveTestPanel.gridwidth = 2;
		this.getContentPane().add(optionsPanel, gbc_simpleMoveTestPanel);
		optionsPanel.add(op1label);
		optionsPanel.add(op1field);
		optionsPanel.add(op2label);
		optionsPanel.add(op2field);
		optionsPanel.add(op3label);
		optionsPanel.add(op3field);

		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		this.getContentPane().add(simpleMovePanel, gbc_panel);
		simpleMovePanel.add(forwardButton);
		simpleMovePanel.add(backwardButton);
		simpleMovePanel.add(leftButton);
		simpleMovePanel.add(rightButton);
		simpleMovePanel.add(kickButton);
		simpleMovePanel.add(dribblerStart);
		simpleMovePanel.add(dribblerStop);

		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		this.getContentPane().add(complexMovePanel, gbc_panel_1);
		complexMovePanel.add(rotateButton);
		complexMovePanel.add(moveButton);
		complexMovePanel.add(moveToButton);
		complexMovePanel.add(rotateAndMoveButton);

		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 4;
		this.getContentPane().add(moveTargetPanel, gbc_panel_2);
		moveTargetPanel.add(moveNoCollTarget);
		moveTargetPanel.add(moveNoCollOppTarget);

		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 5;
		this.getContentPane().add(moveTargetOptionsPanel, gbc_panel_3);
		op4field.setColumns(6);
		op5field.setColumns(6);
		op4field.setText("" + 100);
		op5field.setText("" + 100);
		moveTargetOptionsPanel.add(op4label);
		moveTargetOptionsPanel.add(op4field);
		moveTargetOptionsPanel.add(op5label);
		moveTargetOptionsPanel.add(op5field);

		complexMovePanel.add(dribbleButton);

		this.addWindowListener(new ListenCloseWdw());

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if (strategyThread == null || !strategyThread.isAlive()) {
				// Strategy.reset();
				// strategy = new OffenseSimple(worldState, mover);
				// strategyThread = new Thread(strategy);
				// strategyThread.start();
				//
				//
				// } else {
				// System.err.println("Strategy already active!");
				// }
				//System.out.println("Distance to ball: " + worldState.distanceToBall());
			}
		});

		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Halt and clear active movements
				//mover.interruptMove();
//				try {
//					mover.resetQueue();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				// Stop the dribble thread if it's running
				if (dribbleThread != null && dribbleThread.isAlive()) {
					System.out.println("Killing dribble thread");
					//DribbleBall5.die = true;
					//mover.interruptMove();
					try {
						dribbleThread.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				// Stop strategy if it's running
				if (strategyThread != null && strategyThread.isAlive()) {
					System.out.println("Killing strategy thread");
					//DribbleBall5.die = true;
					//Strategy.stop();
					//strategy.kill();
					try {
						strategyThread.join(3000);
						if (strategyThread.isAlive()) {
							System.out.println("Strategy failed to stop");
							cleanQuit();
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				System.out.println("Stopping the robot");
				// Stop the robot.
				//mover.stopRobot();
			}
		});

		// Run the strategy from here.
		stratStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Allow restart of strategies after previously killing all
				// strategies
				//Strategy.reset();

				startMainPlanner();
			}
		});

		penaltyAtkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int angle = Integer.parseInt(op1field.getText());
				if (angle != 0) {
					//int a = robot.rotate(angle);
				}
//				try {
//					SafeSleep.sleep(200);
//				} catch (InterruptedException e2) {
//					// TODO Auto-generated catch block
//					e2.printStackTrace();
//				}
				//int b = robot.kick();
//				try {
//					SafeSleep.sleep(100);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				// dribbleThread = new DribbleBallThread();
				// dribbleThread.start();
			}
		});

		penaltyDefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//strategy = new PenaltyDefense(worldState, mover);
				//strategyThread = new Thread(strategy);
				//strategyThread.start();
			}
		});

		kickButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.kick();
			}
		});

		dribblerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());
				//mover.dribble(op1);
			}
		});

		dribblerStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.stopdribble();
			}
		});

		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());
				//mover.move(0, op1);
			}
		});

		backwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());

				//mover.move(0, -op1);
			}
		});

		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());

				//mover.move(-op1, 0);
			}
		});

		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());

				//mover.move(op1, 0);
			}
		});

		dribbleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dribbleThread == null || !dribbleThread.isAlive()) {
					dribbleThread = new DribbleBallThread();
					dribbleThread.start();
				} else {
					System.out.println("Dribble is already active!");
				}
			}
		});

		rotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int angle = Integer.parseInt(op1field.getText());

				//mover.rotate(Math.toRadians(angle));
			}
		});

		moveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());
				int op2 = Integer.parseInt(op2field.getText());

				//mover.move(op1, op2);
			}
		});

		moveToButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());
				int op2 = Integer.parseInt(op2field.getText());

				//mover.moveToAndStop(op1, op2);
			}
		});

		rotateAndMoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(op1field.getText());
				int op2 = Integer.parseInt(op2field.getText());
				int op3 = Integer.parseInt(op3field.getText());

				//robot.rotateMove(op1, op2, op3);
			}
		});

		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Disconnecting...");
				//Strategy.alldie = true;
				// Kill the mover and wait for it to stop completely
//				if (mover.isAlive()) {
//					try {
//						mover.kill();
//						mover.join(3000);
//						// If the mover still hasn't stopped within 3
//						// seconds,
//						// assume it's stuck and kill the program
//						if (mover.isAlive()) {
//							System.out.println("Could not kill mover! Shutting down GUI...");
//							cleanQuit();
//						}
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//				}
				//robot.disconnect();
				System.out.println("Disconnected succesfully");
				System.out.println("Reconnecting...");
				try {
					Thread.sleep(400);
					//robot.connect();
					//mover = new RobotMover(worldState, robot);
					//mover.start();
					System.out.println("Reconnected successfully!");
				} catch (Exception e1) {
					System.out.println("Failed to reconnect! Shutting down GUI...");
					cleanQuit();
				}
			}
		});

		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Strategy.alldie = true;
				// Kill the mover and wait for it to stop completely
//				try {
//					mover.kill();
//					// If the mover still hasn't stopped within 3 seconds,
//					// assume it's stuck and kill the program the hard way
//					mover.join(3000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}
				System.out.println("Quitting the GUI");
				cleanQuit();
			}
		});

		forceQuitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Strategy.alldie = true;
				// Kill the mover and wait for it to stop completely
//				try {
//					mover.kill();
//					// If the mover still hasn't stopped within 3 seconds,
//					// assume it's stuck and kill the program the hard way
//					mover.join(3000);
//				} catch (InterruptedException e1) {
//					e1.printStackTrace();
//				}

				System.out.println("Quitting the GUI");
				//robot.clearBuff();
				//robot.forcequit();
				System.exit(0);
			}
		});

		moveNoCollTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.moveToAStar(Integer.parseInt(op4field.getText()), Integer.parseInt(op5field.getText()), false, true);
			}
		});

		moveNoCollOppTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.moveToAStar(Integer.parseInt(op4field.getText()), Integer.parseInt(op5field.getText()), true, true);
			}
		});

		// Center the window on startup
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getPreferredSize();
		this.setLocation((dim.width - frameSize.width) / 2, (dim.height - frameSize.height) / 2);
		this.setResizable(false);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
	}

	public class ListenCloseWdw extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			//Strategy.alldie = true;
			// Kill the mover and wait for it to stop completely
//			try {
//				mover.kill();
//				// If the mover still hasn't stopped within 3 seconds,
//				// assume it's stuck and kill the program the hard way
//				mover.join(3000);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			System.out.println("Quitting the GUI");
			cleanQuit();
		}
	}

	class DribbleBallThread extends Thread {
		public void run() {
//			try {
//				DribbleBall5.die = false;
//				dribbleBall.dribbleBall(worldState, mover);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}
}
