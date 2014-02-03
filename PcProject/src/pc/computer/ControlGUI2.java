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

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.VideoStream;
import pc.vision.Vision;
import pc.vision.gui.VisionGUI;
import pc.world.WorldState;
import au.edu.jcu.v4l4j.V4L4JConstants;
//import movement.RobotMover;
//import strategy.calculations.GoalInfo;
//import strategy.planning.DribbleBall5;
//import strategy.planning.MainPlanner;
//import strategy.planning.PenaltyDefense;
//import strategy.planning.Strategy;
//import strategy.planning.StrategyInterface;
//import utility.SafeSleep;
//import world.state.RobotType;
//import world.state.WorldState;

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
		WorldState worldState = new WorldState();

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
			Vision vision = new Vision(worldState, pitchConstants);

			// Create the Control GUI for threshold setting/etc
			VisionGUI gui = new VisionGUI(width, height);

			vStream.addReceiver(distortionFix);
			distortionFix.addReceiver(gui);
			distortionFix.addReceiver(vision);
			vision.addVisionDebugReceiver(gui);
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

//		System.out.println("Robot ready!");

	}

	private void startMainPlanner() {
		assert (this.strategyThread == null || !this.strategyThread.isAlive()) : "Strategy is already running";
		//strategy = new MainPlanner(worldState, mover);
		//strategyThread = new Thread(strategy);
		//strategyThread.start();
	}

	private static void cleanQuit() {
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

		this.op1field.setColumns(6);
		this.op2field.setColumns(6);
		this.op3field.setColumns(6);
		this.op1field.setText("0");
		this.op2field.setText("0");
		this.op3field.setText("0");
		// Auto-generated GUI code (made more readable)
		GridBagLayout gridBagLayout = new GridBagLayout();
		this.getContentPane().setLayout(gridBagLayout);

		GridBagConstraints gbc_startStopQuitPanel = new GridBagConstraints();
		gbc_startStopQuitPanel.anchor = GridBagConstraints.NORTH;
		gbc_startStopQuitPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_startStopQuitPanel.insets = new Insets(0, 0, 5, 0);
		gbc_startStopQuitPanel.gridx = 0;
		gbc_startStopQuitPanel.gridy = 0;
		this.getContentPane().add(this.startStopQuitPanel, gbc_startStopQuitPanel);
		this.startStopQuitPanel.add(this.startButton);
		this.startStopQuitPanel.add(this.stopButton);
		this.startStopQuitPanel.add(this.resetButton);
		this.startStopQuitPanel.add(this.quitButton);
		this.startStopQuitPanel.add(this.forceQuitButton);
		this.startStopQuitPanel.add(this.stratStartButton);
		this.startStopQuitPanel.add(this.penaltyAtkButton);
		this.startStopQuitPanel.add(this.penaltyDefButton);

		GridBagConstraints gbc_simpleMoveTestPanel = new GridBagConstraints();
		gbc_simpleMoveTestPanel.anchor = GridBagConstraints.NORTH;
		gbc_simpleMoveTestPanel.fill = GridBagConstraints.VERTICAL;
		gbc_simpleMoveTestPanel.insets = new Insets(0, 0, 5, 0);
		gbc_simpleMoveTestPanel.gridx = 0;
		gbc_simpleMoveTestPanel.gridy = 1;
		// gbc_simpleMoveTestPanel.gridwidth = 2;
		this.getContentPane().add(this.optionsPanel, gbc_simpleMoveTestPanel);
		this.optionsPanel.add(this.op1label);
		this.optionsPanel.add(this.op1field);
		this.optionsPanel.add(this.op2label);
		this.optionsPanel.add(this.op2field);
		this.optionsPanel.add(this.op3label);
		this.optionsPanel.add(this.op3field);

		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		this.getContentPane().add(this.simpleMovePanel, gbc_panel);
		this.simpleMovePanel.add(this.forwardButton);
		this.simpleMovePanel.add(this.backwardButton);
		this.simpleMovePanel.add(this.leftButton);
		this.simpleMovePanel.add(this.rightButton);
		this.simpleMovePanel.add(this.kickButton);
		this.simpleMovePanel.add(this.dribblerStart);
		this.simpleMovePanel.add(this.dribblerStop);

		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 3;
		this.getContentPane().add(this.complexMovePanel, gbc_panel_1);
		this.complexMovePanel.add(this.rotateButton);
		this.complexMovePanel.add(this.moveButton);
		this.complexMovePanel.add(this.moveToButton);
		this.complexMovePanel.add(this.rotateAndMoveButton);

		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 4;
		this.getContentPane().add(this.moveTargetPanel, gbc_panel_2);
		this.moveTargetPanel.add(this.moveNoCollTarget);
		this.moveTargetPanel.add(this.moveNoCollOppTarget);

		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 5;
		this.getContentPane().add(this.moveTargetOptionsPanel, gbc_panel_3);
		op4field.setColumns(6);
		op5field.setColumns(6);
		op4field.setText("" + 100);
		op5field.setText("" + 100);
		this.moveTargetOptionsPanel.add(this.op4label);
		this.moveTargetOptionsPanel.add(op4field);
		this.moveTargetOptionsPanel.add(this.op5label);
		this.moveTargetOptionsPanel.add(op5field);

		this.complexMovePanel.add(this.dribbleButton);

		this.addWindowListener(new ListenCloseWdw());

		this.startButton.addActionListener(new ActionListener() {
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

		this.stopButton.addActionListener(new ActionListener() {
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
				if (ControlGUI2.this.dribbleThread != null && ControlGUI2.this.dribbleThread.isAlive()) {
					System.out.println("Killing dribble thread");
					//DribbleBall5.die = true;
					//mover.interruptMove();
					try {
						ControlGUI2.this.dribbleThread.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				// Stop strategy if it's running
				if (ControlGUI2.this.strategyThread != null && ControlGUI2.this.strategyThread.isAlive()) {
					System.out.println("Killing strategy thread");
					//DribbleBall5.die = true;
					//Strategy.stop();
					//strategy.kill();
					try {
						ControlGUI2.this.strategyThread.join(3000);
						if (ControlGUI2.this.strategyThread.isAlive()) {
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
		this.stratStartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Allow restart of strategies after previously killing all
				// strategies
				//Strategy.reset();

				startMainPlanner();
			}
		});

		this.penaltyAtkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int angle = Integer.parseInt(ControlGUI2.this.op1field.getText());
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

		this.penaltyDefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//strategy = new PenaltyDefense(worldState, mover);
				//strategyThread = new Thread(strategy);
				//strategyThread.start();
			}
		});

		this.kickButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.kick();
			}
		});

		this.dribblerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());
				//mover.dribble(op1);
			}
		});

		this.dribblerStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.stopdribble();
			}
		});

		this.forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());
				//mover.move(0, op1);
			}
		});

		this.backwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());

				//mover.move(0, -op1);
			}
		});

		this.leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());

				//mover.move(-op1, 0);
			}
		});

		this.rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());

				//mover.move(op1, 0);
			}
		});

		this.dribbleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ControlGUI2.this.dribbleThread == null || !ControlGUI2.this.dribbleThread.isAlive()) {
					ControlGUI2.this.dribbleThread = new DribbleBallThread();
					ControlGUI2.this.dribbleThread.start();
				} else {
					System.out.println("Dribble is already active!");
				}
			}
		});

		this.rotateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int angle = Integer.parseInt(ControlGUI2.this.op1field.getText());

				//mover.rotate(Math.toRadians(angle));
			}
		});

		this.moveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());
				int op2 = Integer.parseInt(ControlGUI2.this.op2field.getText());

				//mover.move(op1, op2);
			}
		});

		this.moveToButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());
				int op2 = Integer.parseInt(ControlGUI2.this.op2field.getText());

				//mover.moveToAndStop(op1, op2);
			}
		});

		this.rotateAndMoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int op1 = Integer.parseInt(ControlGUI2.this.op1field.getText());
				int op2 = Integer.parseInt(ControlGUI2.this.op2field.getText());
				int op3 = Integer.parseInt(ControlGUI2.this.op3field.getText());

				//robot.rotateMove(op1, op2, op3);
			}
		});

		this.resetButton.addActionListener(new ActionListener() {
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

		this.quitButton.addActionListener(new ActionListener() {
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

		this.forceQuitButton.addActionListener(new ActionListener() {
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

		this.moveNoCollTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//mover.moveToAStar(Integer.parseInt(op4field.getText()), Integer.parseInt(op5field.getText()), false, true);
			}
		});

		this.moveNoCollOppTarget.addActionListener(new ActionListener() {
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
