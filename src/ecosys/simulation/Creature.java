package ecosys.simulation;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.Timer;

import main.LakePanel;
import processing.core.PVector;
import util.Util;

public abstract class Creature extends SimulationObject implements Mover, ActionListener{

	protected PVector speed; // speed
	protected float speedMag; // speed limit
	//Incorporate an energy model along the line of FSM for 
	//your creatures (including both preys and predators).
	protected float energy; // energy
	protected final float FULL_ENERGY = 100;
	protected final float EMPTY_ENERGY = 0;
	protected float engGainRatio = 10; // Default energy gained per food size unit
	protected float engLossRatio = FULL_ENERGY / (30 * 15); // Default energy loss per frame
	protected float sizeGrowRatio = 0.0001f; // size growth ratio per extra energy unit

	protected Color color; // featured color
	protected Arc2D.Double head; // the original body
	
	protected PVector feelerVector;
	protected Line2D feeler;
	protected static float FEELER_RANGE;
	protected boolean chase_mode;
	protected Timer chase_timer;
	protected SimulationObject chase_obj;
	private Timer death_timer;
	private boolean noEnergy;

	// FSM states
	//The FSM employed must use a state variable
	//to refer to a state among different states 
	//specified by you using constants
	protected int state;
	protected final int HUNGRY = 0;
	protected final int HALF_FULL = 1;
	protected final int FULL = 2;
	protected final int OVER_FULL = 3;
	protected final int SICK = 4;
	protected final int DEATH = -1;

	public Creature(float x, float y, int w, int h, float size) {
		super(x, y, w, h, size);
		speedMag = 3f;
		speed = Util.randomPVector(speedMag);
		state = FULL;
		energy = FULL_ENERGY;
		noEnergy = false;

		feelerVector = new PVector();
		feeler = new Line2D.Double();
		
		// start performing actions
		death_timer = new Timer(1000, this);
		death_timer.start();
		death_timer.addActionListener(this);
	}

	public void move() {

		speed.normalize().mult(speedMag);
		// Make the sick packman to slow down their movement gradually
		// (i.e. become ever slower) before they die.
		//Sick creatures move at half of their speed
		if(state == SICK) {
			speed.div(2);
		}
		
		// apply speed to position
		pos.add(speed);

		//When a creature moves, it loses energy at certain rate
		//based on their size AND their speed
		energy -= (engLossRatio*size*speed.mag());
	}

	public void approach(SimulationObject target) {
		float coef = .3f; // coefficient of acceleration relative to maxSpeed
		PVector direction = PVector.sub(target.getPos(), pos).normalize();
		PVector accel = PVector.mult(direction, speedMag * coef);
		speed.add(accel);
	}
	
	protected void updateFeeler() {
		float size = dimension.width / 2 + speedMag * FEELER_RANGE;
		feelerVector = speed.copy().normalize().mult(size);
		feeler.setLine(0.0, 0.0, feelerVector.mag(), 0);
	}
	
	protected boolean detect(SimulationObject obj) {
		PVector feelerEndPoint = PVector.add(pos, feelerVector);
		return obj.getOutline().contains(feelerEndPoint.x, feelerEndPoint.y);
	}

	protected void escape(SimulationObject obj) {
//		System.out.println("escape");
		speed.mult((float) 0.5);
		moveAway(obj);
		speed.limit(speedMag);
		pos.add(speed);
	}
	
	protected void chase(SimulationObject obj) {
//		System.out.println("chase");
		speed.mult((float) 0.5);
		approach(obj);
		speed.limit(speedMag);
		pos.add(speed);
	}

	@Override
	public void update(ArrayList<SimulationObject> objList, LakePanel panel) {
		ArrayList<SimulationObject> fList = filterTargetList(objList);
		traceBestFood(fList);

		updateFeeler();
		resolveCollision(objList);		
		//Return the wall push forces and compute accelerations
		PVector wallSteerAccel = wallPushForce().div((float)size);
		//Make it turn per wall steering accelerations
		speed.add(wallSteerAccel);
		move();

		if (energy > FULL_ENERGY)
			state = OVER_FULL;
		else if (energy == FULL_ENERGY)
			state = FULL;
		else if (energy > FULL_ENERGY / 3)
			state = HALF_FULL;
		else if (energy > FULL_ENERGY / 5)
			  state = HUNGRY;
		//When energy falls below certain level, 
		//draw animal as sick.
		else if (energy > FULL_ENERGY / 10)
			state = SICK;
		// not sure why energy == EMPTY_ENERGY is 
		// not working properly but this also works
		//When they run out of energy and can’t get food to restore
		//within certain time interval (e.g. 3 seconds), they will die.
		else if(energy <= EMPTY_ENERGY && !noEnergy) {
			noEnergy = true;
			death_timer.start();
		}
		// since update is been called every frame and
		// after above condition checking, we know what to do next
		if (state == DEATH) {
			  panel.setStatus(this.animalType() + " died ... " );
			  objList.remove(this);
			  return;
		}
		
		for (int i = 0; i < fList.size(); i++) {
			if (isColliding(fList.get(i))) {
				float foodSize = fList.get(i).getSize();
				//When a food is consumed, it adds to the creature 
				//an amount of energy proportional to the food size
				energy += foodSize * engGainRatio;

				//When the energy goes above certain level (e.g. 100 units as maximum level),
				//it will gain weights proportionate to the extra energy amount
				// update again in case
				if (energy > FULL_ENERGY)
					state = OVER_FULL;
				if (state == OVER_FULL) {
					float extra = energy - FULL_ENERGY;
					energy = FULL_ENERGY;
					size += extra * sizeGrowRatio * size;
				}
				objList.remove(fList.get(i));
				chase_mode = false;
			}
		}

	}

	private String animalType() {
		String type = "unknown animal";
		if (this instanceof Crab)
			type = "Crab";
		else if (this instanceof Fish)
			type = "Fish";
		return type;
	}

	public ArrayList<SimulationObject> filterTargetList(ArrayList<SimulationObject> fList) {
		ArrayList<SimulationObject> list = new ArrayList<>();
		for (SimulationObject f : fList)
			if (eatable(f))
				list.add(f);
		return list;
	}

	public void drawInfo(Graphics2D g) {
		//The display must use appropriate Java font type, 
		//and centered around and above the creature with appropriate gap in-between.
		AffineTransform at = g.getTransform();
		g.translate(pos.x, pos.y);

		String st1 = "Size     : " + String.format("%.2f", size);
		String st2 = "Speed  : " + String.format("%.2f", speed.mag());
		String st3 = "Energy : " + String.format("%.2f", energy);

		Font f = new Font("Courier", Font.PLAIN, 12);
		FontMetrics metrics = g.getFontMetrics(f);

		float textWidth = metrics.stringWidth(st3);
		float textHeight = metrics.getHeight();
		float margin = 10, spacing = 2;

		g.setColor(new Color(255, 255, 255, 60));
		g.fillRect((int) (-textWidth / 2 - margin),
				(int) (-dimension.height * size * .75f - textHeight * 5f - spacing * 4f - margin * 2f),
				(int) (textWidth + margin * 2f), (int) (textHeight * 5f + spacing * 4f + margin * 2f));

		g.setColor(Color.blue.darker());
		g.drawString(this.animalType(), -metrics.stringWidth(this.animalType()) / 2,
				-dimension.height * size * .75f - margin - (textHeight + spacing) * 4f);
		g.setColor(Color.black);
		g.drawString(st1, -textWidth / 2.7f, -dimension.height * size * .75f - margin - (textHeight + spacing) * 2f);
		g.drawString(st2, -textWidth / 2.7f, -dimension.height * size * .75f - margin - (textHeight + spacing) * 1f);
		if (state == SICK)
			  g.setColor(Color.red);
		g.drawString(st3, -textWidth / 2.7f, -dimension.height * size * .75f - margin);
		
		g.setTransform(at);
	}

	public boolean moveAway(SimulationObject obj) {
		// This ensures that the creature does not go outside
		// of the boundary when trying to escape
		//Return the wall push forces and compute accelerations
		PVector wallSteerAccel = wallPushForce().div((float)size);
		//Make it turn per wall steering accelerations
		speed.add(wallSteerAccel);

		float angle = (float) Math.atan2(pos.y-obj.getPos().y, pos.x-obj.getPos().x);
		float coef = .3f;
		PVector accel = new PVector();
		PVector direction = PVector.fromAngle(angle);
		accel = PVector.mult(direction, coef*speedMag);
		speed.add(accel);
		return true;
	}

	protected abstract void traceBestFood(ArrayList<SimulationObject> fList);

	protected abstract boolean eatable(SimulationObject food);
	
	public float getEnergy() {
		return energy;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//When they run out of energy and can’t get food to restore 
		//within certain time interval (e.g. 3 seconds), they will die.
		state = DEATH;
		death_timer.stop();
	}
}
