package utils;

import arbitrator.Arbitrator;
import arbitrator.Behavior;
import behaviors.MoveForward;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;

public class Main 
{	
	public static void main(String[] args) 
	{
		//Setup
		RobotConfig config = new RobotConfig();
		
		//RobotConfig setup
		config.configureDifferentialPilot(Motor.B, Motor.C, 2.1f, 3);
		config.configureColorSensorPort(SensorPort.S3);
		config.configureIRSensorPort(SensorPort.S4);
		config.configureColorScannerMotor(Motor.B);
		config.setLinearSpeed(4);
		config.setLinearAcceleration(3);
		config.setAngularSpeed(24);
		
		//Behavior setup
		Behavior moveForward = new MoveForward(config);
		
		//Object Declarators
		Debugger.setConfig(config);
		new UserInput(64).start();
		
		//Start runtime
		Debugger.debugPrompt();
		new Arbitrator(new Behavior[] {moveForward});
	}
}