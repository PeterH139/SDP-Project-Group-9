package pc.vision.gui.tools;

import java.awt.Rectangle;

import javax.swing.JFrame;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.VideoStream;
import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;
import pc.vision.gui.VisionSettingsPanel;
import pc.world.WorldState;

public class ColourThresholdConfigTool implements GUITool {
	private VisionGUI gui;
	private JFrame subWindow;

	private WorldState worldState;
	private PitchConstants pitchConstants;
	private VideoStream videoStream;
	private DistortionFix distortionFix;

	private VisionSettingsPanel settingsPanel;

	public ColourThresholdConfigTool(VisionGUI gui, WorldState worldState,
			PitchConstants pitchConstants, VideoStream vStream,
			DistortionFix distortionFix) {
		this.gui = gui;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.videoStream = vStream;
		this.distortionFix = distortionFix;

		subWindow = new JFrame("Colour threshold configuration");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		settingsPanel = new VisionSettingsPanel(worldState, pitchConstants,
				vStream, distortionFix);
		gui.settingsPanel = settingsPanel;
		subWindow.add(settingsPanel);

	}

	@Override
	public void activate() {
		// subWindow.setSize(gui.getWidth(), 300);
		Rectangle mainWindowBounds = gui.getBounds();
		System.out.println(mainWindowBounds.y);
		subWindow.setLocation(mainWindowBounds.x + mainWindowBounds.width,
				mainWindowBounds.y);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		subWindow.setVisible(false);
		return true;
	}

}
