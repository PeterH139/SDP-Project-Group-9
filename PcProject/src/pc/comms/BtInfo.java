package pc.comms;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

/**
 * Holds the connection info for both robots as public constants.
 * {@code DEVICE_1_NAME, DEVICE_2_NAME, DEVICE_1_MAC, DEVICE_2_MAC}
 */

public class BtInfo {

	public static final String DEVICE_1_NAME = "MEOW";
	public static final String DEVICE_2_NAME = "group10";

	public static final String DEVICE_1_MAC = "0016530A284F";
	public static final String DEVICE_2_MAC = "";

	public static final NXTInfo MEOW = new NXTInfo(NXTCommFactory.BLUETOOTH,
			"MEOW", "0016530A284F");
	public static final NXTInfo group10 = new NXTInfo(NXTCommFactory.BLUETOOTH,
			"group10", "001653077601");

}
