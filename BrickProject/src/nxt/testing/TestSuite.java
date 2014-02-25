package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;
import lejos.util.TextMenu;
import nxt.brick.Movement;
import nxt.brick.Striker;


/**
 * This puts all of the tests in one place, to make it easier to repeatedly run them as well as to clear up
 * the number of junk files sitting on the brick. <br>
 * <br>
 * To add a test to the list: <br>
 * -Add the name of your test to the end of testsList <br>
 * -In main, add another else if statement with the index of your test name in testsList <br>
 * -From the if statement, call the test
 * 
 * @author Daniel
 *
 */
public class TestSuite {

	private static Striker brick = new Striker();
	
	private static String[] testsList = { "Kicker test", "Movement Test",
			"Catcher Test", "Motor Test"};
	

	public static void main(String[] args) {
		
		while (!Button.ESCAPE.isDown()) {
			TextMenu testsMenu = new TextMenu(testsList, 1, "Select a test");

			int testsNumber = testsMenu.select();

			if (testsNumber == 0) {
				kickerTest();
			} else if (testsNumber == 1) {
				movementTest();
			} else if (testsNumber == 2) {
				catcherTest();
			} else if (testsNumber == 3) {
				motorTest();
			}

			Delay.msDelay(300);
		}
	}

	
	/**
	 * This just makes the robot kick
	 */
	private static void kickerTest() {
		brick.kick(100);
	}

	
	/**
	 * A simple test so that we can check new catcher desings, the catcher comes up, <br>
	 * there's a 1.5 second delay to get the ball in place (Watch your fingers!) and then it resets again.
	 * 
	 * The main point is to not have any more accidental knock ons like in milestone 3.
	 * @author Daniel
	 *
	 */
	private static void catcherTest() {
		// Kicker goes up
		brick.prepKicker(false);

		// 1.5 second delay
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Move the kicker down ("catch")
		brick.resetKicker();

	}

	
	/**
	 * Makes the robot move around a lot to check stability and performance
	 */
	private static void movementTest() {
		LCD.clearDisplay();
		LCD.refresh();

		int count = 0;

		brick.steer(10, 90);
		while (!(Button.ESCAPE.isDown())) {
			while (count < 50) {
				// LCD.drawString(brick.getLeftTacho() + " " +
				// brick.getRightTacho(),0,1);
				LCD.drawString(" Travelled: " + brick.getMovementIncrement(),
						0, 2);
				brick.backward();

				if (brick.getMovementIncrement() < -0.4) {
					count++;
				}

			}

			brick.stop();
			brick.rotate(90);

			while (count > 49 && count < 100) {
				// LCD.drawString(brick.getLeftTacho() + " " +
				// brick.getRightTacho(),0,1);
				LCD.drawString(" Travelled: " + brick.getMovementIncrement(),
						0, 2);
				brick.forward();

				if (brick.getMovementIncrement() > 0.4) {
					count++;
				}
			}

			brick.stop();
			brick.rotate(30);

			while (count > 99 && count < 150) {
				// LCD.drawString(brick.getLeftTacho() + " " +
				// brick.getRightTacho(),0,1);
				LCD.drawString(" Travelled: " + brick.getMovementIncrement(),
						0, 2);
				brick.backward();

				if (brick.getMovementIncrement() < -0.4) {
					count++;
				}
			}

			brick.rotate(180);

		}
	}
	
	private static void motorTest() {
	
		NXTRegulatedMotor a = Motor.A;
		NXTRegulatedMotor b = Motor.B;
		NXTRegulatedMotor c = Motor.C;
		
		Boolean aIsMoving = false;
		Boolean bIsMoving = false;
		Boolean cIsMoving = false;
		
		while (!Button.ESCAPE.isDown()) {
			
			if (Button.RIGHT.isDown()) {
				if (!aIsMoving) {
					a.forward();
					aIsMoving = true;
				} else {
					a.stop();
					aIsMoving = false;
				}
			}
			
			if (Button.RIGHT.isDown()) {
				if (!aIsMoving) {
					a.forward();
					aIsMoving = true;
				} else {
					a.stop();
					aIsMoving = false;
				}
			}
			
			if (Button.LEFT.isDown()) {
				if (!bIsMoving) {
					b.forward();
					bIsMoving = true;
				} else {
					b.stop();
					bIsMoving = false;
				}
			}
			
			if (Button.ENTER.isDown()) {
				if (!cIsMoving) {
					c.forward();
					cIsMoving = true;
				} else {
					c.stop();
					cIsMoving = false;
				}
			}
		}
		
	
	}
	
	}
