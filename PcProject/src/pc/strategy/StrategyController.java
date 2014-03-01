package pc.strategy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import pc.comms.BrickCommServer;
import pc.strategy.interfaces.Strategy;
import pc.vision.Vision;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.oldmodel.WorldState;

public class StrategyController implements WorldStateReceiver {
	boolean haveReset = false;

	private static final int DIVIDER_THRESHOLD = 35; // Used for checking if the
														// robots are too close
														// to the dividing
														// lines.

	public enum StrategyType {
		DO_NOTHING, PASSING, ATTACKING, DEFENDING, PENALTY_ATK, PENALTY_DEF, MARKING, RESET_ATK, RESET_DEF
	}

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public BrickCommServer bcsAttacker, bcsDefender;
	private boolean ballInDefenderArea = false;
	private boolean ballInAttackerArea = false;
	private boolean ballInEnemyAttackerArea = false;
	private boolean ballInEnemyDefenderArea = false;

	private Vision vision;
	private StrategyType currentStrategy = StrategyType.DO_NOTHING;

	private boolean pauseStrategyController = true;
	private static ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	private static ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();

	public StrategyController(Vision vision) {
		this.vision = vision;
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
			// this.vision.removeWorldStateReciver(s);
		}
		StrategyController.currentStrategies = new ArrayList<Strategy>();
		switch (type) {
		case DO_NOTHING:
			break;
		case PASSING:
			Strategy ps = new PassingStrategy(this.bcsAttacker,
					this.bcsDefender);
			StrategyController.currentStrategies.add(ps);
			// this.vision.addWorldStateReceiver(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			Strategy ic = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(as);
			StrategyController.currentStrategies.add(ic);
			// this.vision.addWorldStateReceiver(as);
			as.startControlThread();
			ic.startControlThread();
			break;
		case DEFENDING:
			Strategy AS = new AttackerStrategy(this.bcsAttacker);
			Strategy pds = new PenaltyDefenderStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(pds);
			StrategyController.currentStrategies.add(AS);
			// this.vision.addWorldStateReceiver(ds);
			// this.vision.addWorldStateReceiver(a);
			pds.startControlThread();
			AS.startControlThread();
			break;
		case PENALTY_ATK:
			Strategy penAtk = new PenaltyAttackStrategy(this.bcsAttacker);
			StrategyController.currentStrategies.add(penAtk);
			// this.vision.addWorldStateReceiver(pen);
			penAtk.startControlThread();
			break;
		case PENALTY_DEF:
			Strategy penDef = new PenaltyDefenderStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(penDef);
			penDef.startControlThread();
			break;
		case MARKING:
			Strategy mar = new MarkingStrategy(this.bcsAttacker);
			Strategy ics = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(mar);
			StrategyController.currentStrategies.add(ics);
			// this.vision.addWorldStateReceiver(mar);
			mar.startControlThread();
			ics.startControlThread();
			break;
		case RESET_ATK:
			Strategy res = new ResetStrategy(bcsAttacker, true);
			Strategy pende = new PenaltyDefenderStrategy(bcsDefender);
			StrategyController.currentStrategies.add(res);
			StrategyController.currentStrategies.add(pende);
			res.startControlThread();
			pende.startControlThread();
			break;
		case RESET_DEF:
			Strategy resa = new AttackerStrategy(bcsAttacker);
			Strategy resd = new ResetStrategy(bcsDefender, false);
			StrategyController.currentStrategies.add(resa);
			StrategyController.currentStrategies.add(resd);
			resa.startControlThread();
			resd.startControlThread();
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
		// Check where the ball is, and make a decision on which strategies
		// to
		// run based upon that.
		int defenderCheck = (worldState.weAreShootingRight) ? worldState.dividers[0]
				: worldState.dividers[2];
		int leftCheck = (worldState.weAreShootingRight) ? worldState.dividers[1]
				: worldState.dividers[0];
		int rightCheck = (worldState.weAreShootingRight) ? worldState.dividers[2]
				: worldState.dividers[1];
		float ballX = worldState.getBall().x;
		boolean prevBallInDefenderArea = this.ballInDefenderArea;
		boolean prevBallInAttackerArea = this.ballInAttackerArea;
		boolean prevBallInEnemyDefenderArea = this.ballInEnemyDefenderArea;
		boolean prevBallInEnemyAttackerArea = this.ballInEnemyAttackerArea;
		boolean prevHaveReset = this.haveReset;

		if ((worldState.weAreShootingRight && ballX < defenderCheck)
				|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
			this.ballInDefenderArea = true;
			this.ballInAttackerArea = false;
			this.ballInEnemyDefenderArea = false;
			this.ballInEnemyAttackerArea = false;
		} else if (ballX > leftCheck && ballX < rightCheck) {
			this.ballInDefenderArea = false;
			this.ballInAttackerArea = true;
			this.ballInEnemyDefenderArea = false;
			this.ballInEnemyAttackerArea = false;
		} else if (worldState.weAreShootingRight && ballX > defenderCheck
				&& ballX < leftCheck || !worldState.weAreShootingRight
				&& ballX < defenderCheck && ballX > rightCheck) {
			this.ballInEnemyAttackerArea = true;
			this.ballInEnemyDefenderArea = false;
			this.ballInAttackerArea = false;
			this.ballInDefenderArea = false;
		} else if (!worldState.weAreShootingRight && (ballX < leftCheck)
				|| worldState.weAreShootingRight && (ballX > rightCheck)) {
			this.ballInEnemyAttackerArea = false;
			this.ballInEnemyDefenderArea = true;
			this.ballInAttackerArea = false;
			this.ballInDefenderArea = false;
		}
		// System.out.println("BallAttacker: " + this.ballInAttackerArea +
		// " ballDefender: " + this.ballInDefenderArea +
		// " ballEnemyAttacker: " + this.ballInEnemyAttackerArea +
		// " ballEnemyDefender: " + this.ballInEnemyDefenderArea);
		boolean defXTooClose = Math.abs(worldState.getDefenderRobot().x
				- defenderCheck) < DIVIDER_THRESHOLD;
		boolean atkXTooClose = Math.abs(worldState.getAttackerRobot().x
				- leftCheck) < DIVIDER_THRESHOLD
				|| Math.abs(worldState.getAttackerRobot().x - rightCheck) < DIVIDER_THRESHOLD;
		if (defXTooClose || atkXTooClose) {
			haveReset = true;
		} else {
			haveReset = false;
		}

		if (/*
			 * prevHaveReset != haveReset ||
			 */prevBallInDefenderArea != this.ballInDefenderArea
				|| prevBallInAttackerArea != this.ballInAttackerArea
				|| prevBallInEnemyAttackerArea != this.ballInEnemyAttackerArea
				|| prevBallInEnemyDefenderArea != this.ballInEnemyDefenderArea) {
			// if (haveReset) {
			// if (defXTooClose) {
			// changeToStrategy(StrategyType.RESET_DEF);
			// } else if (atkXTooClose) {
			// changeToStrategy(StrategyType.RESET_ATK);
			// }
			// } else {
			if (this.ballInDefenderArea) {
				changeToStrategy(StrategyType.PASSING);
			}
			if (this.ballInAttackerArea) {
				changeToStrategy(StrategyType.ATTACKING);
			}
			if (this.ballInEnemyAttackerArea) {
				changeToStrategy(StrategyType.DEFENDING);
			}
			if (this.ballInEnemyDefenderArea) {
				changeToStrategy(StrategyType.MARKING);
			}
		}
		// }
	}
}
