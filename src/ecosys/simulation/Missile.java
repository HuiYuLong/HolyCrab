package ecosys.simulation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import main.LakePanel;
import processing.core.PVector;

public class Missile extends SimulationObject implements Mover {

	private PVector speed;
	private Ellipse2D missile;
	private boolean required = true;

	public Missile(float x, float y, float speedx, float speedy) {
		super(x, y, 10, 5, 1f);
		speed = new PVector(speedx, -speedy);
	}

	@Override
	public void draw(Graphics2D g) {
		if(required) {
			AffineTransform at = g.getTransform();
			g.translate(pos.x, pos.y);
			g.scale(size, size);
			g.rotate(speed.heading());
			g.setColor(Color.BLACK);
			g.fill(missile);
			g.setTransform(at);
		}
	}

	@Override
	public void drawInfo(Graphics2D g2) {
		// nothing to do here
	}

	@Override
	public void update(ArrayList<SimulationObject> animList, LakePanel panel) {
		Rectangle2D env = new Rectangle2D.Double(0, 0, panel.PAN_SIZE.width, panel.PAN_SIZE.height);
		
		Crab toughest_crab = null;
		for(int i = 0; i < animList.size(); i++) {
			SimulationObject anim = animList.get(i);
			if(animList.get(0) instanceof Crab && anim instanceof Crab) {
				toughest_crab = (Crab)animList.get(0);
				if(((Crab) anim).getEnergy() > toughest_crab.getEnergy()) {
					toughest_crab = (Crab) anim;
				}
			}
		}
		//The hunter should do target shooting at the predators, which would 
		//hunt per a strategy of killing the toughest predator first
		if(toughest_crab != null) {
//			toughest_pacman.setColor();
			approach(toughest_crab);
		}
		move();
		
		//The missile should also be destroyed right after it moves out of the screen
		checkCollision(panel);
		//When a missile hits a target, the missile itself should be destroyed along 
		//with the target (i.e. disappear from the screen)
		resolveCollision(animList);
	}

	@Override
	protected void setShapeAttributes() {
		missile = new Ellipse2D.Double(-dimension.width / 2, -dimension.height / 2, dimension.width, dimension.height);
	}

	@Override
	protected void setOutline() {
		outline = new Area(missile);
	}

	@Override
	protected Shape getOutline() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.scale(size, size);
		at.rotate(speed.heading());
		return at.createTransformedShape(outline);
	}

	@Override
	public void move() {
//		System.out.println(pos.x + " "+ pos.y + " "+ speed.x + " "+ speed.y);
		pos.add(speed);
	}
	
	//Implement Missile's approach(SimulationObject obj) method to make
	//it do target shooting (like a missile) always on the toughest 
	//pacman object who has the largest energy level in the list.
	@Override
	public void approach(SimulationObject obj) {
		float coef = .3f;
		PVector direction = PVector.sub(obj.getPos(), pos).normalize();
		PVector accel = PVector.mult(direction, coef);
		speed.add(accel);
	}

	@Override
	public void resolveCollision(ArrayList<SimulationObject> objList) {
		for(int i=0; i<objList.size(); i++) {
			SimulationObject obj = objList.get(i);
			if(isColliding(obj)) {
				objList.remove(obj);
				required = false;
			}
		}
		
	}
	
	private void checkCollision(LakePanel panel) {
		Rectangle2D.Double top = new Rectangle2D.Double(panel.LAKE_X,panel.LAKE_Y,panel.LAKE_W,panel.LAKE_Y);
		Rectangle2D.Double bottom = new Rectangle2D.Double(panel.LAKE_X,panel.LAKE_H,panel.LAKE_W,panel.LAKE_H);
		Rectangle2D.Double left = new Rectangle2D.Double(panel.LAKE_X,panel.LAKE_Y,panel.LAKE_X,panel.LAKE_H);
		Rectangle2D.Double right = new Rectangle2D.Double(panel.LAKE_W,panel.LAKE_Y,panel.LAKE_W,panel.LAKE_H);
		
		if(getOutline().intersects(left) && speed.x < 0
			|| getOutline().intersects(right) && speed.x > 0
			|| getOutline().intersects(top) && speed.y < 0
			|| getOutline().intersects(bottom) && speed.y > 0)
			required = false;
	}

}