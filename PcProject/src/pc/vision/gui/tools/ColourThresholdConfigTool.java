package pc.vision.gui.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.PixelInfo;
import pc.vision.VideoStream;
import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;
import pc.vision.gui.VisionSettingsPanel;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.WorldState;

public class ColourThresholdConfigTool implements GUITool {
	private VisionGUI gui;
	private JFrame subWindow;

	private WorldState worldState;
	private PitchConstants pitchConstants;
	private VideoStream videoStream;
	private DistortionFix distortionFix;

	private VisionSettingsPanel settingsPanel;

	private final int videoWidth, videoHeight;

	// Pitch dimension selector variables
	private boolean selectionActive = false;
	private Point anchor;
	private int a;
	private int b;
	private int c;
	private int d;
	private int currentDivider;

	// Mouse listener variables
	boolean letterAdjustment = false;
	boolean yellowPlateAdjustment = false;
	boolean bluePlateAdjustment = false;
	boolean targetAdjustment = false;
	int mouseX;
	int mouseY;

	private MouseInputAdapter mouseSelector = new MouseInputAdapter() {
		Rectangle selection;

		public void mousePressed(MouseEvent e) {
			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:

				selectionActive = true;
				System.out.println("Initialised anchor");
				// Pitch dimension selector
				anchor = e.getPoint();
				System.out.println(anchor.x);
				System.out.println(anchor.y);
				this.selection = new Rectangle(anchor);
				break;
			case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_TARGET:
				worldState.setRobotTargetX(e.getX());
				worldState.setRobotTargetY(e.getY());
				break;
			}
		}

		public void mouseDragged(MouseEvent e) {
			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
				this.selection.setBounds((int) Math.min(anchor.x, e.getX()),
						(int) Math.min(anchor.y, e.getY()),
						(int) Math.abs(e.getX() - anchor.x),
						(int) Math.abs(e.getY() - anchor.y));
				a = (int) Math.min(anchor.x, e.getX());
				b = (int) Math.min(anchor.y, e.getY());
				c = (int) Math.abs(e.getX() - anchor.x);
				d = (int) Math.abs(e.getY() - anchor.y);
				break;
			case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			}
		}

		public void mouseReleased(MouseEvent e) {

			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
				selectionActive = false;

				if (e.getPoint().distance(anchor) > 5) {
					Object[] options = { "Main Pitch", "Side Pitch", "Cancel" };
					int pitchNum = JOptionPane.showOptionDialog(gui,
							"The parameters are to be set for this pitch",
							"Picking a pitch",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);

					// If option wasn't Cancel and the dialog wasn't closed
					if (pitchNum != 2 && pitchNum != JOptionPane.CLOSED_OPTION) {
						System.out.println(pitchNum);
						int top = b;
						int bottom = gui.getVideoHeight() - d - b;
						int left = a;
						int right = gui.getVideoWidth() - c - a;

						if (top > 0 && bottom > 0 && left > 0 && right > 0) {
							// Update pitch constants
							pitchConstants.setTopBuffer(top);
							pitchConstants.setBottomBuffer(bottom);
							pitchConstants.setLeftBuffer(left);
							pitchConstants.setRightBuffer(right);
							pitchConstants.saveConstants(pitchNum);
						} else {
							System.out.println("Pitch selection NOT succesful");
						}
						System.out.print("Top: " + top + " Bottom " + bottom);
						System.out.println(" Right " + right + " Left " + left);

						System.out.println("A: " + a + " B: " + b + " C: " + c
								+ " D:" + d);
					} else if (pitchNum == JOptionPane.CLOSED_OPTION
							|| pitchNum == 2) {
						System.out.println("Closed option picked");
						a = pitchConstants.getLeftBuffer();
						b = pitchConstants.getTopBuffer();
						c = gui.getVideoWidth()
								- pitchConstants.getRightBuffer()
								- pitchConstants.getLeftBuffer();
						d = gui.getVideoHeight()
								- pitchConstants.getTopBuffer()
								- pitchConstants.getBottomBuffer();
					}
					gui.getVideoDisplay().repaint();
				}
				break;
			case VisionSettingsPanel.MOUSE_MODE_BLUE_T:
				letterAdjustment = true;
				/*
				 * VisionGUI.this.selectorImage =
				 * VisionGUI.this.letterTSelectorImage;
				 * VisionGUI.this.currentFile = VisionGUI.this.imgLetterT; //
				 * Get the center coordinates of the selector image in use
				 * VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage
				 * .getWidth(null) / 2; VisionGUI.this.imageCenterY =
				 * VisionGUI.this.selectorImage .getHeight(null) / 2;
				 */
				break;
			case VisionSettingsPanel.MOUSE_MODE_YELLOW_T:
				letterAdjustment = true;
				/*
				 * VisionGUI.this.currentFile = VisionGUI.this.imgLetterT;
				 * VisionGUI.this.selectorImage =
				 * VisionGUI.this.letterTSelectorImage; // Get the center
				 * coordinates of the selector image in use
				 * VisionGUI.this.imageCenterX = VisionGUI.this.selectorImage
				 * .getWidth(null) / 2; VisionGUI.this.imageCenterY =
				 * VisionGUI.this.selectorImage .getHeight(null) / 2;
				 */
				break;
			case VisionSettingsPanel.MOUSE_MODE_GREEN_PLATES:
				if (!bluePlateAdjustment) {
					yellowPlateAdjustment = true;
					/*
					 * VisionGUI.this.currentFile =
					 * VisionGUI.this.imgYellowPlate;
					 * VisionGUI.this.selectorImage =
					 * VisionGUI.this.yellowPlateSelectorImage; // Get the
					 * center coordinates of the selector image in // use
					 * VisionGUI.this.imageCenterX =
					 * VisionGUI.this.selectorImage .getWidth(null) / 2;
					 * VisionGUI.this.imageCenterY =
					 * VisionGUI.this.selectorImage .getHeight(null) / 2;
					 */
				}
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				System.out.println("Division selection mode");
				pitchConstants.getDividers()[currentDivider] = e.getX();
				currentDivider = (currentDivider + 1) % 3;
				break;
			case VisionSettingsPanel.MOUSE_MODE_TARGET:
				break;
			}
		}
	};

	public ColourThresholdConfigTool(VisionGUI gui, WorldState worldState,
			PitchConstants pitchConstants, VideoStream vStream,
			DistortionFix distortionFix) {
		this.gui = gui;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.videoStream = vStream;
		this.distortionFix = distortionFix;

		videoWidth = gui.getVideoWidth();
		videoHeight = gui.getVideoHeight();

		a = this.pitchConstants.getLeftBuffer();
		b = this.pitchConstants.getTopBuffer();
		c = videoWidth - this.pitchConstants.getRightBuffer() - this.a;
		d = videoWidth - this.pitchConstants.getBottomBuffer() - this.b;

		subWindow = new JFrame("Colour threshold configuration");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		settingsPanel = new VisionSettingsPanel(worldState, pitchConstants,
				vStream, distortionFix);
		subWindow.add(settingsPanel);

	}

	@Override
	public void activate() {

		gui.getVideoDisplay().addMouseListener(mouseSelector);
		gui.getVideoDisplay().addMouseMotionListener(mouseSelector);

		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x + mainWindowBounds.width,
				mainWindowBounds.y);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		gui.getVideoDisplay().removeMouseListener(mouseSelector);
		gui.getVideoDisplay().removeMouseMotionListener(mouseSelector);

		subWindow.setVisible(false);
		return true;
	}

	public class PitchBoundsDebugDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame, Graphics2D debugGraphics,
				BufferedImage debugOverlay) {
			// Eliminating area around the pitch dimensions
			if (!selectionActive) {
				int a = pitchConstants.getLeftBuffer();
				int b = pitchConstants.getTopBuffer();
				int c = videoWidth - pitchConstants.getRightBuffer() - a;
				int d = videoHeight - pitchConstants.getBottomBuffer() - b;
				// Making the pitch surroundings transparent
				Composite originalComposite = debugGraphics.getComposite();
				int type = AlphaComposite.SRC_OVER;
				AlphaComposite alphaComp = (AlphaComposite.getInstance(type,
						0.6f));
				debugGraphics.setComposite(alphaComp);
				debugGraphics.setColor(Color.BLACK);
				// Rectangle covering the BOTTOM
				debugGraphics.fillRect(0, 0, videoWidth, b);
				// Rectangle covering the LEFT
				debugGraphics.fillRect(0, b, a, videoHeight);
				// Rectangle covering the BOTTOM
				debugGraphics.fillRect(a + c, b, videoWidth - a, videoHeight
						- b);
				// Rectangle covering the RIGHT
				debugGraphics.fillRect(a, b + d, c, videoHeight - d);
				// Setting back normal settings
				debugGraphics.setComposite(originalComposite);
			}

			if (settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY) {
				// Draw the line around the pitch dimensions
				if (selectionActive) {
					debugGraphics.setColor(Color.YELLOW);
					debugGraphics.drawRect(a, b, c, d);
				}
			}
		}

	}

	public class DividerLineDebugDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame, Graphics2D debugGraphics,
				BufferedImage debugOverlay) {
			// Drawing the dividing lines
			int[] ds = pitchConstants.getDividers();
			int top = pitchConstants.getTopBuffer();
			int bot = videoHeight - pitchConstants.getBottomBuffer();
			debugGraphics.setColor(Color.WHITE);
			debugGraphics.drawLine(ds[0], bot, ds[0], top);
			debugGraphics.drawString("1", ds[0], bot + 20);
			debugGraphics.drawLine(ds[1], bot, ds[1], top);
			debugGraphics.drawString("2", ds[1], bot + 20);
			debugGraphics.drawLine(ds[2], bot, ds[2], top);
			debugGraphics.drawString("3", ds[2], bot + 20);

			debugGraphics.setColor(Color.BLUE);
			final int radius = 5;
			debugGraphics.drawOval(worldState.getRobotTargetX() - radius,
					worldState.getRobotTargetY() - radius, 2 * radius,
					2 * radius);
		}

	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}

}
