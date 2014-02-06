package pc.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import pc.vision.PitchConstants;
import pc.vision.Position;
import pc.vision.Vision;
import pc.vision.interfaces.ObjectRecogniser;
import pc.world.WorldState;
import pc.world.MovingObject;

public class BallRecogniser implements ObjectRecogniser {
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;

	public BallRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants) {
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
	}

	@Override
	public void processFrame(BufferedImage frame, Graphics2D debugGraphics,
			BufferedImage debugOverlay) {
		ArrayList<Position> ballPoints = new ArrayList<Position>();
		int top = this.pitchConstants.getPitchTop();
		int left = this.pitchConstants.getPitchLeft();
		int right = left + this.pitchConstants.getPitchWidth();
		int bottom = top + this.pitchConstants.getPitchHeight();

		float hsbvals[] = new float[3];
		for (int row = top; row < bottom; row++) {
			for (int column = left; column < right; column++) {
				Color c = new Color(frame.getRGB(column, row));
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);

				int ballObj = PitchConstants.OBJECT_BALL;
				float[] colourValues = { c.getRed(), c.getGreen(), c.getBlue(),
						hsbvals[0], hsbvals[1], hsbvals[2], };

				boolean colourMatch = true;
				for (int ch = 0; ch < PitchConstants.NUM_CHANNELS
						&& colourMatch; ch++) {
					if (!Vision.checkBounds(colourValues[ch],
							this.pitchConstants.getLowerThreshold(ballObj, ch),
							this.pitchConstants.getUpperThreshold(ballObj, ch),
							this.pitchConstants
									.isThresholdInverted(ballObj, ch)))
						colourMatch = false;
				}
				if (colourMatch) {
					ballPoints.add(new Position(column, row));
					if (this.pitchConstants.debugMode(PitchConstants.OBJECT_BALL)) {
						debugOverlay.setRGB(column, row, 0xFF000000);
					}
				}
			}
		}

		Position ball = vision.calculatePosition(ballPoints);

		debugGraphics.setColor(Color.red);
		debugGraphics.drawLine(0, ball.getY(), 640, ball.getY());
		debugGraphics.drawLine(ball.getX(), 0, ball.getX(), 480);

		worldState.setBallX(ball.getX());
		worldState.setBallY(ball.getY());

		MovingObject ball_m = new MovingObject(ball.getX(), ball.getY());
		worldState.SetBall(ball_m);
	}

}
