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
	private boolean ballInAttackerArea = false;
	
	private Vision vision;
	private static ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	private static ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();
	
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
	
	public static ArrayList<Strategy> getCurrentStrategies() {
		return currentStrategies;
	}
	public static ArrayList<Strategy> getRemovedStrategies() {
		return removedStrategies;
	}
	public static void setRemovedStrategies(ArrayList<Strategy> removedStrategies) {
		StrategyController.removedStrategies = removedStrategies;
	}
	/**
	 * Change to a particular strategy, removing and stopping the previously running strategy(s).
	 * 
	 * @param type - The strategy type to run
	 */
	public void changeToStrategy(StrategyType type){
		// Stop old threads
		for (Strategy s : StrategyController.currentStrategies){
			s.stopControlThread();
			StrategyController.removedStrategies.add(s);
			//this.vision.removeWorldStateReciver(s);
		}
		StrategyController.currentStrategies = new ArrayList<Strategy>();
		switch(type){
		case PASSING:
			Strategy ps = new PassingStrategy(this.bcsAttacker,this.bcsDefender);
			StrategyController.currentStrategies.add(ps);
			//this.vision.addWorldStateReceiver(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			Strategy ic = new InterceptorStrategy(this.bcsDefender); 
			StrategyController.currentStrategies.add(as);
			StrategyController.currentStrategies.add(ic);
			//this.vision.addWorldStateReceiver(as);
			as.startControlThread();
			ic.startControlThread();
			break;
		case DEFENDING:
			Strategy ms = new MarkingStrategy(this.bcsAttacker);
			Strategy ds = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(ds);
			StrategyController.currentStrategies.add(ms);
			//this.vision.addWorldStateReceiver(ds);
			//this.vision.addWorldStateReceiver(a);
			ds.startControlThread();
			ms.startControlThread();
			break;
		case PENALTY:
			Strategy pen = new PenaltyStrategy(this.bcsAttacker);
			StrategyController.currentStrategies.add(pen);
			//this.vision.addWorldStateReceiver(pen);
			pen.startControlThread();
			break;
		case MARKING:
			Strategy mar = new MarkingStrategy(this.bcsAttacker);
			StrategyController.currentStrategies.add(mar);
			//this.vision.addWorldStateReceiver(mar);
			mar.startControlThread();
			break;
		}
		
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		// Check where the ball is, and make a decision on which strategies to run based upon that.
		int defenderCheck = (worldState.weAreShootingRight) 
				? worldState.dividers[0] : worldState.dividers[2];
		int leftCheck = (worldState.weAreShootingRight) ? worldState.dividers[1] : worldState.dividers[0];
		int rightCheck = (worldState.weAreShootingRight) ? worldState.dividers[2] : worldState.dividers[1];
		float ballX = worldState.getBall().x;
		boolean prevBallInDefenderArea = this.ballInDefenderArea;
		boolean prevBallInAttackerArea = this.ballInAttackerArea;
		
		
		if ((worldState.weAreShootingRight && ballX < defenderCheck)
				|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
			this.ballInDefenderArea = true;
			this.ballInAttackerArea = false;
		} else if (ballX > leftCheck && ballX < rightCheck){
			this.ballInDefenderArea = false;
			this.ballInAttackerArea = true;
		} else {
			this.ballInAttackerArea = false;
			this.ballInDefenderArea = false;
		}
		System.out.println("BallAttacker: " + this.ballInAttackerArea + " ballDefender: " + this.ballInDefenderArea);;
		if (prevBallInDefenderArea != this.ballInDefenderArea || prevBallInAttackerArea != this.ballInAttackerArea){
			if (this.ballInDefenderArea){
				changeToStrategy(StrategyType.PASSING);
			} 
			
			if (this.ballInAttackerArea) {
				changeToStrategy(StrategyType.ATTACKING);
			} 
		
			if(!this.ballInAttackerArea && !this.ballInDefenderArea) {
				changeToStrategy(StrategyType.DEFENDING);
			}
		}  
		
	}
	
}
