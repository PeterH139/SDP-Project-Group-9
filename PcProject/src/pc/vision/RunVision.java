package pc.vision;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.UIManager;

import pc.comms.BrickCommServer;
import pc.comms.BtInfo;
import pc.strategy.InterceptorStrategy;
import pc.strategy.TargetFollowerStrategy;
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
	/**
	 * The main method for the class. Creates the control GUI, and initialises
	 * the image processing.
	 * 
	 * @param args
	 *            Program arguments. Not used.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Default to main pitch
		PitchConstants pitchConstants = new PitchConstants(0);
		// GoalInfo goalInfo = new GoalInfo(pitchConstants);
		// WorldState worldState = new WorldState(goalInfo);
		WorldState worldState = new WorldState();

		// Default values for the main vision window
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 100;

		try {
			BrickCommServer bcs = new BrickCommServer();
			bcs.guiConnect(BtInfo.group10);

			final VideoStream vStream = new VideoStream(videoDevice, width, height,
					channel, videoStandard, compressionQuality);

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

			ColourThresholdConfigTool ctct = new ColourThresholdConfigTool(gui, worldState,
					pitchConstants, vStream, distortionFix);
			gui.addTool(ctct, "Legacy config");
			vision.addRecogniser(ctct.new PitchBoundsDebugDisplay());
			vision.addRecogniser(ctct.new DividerLineDebugDisplay());
			
			HistogramTool histogramTool = new HistogramTool(gui);
			gui.addTool(histogramTool, "Histogram analyser");
			vision.addRecogniser(histogramTool);

			vision.addRecogniser(new BallRecogniser(vision, worldState,
					pitchConstants));
			vision.addRecogniser(new RobotRecogniser(vision, worldState,
					pitchConstants));
//			
//			TargetFollowerStrategy tfs = new TargetFollowerStrategy(bcs);
//			tfs.startControlThread();
//
			 InterceptorStrategy ic = new InterceptorStrategy(bcs);
			 ic.startControlThread();

			vStream.addReceiver(distortionFix);
			vStream.addReceiver(vision);
			distortionFix.addReceiver(gui);
			vision.addVisionDebugReceiver(gui);
			 vision.addWorldStateReceiver(ic);

			gui.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
