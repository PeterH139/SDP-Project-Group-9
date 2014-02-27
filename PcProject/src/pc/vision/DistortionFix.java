package pc.vision;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pc.vision.interfaces.VideoReceiver;

/**
 * Class to remove barrel distortion
 * 
 * @author Rado
 * @author James Hulme
 */
public class DistortionFix implements VideoReceiver {
	private final static int WIDTH = 640;
	private final static int HEIGHT = 480;
	private double barrelCorrectionX = -0.03;
	private double barrelCorrectionY = -0.085;
	private AffineTransform affineTransform;

	private ArrayList<VideoReceiver> videoReceivers = new ArrayList<VideoReceiver>();
	private boolean active = true;

	public DistortionFix() {
		affineTransform = new AffineTransform(); // Identity transformation
	}

	public DistortionFix(final YAMLConfig yamlConfig,
			final PitchConstants pitchConstants) {
		this();

		yamlConfig.addObserver(new Observer() {

			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable arg0, Object yamlData) {
				String pitchName = pitchConstants.getPitchNum() == 0 ? "main"
						: "side";

				Map<String, Object> topData = (Map<String, Object>) yamlData;
				Map<String, Object> data = (Map<String, Object>) topData
						.get("pitch");
				data = (Map<String, Object>) data.get(pitchName);
				data = (Map<String, Object>) data.get("distortion");
				Object rotateDegreesObj = data.get("rotate");
				double rotateDegrees;
				if (rotateDegreesObj instanceof Double)
					rotateDegrees = (Double) rotateDegreesObj;
				else
					rotateDegrees = (Integer) rotateDegreesObj;
				affineTransform = AffineTransform.getRotateInstance(
						Math.toRadians(rotateDegrees), WIDTH / 2, HEIGHT / 2);
			}
		});
	}

	/**
	 * Determines whether the barrel distortion correction is currently active,
	 * i.e. whether the distortion effect is applied to the video stream
	 * 
	 * @return true if the correction is active, false otherwise
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Enables or disables the barrel distortion correction being applied to the
	 * video stream
	 * 
	 * @param active
	 *            true to enable, false to disable
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Remove barrel distortion on whole image
	 * 
	 * Buffers used so we only correct the pitch area not the useless background
	 * area TODO: Actually use these buffers
	 * 
	 * TODO: find an efficient way to merge pixels based on rounding up/down to
	 * avoid "duplicate" pixels - mostly aesthetic
	 * 
	 * @param image
	 *            Frame to correct
	 * @return A new image with no barrel distortion
	 */
	public BufferedImage removeBarrelDistortion(BufferedImage image) {

		BufferedImage newImage = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_INT_RGB);

		// Strip image to a one-dimensional array
		Raster raster = image.getData();
		int[] array = null;
		array = raster.getPixels(0, 0, WIDTH, HEIGHT, array);

		// Array for the fixed image
		int[] arrayNew = new int[921600];

		Point2D.Double point = new Point2D.Double();
		// Apply invBarrelCorrect() to every pixel
		// array has three cells per pixel to represent RGB
		for (int i = 0; i < array.length; i += 3) {

			// Actual values for x,y
			point.x = (i / 3) % (WIDTH);
			point.y = (i / 3) / (WIDTH);

			invBarrelCorrect(point);

			// The first cell of the array for this pixel
			int z = 3 * ((WIDTH) * (int) (point.y) + (int) (point.x));

			if (0 <= z && z < arrayNew.length) {
				arrayNew[i] = array[z]; // R
				arrayNew[i + 1] = array[z + 1]; // G
				arrayNew[i + 2] = array[z + 2]; // B
			}
		}

		// Get new image from the fixed array
		WritableRaster newRaster = (WritableRaster) newImage.getData();
		newRaster.setPixels(0, 0, WIDTH, HEIGHT, arrayNew);
		newImage.setData(newRaster);

		return newImage;

	}

	/**
	 * Barrel correction for single points
	 * 
	 * Used to correct for the distortion of individual points.
	 * 
	 * @see {@link #invBarrelCorrect(Point)} for correcting an image
	 */
	public void barrelCorrect(Point2D.Double point) {
		// first normalise pixel
		double px = (2 * point.x - WIDTH) / (double) WIDTH;
		double py = (2 * point.y - HEIGHT) / (double) HEIGHT;

		// then compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel
		double px1 = px * (1 - barrelCorrectionX * rad);
		double py1 = py * (1 - barrelCorrectionY * rad);

		// then convert back
		point.x = (px1 + 1) * WIDTH / 2;
		point.y = (py1 + 1) * HEIGHT / 2;

		// apply affine transformation
		affineTransform.transform(point, point);
	}

	/**
	 * Inverse barrel correction for single points
	 * 
	 * Used to correct the distortion in an image without producing an odd
	 * grid-like visual artifact Done with an array instead of a Position for a
	 * small improvement in speed.
	 */

	public void invBarrelCorrect(Point2D.Double point) {
		// apply inverse transformation
		try {
			affineTransform.inverseTransform(point, point);
		} catch (NoninvertibleTransformException e) {
			// Will never happen in rotation transform
		}

		double px = (2 * point.x - WIDTH) / (double) WIDTH;
		double py = (2 * point.y - HEIGHT) / (double) HEIGHT;

		// compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel
		double px1 = px * (1 + barrelCorrectionX * rad);
		double py1 = py * (1 + barrelCorrectionY * rad);

		// then convert back
		point.x = (px1 + 1) * WIDTH / 2;
		point.y = (py1 + 1) * HEIGHT / 2;
	}

	/**
	 * Registers an object to receive frames from the distortion fix
	 * 
	 * @param receiver
	 *            The object being registered
	 */
	public void addReceiver(VideoReceiver receiver) {
		this.videoReceivers.add(receiver);
	}

	/**
	 * Used to send a frame to the distortion fix
	 * 
	 * @param frame
	 *            The frame being sent
	 * @param delta
	 *            The time between frames
	 * @param frameCounter
	 *            The current frame index
	 */
	@Override
	public void sendFrame(BufferedImage frame, float delta, int frameCounter) {
		BufferedImage processedFrame = frame;
		if (isActive())
			processedFrame = removeBarrelDistortion(frame);

		for (VideoReceiver receiver : this.videoReceivers)
			receiver.sendFrame(processedFrame, delta, frameCounter);
	}
}
