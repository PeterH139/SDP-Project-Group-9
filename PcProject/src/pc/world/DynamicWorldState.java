package pc.world;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class DynamicWorldState {
	private Ball ball = new Ball();
	private Robot attacker = new Robot(RobotModel.ATTACKER_ROBOT);
	private Robot defender = new Robot(RobotModel.GENERIC_ROBOT);
	private Robot enemyAttacker = new Robot(RobotModel.GENERIC_ROBOT);
	private Robot enemyDefender = new Robot(RobotModel.GENERIC_ROBOT);

	private final List<StateUpdateListener> listeners = new ArrayList<StateUpdateListener>();

	public void addStateListener(StateUpdateListener listener) {
		listeners.add(listener);
	}

	public void removeStateListener(StateUpdateListener listener) {
		listeners.remove(listener);
	}

	public Ball getBall() {
		return ball;
	}

	public Robot getAttacker() {
		return attacker;
	}

	public Robot getDefender() {
		return defender;
	}

	public Robot getEnemyAttacker() {
		return enemyAttacker;
	}

	public Robot getEnemyDefender() {
		return enemyDefender;
	}

	public void pushState(StaticWorldState staticState, long timestamp) {
		ball.pushState(staticState.getBall(), timestamp);
		attacker.pushState(staticState.getAttacker(), timestamp);
		defender.pushState(staticState.getDefender(), timestamp);
		enemyAttacker.pushState(staticState.getEnemyAttacker(), timestamp);
		enemyDefender.pushState(staticState.getEnemyDefender(), timestamp);

		for (StateUpdateListener listener : listeners) {
			listener.stateUpdated();
		}
	}

	public interface StateUpdateListener {
		void stateUpdated();
	}

	public static class Robot {
		private final RobotModel model;
		private DirectedPoint pos;
		private Shape extents;
		private Shape catcher;
		private Shape plate;
		private AffineTransform plateCenterTransform;

		public Robot(RobotModel model) {
			this.model = model;
		}

		public void pushState(DirectedPoint newPos, long timestamp) {
			pos = newPos;
			AffineTransform at = new AffineTransform();
			at.translate(newPos.getX(), newPos.getY());
			at.rotate(newPos.getDirection() + Math.PI / 2);
			plate = at.createTransformedShape(model.getPlate());
			at.translate(model.getPlate().getCenterX(), model.getPlate()
					.getCenterY());
			plateCenterTransform = at;
			extents = at.createTransformedShape(model.getExtents());
			catcher = at.createTransformedShape(model.getCatcher());
		}

		public Point2D getCenter() {
			return pos;
		}

		public double getHeading() {
			return pos.getDirection();
		}

		public Shape getExtents() {
			return extents;
		}

		public Shape getCatcher() {
			return catcher;
		}

		public Shape getPlate() {
			return plate;
		}
		
		public AffineTransform getPlateCenterTransform() {
			return plateCenterTransform;
		}
	}

	public static class Ball {
		private final static double RADIUS = 16; // millimetres

		private Point pos;
		private Shape shape;

		public void pushState(Point newPos, long timestamp) {
			pos = newPos;
			shape = new Ellipse2D.Double(newPos.getX() - RADIUS, newPos.getY()
					- RADIUS, 2 * RADIUS, 2 * RADIUS);
		}

		public Point2D getPoint() {
			return pos;
		}

		public Shape getShape() {
			return shape;
		}
	}
}
