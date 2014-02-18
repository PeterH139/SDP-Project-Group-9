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
		PASSING, ATTACKING, DEFENDING, PENALTY, MARKING
	}
	
	public BrickCommServer bcsAttacker, bcsDefender;
	private boolean ballInDefenderArea = false;
	
	private Vision vision;
	private ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	
	public StrategyController(Vision vision){
		this.vision = vision;
		
		this.bcsAttacker = null;
		this.bcsDefender = null;
		try {
			this.bcsAttacker = new BrickCommServer();
			this.bcsAttacker.guiConnect(BtInfo.group10);
			this.bcsDefender = new BrickCommServer();
			this.bcsDefender.guiConnect(BtInfo.MEOW);
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
			this.vision.removeWorldStateReciver(s);
		}
		
		switch(type){
		case PASSING:
			Strategy ps = new PassingStrategy(this.bcsAttacker,this.bcsDefender);
			this.currentStrategies.add(ps);
			this.vision.addWorldStateReceiver(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			this.currentStrategies.add(as);
			this.vision.addWorldStateReceiver(as);
			as.startControlThread();
			break;
		case DEFENDING:
			Strategy a = new AttackerStrategy(this.bcsAttacker);
			Strategy ds = new InterceptorStrategy(this.bcsDefender);
			this.currentStrategies.add(ds);
			this.currentStrategies.add(a);
			this.vision.addWorldStateReceiver(ds);
			this.vision.addWorldStateReceiver(a);
			ds.startControlThread();
			a.startControlThread();
			break;
		case PENALTY:
			Strategy pen = new PenaltyStrategy(this.bcsAttacker);
			this.currentStrategies.add(pen);
			this.vision.addWorldStateReceiver(pen);
			pen.startControlThread();
			break;
		case MARKING:
			Strategy mar = new MarkingStrategy(this.bcsAttacker);
			this.currentStrategies.add(mar);
			this.vision.addWorldStateReceiver(mar);
			mar.startControlThread();
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
			this.ballInDefenderArea = true;
		} else {
			this.ballInDefenderArea = false;
		}
		
		if (prevBallInDefenderArea != this.ballInDefenderArea){
			if (this.ballInDefenderArea){
				changeToStrategy(StrategyType.PASSING);
			} else {
				changeToStrategy(StrategyType.DEFENDING);
			}
		}  
		
	}
	
}
