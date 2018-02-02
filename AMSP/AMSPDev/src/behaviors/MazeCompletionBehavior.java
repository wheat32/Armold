package behaviors;

import java.io.File;

import centerLineDetection.CenterLineDetector;
import centerLineDetection.CenterLineListener;
import lejos.hardware.Sound;
import lejos.robotics.ColorAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;

public class MazeCompletionBehavior implements Behavior, CenterLineListener
{
	private RobotConfig config;
	private Debugger debugger;
	private ColorAdapter colorAdapter;
	private Direction direction;
	private long timestamp = 0;//TODO add Javadoc for this variable
	
	public MazeCompletionBehavior(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
		colorAdapter = config.getColorAdapter();
	}
	
	/**
	 * <b>This <code>takeControl()</code> method returns <code>true</code> whenever the 
	 * <code>Direction</code> is <code>Finish</code> or whenever it sees the finish line color 
	 * <i>(after a certain amount of time)</i>.</b>
	 * <p>
	 * If the <code>Direction</code> most recently reported is <code>Finish</code>, this method will return
	 * <code>true</code>.
	 * </br>
	 * It will also check for the finish line color, but every time it does check, it must wait n seconds 
	 * before being able to check again. After waiting, if it sees the finish line color and the 
	 * <code>CenterlineDetector</code> does not have a scan running, it will return 
	 * <code>true</code>.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.1 </br>
	 *        Last modified: 2.2.2
	 */
	@Override
	public boolean takeControl()
	{
		if(direction == Direction.Finish)
		{
			return true;
		}
		//Must wait at least 2 seconds before being able to scan again
		else if(System.currentTimeMillis()-timestamp >= 3000)
		{
			if(config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.finishColor) == true)
			{
				timestamp = System.currentTimeMillis();
				return true;
			}
		}
		
		return false;
	}

	/**
	 * <b>When this <code>action()</code> method gets called, it will check if it is at the finish line and 
	 * call <code>wrapUp()</code> if it is.</b>
	 * <p>
	 * This method will stop the scans in the <code>CenterlineDetector</code> class and call the 
	 * <code>RobotConfig</code> class to stop moving the robot.
	 * </br>
	 * Firstly, if the current <code>Direction</code> is <code>Finish</code>, then it will call the 
	 * <code>wrapUp()</code> method which ends the program after a few instructions.
	 * </br>
	 * If the <code>Direction</code> is not <code>Finish</code>, then it will check again to see if the color sensor 
	 * still sees the finish line color. If it does, it 
	 * will request a scan for the finish line through the <code>CenterlineDetector</code> class. If it sees 
	 * the finish line, it will call the <code>wrapUp()</code> method to end the program.
	 * </br>
	 * If the scan came back negative, it will reset the <code>timestamp</code> variable (which controls how
	 * long this object must wait to be able to request another scan), call the
	 * <code>RobotConfig</code> to slightly reverse the robot, and resume the scans in the
	 * <code>CenterlineDetector</code> class.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.1 </br>
	 *        Last modified: 2.2.2
	 */
	@Override
	public void action() 
	{
		debugger.printToScreen("MazeCompletionBehavior: In action method.");
		
		config.getMovePilotInstance().stop();
		
		if(direction == Direction.Finish)
		{
			wrapUp();
		}
		else if(config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.finishColor) == true)
		{

			
			debugger.printToScreen("MazeCompletionBehavior: Requesting scan for finish line.");
			
			Sound.beep();
			
			if(requestFinishScan() == Direction.Finish)
			{
				wrapUp();
			}
		}
		
		debugger.printToScreen("MazeCompletionBehavior: False flag raised. TimeStamp reset.");
		
		timestamp = System.currentTimeMillis();
		config.getMovePilotInstance().travel(-0.4);
	}

	@Override
	public void suppress() 
	{
		//NO SUPPRESS REQUIRED
	}
	
	/**
	 * <b>When this method plays a sound, prints to the console, and exits the program.</b>
	 * 
	 * @author Nick
	 * @param None
	 * @return Nothing
	 * @since 2.1.1 </br>
	 *        Last modified: Never
	 */
	private void wrapUp()
	{
		debugger.printToScreen("MazeCompletionBehavior: Finished Maze!");
		
		final File soundFile = new File("FF7Win.wav");
		Sound.playSample(soundFile, 100);
		
		Sound.beepSequence();
		debugger.exit(true);
	}
	
	@Override
	public void becomeListener(CenterLineDetector det) 
	{
		det.addListener(this);
	}

	@Override
	public void getReport(Direction direction) 
	{
		this.direction = direction;
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
		config.getMovePilotInstance().stop();
		debugger.printToScreen("CenterlineDetector: Finish line scan requested.");

		if(config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.finishColor) == false)
		{
			debugger.printToScreen("CenterlineDetector: Request result: False alarm.");
			return Direction.Straight;
		}

		config.getColorScannerMotor().rotate(-45, true);

		while(config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.finishColor) == true
				&& config.getColorScannerMotor().isMoving())
		{
			Thread.yield();
		}

		if(config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.finishColor) == true)
		{
			config.getColorScannerMotor().rotateTo(0);
			config.getColorScannerMotor().flt();
			return Direction.Finish;
		}
		else
		{
			config.getColorScannerMotor().rotateTo(0);
			config.getColorScannerMotor().rotate(20);
			config.getColorScannerMotor().flt();
			debugger.printToScreen("CenterlineDetector: Request result: False alarm.");
			return Direction.Straight;
		}
	}
}