package behaviors;

import java.io.File;

import centerlineDetection.CenterlineDetector;
import centerlineDetection.CenterlineListener;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.ColorAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.RobotConfig;

public class MazeCompletionBehavior implements Behavior, CenterlineListener
{
	private RobotConfig config;
	private EV3ColorSensor sensor;
	private ColorAdapter colorAdapter;
	private Direction direction;
	private CenterlineDetector det = CenterlineDetector.getInstance();
	private long timestamp = 0;
	
	public MazeCompletionBehavior(RobotConfig config)
	{
		this.config = config;
		sensor = config.getColorSensor();
		colorAdapter = new ColorAdapter(sensor);
	}
	
	/**
	 * <b>This <code>takeControl()</code> method returns <code>true</code> whenever the 
	 * <code>Direction</code> is <code>Finish</code> or whenever it sees the finish line color 
	 * <i>(after a certain amount of time)</i></b>
	 * <p>
	 * If the <code>Direction</code> most recently reported is <code>Finish</code>, this method will return
	 * <code>true</code>.
	 * </br>
	 * It will also check for the finish line color, but every time it does check, it must wait n seconds 
	 * before being able to check again. After it has waited those n seconds, if it sees the finish line 
	 * color and the <code>CenterlineDetector</code> does not have a scan running, it will return 
	 * <code>true</code>.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.1 </br>
	 *        Last modified: 2.1.1
	 */
	@Override
	public boolean takeControl() 
	{
		if(direction == Direction.Finish)
		{
			return true;
		}
		//Must wait at least 2 seconds before being able to scan again
		else if(System.currentTimeMillis()-timestamp >= 2000)
		{
			if(colorAdapter.getColorID() == config.getFinishColor() && det.getIsScanning() == false)
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
	 * <code>wrapUp()</code> class which end the program after a few instructions.
	 * </br>
	 * If the <code>Direction</code> is not <code>Finish</code>, then it will check again if the color sensor 
	 * still sees the finish line color. If it does and there is a scan finishing up in the
	 * <code>CenterlineDetector</code> class, it will wait on that scan to finish before proceeding. Next, it 
	 * will request a scan for the finish line through the <code>CenterlineDetector</code> class. If it sees 
	 * the finish line, it will call the <code>wrapUp()</code> method to end the program.
	 * </br>
	 * If the scan came back negative, it will reset the <code>timestamp</code>, call the
	 * <code>RobotConfig</code> to slightly reverse the robot, and resume the scans in the
	 * <code>CenterlineDetector</code> class.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.1 </br>
	 *        Last modified: 2.1.1
	 */
	@Override
	public void action() 
	{
		System.out.println("[" + config.getTime() + "]"
				+ "MazeCompletionBehavior: In action method.");
		
		det.stop();
		config.getMovePilotInstance().stop();
		
		if(direction == Direction.Finish)
		{
			wrapUp();
		}
		else if(colorAdapter.getColorID() == config.getFinishColor())
		{
			while(det.getIsScanning())//Let the scan conclude
			{
				Thread.yield();
			}
			
			System.out.println("[" + config.getTime() + "]"
					+ "MazeCompletionBehavior: Requesting scan for finish line.");
			
			Sound.beep();
			
			if(det.requestFinishScan() == Direction.Finish)
			{
				wrapUp();
			}
		}
		
		System.out.println("[" + config.getTime() + "]"
				+ "MazeCompletionBehavior: False flag raised. TimeStamp reset.");
		
		timestamp = System.currentTimeMillis();
		config.getMovePilotInstance().travel(-0.4);
		det.start();
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
		System.out.println("[" + config.getTime() + "]"
				+ "MazeCompletionBehavior: Finished Maze!");
		
		final File soundFile = new File("FF7Win.wav");
		Sound.playSample(soundFile, 100);
		
		Sound.beepSequence();
		System.exit(0);
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
