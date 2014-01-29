package pc.vision.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.VideoStream;
import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.VisionDebugReceiver;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

@SuppressWarnings("serial")
public class VisionGUI extends JFrame implements VideoReceiver,
		VisionDebugReceiver, WorldStateReceiver {
	private final int videoWidth;
	private final int videoHeight;

	// Pitch dimension selector variables
	private boolean selectionActive = false;
	private Point anchor;
	private int a;
	private int b;
	private int c;
	private int d;
	private int currentDivider;

	// Stored to only have rendering happen in one place
	private BufferedImage frame;
	private int fps;
	private int frameCounter;
	private BufferedImage debugOverlay;

	// Mouse listener variables
	boolean letterAdjustment = false;
	boolean yellowPlateAdjustment = false;
	boolean bluePlateAdjustment = false;
	boolean targetAdjustment = false;
	int mouseX;
	int mouseY;
	String adjust = "";
	File currentFile;
	File imgLetterT = new File("icons/Tletter2.png");
	File imgYellowPlate = new File("icons/YellowPlateSelector.png");
	File imgBluePlate = new File("icons/BluePlateSelector.png");
	File imgGreyCircle = new File("icons/GreyCircleSelector.png");
	BufferedImage selectorImage = null;
	BufferedImage letterTSelectorImage = null;
	BufferedImage yellowPlateSelectorImage = null;
	BufferedImage bluePlateSelectorImage = null;
	BufferedImage greyCircleSelectorImage = null;
	ArrayList<?>[] extractedColourSettings;
	double imageCenterX;
	double imageCenterY;
	int rotation = 0;
	ArrayList<Integer> xList = new ArrayList<Integer>();
	ArrayList<Integer> yList = new ArrayList<Integer>();

	private final PitchConstants pitchConstants;
	private final VisionSettingsPanel settingsPanel;
	private final JPanel videoDisplay = new JPanel();
	private final WindowAdapter windowAdapter = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			dispose();

			System.exit(0);
		}
	};

	public VisionGUI(final int videoWidth, final int videoHeight,
			WorldState worldState, final PitchConstants pitchConsts,
			final VideoStream vStream, final DistortionFix distortionFix) {

		super("Vision");
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;

		// Set pitch constraints
		this.pitchConstants = pitchConsts;
		this.a = this.pitchConstants.getLeftBuffer();
		this.b = this.pitchConstants.getTopBuffer();
		this.c = this.videoWidth - this.pitchConstants.getRightBuffer() - this.a;
		this.d = this.videoHeight - this.pitchConstants.getBottomBuffer() - this.b;

		try {
			// Image T
			this.letterTSelectorImage = ImageIO.read(this.imgLetterT);
			// Image Yellow plate
			this.yellowPlateSelectorImage = ImageIO.read(this.imgYellowPlate);
			// Image Blue plate
			this.bluePlateSelectorImage = ImageIO.read(this.imgBluePlate);
			// Image Grey circle
			this.greyCircleSelectorImage = ImageIO.read(this.imgGreyCircle);
		} catch (IOException e) {
			System.out.println("Images not found");
			e.printStackTrace();
		}

		Container contentPane = this.getContentPane();

		Dimension videoSize = new Dimension(videoWidth, videoHeight);
		BufferedImage blankInitialiser = new BufferedImage(videoWidth,
				videoHeight, BufferedImage.TYPE_INT_RGB);
		getContentPane().setLayout(null);
		this.videoDisplay.setLocation(0, 0);
		this.videoDisplay.setMinimumSize(videoSize);
		this.videoDisplay.setSize(videoSize);
		contentPane.add(this.videoDisplay);

		this.settingsPanel = new VisionSettingsPanel(worldState,
				this.pitchConstants, vStream, distortionFix);

		this.settingsPanel.setLocation(videoSize.width, 0);
		contentPane.add(this.settingsPanel);

		this.setVisible(true);
		this.getGraphics().drawImage(blankInitialiser, 0, 0, null);

		this.settingsPanel.setSize(this.settingsPanel.getPreferredSize());
		Dimension frameSize = new Dimension(videoWidth
				+ this.settingsPanel.getPreferredSize().width, Math.max(videoHeight,
				this.settingsPanel.getPreferredSize().height));
		contentPane.setSize(frameSize);
		this.setSize(frameSize.width + 8, frameSize.height + 30);
		// Wait for size to actually be set before setting resizable to false.
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		this.setResizable(false);
		this.videoDisplay.setFocusable(true);
		this.videoDisplay.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
				//TODO Empty Block
			}

			public void keyReleased(KeyEvent ke) {
				VisionGUI.this.adjust = KeyEvent.getKeyText(ke.getKeyCode());
			}

			public void keyTyped(KeyEvent e) {
				//TODO Empty Block
			}
		});

		MouseInputAdapter mouseSelector = new MouseInputAdapter() {
			Rectangle selection;

			public void mousePressed(MouseEvent e) {
				switch (VisionGUI.this.settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:

					VisionGUI.this.selectionActive = true;
					System.out.println("Initialised anchor");
					// Pitch dimension selector
					VisionGUI.this.anchor = e.getPoint();
					System.out.println(VisionGUI.this.anchor.x);
					System.out.println(VisionGUI.this.anchor.y);
					this.selection = new Rectangle(VisionGUI.this.anchor);
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					VisionGUI.this.videoDisplay.grabFocus();
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					VisionGUI.this.videoDisplay.grabFocus();
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					VisionGUI.this.videoDisplay.grabFocus();
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
					VisionGUI.this.videoDisplay.grabFocus();
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_TARGET:
					VisionGUI.this.videoDisplay.grabFocus();
					//WorldState.targetX = e.getX();
					//WorldState.targetY = e.getY();
					break;
				}
			}

			public void mouseDragged(MouseEvent e) {
				switch (VisionGUI.this.settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
					this.selection.setBounds((int) Math.min(VisionGUI.this.anchor.x, e.getX()),
							(int) Math.min(VisionGUI.this.anchor.y, e.getY()),
							(int) Math.abs(e.getX() - VisionGUI.this.anchor.x),
							(int) Math.abs(e.getY() - VisionGUI.this.anchor.y));
					VisionGUI.this.a = (int) Math.min(VisionGUI.this.anchor.x, e.getX());
					VisionGUI.this.b = (int) Math.min(VisionGUI.this.anchor.y, e.getY());
					VisionGUI.this.c = (int) Math.abs(e.getX() - VisionGUI.this.anchor.x);
					VisionGUI.this.d = (int) Math.abs(e.getY() - VisionGUI.this.anchor.y);
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
					VisionGUI.this.mouseX = e.getX();
					VisionGUI.this.mouseY = e.getY();
					break;
				}
			}

			public void mouseReleased(MouseEvent e) {

				switch (VisionGUI.this.settingsPanel.getMouseMode()) {
				case VisionSettingsPanel.MOUSE_MODE_OFF:
					break;
				case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
					VisionGUI.this.selectionActive = false;

					if (e.getPoint().distance(VisionGUI.this.anchor) > 5) {
						Object[] options = { "Main Pitch", "Side Pitch",
								"Cancel" };
						int pitchNum = JOptionPane.showOptionDialog(
								getComponent(0),
								"The parameters are to be set for this pitch",
								"Picking a pitch",
								JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);

						// If option wasn't Cancel and the dialog wasn't closed
						if (pitchNum != 2
								&& pitchNum != JOptionPane.CLOSED_OPTION) {
							System.out.println(pitchNum);
							int top = VisionGUI.this.b;
							int bottom = videoHeight - VisionGUI.this.d - VisionGUI.this.b;
							int left = VisionGUI.this.a;
							int right = videoWidth - VisionGUI.this.c - VisionGUI.this.a;

							if (top > 0 && bottom > 0 && left > 0
									&& right > 0) {
								// Update pitch constants
								VisionGUI.this.pitchConstants.setTopBuffer(top);
								VisionGUI.this.pitchConstants.setBottomBuffer(bottom);
								VisionGUI.this.pitchConstants.setLeftBuffer(left);
								VisionGUI.this.pitchConstants.setRightBuffer(right);
								VisionGUI.this.pitchConstants.saveConstants(pitchNum);
							} else {
								System.out.println("Pitch selection NOT succesful");
							}
							System.out.print("Top: " + top + " Bottom "
									+ bottom);
							System.out.println(" Right " + right + " Left "
									+ left);

							System.out.println("A: " + VisionGUI.this.a + " B: " + VisionGUI.this.b + " C: "
									+ VisionGUI.this.c + " D:" + VisionGUI.this.d);
						} else if (pitchNum == JOptionPane.CLOSED_OPTION
								|| pitchNum == 2) {
							System.out.println("Closed option picked");
							VisionGUI.this.a = VisionGUI.this.pitchConstants.getLeftBuffer();
							VisionGUI.this.b = VisionGUI.this.pitchConstants.getTopBuffer();
							VisionGUI.this.c = videoWidth - VisionGUI.this.pitchConstants.getRightBuffer()
									- VisionGUI.this.pitchConstants.getLeftBuffer();
							VisionGUI.this.d = videoHeight - VisionGUI.this.pitchConstants.getTopBuffer()
									- VisionGUI.this.pitchConstants.getBottomBuffer();
						}
						repaint();
					}
					break;
				case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
					VisionGUI.this.letterAdjustment = true;
					VisionGUI.this.selectorImage = VisionGUI.this.letterTSelectorImage;
					VisionGUI.this.currentFile = VisionGUI.this.imgLetterT;
					// Get the center coordinates of the selector image in use
					VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage.getWidth(null) / 2;
					VisionGUI.this.imageCenterY = VisionGUI.this.selectorImage.getHeight(null) / 2;
					break;
				case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
					VisionGUI.this.letterAdjustment = true;
					VisionGUI.this.currentFile = VisionGUI.this.imgLetterT;
					VisionGUI.this.selectorImage = VisionGUI.this.letterTSelectorImage;
					// Get the center coordinates of the selector image in use
					VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage.getWidth(null) / 2;
					VisionGUI.this.imageCenterY = VisionGUI.this.selectorImage.getHeight(null) / 2;
					break;
				case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
					if (!VisionGUI.this.bluePlateAdjustment) {
						VisionGUI.this.yellowPlateAdjustment = true;
						VisionGUI.this.currentFile = VisionGUI.this.imgYellowPlate;
						VisionGUI.this.selectorImage = VisionGUI.this.yellowPlateSelectorImage;
						// Get the center coordinates of the selector image in
						// use
						VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage.getWidth(null) / 2;
						VisionGUI.this.imageCenterY = VisionGUI.this.selectorImage.getHeight(null) / 2;
					}
					break;
				case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
					System.out.println("Division selection mode");
					int[] ds = VisionGUI.this.pitchConstants.getDividers();
					ds[VisionGUI.this.currentDivider] = e.getX();
					VisionGUI.this.currentDivider++;
					if (VisionGUI.this.currentDivider == 3){
						VisionGUI.this.currentDivider = 0;
					}
					break;
				case VisionSettingsPanel.MOUSE_MODE_TARGET:
					System.out.println("target mode");
					VisionGUI.this.targetAdjustment = true;
					VisionGUI.this.currentFile = VisionGUI.this.imgGreyCircle;
					VisionGUI.this.selectorImage = VisionGUI.this.greyCircleSelectorImage;
					// Get the center coordinates of the selector image in use
					VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage.getWidth(null) / 2;
					VisionGUI.this.imageCenterY = VisionGUI.this.selectorImage.getHeight(null) / 2;
					break;
				}
			}
		};

		this.videoDisplay.addMouseListener(mouseSelector);
		this.videoDisplay.addMouseMotionListener(mouseSelector);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.addWindowListener(this.windowAdapter);
	}

	@Override
	public void sendFrame(BufferedImage frame, int fps, int frameCounter) {
		this.frame = frame;
		this.fps = fps;
		this.frameCounter = frameCounter;
	}

	@Override
	public void sendDebugOverlay(BufferedImage debug) {
		// Use the image passed if debug is enabled
		if (this.settingsPanel.isDebugEnabled()) {
			this.debugOverlay = debug;
		}
		// Otherwise discard it and create a new image to work with
		else {
			this.debugOverlay = new BufferedImage(debug.getWidth(),
					debug.getHeight(), debug.getType());
		}
		Graphics debugGraphics = this.debugOverlay.getGraphics();
		Graphics2D g2d = (Graphics2D) debugGraphics;

		// Selected mode in the Vision GUI
		boolean mouseModeBlueT = this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_BLUE_T;
		boolean mouseModeYellowT = this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_YELLOW_T;
		boolean mouseModeGreenPlates = this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES;
		boolean mouseModeGreyCircle = this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_DIVISIONS;
		boolean mouseSelectTarget = this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_TARGET; // moo
		// If the colour selection mode is on (for colour calibration from the
		// image)
		if (mouseSelectTarget) {
			//g2d.drawOval(WorldState.targetX, WorldState.targetY, 5, 5);
			//ControlGUI2.op4field.setText("" + WorldState.targetX);
			//ControlGUI2.op5field.setText("" + WorldState.targetY);
		}

		if (mouseModeBlueT || mouseModeYellowT || mouseModeGreenPlates
				|| mouseModeGreyCircle) {
			// Show the colour selector image
			if (this.letterAdjustment || this.yellowPlateAdjustment
					|| this.bluePlateAdjustment) {
				g2d.drawImage(this.selectorImage, this.mouseX, this.mouseY, null);
			}
			// Controlling the selector image
			rotationControl(this.settingsPanel.getMouseMode());
		}
		
		// Drawing the dividing lines
		int[] ds = this.pitchConstants.getDividers();
		int top = this.pitchConstants.getTopBuffer();
		int bot = this.videoHeight - this.pitchConstants.getBottomBuffer();
		debugGraphics.setColor(Color.WHITE);
		debugGraphics.drawLine(ds[0], bot, ds[0], top);
		debugGraphics.drawString("1", ds[0], bot+20);
		debugGraphics.drawLine(ds[1], bot, ds[1], top);
		debugGraphics.drawString("2", ds[1], bot+20);
		debugGraphics.drawLine(ds[2], bot, ds[2], top);
		debugGraphics.drawString("3", ds[2], bot+20);
		
		// Eliminating area around the pitch dimensions
		if (!this.selectionActive) {
			int a = this.pitchConstants.getLeftBuffer();
			int b = this.pitchConstants.getTopBuffer();
			int c = this.videoWidth - this.pitchConstants.getRightBuffer() - a;
			int d = this.videoHeight - this.pitchConstants.getBottomBuffer() - b;
			// Making the pitch surroundings transparent
			Composite originalComposite = g2d.getComposite();
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite alphaComp = (AlphaComposite.getInstance(type, 0.6f));
			g2d.setComposite(alphaComp);
			debugGraphics.setColor(Color.BLACK);
			// Rectangle covering the BOTTOM
			debugGraphics.fillRect(0, 0, this.videoWidth, b);
			// Rectangle covering the LEFT
			debugGraphics.fillRect(0, b, a, this.videoHeight);
			// Rectangle covering the BOTTOM
			debugGraphics.fillRect(a + c, b, this.videoWidth - a, this.videoHeight - b);
			// Rectangle covering the RIGHT
			debugGraphics.fillRect(a, b + d, c, this.videoHeight - d);
			// Setting back normal settings
			g2d.setComposite(originalComposite);
		}
		
		if (this.settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY) {
			// Draw the line around the pitch dimensions
			if (this.selectionActive) {
				debugGraphics.setColor(Color.YELLOW);
				debugGraphics.drawRect(this.a, this.b, this.c, this.d);
			}
		}
	}
	
	@Override
	public void sendWorldState(WorldState worldState) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Graphics frameGraphics = this.frame.getGraphics();

		// Draw overlay on top of raw frame
		frameGraphics.drawImage(this.debugOverlay, 0, 0, null);

		// Draw frame info and worldstate on top of the result
		// Display the FPS that the vision system is running at
		frameGraphics.setColor(Color.white);
		frameGraphics.drawString("Frame: " + this.frameCounter, 15, 15);
		frameGraphics.drawString("FPS: " + this.fps, 15, 30);

		// Display Ball & Robot Positions
		frameGraphics.drawString("Ball:", 15, 45);
		frameGraphics.drawString("(" + worldState.getBallX() + ", "
				+ worldState.getBallY() + ")", 60, 45);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getBallXVelocity()) + ", "
						+ df.format(worldState.getBallYVelocity()) + ")", 140,
				45);

		frameGraphics.drawString("Blue:", 15, 60);
		frameGraphics.drawString("(" + worldState.getBlueX() + ", "
				+ worldState.getBlueY() + ")", 60, 60);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getBlueXVelocity()) + ", "
						+ df.format(worldState.getBlueYVelocity()) + ")", 140,
				60);
		frameGraphics.drawString(
				"angle: "
						+ df.format(Math.toDegrees(worldState
								.getBlueOrientation())), 260, 60);

		frameGraphics.drawString("Yellow:", 15, 75);
		frameGraphics.drawString("(" + worldState.getYellowX() + ", "
				+ worldState.getYellowY() + ")", 60, 75);
		frameGraphics.drawString(
				"vel: (" + df.format(worldState.getYellowXVelocity()) + ", "
						+ df.format(worldState.getYellowYVelocity()) + ")",
				140, 75);
		frameGraphics.drawString(
				"angle: "
						+ df.format(Math.toDegrees(worldState
								.getYellowOrientation())), 260, 75);

		// Mark goals:
//		Position leftGoal = worldState.goalInfo.getLeftGoalCenter();
//		Position rightGoal = worldState.goalInfo.getRightGoalCenter();

//		frameGraphics.setColor(Color.yellow);
//		frameGraphics.drawOval(leftGoal.getX() - 2, leftGoal.getY() - 2, 4, 4);
//		frameGraphics
//				.drawOval(rightGoal.getX() - 2, rightGoal.getY() - 2, 4, 4);

//		Position leftGoalTop = worldState.goalInfo.getLeftGoalTop();
//		Position leftGoalBottom = worldState.goalInfo.getLeftGoalBottom();
//		frameGraphics.drawLine(leftGoalTop.getX(), leftGoalTop.getY(),
//				leftGoalBottom.getX(), leftGoalBottom.getY());

//		Position rightGoalTop = worldState.goalInfo.getRightGoalTop();
//		Position rightGoalBottom = worldState.goalInfo.getRightGoalBottom();
//		frameGraphics.drawLine(rightGoalTop.getX(), rightGoalTop.getY(),
//				rightGoalBottom.getX(), rightGoalBottom.getY());

		// Draw overall composite to screen
		Graphics videoGraphics = this.videoDisplay.getGraphics();
		videoGraphics.drawImage(this.frame, 0, 0, null);
	}

	public void rotationControl(int mouseMode) {

		int object = -1;

		switch (mouseMode) {
		case (VisionSettingsPanel.MOUSE_MODE_BLUE_T):
			object = PitchConstants.BLUE;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_YELLOW_T):
			object = PitchConstants.YELLOW;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES):
			object = PitchConstants.GREEN;
			break;
		case (VisionSettingsPanel.MOUSE_MODE_DIVISIONS):
			object = PitchConstants.GREY;
			break;
		}

		// Control the selector images using the keyboard
		if (this.letterAdjustment || this.yellowPlateAdjustment || this.bluePlateAdjustment) {
			if (this.adjust.equals("Up")) {
				this.mouseY--;
			} else if (this.adjust.equals("Down")) {
				this.mouseY++;
			} else if (this.adjust.equals("Left")) {
				this.mouseX--;
			} else if (this.adjust.equals("Right")) {
				this.mouseX++;
			} else if (this.adjust.equals("Z")) {
				rotateSelectorImage(Math.toRadians((double) this.rotation--));
			} else if (this.adjust.equals("X")) {
				rotateSelectorImage(Math.toRadians((double) this.rotation++));
			} else if (this.adjust.equals("A")) {
				this.rotation -= 10;
				rotateSelectorImage(Math.toRadians((double) this.rotation));
			} else if (this.adjust.equals("S")) {
				this.rotation += 10;
				rotateSelectorImage(Math.toRadians((double) this.rotation));
			} else if (this.adjust.equals("Enter")) {
				if (this.letterAdjustment) {
					this.letterAdjustment = false;
					this.extractedColourSettings = getColourRange(this.frame, object);
					setColourRange(this.extractedColourSettings, object);
					clearArrayOfLists(this.extractedColourSettings);
				} else if (this.yellowPlateAdjustment) {
					this.yellowPlateAdjustment = false;
					this.bluePlateAdjustment = true;
					this.extractedColourSettings = getColourRange(this.frame, object);
					this.selectorImage = this.bluePlateSelectorImage;
					this.currentFile = this.imgBluePlate;
				} else if (this.bluePlateAdjustment) {
					this.bluePlateAdjustment = false;
					this.extractedColourSettings = getColourRange(this.frame, object);
					setColourRange(this.extractedColourSettings, object);
					clearArrayOfLists(this.extractedColourSettings);
				}
			}
			this.adjust = "";
		}
	}

	public void rotateSelectorImage(double rotationRequired) {
		AffineTransform tx = AffineTransform.getRotateInstance(
				rotationRequired, this.imageCenterX, this.imageCenterY);
		AffineTransformOp op = new AffineTransformOp(tx,
				AffineTransformOp.TYPE_BILINEAR);
		// Reset the original selector image so it is not blurry
		try {
			this.selectorImage = ImageIO.read(this.currentFile);
		} catch (IOException e) {
			// TODO Empty Catch Block
		}
		this.selectorImage = op.filter(this.selectorImage, null);
	}

	public ArrayList<?>[] getColourRange(BufferedImage frame, int object) {

		ArrayList<Integer> redList = new ArrayList<Integer>();
		ArrayList<Integer> greenList = new ArrayList<Integer>();
		ArrayList<Integer> blueList = new ArrayList<Integer>();
		ArrayList<Float> hueList = new ArrayList<Float>();
		ArrayList<Float> satList = new ArrayList<Float>();
		ArrayList<Float> valList = new ArrayList<Float>();
		ArrayList<?>[] colourSettings = { redList, greenList, blueList, hueList,
				satList, valList };

		if (object == PitchConstants.BLUE || object == PitchConstants.YELLOW) {
			/** PROCESSING EITHER LETTER T */
			// Process top part of the letter T
			colourSettings = addColourValues(frame, colourSettings, 12, 35, 15,
					24);
			// Process bottom part of the letter T
			colourSettings = addColourValues(frame, colourSettings, 21, 30, 24,
					44);
		} else if (object == PitchConstants.GREEN) {
			/** PROCESSING EITHER OF THE GREEN PLATES */
			// Process the top left quadrant of the green plate
			colourSettings = addColourValues(frame, colourSettings, 0 + 15,
					10 + 15, 0 + 25, 15 + 25);
			// Process the top right quadrant of the green plate
			colourSettings = addColourValues(frame, colourSettings, 21 + 15,
					30 + 15, 0 + 25, 15 + 25);
			// Process the bottom left quadrant of the green plate
			colourSettings = addColourValues(frame, colourSettings, 0 + 15,
					10 + 15, 25 + 25, 50 + 25);
			// Process the bottom right quadrant of the green plate
			colourSettings = addColourValues(frame, colourSettings, 22 + 15,
					30 + 15, 25 + 25, 50 + 25);
		} else if (object == PitchConstants.GREY) {
			// Process the ball
			colourSettings = addColourValues(frame, colourSettings, 0, 8, 0, 8);
		}
		return colourSettings;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<?>[] addColourValues(BufferedImage frame,
			ArrayList[] colourSettings, int fromX, int toX, int fromY, int toY) {
		int lx = (int) this.imageCenterX;
		int ly = (int) this.imageCenterY;

		for (int x = fromX - lx; x < toX - lx; x++)
			for (int y = fromY - ly; y < toY - ly; y++) {

				// Getting the colour from pixels subject to rotation
				double xR = x * Math.cos(Math.toRadians((double) this.rotation)) - y
						* Math.sin(Math.toRadians((double) this.rotation));
				double yR = x * Math.sin(Math.toRadians((double) this.rotation)) + y
						* Math.cos(Math.toRadians((double) this.rotation));

				this.xList.add(this.mouseX + lx + (int) xR);
				this.yList.add(this.mouseY + ly + (int) yR);

				Color c = new Color(frame.getRGB(this.mouseX + lx + (int) xR, this.mouseY
						+ ly + (int) yR));

				float[] hsbvals = Color.RGBtoHSB(c.getRed(), c.getGreen(),
						c.getBlue(), null);

				colourSettings[0].add(c.getRed()); // RED
				colourSettings[1].add(c.getGreen()); // GREEN
				colourSettings[2].add(c.getBlue()); // BLUE

				colourSettings[3].add(hsbvals[0]); // HUE
				colourSettings[4].add(hsbvals[1]); // SATURATION
				colourSettings[5].add(hsbvals[2]); // VALUE

			}

		this.rotation = 0;
		return colourSettings;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setColourRange(ArrayList[] colourSettings, int object) {
		/** Mean and Standard deviation calculations for the RGB and HSB values */
		// RED LIST
		double meanR = calcMean(colourSettings[0]);
		double stdevR = calcStandardDeviation(colourSettings[0]);
		// GREEN LIST
		double meanG = calcMean(colourSettings[1]);
		double stdevG = calcStandardDeviation(colourSettings[1]);
		// BLUE LIST
		double meanB = calcMean(colourSettings[2]);
		double stdevB = calcStandardDeviation(colourSettings[2]);
		// HUE LIST
		double meanH = calcMeanFloat(colourSettings[3]);
		double stdevH = calcStandardDeviationFloat(colourSettings[3]);
		// SATURATION LIST
		double meanS = calcMeanFloat(colourSettings[4]);
		double stdevS = calcStandardDeviationFloat(colourSettings[4]);
		// VALUE LIST
		double meanV = calcMeanFloat(colourSettings[5]);
		double stdevV = calcStandardDeviationFloat(colourSettings[5]);

		System.out.println("Red mean " + meanR);
		System.out.println("Green mean " + meanG);
		System.out.println("Blue mean " + meanB);
		System.out.println("Red std " + stdevR);
		System.out.println("Green std " + stdevG);
		System.out.println("Blue std " + stdevB);
		System.out.println("H mean " + meanH);
		System.out.println("S mean " + meanS);
		System.out.println("V mean " + meanV);
		System.out.println("H std " + stdevH);
		System.out.println("S std " + stdevS);
		System.out.println("V std " + stdevV);

		/** Setting the sliders in the GUI */
		double stDevConstant = 2;

		this.pitchConstants.setRedLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanR - stDevConstant
						* stdevR)));
		this.pitchConstants.setRedUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanR + stDevConstant
						* stdevR)));

		this.pitchConstants.setGreenLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanG - stDevConstant
						* stdevG)));
		this.pitchConstants.setGreenUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanG + stDevConstant
						* stdevG)));

		this.pitchConstants.setBlueLower(
				object,
				Math.max(PitchConstants.RGBMIN, (int) (meanB - stDevConstant
						* stdevB)));
		this.pitchConstants.setBlueUpper(
				object,
				Math.min(PitchConstants.RGBMAX, (int) (meanB + stDevConstant
						* stdevB)));

		// Works best with the Hue range 0-1 for the blue and yellow Ts
		this.pitchConstants.setHueLower(object,
				Math.max(PitchConstants.HSVMIN, (float) (0)));
		this.pitchConstants.setHueUpper(object,
				Math.min(PitchConstants.HSVMAX, (float) (1)));

		this.pitchConstants.setSaturationLower(
				object,
				Math.max(PitchConstants.HSVMIN, (float) (meanS - stDevConstant
						* stdevS)));
		this.pitchConstants.setSaturationUpper(
				object,
				Math.min(PitchConstants.HSVMAX, (float) (meanS + stDevConstant
						* stdevS)));

		this.pitchConstants.setValueLower(
				object,
				Math.max(PitchConstants.HSVMIN, (float) (meanV - stDevConstant
						* stdevV)));
		this.pitchConstants.setValueUpper(
				object,
				Math.min(PitchConstants.HSVMAX, (float) (meanV + stDevConstant
						* stdevV)));

		this.settingsPanel.reloadSliderDefaults();

	}

	public void clearArrayOfLists(ArrayList<?>[] arrays) {
		for (int i = 0; i < arrays.length; i++)
			arrays[i].clear();
	}

	public double calcStandardDeviationFloat(ArrayList<Float> points) {

		double mean = calcMeanFloat(points);
		double sum = 0;
		for (int i = 0; i < points.size(); i++) {
			float p = points.get(i);
			double diff = p - mean;
			sum += diff * diff;
		}

		return Math.sqrt(sum / points.size());
	}

	public double calcMeanFloat(ArrayList<Float> points) {
		float sum = 0;
		for (int i = 0; i < points.size(); i++) {
			sum += points.get(i);
		}
		return (double) (sum) / points.size();
	}

	public double calcStandardDeviation(ArrayList<Integer> points) {

		double mean = calcMean(points);
		double sum = 0;
		for (int i = 0; i < points.size(); i++) {
			int p = points.get(i);
			double diff = p - mean;
			sum += diff * diff;
		}

		return Math.sqrt(sum / points.size());
	}

	public double calcMean(ArrayList<Integer> points) {
		int sum = 0;
		for (int i = 0; i < points.size(); i++) {
			sum += points.get(i);
		}
		return (double) (sum) / points.size();
	}

}
