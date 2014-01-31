package pc.vision.gui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class InvertibleRangeSlider extends JPanel {
	protected final RangeSlider slider;
	protected final JCheckBox invertCheckBox;

	public InvertibleRangeSlider() {
		super();
		this.slider = new RangeSlider();
		this.invertCheckBox = new JCheckBox("Inv");
		this.add(this.slider);
		this.add(this.invertCheckBox);
	}

	public InvertibleRangeSlider(int min, int max) {
		super();
		this.slider = new RangeSlider(min, max);
		this.invertCheckBox = new JCheckBox();
		this.add(this.slider);
		this.add(this.invertCheckBox);
	}

	public int getLowerValue() {
		return this.slider.getLowerValue();
	}

	public void setLowerValue(int value) {
		this.slider.setLowerValue(value);
	}

	public int getUpperValue() {
		return this.slider.getUpperValue();
	}

	public void setUpperValue(int value) {
		this.slider.setUpperValue(value);
	}

	public boolean isInverted() {
		return this.invertCheckBox.isSelected();
	}

	public void setInverted(boolean inverted) {
		this.invertCheckBox.setSelected(inverted);
	}

	public ChangeListener[] getChangeListeners() {
		return this.slider.getChangeListeners();
	}

	public void addChangeListener(ChangeListener listener) {
		this.slider.addChangeListener(listener);
		this.invertCheckBox.addChangeListener(listener);
	}
}
