package pc.comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class BluetoothConnection {

	private boolean connected = false;
	private boolean robotReady = false;
	
	private static final byte[] ROBOT_READY_CODE = {0,0,0,0};
	
	private InputStream is;
    private OutputStream os;
    
    private NXTComm nxtComm;
    private NXTInfo nxtInfo;
	
    /**
     * After creation use .open() to open the connection to the robot.
     * Device names and MAC addresses can be found and changed in {@link BtInfo}.
     * 
     * @param deviceName
     *            The name of the Bluetooth device
     * @param deviceMacAddress
     *            The MAC address of the Bluetooth device
     */
    public BluetoothConnection(String deviceName, String deviceMacAddress){
    	this.nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH,deviceName,deviceMacAddress);
    	
    	Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                    if (BluetoothConnection.this.connected) {
                            close();
                    }
            }
    	});
    }

	public void open() throws IOException {
        try {
                this.nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
        } catch (NXTCommException e) {
                System.err.println("Could not create connection: " + e.toString());
        }

        System.out.println("Attempting to connect to robot...");

        try {
                this.nxtComm.open(this.nxtInfo);
                this.is = this.nxtComm.getInputStream();
                this.os = this.nxtComm.getOutputStream();

                while (true) {
                        int[] res = receiveFromRobot();
                        boolean equals = true;
                        for (int i = 0; i < 4; i++) { // wait for ready signal
                                if (res[i] != ROBOT_READY_CODE[i]) {
                                        equals = false;
                                        break;
                                }
                        }
                        if (equals) {
                                break;
                        } else {
                                Thread.sleep(10); // Prevent 100% cpuusage
                        }
                }
                System.out.println("Connected to robot!");
                this.robotReady = true;
                this.connected = true;
        } catch (NXTCommException e) {
                throw new IOException("Failed to connect: " + e.toString());
        } catch (InterruptedException e) {
                throw new IOException("Failed to connect: " + e.toString());
        }
    }
		
    public void close() {
        try {
                this.connected = false;
                this.is.close();
                this.os.close();
                this.nxtComm.close();
        } catch (IOException e) {
                System.err.println("Couldn't close Bluetooth connection: "
                                + e.toString());
        }
    }
    
    public int[] receiveFromRobot() throws IOException {
        byte[] res = new byte[4];
        this.is.read(res);
        int[] ret = { (int) (res[0]), (int) (res[1]), (int) (res[2]),
                (int) (res[3]) };
        return ret;
    }
    public void sendToRobotSimple(int[] comm) throws IOException {
        if (!this.connected)
                return;
        byte[] command = { (byte) comm[0], (byte) comm[1], (byte) comm[2],
                        (byte) comm[3] };

        this.os.write(command);
        this.os.flush();
}
    
    public static void main(String[] args) throws IOException{
    	BufferedReader stdin = new BufferedReader( 
                new InputStreamReader(System.in)); 
    	System.out.print("Enter first integer: "); 
        int[] input = new int[4];
        int x = 0;
        int y = 0;
    	BluetoothConnection con = new BluetoothConnection(
    			BtInfo.DEVICE_1_NAME, BtInfo.DEVICE_1_MAC);
    	try {
			con.open(); 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	boolean die = false;
		while(!die) {
		System.out.println("Enter an opcode: ");
		x = Integer.parseInt(stdin.readLine());
		System.out.println("Enter an option1: ");
		y = Integer.parseInt(stdin.readLine());
		input[0] = x;
		input[1] = y;
		input[2] = 0;
		input[3] = 0;
    	con.sendToRobotSimple(input); 
    	int [] receive_info = new int [4];
    	try {
             receive_info = con.receiveFromRobot();
     } catch (IOException e) {
             e.printStackTrace();
     }
    	System.out.println("Received from robot: " + receive_info[0] + " " + receive_info[1] + " " + receive_info[2] + " " + receive_info[3]);
    	if (x == 5) {
    		die = true;}
		}

    	
    }
    
    public boolean isMoving() {
        // TODO Auto-generated method stub
        return false;
}
}
