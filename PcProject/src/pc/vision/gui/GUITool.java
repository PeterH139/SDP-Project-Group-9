package pc.vision.gui;

public interface GUITool {
	/**
	 * Called when this tool is activated.
	 */
	public void activate();
	
	/**
	 * Called when this tool is deactivated.
	 * @return Return false to cancel tool change.
	 */
	public boolean deactivate();
}
