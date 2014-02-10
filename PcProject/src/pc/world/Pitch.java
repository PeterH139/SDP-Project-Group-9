/**
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
		
		//TODO: Add goal areas, "left" and "right" areas of the goal area
		//Add pitch width,  height
		//X and Y boundary representations
		
		// _______________________________
		// |/     |       |       |     \|
		// |      |       |       |      |
		// |      |       |       |      |
		// |      |       |       |      |
		// |      |       |       |      |
		// |      |       |       |      |
		// |\     |       |       |     /|
		// _______________________________
		// EG EDF    AF      EAF     DF  G
		// EG: enemy goal: Y, X1, X2
		// EDF: enemy defence field: Y1, Y2, X1, X2
		// AF: attack field: Y1, Y2, X1, X2
		// EAF: enemy attack field: Y1, Y2, X1, X2
		// DF: defence field: Y1, Y2, X1, X2
		// G: goal: Y, X1, X2
		
		public AttackerFieldArea enemyAttackField, attackField;
		public DefenderFieldArea enemyDefenderField, defenderField;
		public GoalArea enemyGoalArea, goalArea;
}
