package nxt.testing;

import lejos.nxt.Button;
import nxt.brick.Movement;
import nxt.brick.Striker;

public class Milestone2 {
	
	private static Striker brick = new Striker();
	
	public static void main(String[] args) {
		
		while(!Button.ESCAPE.isDown()) {
			// Code for striker
			if (Button.LEFT.isDown()) { 
				brick.kick(Movement.MAXIMUM_KICKER_SPEED);
			}
		
			// Code for keeper
			if (Button.RIGHT.isDown()) {
				brick.kick(90);
			}
		}
	}

}
