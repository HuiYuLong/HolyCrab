/**
 * 
 */

/**
 * @author sophia
 *
 */
package ecosys.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.Timer;

import processing.core.PVector;
import util.Util;

// This subclass is for prey creatures
public class Fish extends Creature implements ActionListener{
	
	// These properties are unique to fish
	private Arc2D.Double body;
	private Arc2D.Double head;
	private Ellipse2D.Double eye;
	private Polygon tail;
	
	private boolean escape_mode;
	private Timer escape_timer;
	private SimulationObject escape_obj;
	
	public Fish(float x, float y, float size) {
		super(x, y, 60, 80, size);
		this.color = Util.randomColor();
		FEELER_RANGE = 20.0f;
		escape_mode = false;
		escape_obj = null;
		
		// start performing actions
		escape_timer = new Timer(1000, this);
		escape_timer.start();
		escape_timer.addActionListener(this);
	}
	
	protected void setShapeAttributes() {
        
        body = new Arc2D.Double(dimension.width/4, -dimension.height/5, dimension.height, dimension.width/2, 30, 360, Arc2D.PIE);
		head = new Arc2D.Double(dimension.width/1.6, -dimension.height/5, dimension.width, dimension.width/2, 270, 180, Arc2D.PIE);
        eye = new Ellipse2D.Double(dimension.width+dimension.width/3, -dimension.height/8, dimension.width/10, dimension.width/10);
		
		int[] px = {dimension.width/4, -dimension.width/4, -dimension.width/10, -dimension.width/4};
		int[] py = {0, -dimension.height/4, 0, dimension.height/4};
		tail = new Polygon(px, py, px.length);
		
		outline = new Area(body);
		outline.add(new Area(head));
		outline.add(new Area(eye));
		outline.add(new Area(tail));
	}

	public void draw(Graphics2D g) {

		//transformation
		AffineTransform af = g.getTransform();		
		g.translate((int)pos.x, (int)pos.y);
		g.rotate(speed.heading());
		g.scale(size, size);
		if (speed.x < 0) g.scale(1, -1);


		if (state == SICK) 
			g.setColor(Color.LIGHT_GRAY);
		else if (state == HUNGRY)
			g.setColor(Color.ORANGE);
		else g.setColor(Color.yellow);
		g.fill(body);
		g.draw(body);
		
		g.setColor(color);
		g.fill(head);
		g.draw(head);
		
		g.fill(tail);
		g.draw(tail);
		
		g.setColor(Color.black);
		g.fill(eye);
		g.draw(eye);
	
		// draw feeler
		g.setColor(Color.RED);
		g.draw(feeler);
		g.fill(feeler);
		
		// change back to the default
		g.setTransform(af);		
		
//		// draw 2D bounding box
//		g.setColor(Color.PINK);
//		g.draw(getBoundary());
	}
	
	@Override
	public void resolveCollision(ArrayList<SimulationObject> objList) {
		for(SimulationObject obj : objList) {
			if(obj != this && isColliding(obj) && (obj instanceof Fish)) {
				moveAway(obj);
			}
			//When hunter moves around, both preys and predators will try to
			//avoid bumping with the hunter using either an FOV or feeler
			//while pursuing their food or escape from a predator
			else if((detect(obj)||isColliding(obj)) && obj instanceof Submarine) {
				moveAway(obj);
			}
			else if(detect(obj) && !escape_mode && obj instanceof Crab) {
				escape_mode = true;
				escape_obj = obj;
				escape_timer.start();
			}
			else if(escape_mode && obj instanceof Crab) {
				escape(obj);
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
		return (food instanceof Food);
	}
	

	@Override
	protected void setOutline() {
		outline = new Area(head);
		outline.add(new Area(body));
		outline.add(new Area(eye));
		outline.add(new Area(tail));
		
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
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		escape_mode = false;
		escape_obj = null;
		escape_timer.stop();
	}
	
}
