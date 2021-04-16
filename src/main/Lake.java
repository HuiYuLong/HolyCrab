/**
 * 
 */

/**
 * @author sophia
 *
 */
package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class Lake {
	private Rectangle2D.Double body;
	private RoundRectangle2D.Double BigRock;
	private RoundRectangle2D.Double smallRock1;
	private Rectangle2D.Double shape;
	private Arc2D.Double shell1;
	private Arc2D.Double shell2;
	
	private Color color;
	private Dimension dimension;
	
	public Lake() {
		this.color = new Color(51,153,255);
		
		this.body = new Rectangle2D.Double();
		this.dimension = new Dimension(60, 80);
		
		setBodyAttributes();
		setShapeAttributes();
	}
	
	private void setBodyAttributes() {
		body.setRect(LakePanel.LAKE_X, LakePanel.LAKE_Y, LakePanel.LAKE_W, LakePanel.LAKE_H);
	}

	private void setShapeAttributes() {
		BigRock = new RoundRectangle2D.Double(dimension.width*2, dimension.height*6, dimension.width*2.2, dimension.height*1.1, 100, 80);
		smallRock1 = new RoundRectangle2D.Double(dimension.width*1.5, dimension.height*6.6, dimension.width*0.9, dimension.height*0.5, 360, 30);
		shape = new Rectangle2D.Double(85, 550, 160, 20);
		shell1 = new Arc2D.Double(dimension.width*15, dimension.height*7, dimension.width*1.7, dimension.height, 100, 80, Arc2D.PIE);
		shell2 = new Arc2D.Double(dimension.width*15.3, dimension.height*7.1, dimension.width*1.2, dimension.height * 0.7, 110, -80, Arc2D.PIE);
	}
	
	public void draw(Graphics2D g) {
		
		g.setColor(color);
		g.fill(body);
		g.draw(body);


		final Color DARK_GRAY = new Color(102,102,102);
		g.setColor(DARK_GRAY);
		g.fill(BigRock);
		g.draw(BigRock);
		g.setColor(Color.gray);
		g.fill(smallRock1);
		g.draw(smallRock1);

		g.setColor(color);
		g.fill(shape);
		g.draw(shape);

		g.setColor(Color.pink);
		g.fill(shell2);
		g.draw(shell2);
		g.setColor(Color.ORANGE);
		g.fill(shell1);
		g.draw(shell1);
		
	}
}
