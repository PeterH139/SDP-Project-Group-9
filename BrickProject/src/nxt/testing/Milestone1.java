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
	static final int TRACK_WIDTH = 120;
	static final int TRAVEL_SPEED = 100;
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
	static Move forwardMove = new Move(MoveType.TRAVEL, 200, 0, true);
	static Move turnLeftMove = new Move(MoveType.ROTATE, 0, 120, true);
	static Move turnRightMove = new Move(MoveType.ROTATE, 0, -120, true);
	
	
	public static void main(String[] args) {
		pilot.setTravelSpeed(TRAVEL_SPEED);
		pilot.setRotateSpeed(pilot.getMaxRotateSpeed()/40);
		leftHome = false;
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

			LCD.clear();
			LCD.drawString(pos.getPose().getX() + " " + pos.getPose().getY() , 0, 3);
			LCD.drawString(start.getX() + " " + start.getY() , 0, 4);
			
			// Determine appropriate action, based upon location and sensor inputs.
			// Carry out action and update location.
			switch (loc){
			case UNKNOWN:
				pilot.forward();
				if (isWhiteRight || isWhiteLeft){
					rightHitFirst = isWhiteRight ? true : false;
					haveMadeContact = true;
					loc = Location.ON_EDGE;
					LCD.clear();
					LCD.drawString("On Edge!", 0, 2);
				}
				
				break;
			case ON_EDGE:
				boolean xCheck = (pos.getPose().getX() <= start.getX() + 20) && (pos.getPose().getX() >= start.getX() -20);
				boolean yCheck = (pos.getPose().getY() <= start.getY() + 20) && (pos.getPose().getY() >= start.getY() -20);
				if (xCheck && yCheck && leftHome) { loc=Location.FINISHED; }
				  else {
					if (rightHitFirst){
						pilot.rotate(120, true);
						pos.moveStarted(turnLeftMove, pilot);
					} else {
						pilot.rotate(120, true);
						pos.moveStarted(turnRightMove, pilot);
					}
					while (isWhiteRight || isWhiteLeft){
						checkSensors();
					} 
					if (first){ start = pos.getPose(); first = false;}
					pilot.forward();
					pos.moveStarted(forwardMove, pilot);
					loc = Location.PARALLEL_TO_WALL;
				}
				break;
				
			case PARALLEL_TO_WALL:
				xCheck = (pos.getPose().getX() <= start.getX() + 20) && (pos.getPose().getX() >= start.getX() -20);
				yCheck = (pos.getPose().getY() <= start.getY() + 20) && (pos.getPose().getY() >= start.getY() -20);
				if (xCheck && yCheck && leftHome) { 
					loc = Location.FINISHED; 
				}
				if (pos.getPose().getX() >= (start.getX() + 100)) {
					leftHome = true;
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
