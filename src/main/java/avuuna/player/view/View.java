package avuuna.player.view;

import java.io.*;

import javax.swing.*;

public class View extends JFrame implements Serializable {
	private static final long serialVersionUID = 8317884983802007076L;

	public View(String titulo) {
		setTitle(titulo);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	protected final void lastConfig() {
		pack();
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);
	}
}