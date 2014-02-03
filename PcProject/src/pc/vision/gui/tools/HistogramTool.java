package pc.vision.gui.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;
import pc.vision.interfaces.ObjectRecogniser;

public class HistogramTool implements GUITool, ObjectRecogniser {
	private VisionGUI gui;

	private JFrame subWindow;
	private HistogramDisplay histogramDisplay;
	private GUIMouseListener mouseListener = new GUIMouseListener();

	private boolean isActive = false;
	private Point centerPoint;
	private int radius;
	private boolean needsRefresh = true;

	public HistogramTool(VisionGUI gui) {
		this.gui = gui;

		subWindow = new JFrame("Histogram");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		histogramDisplay = new HistogramDisplay();
		subWindow.add(histogramDisplay);
	}

	@Override
	public void activate() {
		isActive = true;

		gui.getVideoDisplay().addMouseListener(mouseListener);
		gui.getVideoDisplay().addMouseMotionListener(mouseListener);

		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x, mainWindowBounds.y
				+ mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		isActive = false;
		gui.getVideoDisplay().removeMouseListener(mouseListener);
		gui.getVideoDisplay().removeMouseMotionListener(mouseListener);
		subWindow.setVisible(false);
		return true;
	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}

	@Override
	public void processFrame(BufferedImage frame, Graphics2D debugGraphics,
			BufferedImage debugOverlay) {
		if (needsRefresh) {
			needsRefresh = false;
			histogramDisplay.updateImage(refreshHistogram(frame));
		}

		if (isActive && centerPoint != null) {
			debugGraphics.setColor(Color.WHITE);
			debugGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			debugGraphics.drawOval(centerPoint.x - radius, centerPoint.y
					- radius, 2 * radius, 2 * radius);
		}
	}

	/**
	 * The method creates a histogram from a raw frame.
	 * 
	 * @param frame
	 *            Raw frame from camera
	 */
	private BufferedImage refreshHistogram(BufferedImage frame) {
		BufferedImage histogram = new BufferedImage(
				HistogramDisplay.HISTOGRAM_WIDTH,
				HistogramDisplay.HISTOGRAM_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
		if (centerPoint == null)
			return histogram;

		Graphics2D g = histogram.createGraphics();

		int[][] valueCounts = new int[3][256];

		Rectangle selection = new Rectangle(centerPoint.x - radius,
				centerPoint.y - radius, 2 * radius, 2 * radius);
		selection = selection.intersection(new Rectangle(0, 0,
				frame.getWidth(), frame.getHeight()));

		// Gather data
		Raster raster = frame.getData();
		int[] rgb = new int[3]; // Preallocated array
		for (int y = (int) selection.getMinY(); y < selection.getMaxY(); y++) {
			for (int x = (int) selection.getMinX(); x < selection.getMaxX(); x++) {
				if (Math.hypot(x - centerPoint.x, y - centerPoint.y) < radius) {
					// The pixel is inside the circle
					raster.getPixel(x, y, rgb);
					for (int channel = 0; channel < 3; channel++) {
						valueCounts[channel][rgb[channel]]++;
					}
				}
			}
		}

		// Normalise values
		for (int channel = 0; channel < 3; channel++) {
			int maxValue = 1; // 1 to avoid division by zero
			for (int i = 0; i < 256; i++)
				if (valueCounts[channel][i] > maxValue)
					maxValue = valueCounts[channel][i];
			double scalingFactor = 1.0 * HistogramDisplay.HISTOGRAM_HEIGHT
					/ maxValue;
			for (int i = 0; i < 256; i++)
				valueCounts[channel][i] = (int) (HistogramDisplay.HISTOGRAM_HEIGHT - valueCounts[channel][i]
						* scalingFactor);
		}

		// Draw the curves
		int[] xPoints = new int[256];
		for (int i = 0; i < 256; i++) {
			xPoints[i] = i * HistogramDisplay.HISTOGRAM_WIDTH / 256;
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		for (int channel = 0; channel < 3; channel++) {
			g.setColor(channel == 0 ? Color.RED : channel == 1 ? Color.GREEN
					: Color.BLUE);
			g.drawPolyline(xPoints, valueCounts[channel], xPoints.length);
		}
		return histogram;
	}

	private class GUIMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			centerPoint = e.getPoint();
			radius = 0;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			radius = (int) (Math.hypot(e.getX() - centerPoint.x, e.getY()
					- centerPoint.y) / 2);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			needsRefresh = true;
		}
	}

	private class HistogramDisplay extends JPanel {
		public static final int HISTOGRAM_WIDTH = 800;
		public static final int HISTOGRAM_HEIGHT = 300;

		private BufferedImage histogramImage;

		private JPanel imagePanel = new JPanel() {
			protected void paintComponent(java.awt.Graphics g) {
				super.paintComponent(g);
				g.drawImage(histogramImage, 0, 0, null);
			};
		};

		public HistogramDisplay() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			imagePanel.setPreferredSize(new Dimension(HISTOGRAM_WIDTH,
					HISTOGRAM_HEIGHT));
			add(imagePanel);
		}

		public void updateImage(BufferedImage image) {
			histogramImage = image;
			imagePanel.repaint();
		}
	}
}
