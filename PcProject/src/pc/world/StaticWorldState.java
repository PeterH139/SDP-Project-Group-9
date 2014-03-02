package pc.world;

import java.awt.Point;

public class StaticWorldState {
	private Point ball;
	private DirectedPoint attacker, defender;
	private DirectedPoint enemyAttacker, enemyDefender;

	public Point getBall() {
		return ball;
	}

	public void setBall(Point ball) {
		this.ball = ball;
	}

	public DirectedPoint getAttacker() {
		return attacker;
	}

	public void setAttacker(DirectedPoint attacker) {
		this.attacker = attacker;
	}

	public DirectedPoint getDefender() {
		return defender;
	}

	public void setDefender(DirectedPoint defender) {
		this.defender = defender;
	}

	public DirectedPoint getEnemyAttacker() {
		return enemyAttacker;
	}

	public void setEnemyAttacker(DirectedPoint enemyAttacker) {
		this.enemyAttacker = enemyAttacker;
	}

	public DirectedPoint getEnemyDefender() {
		return enemyDefender;
	}

	public void setEnemyDefender(DirectedPoint enemyDefender) {
		this.enemyDefender = enemyDefender;
	}

}
