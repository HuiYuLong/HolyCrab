package ecosys.simulation;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import main.LakePanel;
import processing.core.PVector;

public abstract class SimulationObject {
	protected PVector pos;
	protected Dimension dimension;
	protected float size;
	protected Area outline;
	
	public SimulationObject(float x, float y, int w, int h, float size) {
		this.pos = new PVector(x,y);
		this.dimension = new Dimension(w,h);
		this.size = size;
		setShapeAttributes();
		setOutline();
	}
	
	public Rectangle2D getBoundingBox() {
		return getOutline().getBounds2D();
	}
	
	public float getSize() {
		return size;
	}
	
	public PVector getPos() {
		return pos;
	}
	
	protected boolean isColliding(SimulationObject other) {
		return (getOutline().intersects(other.getBoundingBox()) &&
				other.getOutline().intersects(getBoundingBox()) );
	}
	
	public PVector wallPushForce() {
		PVector force = new PVector();
		float wallCoef = 50.0f;
		
		// compute force based on distance from edge
		double distance = 0;
		int max_size = Math.max(dimension.height, dimension.width);
		distance = LakePanel.leftEdge.ptLineDist(pos.x, pos.y) - max_size * size;
		force.add(new PVector((float) (+wallCoef / Math.pow(distance, 2)), 0.00f)); //left wall force
		
		distance = LakePanel.rightEdge.ptLineDist(pos.x, pos.y) - max_size * size;
		force.add(new PVector((float) (-wallCoef / Math.pow(distance, 2)), 0.00f)); //right wall force
		
		distance = LakePanel.topEdge.ptLineDist(pos.x, pos.y) - max_size * size;
		force.add(new PVector(0.00f, (float) (+wallCoef / Math.pow(distance, 2))));	//top wall force
		
		distance = LakePanel.bottomEdge.ptLineDist(pos.x, pos.y) - max_size * size;
		force.add(new PVector(0.00f, (float) (-wallCoef / Math.pow(distance, 2))));	//bottom wall force
		
		return force;
	}
	
	//Change the superclass in A3P1 into an abstract class, 
	//and make at least its draw method to be an abstract one
	public abstract void draw(Graphics2D g2);
	public abstract void drawInfo(Graphics2D g2);
	public abstract void update(ArrayList<SimulationObject> objList, LakePanel panel);
	protected abstract void setShapeAttributes();
	protected abstract void setOutline();
	protected abstract Shape getOutline();
}
