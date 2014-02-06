package pc.vision;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pc.comms.BrickCommServer;
import pc.comms.BtInfo;
import pc.strategy.InterceptorStrategy;
import pc.vision.gui.VisionGUI;
import pc.vision.gui.tools.ColourThresholdConfigTool;
import pc.vision.gui.tools.HistogramTool;
import pc.vision.recognisers.BallRecogniser;
import pc.vision.recognisers.RobotRecogniser;
import pc.world.WorldState;
import au.edu.jcu.v4l4j.V4L4JConstants;
//import strategy.calculations.GoalInfo;
//import world.state.WorldState;

/**
 * The main class used to run the vision system. Creates the control GUI, and
 * initialises the image processing.
 * 
 * @author s0840449
 */
public class RunVision {
	static Options cmdLineOptions;

	static {
		cmdLineOptions = new Options();
		cmdLineOptions.addOption("nobluetooth", false,
				"Disable Bluetooth support");
	}

	/**
	 * The main method for the class. Creates the control GUI, and initialises
	 * the image processing.
	 * 
	 * @param args
	 *            Program arguments.
	 */
	public static void main(String[] args) {
		CommandLine cmdLine;
		try {
			CommandLineParser parser = new GnuParser();
			cmdLine = parser.parse(cmdLineOptions, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return;
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Default to main pitch
		final PitchConstants pitchConstants = new PitchConstants(0);
		// GoalInfo goalInfo = new GoalInfo(pitchConstants);
		// WorldState worldState = new WorldState(goalInfo);
		WorldState worldState = new WorldState();
		
		// Default values for the main vision window
		String videoDevice = "/dev/video0";
		int width = VideoStream.FRAME_WIDTH;
		int height = VideoStream.FRAME_HEIGHT;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 100;

		final boolean enableBluetooth = !cmdLine.hasOption("nobluetooth");

		try {
			BrickCommServer bcs = null;
			if (enableBluetooth) {
				bcs = new BrickCommServer();
				bcs.guiConnect(BtInfo.group10);
			}

			final VideoStream vStream = new VideoStream(videoDevice, width,
					height, channel, videoStandard, compressionQuality);

			DistortionFix distortionFix = new DistortionFix(pitchConstants);

			// Create the Control GUI for threshold setting/etc
			VisionGUI gui = new VisionGUI(width, height);

			gui.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					vStream.shutdown();
				}
			});

			// Create a new Vision object to serve the main vision window
			Vision vision = new Vision(worldState, pitchConstants);

			ColourThresholdConfigTool ctct = new ColourThresholdConfigTool(gui,
					worldState, pitchConstants, vStream, distortionFix);
			gui.addTool(ctct, "Legacy config");
			vision.addRecogniser(ctct.new PitchBoundsDebugDisplay());
			vision.addRecogniser(ctct.new DividerLineDebugDisplay());

			HistogramTool histogramTool = new HistogramTool(gui, pitchConstants);
			gui.addTool(histogramTool, "Histogram analyser");
			vision.addRecogniser(histogramTool);

			vision.addRecogniser(new BallRecogniser(vision, worldState,
					pitchConstants));
			vision.addRecogniser(new RobotRecogniser(vision, worldState,
					pitchConstants));
			//
			// TargetFollowerStrategy tfs = new TargetFollowerStrategy(bcs);
			// tfs.startControlThread();
			//
			if (enableBluetooth) {
				InterceptorStrategy ic = new InterceptorStrategy(bcs);
				ic.startControlThread();
				vision.addWorldStateReceiver(ic);
			}

			vStream.addReceiver(distortionFix);
			vStream.addReceiver(vision);
			distortionFix.addReceiver(gui);
			vision.addVisionDebugReceiver(gui);

			gui.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
