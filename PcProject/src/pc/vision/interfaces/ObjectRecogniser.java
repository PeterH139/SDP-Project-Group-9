package pc.vision.interfaces;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public interface ObjectRecogniser {
	/* debugOverlay should not be needed in the future */
	public void processFrame(BufferedImage frame, Graphics2D debugGraphics,
			BufferedImage debugOverlay);
}
