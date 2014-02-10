/**
 * Name: DefenderFieldArea.java
 * Author: Dimitar Petrov
 * Description: A data structure to store the information regarding a defender
 * robot area of the playing field: 4 integer values, denoting the boundaries X and Y
 * axis boundaries for the playing field. Also 4 points that denote the sides of the
 * trapezoid 
 * */
package pc.world;

public class DefenderFieldArea {
	public int x1, x2, y1, y2;
	public int[] left_side, right_side;
}
