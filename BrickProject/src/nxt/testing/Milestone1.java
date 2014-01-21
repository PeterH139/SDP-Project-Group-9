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
	static final int TRAVEL_SPEED = 100;
	static NXTRegulatedMotor leftMotor = Motor.B;
	static NXTRegulatedMotor rightMotor = Motor.A;
	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	
	static int thresholdValue;
	static LightSensor lsLeft = new LightSensor(SensorPort.S3);
	static LightSensor lsRight = new LightSensor(SensorPort.S2);
	static Location loc = Location.UNKNOWN;
	static boolean die = false;
	static int lowLightValue, highLightValue;
	static boolean isWhiteRight, isWhiteLeft, haveMadeContact, rightHitFirst;
	
	
	public static void main(String[] args) {
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(pilot.getMaxRotateSpeed()/20);
		
		calibrateValues();
		while(!die){
			// Check Sensor and button inputs
			checkSensors();
			
			if (Button.LEFT.isDown()){
				calibrateValues();
				loc = Location.UNKNOWN;
				LCD.clear();
				LCD.drawString("I'm Lost! :(", 0, 2);
				haveMadeContact = false;
			} else if (Button.ESCAPE.isDown()){
				die = true;
			}

			// Determine appropriate action, based upon location and sensor inputs.
			// Carry out action and update location.
			switch (loc){
			case UNKNOWN:
				if (!haveMadeContact){
					pilot.forward();
					if (isWhiteRight || isWhiteLeft){
						rightHitFirst = isWhiteRight ? true : false;
						haveMadeContact = true;
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
				if (first){ start = brick.getLocation(); first = false;}
				LCD.drawString(start.toString(), 0, 3);
				if (brick.getLocation() == start) { pilot.stop();
				} else {
					if (rightHitFirst){
						pilot.rotateLeft();	
					} else {
						pilot.rotateRight();
					}
					
					while (isWhiteRight || isWhiteLeft){
						checkSensors();
					} 
					pilot.forward();
					loc = Location.PARALLEL_TO_WALL;
				}
				break;
				
			case PARALLEL_TO_WALL:
				
				if (brick.getLocation() == start) { pilot.stop(); }
				if (first) start = brick.getLocation(); first = false;
				if (isWhiteRight || isWhiteLeft){
					pilot.stop();
					loc = Location.ON_EDGE;
				}
			}
		}
	}

	private static void checkSensors() {
		int rightReading = lsRight.readValue();
		int leftReading = lsLeft.readValue();
		isWhiteRight = (rightReading > thresholdValue) ? true : false;
		isWhiteLeft = (leftReading > thresholdValue) ? true : false;
	}

	private static void calibrateValues() {
		LCD.clear();
		LCD.drawString("Calibrate green...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		lowLightValue = lsLeft.readValue();
		Sound.playTone(1000, 200, 100);
		
		LCD.clear();
		LCD.drawString("Calibrate white...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		highLightValue = lsLeft.readValue();      
		Sound.playTone(1000, 200, 100);
		
		thresholdValue = (highLightValue + lowLightValue)/2;
	}
}
