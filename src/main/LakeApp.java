package main;

import javax.swing.JFrame;

public class LakeApp extends JFrame {

	private static final long serialVersionUID = 6457792220456140992L;

	public LakeApp(String title) {
		super(title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// instantiating our BallPanel
		LakePanel panel = new LakePanel();
		
		// adding it to the current frame
		this.add(panel);
		
		// displaying the frame
		this.setVisible(true);
		this.pack();
	}

	public static void main(String[] args) {
		new LakeApp("My Interactive Lake App");
	}

}
