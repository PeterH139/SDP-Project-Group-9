package pc.world;

import java.awt.Rectangle;

public class RobotModel {
	public static final RobotModel ATTACKER_ROBOT = new RobotModel(
			new Rectangle(-35, -30, 80, 80),
			new Rectangle(-50, -90, 100, 140),
			new Rectangle(-35, -80, 70, 35));
	
	public static final RobotModel GENERIC_ROBOT = new RobotModel(
			new Rectangle(-35, -30, 80, 80),
			new Rectangle(-60, -60, 120, 120),
			null);


	private Rectangle plate; // Origin is robot position
	private Rectangle extents; // Origin is plate center
	private Rectangle catcher; // Origin is plate center

	private RobotModel(Rectangle plate, Rectangle extents, Rectangle catcher) {
		this.plate = plate;
		this.extents = extents;
		this.catcher = catcher;
	}

	public Rectangle getExtents() {
		return extents;
	}

	public Rectangle getPlate() {
		return plate;
	}

	public Rectangle getCatcher() {
		return catcher;
	}

}
