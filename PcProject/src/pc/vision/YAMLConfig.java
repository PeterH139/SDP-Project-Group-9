package pc.vision;

import java.util.Observable;

public class YAMLConfig extends Observable {
	public void pushConfig(Object yamlData) {
		setChanged();
		notifyObservers(yamlData);
	}
}
