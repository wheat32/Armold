package utils;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

/**
 * This class contains various port assignments and defines static factory
 * methods for shared service creation for the robot as a whole.
 * 
 * @author Krish Pillai
 *
 */
public class RobotConfig
{
	private int foreground = Color.BLACK;
	private int finish = Color.BLUE;
	private int border = Color.RED;

	private EV3TouchSensor[] touchSensors = new EV3TouchSensor[2];
	private EV3ColorSensor colorSensor;

	private Port colorSensorPort;
	private Port[] touchSensorPorts = new Port[2];

	private RegulatedMotor colorScannerMotor;

	private MovePilot movePilot;

	private long timeStamp = System.currentTimeMillis();

	/**
	 * This method builds a two-wheeled differential chassis. Takes designated
	 * motors with wheel diameter and offsets from the axle midpoint as
	 * arguments. It is assumed that identical wheels are used to build the
	 * chassis. All directions are specified from the point of reference of
	 * viewer seated at the midpoint of the two-wheeled axle while facing in the
	 * forward direction.
	 * 
	 * @param motors
	 * @param diameters
	 * @param offsets
	 */
	public void configureDifferentialPilot(NXTRegulatedMotor left, NXTRegulatedMotor right, 
			float wheelDiameter, int offsetFromAxleCenter)
	{
		Wheel[] wheels = new Wheel[2];
		wheels[0] = WheeledChassis.modelWheel(left, wheelDiameter).offset(-offsetFromAxleCenter);
		wheels[1] = WheeledChassis.modelWheel(right, wheelDiameter).offset(offsetFromAxleCenter);
		WheeledChassis chassis = new WheeledChassis(wheels, WheeledChassis.TYPE_DIFFERENTIAL);
		movePilot = new MovePilot(chassis);
	}

	/**
	 * Configures Color sensor service on specified port.
	 * 
	 * @param port
	 */
	public void configureColorSensorPort(Port port)
	{
		colorSensorPort = port;
	}

	public void configureTouchSensorPorts(Port[] ports)
	{
		for(int i = 0; i < ports.length; i++)
		{
			touchSensorPorts[i] = ports[i];
		}
	}

	/**
	 * Get the Color Sensor Port
	 * 
	 * @return the sensor port
	 */
	public Port getColorSensorPort()
	{
		return colorSensorPort;
	}

	/**
	 * Get the Touch Sensor Port
	 * 
	 * @return the sensor port
	 */
	public Port[] getTouchSensorPort()
	{
		return touchSensorPorts;
	}

	/**
	 * Configures Color sensor turret motor.
	 * 
	 * @param motor
	 */
	public void configureColorScannerMotor(RegulatedMotor motor)
	{
		colorScannerMotor = motor;
	}

	/*
	 * public void configureCenterlineDetector(int delay, int scanSpeed){
	 * this.centerlineDetector = new CenterlineDetector(this, delay, scanSpeed);
	 * }
	 */

	public EV3TouchSensor[] getTouchSensors()
	{
		if(touchSensors[0] == null)
		{
			if(touchSensorPorts[0] == null)
			{
				throw new NullPointerException("Touch sensor ports undefined!");
			}
			for(int i = 0; i < touchSensorPorts.length; i++)
			{
				touchSensors[i] = new EV3TouchSensor(touchSensorPorts[i]);
			}
		}
		return touchSensors;
	}

	public EV3ColorSensor getColorSensor()
	{
		if(colorSensor == null)
		{
			if(colorSensorPort == null)
			{
				throw new NullPointerException("Color sensor port undefined!");
			}
			colorSensor = new EV3ColorSensor(colorSensorPort);
		}
		return colorSensor;
	}

	/**
	 * Returns reference to configured Movepilot which can be used for
	 * translations and turns
	 * 
	 * @return
	 */
	public MovePilot getMovePilotInstance()
	{
		return movePilot;
	}

	/**
	 * Sets the speed at which the robot will travel forward and backward. Speed
	 * is measured in units/second. e.g. If wheel diameter is cm, then speed is
	 * cm/sec.
	 * 
	 * @param speed
	 */
	public void setLinearSpeed(double speed)
	{
		movePilot.setLinearSpeed(speed);
		// System.out.println(speed);
	}

	/**
	 * Sets the acceleration at which the robot will accelerate at the start of
	 * a move and decelerate at the end of a move. Acceleration is measured in
	 * units/second^2. e.g. If wheel diameter is cm, then acceleration is
	 * cm/sec^2. If acceleration is set during a move it will not be in used for
	 * the current move, it will be in effect with the next move.
	 * 
	 * @param acceleration
	 */
	public void setLinearAcceleration(double acceleration)
	{
		movePilot.setLinearAcceleration(acceleration);
	}

	/**
	 * Sets the rotation speed of the robot Sets the angular velocity of the
	 * rotate() methods
	 * 
	 * @param speed
	 */
	public void setAngularSpeed(double speed)
	{
		movePilot.setAngularSpeed(speed);
	}

	/**
	 * Sets the acceleration at which the robot will accelerate at the start of
	 * a move and decelerate at the end of a move. Acceleration is measured in
	 * units/second^2. e.g. If wheel diameter is cm, then acceleration is
	 * cm/sec^2. If acceleration is set during a move it will not be in used for
	 * the current move, it will be in effect with the next move.
	 * 
	 * @param acceleration
	 */
	public void setAngularAcceleration(double acceleration)
	{
		movePilot.setAngularAcceleration(acceleration);
	}

	public RegulatedMotor getColorScannerMotor()
	{
		if(colorScannerMotor == null)
		{
			throw new NullPointerException("Color Scanner motor not assigned!");
		}

		return colorScannerMotor;
	}

	public long getTime()
	{
		return System.currentTimeMillis() - timeStamp;
	}

	public int getForegroundColor()
	{
		return foreground;
	}

	public int getFinishColor()
	{
		return finish;
	}

	public int getBorderColor()
	{
		return border;
	}

	public void resetTimeStamp()
	{
		timeStamp = System.currentTimeMillis();
	}
}