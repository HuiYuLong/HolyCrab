package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

import ecosys.simulation.Crab;
import ecosys.simulation.Fish;
import ecosys.simulation.SimulationObject;
import ecosys.simulation.Submarine;
import ecosys.simulation.Creature;
import util.Util;

public class LakePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private ArrayList<SimulationObject> objList;
	private Submarine submarine;
	private Timer restore_timer;
	private Timer t;
	private int MAX_FOOD = 12;
	public int num_fish = 12;
	public int num_crab = 6;
	private String status = "status...";
	private boolean showInfo = true;
	private boolean up, down, left, right, fire;
	
	public final static Dimension PAN_SIZE = new Dimension(1200,800);
	
	public final static int LAKE_X = 50, LAKE_Y = 50;
	public final static int LAKE_W = 1100, LAKE_H = 700;
	public static Line2D.Double rightEdge;
	public static Line2D.Double leftEdge;
	public static Line2D.Double topEdge;
	public static Line2D.Double bottomEdge;
	private Lake lake;

	public LakePanel() {
		super();
		this.setPreferredSize(PAN_SIZE);
		
		rightEdge = new Line2D.Double(LAKE_X + LAKE_W, LAKE_Y, LAKE_X+LAKE_W, LAKE_Y + LAKE_H);
		leftEdge = new Line2D.Double(LAKE_X, LAKE_Y, LAKE_X, LAKE_Y+LAKE_H);
		topEdge = new Line2D.Double(LAKE_X, LAKE_Y, LAKE_X+LAKE_W, LAKE_Y);
		bottomEdge = new Line2D.Double(LAKE_X, LAKE_Y+LAKE_H, LAKE_X+LAKE_W, LAKE_Y + LAKE_H);
		// create a lake instance
		lake = new Lake();
		
		this.objList = new ArrayList<>();
		// and new foods
		for (int i = 0; i < MAX_FOOD; i++)
			objList.add(Util.randomFood());
		
		//Create 6 predators and 12 prey creatures, and include 
		//both of them in a single ArrayList typed with their superclass
		for (int i = 0; i < num_fish; i++) {
			this.addFish();
		}
		for (int i = 0; i < num_crab; i++) {
			this.addCrab();
		}
		
		submarine = new Submarine(100, 
                PAN_SIZE.height/2, 0.6f);
		objList.add(submarine);
		
		t = new Timer(33, this);
		t.start();

		addKeyListener(new MyKeyAdapter());
		setFocusable(true);
		
		// start performing actions
		restore_timer = new Timer(5000, this);
		restore_timer.start();
		restore_timer.addActionListener(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		//Once achieving the goal (i.e. killing half of the predators), 
		//the hunter will disappear,
		if(filterCrabList(objList).size() < num_crab/2) {
			objList.clear();
			t.stop();
			restore_timer.start();
		}
		
		super.paintComponent(g);
		setBackground(Color.darkGray);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		lake.draw(g2);
		// draw objects
		for (int i=0; i < objList.size(); ++i) {
			SimulationObject obj = objList.get(i);
			obj.draw(g2);
			
			//A hunter won’t appear until the number of preys drops to half
			//of its initial population (as a result of either predating or
			//starvation), and try to rescue the rest preys by killing the predators.
			//It will repeat killing as such until the population of predators also 
			//drop to half of its initial population.
			if(filterFishList(objList).size() < num_fish/2
					&& filterCrabList(objList).size() > num_crab/2)
				submarine.required = true;
			//Once achieving the goal (i.e. killing half of the predators), the hunter
			//will disappear, and both preys and predators will, in a delay of 5 seconds,
			//respawn to their initial population level (as specified in 9) below), and 
			//the cycle of predating and killing starts again
			else {
				submarine.required = false;
			}
			
			submarine.draw(g2);
			
			//The information for preys and predators should 
			//include their energy level and speed magnitude,
			if(showInfo) {
				if(obj instanceof Submarine && submarine.required)
					obj.drawInfo(g2);
				else if(!(obj instanceof Submarine))
					obj.drawInfo(g2);
			}
		}

		drawStatusBar(g2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//and both preys and predators will, 
		//in a delay of 5 seconds, respawn to their initial population 
		//level (as specified in 9) below), and the cycle of predating 
		//and killing starts again
		restore_timer.stop();
		if(t.isRunning()) {
			// update every object in the simulation
			for (int i = 0; i < objList.size(); i++)
				objList.get(i).update(objList, this);
	
			// replace eaten food
			if (Util.countFood(objList) < MAX_FOOD && filterFishList(objList).size() > 0)
				objList.add(Util.randomFood());
	
			if(submarine.required) {
				//use keyboard control to kill predators by shooting at them
				if(fire) {
					submarine.fire();
					//Each key press should shoot only one missile rather than a stream of it
					fire = false;
				}
			}
			submarine.update(objList, this);
	
			repaint();
		}
		else {
			t.start();
			//Create 6 predators and 12 prey creatures, and include both
			//of them in a single ArrayList typed with their superclass.
			for (int i = 0; i < num_fish; i++) {
				this.addFish();
			}
			for (int i = 0; i < num_crab; i++) {
				this.addCrab();
			}
			objList.add(submarine);
		}
	}

	private class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_RIGHT)
	            right = true;
	        if (e.getKeyCode() == KeyEvent.VK_LEFT)
	            left = true;
	        if (e.getKeyCode() == KeyEvent.VK_UP)
	            up = true;
	        if (e.getKeyCode() == KeyEvent.VK_DOWN)
	            down = true;
	        if(e.getKeyCode() == KeyEvent.VK_SPACE)
	            fire = true;
	        //You should be able to toggle the display of the information 
	        //on and off for all creatures (including hunter) all at once
	        //with the d key of keyboard
			if (e.getKeyCode() == KeyEvent.VK_D) {
				if (showInfo)
					showInfo = false;
				else
					showInfo = true;
			}
		}
		
		public void keyReleased(KeyEvent e) {
			  if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			    right = false;
			  if (e.getKeyCode() == KeyEvent.VK_LEFT)
			    left = false;
			  if (e.getKeyCode() == KeyEvent.VK_UP)
			    up = false;
			  if (e.getKeyCode() == KeyEvent.VK_DOWN)
			    down = false;
			  if(e.getKeyCode() == KeyEvent.VK_SPACE)
			    fire = false;
		}
	}
	
	private void drawStatusBar(Graphics2D g) {
	    Font f = new Font("Arial", Font.BOLD, 12);
	    g.setFont(f);
	    g.setColor(Color.LIGHT_GRAY);
	    g.fillRect(0, getSize().height-24, 
	                getSize().width, 24);
	    g.setColor(Color.BLACK);
	    g.drawString(status, 12, getSize().height-8);
	}
	
	public void setStatus(String st) {
	    this.status = st;
	}
	
	public void addCrab() {
		float  x = Util.random(LAKE_X+20, LAKE_W-20);
		float y =  Util.random(LAKE_Y+20, LAKE_H-20);
		float size = Util.random(0.3, 0.6);
		this.objList.add(new Crab(x, y, size));
	}
	
	public void addFish() {
		float  x = Util.random(LAKE_X+20, LAKE_W-20);
		float y =  Util.random(LAKE_Y+20, LAKE_H-20);
		float size = Util.random(0.2, 0.5);
		this.objList.add(new Fish(x, y, size));
	}
	
	public ArrayList<SimulationObject> filterFishList(ArrayList<SimulationObject> fList) {
		ArrayList<SimulationObject> list = new ArrayList<>();
		for (SimulationObject f : fList)
			if (f instanceof Fish)
				list.add(f);
		return list;
	}
	public ArrayList<SimulationObject> filterCrabList(ArrayList<SimulationObject> fList) {
		ArrayList<SimulationObject> list = new ArrayList<>();
		for (SimulationObject f : fList)
			if (f instanceof Crab)
				list.add(f);
		return list;
	}

}
