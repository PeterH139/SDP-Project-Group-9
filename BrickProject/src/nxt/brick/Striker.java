package nxt.brick;

/*
 *  This class allows changes to general movements that are specific for the striker
 *  
 *  @author Ross Grassie
 */

public class Striker extends Movement {

	static final int TRACK_WIDTH_STRIKER = 121;
	static final int TRAVEL_SPEED_STRIKER = 90;
	
	public Striker() {
		super(TRACK_WIDTH_STRIKER);
		setMaxPilotSpeed(TRAVEL_SPEED_STRIKER);
	}
	
	public void shoot(int strength) {
		super.kick(strength);
	}

}
