package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Pose;
import lejos.geom.*;
 

public class Milestone1 {
	
	private static Pose brick = new Pose();
	private static Point start;
	private static boolean first = true;
	
	private enum Location {
		UNKNOWN, ON_EDGE, PARALLEL_TO_WALL
	}
	
	static final int TYRE_DIAMETER = 56;
	static final int TRACK_WIDTH = 120;
	static final double ROTATE_ANGLE = 2;
	static NXTRegulatedMotor leftMotor = Motor.A;
	static NXTRegulatedMotor rightMotor = Motor.B;
	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	
	static int thresholdValue;
	static LightSensor lsLeft = new LightSensor(SensorPort.S1);
	static LightSensor lsRight = new LightSensor(SensorPort.S2);
	static Location loc = Location.UNKNOWN;
	static boolean die = false;
	static int lowLightValue, highLightValue;
	static boolean isWhiteRight, isWhiteLeft, haveMadeContact, rightHitFirst;
	
	
	public static void main(String[] args) {
		leftMotor.setSpeed(leftMotor.getMaxSpeed()/20);
		rightMotor.setSpeed(rightMotor.getMaxSpeed()/20);
		calibrateValues();
		Point origin = brick.getLocation();
		while(!die){
			// Check Sensor and button inputs
			int rightReading = lsRight.getLightValue();
			int leftReading = lsLeft.getLightValue();
			isWhiteRight = (rightReading > thresholdValue) ? true : false;
			isWhiteLeft = (leftReading > thresholdValue) ? true : false;
			
			if (Button.LEFT.isDown()){
				calibrateValues();
				loc = Location.UNKNOWN;
				LCD.clear();
				LCD.drawString("I'm Lost! :(", 0, 2);
			} else if (Button.ESCAPE.isDown()){
				die = true;
			}

			// Determine appropriate action, based upon location and sensor inputs.
			// Carry out action and update location.
			switch (loc){
			case UNKNOWN:
				if (first) start = brick.getLocation(); first = false;
				if (!haveMadeContact){
					pilot.forward();
					if (isWhiteRight || isWhiteLeft){
						rightHitFirst = isWhiteRight ? true : false;
					}
				} else {
					if (isWhiteRight && isWhiteLeft){
						loc = Location.ON_EDGE;
						LCD.clear();
						LCD.drawString("On Edge!", 0, 2);
					}
				}
				break;
			case ON_EDGE:
				if (first) start = brick.getLocation(); first = false;
				while (isWhiteRight && isWhiteLeft){
					if (rightHitFirst){
						pilot.rotate(ROTATE_ANGLE);
					} else {
						pilot.rotate(-ROTATE_ANGLE);
					}
				}
				pilot.forward();
				loc = Location.PARALLEL_TO_WALL;
				LCD.clear();
				LCD.drawString("Paralell to wall!", 0, 2);
				checkIfLost();
				break;
			case PARALLEL_TO_WALL:
				if (first) start = brick.getLocation(); first = false;
				if (isWhiteRight && isWhiteLeft){
					pilot.stop();
					loc = Location.ON_EDGE;
					LCD.clear();
					LCD.drawString("On Edge!", 0, 2);
				} 
				checkIfLost();
			}
			if (brick.getLocation() == start) {
				float bearing = brick.relativeBearing(origin);
				brick.rotateUpdate(bearing);
				float distance = brick.distanceTo(origin);
				brick.moveUpdate(distance);
			}
		}
	}

	private static void checkIfLost() {
		if (!(isWhiteRight || isWhiteLeft)){
			loc = Location.UNKNOWN;
			LCD.clear();
			LCD.drawString("I'm Lost! :(", 0, 2);
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
		
		thresholdValue = (highLightValue + lowLightValue)/2;
	}
}
