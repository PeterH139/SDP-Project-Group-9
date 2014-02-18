package pc.strategy.interfaces;

import pc.vision.interfaces.WorldStateReceiver;

public interface Strategy extends WorldStateReceiver{
	
	public void startControlThread();
	public void stopControlThread();

}
