package pc.strategy;

import java.util.ArrayList;

import lejos.pc.comm.NXTCommException;
import pc.comms.BrickCommServer;
import pc.comms.BrickControlGUI;
import pc.comms.BtInfo;
import pc.strategy.interfaces.Strategy;
import pc.vision.Vision;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class StrategyController implements WorldStateReceiver {
	boolean haveReset = false;

	private static final int DIVIDER_THRESHOLD = 35; // Used for checking if the
														// robots are too close
														// to the dividing
														// lines.

	public enum StrategyType {
		DO_NOTHING, PASSING, ATTACKING, DEFENDING, PENALTY_ATK, PENALTY_DEF, MARKING, RESET_ATK, RESET_DEF
	}

	public BrickCommServer bcsAttacker, bcsDefender;
	private boolean ballInDefenderArea = false;
	private boolean ballInAttackerArea = false;
	private boolean ballInEnemyAttackerArea = false;
	private boolean ballInEnemyDefenderArea = false;

	private Vision vision;

	public boolean pauseStrategyController = true;
	private static ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	private static ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();

	public StrategyController(Vision vision) {
		this.vision = vision;

		this.bcsAttacker = null;
		this.bcsDefender = null;
		try {
			this.bcsAttacker = new BrickCommServer();
			BrickControlGUI.guiConnect(this.bcsAttacker, BtInfo.group10);
			this.bcsDefender = new BrickCommServer();
			BrickControlGUI.guiConnect(this.bcsDefender, BtInfo.MEOW);
		} catch (NXTCommException e) {
			e.printStackTrace();
		}
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
			System.out.println("Changing to Passing.");
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			Strategy ic = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(as);
			StrategyController.currentStrategies.add(ic);
			// this.vision.addWorldStateReceiver(as);
			as.startControlThread();
			ic.startControlThread();
			System.out.println("Changing to Attacking.");
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
			System.out.println("Changing to Defending.");
			break;
		case PENALTY_ATK:
			Strategy penAtk = new PenaltyAttackStrategy(this.bcsAttacker);
			StrategyController.currentStrategies.add(penAtk);
			// this.vision.addWorldStateReceiver(pen);
			penAtk.startControlThread();
			System.out.println("Changing to Penalty Attack.");
			break;
		case PENALTY_DEF:
			Strategy penDef = new PenaltyDefenderStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(penDef);
			penDef.startControlThread();
			System.out.println("Changing to Penalty Defence");
			break;
		case MARKING:
			Strategy mar = new MarkingStrategy(this.bcsAttacker);
			Strategy ics = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(mar);
			StrategyController.currentStrategies.add(ics);
			// this.vision.addWorldStateReceiver(mar);
			mar.startControlThread();
			ics.startControlThread();
			System.out.println("Changing to Marking.");
			break;
		case RESET_ATK:
			Strategy res = new ResetStrategy(bcsAttacker, true);
			Strategy pende = new PenaltyDefenderStrategy(bcsDefender);
			StrategyController.currentStrategies.add(res);
			StrategyController.currentStrategies.add(pende);
			res.startControlThread();
			pende.startControlThread();
			System.out.println("Changing to Reset Attack.");
			break;
		case RESET_DEF:
			Strategy resa = new AttackerStrategy(bcsAttacker);
			Strategy resd = new ResetStrategy(bcsDefender, false);
			StrategyController.currentStrategies.add(resa);
			StrategyController.currentStrategies.add(resd);
			resa.startControlThread();
			resd.startControlThread();
			System.out.println("Changing to Reset Defence.");
			break;
		default:
			break;
		}

	}

	@Override
	public void sendWorldState(WorldState worldState) {
		if (!pauseStrategyController) {
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
					&& ballX < leftCheck) {
				this.ballInEnemyAttackerArea = true;
				this.ballInEnemyDefenderArea = false;
				this.ballInAttackerArea = false;
				this.ballInDefenderArea = false;
			} else if (ballX > rightCheck) {
				this.ballInEnemyAttackerArea = false;
				this.ballInEnemyDefenderArea = true;
				this.ballInAttackerArea = false;
				this.ballInDefenderArea = false;
			}
//			 System.out.println("BallAttacker: " + this.ballInAttackerArea +
//			 " ballDefender: " + this.ballInDefenderArea +
//			 " ballEnemyAttacker: " + this.ballInEnemyAttackerArea + 
//			 " ballEnemyDefender: " + this.ballInEnemyDefenderArea);
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
		} else {
			System.out.println("Strategy Controller Paused");
			changeToStrategy(StrategyType.DO_NOTHING);
		}
	}
}
