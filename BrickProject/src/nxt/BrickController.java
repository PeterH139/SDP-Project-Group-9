package nxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.robotics.navigation.DifferentialPilot;

public class BrickController {
	
	private static boolean die = false;
	static final int TYRE_DIAMETER = 56;
	static final int TRACK_WIDTH = 116;
	static final int TRAVEL_SPEED = 90;
	static NXTRegulatedMotor leftMotor = Motor.B;
	static NXTRegulatedMotor rightMotor = Motor.A;
	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, leftMotor, rightMotor);
	
	private final static int DO_NOTHING = 0;
    private final static int FORWARDS = 1;
    private final static int QUIT = 5;
    private final static int FORCEQUIT = 55;
	
	private static InputStream is;
	private static OutputStream os;

	public static void main(String[] args) {
		
	while(!die) {
		try {
			
		
			NXTConnection connection = initializeBluetooth();
			
			int opcode = DO_NOTHING;
	        int option1, option2, option3;
	        
	        while ((opcode != QUIT) && (opcode != FORCEQUIT) && !(Button.ESCAPE.isDown())) {

                // Get the next command from the inputstream
                byte[] byteBuffer = new byte[4];
                is.read(byteBuffer);
                
             // We send 4 different numbers, use as options
                opcode = byteBuffer[0];
                option1 = byteBuffer[1];
                option2 = byteBuffer[2];
                option3 = byteBuffer[3];
                

                if (opcode > 0)
                        LCD.drawString("opcode = " + opcode, 0, 2);
                
                switch (opcode) {

                case FORWARDS:
                        LCD.clear();
                        LCD.drawString("Forward!", 0, 2);
                        LCD.refresh();
                        pilot.forward();
                        replytopc(opcode, os);
                        break;

                case QUIT: // Exit the loop, close connection
                        // Sound.twoBeeps();
                        break;

                case FORCEQUIT:
                        // Quit the brick. otherwise it'd loop back to
                        // waiting for connection.
                        die = true;
                        break;
                default:
                }
	        }
	        is.close();
            os.close();
            Thread.sleep(100); // Waiting for data to drain
            LCD.clear();
            LCD.drawString("Closing", 0, 2);
            LCD.refresh();
            Sound.playTone(1500, 400, 100);
            LCD.clear();
            Thread.sleep(200);
			
		} catch (Exception e) {
            LCD.drawString("Exception:", 0, 2);
            String msg = e.getMessage();
            if (msg != null)
                    LCD.drawString(msg, 2, 3);
            else
                    LCD.drawString("Error message is null", 2, 3);
    }
		}
	}
	
	private static NXTConnection initializeBluetooth() throws IOException {
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
        return connection;
        
	}
	
	 public static void replytopc(int opcode, OutputStream os) throws IOException {
         byte[] reply = { 111, (byte) opcode, 0, 0 };
         os.write(reply);
         os.flush();
 }
	

	

}
