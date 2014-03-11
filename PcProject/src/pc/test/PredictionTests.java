/**
 * Name: PredictionTests.java
 * Author: Dimitar Petrov
 * Description: Provides tests for the computation methods in PredictionTests
 * Yes, i know it is a dumb way to test, I will change it when I have time.
 * */

package pc.test;
import pc.prediction.*;
import pc.world.oldmodel.Point2;

import java.util.ArrayList;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
public class PredictionTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Testing point distance calculation
		Point2 a1 = new Point2(10.0f, 20.0f);
		Point2 a2 = new Point2(20.0f, 30.0f);
		Point2 a3 = new Point2(30.0f, 40.0f);
		Point2 a4 = new Point2(12.154f, 35.6f);
		Point2 a5 = new Point2(78.5689f, 123.485f);
		
		Point2 b1 = new Point2(11.0f, 0.0f);
		Point2 b2 = new Point2(21.0f, 10.0f);
		Point2 b3 = new Point2(31.0f, 20.0f);
		Point2 b4 = new Point2(65.75f, 23.69f);
		Point2 b5 = new Point2(215.2835f, 65.98f);
		
		float distance_a1_b1 = Calculations.GetDistance(a1, b1);
		float distance_a1_b2 = Calculations.GetDistance(a1, b2);
		float distance_a2_b3 = Calculations.GetDistance(a2, b3);
		
		float t_distance_a1_b1 = (float)Math.sqrt(401);
		float t_distance_a1_b2 = (float)Math.sqrt(221);
		float t_distance_a2_b3 = (float)Math.sqrt(221);
		
		if(distance_a1_b1 != t_distance_a1_b1)
			System.out.println("distance_a1_b1 fails");
		else if(distance_a1_b2 != t_distance_a1_b2)
			System.out.println("distance_a1_b2 fails");
		else if(distance_a2_b3 != t_distance_a2_b3)
			System.out.println("distance_a2_b3 fails");
		else
			System.out.println("Distance between points tests passed");
		
		ArrayList<Point2> travelPath = new ArrayList<Point2>();
		//Real trajectory test
		Point2 p1 = new Point2(437.75714f, 209.81429f);
		Point2 p2 = new Point2(435.75714f, 210.2f);
		Point2 p3 = new Point2(431.72562f, 209.0061f);
		Point2 p4 = new Point2(189.83582f, 141.14925f);
		Point2 p5 = new Point2(166.12643f, 135.06897f);
		Point2 p6 = new Point2(139.70946f, 130.41216f);
		Point2 p7 = new Point2(111.51923f, 123.21795f);
		Point2 p8 = new Point2(96.982605f, 119.4087f);
		Point2 p9 = new Point2(96.982605f, 119.4087f);
		
		travelPath.add(p1);
		travelPath.add(p2);
		travelPath.add(p3);
		travelPath.add(p4);
		travelPath.add(p5);
		travelPath.add(p6);
		travelPath.add(p7);
		travelPath.add(p8);
		travelPath.add(p9);
		
		/*
		float[] velocities = new float[travelPath.size()];
		float[] data = new float[3];
		float prediction = 0;
		//print out distance traveled/velocities:
		for(int i=0; i < travelPath.size()-1; i++){
			velocities[i] = Calculations.GetDistance(travelPath.get(i), travelPath.get(i+1));
			
			data[2] = data[1];
			data[1] = data[0];
			data[0] = velocities[i];
			if(data[2] != 0)
				prediction = Calculations.LinearPrediction(data);
			if(i > 1)
				System.out.println("Prediction: "+prediction+" Actual distance: "+velocities[i-1]);
		}
		*/
		
		//Point2 t_pred = Calculations.PredictNextPoint(travelPath);
		//System.out.println(t_pred.getX()+" "+t_pred.getY());
		//Linear prediction tests
		//float[] trajectory1 = {10f, 8f, 7f, 6.75f }; 
		//System.out.println(Calculations.LinearPrediction(trajectory1));
		
		
		//boundary prediction tests, Right side
		/*
		Point2 r_1 = new Point2(5f,5f);
		Point2 r_2 = new Point2(10f,10f);
		Point2 corr = Calculations.CalculateBounceCoordinate(r_2, Calculations.CorrectionType.LEFT_OR_RIGHT, 7.5f);
		System.out.println("X="+corr.getX()+" Y="+corr.getY());
		corr = Calculations.CalculateBounceCoordinate(r_2, Calculations.CorrectionType.TOP_OR_BOTTOM, 7.5f);
		System.out.println("X="+corr.getX()+" Y="+corr.getY());
		*/
		
		Oracle tester = new Oracle(0, 100, 100, 0);
		Point2 pred = tester.PredictState(travelPath, 1);
		System.out.print(pred.getX() + " " + pred.getY());
		 /* *
		try {
			String result = RunLinearPredictionAnalysis("/afs/inf.ed.ac.uk/user/s11/s1109056/Documents/test.txt");
			System.out.println(result);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
	
	
 	private static String RunLinearPredictionAnalysis(String fileName) throws SAXException{
		//Open file and parse as an xml document
		File source = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			try{
				doc = dBuilder.parse(source);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(doc != null)
			doc.normalize();
		
		//Extract each message, get values and parse as a Point
		ArrayList<Point2> allPointSamples = new ArrayList<Point2>();
		NodeList messages = doc.getElementsByTagName("message");
		for(int i=0; i < messages.getLength(); i++){
			Node messageNode = messages.item(i);
			
			if (messageNode.getNodeType() == Node.ELEMENT_NODE) {
				Element message = (Element) messageNode;
				//extracting message, creating point, adding it to list
				String parseMe = message.getTextContent();
				String[] parts = parseMe.split(" ");
				Point2 newPoint = new Point2(Float.valueOf(parts[0].substring(2)), Float.valueOf(parts[1].substring(2)));
				allPointSamples.add(newPoint);
			}			
		}
		
		//Calculate velocity for each point
		ArrayList<Float> velocities = new ArrayList<Float>();
		for(int i=0; i < allPointSamples.size()-1; i++){
			velocities.add(Calculations.GetDistance(allPointSamples.get(i), allPointSamples.get(i+1)));
		}
		//run prediction analysis for all points, starting with the 3rd
		long count_20 = 0, count_20_50 = 0, count_50 = 0; 
		float data[] = {0, velocities.get(0), velocities.get(1)};
		for(int i=2; i < velocities.size()-1; i++){
			data[0] = data[1];
			data[1] = data[2];
			data[2] = velocities.get(i);
			
			float prediction = Calculations.LinearPrediction(data);
			
			//after each computation, compare against the actual velocity
			float diff = Math.abs(prediction - velocities.get(i+1));
			if(diff < 20){
				count_20 ++;
			}
			else if(diff > 20 && diff < 50){
				count_20_50 ++;				
			}
			else if(diff > 50){
				count_50 ++;				
			}
			
		}
		//if diff of 20
			//ok
		//if diff of 20 - 50
			//not ok		
		//if diff of 50+
			//very bad
		long totalCount = count_20 + count_20_50 + count_50;
		
		String statistics = String.format("Difference of 20: %d \r\n" +
										  "Difference of 20-50: %d, \r\n" +
										  "Difference of more than 50: %d",
										  count_20,
										  count_20_50,
										  count_50);
		//output overall statistics in string format
		return statistics;
	}

}