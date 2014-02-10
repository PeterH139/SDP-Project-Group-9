package pc.test;

import java.io.IOException;

import pc.comms.BluetoothConnection;
import pc.comms.BrickCommServer;
import pc.comms.BtInfo;
import pc.strategy.InterceptorStrategy;

public class BtTest {
	
	public BtTest() {		
	}
	
	public static void main(String args[]) {
		BluetoothConnection con = new BluetoothConnection(
				BtInfo.DEVICE_1_NAME, BtInfo.DEVICE_1_MAC);		
		
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
