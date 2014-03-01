package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;
import pc.vision.PixelInfo;
import pc.vision.Position;
import pc.vision.Vector2f;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.StaticWorldState;
import pc.world.oldmodel.MovingObject;
import pc.world.oldmodel.WorldState;

public class BallRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private DistortionFix distortionFix;
	private Vector2f previousBallPosition = new Vector2f(0, 0);
	private pc.logging.Logging logger;

	public BallRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants, DistortionFix distortionFix) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
		logger = new pc.logging.Logging();
	}

	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay,
			StaticWorldState result) {
		ArrayList<Position> ballPoints = new ArrayList<Position>();
		int top = this.pitchConstants.getPitchTop();
		int left = this.pitchConstants.getPitchLeft();
		int right = left + this.pitchConstants.getPitchWidth();
		int bottom = top + this.pitchConstants.getPitchHeight();

		for (int row = top; row < bottom; row++) {
			for (int column = left; column < right; column++) {
				if (pixels[column][row] != null) {
					if (vision.isColour(pixels[column][row],
							PitchConstants.OBJECT_BALL)) {
						ballPoints.add(new Position(column, row));
						if (this.pitchConstants
								.debugMode(PitchConstants.OBJECT_BALL)) {
							debugOverlay.setRGB(column, row, 0xFF000000);
						}
					}
				}
			}
		}

		Vector2f ballPosition = vision.calculatePosition(ballPoints);

		if (ballPosition.x == 0 && ballPosition.y == 0) {
			ballPosition = previousBallPosition;
			// logger.Log("Ball Lost");
		} else {
			// Distortion fixing
			Point2D.Double point = new Point2D.Double(ballPosition.x,
					ballPosition.y);
			distortionFix.barrelCorrect(point);
			ballPosition.x = (float) point.x;
			ballPosition.y = (float) point.y;

			previousBallPosition = ballPosition;

			// logger.Log("X="+ballPosition.x+" Y="+ballPosition.y);
			// logger.Log("["+ballPosition.x+", "+ballPosition.y+"]");
		}

		MovingObject ball_m = new MovingObject(ballPosition.x, ballPosition.y);
		worldState.setBall(ball_m);
		
		result.setBall(new Point(0, 0));

		// Debugging Graphics
		debugGraphics.setColor(Color.red);
		debugGraphics.drawLine(0, (int) ballPosition.y, 640,
				(int) ballPosition.y);
		debugGraphics.drawLine((int) ballPosition.x, 0, (int) ballPosition.x,
				480);
	}

}
