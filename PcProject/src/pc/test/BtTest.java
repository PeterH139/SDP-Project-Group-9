/* Results: 
 * 
 * When sending one command to group10 followed by one command to MEOW:
 * Average of 60 milliseconds to 1) send a command with or without a 
 * parameter to read, probably no difference on using int vs. double or sending
 * two parameters instead of one. 2) receive a boolean back.
 * Mostly varies between 42 and 80, will get better idea of how, soon.
 * 
 * When sending commands to just one robot:
 * Average of 50 milliseconds. 
 */

package pc.test;

import java.util.Random;

import lejos.pc.comm.NXTInfo;
import pc.comms.BrickCommServer;
import pc.comms.BrickControlGUI;
import pc.comms.BtInfo;

public class BtTest {
	
	public BtTest() {		
	}
	
	//variable type to be sent to the robot
	public enum SendInfoType {
		NONE, INT, DOUBLE, INTANDDOUBLE
	}
	
	static double sendCommand(BrickCommServer bcs, SendInfoType info) {
		double duration = -1; //tests haven't completed
		
		try {
			
			long startTime = System.nanoTime();
			
			Boolean result = false;
			switch(info) {
				case INT: result = bcs.robotTestINT(1); break;
				case DOUBLE: result = bcs.robotTestDOUBLE(1.0); break;
				case INTANDDOUBLE: result = bcs.robotTestINTANDDOUBLE(1, 1.0); break;
				default: result = bcs.robotTest(); break;
			}
			
			if (result) {
				long endTime = System.nanoTime();
				duration = (double)(endTime - startTime) / 1000000.0;
			}
	    	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return duration;
	}
	
	static BrickCommServer connect(NXTInfo brickInfo) {
		BrickCommServer bcs = null;
		bcs = new BrickCommServer();
		try {
			BrickControlGUI.guiConnect(bcs, brickInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bcs;
	}
	
	static void testRobot(NXTInfo brickInfo, SendInfoType info) {
		BrickCommServer bcs = connect(brickInfo);
		double duration = sendCommand(bcs, info);
		
		if(duration != -1) {
			System.out.println("Bluetooth test took " + duration + " millis on robot " + brickInfo.name);
			System.out.println("Sending info type: " + info);
		}
		else {
			System.out.println("Bluetooth test did not finish");
		}
		
	}
	
	static void testBoth(SendInfoType infoGroup10, SendInfoType infoMEOW) {
		try {
			BrickCommServer bcsGroup10 = connect(BtInfo.group10);		
			BrickCommServer bcsMEOW = connect(BtInfo.MEOW);
			
			double durationGroup10 = sendCommand(bcsGroup10, infoGroup10);
			double durationMEOW = sendCommand(bcsMEOW, infoMEOW);
			double duration = durationGroup10 + durationMEOW;
			
			if (durationGroup10 != -1 && durationGroup10 != -1) {
				System.out.println("Bluetooth test took " + duration + " millis");
				System.out.println("Sending info type: " + infoGroup10 + "to group10, " + 
														   infoMEOW + "to MEOW");
			}
			else {
				System.out.println("Bluetooth test did not finish");
			}
	    	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	static SendInfoType randInfoType() { //returns a random type of info to be sent to robot
		SendInfoType infoType;
		
		Random rand = new Random();
		int n = rand.nextInt(4);
		switch (n) {
		case 1: infoType = SendInfoType.INT; break;
		case 2: infoType = SendInfoType.DOUBLE; break;
		case 3: infoType = SendInfoType.INTANDDOUBLE; break;
		default: infoType = SendInfoType.NONE; break;
		}
		
		return infoType;
	}
	
	//sends one command with random things to read to group10 followed by one to MEOW, *tests* times
	static void testMultiple(int tests) {
		try {
			BrickCommServer bcsGroup10 = connect(BtInfo.group10);		
			BrickCommServer bcsMEOW = connect(BtInfo.MEOW);
			
			SendInfoType randTypesGroup10[] = new SendInfoType[tests];
			SendInfoType randTypesMEOW[] = new SendInfoType[tests];
			double durationsGroup10[] = new double[tests];
			double durationsMEOW[] = new double[tests];
			double duration = 0;
			int testsFailed = 0;
			
			for (int i=0; i<tests; i++) {
				randTypesGroup10[i] = randInfoType();
				randTypesMEOW[i] = randInfoType();
				
				durationsGroup10[i] = sendCommand(bcsGroup10, randTypesGroup10[i]);
				durationsMEOW[i] = sendCommand(bcsMEOW, randTypesMEOW[i]);
				
				if (durationsGroup10[i] != -1 && durationsMEOW[i] != -1) {
					duration += durationsGroup10[i] + durationsMEOW[i];
				}
				else {
					testsFailed++;
				}
			}
			
			if (testsFailed == 0) {
				System.out.println("Bluetooth test took " + duration + " millis on " + tests + "tests");
			}
			else {
				System.out.println(testsFailed + "bluetooth tests did not finish, the rest took "+ duration + "millis");
			}
			
			double durationSum=0;
			double ints=0, doubles=0, intsanddoubles=0, none=0;
			int intsn=0, doublesn=0, intsanddoublesn=0, nonen=0;
			SendInfoType type;
			double durr;
			
			System.out.println("group10:");
			for (int i=0; i<tests; i++) {
				type = randTypesGroup10[i];
				durr = durationsGroup10[i];
				System.out.println(type + " " + durr);
				
				durationSum+=durationsGroup10[i];
				switch (type) {
				case INT: ints += durr; intsn+=1; break;
				case DOUBLE: doubles += durr; doublesn+=1; break;
				case INTANDDOUBLE: intsanddoubles += durr; intsanddoublesn+=1; break;
				case NONE: none += durr; nonen+=1; break;
				}

			}
			System.out.println(durationSum/tests);
			System.out.println("INT: " + ints/intsn);
			System.out.println("DOUBLE: " + doubles/doublesn);
			System.out.println("INTANDDOUBLE: " + intsanddoubles/intsanddoublesn);
			System.out.println("NONE: " + none/nonen);
			
			
			System.out.println("MEOW:");
			durationSum=0;
			ints=0; doubles=0; intsanddoubles=0; none=0;
			intsn=0; doublesn=0; intsanddoublesn=0; nonen=0;
			for (int i=0; i<tests; i++) {
				type = randTypesGroup10[i];
				durr = durationsGroup10[i];
				System.out.println(type + " " + durr);
				
				durationSum+=durationsMEOW[i];
				switch (type) {
				case INT: ints += durr; intsn+=1; break;
				case DOUBLE: doubles += durr; doublesn+=1; break;
				case INTANDDOUBLE: intsanddoubles += durr; intsanddoublesn+=1; break;
				case NONE: none += durr; nonen+=1; break;
				}
			}
			System.out.println(durationSum/tests);
			System.out.println("INT: " + ints/intsn);
			System.out.println("DOUBLE: " + doubles/doublesn);
			System.out.println("INTANDDOUBLE: " + intsanddoubles/intsanddoublesn);
			System.out.println("NONE: " + none/nonen);
			
	    	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		
		//testRobot(BtInfo.group10, SendInfoType.INT);
		//testRobot(BtInfo.MEOW, SendInfoType.INTANDDOUBLE);
		//testBoth();
		testMultiple(100);
		
		
	}
	
}
