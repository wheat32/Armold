package centerLineDetection;

import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.TouchAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;
import utils.SensorUtils;

public class CenterLineDetector implements IntersectionType, Behavior
{
	//Hardware-related instance variables
	private final RegulatedMotor colorMotor;
	private TouchAdapter[] touchAdapters = new TouchAdapter[2];
	private final ColorAdapter colorAdapter;
	
	//AMSP objects
	private final RobotConfig config;
	private final Debugger debugger;
	private final SensorUtils sensorUtils;
	
	/**
	 * <b>This instance variable holds the <code>CenterlineListeners</code>
	 * <i>(or just listeners)</i> in an <code>ArrayList</code>.</b>
	 * 
	 * @since 1.0.0
	 */
	private ArrayList<CenterLineListener> listeners = new ArrayList<>();
	private final Color foregroundColor;
	private Direction direction;
	
	/**
	 * <b>Constructor for the CenterlineDetector.</b>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param config
	 *            <code>RobotConfig</code>
	 * @param delay
	 *            <code>int</code> - time (in milliseconds) between scan cycles
	 * @since 2.0.0 </br>
	 *        Last modified: 2.1.1
	 */
	public CenterLineDetector(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
		sensorUtils = config.getSensorUtils();
		colorMotor = config.getColorScannerMotor();
		colorAdapter = config.getColorAdapter();
		touchAdapters = config.getTouchAdapter();
		foregroundColor = config.foregroundColor;
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
	public void addListener(CenterLineListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * <b>THE JAVADOC FOR THIS METHOD IS OUT OF DATE.</b>
	 * <p>
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
	 *        Last modified: 2.2.4
	 */
	private Direction readLine()
	{
		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == true)
		{
			debugger.printToScreen("CenterLineDetector: Rotating color motor left...");
			colorMotor.rotate(1, true);//Left
		}
		else
		{
			debugger.printToScreen("CenterLineDetector: Rotating color motor right...");
			colorMotor.rotate(-1, true);//Right
		}
		
		if(colorMotor.getTachoCount() >= 25)//Left turn
		{
			config.getMovePilotInstance().stop();
			Sound.beep();
			debugger.printToScreen("CenterLineDetector: Found left turn.");
			return Direction.LeftTurn;
		}
		else if(colorMotor.getTachoCount() <= -25)//Right turn
		{
			config.getMovePilotInstance().stop();
			Sound.beep();
			debugger.printToScreen("CenterLineDetector: Found right turn.");
			return Direction.RightTurn;
		}
		else
		{
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
	 *        Last modified: 2.2.4
	 */
	public void calibrate()
	{
		debugger.printToScreen("CenterLineDetector: Beginning calibration...");
		
		byte oldMotorSpeed = (byte) colorMotor.getSpeed();
		colorMotor.setSpeed(colorMotor.getSpeed()*2);

		colorMotor.rotate(180, true);

		//Rotate to the left sensor
		while(colorMotor.isMoving() == true && touchAdapters[0].isPressed() == false 
				&& sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false)
		{
			Thread.yield();
		}

		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == true)
		{
			colorMotor.resetTachoCount();
			colorMotor.setSpeed(oldMotorSpeed);
			//Left
			colorMotor.rotate(2);
			colorMotor.flt();
			makeReport(Direction.Straight);
		
			Sound.beepSequenceUp();
	
			debugger.printToScreen("CenterLineDetector: Finished calibrating.");
			return;
		}
		
		colorMotor.rotate(-180, true);//Rotate right

		while(colorMotor.isMoving() == true && touchAdapters[1].isPressed() == false
				&& sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == false)//[1] is right
		{
			Thread.yield();
		}
		
		if(sensorUtils.checkColorRange(colorAdapter.getColor(), foregroundColor) == true)
		{
			colorMotor.resetTachoCount();
			colorMotor.setSpeed(oldMotorSpeed);
			colorMotor.rotate(2);
			colorMotor.flt();
			makeReport(Direction.Straight);
		
			Sound.beepSequenceUp();
	
			debugger.printToScreen("CenterLineDetector: Finished calibrating.");
			return;
		}

		throw new RuntimeException("CenterLineDetector: Line was not found in the vicinity of the robot. "
					+ "Aborting program...");
	}

	@Override
	public boolean takeControl()
	{
		direction = readLine();
		return (direction != Direction.Straight) ? true : false;
	}

	@Override
	public void action()
	{
		this.makeReport(direction);
	}

	@Override
	public void suppress()
	{
		//NOTHING
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
	public synchronized void makeReport(Direction dir)
	{
		debugger.printToScreen("CenterLineDetector: Making report - Direction is " + dir);
		direction = dir;
		for(CenterLineListener listener : listeners)
		{
			listener.getReport(dir);//Make the report
		}
	}
}