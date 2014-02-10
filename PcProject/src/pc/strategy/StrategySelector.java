package pc.strategy;

import pc.comms.BrickCommServer;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

/* Class to select a relevant strategy depending on what is happening during the game.
 * 
 * @author Ross Grassie (s1131494)
 * 
 */

public class StrategySelector {

	public static WorldStateReceiver selector(BrickCommServer bcs, WorldState state) {
		
		if (state.GetPossession()) {
			AttackerStrategy as = new AttackerStrategy(bcs);
			return as;
		} else {
			InterceptorStrategy is = new InterceptorStrategy(bcs);
			return is;
		}
	}
}
