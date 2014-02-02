package nxt.brick;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

/**
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

	public final boolean INVERSE_WHEELS = false;
	public int maxPilotSpeed = 900;					// 90

	// TODO: potential changes to be made here due to different robots
	public static final int MAXIMUM_KICKER_SPEED = 900;
	public static final int MEDIUM_KICKER_SPEED = 600;
	public static final int LOW_KICKER_SPEED = 300;
	public static final int ACCELERATION = MAXIMUM_KICKER_SPEED * 8;
	public static final int REVERSE_KICKER_DIRECTION = -1;
	public static final int GEAR_ERROR_RATIO = 5 * REVERSE_KICKER_DIRECTION;

	private static volatile boolean isKicking = false;

	public Movement(double trackWidth) {
		super(TYRE_DIAMETER, trackWidth, LEFT_WHEEL, RIGHT_WHEEL);
	}

	public static void floatWheels() {
		LEFT_WHEEL.flt();
		RIGHT_WHEEL.flt();
		KICKER.flt();
	}
	public void catchBall() {
		KICKER.rotateTo(-60/GEAR_ERROR_RATIO);
	}
	public void liftKicker() {
		KICKER.rotateTo(80/GEAR_ERROR_RATIO);
	}
	public void kick(int speed) {

		if (isKicking) {
			return;
		}

		isKicking = true;

		KICKER.setSpeed(speed);

		// Move kicker back
		KICKER.rotateTo(-35/GEAR_ERROR_RATIO);
		KICKER.waitComplete();

		// Kick
		KICKER.rotateTo(120/GEAR_ERROR_RATIO);
		KICKER.waitComplete();

		// Reset
		KICKER.rotateTo(-65/GEAR_ERROR_RATIO);
		KICKER.waitComplete();

		//KICKER.flt();

		isKicking = false;
	}

	private static void setMotorSpeed(NXTRegulatedMotor motor, int speed) {
		boolean forward = true;
		if (speed < 0) {
			forward = false;
			speed = -1 * speed;
		}

		motor.setSpeed(speed);
		if (forward)
			motor.forward();
		else
			motor.backward();
	}

	public void setWheelSpeeds(int leftWheelSpeed, int rightWheelSpeed) {
		if (leftWheelSpeed > this.maxPilotSpeed)
			leftWheelSpeed = this.maxPilotSpeed;
		if (rightWheelSpeed > this.maxPilotSpeed)
			rightWheelSpeed = this.maxPilotSpeed;

		if (this.INVERSE_WHEELS) {
			leftWheelSpeed *= -1;
			rightWheelSpeed *= -1;
		}
		setMotorSpeed(LEFT_WHEEL, leftWheelSpeed);
		setMotorSpeed(RIGHT_WHEEL, rightWheelSpeed);
	}

	public int getMaximumWheelSpeed() {
		return this.maxPilotSpeed;
	}

	public boolean isReady() {
		return true;
	}

	public void connect() {
		//TODO Empty Block
	}

	public void disconnect() {
		//TODO Empty Block
	}

	/*
	 * TODO: potentially change or remove these as Tachometer appears to be
	 * out-dated.
	 * 
	 * If altering look at the OdometryPoseProvider class
	 * 
	 */
	public int getLeftTacho() {
		return LEFT_WHEEL.getTachoCount();
	}

	public int getRightTacho() {
		return RIGHT_WHEEL.getTachoCount();
	}

	public void resetLeftTacho() {
		LEFT_WHEEL.resetTachoCount();
	}

	public void resetRightTacho() {
		RIGHT_WHEEL.resetTachoCount();
	}

	public int getLeftSpeed() {
		return LEFT_WHEEL.getSpeed();
	}

	public int getRightSpeed() {
		return RIGHT_WHEEL.getSpeed();
	}

	public void setLeftSpeed(int speed) {
		setMotorSpeed(LEFT_WHEEL, speed);
	}

	public void setRightSpeed(int speed) {
		setMotorSpeed(RIGHT_WHEEL, speed);
	}

}
