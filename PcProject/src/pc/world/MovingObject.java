/**
 * Name : MovingObject.java
 * Author : Dimitar Petrov
 * Description : Stores data relating to an object capable of movement:
 * Coordinates, velocity, angular orientation
 * */
package pc.world;

public class MovingObject {
	//x,y, representation on the grid
	public int x;
	public int y;
	
	public double velocity;
	
	//Orientation coordinates
	//TODO: possibly unneeded?
	public double angular_x;
	public double angular_y;
	public float orientation_angle;
	
	/**
	 * Initializes a moving object
	 * @param x represents the X coordinate
	 * @param y represents the Y coordinate
	 * @param angle represents the orientation angle of the object
	 * */
	public MovingObject(int x, int y, float angle){
		this.x = x;
		this.y = y;
		this.orientation_angle = angle;
	}
}
