package utils;

import arbitrator.Arbitrator;

public class Main 
{	
	public static void main(String[] args) 
	{
		//Setup
		RobotConfig config = new RobotConfig();
		config.setDebugger(new Debugger(config));
		
		//Start runtime
		config.getDebugger().debugPrompt();
		
	}
}