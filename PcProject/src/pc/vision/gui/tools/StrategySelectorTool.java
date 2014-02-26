package pc.vision.gui.tools;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import pc.strategy.StrategyController;
import pc.strategy.StrategyController.StrategyType;
import pc.vision.gui.GUITool;
import pc.vision.gui.VisionGUI;
import pc.world.WorldState;

public class StrategySelectorTool implements GUITool{

	private VisionGUI gui;
	private JFrame subWindow;
	private StrategyController sc;

	public StrategySelectorTool(VisionGUI gui, StrategyController sc) {
		this.gui = gui;
		this.sc = sc;
		
		subWindow = new JFrame("Strategy Selector");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		subWindow.add(new StrategyPicker());
	}
	
	@Override
	public void activate() {
		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x,
				mainWindowBounds.y + mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		subWindow.setVisible(false);
		return true;
	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}
	
	@SuppressWarnings("serial")
	public class StrategyPicker extends JPanel{
		
		private JButton atkStrat = new JButton("Attacking");
		private JButton defStrat = new JButton("Defending");
		private JButton passStrat = new JButton("Passing");
		private JButton penStrat = new JButton("Penalty");
		private JButton marStrat = new JButton("Marking");
		private JButton pauseController = new JButton("Pause");
		private JButton startController = new JButton("Start");
		
		public StrategyPicker(){
			this.add(atkStrat);
			this.add(defStrat);
			this.add(passStrat);
			this.add(penStrat);
			this.add(marStrat);
			this.add(pauseController);
			this.add(startController);
			
			atkStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.ATTACKING);
				}
			});
			defStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.DEFENDING);
				}
			});
			passStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.PASSING);
				}
			});
			penStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.PENALTY_ATK);
				}
			});
			marStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.MARKING);
				}
			});
			pauseController.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					sc.pauseStrategyController = true;
				}
			});
			startController.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.pauseStrategyController = false;
					sc.changeToStrategy(StrategyType.DEFENDING);
				}
			});
		}
		
	}

}
