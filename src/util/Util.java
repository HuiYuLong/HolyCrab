package util;

import java.awt.Color;
import java.util.ArrayList;

import ecosys.simulation.Food;
import ecosys.simulation.SimulationObject;
import main.LakePanel;
import processing.core.PVector;

public class Util {

	public static float random(double min, double max) {
		return (float) (Math.random()*(max-min)+min);
	}
	
	public static float random(double max) {
		return (float) (Math.random()*max);
	}
	
	public static Color randomColor() {
		int r = (int) random(255);
		int g = (int) random(255);
		int b = (int) random(255);
		
		return new Color(r,g,b);
	}
	
	public static PVector randomPVector(int maxX, int maxY) {		
		return new PVector((float)random(maxX), (float)random(maxY));	
	}
	
	public static PVector randomPVector(float magnitude) {
		return PVector.random2D().mult(magnitude);
	}
	
	public static Food randomFood() {
		return new Food(	Util.random(80, LakePanel.PAN_SIZE.width-80), 
						Util.random(80, LakePanel.PAN_SIZE.height-80),
						Util.random(1,5));
	}
	
	public static int countFood(ArrayList<SimulationObject> objList) {
		int i = 0;
		for (SimulationObject obj:objList) if (obj instanceof Food) i++;
		return i;
	}
}
