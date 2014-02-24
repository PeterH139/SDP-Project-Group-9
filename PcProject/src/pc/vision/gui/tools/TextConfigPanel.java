package pc.vision.gui.tools;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import pc.vision.YAMLConfig;

public class TextConfigPanel extends JPanel {
	public static final String CONFIG_FILENAME = "config.yml";

	private YAMLConfig yamlConfig;
	private JTextArea editor;
	private Yaml yaml = new Yaml();

	public TextConfigPanel(YAMLConfig yamlConfig) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.yamlConfig = yamlConfig;
		editor = new JTextArea(15, 40);
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		add(new JScrollPane(editor,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		add(btnPanel);
		JButton reloadBtn = new JButton("Reload");
		reloadBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				reloadConfig();
			}
		});
		btnPanel.add(reloadBtn);
		JButton saveBtn = new JButton("Save as " + CONFIG_FILENAME);
		saveBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILENAME));
					editor.write(writer);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnPanel.add(saveBtn);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					CONFIG_FILENAME));
			editor.read(reader, null);
			reader.close();
		} catch (FileNotFoundException e) {
			// Ignore silently
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!editor.getText().isEmpty())
			reloadConfig();
	}

	private void reloadConfig() {
		Object yamlData = null;
		String yamlStr = editor.getText();
		try {
			yamlData = yaml.load(yamlStr);
		} catch (YAMLException e) {
			System.err.println(e.getMessage());
			return;
		}
		try {
			yamlConfig.pushConfig(yamlData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
