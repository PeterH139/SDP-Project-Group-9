/**
 * Name: Oracle.java
 * Author: Dimitar Petrov
 * Description: Predicts the next state of a point, based on a history of the points movements.
 * The prediction algorithm needs information for at least 4 points to make a prediction
 * possibly implement a prediction scheme for 4 or less points
 * **/

package pc.prediction;

import pc.prediction.Calculations.CorrectionType;
import pc.world.oldmodel.*;
import pc.world.Pitch;
import java.util.ArrayList;

public class Oracle {
	private int _boundaryTop;
	private int _boundaryBottom;
	private int _boundaryLeft;
	private int _boundaryRight;
	
	public Oracle(int top_b, int bottom_b, int left_b, int right_b){
		this._boundaryBottom = bottom_b;
		this._boundaryLeft = left_b;
		this._boundaryRight = right_b;
		this._boundaryTop = top_b;		
	}
	
	/**
	 * Returns the coordinates of the predicted state of a point.
	 * Predictions several states forward will be more inaccurate.
	 * TODO: Change frames_forward to time in ms
	 * TODO: Implement boundary calculations and detections for the corners
	 * TODO: Implement a check to see if a point is within the Pitch (use pitch.getBoundPolygon somehow?)
	 * */
	public Point2 PredictState(ArrayList<Point2> history, int frames_forward){
		if(frames_forward < 0)
			throw new IllegalArgumentException("frames_forward cannot be a negative value: "+frames_forward);
		else{
			Point2 prediction = null;
			while(frames_forward > 0){
				frames_forward--;
				//get future point
				prediction = Calculations.PredictNextPoint(history);
				//check for boundary violation
				boolean boundaryCheck = true;
				
				if(boundaryCheck){
					//correct
					
					//TOP violation
					if(prediction.getY() > _boundaryTop)
						prediction = Calculations.CalculateBounceCoordinate(prediction, CorrectionType.TOP_OR_BOTTOM, _boundaryTop);
					//Bottom violation
					if(prediction.getY() < _boundaryBottom)
						prediction = Calculations.CalculateBounceCoordinate(prediction, CorrectionType.TOP_OR_BOTTOM, _boundaryBottom);
					//LEFT violation
					if(prediction.getX() < _boundaryLeft)
						prediction = Calculations.CalculateBounceCoordinate(prediction, CorrectionType.LEFT_OR_RIGHT, _boundaryLeft);
					//Right violation
					if(prediction.getY() > _boundaryRight)
						prediction = Calculations.CalculateBounceCoordinate(prediction, CorrectionType.LEFT_OR_RIGHT, _boundaryRight);
					
				}
				//add to history
				history.add(prediction);
			}
			
			return prediction;
		}
	}
		
		public void SetBoundaries(int top, int bottom, int left, int right){
			this._boundaryBottom = bottom;
			this._boundaryLeft = left;
			this._boundaryRight = right;
			this._boundaryTop = top;		
		}
		
	}
	

