package nxt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nxt.brick.Movement;

import reallejos.shared.RobotOpcode;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BrickCommClient {

	NXTConnection connection;
	DataInputStream pcInput;
	DataOutputStream pcOutput;
	RobotController rc;
	int kickerState = 0;
	
	boolean movingForwards = false, movingBackwards = false;
	int turnRadius = 0;

	public BrickCommClient(RobotController rc) {
		this.rc = rc;
	}

	public void makeConnection() {
		System.out.println("Bluetooth\nWaiting for connection");
		connection = Bluetooth.waitForConnection();
		pcInput = connection.openDataInputStream();
		pcOutput = connection.openDataOutputStream();
	}

	public void releaseConnection() {
		connection.close();
	}
	
	private void handleStop() {
		System.out.println("Stop");
		rc.getMovementController().stop();
		movingForwards = movingBackwards = false;
	}
	private void handleCatch() {
		System.out.println("Catch");
		rc.getMovementController().resetKicker();
		kickerState = 0;
	}
	
	private void handleForwards() {
		System.out.println("Forwards");
		rc.getMovementController().forward();
		movingForwards = true;
	}
	
	private void handleBackwards() {
		System.out.println("Backwards");
		rc.getMovementController().backward();
		movingBackwards = true;
	}
	
	private void handleKick() throws IOException {
		int speed = pcInput.readInt();
		System.out.println("Kick");
		rc.getMovementController().kick(speed);
		kickerState = 0;
	}
	private void handlePrepCatcher(int state) throws IOException {
		System.out.println("Preparing Catcher");
		if (kickerState == 0)
			rc.getMovementController().liftKicker();
		kickerState = 1;
	}
	private void handleRotate(boolean clockwise) throws IOException {
		System.out.println("Rotate");
		if (clockwise)
			rc.getMovementController().rotateRight();
		else
			rc.getMovementController().rotateLeft();
	}
	
	private void handleRotateBy() throws IOException {
		int angle = pcInput.readInt();
		rc.getMovementController().rotate(angle);
	}

	private void handleArcForwards() throws IOException {
		double radius = pcInput.readDouble();
		int distance = pcInput.readInt();
		rc.getMovementController().travelArc(radius, distance, true);
	}
	
	public void handleWheelSpeed() throws IOException {
		double speed = pcInput.readDouble();
		rc.getMovementController().setRotateSpeed(speed);
	}
	
	private void handleTravel() throws IOException {
		int distance = pcInput.readInt();
		int speed = pcInput.readInt();
		rc.getMovementController().setTravelSpeed(speed);
		rc.getMovementController().travel(distance, true);	
	}
	
	private void handleTest() throws IOException {
		System.out.println("Testing Bluetooth");
		pcOutput.writeBoolean(true);
		pcOutput.flush();
	}

	public void runController() {
		try {
			System.out.println("Controller ready");
			while (true) {
				int opcode = pcInput.readInt();
				
				switch (opcode) {
				case RobotOpcode.STOP:
					handleStop();
					break;
				case RobotOpcode.FORWARDS:
					handleForwards();
					break;
				case RobotOpcode.BACKWARDS:
					handleBackwards();
					break;
				case RobotOpcode.KICK:
					handleKick();
					break;
				case RobotOpcode.ROTATE_LEFT:
					handleRotate(false);
					break;
				case RobotOpcode.ROTATE_RIGHT:
					handleRotate(true);
					break;
				case RobotOpcode.ARC_FORWARDS:
					handleArcForwards();
					break;
				case RobotOpcode.ROTATE_BY:
					handleRotateBy();
					break;
				case RobotOpcode.TRAVEL:
					handleTravel();
					break;
				case RobotOpcode.APPROACHING_BALL:
					handlePrepCatcher(kickerState);
					break;
				case RobotOpcode.WHEEL_SPEED:
					handleWheelSpeed();
					break;
				case RobotOpcode.CATCH:
					handleCatch();
					break;
				case RobotOpcode.TEST:
					handleTest();
					break;
				
				case RobotOpcode.QUIT:
					return;
				default:
					System.err.println("Unknown opcode");
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("I/O Exception");
		}
		Movement.floatWheels();
	}

	public static void main(String[] args) {
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				System.exit(0);
			}
			
			@Override
			public void buttonPressed(Button b) {
			}
		});
		RobotController rc = new RobotController();
		rc.getMovementController().setTravelSpeed(300);
		BrickCommClient bc = new BrickCommClient(rc);
		while (true) {
			bc.makeConnection();
			bc.runController();
			bc.releaseConnection();
		}
	}

}
