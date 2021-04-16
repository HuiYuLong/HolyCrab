package ecosys.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import javax.swing.Timer;

import processing.core.PVector;
import util.Util;

// This is a predator class
public class Crab extends Creature implements ActionListener{
	
	private RoundRectangle2D.Double body;
	
	private Ellipse2D.Double left_eye,right_eye,left_eye_dot,right_eye_dot;
	private Arc2D.Double leg1,leg2,leg3,leg4,leg5,leg6;
	private Arc2D.Double left_tong,right_tong;
	
	public Crab(float x, float y, float size) {
		super(x, y, 60, 80, size);
		this.color = Util.randomColor();
		FEELER_RANGE = 30.0f;
		chase_mode = false;
		chase_obj = null;
		
		// start performing actions
		chase_timer = new Timer(1000, this);
		chase_timer.start();
		chase_timer.addActionListener(this);
	}

	@Override
	protected void setShapeAttributes() {
		head = new Arc2D.Double(dimension.width/1.8, -dimension.height / 2, dimension.width / 2, dimension.height, 30, 360, Arc2D.PIE);
    	body = new RoundRectangle2D.Double(0, -dimension.height / 2, dimension.width, dimension.height, 50, 60);
		
    	left_eye = new Ellipse2D.Double(dimension.width, -dimension.height / 3.5, dimension.width / 4, dimension.width / 4);
		right_eye = new Ellipse2D.Double(dimension.width, dimension.height / 8, dimension.width / 4, dimension.width / 4);
		left_eye_dot = new Ellipse2D.Double(dimension.width*1.1, -dimension.height / 3.5, dimension.width / 10, dimension.width / 10);
		right_eye_dot = new Ellipse2D.Double(dimension.width*1.1, dimension.height / 8, dimension.width / 10, dimension.width / 10);
		left_tong = new Arc2D.Double(dimension.width/2, -dimension.height / 1.25, dimension.width / 3, dimension.height / 3, 90, 300, Arc2D.PIE);
		right_tong = new Arc2D.Double(dimension.width/2, dimension.height / 2.1, dimension.width / 3, dimension.height / 3, 320, 300, Arc2D.PIE);
		
		leg1 = new Arc2D.Double(dimension.width / 2.4, -dimension.height / 1.1, dimension.width / 10, dimension.height / 2, 0, 280, Arc2D.PIE);
		leg2 = new Arc2D.Double(dimension.width / 3.6, -dimension.height / 1.1, dimension.width / 10, dimension.height / 2, 0, 280, Arc2D.PIE);
		leg3 = new Arc2D.Double(dimension.width / 6, -dimension.height / 1.1, dimension.width / 10, dimension.height / 2, 0, 280, Arc2D.PIE);
		leg4 = new Arc2D.Double(dimension.width / 2.4, dimension.height / 2.2, dimension.width / 10, dimension.height / 2, 0, 280, Arc2D.PIE);
		leg5 = new Arc2D.Double(dimension.width / 3.6, dimension.height / 2.2, dimension.width / 10, dimension.height / 2, 360, 280, Arc2D.PIE);
		leg6 = new Arc2D.Double(dimension.width / 6, dimension.height / 2.2, dimension.width / 10, dimension.height / 2, 360, 280, Arc2D.PIE);
	}

	@Override
	public void draw(Graphics2D g) {
		// transformation
		AffineTransform at = g.getTransform();
		g.translate(pos.x, pos.y);
		g.rotate(speed.heading());
		g.scale(size, size);
		if (speed.x < 0)
			g.scale(1, -1);
		
		//body
		//Use appropriate visual to show the creature’s 
		//different states of energy level including dying
		// When it's half hungry, it's body will turn into orange
		// When it's sick, the whole body:
		// including tong,leg and eye will turn into gray
		if (state == SICK) 
			g.setColor(Color.LIGHT_GRAY);
		else if (state == HUNGRY) {
			g.setColor(Color.ORANGE);
		}
		else g.setColor(Color.red);
		g.fill(body);
		g.draw(body);
		
		//tong
		if (state == SICK) 
			g.setColor(Color.LIGHT_GRAY);
		else g.setColor(Color.red);
		g.fill(left_tong);
		g.draw(left_tong);
		g.fill(right_tong);
		g.draw(right_tong);
		
		//leg
		g.fill(leg1);
		g.draw(leg1);
		g.fill(leg2);
		g.draw(leg2);
		g.fill(leg3);
		g.draw(leg3);
		g.fill(leg4);
		g.draw(leg4);
		g.fill(leg5);
		g.draw(leg5);
		g.fill(leg6);
		g.draw(leg6);
		
		//eye	
		g.fill(left_eye);
		g.fill(right_eye);
		

		if (state == HALF_FULL) {
			g.setColor(Color.CYAN);
		} else if (state == HUNGRY) {
			g.setColor(Color.YELLOW);
		} else if (state == SICK) {
			g.setColor(Color.gray);
		}
		else {
			g.setColor(Color.black); 
		}
		g.fill(left_eye_dot);
		g.fill(right_eye_dot);
		
//		// draw feeler
//		g.setColor(Color.PINK);
//		g.draw(feeler);
//		g.fill(feeler);
		
		g.setTransform(at);
		
//		// draw 2D bounding box
//		g.setColor(Color.PINK);
//		g.draw(getOutline());
	}

	@Override
	public void resolveCollision(ArrayList<SimulationObject> objList) {
		for(SimulationObject obj : objList) {
			if(obj != this && isColliding(obj) && (obj instanceof Crab)) {
				moveAway(obj);
			}
			//When hunter moves around, both preys and predators will try to
			//avoid bumping with the hunter using either an FOV or feeler
			//while pursuing their food or escape from a predator
			else if( (detect(obj)||isColliding(obj)) && obj instanceof Submarine) {
				moveAway(obj);
			}
			else if(detect(obj) && !chase_mode && obj instanceof Fish) {
				chase_mode = true;
				chase_obj = obj;
				chase_timer.start();
			}
			else if(chase_mode && obj instanceof Fish) {
				chase(obj);
			}
		}
		
	}
	
	@Override
	protected void traceBestFood(ArrayList<SimulationObject> fList) {
		if (fList.size() > 0) {
			// find 1st target
			SimulationObject target = fList.get(0);
			float distToTarget = PVector.dist(pos, target.getPos());

			// find the closer one
			for (SimulationObject f : fList)
				if (PVector.dist(pos, f.getPos()) < distToTarget) {
					target = f;
					distToTarget = PVector.dist(pos, target.getPos());
				}

			// make animal follow this target
			this.approach(target);
		}
	}

	@Override
	protected boolean eatable(SimulationObject food) {
		return (food instanceof Fish);
	}

	@Override
	protected void setOutline() {
		outline = new Area(head);
		outline.add(new Area(body));
		outline.add(new Area(left_eye));
		outline.add(new Area(right_eye));
		outline.add(new Area(left_tong));
		outline.add(new Area(right_tong));
		outline.add(new Area(leg1));
		outline.add(new Area(leg2));
		outline.add(new Area(leg3));
		outline.add(new Area(leg4));
		outline.add(new Area(leg5));
		outline.add(new Area(leg6));
	}

	@Override
	protected Shape getOutline() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.rotate(speed.heading());
		at.scale(size, size);
		return at.createTransformedShape(outline);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		chase_mode = false;
		chase_obj = null;
		chase_timer.stop();
	}
}
