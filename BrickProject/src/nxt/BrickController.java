package nxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

public class BrickController {
	
	private static boolean die = false;
	
	private static InputStream is;
	private static OutputStream os;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		try {
			initializeBluetooth();
			//mainLoop();
		} catch (IOException e) {
			LCD.clear();
			LCD.drawString(e.getMessage(), 0, 2);
			e.printStackTrace();
		}
	}
	
	private static void initializeBluetooth() throws IOException {
		LCD.clear();
        LCD.drawString("Waiting for", 0, 2);
        LCD.drawString("Bluetooth...", 0, 3);
        NXTConnection connection = Bluetooth.waitForConnection();
        is = connection.openInputStream();
        os = connection.openOutputStream();
        LCD.clear();
        LCD.drawString("Connected!", 0, 2);
        byte[] robotready = { 0, 0, 0, 0 };
        if (os == null)
                throw new IOException("Output stream is null!");
        os.write(robotready);
        os.flush();
        Sound.playTone(1000, 200, 100);
	}
	
	private static void mainLoop() {
		while(!die){
			
		}
	}

	

}
