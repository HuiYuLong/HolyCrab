package ecosys.simulation;

import java.util.ArrayList;

import processing.core.PVector;

public interface Mover {
	public void move();

	void approach(SimulationObject obj);
	
	void resolveCollision(ArrayList<SimulationObject> objList);
}
