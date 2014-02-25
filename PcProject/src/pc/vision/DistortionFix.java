package pc.vision;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import pc.vision.interfaces.VideoReceiver;

/**
 * Class to remove barrel distortion
 * 
 * @author Rado
 * @author James Hulme
 */
public class DistortionFix implements VideoReceiver {
	private static int width = 640;
	private static int height = 480;
	public static double barrelCorrectionX = -0.03;
	public static double barrelCorrectionY = -0.085;

	private ArrayList<VideoReceiver> videoReceivers = new ArrayList<VideoReceiver>();
	private boolean active = true;

	private final PitchConstants pitchConstants;
	
	public DistortionFix(final PitchConstants pitchConstants) {
		this.pitchConstants = pitchConstants;
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
	 * area
	 * TODO: Actually use these buffers
	 * 
	 * TODO: find an efficient way to merge pixels based on rounding up/down to
	 * avoid "duplicate" pixels - mostly aesthetic
	 * 
	 * @param image
	 *            Frame to correct
	 * @return A new image with no barrel distortion
	 */
	public static BufferedImage removeBarrelDistortion(BufferedImage image) {

		BufferedImage newImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		//Strip image to a one-dimensional array
		Raster raster = image.getData();
		int[] array = null;
		array = raster.getPixels(0, 0, width, height, array);

		//Array for the fixed image
		int[] arrayNew = new int[921600];

		int x, y, z;
		int[] xy;

		//Normalise x and y.
		//This used to be calculated inside invBarrelCorrect(), now reusing the values
		double[] normX = new double[width];
		double[] normY = new double[height];
		for (int xi = 0; xi < width; xi++) {
			normX[xi] = (2 * xi - width) / (double) width;
		}
		for (int yi = 0; yi < height; yi++) {
			normY[yi] = (2 * yi - height) / (double) height;
		}

		//Apply invBarrelCorrect() to every pixel
		//array has three cells per pixel to represent RGB
		for (int i = 0; i < array.length; i += 3) {

			//Actual values for x,y
			x = (i/3) % (width);
			y = (i/3) / (width);

			xy = invBarrelCorrect(x, y, normX[x], normY[y]);

			//The first cell of the array for this pixel
			z = (width) * xy[1] + xy[0];
			z *= 3;

			//I'm not 100% sure we don't need this
			// if (0 <= xy[0] && xy[0] < width && 0 <= xy[1] && xy[1] < height)
			// {
			arrayNew[i] = array[z];		//R
			arrayNew[i+1] = array[z+1];	//G
			arrayNew[i+2] = array[z+2];	//B
			// }
		}

		//Get new image from the fixed array
		WritableRaster newRaster = (WritableRaster) newImage.getData();
		newRaster.setPixels(0, 0, width, height, arrayNew);
		newImage.setData(newRaster);

		return newImage;

	}
	
	/**
	 * Barrel correction for single points
	 * 
	 * Used to correct for the distortion of individual points.
	 * 
	 * @see {@link #invBarrelCorrect(Point)} for correcting an image
	 * 
	 * @param p
	 *            Point to fix
	 * @return Fixed Point
	 */
	public static void barrelCorrect(int x, int y, float[] xy) {
		// first normalise pixel
		double px = (2 * x - width) / (double) width;
		double py = (2 * y - height) / (double) height;

		// then compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel
		double px1 = px * (1 - barrelCorrectionX * rad);
		double py1 = py * (1 - barrelCorrectionY * rad);

		// then convert back
		xy[0] = (float) ((px1 + 1) * width / 2);
		xy[1] = (float) ((py1 + 1) * height / 2);
	}
	
	/**
	 * Inverse barrel correction for single points
	 * 
	 * Used to correct the distortion in an image without producing an odd
	 * grid-like visual artifact
	 * Done with an array instead of a Position for a small improvement in speed.
	 * 
	 * @param x
	 *            x of Point to "unfix"
	 * @param y
	 *            y of Point to "unfix"
	 * @param px
	 * 			  Normalised coordinate for x 
	 * @param py
	 * 			  Normalised coordinate for y
	 */

	public static int[] invBarrelCorrect(int x, int y, double px, double py) {

		// compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel
		double px1 = px * (1 + barrelCorrectionX * rad);
		double py1 = py * (1 + barrelCorrectionY * rad);

		// then convert back
		int pixi = (int) ((px1 + 1) * width / 2);
		int pixj = (int) ((py1 + 1) * height / 2);
		
		//Returning as an object or 1000*pixi+pixj was slower than int[]
		//Any other ideas?
		int[] xy = new int[2];
		xy[0] = pixi;
		xy[1] = pixj;
		return xy;
	}

	/**
	 * Inverse Barrel Correction including normalization to be used only for calculating 
	 * the distortion fix of 5 points: the ball and the 5 robots.
	 */
	
	public static void invBarrelCorrectWithNorm(int x, int y, float[] xy) {
		double px = (2 * x - width) / (double) width;
		double py = (2 * y - height) / (double) height;
		
		// compute the radius of the pixel you are working with
		double rad = px * px + py * py;

		// then compute new pixel
		double px1 = px * (1 + barrelCorrectionX * rad);
		double py1 = py * (1 + barrelCorrectionY * rad);

		// then convert back
		int pixi = (int) ((px1 + 1) * width / 2);
		int pixj = (int) ((py1 + 1) * height / 2);
				
		//Returning as an object or 1000*pixi+pixj was slower than int[]
		//Any other ideas?;
		xy[0] = pixi;
		xy[1] = pixj;

		
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
		BufferedImage processedFrame;

		// If the distortion overlay is active, apply it
		if (isActive()) {
			processedFrame = removeBarrelDistortion(frame);
		}
		// Otherwise just forward the frame as-is
		else
			processedFrame = frame;

		for (VideoReceiver receiver : this.videoReceivers)
			receiver.sendFrame(processedFrame, delta, frameCounter);
	}
}
