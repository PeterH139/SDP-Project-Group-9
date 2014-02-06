package pc.vision.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.VideoStream;
import pc.world.WorldState;
//import world.state.WorldState;

/**
 * Creates and maintains the swing-based Control GUI, which provides both
 * control manipulation (pitch choice, direction, etc) and threshold setting.
 * Also allows the saving/loading of threshold values to a file.
 * 
 * @author s0840449 - original
 * @author Alex Adams (s1046358) - heavy refactoring & improvements
 */
@SuppressWarnings("serial")
public class VisionSettingsPanel extends JPanel {
	public static final int MOUSE_MODE_OFF = 0;
	public static final int MOUSE_MODE_PITCH_BOUNDARY = 1;
	public static final int MOUSE_MODE_BLUE_T = 2;
	public static final int MOUSE_MODE_YELLOW_T = 3;
	public static final int MOUSE_MODE_GREEN_PLATES = 4;
	public static final int MOUSE_MODE_DIVISIONS = 5;
	public static final int MOUSE_MODE_TARGET = 6;

	// A PitchConstants class used to load/save constants for the pitch
	private final PitchConstants pitchConstants;

	// Stores information about the current world state, such as shooting
	// direction, ball location, etc
	//private final WorldState worldState;

	private final DistortionFix distortionFix;

	private boolean debugEnabled = true;

	private int mouseMode;

	// Load/Save buttons
	private JButton saveButton;
	private JButton loadButton;

	// Tabs
	private final JTabbedPane tabPane = new JTabbedPane();
	private final JPanel mainTabPanel = new JPanel();
	private final CameraSettingsPanel camPanel;

	// Radio buttons and their change listeners
	private final JRadioButton rdbtnPitch0 = new JRadioButton("Main");
	private final JRadioButton rdbtnPitch1 = new JRadioButton("Side Room");
	private final ActionListener pitchActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update the world state and pitch constants
			int pitchNum = VisionSettingsPanel.this.rdbtnPitch0.isSelected() ? 0 : 1;
			//worldState.setMainPitch(rdbtnPitch0.isSelected());
			//worldState.setPitch(pitchNum);
			VisionSettingsPanel.this.pitchConstants.setPitchNum(pitchNum);
		}
	};

	private final JRadioButton rdbtnYellow = new JRadioButton("Yellow");
	private final JRadioButton rdbtnBlue = new JRadioButton("Blue");
	private final ActionListener colourActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update which colour is ours
			//worldState.setColour(rdbtnBlue.isSelected() ? 1 : 0);
			//worldState.setWeAreBlue(rdbtnBlue.isSelected());
		}
	};

	private final JRadioButton rdbtnRight = new JRadioButton("Right");
	private final JRadioButton rdbtnLeft = new JRadioButton("Left");
	private final ActionListener directionActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update which direction the other team's goal is in
			//int isLeft = rdbtnLeft.isSelected() ? 1 : 0;
			//worldState.setDirection(isLeft);
			//worldState.setWeAreOnLeft(rdbtnLeft.isSelected());
		}
	};

	private final JRadioButton rdbtnDistortOn = new JRadioButton("On");
	private final JRadioButton rdbtnDistortOff = new JRadioButton("Off");
	private final ActionListener distortionActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update whether distortion is active
			if (VisionSettingsPanel.this.rdbtnDistortOn.isSelected()) {
				VisionSettingsPanel.this.distortionFix.setActive(true);
			} else {
				VisionSettingsPanel.this.distortionFix.setActive(false);
			}
		}
	};

	private final JRadioButton rdbtnDebugOn = new JRadioButton("On");
	private final JRadioButton rdbtnDebugOff = new JRadioButton("Off");
	private final ActionListener debugActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Update whether debug overlay is active
			if (VisionSettingsPanel.this.rdbtnDebugOn.isSelected()) {
				VisionSettingsPanel.this.debugEnabled = true;
			} else {
				VisionSettingsPanel.this.debugEnabled = false;
			}
		}
	};

	private final JRadioButton rdbtnMouseModeOff = new JRadioButton();
	private final JRadioButton rdbtnMouseModePitch = new JRadioButton();
	private final JRadioButton rdbtnMouseModeBlue = new JRadioButton();
	private final JRadioButton rdbtnMouseModeYellow = new JRadioButton();
	private final JRadioButton rdbtnMouseModeGreenPlates = new JRadioButton();
	private final JRadioButton rdbtnMouseModeGreyCircles = new JRadioButton();
	private final JRadioButton targetSelection = new JRadioButton();

	/**
	 * Default constructor.
	 * 
	 * @param worldState
	 *            A WorldState object to update the pitch choice, shooting
	 *            direction, etc.
	 * @param pitchConstants
	 *            A PitchConstants object to allow saving/loading of data.
	 */
	public VisionSettingsPanel(WorldState worldState,
			final PitchConstants pitchConstants, final VideoStream vStream,
			final DistortionFix distortionFix) {
		// Both state objects must not be null.
		assert (worldState != null) : "worldState is null";
		assert (pitchConstants != null) : "pitchConstants is null";

		//this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
		this.camPanel = new CameraSettingsPanel(vStream,
				System.getProperty("user.dir") + "/constants/pitch"
						+ pitchConstants.getPitchNum() + "camera");

		// The main (default) tab
		this.mainTabPanel.setLayout(new BoxLayout(this.mainTabPanel, BoxLayout.Y_AXIS));
		setUpMainPanel();

		this.tabPane.addTab("Main", this.mainTabPanel);
		this.tabPane.addTab("Camera", this.camPanel);

		this.add(this.tabPane);
		this.setSize(this.getPreferredSize());
	}

	/**
	 * Sets up the main tab, adding in the pitch choice, the direction choice,
	 * the robot-colour choice and save/load buttons.
	 */
	private void setUpMainPanel() {
		// Pitch choice
		JPanel pitchPanel = new JPanel();
		JLabel pitchLabel = new JLabel("Pitch:");
		pitchPanel.add(pitchLabel);

		ButtonGroup pitchChoice = new ButtonGroup();
		pitchChoice.add(this.rdbtnPitch0);
		pitchChoice.add(this.rdbtnPitch1);
		pitchPanel.add(this.rdbtnPitch0);
		pitchPanel.add(this.rdbtnPitch1);

		this.rdbtnPitch0.addActionListener(this.pitchActionListener);
		this.rdbtnPitch1.addActionListener(this.pitchActionListener);

		this.mainTabPanel.add(pitchPanel);

		// Colour choice
		JPanel colourPanel = new JPanel();
		JLabel colourLabel = new JLabel("Our colour:");
		colourPanel.add(colourLabel);

		ButtonGroup colourChoice = new ButtonGroup();
		colourChoice.add(this.rdbtnBlue);
		colourPanel.add(this.rdbtnBlue);
		colourChoice.add(this.rdbtnYellow);
		colourPanel.add(this.rdbtnYellow);

		this.rdbtnYellow.addActionListener(this.colourActionListener);
		this.rdbtnBlue.addActionListener(this.colourActionListener);

		this.mainTabPanel.add(colourPanel);

		// Direction choice
		JPanel directionPanel = new JPanel();
		JLabel directionLabel = new JLabel("Our goal:");
		directionPanel.add(directionLabel);

		ButtonGroup directionChoice = new ButtonGroup();
		directionChoice.add(this.rdbtnLeft);
		directionPanel.add(this.rdbtnLeft);
		directionChoice.add(this.rdbtnRight);
		directionPanel.add(this.rdbtnRight);

		this.rdbtnRight.addActionListener(this.directionActionListener);
		this.rdbtnLeft.addActionListener(this.directionActionListener);

		this.mainTabPanel.add(directionPanel);

		// Distortion
		JPanel distortionPanel = new JPanel();
		JLabel distortionLabel = new JLabel("Distortion Fix:");
		distortionPanel.add(distortionLabel);

		ButtonGroup distortionChoice = new ButtonGroup();
		distortionChoice.add(this.rdbtnDistortOn);
		distortionPanel.add(this.rdbtnDistortOn);
		distortionChoice.add(this.rdbtnDistortOff);
		distortionPanel.add(this.rdbtnDistortOff);

		this.rdbtnDistortOn.addActionListener(this.distortionActionListener);
		this.rdbtnDistortOff.addActionListener(this.distortionActionListener);

		this.mainTabPanel.add(distortionPanel);

		// Distortion
		JPanel debugPanel = new JPanel();
		JLabel debugLabel = new JLabel("Debug Overlay:");
		debugPanel.add(debugLabel);

		ButtonGroup debugChoice = new ButtonGroup();
		debugChoice.add(this.rdbtnDebugOn);
		debugPanel.add(this.rdbtnDebugOn);
		debugChoice.add(this.rdbtnDebugOff);
		debugPanel.add(this.rdbtnDebugOff);

		this.rdbtnDebugOn.addActionListener(this.debugActionListener);
		this.rdbtnDebugOff.addActionListener(this.debugActionListener);

		this.mainTabPanel.add(debugPanel);

		// Mouse mode selector
		JPanel mouseModePanel = new JPanel();
		GridBagLayout gbl_mouseModePanel = new GridBagLayout();
		// gbl_mouseModePanel.columnWidths = new int[]{41, 0, 0};
		// gbl_mouseModePanel.rowHeights = new int[]{36, 0, 0, 19, 0, 0};
		// gbl_mouseModePanel.columnWeights = new double[]{1.0,
		// Double.MIN_VALUE, 1.0};
		// gbl_mouseModePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0,
		// Double.MIN_VALUE};
		mouseModePanel.setLayout(gbl_mouseModePanel);
		JLabel mouseModeLabel = new JLabel("Mouse Mode");
		GridBagConstraints gbc_mouseModeLabel = new GridBagConstraints();
		gbc_mouseModeLabel.gridwidth = 2;
		gbc_mouseModeLabel.fill = GridBagConstraints.VERTICAL;
		gbc_mouseModeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeLabel.gridx = 0;
		gbc_mouseModeLabel.gridy = 0;
		mouseModePanel.add(mouseModeLabel, gbc_mouseModeLabel);

		ButtonGroup mouseModeChoice = new ButtonGroup();
		this.mainTabPanel.add(mouseModePanel);
		mouseModeChoice.add(this.rdbtnMouseModeOff);
		mouseModeChoice.add(this.rdbtnMouseModePitch);
		mouseModeChoice.add(this.rdbtnMouseModeBlue);
		mouseModeChoice.add(this.rdbtnMouseModeYellow);
		mouseModeChoice.add(this.rdbtnMouseModeGreenPlates);
		mouseModeChoice.add(this.rdbtnMouseModeGreyCircles);
		mouseModeChoice.add(this.targetSelection);

		GridBagConstraints gbc_rdbtnMouseModeOff = new GridBagConstraints();
		gbc_rdbtnMouseModeOff.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeOff.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeOff.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeOff.gridx = 0;
		gbc_rdbtnMouseModeOff.gridy = 1;
		mouseModePanel.add(this.rdbtnMouseModeOff, gbc_rdbtnMouseModeOff);
		this.rdbtnMouseModeOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModeOff.isSelected())
					setMouseMode(MOUSE_MODE_OFF);
			}
		});

		GridBagConstraints gbc_mouseModeOffLabel = new GridBagConstraints();
		gbc_mouseModeOffLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeOffLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeOffLabel.gridx = 1;
		gbc_mouseModeOffLabel.gridy = 1;
		JLabel mouseModeOffLabel = new JLabel("Off");
		mouseModeOffLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeOffLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModeOff.doClick();
			}
		});
		mouseModePanel.add(mouseModeOffLabel, gbc_mouseModeOffLabel);

		GridBagConstraints gbc_rdbtnMouseModePitch = new GridBagConstraints();
		gbc_rdbtnMouseModePitch.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModePitch.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModePitch.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModePitch.gridx = 0;
		gbc_rdbtnMouseModePitch.gridy = 2;
		mouseModePanel.add(this.rdbtnMouseModePitch, gbc_rdbtnMouseModePitch);
		this.rdbtnMouseModePitch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModePitch.isSelected())
					setMouseMode(MOUSE_MODE_PITCH_BOUNDARY);
			}
		});

		GridBagConstraints gbc_mouseModePitchLabel = new GridBagConstraints();
		gbc_mouseModePitchLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModePitchLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModePitchLabel.gridx = 1;
		gbc_mouseModePitchLabel.gridy = 2;
		JLabel mouseModePitchLabel = new JLabel("Pitch Boundary Selection");
		mouseModePitchLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModePitchLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModePitch.doClick();
			}
		});
		mouseModePanel.add(mouseModePitchLabel, gbc_mouseModePitchLabel);

		GridBagConstraints gbc_rdbtnMouseModeBlue = new GridBagConstraints();
		gbc_rdbtnMouseModeBlue.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeBlue.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeBlue.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeBlue.gridx = 0;
		gbc_rdbtnMouseModeBlue.gridy = 3;
		mouseModePanel.add(this.rdbtnMouseModeBlue, gbc_rdbtnMouseModeBlue);
		this.rdbtnMouseModeBlue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModeBlue.isSelected())
					setMouseMode(MOUSE_MODE_BLUE_T);
			}
		});

		GridBagConstraints gbc_mouseModeBlueLabel = new GridBagConstraints();
		gbc_mouseModeBlueLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeBlueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeBlueLabel.gridx = 1;
		gbc_mouseModeBlueLabel.gridy = 3;
		JLabel mouseModeBlueLabel = new JLabel("Blue T Plate Selection");
		mouseModeBlueLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeBlueLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModeBlue.doClick();
			}
		});
		mouseModePanel.add(mouseModeBlueLabel, gbc_mouseModeBlueLabel);

		GridBagConstraints gbc_rdbtnMouseModeYellow = new GridBagConstraints();
		gbc_rdbtnMouseModeYellow.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeYellow.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeYellow.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeYellow.gridx = 0;
		gbc_rdbtnMouseModeYellow.gridy = 4;
		mouseModePanel.add(this.rdbtnMouseModeYellow, gbc_rdbtnMouseModeYellow);
		this.rdbtnMouseModeYellow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModeYellow.isSelected())
					setMouseMode(MOUSE_MODE_YELLOW_T);
			}
		});

		GridBagConstraints gbc_mouseModeYellowLabel = new GridBagConstraints();
		gbc_mouseModeYellowLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeYellowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeYellowLabel.gridx = 1;
		gbc_mouseModeYellowLabel.gridy = 4;
		JLabel mouseModeYellowLabel = new JLabel("Yellow T Plate Selection");
		mouseModeYellowLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeYellowLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModeYellow.doClick();
			}
		});
		mouseModePanel.add(mouseModeYellowLabel, gbc_mouseModeYellowLabel);

		GridBagConstraints gbc_rdbtnMouseModeGreenPlates = new GridBagConstraints();
		gbc_rdbtnMouseModeGreenPlates.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeGreenPlates.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeGreenPlates.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeGreenPlates.gridx = 0;
		gbc_rdbtnMouseModeGreenPlates.gridy = 5;
		mouseModePanel.add(this.rdbtnMouseModeGreenPlates,
				gbc_rdbtnMouseModeGreenPlates);
		this.rdbtnMouseModeGreenPlates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModeGreenPlates.isSelected())
					setMouseMode(MOUSE_MODE_GREEN_PLATES);
			}
		});

		GridBagConstraints gbc_mouseModeGreenPlatesLabel = new GridBagConstraints();
		gbc_mouseModeGreenPlatesLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeGreenPlatesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeGreenPlatesLabel.gridx = 1;
		gbc_mouseModeGreenPlatesLabel.gridy = 5;
		JLabel mouseModeGreenPlatesLabel = new JLabel("Green Plate Selection");
		mouseModeGreenPlatesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeGreenPlatesLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModeGreenPlates.doClick();
			}
		});
		mouseModePanel.add(mouseModeGreenPlatesLabel,
				gbc_mouseModeGreenPlatesLabel);

		GridBagConstraints gbc_rdbtnMouseModeGreyCircles = new GridBagConstraints();
		gbc_rdbtnMouseModeGreyCircles.anchor = GridBagConstraints.EAST;
		gbc_rdbtnMouseModeGreyCircles.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMouseModeGreyCircles.fill = GridBagConstraints.VERTICAL;
		gbc_rdbtnMouseModeGreyCircles.gridx = 0;
		gbc_rdbtnMouseModeGreyCircles.gridy = 6;
		mouseModePanel.add(this.rdbtnMouseModeGreyCircles,
				gbc_rdbtnMouseModeGreyCircles);
		this.rdbtnMouseModeGreyCircles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.rdbtnMouseModeGreyCircles.isSelected())
					setMouseMode(MOUSE_MODE_DIVISIONS);
			}
		});

		GridBagConstraints gbc_mouseModeGreyCirclesLabel = new GridBagConstraints();
		gbc_mouseModeGreyCirclesLabel.anchor = GridBagConstraints.WEST;
		gbc_mouseModeGreyCirclesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mouseModeGreyCirclesLabel.gridx = 1;
		gbc_mouseModeGreyCirclesLabel.gridy = 6;
		JLabel mouseModeGreyCirclesLabel = new JLabel("Pitch Dividers Selection");
		mouseModeGreyCirclesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mouseModeGreyCirclesLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.rdbtnMouseModeGreyCircles.doClick();
			}
		});

		GridBagConstraints gbc_targetSelection = new GridBagConstraints();
		gbc_targetSelection.anchor = GridBagConstraints.EAST;
		gbc_targetSelection.insets = new Insets(0, 0, 5, 5);
		gbc_targetSelection.fill = GridBagConstraints.VERTICAL;
		gbc_targetSelection.gridx = 0;
		gbc_targetSelection.gridy = 7;
		mouseModePanel.add(this.targetSelection, gbc_targetSelection);
		this.targetSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (VisionSettingsPanel.this.targetSelection.isSelected())
					setMouseMode(MOUSE_MODE_TARGET);
			}
		});

		GridBagConstraints gbc_targetSelectionLabel = new GridBagConstraints();
		gbc_targetSelectionLabel.anchor = GridBagConstraints.WEST;
		gbc_targetSelectionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_targetSelectionLabel.gridx = 1;
		gbc_targetSelectionLabel.gridy = 7;
		JLabel targetSelectionLabel = new JLabel("Target Selection");
		targetSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		targetSelectionLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				VisionSettingsPanel.this.targetSelection.doClick();
			}
		});
		mouseModePanel.add(targetSelectionLabel, gbc_targetSelectionLabel);

		mouseModePanel.add(mouseModeGreyCirclesLabel,
				gbc_mouseModeGreyCirclesLabel);

		// Save/load buttons
		JPanel saveLoadPanel = new JPanel();

		this.saveButton = new JButton("Save Settings");
		this.saveButton.addMouseListener(new MouseAdapter() {
			// Attempt to write all of the current thresholds to a file with a
			// name
			// based on the currently selected pitch.
			@Override
			public void mouseClicked(MouseEvent e) {
				int pitchNum = VisionSettingsPanel.this.pitchConstants.getPitchNum();

				int result = JOptionPane.showConfirmDialog(VisionSettingsPanel.this.saveButton,
						"Are you sure you want to save current constants for pitch "
								+ pitchNum + "?");

				if (result == JOptionPane.NO_OPTION
						|| result == JOptionPane.CANCEL_OPTION)
					return;

				VisionSettingsPanel.this.pitchConstants.saveConstants(System.getProperty("user.dir")
						+ "/constants/pitch" + pitchNum);
				VisionSettingsPanel.this.camPanel.saveSettings(System.getProperty("user.dir")
						+ "/constants/pitch" + pitchNum + "camera");
			}
		});

		saveLoadPanel.add(this.saveButton);

		this.loadButton = new JButton("Load Settings");
		this.loadButton.addMouseListener(new MouseAdapter() {
			// Override the current threshold settings from those set in
			// the correct constants file for the current pitch.
			@Override
			public void mouseClicked(MouseEvent e) {
				int pitchNum = VisionSettingsPanel.this.rdbtnPitch0.isSelected() ? 0 : 1;

				int result = JOptionPane.showConfirmDialog(VisionSettingsPanel.this.loadButton,
						"Are you sure you want to load "
								+ "pre-saved constants for pitch " + pitchNum
								+ "?");

				if (result == JOptionPane.NO_OPTION
						|| result == JOptionPane.CANCEL_OPTION)
					return;

				VisionSettingsPanel.this.pitchConstants.setPitchNum(pitchNum);
				VisionSettingsPanel.this.camPanel.loadSettings(System.getProperty("user.dir")
						+ "/constants/pitch" + pitchNum + "camera");
			}
		});

		saveLoadPanel.add(this.loadButton);

		this.mainTabPanel.add(saveLoadPanel);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		this.rdbtnPitch0.doClick();
		this.rdbtnBlue.doClick();
		this.rdbtnLeft.doClick();
		this.rdbtnDistortOff.doClick();
		this.rdbtnDebugOn.doClick();
		this.rdbtnMouseModeOff.doClick();
	}

	public int getMouseMode() {
		return this.mouseMode;
	}

	public void setMouseMode(int mouseMode) {
		this.mouseMode = mouseMode;
	}

	public boolean isDebugEnabled() {
		return this.debugEnabled;
	}
}
