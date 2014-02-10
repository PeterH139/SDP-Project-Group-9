/**
 * Name : MovingObject.java
 * Author : Dimitar Petrov
 * Description : Stores data relating to an object capable of movement:
 * Coordinates, velocity, angular orientation
 * */
package pc.world;

public class MovingObject {
	//x,y, representation on the grid
	//x,y are mm representations
	public float x;
	public float y;
	
	public double velocity;
	
	//Orientation coordinates
	public float orientation_angle;
	
	/**
	 * Initializes a moving object
	 * @param x represents the X coordinate
	 * @param y represents the Y coordinate
	 * @param angle represents the orientation angle of the object
	 * */
	public MovingObject(float x, float y, float angle){
		this.x = x;
		this.y = y;
		this.orientation_angle = angle;
	}
	
	/**
	 * Initializes a moving object
	 * @param x represents the X coordinate
	 * @param y represents the Y coordinate
	 * */
	public MovingObject(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	//rotating point of robots, need to represent
	//robot dimension extension from plates
}
