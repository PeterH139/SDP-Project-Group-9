package nxt.testing;

import nxt.brick.Striker;

/*
 * Short test to check how durable the designs are under fast movements
 * 
 * @author Ross Grassie
 * 
 */

public class MovementTest {

	private static Striker striker = new Striker();
	static int count = 0; 
	
	public static void main(String[] args) {
		
		striker.steer(10,90);
		
		while (count < 50) {
			striker.backward();

			if (striker.getMovementIncrement() < -0.4) {
				count++;
			}
			
		}
		
		striker.stop();
		striker.rotate(90);
		
		while (count > 49 && count < 100) {
			striker.forward();
			
			if (striker.getMovementIncrement() > 0.4) {
				count++;
			}
		}
		
		striker.stop();
		striker.rotate(30);
		
		while (count > 99 && count < 150) {
			striker.backward();
			
			if (striker.getMovementIncrement() < -0.4) {
				count++;
			}
		}
		
		striker.rotate(180);
		
	}
}
