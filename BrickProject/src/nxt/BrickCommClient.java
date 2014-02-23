package nxt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import nxt.brick.Movement;
import reallejos.shared.RobotOpcode;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.util.Delay;
import lejos.util.TextMenu;

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
		this.connection = Bluetooth.waitForConnection();
		this.pcInput = this.connection.openDataInputStream();
		this.pcOutput = this.connection.openDataOutputStream();
	}

	public void releaseConnection() {
		this.connection.close();
	}
	
	private void handleStop() {
		this.rc.getMovementController().stop();
		this.movingForwards = false;
		this.movingBackwards = false;
	}
	private void handleCatch() {
		this.rc.getMovementController().resetKicker(true);
		this.kickerState = 0;
	}
	
	private void handleForwards() {
		this.rc.getMovementController().forward();
		this.movingForwards = true;
	}
	
	private void handleBackwards() {
		this.rc.getMovementController().backward();
		this.movingBackwards = true;
	}
	
	private void handleKick() throws IOException {
		int speed = this.pcInput.readInt();
		this.rc.getMovementController().kick(speed);
		this.kickerState = 0;
	}
	private void handlePrepCatcher() throws IOException {
		if (this.kickerState == 0){
			this.rc.getMovementController().prepKicker(false);
			this.kickerState = 1;
		}
	}
	private void handleRotate(boolean clockwise) throws IOException {
		if (clockwise)
			this.rc.getMovementController().rotateRight();
		else
			this.rc.getMovementController().rotateLeft();
	}
	
	private void handleRotateBy() throws IOException {
		int angle = this.pcInput.readInt();
		double speed = this.pcInput.readDouble();
		this.rc.getMovementController().setRotateSpeed(speed);
		this.rc.getMovementController().rotate(angle, true);
	}

	private void handleArcForwards() throws IOException {
		double radius = this.pcInput.readDouble();
		int distance = this.pcInput.readInt();
		int speed = this.pcInput.readInt();
		this.rc.getMovementController().setTravelSpeed(speed);
		this.rc.getMovementController().travelArc(radius, distance, true);
	}
	
	private void handleTravel() throws IOException {
		this.kickerState = 0;
		int distance = this.pcInput.readInt();
		int speed = this.pcInput.readInt();
		this.rc.getMovementController().setTravelSpeed(speed);
		this.rc.getMovementController().travel(distance, true);	
	}
		
	private void handleTest() throws IOException {
		this.pcOutput.writeBoolean(true);
		this.pcOutput.flush();
	}
	
	private void handleTestINT() throws IOException {
		this.pcOutput.writeBoolean(true);
		this.pcOutput.flush();
		int test = this.pcInput.readInt();
	}
	
	private void handleTestDOUBLE() throws IOException {
		this.pcOutput.writeBoolean(true);
		this.pcOutput.flush();
		double test = this.pcInput.readDouble();
	}
	
	private void handleTestINTANDDOUBLE() throws IOException {
		this.pcOutput.writeBoolean(true);
		this.pcOutput.flush();
		int testINT = this.pcInput.readInt();
		double testDOUBLE = this.pcInput.readDouble();
	}

	public void runController() {
		try {
			System.out.println("Controller ready");
			while (true) {
				int opcode = this.pcInput.readInt();
				
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
					handlePrepCatcher();
					break;
				case RobotOpcode.CATCH:
					handleCatch();
					break;
				case RobotOpcode.TEST:
					handleTest();
					break;
				case RobotOpcode.TESTINT:
					handleTestINT();
					break;
				case RobotOpcode.TESTDOUBLE:
					handleTestDOUBLE();
					break;
				case RobotOpcode.TESTINTANDDOUBLE:
					handleTestINTANDDOUBLE();
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
		String[] robotsList = {"Keeper", "Attacker"};
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				System.exit(0);
			}  
			
			@Override
			public void buttonPressed(Button b) {
			}
		});
		
		boolean selectionMade = false;
		boolean isKeeper = false;
		
		while (!selectionMade) {
			TextMenu robotSelection = new TextMenu(robotsList, 1, "What robot am I?");

			int robotNumber = robotSelection.select();

			if (robotNumber == 0) {
				isKeeper = true;
				selectionMade = true;
			} else if (robotNumber == 1) {
				isKeeper = false;
				selectionMade = true;
			} 
			
			Delay.msDelay(300);
		}
		
		LCD.clearDisplay();
		LCD.refresh();		
		
		RobotController rc = new RobotController(isKeeper);
		rc.getMovementController().setTravelSpeed(300);
		BrickCommClient bc = new BrickCommClient(rc);
		while (true) {
			bc.makeConnection();
			bc.runController();
			bc.releaseConnection();
		}
	}

}
