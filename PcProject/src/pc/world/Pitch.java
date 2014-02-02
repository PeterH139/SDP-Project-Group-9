/*
 * Name : Pitch.java
 * Author : Dimitar Petrov
 * Description : Represents the playing field, 
 * specifies the attacker and defender areas
 * 
 * */
package pc.world;

import java.util.ArrayList;

public class Pitch {
		public ArrayList<double[]> pitchArea;
		
		//Friendly Defender Area
		public ArrayList<double[]> defenderArea;
		//Friendly Attacker Area
		public ArrayList<double[]> attackerArea;
		
		//Enemy Defender Area
		public ArrayList<double[]> enemyDefenderArea;
		//Enemy Attacker Area
		public ArrayList<double[]> enemyAttackerArea;
}
