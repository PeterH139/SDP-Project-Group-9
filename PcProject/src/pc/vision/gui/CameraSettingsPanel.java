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
	private final JSlider brightnessSlider = new JSlider(this.brightnessMin,
			this.brightnessMax + 1);

	/**
	 * A ChangeListener to update the video stream's brightness setting when the
	 * brightness slider is adjusted
	 */
	private class BrightnessChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			CameraSettingsPanel.this.vStream.setBrightness(Math.min(CameraSettingsPanel.this.brightnessMax,
					CameraSettingsPanel.this.brightnessSlider.getValue()));
			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
		}
	}

	private final int contrastMin = 0;
	private final int contrastMax = 127;
	private final JPanel contrastPanel = new JPanel();
	private final JLabel contrastLabel = new JLabel("Contrast:");
	private JSlider contrastSlider = new JSlider(this.contrastMin, this.contrastMax + 1);

	/**
	 * A ChangeListener to update the video stream's contrast setting when the
	 * contrast slider is adjusted
	 */
	private class ContrastChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			CameraSettingsPanel.this.vStream.setContrast(Math.min(CameraSettingsPanel.this.contrastMax, CameraSettingsPanel.this.contrastSlider.getValue()));
			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
		}
	}

	private final int saturationMin = 0;
	private final int saturationMax = 127;
	private final JPanel saturationPanel = new JPanel();
	private final JLabel saturationLabel = new JLabel("Saturation:");
	private JSlider saturationSlider = new JSlider(this.saturationMin,
			this.saturationMax + 1);

	/**
	 * A ChangeListener to update the video stream's saturation setting when the
	 * saturation slider is adjusted
	 */
	private class SaturationChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			CameraSettingsPanel.this.vStream.setSaturation(Math.min(CameraSettingsPanel.this.saturationMax,
					CameraSettingsPanel.this.saturationSlider.getValue()));
			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
		}
	}

	private final int hueMin = -128;
	private final int hueMax = 127;
	private final JPanel huePanel = new JPanel();
	private final JLabel hueLabel = new JLabel("Hue:");
	private JSlider hueSlider = new JSlider(this.hueMin, this.hueMax + 1);

	/**
	 * A ChangeListener to update the video stream's hue setting when the hue
	 * slider is adjusted
	 */
	private class HueChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			CameraSettingsPanel.this.vStream.setHue(Math.min(CameraSettingsPanel.this.hueMax, CameraSettingsPanel.this.hueSlider.getValue()));
			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
		}
	}

	private final int chromaGainMin = 0;
	private final int chromaGainMax = 127;
	private final JPanel chromaGainPanel = new JPanel();
	private final JLabel chromaGainLabel = new JLabel("Chroma Gain:");
	private final JSlider chromaGainSlider = new JSlider(this.chromaGainMin,
			this.chromaGainMax + 1);

	/**
	 * A ChangeListener to update the video stream's chroma gain setting when
	 * the chroma gain slider is adjusted
	 */
	private class ChromaGainChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			CameraSettingsPanel.this.vStream.setChromaGain(Math.min(CameraSettingsPanel.this.chromaGainMax,
					CameraSettingsPanel.this.chromaGainSlider.getValue()));
			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
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
			if (CameraSettingsPanel.this.chromaAGCCheckBox.isSelected())
				CameraSettingsPanel.this.vStream.setChromaAGC(true);
			else
				CameraSettingsPanel.this.vStream.setChromaAGC(false);

			CameraSettingsPanel.this.vStream.updateVideoDeviceSettings();
		}
	}

	public CameraSettingsPanel(final VideoStream vStream, String settingsFile) {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.vStream = vStream;

		initialiseSlider(this.brightnessSlider, 16, 64);
		this.brightnessSlider.addChangeListener(new BrightnessChangeListener());
		this.brightnessPanel.add(this.brightnessLabel);
		this.brightnessPanel.add(this.brightnessSlider);
		this.add(this.brightnessPanel);

		initialiseSlider(this.contrastSlider, 8, 32);
		this.contrastSlider.addChangeListener(new ContrastChangeListener());
		this.contrastPanel.add(this.contrastLabel);
		this.contrastPanel.add(this.contrastSlider);
		this.add(this.contrastPanel);

		initialiseSlider(this.saturationSlider, 8, 32);
		this.saturationSlider.addChangeListener(new SaturationChangeListener());
		this.saturationPanel.add(this.saturationLabel);
		this.saturationPanel.add(this.saturationSlider);
		this.add(this.saturationPanel);

		initialiseSlider(this.hueSlider, 16, 64);
		this.hueSlider.addChangeListener(new HueChangeListener());
		this.huePanel.add(this.hueLabel);
		this.huePanel.add(this.hueSlider);
		this.add(this.huePanel);

		initialiseSlider(this.chromaGainSlider, 8, 32);
		this.chromaGainSlider.addChangeListener(new ChromaGainChangeListener());
		this.chromaGainPanel.add(this.chromaGainLabel);
		this.chromaGainPanel.add(this.chromaGainSlider);
		this.add(this.chromaGainPanel);

		this.chromaAGCCheckBox.addActionListener(new ChromaAGCActionListener());
		this.chromaAGCPanel.add(this.chromaAGCCheckBox);
		this.add(this.chromaAGCPanel);

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
			file.write(String.valueOf(this.vStream.getBrightness()) + "\n");
			file.write(String.valueOf(this.vStream.getContrast()) + "\n");
			file.write(String.valueOf(this.vStream.getSaturation()) + "\n");
			file.write(String.valueOf(this.vStream.getHue()) + "\n");
			file.write(String.valueOf(this.vStream.getChromaGain()) + "\n");
			file.write(String.valueOf(this.vStream.getChromaAGC()) + "\n");
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
			this.brightnessSlider.setValue(data);
			this.vStream.setBrightness(data);

			data = reader.nextInt();
			this.contrastSlider.setValue(data);
			this.vStream.setContrast(data);

			data = reader.nextInt();
			this.saturationSlider.setValue(data);
			this.vStream.setSaturation(data);

			data = reader.nextInt();
			this.hueSlider.setValue(data);
			this.vStream.setHue(data);

			data = reader.nextInt();
			this.chromaGainSlider.setValue(data);
			this.vStream.setChromaGain(data);

			boolean chromaAGC = reader.nextBoolean();
			this.chromaAGCCheckBox.setSelected(chromaAGC);
			this.vStream.setChromaAGC(chromaAGC);
			
			reader.close();
		} catch (Exception e) {
			System.err.println("Cannot load camera settings file " + fileName);
			System.err.println(e.getMessage());
			loadDefaultSettings();
			return;
		}

		this.vStream.updateVideoDeviceSettings();
	}

	/**
	 * Loads default video device settings in the event loadSettings fails
	 */
	private void loadDefaultSettings() {
		this.brightnessSlider.setValue(DEFAULT_BRIGHTNESS);
		this.vStream.setBrightness(DEFAULT_BRIGHTNESS);

		this.contrastSlider.setValue(DEFAULT_CONTRAST);
		this.vStream.setContrast(DEFAULT_CONTRAST);

		this.saturationSlider.setValue(DEFAULT_SATURATION);
		this.vStream.setSaturation(DEFAULT_SATURATION);

		this.hueSlider.setValue(DEFAULT_HUE);
		this.vStream.setHue(DEFAULT_HUE);

		this.chromaGainSlider.setValue(DEFAULT_CHROMA_GAIN);
		this.vStream.setChromaGain(DEFAULT_CHROMA_GAIN);

		this.chromaAGCCheckBox.setSelected(DEFAULT_CHROMA_AGC);
		this.vStream.setChromaAGC(DEFAULT_CHROMA_AGC);

		this.vStream.updateVideoDeviceSettings();
	}
}
