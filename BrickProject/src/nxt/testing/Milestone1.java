package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Move.MoveType;
import lejos.robotics.navigation.Pose;
 
public class Milestone1 {
	
	private static Pose start = new Pose();
	private static boolean first = true;
	
	private enum Location {
		UNKNOWN, ON_EDGE, PARALLEL_TO_WALL, FINISHED
	}
	
	static final int TYRE_DIAMETER = 56;
	static final int TRACK_WIDTH = 116;
	static final int TRAVEL_SPEED = 90;
	static final long ROUND_TIME = 52500;
	static NXTRegulatedMotor leftMotor = Motor.B;
	static NXTRegulatedMotor rightMotor = Motor.A;
	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	static OdometryPoseProvider pos = new OdometryPoseProvider(pilot);
	static int thresholdValue;
	static LightSensor lsLeft = new LightSensor(SensorPort.S3);
	static LightSensor lsRight = new LightSensor(SensorPort.S2);
	static Location loc = Location.UNKNOWN;
	static boolean die = false;
	static int lowLightValue, highLightValue;
	static boolean isWhiteRight, isWhiteLeft, haveMadeContact, rightHitFirst, leftHome;
    static long startTime = 0;
	
	
	public static void main(String[] args) {
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(pilot.getMaxRotateSpeed()/20  );
		leftHome = false;
		boolean firstRound = false;
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
			long timeCheck = System.currentTimeMillis() - startTime;
			LCD.clear();
			LCD.drawString(pos.getPose().getX() + " " + pos.getPose().getY() , 0, 3);
			LCD.drawString(start.getX() + " " + start.getY() , 0, 4);
			LCD.drawString("firstRound: " + firstRound, 0, 5);
			LCD.drawString("leftHome: " + leftHome, 0, 6);
			LCD.drawString("timeCheck:" + timeCheck, 0, 2);
			
			boolean xCheck = (pos.getPose().getX() <= start.getX() + 5) && (pos.getPose().getX() >= start.getX() -5);
			// Determine appropriate action, based upon location and sensor inputs.
			// Carry out action and update location.
			switch (loc){
			case UNKNOWN:
				pilot.forward();
				if (isWhiteRight || isWhiteLeft){
					rightHitFirst = isWhiteRight ? true : false;
					haveMadeContact = true;
					startTime = System.currentTimeMillis();
					loc = Location.ON_EDGE;
					LCD.clear();
					LCD.drawString("On Edge!", 0, 2);
				}
				
				break;
			case ON_EDGE:
				
				if (xCheck && firstRound && leftHome) { loc=Location.FINISHED; }
				  else {
					if (rightHitFirst){
						pilot.rotate(120, true);
					} else {
						pilot.rotate(-120, true);
					}
					while (isWhiteRight || isWhiteLeft){
						checkSensors();
					} 
					if (first){ start = pos.getPose(); first = false;}
					if (rightHitFirst) {
						leftMotor.setSpeed(100);
						rightMotor.setSpeed(90);
						leftMotor.forward();
						rightMotor.forward();
					}
					else {
						leftMotor.setSpeed(90);
						rightMotor.setSpeed(100);
						leftMotor.forward();
						rightMotor.forward();
					}
					
					if (rightHitFirst) {
						leftMotor.setSpeed(200);
						rightMotor.setSpeed(180);
						leftMotor.forward();
						rightMotor.forward();
					}
					else {
						leftMotor.setSpeed(180);
						rightMotor.setSpeed(200);
						leftMotor.forward();
						rightMotor.forward();
					}
					
					loc = Location.PARALLEL_TO_WALL;
				}
				break;
				
			case PARALLEL_TO_WALL:
				if ((xCheck && firstRound && leftHome) || (timeCheck > ROUND_TIME)) { 
					loc = Location.FINISHED; 
				}
				if ((pos.getPose().getX() >= (start.getX() + 100) || (pos.getPose().getX() <= start.getX() - 100))) {
					leftHome = true;
				}
				if (xCheck && leftHome) {
					firstRound = true;
					leftHome = false;
				}
				if (isWhiteRight || isWhiteLeft){
					loc = Location.ON_EDGE;
				}
				break;
			case FINISHED:
				pilot.stop();
				die=true;
				break;
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
		LCD.drawString("Calibrate white...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		highLightValue = lsLeft.readValue();      
		Sound.playTone(1000, 200, 100);
		
		LCD.clear();
		LCD.drawString("Calibrate green...", 0, 2);
		LCD.drawString("Use left sensor", 0, 3);
		LCD.drawString("and Press ENTER", 0, 4);
		Button.ENTER.waitForPressAndRelease();
		lowLightValue = lsLeft.readValue();
		Sound.playTone(1000, 200, 100);
		
		thresholdValue = (highLightValue + lowLightValue)/2;
	}
}
