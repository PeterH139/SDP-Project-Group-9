package nxt.brick;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

/*
 * The Movement class. Handles the actual driving and movement of the robot, once
 * BrickController has processed the commands.
 * 
 * That is -- defines the behaviour of the robot when it receives the command.
 * 
 * Adapted from SDP2013 groups 7 code -- original author sauliusl
 * 
 * @author Ross Grassie
 * 
 */
public class Movement extends DifferentialPilot {

	static NXTRegulatedMotor LEFT_WHEEL = Motor.B;
	static NXTRegulatedMotor RIGHT_WHEEL = Motor.C;
	static NXTRegulatedMotor KICKER = Motor.A;
	static final int TYRE_DIAMETER = 56;

	public int maxPilotSpeed = 200;					// 90 for tests

	// TODO: potential changes to be made here due to different robots
	public static final int MAXIMUM_KICKER_SPEED = (int) KICKER.getMaxSpeed();
	public static final int MEDIUM_KICKER_SPEED = 600;
	public static final int LOW_KICKER_SPEED = 300;
	public static final int ACCELERATION = MAXIMUM_KICKER_SPEED * 8;
	public static final int REVERSE_KICKER_DIRECTION = -1;
	public static final int GEAR_ERROR_RATIO = 5 * REVERSE_KICKER_DIRECTION;

	private static volatile boolean isKicking = false;

	public Movement(double trackWidth) {
		super(TYRE_DIAMETER, trackWidth, LEFT_WHEEL, RIGHT_WHEEL);
		KICKER.flt();
		KICKER.resetTachoCount();
	}

	public static void floatWheels() {
		LEFT_WHEEL.flt();
		RIGHT_WHEEL.flt();
		KICKER.flt();
	}
	public void resetKicker() {
		KICKER.rotateTo(0/GEAR_ERROR_RATIO);
	}
	public void liftKicker() {
		KICKER.rotateTo(120/GEAR_ERROR_RATIO);
	}
	public void kick(int speed) {

		if (isKicking) {
			return;
		}

		isKicking = true;
		
		KICKER.setSpeed(speed);

		// Kick
		liftKicker();
		// Reset
		resetKicker();
		
		isKicking = false;
	}
	
	public void movingKick(int speed) {
		isKicking = true;
		while(isKicking) {
			setTravelSpeed(maxPilotSpeed);
			travel(.5,true);
			kick(speed);
		}
	}
	
	public boolean isReady() {
		return true;
	}

}
