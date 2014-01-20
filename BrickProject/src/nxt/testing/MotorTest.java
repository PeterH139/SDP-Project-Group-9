package nxt.testing;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;


public class MotorTest {
	
	static NXTRegulatedMotor a = Motor.A;
	static NXTRegulatedMotor b = Motor.B;
	static NXTRegulatedMotor c = Motor.C;
	
	static LightSensor ls;
	
	static boolean die = false;
	
	public static void main(String[] args){
		
		ls = new LightSensor(SensorPort.S1);
		
		while(!die){
			checkButtonPresses();
			LCD.clear();
			System.out.println(ls.getLightValue());
		}
	}

	private static void checkButtonPresses() {
		if(Button.LEFT.isDown()){
			if (!a.isMoving()){
				a.forward();
			} else {
				a.flt();
			}
		}
		if (Button.RIGHT.isDown()){
			if (!b.isMoving()){
				b.forward();
			} else {
				b.flt();
			}
		}
		if (Button.ENTER.isDown()){
			if (!c.isMoving()){
				c.forward();
			} else {
				c.flt();
			}
		}
		if (Button.ESCAPE.isDown()){
			die = true;
		}
	}
}