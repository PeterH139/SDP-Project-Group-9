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
import pc.strategy.TargetFollowerStrategy;
import pc.vision.gui.VisionGUI;
import pc.vision.gui.tools.ColourThresholdConfigTool;
import pc.vision.gui.tools.HistogramTool;
import pc.vision.gui.tools.PitchModelView;
import pc.vision.gui.tools.StrategySelectorTool;
import pc.vision.recognisers.BallRecogniser;
import pc.vision.recognisers.RobotRecogniser;
import pc.world.Pitch;
import pc.world.WorldState;
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

		final YAMLConfig yamlConfig = new YAMLConfig();
		// 0 = default to main pitch
		final PitchConstants pitchConstants = new PitchConstants(0);
		final Pitch pitch = new Pitch(yamlConfig);
		WorldState worldState = new WorldState(pitch);

		// Default values for the main vision window
		String videoDevice = "/dev/video0";
		int width = VideoStream.FRAME_WIDTH;
		int height = VideoStream.FRAME_HEIGHT;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 100;

		final boolean enableBluetooth = !cmdLine.hasOption("nobluetooth");

		// Create a new Vision object to serve the main vision window
		Vision vision = new Vision(worldState, pitchConstants);

		try {
			StrategyController strategyController = null;
			if (enableBluetooth) {
				strategyController = new StrategyController(vision);
				// Vision.addWorldStateReceiver(strategyController);
			}

			final VideoStream vStream = new VideoStream(videoDevice, width,
					height, channel, videoStandard, compressionQuality);

			DistortionFix distortionFix = new DistortionFix(pitchConstants);

			// Create the Control GUI for threshold setting/etc
			VisionGUI gui = new VisionGUI(width, height, yamlConfig);

			gui.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					vStream.shutdown();
				}
			});

			ColourThresholdConfigTool ctct = new ColourThresholdConfigTool(gui,
					worldState, pitchConstants, vStream, distortionFix);
			gui.addTool(ctct, "Settings");
			vision.addRecogniser(ctct.new PitchBoundsDebugDisplay());
			vision.addRecogniser(ctct.new DividerLineDebugDisplay());
			vision.addRecogniser(ctct.new GoalPositionDebugDisplay());

			HistogramTool histogramTool = new HistogramTool(gui, pitchConstants);
			gui.addTool(histogramTool, "Colour Thresholds");
			vision.addRecogniser(histogramTool);

			PitchModelView pmvTool = new PitchModelView(gui, pitchConstants,
					pitch);
			gui.addTool(pmvTool, "Pitch Model");
			Vision.addWorldStateReceiver(pmvTool);

			StrategySelectorTool sst = new StrategySelectorTool(gui, strategyController);
			gui.addTool(sst, "Strategy Selector");
			
			vision.addRecogniser(new BallRecogniser(vision, worldState,
					pitchConstants));
			vision.addRecogniser(new RobotRecogniser(vision, worldState,
					pitchConstants));

			if (enableBluetooth) {
				StrategySelectorTool stratSelect = new StrategySelectorTool(
						gui, strategyController);
				gui.addTool(stratSelect, "Strategy Selector");

				strategyController
						.changeToStrategy(StrategyController.StrategyType.DEFENDING);
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
