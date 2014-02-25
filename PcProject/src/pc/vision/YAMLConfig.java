package pc.vision;

import java.util.Observable;

public class YAMLConfig extends Observable {
	private Object yamlData;
	
	public void reloadConfig() {
		setChanged();
		try {
			notifyObservers(yamlData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pushConfig(Object yamlData) {
		setChanged();
		notifyObservers(yamlData);
		this.yamlData = yamlData;
	}
}
