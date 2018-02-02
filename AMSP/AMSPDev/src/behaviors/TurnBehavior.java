package behaviors;

import centerLineDetection.CenterLineDetector;
import centerLineDetection.CenterLineListener;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;
import utils.SensorUtils;

public class TurnBehavior implements Behavior, CenterLineListener
{

	private RegulatedMotor scannerMotor;
	private ColorAdapter colorAdapter;
	private MovePilot pilot;
	private RobotConfig config;
	private Debugger debugger;
	private SensorUtils sensorUtils;
	private Color foreground;
	private CenterLineDetector det;
	private Direction direction = Direction.Straight;//TODO test this as null
	
	public TurnBehavior(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
		sensorUtils = config.getSensorUtils();
		scannerMotor = config.getColorScannerMotor();
		colorAdapter = config.getColorAdapter();
		pilot = config.getMovePilotInstance();
		foreground = config.foregroundColor;
	}
	
	/**
	 * <b>This <code>takeControl()</code> method returns true whenever the color sensor sees the foreground color or the
	 * <code>Direction</code> is either <code>LeftTurn</code>, <code>RightTurn</code>, or <code>DeadEnd</code>.</b>
	 * <p>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.1
	 */
	@Override
	public boolean takeControl() 
	{
		//If the Direction is any intersection type, go into this block
		return (direction == Direction.LeftTurn || direction == Direction.RightTurn || direction == Direction.DeadEnd)
				? true : false;
	}

	/**
	 * <b>When this <code>action()</code> method gets called, it will make the robot react differently 
	 * depending on the current <code>Direction</code>.</b>
	 * <p>
	 * The first thing that will happen is if the <code>CenterlineDetector</code> has a scan 
	 * running, it will wait on it to complete and then call the <code>CenterlineDetector</code> to stop the
	 * scans. It will then rotate the color sensor to <code>0</code> <i>(which, if the calibration was 
	 * performed correctly, should be in the front and center of the robot)</i>. It will then proceed into a 
	 * case statement whose condition is the <code>Direction</code>.
	 * </br>
	 * - <code>Direction.DeadEnd</code> - There are two parts to this turn. The robot will rotate right 
	 * until it finds a line. If it finds a
	 * line within the first part of the turn, it assumes it found a right turn. If it doesn't, it will start
	 * the second part of the rotation, rotating until it finds the line it traveled on previously. If it 
	 * never finds the line, it will begin rotating left until it is facing approximately
	 * the direction it came from.
	 * </br>
	 * - <code>Direction.RightTurn</code> - If it goes into this case, a {@link RuntimeException} will get
	 * thrown.
	 * </br>
	 * - <code>Direction.LeftTurn</code> - The robot will begin to rotate to the left, assuming it might see the 
	 * foreground line when it begins rotating. If it does, it will rotate over it and continue until it
	 * sees the next line. If it doesn't see a line in those initial few degrees, it will continue rotating 
	 * anyways until it sees a line. If it never sees a line, it will rotate back to where it started initially 
	 * and report a <code>Direction.DeadEnd</code> and return out of the method <i>(the method will get 
	 * called again but it will go into the</i> <code>Direction.DeadEnd</code><i> case)</i>.
	 * <p>
	 * When it reaches the end of the method, it will rotate the color sensor to its standard, offset to the left, position, 
	 * send a <code>Direction.Straight</code> report, and resume the scans in the <code>CenterlineDetector</code>
	 * class.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.1
	 * @throws RuntimeException if it enters the <code>RightTurn</code> case or <code>default</code> case.
	 */
	@Override
	public void action()
	{
		debugger.printToScreen("TurnBehavior: Action for direction " + direction);
		
		pilot.stop();
		scannerMotor.rotateTo(0);
		scannerMotor.flt();
		
		//Check the direction and recover accordingly
		switch(direction)
		{
			case DeadEnd:
				pilot.rotate(200, true);//Positive is right
				
				while(sensorUtils.checkColorRange(colorAdapter.getColor(), foreground) == false 
						&& pilot.isMoving() == true)
				{
					Thread.yield();
				}
				
				if(pilot.isMoving() == false)
				{
					pilot.rotate(-200);//Negative is left
					throw new RuntimeException("TurnBehavior: Failed scan in the DeadEnd case.");
				}
				break;
			case RightTurn:
				//Travel slightly forward to get in the middle of the intersection
				pilot.travel(3.5);
				
				pilot.rotate(200, true);//Positive is right
				
				while(sensorUtils.checkColorRange(colorAdapter.getColor(), foreground) == false 
						&& pilot.isMoving() == true)
				{
					Thread.yield();
				}
				
				if(pilot.isMoving() == false)
				{
					pilot.rotate(-200);//Negative is left
					det.makeReport(Direction.LeftTurn);
					return;
				}
				break;
			case LeftTurn:
				//Travel slightly forward to get in the middle of the intersection
				pilot.travel(3.5);
				
				pilot.rotate(-120, true);//Negative is left
				
				while(sensorUtils.checkColorRange(colorAdapter.getColor(), foreground) == false 
						&& pilot.isMoving() == true)
				{
					Thread.yield();
				}
				
				if(pilot.isMoving() == false)
				{
					pilot.rotate(120);//Positive is right
					det.makeReport(Direction.DeadEnd);
					return;
				}
				break;
			default:
				throw new RuntimeException("[" + config.getTime() + "] "
						+ "TurnBehavior: Went into the default case in action method. "
						+ "Direction was: " + direction + ".");
		}
		
		pilot.stop();
		debugger.printToScreen("TurnBehavior: Finished action for direction " + direction);
		det.makeReport(Direction.Straight);
		return;
	}

	@Override
	public void suppress() 
	{
		debugger.printToScreen("TurnBehavior: Being suppressed");
	}
	
	@Override
	public void becomeListener(CenterLineDetector det) 
	{
		this.det = det;
		det.addListener(this);
	}

	@Override
	public void getReport(Direction direction) 
	{
		this.direction = direction;
	}
}