package behaviors;

import centerlineDetection.CenterlineDetector;
import centerlineDetection.CenterlineListener;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.ColorAdapter;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.TouchAdapter;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;
import utils.RobotConfig;

public class TurnBehavior implements Behavior, CenterlineListener
{

	private RegulatedMotor scannerMotor;
	private ColorAdapter colorAdapter;
	private EV3TouchSensor[] touchSensors = new EV3TouchSensor[2];
	private TouchAdapter[] touchAdapters = new TouchAdapter[2];
	private MovePilot pilot;
	private RobotConfig config;
	private int foreground;
	private CenterlineDetector det = CenterlineDetector.getInstance();
	private Direction direction = Direction.Straight;
	
	public TurnBehavior(RobotConfig config)
	{
		this.config = config;
		scannerMotor = config.getColorScannerMotor();
		colorAdapter = new ColorAdapter(config.getColorSensor());
		touchSensors = config.getTouchSensors();
		foreground = config.getForegroundColor();
		pilot = config.getMovePilotInstance();
		
		for(int i = 0; i < touchSensors.length; i++)
		{
			touchAdapters[i] = new TouchAdapter(touchSensors[i]);
		}
	}
	
	@Override
	public boolean takeControl() 
	{
		if(det.getIsScanning() == false)
		{
			//If while going straight and see the foreground line, go into this block
			if(direction == Direction.Straight && colorAdapter.getColorID() == foreground)
			{
				System.out.println("[" + config.getTime() + "] Turn Behavior found left turn.");
				det.stop();
				det.makeReport(Direction.LeftTurn);
				return true;
			}
			//If the Direction is any intersection type, go into this block
			else if(direction == Direction.LeftTurn || direction == Direction.RightTurn 
					|| direction == Direction.DeadEnd)
			{
				det.stop();
				return true;
			}
		}
		
		return false;
	}

	/**
	 * <b>When this <code>action()</code> method gets called, it will make the robot react differently 
	 * depending on the current <code>Direction</code>.</b>
	 * <p>
	 * The first thing that happens in this method, is if the <code>CenterlineDetector</code> has a scan 
	 * running, it will wait on it to complete and then call the <code>CenterlineDetector</code> to stop the
	 * scans. It will then rotate the color sensor to <code>0</code> <i>(which, if the calibration was 
	 * performed correctly, should be in the front and center of the robot)</i>. It will then proceed into a 
	 * case statement whose condition is the <code>Direction</code>.
	 * </br>
	 * - <code>Direction.DeadEnd</code> - The robot will rotate right until it finds a line. If it finds a
	 * line within the first 120 degrees, it assumes it found a right turn. If it doesn't, it will continue
	 * rotating. If it never finds the line, it will begin rotating left until it is facing approximately
	 * the direction it came from.
	 * </br>
	 * - <code>Direction.RightTurn</code> - If it goes into this case, a {@link RuntimeException} will get
	 * thrown.
	 * </br>
	 * - <code>Direction.LeftTurn</code> - The robot will begin to rotate to the left, assuming it see the 
	 * foreground line when it begins rotating. If it does, it will rotate over it, and continue until it
	 * sees the next line. If it doesn't see a line in those initial few degrees, it will continue rotating 
	 * anyways until it sees a line. If it never sees a line, it will rotate back towards where it started 
	 * and report a <code>Direction.DeadEnd</code> and return out of the method <i>(the method will get 
	 * called again but it will go into the <code>Direction.DeadEnd</code> case)</i>.
	 * <p>
	 * When it reaches the end of the method, it will rotate the color sensor to its moving position, send a 
	 * <code>Direction.Straight</code> report, and resume the scans in the <code>CenterlineDetector</code>
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
		System.out.println("[" + config.getTime() + "]"
				+ "TurnBehavior: Action for direction " + direction);
		
		while(det.getIsScanning())//Let the scan conclude
		{
			Thread.yield();
		}
		det.stop();
		
		pilot.stop();
		scannerMotor.rotateTo(0);
		scannerMotor.flt();
		
		//Check the direction and recover accordingly
		switch(direction)
		{
			case DeadEnd:
				pilot.rotate(120, true);//positive is right
					
				while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
				{
					Thread.yield();
				}

				pilot.stop();
				//If the robot found the line again, break out of the switch.
				//In this instance, it most likely found a right turn.
				if(colorAdapter.getColorID() == foreground)
				{
					break;
				}
				
				//If it didn't find the line in the initial 120 degree rotation, we know it wasn't a right
				//turn and it should rotate further to go back the way it came from
				pilot.rotate(120, true);
					
				while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
				{
					Thread.yield();
				}
				
				//If it STILL didn't see the foreground line, it more than likely rotated past 180 degrees,
				//so it should rotate back to the left 60 degrees to approximately be facing the way it
				//came from.
				if(colorAdapter.getColorID() != foreground)
				{
					pilot.rotate(-60, true);
					
					//Continue rotating back until it sees the foreground line color (because maybe it missed
					//the color the first time going through). It's a "Jesus Take the Wheel" moment from
					//here as it will blindly drive forward towards where the line might be.
					while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
					{
						Thread.yield();
					}
				}
				break;
			case RightTurn:
				//No right turn logic yet, and Direction is never set to RightTurn yet.
				throw new RuntimeException("[" + config.getTime() + "]"
						+ "TurnBehavior: Went into right turn case.");
			case LeftTurn:	
				int rotation = -24;
				
				//Travel slightly forward to get in the middle of the intersection
				pilot.travel(3.5);
				//a small [rotation] degree turn followed by a [rotation] degree turn
				pilot.rotate(rotation, true);
					
				//While rotating and doesn't see the foreground...
				while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
				{
					Thread.yield();
				}
				
				//If a foreground colored line was found right after the left turn began
				if(colorAdapter.getColorID() == foreground)
				{
					//Rotate until just past the line
					pilot.rotate(-20, true);
					
					//While rotating and the color sensor sees the foreground color...
					while(pilot.isMoving() && colorAdapter.getColorID() == foreground)
					{
						Thread.yield();
					}
				}
				
				//finish the turn (the rotation in degrees evaluates negative)
				pilot.rotate(Math.abs(rotation)-130, true);
				
				while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
				{
					Thread.yield();
				}
				
				//Check if it never saw the foreground during the turn
				if(colorAdapter.getColorID() != foreground)
				{	
					//Rotate back to the starting point
					pilot.rotate(130-Math.abs(rotation), true);
						
					while(pilot.isMoving() && colorAdapter.getColorID() != foreground)
					{
						Thread.yield();
					}
					
					det.makeReport(Direction.DeadEnd);
					return;
				}
				break;
			default:
				throw new RuntimeException("[" + config.getTime() + "]"
						+ "TurnBehavior: Went into the default case in action method.");
		}
		
		pilot.stop();
		scannerMotor.rotateTo(20);
		scannerMotor.flt();
		det.makeReport(Direction.Straight);
		det.start();
	}

	@Override
	public void suppress() 
	{
		System.out.println("[" + config.getTime() + "] TurnBehavior: Being suppressed");
	}
	
	@Override
	public void becomeListener(CenterlineDetector det) 
	{
		det.addListener(this);
	}

	@Override
	public void report(Direction direction) 
	{
		this.direction = direction;
	}
}
