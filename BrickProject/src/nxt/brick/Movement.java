package nxt.brick;

import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.util.Delay;

/**
 * The Movement class. Handles the actual driving and movement of the robot, once
 * BrickController has processed the commands.
 * 
 * It defines the behaviour of the robot when it receives the command.
 * 
 * Adapted from SDP2013 groups 7 code: original author sauliusl
 * 
 * @author Ross Grassie
 * 
 */
public class Movement extends DifferentialPilot {

	static NXTRegulatedMotor LEFT_WHEEL = Motor.B;
	static NXTRegulatedMotor RIGHT_WHEEL = Motor.C;
	static NXTRegulatedMotor KICKER = Motor.A;
	static NXTMotor KICKER_UNREGULATED = new NXTMotor(MotorPort.A);
	static final int TYRE_DIAMETER = 56;
	public int maxPilotSpeed;
	public static final int KICKER_UP_ANGLE = 80;
	public static final int MAXIMUM_KICKER_SPEED = (int) KICKER.getMaxSpeed() + 100;
	public static final int ACCELERATION = MAXIMUM_KICKER_SPEED * 8;
	public static final int REVERSE_KICKER_DIRECTION = -1;
	public static final int GEAR_RATIO = 5 * REVERSE_KICKER_DIRECTION;
	public boolean kickerIsDown = true;

	private static volatile boolean isKicking = false;

	public Movement(double trackWidth) {
		super(TYRE_DIAMETER, trackWidth, LEFT_WHEEL, RIGHT_WHEEL);
		floatWheels();
		resetCatcher();
	}
	
	public void setMaxPilotSpeed(int speed) {
		this.maxPilotSpeed = speed;
	}

	public static void floatWheels() {
		LEFT_WHEEL.flt();
		RIGHT_WHEEL.flt();
		KICKER.flt();
	}
	public void resetKicker() {
		if (kickerIsDown) {
			return;
		}
		KICKER.rotateTo(0);
		kickerIsDown = true;
	}
	public void prepKicker() {
		if (!kickerIsDown) {
			return;
		}
		int prevSpeed = KICKER.getSpeed();
		KICKER.setSpeed(50);
		KICKER.rotate(KICKER_UP_ANGLE/GEAR_RATIO);
		KICKER.setSpeed(prevSpeed);
		kickerIsDown = false;
	}
	
	public void kick(int speed) {

		if (!kickerIsDown) {
			return;
		}
		
		if (speed > 100) {
			speed = 100;
		}
		
		KICKER.setSpeed(speed * MAXIMUM_KICKER_SPEED/100);
		KICKER.setAcceleration(ACCELERATION);

		// Kick
		KICKER.rotate(120/GEAR_RATIO);
		// Reset
		KICKER.rotateTo(KICKER_UP_ANGLE/GEAR_RATIO);
		
		kickerIsDown = false;
	}
	
	public void movingKick(int speed) {
		isKicking = true;
		while(isKicking) {
			setTravelSpeed(this.maxPilotSpeed);
			travel(.5,true);
			kick(speed);
		}
	}
	
	public boolean isReady() {
		return true;
	}

	public void resetCatcher() {
		kickerIsDown = true;
		KICKER.suspendRegulation();
		KICKER_UNREGULATED.setPower(30);
		KICKER_UNREGULATED.forward();
		Delay.msDelay(500);
		KICKER_UNREGULATED.flt();
		Delay.msDelay(500);
		KICKER.resetTachoCount();
		prepKicker();
	}

}
