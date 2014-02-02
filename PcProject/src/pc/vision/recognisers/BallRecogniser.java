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
		ArrayList<Position> ignore = new ArrayList<Position>();
		int leftBuffer = this.pitchConstants.getLeftBuffer();
		int rightBuffer = this.pitchConstants.getRightBuffer();
		vision.searchColumn(ignore, ballPoints, frame, debugOverlay,
				leftBuffer, frame.getWidth() - rightBuffer, true);
		Position ball = vision.calculatePosition(ballPoints);

		debugGraphics.setColor(Color.red);
		debugGraphics.drawLine(0, ball.getY(), 640, ball.getY());
		debugGraphics.drawLine(ball.getX(), 0, ball.getX(), 480);

		worldState.setBallX(ball.getX());
		worldState.setBallY(ball.getY());
	}

}
