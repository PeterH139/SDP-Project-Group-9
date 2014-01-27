package nxt.brick;

/*
 *  This class allows changes to general movements that are specific for the keeper
 *  
 *  @author Ross Grassie
 */

public class Keeper extends Movement {

	static final int TRACK_WIDTH_KEEPER = 130;
	static final int TRAVEL_SPEED_KEEPER = 90;
	
	public Keeper() {
		super(TRACK_WIDTH_KEEPER);
	}

}
