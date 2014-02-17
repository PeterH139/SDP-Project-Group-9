package nxt.testing;

import nxt.brick.Striker;

/**
 * A simple test so that we can check new catcher desings, the catcher comes up, <br>
 * there's a 1.5 second delay to get the ball in place (Watch your fingers!) and then it resets again.
 * 
 * The main point is to not have any more accidental knock ons like in milestone 3.
 * @author Daniel
 *
 */
public class CatcherTest {
	
	private static Striker striker = new Striker();
	
	public static void main(String[] args0) {
		
		//Kicker goes up
		striker.liftKicker(false);
		
		//1.5 second delay
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		//Move the kicker down ("catch")
		striker.resetKicker(false);
		
	}

}
