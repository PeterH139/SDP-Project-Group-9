package pc.vision.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.VisionDebugReceiver;

@SuppressWarnings("serial")
public class VisionGUI extends JFrame implements VideoReceiver,
		VisionDebugReceiver {
	private final int videoWidth;
	private final int videoHeight;

	// Stored to only have rendering happen in one place
	private BufferedImage rawFrame, renderedFrame;
	private float delta;
	private int frameCounter;

	private final JPanel videoDisplay = new JPanel() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// The most recent frame is always drawn
			g.drawImage(renderedFrame, 0, 0, null);
		}
	};
	
	private final JList toolList;
	private ToolWrapper currentToolWrapper;
	
	private final WindowAdapter windowAdapter = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			// Tool deactivate function may prevent from closing
			if (currentToolWrapper == null || currentToolWrapper.tool.deactivate()) {
				DefaultListModel dlm = (DefaultListModel) toolList.getModel();
				for (int i = 0; i < dlm.size(); i++)
					((ToolWrapper) dlm.get(i)).tool.dispose();
				dispose();
			}
		}
	};

	public VisionGUI(int videoWidth, int videoHeight) {
		super("Vision");
		this.videoWidth = videoWidth;
		this.videoHeight = videoHeight;

		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

		Dimension videoSize = new Dimension(videoWidth, videoHeight);
		this.videoDisplay.setPreferredSize(videoSize);
		getContentPane().add(this.videoDisplay);

		//getContentPane().add(Box.createHorizontalStrut(10));

		toolList = new JList(new DefaultListModel());
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {
				handleToolChange();
			}
		});
		JScrollPane listScroller = new JScrollPane(toolList);
		listScroller.setPreferredSize(new Dimension(150, 0));
		getContentPane().add(listScroller);

		setResizable(false);
		pack();
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this.windowAdapter);
	}

	private void handleToolChange() {
		ToolWrapper tw = (ToolWrapper) toolList.getSelectedValue();
		if (tw == currentToolWrapper)
			return;
		if (currentToolWrapper != null && !currentToolWrapper.tool.deactivate()) {
			// If deactivate() returns false, we want to keep the current tool
			toolList.setSelectedValue(currentToolWrapper, true);
			return;
		}
		currentToolWrapper = tw;
		if (currentToolWrapper != null)
			currentToolWrapper.tool.activate();
	}

	public void addTool(GUITool tool, String name) {
		DefaultListModel model = (DefaultListModel) toolList
				.getModel();
		model.addElement(new ToolWrapper(tool, name));
	}
	
	public JPanel getVideoDisplay() {
		return videoDisplay;
	}
	
	public int getVideoWidth() {
		return videoWidth;
	}
	
	public int getVideoHeight() {
		return videoHeight;
	}

	@Override
	public void sendFrame(BufferedImage frame, float delta, int frameCounter) {
		this.rawFrame = frame;
		this.delta = delta;
		this.frameCounter = frameCounter;
	}

	@Override
	public void sendDebugOverlay(BufferedImage debugOverlay) {
		// Draw overall composite to screen
		Graphics frameGraphics = rawFrame.getGraphics();
		frameGraphics.drawImage(debugOverlay, 0, 0, null);
		
		// Draw frame info and worldstate on top of the result
		// Display the FPS that the vision system is running at
		frameGraphics.setColor(Color.white);
		frameGraphics.drawString("Frame: " + this.frameCounter, 15, 15);
		frameGraphics.drawString("FPS: " + 1 / this.delta, 15, 30);
		
		renderedFrame = rawFrame;
		videoDisplay.repaint();
	}

	private class ToolWrapper {
		public GUITool tool;
		public String name;
		
		public ToolWrapper(GUITool tool, String name) {
			this.tool = tool;
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
