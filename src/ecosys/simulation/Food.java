package ecosys.simulation;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.util.ArrayList;

import main.LakePanel;

public class Food extends SimulationObject {

	private Arc2D.Double seaweed; // geometric shape
	private Color foodColor; // shape color
	final static Color DARKER_GREEN = new Color(0, 140, 0);

	public Food(float x, float y, float size) {
		super(x, y, 10, 10, size);
		this.foodColor = DARKER_GREEN;
	}

	@Override
	public void draw(Graphics2D g) {
		AffineTransform at = g.getTransform();

		g.translate(pos.x, pos.y);
		g.scale(size, size);

		// draw food
		g.setColor(foodColor);
		g.fill(seaweed);

		g.setTransform(at);

		drawInfo(g);
	}

	@Override
	protected void setShapeAttributes() {
//		this.seaweed = new Ellipse2D.Double(-dim.width / 2, -dim.height / 2, dim.width, dim.height);

		seaweed = new Arc2D.Double(-2, -dimension.height / 2, dimension.width / 2, dimension.height, 30, 160, Arc2D.PIE);
		outline = new Area(seaweed);
	}

	@Override
	public void setOutline() {
		outline = new Area(seaweed);
	}

	@Override
	public Shape getOutline() {
		AffineTransform at = new AffineTransform();
		at.translate(pos.x, pos.y);
		at.scale(size, size);
		return at.createTransformedShape(outline);
	}

	@Override
	public void update(ArrayList<SimulationObject> objList, LakePanel panel) {
		// nothing, food don't need to be updated
	}

	public void drawInfo(Graphics2D g) {
//		AffineTransform at = g.getTransform();
//		g.translate(pos.x, pos.y);
//		g.setColor(Color.WHITE);
//
//		Font f = new Font("Arial", Font.BOLD, 12);
//		g.setFont(f);
//		String st = String.format("%.2f", size);
//		FontMetrics metrics = g.getFontMetrics(f);
//		g.drawString(st, -metrics.stringWidth(st) / 2, -dimension.height / 2 * size - 5);
//		g.setTransform(at);
	}

}
