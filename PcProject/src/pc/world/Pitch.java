/**
 * Name : Pitch.java
 * Author : Dimitar Petrov
 * Description : Represents the playing field, 
 * specifies the attacker and defender areas
 * 
 * */
package pc.world;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pc.vision.PitchConstants;
import pc.vision.Vector2f;
import pc.vision.YAMLConfig;

public class Pitch extends Observable {
	/*
	 * All lengths and distances are in millimetres.
	 */

	public static final int BALL_RADIUS = 25;
	public static final int PLATE_EDGE_LENGTH = 100;

	private int pitchWidth = 1400;
	private int pitchHeight = 800;
	private int cornerCutoffX = 100;
	private int cornerCutoffY = 200;
	private int goalHeight = 300;
	private int ballRadius = 20;
	
	private int zoneDividerWidth = 100;
	private int zoneDividerOffset = 400;

	private int pitchCenterFrameX = 320;
	private int pitchCenterFrameY = 240;
	private int pitchFrameWidth = 600;

	// Height is inferred from pitchWidth/pitchHeight ratio

	public Pitch(YAMLConfig yamlConfig, final PitchConstants pitchConstants) {
		yamlConfig.addObserver(new Observer() {

			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable arg0, Object yamlData) {
				String pitchName = pitchConstants.getPitchNum() == 0 ? "main"
						: "side";

				Map<String, Object> topData = (Map<String, Object>) yamlData;
				Map<String, Object> data = (Map<String, Object>) topData.get("pitch");
				data = (Map<String, Object>) data.get(pitchName);
				List<Integer> pitchDims = (List<Integer>) data.get("size");
				setPitchWidth(pitchDims.get(0));
				setPitchHeight(pitchDims.get(1));
				List<Integer> corners = (List<Integer>) data.get("corners");
				setCornerCutoffX(corners.get(0));
				setCornerCutoffY(corners.get(1));
				setGoalHeight((Integer) data.get("goalHeight"));
				
				Map<String, Object> zoneDivs = (Map<String, Object>) data.get("zoneDividers");
				setZoneDividerWidth((Integer) zoneDivs.get("width"));
				setZoneDividerOffset((Integer) zoneDivs.get("offset"));

				Map<String, Object> frameProjection = (Map<String, Object>) data
						.get("videoFrameProjection");
				List<Integer> pitchCenter = (List<Integer>) frameProjection
						.get("centerPx");
				setPitchCenterFrameX(pitchCenter.get(0));
				setPitchCenterFrameY(pitchCenter.get(1));
				setPitchFrameWidth((Integer) frameProjection.get("widthPx"));
			}
		});
	}

	public int getPitchWidth() {
		return pitchWidth;
	}

	public void setPitchWidth(int pitchWidth) {
		if (this.pitchWidth != pitchWidth) {
			this.pitchWidth = pitchWidth;
			setChanged();
			notifyObservers();
		}
	}

	public int getPitchHeight() {
		return pitchHeight;
	}

	public void setPitchHeight(int pitchHeight) {
		if (this.pitchHeight != pitchHeight) {
			this.pitchHeight = pitchHeight;
			setChanged();
			notifyObservers();
		}
	}

	public int getCornerCutoffX() {
		return cornerCutoffX;
	}

	public void setCornerCutoffX(int cornerCutoffX) {
		if (this.cornerCutoffX != cornerCutoffX) {
			this.cornerCutoffX = cornerCutoffX;
			setChanged();
			notifyObservers();
		}
	}

	public int getCornerCutoffY() {
		return cornerCutoffY;
	}

	public void setCornerCutoffY(int cornerCutoffY) {
		if (this.cornerCutoffY != cornerCutoffY) {
			this.cornerCutoffY = cornerCutoffY;
			setChanged();
			notifyObservers();
		}
	}

	public int getGoalHeight() {
		return goalHeight;
	}

	public void setGoalHeight(int goalHeight) {
		if (this.goalHeight != goalHeight) {
			this.goalHeight = goalHeight;
			setChanged();
			notifyObservers();
		}
	}

	public Polygon getBoundsPolygon() {
		int halfPitchWidth = getPitchWidth() / 2;
		int halfPitchHeight = getPitchHeight() / 2;
		Polygon polygon = new Polygon();
		polygon.addPoint(-halfPitchWidth + getCornerCutoffX(), -halfPitchHeight);
		polygon.addPoint(halfPitchWidth - getCornerCutoffX(), -halfPitchHeight);
		polygon.addPoint(halfPitchWidth, -halfPitchHeight + getCornerCutoffY());
		polygon.addPoint(halfPitchWidth, halfPitchHeight - getCornerCutoffY());
		polygon.addPoint(halfPitchWidth - getCornerCutoffX(), halfPitchHeight);
		polygon.addPoint(-halfPitchWidth + getCornerCutoffX(), halfPitchHeight);
		polygon.addPoint(-halfPitchWidth, halfPitchHeight - getCornerCutoffY());
		polygon.addPoint(-halfPitchWidth, -halfPitchHeight + getCornerCutoffY());
		return polygon;
	}

	public int getPitchCenterFrameX() {
		return pitchCenterFrameX;
	}

	public void setPitchCenterFrameX(int pitchCenterFrameX) {
		this.pitchCenterFrameX = pitchCenterFrameX;
	}

	public int getPitchCenterFrameY() {
		return pitchCenterFrameY;
	}

	public void setPitchCenterFrameY(int pitchCenterFrameY) {
		this.pitchCenterFrameY = pitchCenterFrameY;
	}

	public int getPitchFrameWidth() {
		return pitchFrameWidth;
	}

	public void setPitchFrameWidth(int pitchFrameWidth) {
		this.pitchFrameWidth = pitchFrameWidth;
	}

	/**
	 * Converts a given point from pixels to millimetres.
	 */
	public void framePointToModel(Point2D point) {
		double scale = (double) getPitchWidth() / getPitchFrameWidth();
		point.setLocation(scale * (point.getX() - getPitchCenterFrameX()),
				scale * (point.getY() - getPitchCenterFrameY()));
	}

	public int getZoneDividerWidth() {
		return zoneDividerWidth;
	}

	public void setZoneDividerWidth(int zoneDividerWidth) {
		this.zoneDividerWidth = zoneDividerWidth;
	}

	public int getZoneDividerOffset() {
		return zoneDividerOffset;
	}

	public void setZoneDividerOffset(int zoneDividerOffset) {
		this.zoneDividerOffset = zoneDividerOffset;
	}
}
