package pc.world;

import java.awt.Point;

public class DirectedPoint extends Point {
	private static final long serialVersionUID = -4732543264234985413L;

	private double direction;
	
	public DirectedPoint() {
		super();
		direction = 0;
	}
	
	public DirectedPoint(DirectedPoint p) {
		super(p);
		direction = p.getDirection();
	}
	
	public DirectedPoint(int x, int y, double direction) {
		super(x, y);
		this.direction = direction;
	}
	
	public double getDirection() {
		return direction;
	}
	
	public void setDirection(double direction) {
		this.direction = direction;
	}
}
