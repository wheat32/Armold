package behaviors;

import centerLineDetection.CenterLineDetector;
import centerLineDetection.CenterLineListener;
import centerLineDetection.IntersectionType;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;

public class ForwardBehavior implements Behavior, CenterLineListener, IntersectionType 
{
	private RobotConfig config;
	private Debugger debugger;
	private Direction direction;

	public ForwardBehavior(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
	}

	/**
	 * <b>JAVADOC IS OUT OF DATE</b>
	 * <p>
	 * <b>This <code>takeControl()</code> method returns true whenever no scan is happening inside the 
	 * <code>CenterlineDetector</code> and if the color sensor does <i>not</i> see the foreground, 
	 * border, or finish color.</b>
	 * 
	 * @author Krish, Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.0 </br>
	 *        Last modified: 2.2.4
	 */
	@Override
	public boolean takeControl()
	{
		return (direction == Direction.Straight) ? true : false;
	}

	/**
	 * <b>JAVADOC IS OUT OF DATE</b>
	 * <p>
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
	 *        Last modified: 2.2.4
	 */
	@Override
	public void action() 
	{
		if(direction == Direction.Straight && config.getMovePilotInstance().isMoving() == false)
		{
			debugger.printToScreen("ForwardBehavior: Driving forward...");
			config.getMovePilotInstance().forward();
		}
		else if(direction != Direction.Straight)
		{
			debugger.printToScreen("ForwardBehavior: Stopping motion...");
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

	@Override
	public void getReport(Direction direction)
	{
		this.direction = direction;
	}

	@Override
	public void becomeListener(CenterLineDetector det)
	{
		det.addListener(this);
	}
}