package pc.test;

import pc.comms.BrickCommServer;
import pc.comms.BtInfo;

public class BtTest {
	
	public BtTest() {		
	}
	
	public static void main(String args[]) {	
		try {
			BrickCommServer bcs = null;
			bcs = new BrickCommServer();
			bcs.guiConnect(BtInfo.MEOW);
			
			long startTime = System.nanoTime();
			if (bcs.robotTest()) {
				long endTime = System.nanoTime();
				double duration = (double)(endTime - startTime) / 1000000.0;
				System.out.println("Bluetooth test took " + duration + " millis");
			}
			else {
				System.out.println("Bluetooth test did not finish");
			}
	    	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
