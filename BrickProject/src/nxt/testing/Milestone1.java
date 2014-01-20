package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
 

public class Milestone1 {
	
	private static int WHITE_VALUE = 45;

	private enum Location {
		UNKNOWN, FACING_WALL, PARALLEL_TO_WALL, IN_CORNER
	}
	
	static NXTRegulatedMotor a = Motor.A;
	static NXTRegulatedMotor b = Motor.B;
	
	static LightSensor ls = new LightSensor(SensorPort.S1);
	
	static Location loc = Location.UNKNOWN;
	private static boolean die = false;
	
	private static int lowLightValue, highLightValue;
	
	public static void main(String[] args) {
		calibrateValues();
		while(!die){
			// Check Sensor and button inputs
			int lightReading = ls.getLightValue();
			if (Button.LEFT.isDown()){
				calibrateValues();
			} else if (Button.ESCAPE.isDown()){
				die = true;
			}
			
			movement(lightReading);
			// Determine appropriate action, based upon location and sensor inputs.
//			switch (loc){
//			case UNKNOWN:
//			case FACING_WALL:
//			case PARALLEL_TO_WALL:
//			case IN_CORNER:
//			}
			
			// Carry out action and update location.
		}
	}

	private static void calibrateValues() {
		LCD.clear();
		LCD.drawString("Calibrate green...", 0, 2);
		LCD.drawString("Use left sensor & press Enter", 0, 3);
		Button.ENTER.waitForPressAndRelease();
		lowLightValue = ls.getLightValue();
		
		LCD.clear();
		LCD.drawString("Calibrate white...", 0, 2);
		LCD.drawString("Use left sensor & press Enter", 0, 3);
		Button.ENTER.waitForPressAndRelease();
		highLightValue = ls.getLightValue();
		
		LCD.drawString("Green: " + lowLightValue, 0, 2);
		LCD.drawString("White: " + highLightValue, 0, 3);
	}
	
	public static void movement(int lightValue) {
		if (lightValue > WHITE_VALUE) {
			a.stop();
			b.stop();
			a.setSpeed(a.getMaxSpeed()/5);
			b.setSpeed(b.getMaxSpeed()/5);
			a.forward();
			b.backward();
		}
		else {
			a.setSpeed(a.getMaxSpeed());
			b.setSpeed(b.getMaxSpeed());
			a.forward();
			b.forward();
		}

}
}
