package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.robotics.navigation.DifferentialPilot;
 

public class Milestone1 {
	
	static final int TYRE_DIAMETER = 56;
	static final int TRACK_WIDTH = 110;
	
	private static int THRESHOLD_VALUE;
	
	private enum Location {
		UNKNOWN, FACING_WALL, PARALLEL_TO_WALL
	}

	static NXTRegulatedMotor leftMotor = Motor.A;
	static NXTRegulatedMotor rightMotor = Motor.B;
	
	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	
	static LightSensor lsLeft = new LightSensor(SensorPort.S1);
	static LightSensor lsRight = new LightSensor(SensorPort.S2);
	
	static Location loc = Location.UNKNOWN;
	private static boolean die = false;
	
	private static int lowLightValue, highLightValue;
	private static boolean isWhiteRight, isWhiteLeft;
	
	public static void main(String[] args) {
		leftMotor.setSpeed(leftMotor.getMaxSpeed()/5);
		rightMotor.setSpeed(rightMotor.getMaxSpeed()/5);
		calibrateValues();
		while(!die){
			// Check Sensor and button inputs
			int rightReading = lsRight.getLightValue();
			int leftReading = lsLeft.getLightValue();
			isWhiteRight = (rightReading > THRESHOLD_VALUE) ? true : false;
			isWhiteLeft = (leftReading > THRESHOLD_VALUE) ? true : false;
			
			if (Button.LEFT.isDown()){
				calibrateValues();
			} else if (Button.ESCAPE.isDown()){
				die = true;
			}

			// Determine appropriate action, based upon location and sensor inputs.
			switch (loc){
			case UNKNOWN:
				if (isWhiteRight) rightMotor.flt(); else rightMotor.forward();
				if (isWhiteLeft) leftMotor.flt(); else leftMotor.forward();
				if (isWhiteRight && isWhiteLeft){
					loc = Location.FACING_WALL;
					LCD.clear();
					LCD.drawString("Facing a wall!", 0, 2);
				}
				break;
			case FACING_WALL:
				if (isWhiteRight && isWhiteLeft){
					pilot.rotate(90);
					pilot.forward();
					loc = Location.PARALLEL_TO_WALL;
					LCD.clear();
					LCD.drawString("Paralell to wall!", 0, 2);
				} else {
					loc = Location.UNKNOWN;
				}
				break;
			case PARALLEL_TO_WALL:
				if (isWhiteRight && isWhiteLeft){
					pilot.stop();
					loc = Location.FACING_WALL;
					LCD.clear();
					LCD.drawString("Facing a wall!", 0, 2);
				} else if (isWhiteRight ^ isWhiteLeft){
					pilot.forward();
				} else {
					loc = Location.UNKNOWN;
				}
			}
			// Carry out action and update location.
		}
	}

	private static void calibrateValues() {
		LCD.clear();
		LCD.drawString("Calibrate green...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		lowLightValue = lsLeft.getLightValue();
		Sound.playTone(1000, 200, 100);
		
		LCD.clear();
		LCD.drawString("Calibrate white...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		highLightValue = lsLeft.getLightValue();        
		Sound.playTone(1000, 200, 100);
		
		THRESHOLD_VALUE = (highLightValue + lowLightValue)/2;
	}
}
