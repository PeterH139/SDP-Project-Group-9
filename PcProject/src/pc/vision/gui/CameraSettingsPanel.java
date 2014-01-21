package pc.vision.gui;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pc.vision.VideoStream;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * A GUI panel for adjusting video device settings
 * 
 * @author Alex Adams (s1046358)
 */
@SuppressWarnings("serial")
class CameraSettingsPanel extends JPanel {
	// Values loaded when the settings file is missing
	private final static int DEFAULT_BRIGHTNESS = 128;
	private final static int DEFAULT_CONTRAST = 64;
	private final static int DEFAULT_SATURATION = 64;
	private final static int DEFAULT_HUE = 0;
	private final static int DEFAULT_CHROMA_GAIN = 0;
	private final static boolean DEFAULT_CHROMA_AGC = true;

	private final VideoStream vStream;

	private final int brightnessMin = 0;
	private final int brightnessMax = 255;
	private final JPanel brightnessPanel = new JPanel();
	private final JLabel brightnessLabel = new JLabel("Brightness:");
	private final JSlider brightnessSlider = new JSlider(brightnessMin,
			brightnessMax + 1);

	/**
	 * A ChangeListener to update the video stream's brightness setting when the
	 * brightness slider is adjusted
	 */
	private class BrightnessChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			vStream.setBrightness(Math.min(brightnessMax,
					brightnessSlider.getValue()));
			vStream.updateVideoDeviceSettings();
		}
	}

	private final int contrastMin = 0;
	private final int contrastMax = 127;
	private final JPanel contrastPanel = new JPanel();
	private final JLabel contrastLabel = new JLabel("Contrast:");
	private JSlider contrastSlider = new JSlider(contrastMin, contrastMax + 1);

	/**
	 * A ChangeListener to update the video stream's contrast setting when the
	 * contrast slider is adjusted
	 */
	private class ContrastChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			vStream.setContrast(Math.min(contrastMax, contrastSlider.getValue()));
			vStream.updateVideoDeviceSettings();
		}
	}

	private final int saturationMin = 0;
	private final int saturationMax = 127;
	private final JPanel saturationPanel = new JPanel();
	private final JLabel saturationLabel = new JLabel("Saturation:");
	private JSlider saturationSlider = new JSlider(saturationMin,
			saturationMax + 1);

	/**
	 * A ChangeListener to update the video stream's saturation setting when the
	 * saturation slider is adjusted
	 */
	private class SaturationChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			vStream.setSaturation(Math.min(saturationMax,
					saturationSlider.getValue()));
			vStream.updateVideoDeviceSettings();
		}
	}

	private final int hueMin = -128;
	private final int hueMax = 127;
	private final JPanel huePanel = new JPanel();
	private final JLabel hueLabel = new JLabel("Hue:");
	private JSlider hueSlider = new JSlider(hueMin, hueMax + 1);

	/**
	 * A ChangeListener to update the video stream's hue setting when the hue
	 * slider is adjusted
	 */
	private class HueChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			vStream.setHue(Math.min(hueMax, hueSlider.getValue()));
			vStream.updateVideoDeviceSettings();
		}
	}

	private final int chromaGainMin = 0;
	private final int chromaGainMax = 127;
	private final JPanel chromaGainPanel = new JPanel();
	private final JLabel chromaGainLabel = new JLabel("Chroma Gain:");
	private final JSlider chromaGainSlider = new JSlider(chromaGainMin,
			chromaGainMax + 1);

	/**
	 * A ChangeListener to update the video stream's chroma gain setting when
	 * the chroma gain slider is adjusted
	 */
	private class ChromaGainChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			vStream.setChromaGain(Math.min(chromaGainMax,
					chromaGainSlider.getValue()));
			vStream.updateVideoDeviceSettings();
		}
	}

	private final JPanel chromaAGCPanel = new JPanel();
	private final JCheckBox chromaAGCCheckBox = new JCheckBox("Chroma AGC");

	/**
	 * An ActionListener to update the video stream's chroma AGC setting when
	 * the chroma AGC checkbox is activated either by mouse or keyboard
	 */
	private class ChromaAGCActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (chromaAGCCheckBox.isSelected())
				vStream.setChromaAGC(true);
			else
				vStream.setChromaAGC(false);

			vStream.updateVideoDeviceSettings();
		}
	}

	public CameraSettingsPanel(final VideoStream vStream, String settingsFile) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.vStream = vStream;

		initialiseSlider(brightnessSlider, 16, 64);
		brightnessSlider.addChangeListener(new BrightnessChangeListener());
		brightnessPanel.add(brightnessLabel);
		brightnessPanel.add(brightnessSlider);
		this.add(brightnessPanel);

		initialiseSlider(contrastSlider, 8, 32);
		contrastSlider.addChangeListener(new ContrastChangeListener());
		contrastPanel.add(contrastLabel);
		contrastPanel.add(contrastSlider);
		this.add(contrastPanel);

		initialiseSlider(saturationSlider, 8, 32);
		saturationSlider.addChangeListener(new SaturationChangeListener());
		saturationPanel.add(saturationLabel);
		saturationPanel.add(saturationSlider);
		this.add(saturationPanel);

		initialiseSlider(hueSlider, 16, 64);
		hueSlider.addChangeListener(new HueChangeListener());
		huePanel.add(hueLabel);
		huePanel.add(hueSlider);
		this.add(huePanel);

		initialiseSlider(chromaGainSlider, 8, 32);
		chromaGainSlider.addChangeListener(new ChromaGainChangeListener());
		chromaGainPanel.add(chromaGainLabel);
		chromaGainPanel.add(chromaGainSlider);
		this.add(chromaGainPanel);

		chromaAGCCheckBox.addActionListener(new ChromaAGCActionListener());
		chromaAGCPanel.add(chromaAGCCheckBox);
		this.add(chromaAGCPanel);

		loadSettings(settingsFile);
	}

	/**
	 * Sets up initial settings for one of the sliders
	 * 
	 * @param slider
	 *            The slider to set up
	 * @param minorTick
	 *            The value difference between the smaller ticks on the slider
	 * @param majorTick
	 *            The value difference between the larger ticks on the slider
	 */
	private static void initialiseSlider(JSlider slider, int minorTick,
			int majorTick) {
		slider.setOrientation(JSlider.HORIZONTAL);
		slider.setMinorTickSpacing(minorTick);
		slider.setMajorTickSpacing(majorTick);

		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
	}

	/**
	 * Saves the video device settings to a file in the specified location
	 * 
	 * @param fileName
	 *            where to save the file
	 */
	public void saveSettings(String fileName) {
		try {
			FileWriter file = new FileWriter(new File(fileName));
			file.write(String.valueOf(vStream.getBrightness()) + "\n");
			file.write(String.valueOf(vStream.getContrast()) + "\n");
			file.write(String.valueOf(vStream.getSaturation()) + "\n");
			file.write(String.valueOf(vStream.getHue()) + "\n");
			file.write(String.valueOf(vStream.getChromaGain()) + "\n");
			file.write(String.valueOf(vStream.getChromaAGC()) + "\n");
			file.close();
		} catch (IOException e) {
			System.err
					.println("Error writing camera settings file " + fileName);
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Loads video device settings from the specified file and updates the GUI
	 * and VideoStream. It is assumed the file is well formed
	 * 
	 * @param fileName
	 */
	public void loadSettings(String fileName) {
		Scanner reader;
		try {
			reader = new Scanner(new File(fileName));
			assert (reader != null);
			int data = 0;

			data = reader.nextInt();
			brightnessSlider.setValue(data);
			vStream.setBrightness(data);

			data = reader.nextInt();
			contrastSlider.setValue(data);
			vStream.setContrast(data);

			data = reader.nextInt();
			saturationSlider.setValue(data);
			vStream.setSaturation(data);

			data = reader.nextInt();
			hueSlider.setValue(data);
			vStream.setHue(data);

			data = reader.nextInt();
			chromaGainSlider.setValue(data);
			vStream.setChromaGain(data);

			boolean chromaAGC = reader.nextBoolean();
			chromaAGCCheckBox.setSelected(chromaAGC);
			vStream.setChromaAGC(chromaAGC);
			
			reader.close();
		} catch (Exception e) {
			System.err.println("Cannot load camera settings file " + fileName);
			System.err.println(e.getMessage());
			loadDefaultSettings();
			return;
		}

		vStream.updateVideoDeviceSettings();
	}

	/**
	 * Loads default video device settings in the event loadSettings fails
	 */
	private void loadDefaultSettings() {
		brightnessSlider.setValue(DEFAULT_BRIGHTNESS);
		vStream.setBrightness(DEFAULT_BRIGHTNESS);

		contrastSlider.setValue(DEFAULT_CONTRAST);
		vStream.setContrast(DEFAULT_CONTRAST);

		saturationSlider.setValue(DEFAULT_SATURATION);
		vStream.setSaturation(DEFAULT_SATURATION);

		hueSlider.setValue(DEFAULT_HUE);
		vStream.setHue(DEFAULT_HUE);

		chromaGainSlider.setValue(DEFAULT_CHROMA_GAIN);
		vStream.setChromaGain(DEFAULT_CHROMA_GAIN);

		chromaAGCCheckBox.setSelected(DEFAULT_CHROMA_AGC);
		vStream.setChromaAGC(DEFAULT_CHROMA_AGC);

		vStream.updateVideoDeviceSettings();
	}
}
