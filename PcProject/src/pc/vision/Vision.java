package pc.vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.sound.midi.Receiver;

import pc.vision.interfaces.VideoReceiver;
import pc.vision.interfaces.VisionDebugReceiver;
import pc.vision.interfaces.WorldStateReceiver;
//import world.state.WorldState;

/**
 * The main class for showing the video feed and processing the video data.
 * Identifies ball and robot locations, and robot orientations.
 * 
 */
public class Vision implements VideoReceiver {

	// Variables used in processing video
	private final PitchConstants pitchConstants;
	//private final WorldState worldState;
	private ArrayList<VisionDebugReceiver> visionDebugReceivers = new ArrayList<VisionDebugReceiver>();
	private ArrayList<WorldStateReceiver> worldStateReceivers = new ArrayList<WorldStateReceiver>();

	private Position bluePlateCentroid = new Position(0, 0);
	private Position yellowPlateCentroid = new Position(0, 0);

	private final int YELLOW_T = 0;
	private final int BLUE_T = 1;
	private final int BALL = 2;
	private final int GREEN_PLATE = 3;
	private final int GREY_CIRCLE = 4;

	/**
	 * Default constructor.
	 * 
	 * @param worldState
	 * @param pitchConstants
	 * @param pitchConstants
	 * 
	 */
	//public Vision(WorldState worldState, PitchConstants pitchConstants) {
	public Vision(PitchConstants pitchConstants) {
		// Set the state fields.
		//this.worldState = worldState;
		this.pitchConstants = pitchConstants;
	}

	/**
	 * @return The current world state
	 */
//	public WorldState getWorldState() {
//		return worldState;
//	}

	/**
	 * Registers an object to receive the debug overlay from the vision system
	 * 
	 * @param receiver
	 *            The object being registered
	 */
	public void addVisionDebugReceiver(VisionDebugReceiver receiver) {
		visionDebugReceivers.add(receiver);
	}

	/**
	 * Registers an object to receive the world state from the vision system
	 * 
	 * @param receiver
	 *            The object being registered
	 */
	public void addWorldStateReceiver(WorldStateReceiver receiver) {
		worldStateReceivers.add(receiver);
	}

	/**
	 * Processes an input image, extracting the ball and robot positions and
	 * robot orientations from it, and then displays the image (with some
	 * additional graphics layered on top for debugging) in the vision frame.
	 * 
	 * @param image
	 *            The image to process and then show.
	 * @param frameRate
	 *            The frame rate
	 * @param counter
	 *            The index of the current frame
	 */
	public void sendFrame(BufferedImage frame, int frameRate, int counter) {
		BufferedImage debugOverlay = new BufferedImage(frame.getWidth(),
				frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics debugGraphics = debugOverlay.getGraphics();

		// Variables needed to determine the position of all objects
		int ballX = 0;
		int ballY = 0;
		int numBallPos = 0;

		int greenX = 0;
		int greenY = 0;
		int numGreenPos = 0;

		int yellowY = 0;
		int yellowX = 0;
		int numYellowPos = 0;

		int blueX = 0;
		int blueY = 0;
		int numBluePos = 0;

		ArrayList<Position> ballPoints = new ArrayList<Position>();
		ArrayList<Position> greenPoints = new ArrayList<Position>();
		ArrayList<Position> bluePoints = new ArrayList<Position>();
		ArrayList<Position> yellowPoints = new ArrayList<Position>();

		int topBuffer = pitchConstants.getTopBuffer();
		int bottomBuffer = pitchConstants.getBottomBuffer();
		int leftBuffer = pitchConstants.getLeftBuffer();
		int rightBuffer = pitchConstants.getRightBuffer();

		/**
		 * Processing every pixel in the frame. For every pixel within the
		 * pitch, test to see if it belongs to the ball, the Yellow T, Blue T,
		 * Green plate, Grey circle
		 */
		for (int row = topBuffer; row < frame.getHeight() - bottomBuffer; row++) {
			for (int column = leftBuffer; column < frame.getWidth()
					- rightBuffer; column++) {

				// The RGB colours and hsv values for the current pixel.
				Color c = new Color(frame.getRGB(column, row));
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);

				if (pitchConstants.debugMode(PitchConstants.GREY)
						&& isColour(c, hsbvals, GREY_CIRCLE)) {
					debugOverlay.setRGB(column, row, 0xFFFF0099);
				}

				/** Checking if the pixel is a part of the Blue T */
				if (isColour(c, hsbvals, BLUE_T)) {
					blueX += column;
					blueY += row;
					numBluePos++;

					bluePoints.add(new Position(column, row));

					/*
					 * If we're in the "Blue Robot" tab, we show what pixels
					 * we're looking at, for debugging and to help with
					 * threshold setting.
					 */
					if (pitchConstants.debugMode(PitchConstants.BLUE)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Yellow T */
				if (isColour(c, hsbvals, YELLOW_T)) {
					yellowX += column;
					yellowY += row;
					numYellowPos++;

					yellowPoints.add(new Position(column, row));

					/*
					 * If we're in the "Yellow Robot" tab, we show what pixels
					 * we're looking at, for debugging and to help with
					 * threshold setting.
					 */
					if (pitchConstants.debugMode(PitchConstants.YELLOW)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Green Plate */
				if (isColour(c, hsbvals, GREEN_PLATE)) {
					greenX += column;
					greenY += row;
					numGreenPos++;

					greenPoints.add(new Position(column, row));

					/*
					 * If we're in the "Green Plate" tab, we show what pixels
					 * we're looking at, for debugging and to help with
					 * threshold setting.
					 */
					if (pitchConstants.debugMode(PitchConstants.GREEN)) {
						debugOverlay.setRGB(column, row, 0xFFFF0099);
					}
				}

				/** Checking if the pixel is a part of the Ball */
				if (isColour(c, hsbvals, BALL)) {
					ballX += column;
					ballY += row;
					numBallPos++;

					ballPoints.add(new Position(column, row));

					/*
					 * If we're in the "Ball" tab, we show what pixels we're
					 * looking at, for debugging and to help with threshold
					 * setting.
					 */
					if (pitchConstants.debugMode(PitchConstants.BALL)) {
						debugOverlay.setRGB(column, row, 0xFF000000);
					}
				}
			}
		}

		/**
		 * Calculating the centre points of the different objects on the pitch.
		 * Position objects to hold the centre point of the ball, both Ts and
		 */

		Position ball = null;
		Position green;
		Position blue;
		Position yellow;

		/** Yellow */
		if (numYellowPos > 10) {
			yellowX /= numYellowPos;
			yellowY /= numYellowPos;

			yellow = new Position(yellowX, yellowY);
			//yellow.fixValues(worldState.getYellowX(), worldState.getYellowY());
			yellow.filterPoints(yellowPoints);
		} else {
			//yellow = new Position(worldState.getYellowX(),
			//		worldState.getYellowY());
			yellow = new Position(yellowX, yellowY); // temp
		}

		/** Blue */
		if (numBluePos > 10) {
			blueX /= numBluePos;
			blueY /= numBluePos;

			blue = new Position(blueX, blueY);
			//blue.fixValues(worldState.getBlueX(), worldState.getBlueY());
			blue.filterPoints(bluePoints);
		} else {
			//blue = new Position(worldState.getBlueX(), worldState.getBlueY());
			blue = new Position(blueX, blueY); // temp
		}

		/** Green plate */
		if (numGreenPos > 20) {
			greenX /= numGreenPos;
			greenY /= numGreenPos;

			green = new Position(greenX, greenY);
			// TODO: move this to k-means stuff
			// green.fixValues(worldState.getGreenX(), worldState.getGreenY());
			// green.filterPoints(greenXPoints, greenYPoints);
		} else {
			//green = new Position(worldState.getGreenX(), worldState.getGreenY());
			green = new Position(greenX, greenY); // temp
		}

		/** Processing the Green plates */

		try {
			double sumSqrdError = Kmeans.sumSquaredError(greenPoints, green);

			double blueAngle = 0;
			double yellowAngle = 0;

			// Check that we actually have 2 plates before attempting to cluster
			// them
			if (sumSqrdError > Kmeans.errortarget) {
				/** TWO PLATES ON FIELD SCENARIO - RUN KMEANS */
				double[] angles = differentiateBetweenPlates(frame,
						debugOverlay, greenPoints, blue, yellow);

				blueAngle = angles[0];
				yellowAngle = angles[1];
			} else {
				/** ONE PLATE ON FIELD SCENARIO - NO KMEANS NEEDED */
				double angle = findPlateAngle(frame, debugOverlay, green,
						greenPoints);

				// If the single plate is blue, assign the angle to the blue
				// plate
				if (numBluePos > numYellowPos) {
					blueAngle = angle;
					bluePlateCentroid = green;
					
					// Set the other plate to some position off the pitch
					yellowAngle = 0.0;
					yellowPlateCentroid = new Position(0,0);
				}
				// Otherwise assign it to the yellow plate
				else {
					yellowAngle = angle;
					yellowPlateCentroid = green;
					
					// Set the other plate to some position off the pitch
					blueAngle = 0.0;
					bluePlateCentroid = new Position(0,0);
				}
			}

			/** Ball */
			// If we have only found a few 'Ball' pixels, chances are that the
			// ball
			// has not actually been detected.
			if (numBallPos > 15) {
				ballX /= numBallPos;
				ballY /= numBallPos;

				ball = new Position(ballX, ballY);
				//ball.fixValues(worldState.getBallX(), worldState.getBallY());
				ball.filterPoints(ballPoints);
			} else {
				ball = new Position(ballX, ballY);
//				int ballrobot = worldState.whoHasTheBall();
//				switch (ballrobot) {
//				case 1: // Blue robot has the ball
//					ball = new Position(
//							(int) (blue.getX() + 30 * Math.sin(blueAngle)),
//							(int) (blue.getY() - 30 * Math.cos(blueAngle)));
//					break;
//				case 2:
//					ball = new Position(
//							(int) (yellow.getX() + 30 * Math.sin(yellowAngle)),
//							(int) (yellow.getY() - 30 * Math.cos(yellowAngle)));
//					break;
//				case -1:
//					//ball = new Position(worldState.getBallX(),
//							//worldState.getBallY());
//				}
			}

			ballPoints = Position.removeOutliers(ballPoints, ball);

			ball = DistortionFix.barrelCorrect(ball);
			green = DistortionFix.barrelCorrect(green);
			blue = DistortionFix.barrelCorrect(bluePlateCentroid);
			yellow = DistortionFix.barrelCorrect(yellowPlateCentroid);

			/** Worldstate settings */
			// TODO: Sort out all of the world state settings.
//			worldState.setBallX(ball.getX());
//			worldState.setBallY(ball.getY());
//			worldState.setGreenX(green.getX());
//			worldState.setGreenY(green.getY());
//			worldState.setBlueX(blue.getX());
//			worldState.setBlueY(blue.getY());
//			worldState.setYellowX(yellow.getX());
//			worldState.setYellowY(yellow.getY());
//			worldState.setBlueOrientation(blueAngle);
//			worldState.setYellowOrientation(yellowAngle);
//			worldState.update();
//
//			worldState.setOurRobot();
//			worldState.setTheirRobot();
//			worldState.setBall();
//			worldState.updatePossesion();

			// Only display these markers in non-debug mode.
			boolean anyDebug = false;
			for (int i = 0; i < 5; ++i) {
				if (pitchConstants.debugMode(i)) {
					anyDebug = true;
					break;
				}
			}

			if (!anyDebug) {
				debugGraphics.setColor(Color.red);
				//debugGraphics.drawLine(0, worldState.getBallY(), 640,
						//worldState.getBallY());
				//debugGraphics.drawLine(worldState.getBallX(), 0,
						//worldState.getBallX(), 480);
				debugGraphics.setColor(Color.white);
			}
		} catch (NoAngleException e) {
			debugGraphics.drawString(e.getMessage(), 15, 440);
			// System.err.println(e.getClass().toString() + ": " +
			// e.getMessage());
			// e.printStackTrace(System.err);
		}

		for (VisionDebugReceiver receiver : visionDebugReceivers)
			receiver.sendDebugOverlay(debugOverlay);
		//for (WorldStateReceiver receiver : worldStateReceivers)
			//receiver.sendWorldState(worldState);
		for (WorldStateReceiver reciever : worldStateReceivers)
			reciever.sendWorldState();
	}

	/**
	 * Tests if an integer value is within bounds, or outside bounds if the
	 * range is inverted
	 * 
	 * @param value
	 *            The value to check
	 * @param lower
	 *            The lower bound
	 * @param upper
	 *            The upper bound
	 * @param inverted
	 *            true if the range is inverted, false otherwise
	 * @return true if the value is within bounds, false otherwise
	 */
	private boolean checkBounds(int value, int lower, int upper,
			boolean inverted) {
		if (!inverted)
			return (lower <= value && value <= upper);
		else
			return (upper <= value || value <= lower);
	}

	/**
	 * Tests if a floating point value is within bounds, or outside bounds if
	 * the range is inverted
	 * 
	 * @param value
	 *            The value to check
	 * @param lower
	 *            The lower bound
	 * @param upper
	 *            The upper bound
	 * @param inverted
	 *            true if the range is inverted, false otherwise
	 * @return true if the value is within bounds, false otherwise
	 */
	private boolean checkBounds(float value, float lower, float upper,
			boolean inverted) {
		if (!inverted)
			return (lower <= value && value <= upper);
		else
			return (upper <= value || value <= lower);
	}

	/**
	 * Determines if a pixel is part of the object specified, based on input RGB
	 * colours and hsv values.
	 * 
	 * @param color
	 *            The RGB colours for the pixel.
	 * @param hsbvals
	 *            The HSV values for the pixel.
	 * @param object
	 *            Indication which object we're looking for.
	 * @return True if the RGB and HSV values are within the defined thresholds
	 *         (and thus the pixel is part of the blue T), false otherwise.
	 */
	private boolean isColour(Color colour, float[] hsbvals, int object) {
		int objectIdx = -1;

		switch (object) {
		case BLUE_T:
			objectIdx = PitchConstants.BLUE;
			break;
		case YELLOW_T:
			objectIdx = PitchConstants.YELLOW;
			break;
		case BALL:
			objectIdx = PitchConstants.BALL;
			break;
		case GREY_CIRCLE:
			objectIdx = PitchConstants.GREY;
			break;
		case GREEN_PLATE:
			objectIdx = PitchConstants.GREEN;
		}

		return checkBounds(colour.getRed(),
				pitchConstants.getRedLower(objectIdx),
				pitchConstants.getRedUpper(objectIdx),
				pitchConstants.isRedInverted(objectIdx))
				&& checkBounds(colour.getGreen(),
						pitchConstants.getGreenLower(objectIdx),
						pitchConstants.getGreenUpper(objectIdx),
						pitchConstants.isGreenInverted(objectIdx))
				&& checkBounds(colour.getBlue(),
						pitchConstants.getBlueLower(objectIdx),
						pitchConstants.getBlueUpper(objectIdx),
						pitchConstants.isBlueInverted(objectIdx))
				&& checkBounds(hsbvals[0],
						pitchConstants.getHueLower(objectIdx),
						pitchConstants.getHueUpper(objectIdx),
						pitchConstants.isHueInverted(objectIdx))
				&& checkBounds(hsbvals[1],
						pitchConstants.getSaturationLower(objectIdx),
						pitchConstants.getSaturationUpper(objectIdx),
						pitchConstants.isSaturationInverted(objectIdx))
				&& checkBounds(hsbvals[2],
						pitchConstants.getValueLower(objectIdx),
						pitchConstants.getValueUpper(objectIdx),
						pitchConstants.isValueInverted(objectIdx));
	}

	/**
	 * Finds four farthest points from the given centroid in the set of points
	 * given. (Used to find the four corners of the Green plate)
	 * 
	 * @param debugOverlay
	 *            The second top layer on top of the video feed, that is used to
	 *            draw on for debugging.
	 * @param xpoints
	 *            Set of x coordinates of points from which the farthest points
	 *            are selected.
	 * @param ypoints
	 *            Set of y coordinates of points from which the farthest points
	 *            are selected.
	 * @param distMax
	 *            The max distance squared from the centroid in which the
	 *            farthest points can be found.
	 */
	public Position[] findFurthest(BufferedImage debugOverlay,
			Position centroid, ArrayList<Position> points, int distMax)
			throws NoAngleException {
		if (points.size() < 5) {
			throw new NoAngleException(
					"List of points is too small to calculate angle");
		}

		/** The minimum distance between two corners */
		final double cornerThreshold = 500.0;

		// Intialising the array of four points
		Position[] corners = new Position[4];
		for (int i = 0; i < corners.length; i++) {
			corners[i] = new Position(0, 0);
		}

		double dist = 0;
		int index = 0;
		// First farthest point
		for (int i = 0; i < points.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid,
					points.get(i));

			if (currentDist > dist && currentDist < distMax) {
				dist = currentDist;
				index = i;
			}
		}
		corners[0] = points.get(index);

		index = 0;
		dist = 0;
		// Second farthest point
		for (int i = 0; i < points.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid,
					points.get(i));

			double distTo0 = Position.sqrdEuclidDist(corners[0], points.get(i));

			if (currentDist > dist && currentDist < distMax
					&& distTo0 > cornerThreshold) {
				dist = currentDist;
				index = i;
			}
		}
		corners[1] = points.get(index);

		index = 0;
		dist = 0;

		// Third farthest point
		for (int i = 0; i < points.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid,
					points.get(i));

			double distTo0 = Position.sqrdEuclidDist(corners[0], points.get(i));
			double distTo1 = Position.sqrdEuclidDist(corners[1], points.get(i));

			if (currentDist > dist && currentDist < distMax
					&& distTo0 > cornerThreshold && distTo1 > cornerThreshold) {
				dist = currentDist;
				index = i;
			}
		}
		corners[2] = points.get(index);

		index = 0;
		dist = 0;

		// Fourth farthest point
		for (int i = 0; i < points.size(); i++) {
			double currentDist = Position.sqrdEuclidDist(centroid,
					points.get(i));

			double distTo0 = Position.sqrdEuclidDist(corners[0], points.get(i));
			double distTo1 = Position.sqrdEuclidDist(corners[1], points.get(i));
			double distTo2 = Position.sqrdEuclidDist(corners[2], points.get(i));

			if (currentDist > dist && currentDist < distMax
					&& distTo0 > cornerThreshold && distTo1 > cornerThreshold
					&& distTo2 > cornerThreshold) {
				dist = currentDist;
				index = i;
			}
		}
		corners[3] = points.get(index);

		// Display the four farthest points in the
		Graphics debugGraphics = debugOverlay.getGraphics();
		for (int i = 0; i < corners.length; i++)
			debugGraphics.drawOval(corners[i].getX() - 2,
					corners[i].getY() - 2, 4, 4);

		return corners;
	}

	/**
	 * Finds two furtherest points by comparing all points between each other.
	 * 
	 * @param debugOverlay
	 *            The second top layer on top of the video feed, that is used to
	 *            draw on for debugging.
	 * @param points
	 *            Set of points from which the farthest points are selected.
	 */
	public Position[] findFurthestNoCenter(BufferedImage debugOverlay,
			ArrayList<Position> points) {
		int maxdist = 0;
		Position point1 = new Position(0, 0);
		Position point2 = new Position(0, 0);
		for (int i = 0; i < points.size(); i++) {
			for (int j = i + 1; j < points.size(); j++) {
				Position p = points.get(i);
				Position q = points.get(j);

				int tempdist = Position.sqrdEuclidDist(p, q);
				if (maxdist < tempdist) {
					point1 = p;
					point2 = q;

					maxdist = tempdist;
				}
			}
		}
		return new Position[] { point1, point2 };
	}

	/**
	 * Finds the two mean points of the two shortest sides.
	 * 
	 * @param points
	 *            The set of four points. (Representing the corners of the Green
	 *            plate)
	 */
	public Position[] findAvgPtOfTwoShortestSides(Position[] points) {

		// Initialise the minimum distance to be the maximum value possible
		int distMin = Integer.MAX_VALUE;
		// The first pair of points with the shortest distance in between
		int pairApt1 = -1;
		int pairApt2 = -1;
		// The second pair of point with the second shortest distance in between
		int pairBpt1;
		int pairBpt2;
		// Boolean array that helps to distinguish pairA from pairB
		boolean[] used = new boolean[4];

		// Finding the indices of the two points between which the difference is
		// the shortest
		for (int i = 0; i < points.length; i++)
			for (int j = 0; j < i; j++) {
				int dist = Position.sqrdEuclidDist(points[i], points[j]);

				if (dist < distMin) {
					pairApt1 = i;
					pairApt2 = j;
					distMin = dist;
				}
			}

		// Marking the found pair true; and finding the other pair.
		used[pairApt1] = true;
		used[pairApt2] = true;

		int i;
		for (i = 0; used[i]; ++i)
			;
		pairBpt1 = i;
		for (i = pairBpt1 + 1; used[i]; ++i)
			;
		pairBpt2 = i;

		// Calculating the means between each of the two points, which are then
		// returned.
		Position top = new Position(
				(points[pairBpt1].getX() + points[pairBpt2].getX()) / 2,
				(points[pairBpt1].getY() + points[pairBpt2].getY()) / 2);
		Position bottom = new Position(
				(points[pairApt1].getX() + points[pairApt2].getX()) / 2,
				(points[pairApt1].getY() + points[pairApt2].getY()) / 2);

		return new Position[] { top, bottom };
	}

	/**
	 * Finds the two mean points of the two shortest sides.
	 * 
	 * @param frame
	 *            The frame that is being processed
	 * @param debugOverlay
	 *            The debugging layer on top of the frame
	 * @param points
	 *            The points of the green pixels
	 * @param blueTCentroid
	 *            The blue T centroid, that is used as one of the initial means
	 *            for the kMeans
	 * @param yellowTCentroid
	 *            The yellow T centroid, that is used as one of the initial
	 *            means for the kMeans
	 * @return A 2-element double array with the blue plate's angle as the first
	 *         element and the yellow plate's angle as the second
	 * 
	 * @throws NoAngleException
	 *             When the angle of either plate cannot be determined
	 */
	public double[] differentiateBetweenPlates(BufferedImage frame,
			BufferedImage debugOverlay, ArrayList<Position> points,
			Position blueTCentroid, Position yellowTCentroid)
			throws NoAngleException {

		Graphics debugGraphics = debugOverlay.getGraphics();

		// Use the centroids of the Ts as the initial means for kMeans

		// Doing k = 2 kMeans on the Green Plate pixels to separate them
		Cluster kMeansRes = Kmeans.doKmeans(points, blueTCentroid,
				yellowTCentroid);
		Position plate1mean = kMeansRes.getMean(0);
		Position plate2mean = kMeansRes.getMean(1);
		ArrayList<Position> cluster1 = kMeansRes.getCluster(0);
		ArrayList<Position> cluster2 = kMeansRes.getCluster(1);

		// TODO: DEBUGGING CODE
		// Only display these markers in non-debug mode.
		boolean anyDebug = false;
		for (int i = 0; i < 5; ++i) {
			if (pitchConstants.debugMode(i)) {
				anyDebug = true;
				break;
			}
		}

		// Colouring the two clusters
		if (!anyDebug) {
			debugGraphics.setColor(Color.BLACK);
			debugGraphics.drawRect(plate1mean.getX() - 5,
					plate1mean.getY() - 5, 10, 10);
			debugGraphics.setColor(Color.WHITE);
			debugGraphics.drawRect(plate2mean.getX() - 5,
					plate2mean.getY() - 5, 10, 10);
			if (cluster1.size() > 0 && cluster2.size() > 0) {
				debugGraphics.setColor(Color.CYAN);
				for (int i = 0; i < cluster1.size(); i++) {
					Position p = cluster1.get(i);
					debugGraphics.drawRect(p.getX(), p.getY(), 1, 1);
				}

				debugGraphics.setColor(Color.magenta);
				for (int i = 0; i < cluster2.size(); i++) {
					Position p = cluster2.get(i);
					debugGraphics.drawRect(p.getX(), p.getY(), 1, 1);
				}
			}
		}
		
		double blueAngle = findPlateAngle(frame, debugOverlay, plate1mean,
				cluster1);
		double yellowAngle = findPlateAngle(frame, debugOverlay, plate2mean,
				cluster2);

		bluePlateCentroid = plate1mean;
		yellowPlateCentroid = plate2mean;

		return new double[] { blueAngle, yellowAngle };
	}

	/**
	 * Finds the two mean points of the two shortest sides.
	 * 
	 * @param frame
	 *            The frame that is being processed
	 * @param debugOverlay
	 *            The debugging layer on top of the frame
	 * @param plateCentroids
	 *            The centroid of the plate
	 * @param points
	 *            The points of the green pixels
	 * @return The bearing for the plate
	 * 
	 * @throws NoAngleException
	 *             When the angle of the plate can't be determined
	 */
	public double findPlateAngle(BufferedImage frame,
			BufferedImage debugOverlay, Position plateCentroid,
			ArrayList<Position> points) throws NoAngleException {
		Graphics debugGraphics = debugOverlay.getGraphics();

		// The constant 850 passed is the max squared distance from the
		// centroid in which the farthest points can be located.for one
		// pain
		Position[] plateCorners = null;
		plateCorners = findFurthest(debugOverlay, plateCentroid, points, 850);

		// Finding the shortest sides of the plates and returns their
		// average values in order to draw the line in the middle of the
		// plate
		Position[] avgPts = findAvgPtOfTwoShortestSides(plateCorners);
		Position avg1 = avgPts[0];
		Position avg2 = avgPts[1];

		/**
		 * Determining the orientation of the plate by looking at square at
		 * around the top 1/7 of the plate and bottom 1/7 to find the grey
		 * circle.
		 */
		int searchPtX = (6 * avg1.getX() + avg2.getX()) / 7;
		int searchPtY = (6 * avg1.getY() + avg2.getY()) / 7;
		Position searchPt1 = new Position(searchPtX, searchPtY);

		searchPtX = (avg1.getX() + 6 * avg2.getX()) / 7;
		searchPtY = (avg1.getY() + 6 * avg2.getY()) / 7;
		Position searchPt2 = new Position(searchPtX, searchPtY);

		Color colour = null;
		float[] colourHSV = null;

		// Try one (short) side
		int searchPt1GreyPoints = 0;
		int xMin = searchPt1.getX() - 5, xMax = xMin + 10;
		int yMin = searchPt1.getY() - 5, yMax = yMin + 10;
		for (int x = xMin; x < xMax; ++x) {
			for (int y = yMin; y < yMax; ++y) {
				colour = new Color(frame.getRGB(x, y));
				colourHSV = Color.RGBtoHSB(colour.getRed(), colour.getGreen(),
						colour.getBlue(), null);

				if (isColour(colour, colourHSV, GREY_CIRCLE)) {
					++searchPt1GreyPoints;
				}
			}
		}
		// Try the other side
		int searchPt2GreyPoints = 0;
		xMin = searchPt2.getX() - 5;
		xMax = xMin + 10;
		yMin = searchPt2.getY() - 5;
		yMax = yMin + 10;
		for (int x = xMin; x < xMax; ++x) {
			for (int y = yMin; y < yMax; ++y) {
				colour = new Color(frame.getRGB(x, y));
				colourHSV = Color.RGBtoHSB(colour.getRed(), colour.getGreen(),
						colour.getBlue(), null);

				if (isColour(colour, colourHSV, GREY_CIRCLE)) {
					++searchPt2GreyPoints;
				}
			}
		}

		double xvector = 0;
		double yvector = 0;

		// Apply barrel correction before calculating angle
		avg1 = DistortionFix.barrelCorrect(avg1);
		avg2 = DistortionFix.barrelCorrect(avg2);

		// Checking which side has more "grey" points - that side is the
		// back, the other is the front
		Position front = null, back = null;
		boolean error = false;
		if (searchPt1GreyPoints > searchPt2GreyPoints) {
			xvector = avg1.getX() - avg2.getX();
			yvector = avg1.getY() - avg2.getY();

			front = searchPt2;
			back = searchPt1;
		} else if (searchPt1GreyPoints < searchPt2GreyPoints) {
			xvector = avg2.getX() - avg1.getX();
			yvector = avg2.getY() - avg1.getY();

			front = searchPt1;
			back = searchPt2;
		} else {
			error = true;
			front = searchPt1;
			back = searchPt2;
		}

		/** Debugging shapes drawn on the debugging layer of the video feed */
		debugGraphics.setColor(Color.magenta);
		debugGraphics.drawRect(front.getX() - 5, front.getY() - 5, 10, 10);
		debugGraphics.setColor(Color.black);
		debugGraphics.drawRect(back.getX() - 5, back.getY() - 5, 10, 10);

		// Line through the averages for one plate
		debugGraphics.setColor(Color.white);
		debugGraphics.drawLine(avg1.getX(), avg1.getY(), avg2.getX(),
				avg2.getY());
		debugGraphics.drawOval(plateCentroid.getX() - 1,
				plateCentroid.getY() - 1, 2, 2);

		// The ovals around the 4 corners of the plate
		debugGraphics.drawOval(plateCorners[0].getX() - 1,
				plateCorners[0].getY() - 1, 2, 2);
		debugGraphics.drawOval(plateCorners[1].getX() - 1,
				plateCorners[1].getY() - 1, 2, 2);
		debugGraphics.drawOval(plateCorners[2].getX() - 1,
				plateCorners[2].getY() - 1, 2, 2);
		debugGraphics.drawOval(plateCorners[3].getX() - 1,
				plateCorners[3].getY() - 1, 2, 2);

		if (error)
			throw new NoAngleException("Can't distinguish front from back");
		
		double angle = 0;
		angle = Math.acos(yvector
				/ Math.sqrt(xvector * xvector + yvector * yvector));
		if (xvector > 0)
			angle = 2.0 * Math.PI - angle;
		return angle;
	}
}
