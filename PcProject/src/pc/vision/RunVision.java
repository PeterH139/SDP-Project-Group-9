package pc.vision;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pc.strategy.StrategyController;
import pc.vision.gui.VisionGUI;
import pc.vision.gui.tools.AlignmentTool;
import pc.vision.gui.tools.ColourThresholdConfigTool;
import pc.vision.gui.tools.HistogramTool;
import pc.vision.gui.tools.PitchModelView;
import pc.vision.gui.tools.StrategySelectorTool;
import pc.vision.recognisers.BallRecogniser;
import pc.vision.recognisers.RobotRecogniser;
import pc.world.DynamicWorldState;
import pc.world.Pitch;
import pc.world.oldmodel.WorldState;
import au.edu.jcu.v4l4j.V4L4JConstants;

/**
 * The main class used to run the vision system. Creates the control GUI, and
 * initialises the image processing.
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
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		final YAMLConfig yamlConfig = new YAMLConfig();
		// 0 = default to main pitch
		final PitchConstants pitchConstants = new PitchConstants(0);
		final Pitch pitch = new Pitch(yamlConfig, pitchConstants);
		WorldState worldState = new WorldState(pitch);

		DynamicWorldState dynamicWorldState = new DynamicWorldState();

		// Default values for the main vision window
		String videoDevice = "/dev/video0";
		int width = VideoStream.FRAME_WIDTH;
		int height = VideoStream.FRAME_HEIGHT;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 100;

		// Create a new Vision object to serve the main vision window
		Vision vision = new Vision(worldState, pitchConstants,
				dynamicWorldState);

		try {
			StrategyController strategyController = null;
			strategyController = new StrategyController();
			Vision.addWorldStateReceiver(strategyController);

			final VideoStream vStream = new VideoStream(videoDevice, width,
					height, channel, videoStandard, compressionQuality);

			DistortionFix distortionFix = new DistortionFix(yamlConfig,
					pitchConstants);

			// Create the Control GUI for threshold setting/etc
			VisionGUI gui = new VisionGUI(width, height, yamlConfig);

			gui.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					vStream.shutdown();
				}
			});

			ColourThresholdConfigTool ctct = new ColourThresholdConfigTool(gui,
					worldState, pitchConstants, vStream, distortionFix,
					yamlConfig);
			gui.addTool(ctct, "Settings");
			vision.addRecogniser(ctct.new PitchBoundsDebugDisplay());
			vision.addRecogniser(ctct.new DividerLineDebugDisplay());
			vision.addRecogniser(ctct.new GoalPositionDebugDisplay());

			HistogramTool histogramTool = new HistogramTool(gui, pitchConstants);
			gui.addTool(histogramTool, "Colour Thresholds");

			PitchModelView pmvTool = new PitchModelView(gui, pitchConstants,
					pitch, distortionFix, dynamicWorldState);
			gui.addTool(pmvTool, "Pitch Model");
			pmvTool.addViewProvider(new BallRecogniser.ViewProvider(
					dynamicWorldState, pitch));
			pmvTool.addViewProvider(new RobotRecogniser.ViewProvider(
					dynamicWorldState, pitch));

			vision.addRecogniser(new BallRecogniser(vision, worldState,
					pitchConstants, distortionFix, pitch));
			vision.addRecogniser(new RobotRecogniser(vision, worldState,
					pitchConstants, distortionFix, pitch));
			
			vision.addRecogniser(histogramTool);

			StrategySelectorTool stratSelect = new StrategySelectorTool(gui,
					strategyController);
			gui.addTool(stratSelect, "Robot and strategy control");
			
			AlignmentTool alignmentTool = new AlignmentTool(gui);
			gui.addTool(alignmentTool, "Alignment");
			vision.addRecogniser(alignmentTool.new FrameDisplay());

			vStream.addReceiver(pmvTool);
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
