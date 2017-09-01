package utils;

import behaviors.EmergencyBehavior;
import behaviors.ForwardBehavior;
import behaviors.MazeCompletionBehavior;
import behaviors.TurnBehavior;
import centerlineDetection.CenterlineDetector;
import centerlineDetection.CenterlineListener;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Main 
{	
	/**
	 * <b>The <code>main()</code> method is the starting point for the program. It will construct the objects and initialize
	 * them before starting the arbitrator.</b>
	 * <p>
	 * 
	 * @author Krish, Shaun, Caroline, Nick
	 * @param None
	 * @return Nothing.
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.2
	 */
	public static void main(String[] args) 
	{
		//RobotConfig setup
		RobotConfig config = new RobotConfig();
		Port[] touchPorts = {SensorPort.S1, SensorPort.S4};//Required to be here
		config.configureDifferentialPilot(Motor.A, Motor.D, 2.1f, 3);
		config.configureColorScannerMotor(Motor.B);
		config.configureColorSensorPort(SensorPort.S3);
		config.configureTouchSensorPorts(touchPorts);
		config.setLinearSpeed(4);
		config.setLinearAcceleration(3);
		config.setAngularSpeed(24);
		
		//Object declarators
		CenterlineDetector det = new CenterlineDetector(config, 1400, 50);
		UserInput userInput = new UserInput(64);
		Arbitrator arby;
		Behavior[] behaviors;
		
		//Behavior setups and object instantiations
		Behavior b1 = new ForwardBehavior(config);
		Behavior b2 = new TurnBehavior(config);
		Behavior b3 = new MazeCompletionBehavior(config);
		Behavior b4 = new EmergencyBehavior(config);
		behaviors = new Behavior[] {b1, b3, b2, b4};//Priority: Lowest Priority <--> Highest Priority
		arby = new Arbitrator(behaviors);
		
		//Create Listeners
		((CenterlineListener)b2).becomeListener(det);
		((CenterlineListener)b3).becomeListener(det);
		
		userInput.start();
		
		//Calibrate
		det.calibrate();
		//Start the scan timer
		det.start();
		//Start the arbitrator
		arby.go();
	}
}