package pc.vision.gui.tools;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.Vector2f;
import pc.vision.YAMLConfig;
import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;
import pc.vision.interfaces.PitchViewProvider;
import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.DynamicWorldState;
import pc.world.DynamicWorldState.StateUpdateListener;
import pc.world.Pitch;
import pc.world.oldmodel.WorldState;

public class PitchModelView implements GUITool, VideoReceiver,
		StateUpdateListener {

	private static final int WIDTH = 480;
	private static final int HEIGHT = 320;

	private VisionGUI gui;
	private PitchConstants pitchConstants;
	private DistortionFix distortionFix;
	private Pitch pitch;
	private JFrame subWindow;
	private PitchView pitchView;
	private DynamicWorldState dynamicWorldState;

	private List<PitchViewProvider> viewProviders = new ArrayList<PitchViewProvider>();

	private BufferedImage backgroundFrame, overlayFrame;
	private boolean shouldUpdateFrame = false;

	public PitchModelView(VisionGUI gui, PitchConstants pitchConstants,
			Pitch pitch, DistortionFix distortionFix,
			DynamicWorldState dynamicWorldState) {
		this.gui = gui;
		this.pitchConstants = pitchConstants;
		this.pitch = pitch;
		this.distortionFix = distortionFix;
		this.dynamicWorldState = dynamicWorldState;

		overlayFrame = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_4BYTE_ABGR);

		dynamicWorldState.addStateListener(this);

		subWindow = new JFrame("Pitch Model");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		subWindow.getContentPane().setLayout(new FlowLayout());

		pitchView = new PitchView();
		subWindow.getContentPane().add(pitchView);

		final JCheckBox grabFrameBtn = new JCheckBox("Draw video frame");
		grabFrameBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				shouldUpdateFrame = grabFrameBtn.isSelected();
			}
		});
		subWindow.getContentPane().add(grabFrameBtn);
	}

	@Override
	public void stateUpdated() {
		synchronized (overlayFrame) {
			Graphics2D g = overlayFrame.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g.setBackground(new Color(255, 255, 255, 0));
			g.clearRect(0, 0, overlayFrame.getWidth(), overlayFrame.getHeight());

			pitchView.transformGraphics(g);

			for (PitchViewProvider provider : viewProviders) {
				provider.drawOnPitch(g);
			}

			g.dispose();
		}
		pitchView.repaint();
	}

	public void addViewProvider(PitchViewProvider provider) {
		viewProviders.add(provider);
	}

	@Override
	public void sendFrame(BufferedImage frame, float delta, int frameCounter,
			long timestamp) {
		if (shouldUpdateFrame) {
			backgroundFrame = distortionFix.removeBarrelDistortion(frame);
			pitchView.repaint();
		} else {
			backgroundFrame = null;
		}
	}

	@Override
	public void activate() {
		/*
		 * Position the tool window below the main GUI window.
		 */
		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x, mainWindowBounds.y
				+ mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		subWindow.setVisible(false);
		return true;
	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}

	private class PitchView extends JPanel {
		public PitchView() {
			super();
			setPreferredSize(new Dimension(PitchModelView.WIDTH,
					PitchModelView.HEIGHT));
		}

		public void transformGraphics(Graphics2D g) {
			double scale = 0.9 * Math.min(
					(double) getWidth() / pitch.getPitchWidth(),
					(double) getHeight() / pitch.getPitchHeight());
			g.translate(getWidth() / 2, getHeight() / 2);
			g.scale(scale, scale);
		}

		@Override
		protected void paintComponent(Graphics originalGraphics) {
			super.paintComponent(originalGraphics);
			Graphics2D g = (Graphics2D) originalGraphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			Pitch p = pitch;

			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());

			transformGraphics(g);

			// Draw pitch boundary
			g.setColor(Color.GRAY);
			g.fillPolygon(p.getBoundsPolygon());

			// Draw zone dividers
			g.setColor(Color.WHITE);
			int halfPitchHeight = p.getPitchHeight() / 2;
			int halfWidth = p.getZoneDividerWidth() / 2;
			g.fillRect(-p.getZoneDividerOffset() - halfWidth, -halfPitchHeight,
					2 * halfWidth, 2 * halfPitchHeight);
			g.fillRect(-halfWidth, -halfPitchHeight, 2 * halfWidth,
					2 * halfPitchHeight);
			g.fillRect(p.getZoneDividerOffset() - halfWidth, -halfPitchHeight,
					2 * halfWidth, 2 * halfPitchHeight);

			// Draw goals
			g.setColor(Color.YELLOW);
			g.setStroke(new BasicStroke(10));
			int halfPitchWidth = p.getPitchWidth() / 2;
			g.drawLine(-halfPitchWidth, -p.getGoalHeight() / 2,
					-halfPitchWidth, p.getGoalHeight() / 2);
			g.drawLine(halfPitchWidth, -p.getGoalHeight() / 2, halfPitchWidth,
					p.getGoalHeight() / 2);

			// Draw ball and robots
			synchronized (overlayFrame) {
				originalGraphics.drawImage(overlayFrame, 0, 0, null);
			}

			if (backgroundFrame != null) {
				AffineTransform at = new AffineTransform();
				double frameScale = (double) pitch.getPitchWidth()
						/ pitch.getPitchFrameWidth();
				at.scale(frameScale, frameScale);
				at.translate(-pitch.getPitchCenterFrameX(),
						-pitch.getPitchCenterFrameY());
				Composite oldComposite = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.5f));
				g.drawImage(backgroundFrame, at, null);
				g.setComposite(oldComposite);
			}

			g.dispose();
		}
	}
}
