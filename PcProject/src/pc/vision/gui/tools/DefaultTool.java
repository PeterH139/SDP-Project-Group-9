package pc.vision.gui.tools;

import javax.swing.JOptionPane;

import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;

public class DefaultTool implements GUITool {
	private VisionGUI gui;

	public DefaultTool(VisionGUI gui) {
		this.gui = gui;
	}
	
	@Override
	public void activate() {
	}

	@Override
	public boolean deactivate() {
		return true;
	}

	@Override
	public void dispose() {
	}

}
