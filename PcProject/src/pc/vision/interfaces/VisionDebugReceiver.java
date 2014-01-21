package pc.vision.interfaces;

import java.awt.image.BufferedImage;

/**
 * An interface for classes which receive the debug overlay from vision
 * 
 * @author Alex Adams (s1046358)
 */
public interface VisionDebugReceiver {
	public void sendDebugOverlay(BufferedImage debug);
}
