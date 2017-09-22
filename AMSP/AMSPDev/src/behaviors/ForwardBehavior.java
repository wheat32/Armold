package behaviors;

import centerlineDetection.CenterlineDetector;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;
import utils.SensorUtils;

public class ForwardBehavior implements Behavior 
{
	private RobotConfig config;
	private Debugger debugger;
	private SensorUtils sensorUtils;
	private CenterlineDetector det = CenterlineDetector.getInstance();
	private ColorAdapter colorAdapter;

	public ForwardBehavior(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
		sensorUtils = config.getSensorUtils();
		colorAdapter = new ColorAdapter(config.getColorSensor());
	}

	/**
	 * <b>This <code>takeControl()</code> method returns true whenever no scan is happening inside the 
	 * <code>CenterlineDetector</code> and if the color sensor does <i>not</i> see the foreground, 
	 * border, or finish color.</b>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.0 </br>
	 *        Last modified: 2.2.2
	 */
	@Override
	public boolean takeControl()
	{
		if(det.getIsScanning() == false)
		{
			Color color = colorAdapter.getColor();
			
			if(sensorUtils.checkColorRange(color, config.getForegroundColor()) == false
				&& sensorUtils.checkColorRange(color, config.getBorderColor()) == false
				&& sensorUtils.checkColorRange(color, config.getFinishColor()) == false)
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * <b>When this <code>action()</code> method gets called, the robot will either begin to drive forward or
	 * stop depending on whether the <code>CenterlineDetector</code> is scanning or not.</b>
	 * <p>
	 * If the <code>CenterlineDetector</code> is <i>not</i> scanning and the robot is <i>not</i> currently 
	 * moving, then it will tell the <code>config</code> to start moving the robot.
	 * </br>
	 * If the <code>CenterlineDetector</code> <i>is</i> scanning and the robot <i>is</i> currently moving,
	 * then it will tell the <code>config</code> to stop the robot.
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.2.2
	 */
	@Override
	public void action() 
	{
		if(det.getIsScanning() == false && config.getMovePilotInstance().isMoving() == false)
		{
			debugger.printToScreen("ForwardBehavior: Driving forward...");
			config.getMovePilotInstance().forward();
		}
		else if(det.getIsScanning() == true && config.getMovePilotInstance().isMoving() == true 
				&& sensorUtils.checkColorRange(colorAdapter.getColor(), config.getForegroundColor()) == true)
		{
			debugger.printToScreen("ForwardBehavior: Stopping forward motion.");
			config.getMovePilotInstance().stop();
		}
	}

	/**
	 * <b>When this <code>suppress()</code> method gets called, the robot will stop moving.</b>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.0
	 */
	@Override
	public void suppress() 
	{
		debugger.printToScreen("ForwardBehavior: Suppressing forward behavior.");
		config.getMovePilotInstance().stop();
	}
}