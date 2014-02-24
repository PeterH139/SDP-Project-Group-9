/**
 * Name: Point.java
 * Author: Dimitar Petrov
 * Description: represents a point in a 2D plane 
 */
package pc.world;

public class Point {
	private float x, y;

	public Point(float x, float y){
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
