package nxt.testing;

import nxt.brick.Movement;
import nxt.brick.Striker;

public class KickerTest {
	
	private static Striker brick = new Striker();
	
	public static void main(String[] args) {
		brick.shoot(Movement.MAXIMUM_KICKER_SPEED);
	}

}
