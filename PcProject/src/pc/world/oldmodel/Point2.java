/**
 * Name: Point.java
 * Author: Dimitar Petrov
 * Description: represents a point in a 2D plane 
 */
package pc.world.oldmodel;

public class Point2 {
	private float x, y;

	public Point2(float x, float y){
		this.x = x;
		this.y = y;		
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}
