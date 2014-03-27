package pc.strategy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.oldmodel.WorldState;

public class StrategyController implements WorldStateReceiver {

	/** Measured in milliseconds */
	public static final int STRATEGY_TICK = 100; // TODO: Test lower values for this and see where it breaks
	
	boolean haveReset = false;

	public enum StrategyType {
		DO_NOTHING, PASSING, ATTACKING, DEFENDING, MARKING
	}
	
	public enum BallLocation{
		DEFENDER, ATTACKER, ENEMY_DEFENDER, ENEMY_ATTACKER
	}

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public BrickCommServer bcsAttacker, bcsDefender;
	private BallLocation ballLocation;

	private StrategyType currentStrategy = StrategyType.DO_NOTHING;

	private boolean pauseStrategyController = true;
	private static ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	private static ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();

	public StrategyController() {
		this.bcsAttacker = new BrickCommServer();
		this.bcsDefender = new BrickCommServer();
	}

	public StrategyType getCurrentStrategy() {
		return currentStrategy;
	}

	public boolean isPaused() {
		return pauseStrategyController;
	}

	public void setPaused(boolean paused) {
		boolean oldValue = pauseStrategyController;
		pauseStrategyController = paused;
		pcs.firePropertyChange("paused", oldValue, paused);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public static ArrayList<Strategy> getCurrentStrategies() {
		return currentStrategies;
	}

	public static ArrayList<Strategy> getRemovedStrategies() {
		return removedStrategies;
	}

	public static void setRemovedStrategies(
			ArrayList<Strategy> removedStrategies) {
		StrategyController.removedStrategies = removedStrategies;
	}

	/**
	 * Change to a particular strategy, removing and stopping the previously
	 * running strategy(s).
	 * 
	 * @param type
	 *            - The strategy type to run
	 */
	public void changeToStrategy(StrategyType type) {
		// Stop old threads
		for (Strategy s : StrategyController.currentStrategies) {
			s.stopControlThread();
			StrategyController.removedStrategies.add(s);
		}
		StrategyController.currentStrategies = new ArrayList<Strategy>();
		
		switch (type) {
		case DO_NOTHING:
			break;
		case PASSING:
			Strategy ps = new PassingStrategy(this.bcsAttacker,
					this.bcsDefender);
			StrategyController.currentStrategies.add(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			Strategy ic = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(as);
			StrategyController.currentStrategies.add(ic);
			as.startControlThread();
			ic.startControlThread();
			break;
		case DEFENDING:
			Strategy AS = new AttackerStrategy(this.bcsAttacker);
			Strategy pds = new DefenderStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(pds);
			StrategyController.currentStrategies.add(AS);
			pds.startControlThread();
			AS.startControlThread();
			break;
		case MARKING:
			Strategy mar = new MarkingStrategy(this.bcsAttacker);
			Strategy ics = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(mar);
			StrategyController.currentStrategies.add(ics);
			mar.startControlThread();
			ics.startControlThread();
			break;
		default:
			break;
		}
		StrategyType oldType = currentStrategy;
		currentStrategy = type;
		pcs.firePropertyChange("currentStrategy", oldType, currentStrategy);
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		if (pauseStrategyController)
			return;
		// Check where the ball is, and make a decision on which strategies to
		// run based upon that.
		int defenderCheck = (worldState.weAreShootingRight) ? worldState.dividers[0]
				: worldState.dividers[2];
		int leftCheck = (worldState.weAreShootingRight) ? worldState.dividers[1]
				: worldState.dividers[0];
		int rightCheck = (worldState.weAreShootingRight) ? worldState.dividers[2]
				: worldState.dividers[1];
		float ballX = worldState.getBall().x;
		// Mark zone the ball was in on the previous frame.
		BallLocation prevBallLocation = this.ballLocation;

		// Find where the ball is located on the pitch
		if ((worldState.weAreShootingRight && ballX < defenderCheck)
				|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
			this.ballLocation = BallLocation.DEFENDER;
		} else if (ballX > leftCheck && ballX < rightCheck) {
			this.ballLocation = BallLocation.ATTACKER;
		} else if (worldState.weAreShootingRight && ballX > defenderCheck
				&& ballX < leftCheck || !worldState.weAreShootingRight
				&& ballX < defenderCheck && ballX > rightCheck) {
			this.ballLocation = BallLocation.ENEMY_ATTACKER;
		} else if (!worldState.weAreShootingRight && (ballX < leftCheck)
				|| worldState.weAreShootingRight && (ballX > rightCheck)) {
			this.ballLocation = BallLocation.ENEMY_DEFENDER;
		}

		// Change strategy only if the ball has changed pitch area.
		if (prevBallLocation != ballLocation){			
			switch(this.ballLocation){
			case ATTACKER:
				changeToStrategy(StrategyType.ATTACKING);
				break;
			case DEFENDER:
				changeToStrategy(StrategyType.PASSING);
				break;
			case ENEMY_ATTACKER:
				changeToStrategy(StrategyType.DEFENDING);
				break;
			case ENEMY_DEFENDER:
				changeToStrategy(StrategyType.MARKING);
				break;
			}
		}
	}
}
