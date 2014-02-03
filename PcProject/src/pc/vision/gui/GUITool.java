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

	/**
	 * This method should release GUI resources that
	 * may prevent the VM from shutting down.
	 * 
	 * In most cases it needs to call dispose() on any
	 * frames the tool has created.
	 */
	public void dispose();
}
