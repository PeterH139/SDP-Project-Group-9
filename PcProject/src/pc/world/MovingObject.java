/*
 * Name : MovingObject.java
 * Author : Dimitar Petrov
 * Description : Stores data relating to an object capable of movement:
 * Coordinates, velocity, angular orientation
 * */
package pc.world;

public class MovingObject {
	//x,y, representation on the grid
	double x;
	double y;
	
	double velocity;
	
	//Orientation coordinates
	double angular_x;
	double angular_y;
}
