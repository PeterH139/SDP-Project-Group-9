package pc.prediction;
import pc.world.Point;
import java.util.ArrayList;

public final class Calculations {
	
	/**
	 * 
	 * @return returns the distance between 2 points in a 2D plane
	 * */
	public static float GetDistance(Point a, Point b){
		double x1,x2,y1,y2;
		x1 = (double) a.getX();
		x2 = (double) b.getX();
		y1 = (double) a.getY();
		y2 = (double) b.getY();
		
		double distance = Math.sqrt(Math.pow((y1-y2),2)+Math.pow((x1-x2),2));
		return (float) distance;
	}
	
	
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
		
		return v1 - (a1_2 - acc_decay);
	}
}
