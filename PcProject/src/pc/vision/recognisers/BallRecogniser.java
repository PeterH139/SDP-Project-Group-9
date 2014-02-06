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
		int top = this.pitchConstants.getTopBuffer();
		int left = this.pitchConstants.getLeftBuffer();
		int right = frame.getWidth() - this.pitchConstants.getRightBuffer();
		int bottom = frame.getHeight() - this.pitchConstants.getBottomBuffer();
		
		for (int row = top; row < bottom; row++){
			for (int column = left; column < right; column++){
				Color c = new Color(frame.getRGB(column, row));
				float hsbvals[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
				
				int ballObj = PitchConstants.BALL;
				
				boolean colourMatch = Vision.checkBounds(c.getRed(),
						this.pitchConstants.getRedLower(ballObj),
						this.pitchConstants.getRedUpper(ballObj),
						this.pitchConstants.isRedInverted(ballObj))
						&& Vision.checkBounds(c.getGreen(),
								this.pitchConstants.getGreenLower(ballObj),
								this.pitchConstants.getGreenUpper(ballObj),
								this.pitchConstants.isGreenInverted(ballObj))
						&& Vision.checkBounds(c.getBlue(),
								this.pitchConstants.getBlueLower(ballObj),
								this.pitchConstants.getBlueUpper(ballObj),
								this.pitchConstants.isBlueInverted(ballObj))
						&& Vision.checkBounds(hsbvals[0],
								this.pitchConstants.getHueLower(ballObj),
								this.pitchConstants.getHueUpper(ballObj),
								this.pitchConstants.isHueInverted(ballObj))
						&& Vision.checkBounds(hsbvals[1],
								this.pitchConstants.getSaturationLower(ballObj),
								this.pitchConstants.getSaturationUpper(ballObj),
								this.pitchConstants.isSaturationInverted(ballObj))
						&& Vision.checkBounds(hsbvals[2],
								this.pitchConstants.getValueLower(ballObj),
								this.pitchConstants.getValueUpper(ballObj),
								this.pitchConstants.isValueInverted(ballObj));
				
				if (colourMatch){
					ballPoints.add(new Position(column,row));
					if (this.pitchConstants.debugMode(PitchConstants.BALL)) {
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

		MovingObject ball_m =  new MovingObject(ball.getX(), ball.getY());
		worldState.SetBall(ball_m);
	}

}
