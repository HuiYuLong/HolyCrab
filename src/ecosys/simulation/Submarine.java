package ecosys.simulation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import main.LakePanel;
import processing.core.PVector;
import util.Util;

//make it a subclass to the abstract superclass as well so as to take
//advantage of the shared properties and functionality
public class Submarine extends SimulationObject implements Mover {

	private RoundRectangle2D.Double body, head;
	private Ellipse2D.Double window1,window2;
	protected PVector speed; // speed
	protected float speedMag; // speed limit
	public boolean required;
	private float health_point = 100;

	private ArrayList<Missile>missileList = new ArrayList<Missile>(); 
	
	public Submarine(float x, float y, float size) {
		super(x, y, 60, 80, size);

		speedMag = 5f;
		speed = Util.randomPVector(speedMag);
	}
	
	@Override
	protected void setShapeAttributes() {
    	body = new RoundRectangle2D.Double(0, -dimension.height / 2, dimension.width, dimension.height, 50, 60);
    	head = new RoundRectangle2D.Double(dimension.width/2.5, -dimension.height/4, dimension.width/1.2, dimension.height/2, 20, 60);
    	window1 = new Ellipse2D.Double(dimension.width/3, -dimension.height / 3, dimension.width / 3, dimension.width / 3);
    	window2 = new Ellipse2D.Double(dimension.width/3, dimension.height / 9, dimension.width / 3, dimension.width / 3);
	}

	@Override
	public void move() {

		//The hunter will move up and down along a path near an edge of the 
		//environment on its own (i.e. move autonomously without keyboard control)
		speed.normalize().mult(speedMag);
		speed.x = 0.02f;
		pos.add(speed);
	}

	@Override
	public void approach(SimulationObject target) {
		float coef = .3f; // coefficient of acceleration relative to maxSpeed
		PVector direction = PVector.sub(target.getPos(), pos).normalize();
		PVector accel = PVector.mult(direction, speedMag * coef);
		speed.add(accel);
	}

	@Override
	public void resolveCollision(ArrayList<SimulationObject> objList) {
		// TODO: remove missile after colliding with one of the crab
	}

	@Override
	public void draw(Graphics2D g) {
		//Add a hunter class to your ecosystem, in appropriate figure
		// The submarine is not a creature but a simulate object that
		// has significant visual features on its appearance
		if(required) {
			// transformation
			AffineTransform at = g.getTransform();
			g.translate(pos.x, pos.y);
			g.rotate(speed.heading());
			g.scale(size, size);
			if (speed.y < 0)
				g.scale(1, -1);
			
			//head
			g.setColor(Color.BLACK);
			g.draw(head);
			g.setColor(Color.YELLOW);
			g.fill(head);
			
			//body
			g.fill(body);
			g.setColor(Color.BLACK);
			g.draw(body);
			
			//windows
			g.setColor(new Color(51,153,255));
			g.fill(window1);
			g.fill(window2);
			g.setColor(Color.BLUE);
			g.draw(window1);
			g.draw(window2);
			
			g.setTransform(at);
		}
		for (Missile m : missileList)
			m.draw(g);
	}
	
	public void fire(){
		PVector mSpeed = PVector.fromAngle(speedMag, speed.copy()).mult(5);

		//Add a missile object to the list for shooting
		missileList.add(new Missile(pos.x, pos.y, mSpeed.x, mSpeed.y));
	}
	
	@Override
	public void drawInfo(Graphics2D g) {
		AffineTransform at = g.getTransform();
		g.translate(pos.x, pos.y);

		String st1 = "Health Points: " + String.format("%.2f", health_point);

		Font f = new Font("Courier", Font.PLAIN, 12);
		FontMetrics metrics = g.getFontMetrics(f);

		float textWidth = metrics.stringWidth(st1);
		float textHeight = metrics.getHeight();
		float margin = 8, spacing = 2;

		g.setColor(new Color(255, 255, 255, 60));
		g.fillRect((int) (-textWidth / 2 - margin),
				(int) (-dimension.height * size * .75f - textHeight * 5f - spacing * 4f - margin * 2f),
				(int) (textWidth + margin * 2f), (int) (textHeight * 5f + spacing * 4f + margin * 2f));

		g.setColor(Color.blue.darker());
		g.drawString("submarine", -metrics.stringWidth("submarine") / 2,
				-dimension.height * size * .75f - margin - (textHeight + spacing) * 4f);
		g.setColor(Color.black);
		g.drawString(st1, -textWidth / 2.6f, -dimension.height * size * .75f - margin - (textHeight + spacing) * 2f);
		
		g.setTransform(at);
	}

	@Override
	public void update(ArrayList<SimulationObject> objList, LakePanel panel) {
//		resolveCollision(objList);
		
		//Return the wall push forces and compute accelerations
		PVector wallSteerAccel = wallPushForce().div((float)size);
		//Make it turn per wall steering accelerations
		speed.add(wallSteerAccel);
		move();
		
		// move each of the missiles and check collision with animals
		for (Missile m : missileList) {
			m.update(objList, panel);
		}
		
		//Use status bar within window’s frame area with appropriately
		//formatted message to indicate appearance and disappearance
		//of hunter, and dynamic message about how many more predators
		//it needs to kill before restoring the balance of the ecosystem
		String status = "submarine required";
		int kill_crabs;
		String st;
		kill_crabs = panel.filterCrabList(objList).size() - panel.num_crab/2;
		if(kill_crabs < 0)
			kill_crabs = 0;

		if(!required) {
			status = "submarine not required";
			st = String.format("%s", status);
		}
		else
			st = String.format("%s: need to kill %d more crabs before retoring the balance of the ecosystem", status, kill_crabs);
		panel.setStatus(st);

	}

	@Override
	protected void setOutline() {
		outline = new Area(body);
		outline.add(new Area(head));
		outline.add(new Area(window1));
		outline.add(new Area(window2));
		
	}

	@Override
	protected Shape getOutline() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(speed.heading());
		at.scale(size, size);
		return at.createTransformedShape(outline);
	}

	
//	public void hunt(ArrayList<SimulationObject> objList) {
////System.out.println("hunt");
//for (int i = 0; i < objList.size(); i++) {
//	SimulationObject obj = objList.get(i);
//	if (obj instanceof Crab) {
//		speed.mult((float) 0.5);
//		approach(obj);
//		speed.limit(speedMag);
//		pos.add(speed);
//	}
//}
//}
}
