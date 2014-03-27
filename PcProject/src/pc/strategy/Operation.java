package pc.strategy;

public class Operation {

	public enum Type{
		DO_NOTHING, ATKTRAVEL, ATKMOVEKICK, ATKROTATE, ATKPREPARE_CATCH, ATKCATCH, ATKKICK, ATKARC_LEFT, ATKARC_RIGHT, DEFTRAVEL, DEFROTATE, DEFPREPARE_CATCH, DEFCATCH, DEFKICK, ROTATENMOVE, DEFARC_LEFT, DEFARC_RIGHT, ATKCONFUSEKICKRIGHT, ATKCONFUSEKICKLEFT
	}
	
	public Type op = Type.DO_NOTHING;
	public double radius;
	public int travelDistance, travelSpeed, rotateBy, rotateSpeed;
	
}
