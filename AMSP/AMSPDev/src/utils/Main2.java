package utils;

import behaviors.EmergencyBehavior;
import behaviors.ForwardBehavior;
import behaviors.MazeCompletionBehavior;
import behaviors.TurnBehavior;
import centerLineDetection.CenterLineDetector;
import centerLineDetection.CenterLineListener;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Main2 
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
	 *        Last modified: 2.2.2
	 */
	public static void main(String[] args) 
	{
		//RobotConfig setup
		RobotConfig config = new RobotConfig();
		config.configureTouchSensorPorts(new Port[]{SensorPort.S1, SensorPort.S4});//Required to be here
		config.configureDifferentialPilot(Motor.A, Motor.D, 2.1f, 3);
		config.configureColorScannerMotor(Motor.B);
		config.configureColorSensorPort(SensorPort.S3);
		config.setLinearSpeed(4.0);
		config.setLinearAcceleration(3);
		config.setAngularSpeed(24);
		config.setColorScannerMotorRotationSpeed(42);
		
		Debugger debugger = new Debugger(config);
		Thread.setDefaultUncaughtExceptionHandler(debugger);
		config.setDebugger(debugger);
		debugger.debugPrompt();
		
		//Object declarations
		config.setSensorUtils(new SensorUtils(config));
		CenterLineDetector det = new CenterLineDetector(config);
		new UserInput(64, debugger);
		
		//Behavior setups and object instantiations
		Behavior b1 = new ForwardBehavior(config);
		Behavior b2 = new TurnBehavior(config);
		//Behavior b3 = new MazeCompletionBehavior(config);
		Behavior b4 = new EmergencyBehavior(config, det);
		Behavior b5 = (Behavior) det;
		Behavior[] behaviors = {b1, b5/*, b3*/, b2, b4};//Priority: Lowest Priority <--> Highest Priority
		
		//Create Listeners
		((CenterLineListener) (b1)).becomeListener(det);
		((CenterLineListener) (b2)).becomeListener(det);
		//((CenterLineListener) (b3)).becomeListener(det);
		
		config.resetTimeStamp();
		
		//Calibrate
		det.calibrate();
		//Start the arbitrator
		new Arbitrator(behaviors).go();
	}
}