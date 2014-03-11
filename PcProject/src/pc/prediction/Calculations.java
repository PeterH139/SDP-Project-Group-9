package pc.prediction;
import pc.world.oldmodel.Point2;

import java.util.ArrayList;

public final class Calculations {
	
	/**
	 * 
	 * @return returns the distance between 2 points in a 2D plane
	 * */
	public static float GetDistance(Point2 a, Point2 b){
		double x1,x2,y1,y2;
		x1 = (double) a.getX();
		x2 = (double) b.getX();
		y1 = (double) a.getY();
		y2 = (double) b.getY();
		
		double distance = Math.sqrt(Math.pow((y1-y2),2)+Math.pow((x1-x2),2));
		return (float) distance;
	}
	
	/**
	 * returns the slope of a line determined by two points
	 * */
	public static float GetSlopeOfLine(Point2 a, Point2 b){
		return (a.getX()-b.getY())/(a.getY()-b.getY());		
	}
	/**
	 * [0] is the distance in moment t (current time - 1)
	 * [1] is the distance in moment t-1 (current time - 2)
	 * [2] is the distance in moment t-2 (current time - 3)
	 * */
	public static float LinearPrediction(float[] data){
		float v1,v2,v3,a1_2,a2_3, acc_decay;
		v1 = data[data.length-1];
		v2 = data[data.length-2];
		v3 = data[data.length-3];
	    a1_2 = Math.abs(v2-v1);
	    a2_3 = Math.abs(v3-v2);
	    acc_decay = a2_3 - a1_2;
	   // System.out.println(a1_2);
	    //System.out.println(a2_3);
	    //System.out.println(acc_decay);
		if(v1 - (a1_2 - acc_decay) > 0)
			return v1 - (a1_2 - acc_decay);
		else
			return 0;
	}
	
	public enum CorrectionType{
		TOP_OR_BOTTOM, LEFT_OR_RIGHT, 		
	}
	
	/**
	 * This method will calculate the coordinates of the end point, if its trajectory indicates it will
	 * bounce off a wall
	 * TODO: implement support for the corners
	 * */
	public static Point2 CalculateBounceCoordinate(Point2 prediction, CorrectionType type, float boundary){
		float c = prediction.getX(), d = prediction.getY();
		if(type == CorrectionType.TOP_OR_BOTTOM){
			float t = Math.abs(d - boundary);
			float y = Math.abs(d - 2*t);
			Point2 correction = new Point2(c,y);
			return correction;
		}
		else if(type == CorrectionType.LEFT_OR_RIGHT){
			float t = Math.abs(boundary - c);
			float x = Math.abs(c - 2*t);
			Point2 correction = new Point2(x,d);
			return correction;			
		}
		return null;
	}
	
	public static Point2 GetPointViaDistance(float distance, Point2 a, Point2 b){
		//odd case where a == b
		if(a.getX() == b.getX() && a.getY() == b.getY())
			return a;
		
		float dist = GetDistance(a,b);
		float sinA = Math.abs(a.getY()-b.getY())/dist;
		float cosA = Math.abs(a.getX()-b.getX())/dist;
		
		float x = b.getX()+cosA*distance;
		float y = b.getY()+sinA*distance;
		
		Point2 pred = new Point2(x,y);		
		return pred;		
	}
	
	
	public static Point2 PredictNextPoint(ArrayList<Point2> history){
		if(history.size() < 4)
			throw new IllegalArgumentException("Cannot make a prediction based on less than 4 points");
		int size = history.size();
		//compute distance traveled for the last 4 points
		float[] distances = new float[4];
		distances[0] = GetDistance(history.get(size - 4), history.get(size-3));
		distances[1] = GetDistance(history.get(size - 3), history.get(size-2));
		distances[2] = GetDistance(history.get(size - 2), history.get(size-1));
		//System.out.println(String.format("%f %f %f",distances[0], distances[1], distances[2]));
		//Get predicted distance
		float prediction = LinearPrediction(distances);
		//System.out.println(prediction);
		Point2 pred = GetPointViaDistance(prediction, history.get(size-2), history.get(size-1));
		
		
		return pred;
	}
}
