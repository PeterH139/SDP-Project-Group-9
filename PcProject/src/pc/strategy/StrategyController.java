package pc.strategy;

import java.util.ArrayList;

import lejos.pc.comm.NXTCommException;
import pc.comms.BrickCommServer;
import pc.comms.BtInfo;
import pc.strategy.interfaces.Strategy;
import pc.vision.Vision;
import pc.vision.interfaces.WorldStateReceiver;
import pc.world.WorldState;

public class StrategyController implements WorldStateReceiver {
	
	public enum StrategyType{
		PASSING, ATTACKING, DEFENDING, PENALTY
	}
	
	public BrickCommServer bcsAttacker, bcsDefender;
	private boolean ballInDefenderArea = false;
	
	private Vision vision;
	private ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	
	public StrategyController(Vision vision){
		this.vision = vision;
		
		bcsAttacker = null;
		bcsDefender = null;
		try {
			bcsAttacker = new BrickCommServer();
			bcsAttacker.guiConnect(BtInfo.group10);
			bcsDefender = new BrickCommServer();
			bcsDefender.guiConnect(BtInfo.MEOW);
		} catch (NXTCommException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Change to a particular strategy, removing and stopping the previously running strategy(s).
	 * 
	 * @param type - The strategy type to run
	 */
	public void changeToStrategy(StrategyType type){
		// Stop old threads
		for (Strategy s : this.currentStrategies){
			s.stopControlThread();
			vision.removeWorldStateReciver(s);
		}
		
		switch(type){
		case PASSING:
			Strategy ps = new PassingStrategy(bcsAttacker,bcsDefender);
			currentStrategies.add(ps);
			vision.addWorldStateReceiver(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(bcsAttacker);
			currentStrategies.add(as);
			vision.addWorldStateReceiver(as);
			as.startControlThread();
			break;
		case DEFENDING:
			Strategy a = new AttackerStrategy(bcsAttacker);
			Strategy ds = new InterceptorStrategy(bcsDefender);
			currentStrategies.add(ds);
			currentStrategies.add(a);
			vision.addWorldStateReceiver(ds);
			vision.addWorldStateReceiver(a);
			ds.startControlThread();
			a.startControlThread();
			break;
		case PENALTY:
			Strategy pen = new PenaltyStrategy(bcsAttacker);
			currentStrategies.add(pen);
			vision.addWorldStateReceiver(pen);
			pen.startControlThread();
			break;
		}
		
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		// Check where the ball is, and make a decision on which strategies to run based upon that.
		int defenderCheck = (worldState.weAreShootingRight) 
				? worldState.dividers[0] : worldState.dividers[2];
		float ballX = worldState.getBall().x;
		boolean prevBallInDefenderArea = this.ballInDefenderArea;
		
		if ((worldState.weAreShootingRight && ballX < defenderCheck)
				|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
			ballInDefenderArea = true;
		} else {
			ballInDefenderArea = false;
		}
		
		if (prevBallInDefenderArea != this.ballInDefenderArea){
			if (ballInDefenderArea){
				changeToStrategy(StrategyType.PASSING);
			} else {
				changeToStrategy(StrategyType.DEFENDING);
			}
		}  
		
	}
	
}
