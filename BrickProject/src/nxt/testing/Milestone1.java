package nxt.testing;

import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;

public class Milestone1 {

	private enum Location {
		UNKNOWN, FACING_WALL, PARALLEL_TO_WALL, IN_CORNER
	}
	
	static NXTRegulatedMotor a = Motor.A;
	static NXTRegulatedMotor b = Motor.B;
	
	static LightSensor lsLeft = new LightSensor(SensorPort.S1);
	static LightSensor lsRight = new LightSensor(SensorPort.S2);
	
	static Location loc = Location.UNKNOWN;
	private static boolean die = false;
	
	public static void main(String[] args) {
		while(!die){
			// Check Sensor inputs
			int leftReading = lsLeft.getLightValue();
			int rightReading = lsRight.getLightValue();
			
			// Determine appropriate action, based upon location and sensor inputs.
			
			// Carry out action and update location.
		}
	}

}
