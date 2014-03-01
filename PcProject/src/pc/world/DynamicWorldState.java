package pc.world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class DynamicWorldState {
	private Point ball;
	private DirectedPoint attacker, defender;
	private DirectedPoint enemyAttacker, enemyDefender;

	private final List<StateUpdateListener> listeners = new ArrayList<StateUpdateListener>();

	public void addStateListener(StateUpdateListener listener) {
		listeners.add(listener);
	}
	
	public void removeStateListener(StateUpdateListener listener) {
		listeners.remove(listener);
	}
	
	public Point getBall() {
		return ball;
	}

	public DirectedPoint getAttacker() {
		return attacker;
	}

	public DirectedPoint getDefender() {
		return defender;
	}

	public DirectedPoint getEnemyAttacker() {
		return enemyAttacker;
	}

	public DirectedPoint getEnemyDefender() {
		return enemyDefender;
	}

	public void pushState(StaticWorldState staticState, long timestamp) {
		ball = staticState.getBall();
		attacker = staticState.getAttacker();
		defender = staticState.getDefender();
		enemyAttacker = staticState.getEnemyAttacker();
		enemyDefender = staticState.getEnemyDefender();
		
		for (StateUpdateListener listener : listeners) {
			listener.stateUpdated();
		}
	}

	public interface StateUpdateListener {
		void stateUpdated();
	}
}
