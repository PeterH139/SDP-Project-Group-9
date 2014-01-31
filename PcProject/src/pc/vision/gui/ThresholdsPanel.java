package pc.vision.gui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import pc.vision.PitchConstants;

/**
 * A UI container for holding the contents of each of the threshold tabs
 * 
 * @author Alex Adams (s1046358)
 */
@SuppressWarnings("serial")
class ThresholdsPanel extends JPanel {
	private static final int SLIDER_MIN = 0;
	private static final int SLIDER_MAX = 256;

	private final int redMin = SLIDER_MIN;
	private final int redMax = SLIDER_MAX;
	private final JPanel redPanel = new JPanel();
	private final JLabel redLabel = new JLabel("Red:");
	private InvertibleRangeSlider redSlider;

	private final int greenMin = SLIDER_MIN;
	private final int greenMax = SLIDER_MAX;
	private final JPanel greenPanel = new JPanel();
	private final JLabel greenLabel = new JLabel("Green:");
	private InvertibleRangeSlider greenSlider;

	private final int blueMin = SLIDER_MIN;
	private final int blueMax = SLIDER_MAX;
	private final JPanel bluePanel = new JPanel();
	private final JLabel blueLabel = new JLabel("Blue:");
	private InvertibleRangeSlider blueSlider;

	private final int hueMin = SLIDER_MIN;
	private final int hueMax = SLIDER_MAX;
	private final JPanel huePanel = new JPanel();
	private final JLabel hueLabel = new JLabel("Hue:");
	private InvertibleRangeSlider hueSlider;

	private final int saturationMin = SLIDER_MIN;
	private final int saturationMax = SLIDER_MAX;
	private final JPanel saturationPanel = new JPanel();
	private final JLabel saturationLabel = new JLabel("Sat:");
	private InvertibleRangeSlider saturationSlider;

	private final int valueMin = SLIDER_MIN;
	private final int valueMax = SLIDER_MAX;
	private final JPanel valuePanel = new JPanel();
	private final JLabel valueLabel = new JLabel("Value:");
	private InvertibleRangeSlider valueSlider;

	/**
	 * Constructs a ThresholdsPanel with the default setting of all minimums to
	 * 0, all maximums to 255.
	 * 
	 * TODO: Possibly add functionality for different settings. (Currently not
	 * needed)
	 */
	public ThresholdsPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.redSlider = new InvertibleRangeSlider(this.redMin, this.redMax + 1);
		this.redPanel.add(this.redLabel);
		this.redPanel.add(this.redSlider);
		this.add(this.redPanel);

		this.greenSlider = new InvertibleRangeSlider(this.greenMin, this.greenMax + 1);
		this.greenPanel.add(this.greenLabel);
		this.greenPanel.add(this.greenSlider);
		this.add(this.greenPanel);

		this.blueSlider = new InvertibleRangeSlider(this.blueMin, this.blueMax + 1);
		this.bluePanel.add(this.blueLabel);
		this.bluePanel.add(this.blueSlider);
		this.add(this.bluePanel);

		this.hueSlider = new InvertibleRangeSlider(this.hueMin, this.hueMax + 1);
		this.huePanel.add(this.hueLabel);
		this.huePanel.add(this.hueSlider);
		this.add(this.huePanel);

		this.saturationSlider = new InvertibleRangeSlider(this.saturationMin,
				this.saturationMax + 1);
		this.saturationPanel.add(this.saturationLabel);
		this.saturationPanel.add(this.saturationSlider);
		this.add(this.saturationPanel);

		this.valueSlider = new InvertibleRangeSlider(this.valueMin, this.valueMax + 1);
		this.valuePanel.add(this.valueLabel);
		this.valuePanel.add(this.valueSlider);
		this.add(this.valuePanel);
	}

	/**
	 * Gets the lower and upper values for the red threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getRedSliderValues() {
		int[] lowerUpper = new int[] { this.redSlider.getLowerValue(),
				this.redSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the red threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setRedSliderValues(int lower, int upper) {
		this.redSlider.setLowerValue(lower);
		this.redSlider.setUpperValue(upper);
	}

	/**
	 * Tests if the red slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isRedSliderInverted() {
		return this.redSlider.isInverted();
	}

	/**
	 * Sets whether the red slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setRedSliderInverted(boolean inverted) {
		this.redSlider.setInverted(inverted);
	}

	/**
	 * Gets the lower and upper values for the green threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getGreenSliderValues() {
		int[] lowerUpper = new int[] { this.greenSlider.getLowerValue(),
				this.greenSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the green threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setGreenSliderValues(int lower, int upper) {
		this.greenSlider.setLowerValue(lower);
		this.greenSlider.setUpperValue(upper);
	}

	/**
	 * Tests if the green slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isGreenSliderInverted() {
		return this.greenSlider.isInverted();
	}

	/**
	 * Sets whether the green slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setGreenSliderInverted(boolean inverted) {
		this.greenSlider.setInverted(inverted);
	}

	/**
	 * Gets the lower and upper values for the blue threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getBlueSliderValues() {
		int[] lowerUpper = new int[] { this.blueSlider.getLowerValue(),
				this.blueSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the blue threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setBlueSliderValues(int lower, int upper) {
		this.blueSlider.setLowerValue(lower);
		this.blueSlider.setUpperValue(upper);
	}

	/**
	 * Tests if the blue slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isBlueSliderInverted() {
		return this.blueSlider.isInverted();
	}

	/**
	 * Sets whether the blue slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setBlueSliderInverted(boolean inverted) {
		this.blueSlider.setInverted(inverted);
	}

	/**
	 * Gets the lower and upper values for the hue threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getHueSliderValues() {
		int[] lowerUpper = new int[] { this.hueSlider.getLowerValue(),
				this.hueSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the hue threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setHueSliderValues(int lower, int upper) {
		this.hueSlider.setLowerValue(lower);
		this.hueSlider.setUpperValue(upper);
	}
	
	/**
	 * Tests if the hue slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isHueSliderInverted() {
		return this.hueSlider.isInverted();
	}

	/**
	 * Sets whether the hue slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setHueSliderInverted(boolean inverted) {
		this.hueSlider.setInverted(inverted);
	}

	/**
	 * Gets the lower and upper values for the saturation threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getSaturationSliderValues() {
		int[] lowerUpper = new int[] { this.saturationSlider.getLowerValue(),
				this.saturationSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the saturation threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setSaturationSliderValues(int lower, int upper) {
		this.saturationSlider.setLowerValue(lower);
		this.saturationSlider.setUpperValue(upper);
	}
	
	/**
	 * Tests if the saturation slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isSaturationSliderInverted() {
		return this.saturationSlider.isInverted();
	}

	/**
	 * Sets whether the saturation slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setSaturationSliderInverted(boolean inverted) {
		this.saturationSlider.setInverted(inverted);
	}

	/**
	 * Gets the lower and upper values for the colour value threshold slider
	 * 
	 * @return An int[] in the format {lower, upper}
	 */
	public int[] getValueSliderValues() {
		int[] lowerUpper = new int[] { this.valueSlider.getLowerValue(),
				this.valueSlider.getUpperValue() };
		return lowerUpper;
	}

	/**
	 * Sets the lower and upper values for the colour value threshold slider
	 * 
	 * @param lower
	 *            The new lower value
	 * @param upper
	 *            The new upper value
	 */
	public void setValueSliderValues(int lower, int upper) {
		this.valueSlider.setLowerValue(lower);
		this.valueSlider.setUpperValue(upper);
	}
	
	/**
	 * Tests if the value slider is inverted
	 * 
	 * @return true if it is inverted, false otherwise
	 */
	public boolean isValueSliderInverted() {
		return this.valueSlider.isInverted();
	}

	/**
	 * Sets whether the value slider is inverted
	 * 
	 * @param inverted
	 *            true if it should be inverted, false otherwise
	 */
	public void setValueSliderInverted(boolean inverted) {
		this.valueSlider.setInverted(inverted);
	}

	/**
	 * Used to set all the slider values at once
	 * 
	 * @param index
	 *            The PitchConstants index that corresponds to this
	 *            ThresholdsPanel
	 * @param pitchConstants
	 *            A PitchConstants object holding the threshold values the
	 *            sliders are to be set to
	 */
	public void setSliderValues(int index, PitchConstants pitchConstants) {
		setRedSliderValues(pitchConstants.getRedLower(index),
				pitchConstants.getRedUpper(index));
		setRedSliderInverted(pitchConstants.isRedInverted(index));
		
		setGreenSliderValues(pitchConstants.getGreenLower(index),
				pitchConstants.getGreenUpper(index));
		setGreenSliderInverted(pitchConstants.isGreenInverted(index));
		
		setBlueSliderValues(pitchConstants.getBlueLower(index),
				pitchConstants.getBlueUpper(index));
		setBlueSliderInverted(pitchConstants.isBlueInverted(index));

		setHueSliderValues((int) (255.0 * pitchConstants.getHueLower(index)),
				(int) (255.0 * pitchConstants.getHueUpper(index)));
		setHueSliderInverted(pitchConstants.isHueInverted(index));
		
		setSaturationSliderValues(
				(int) (255.0 * pitchConstants.getSaturationLower(index)),
				(int) (255.0 * pitchConstants.getSaturationUpper(index)));
		setSaturationSliderInverted(pitchConstants.isSaturationInverted(index));
		
		setValueSliderValues(
				(int) (255.0 * pitchConstants.getValueLower(index)),
				(int) (255.0 * pitchConstants.getValueUpper(index)));
		setValueSliderInverted(pitchConstants.isValueInverted(index));
	}

	/**
	 * Sets one (and only one) ChangeListener for the red slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setRedSliderChangeListener(ChangeListener listener) {
		assert (this.redSlider.getChangeListeners().length == 0);
		this.redSlider.addChangeListener(listener);
	}

	/**
	 * Sets one (and only one) ChangeListener for the green slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setGreenSliderChangeListener(ChangeListener listener) {
		assert (this.greenSlider.getChangeListeners().length == 0);
		this.greenSlider.addChangeListener(listener);
	}

	/**
	 * Sets one (and only one) ChangeListener for the blue slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setBlueSliderChangeListener(ChangeListener listener) {
		assert (this.blueSlider.getChangeListeners().length == 0);
		this.blueSlider.addChangeListener(listener);
	}

	/**
	 * Sets one (and only one) ChangeListener for the hue slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setHueSliderChangeListener(ChangeListener listener) {
		assert (this.hueSlider.getChangeListeners().length == 0);
		this.hueSlider.addChangeListener(listener);
	}

	/**
	 * Sets one (and only one) ChangeListener for the saturation slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setSaturationSliderChangeListener(ChangeListener listener) {
		assert (this.saturationSlider.getChangeListeners().length == 0);
		this.saturationSlider.addChangeListener(listener);
	}

	/**
	 * Sets one (and only one) ChangeListener for the value slider
	 * 
	 * @param listener
	 *            The ChangeListener
	 */
	public void setValueSliderChangeListener(ChangeListener listener) {
		assert (this.valueSlider.getChangeListeners().length == 0);
		this.valueSlider.addChangeListener(listener);
	}
}
