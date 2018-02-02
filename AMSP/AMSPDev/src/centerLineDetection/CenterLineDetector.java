package centerlineDetection;

import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.TouchAdapter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;
import utils.Debugger;
import utils.RobotConfig;
import utils.SensorUtils;

public class CenterlineDetector implements IntersectionType
{
	private RegulatedMotor motor;
	private EV3TouchSensor[] touchSensors = new EV3TouchSensor[2];
	private TouchAdapter[] touchAdapters = new TouchAdapter[2];
	private RobotConfig config;
	private Debugger debugger;
	private SensorUtils sensorUtils;
	private ColorAdapter colorAdapter;
	/**
	 * <b>This instance variable controls the timer.</b>
	 * <p>
	 * As long as the service is running, scans will continue to happen.
	 * 
	 * @since 1.0.0
	 */
	private Timer service;
	private Color foregroundColor;
	private Color finishColor;
	/**
	 * <b>This instance variable holds the <code>CenterlineListeners</code>
	 * <i>(or just listeners)</i> in an <code>ArrayList</code>.</b>
	 * 
	 * @since 1.0.0
	 */
	private ArrayList<CenterlineListener> listeners = new ArrayList<>();
	/**
	 * <b>This <i>static</i> instance variable holds an instance of the
	 * <code>CenterlineDetector</code> object.</b>
	 * 
	 * @since 1.0.0
	 */
	private static CenterlineDetector instance;
	/**
	 * <b>This instance variable is set to <code>true</code> whenever any scan
	 * inside of this class is running and is set to <code>false</code> whenever
	 * no scans from inside this class are running.</b>
	 * 
	 * @since 1.0.0
	 */
	private boolean isScanning = false;
	/**
	 * <b>If this instance variable is set to <code>true</code>, then all scans
	 * inside this class will stop.</b>
	 * 
	 * @since 1.0.0
	 */
	private boolean stopScanning = false;

	/**
	 * <b>Constructor for the CenterlineDetector.</b>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param config
	 *            <code>RobotConfig</code>
	 * @param delay
	 *            <code>int</code> - time (in milliseconds) between scan cycles
	 * @param scanSpeed
	 *            <code>int</code> - the speed at which the motor holding the
	 *            color sensor rotates
	 * @since 2.0.0 </br>
	 *        Last modified: 2.1.1
	 */
	public CenterlineDetector(RobotConfig config, int delay, int scanSpeed)
	{
		this.config = config;
		debugger = config.getDebugger();
		sensorUtils = config.getSensorUtils();
		motor = config.getColorScannerMotor();
		motor.setSpeed(scanSpeed);
		colorAdapter = new ColorAdapter(config.getColorSensor());
		foregroundColor = config.getForegroundColor();
		finishColor = config.getFinishColor();

		service = new Timer(delay, new TimerListener()
		{
			@Override
			public void timedOut()
			{
				scan();
			}
		});

		touchSensors = config.getTouchSensors();
		instance = this;

		for(int i = 0; i < touchSensors.length; i++)
		{
			touchAdapters[i] = new TouchAdapter(touchSensors[i]);
		}
	}

	/**
	 * <b>This <i>static</i> method returns an instance of the
	 * CenterlineDetector.</b>
	 * 
	 * @author Krish
	 * @param None
	 * @return instance <code>CenterlineDetector</code> - an instance of the
	 *         CenterlineDetector
	 * @since 1.0.0 </br>
	 *        Last modified: Never
	 * @throws NullPointerException
	 *             if there is no instance
	 */
	public static CenterlineDetector getInstance()
	{
		if(instance == null)
		{
			throw new NullPointerException("No instance");
		}
		return instance;
	}

	/**
	 * <b>This method adds an object to the list of listeners.</b>
	 * <p>
	 * Any object which extends <code>CenterlineListener</code> must become a listener. Being
	 * a listener allows the object to get reports made every scan.
	 * 
	 * @author Krish
	 * @param listener
	 *            - any object which extends <code>CenterlineListener</code>
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: Never
	 */
	public void addListener(CenterlineListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * <b>This method runs a short scan upon a request to see if the robot
	 * has reached the finish line.</b>
	 * <p>
	 * This method sets <code>isScanning</code> to <code>true</code> and stops
	 * the movement of the robot upon starting. It will check if the color
	 * sensor sees the finish line color first. If it does <b>NOT</b>, it will
	 * return <code>Direction.Straight</code>, leaving the method and setting
	 * <code>isScanning</code> to <code>false</code>. </br>
	 * If it does, it will confirm that it is on the finish line with a short
	 * scan. The color sensor will rotate n degrees to the right, while checking
	 * if it sees the finish line color the entire length of the rotation. If it
	 * saw the finish line color the entire scan, it will return
	 * <code>Direction.Finish</code>. If it did <b>NOT</b>, it will return
	 * <code>Direction.Straight</code> and leave the method after setting
	 * <code>isScanning</code> to <code>false</code>.
	 * 
	 * @author Nick
	 * @param None
	 * @return Direction <code>enum</code>
	 * @since 2.1.0 </br>
	 *        Last modified: Never
	 */
	public Direction requestFinishScan()
	{
		isScanning = true;
		config.getMovePilotInstance().stop();
		debugger.printToScreen("CenterlineDetector: Finish line scan requested.");

		if(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == false)
		{
			isScanning = false;
			debugger.printToScreen("CenterlineDetector: Request result: False alarm.");
			return Direction.Straight;
		}

		motor.rotate(-45, true);

		while(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == true
				&& motor.isMoving())
		{
			Thread.yield();
		}

		if(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == true)
		{
			motor.rotateTo(0);
			motor.flt();
			isScanning = false;
			return Direction.Finish;
		}
		else
		{
			motor.rotateTo(0);
			motor.rotate(20);
			motor.flt();
			isScanning = false;
			debugger.printToScreen("CenterlineDetector: Request result: False alarm.");
			return Direction.Straight;
		}
	}

	/**
	 * <b>This method scans to make sure it is still following the line.</b>
	 * <p>
	 * &nbsp This method starts with setting <code>isScanning</code> to
	 * <code>true</code> and stopping the movement of the robot. Before it
	 * scans, it will check if it sees the finish line. If it does, the variable
	 * <code>seesFinish</code> gets set to 3 <i>(this gets counted down later as
	 * an assurance it sees the finish line and is not mistaken)</i>. </br>
	 * &nbsp The color sensor will now begin rotating right n degrees towards
	 * the line (which is presumably in front of the robot) while
	 * scanning for the color of the line. While scanning for the line, it will
	 * continue checking if the finish line is in sight. If the finish line is
	 * not in sight, <code>seesFinish</code> will continuously decrease until it reaches 0.
	 * </br>
	 * &nbsp If it never sees the middle line after rotating right the n degrees
	 * (and does not see the finish line color), <code>isScanning</code> will be
	 * set to <code>false</code> and it will return
	 * <code>Direction.DeadEnd</code>. </br>
	 * &nbsp Else if it saw the finish line color during the whole rotation, then 
	 * <code>isScanning</code> will be set to <code>false</code> and it will return
	 * <code>Direction.Finish</code>. </br>
	 * &nbsp If none of the above occur, then it will assume it saw the middle
	 * line at some point, set <code>isScanning</code> to <code>false</code> and
	 * it will return <code>Direction.Straight</code>.
	 * 
	 * @author Krish, Caroline, Nick, Shaun
	 * @param None
	 * @return Direction <code>enum</code>
	 * @since 1.0.0 </br>
	 *        Last modified: 2.2.2
	 */
	private Direction findLine()
	{
		isScanning = true;
		config.getMovePilotInstance().stop();
		byte seesFinish = 0;
		debugger.printToScreen("CenterlineDetector: Finding line");

		// Check for the finish line before conducting routine scan
		if(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == true)
		{
			seesFinish = 3;
		}

		motor.rotate(-45, true);

		//While scanner is rotating right
		while(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false 
				&& motor.isMoving())
		{
			Thread.yield();

			if(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == false && seesFinish > 0)
			{
				seesFinish--;
			}

		}

		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false)
		{
			Sound.beep();
			motor.rotateTo(0);
			motor.flt();
			debugger.printToScreen("CenterlineDetector: Dead end detected.");
			isScanning = false;
			return Direction.DeadEnd;
		}
		else if(sensorUtils.checkColorRange(colorAdapter.getColor(), finishColor) == true && seesFinish > 0)
		{
			debugger.printToScreen("CenterlineDetector: Found finish.");
			isScanning = false;
			return Direction.Finish;
		}
		else
		{
			debugger.printToScreen("CenterlineDetector: Found path. Color is: "+ colorAdapter.getColorID()
			+ ". (" + foregroundColor + ") is black.");
			motor.rotateTo(20);
			motor.flt();
			isScanning = false;
			return Direction.Straight;
		}
	}

	/**
	 * <b>Javadocs must be re-written for this method!</b><p>
	 * <b>This method adjusts the color sensor to find where the line is before
	 * it begins moving.</b>
	 * <p>
	 * The color sensor will begin by rotating slightly to the right to ensure
	 * it is not in contact with the left touch sensor at runtime. It will then
	 * rotate to the left until it is touching the left touch sensor. </br>
	 * <b>The calibration begins at this point.</b></br>
	 * The color sensor will rotate to the right until it is in contact with the
	 * touch sensor on the right while measuring the distance it rotated. It
	 * will then rotate to the left half the distance it rotated to the right on
	 * the previous step. Once the rotation stops, in theory, the sensor should
	 * be rotated to the center point in front of the robot. Once
	 * the line is found the position of the sensor in relation to the robot
	 * will be saved in memory for the duration of the run. Next, the color
	 * sensor will rotate n degrees to the left, where it will remain while
	 * moving. Then calibration is complete.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.2.3
	 */
	public void calibrate()
	{
		isScanning = true;
		
		debugger.printToScreen("CenterlineDetector: Beginning calibration...");
		
		short oldMotorSpeed = (short) motor.getSpeed();
		motor.setSpeed((int) (motor.getSpeed() * 2.5));

		motor.rotate(180, true);

		//Rotate to the left sensor
		while(motor.isMoving() == true && touchAdapters[0].isPressed() == false 
				&& sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false)
		{
			Thread.yield();
		}

		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == true)
		{
			motor.resetTachoCount();
			motor.setSpeed(oldMotorSpeed);
			motor.rotate(20);
			motor.flt();
			makeReport(Direction.Straight);
		
			Sound.beepSequenceUp();
	
			debugger.printToScreen("CenterlineDetector: Finished calibrating.");
			
			isScanning = false;
			return;
		}
		
		motor.rotate(-180, true);//Rotate right

		while(motor.isMoving() == true && touchAdapters[1].isPressed() == false
				&& sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false)//[1] is right
		{
			Thread.yield();
		}
		
		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == true)
		{
			motor.resetTachoCount();
			motor.setSpeed(oldMotorSpeed);
			motor.rotate(20);
			motor.flt();
			makeReport(Direction.Straight);
		
			Sound.beepSequenceUp();
	
			debugger.printToScreen("CenterlineDetector: Finished calibrating.");
			
			isScanning = false;
			return;
		}

		throw new RuntimeException("CenterlineDetector: Line was not found in the vicinity of the robot. "
					+ "Aborting program...");
	}

	/**
	 * <b>This method gets called whenever a scan should be initiated, gets the
	 * <code>Direction</code> by calling the <code>findLine()</code> method, and
	 * sends it to the <code>makeReport()</code> method.</b>
	 * <p>
	 * This method only runs if <code>stopScanning</code> and
	 * <code>isScanning</code> are set to <code>false</code>. This is mostly to
	 * prevent conflicting scans or scanning during a turn.
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.0.0
	 */
	private void scan()
	{
		if(stopScanning == false && isScanning == false)
		{
			makeReport(findLine());
		}
	}

	/**
	 * <b>This method gets called whenever a report must get sent to all of the
	 * listeners.</b>
	 * <p>
	 * Whichever method calls the <code>makeReport()</code> method must provide
	 * a <code>Direction</code> to be sent to all of the listeners.
	 * 
	 * @author Krish, Caroline, Nick
	 * @param dir
	 *            <code>Direction</code> - the <code>Direction</code> to be sent
	 *            out in the report
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.0.0
	 */
	public void makeReport(Direction dir)
	{
		debugger.printToScreen("CenterlineDetector: Making report - Direction is " + dir);
		for(CenterlineListener listener : listeners)
		{
			listener.report(dir);
		}
	}

	/**
	 * <b>This method gets called whenever scans need to start/resume.</b>
	 * <p>
	 * When this method is called, <code>service.start()</code> gets called,
	 * which resumes the scans and sets <code>stopScanning</code> to
	 * <code>false</code>.
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.0.0
	 * @throws NullPointerException
	 *             if there are no listeners defined
	 */
	public void start()
	{
		if(listeners.size() == 0)
		{
			throw new NullPointerException("Centerline listeners not defined!");
		}
		service.start();
		stopScanning = false;
	}

	/**
	 * <b>This method gets called whenever scans need to stop.</b>
	 * <p>
	 * When this method is called, <code>service.stop()</code> gets called,
	 * which stops the scans and sets <code>stopScanning</code> to
	 * <code>true</code>.
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.0.0
	 */
	public void stop()
	{
		service.stop();
		stopScanning = true;
	}

	/**
	 * <b>Getter for the <code>isScanning</code> variable.</b>
	 * 
	 * @author Nick
	 * @param None
	 * @return boolean
	 * @since 2.0.0 </br>
	 *        Last modified: Never
	 */
	public boolean getIsScanning()
	{
		return isScanning;
	}
}