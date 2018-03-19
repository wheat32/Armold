package utils;

import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;

public class Main 
{	
	public static void main(String[] args) 
	{
		//Setup
		RobotConfig config = new RobotConfig();
		config.setDebugger(new Debugger(config));
		
		//RobotConfig setup
		config.configureDifferentialPilot(Motor.B, Motor.C, 2.1f, 3);
		config.configureColorSensorPort(SensorPort.S3);
		config.configureIRSensorPort(SensorPort.S4);
		config.configureColorScannerMotor(Motor.B);
		config.setLinearSpeed(4);
		config.setLinearAcceleration(3);
		config.setAngularSpeed(24);
		
		//Object Declarators
		new UserInput(64).start();
		
		//Start runtime
		config.getDebugger().debugPrompt();
	}
}