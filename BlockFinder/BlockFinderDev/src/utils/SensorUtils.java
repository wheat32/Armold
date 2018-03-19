package utils;

import lejos.robotics.Color;

public class SensorUtils 
{
	private RobotConfig config;
	
	public SensorUtils(RobotConfig config)
	{
		this.config = config;
	}
	
	public boolean checkColorRange(Color currColor, Color targetColor)
	{
		int currColorElems[] = {currColor.getRed(), currColor.getGreen(), currColor.getBlue()};
		int targetColorElems[] = {targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue()};
		
		for(int i = 0; i < currColorElems.length; i++)
		{
			if(currColorElems[i] - targetColorElems[i] > config.colorBuffer
					|| currColorElems[i] - targetColorElems[i] < config.colorBuffer*-1)
			{
				return false;
			}
		}
		
		return true;
	}
}