/**
 * Name : Pitch.java
 * Author : Dimitar Petrov
 * Description : Represents the playing field, 
 * specifies the attacker and defender areas
 * 
 * */
package pc.world;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

import com.google.gson.Gson;

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
		polygon.addPoint(-halfPitchWidth + getCornerCutoffX(),
				-halfPitchHeight);
		polygon.addPoint(halfPitchWidth - getCornerCutoffX(),
				-halfPitchHeight);
		polygon.addPoint(halfPitchWidth,
				-halfPitchHeight + getCornerCutoffY());
		polygon.addPoint(halfPitchWidth,
				halfPitchHeight - getCornerCutoffY());
		polygon.addPoint(halfPitchWidth - getCornerCutoffX(),
				halfPitchHeight);
		polygon.addPoint(-halfPitchWidth + getCornerCutoffX(),
				halfPitchHeight);
		polygon.addPoint(-halfPitchWidth,
				halfPitchHeight - getCornerCutoffY());
		polygon.addPoint(-halfPitchWidth,
				-halfPitchHeight + getCornerCutoffY());
		return polygon;
	}

	public void saveToFile(String filename) {
		Gson gson = new Gson();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			gson.toJson(this, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadFromFile(String filename) {
		Gson gson = new Gson();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Pitch newPitch = gson.fromJson(reader, Pitch.class);
			reader.close();

			pitchWidth = newPitch.getPitchWidth();
			pitchHeight = newPitch.getPitchHeight();
			cornerCutoffX = newPitch.getCornerCutoffX();
			cornerCutoffY = newPitch.getCornerCutoffY();
			goalHeight = newPitch.getGoalHeight();

			setChanged();
			notifyObservers();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
