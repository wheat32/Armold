package behaviors;

import centerlineDetection.CenterlineDetector;
import centerlineDetection.IntersectionType;
import lejos.robotics.ColorAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;

public class EmergencyBehavior implements Behavior, IntersectionType
{
	private RobotConfig config;
	private Debugger debugger;
	private ColorAdapter colorAdapter;
	private CenterlineDetector det = CenterlineDetector.getInstance();
	
	public EmergencyBehavior(RobotConfig config)
	{
		this.config = config;
		debugger = config.getDebugger();
		colorAdapter = new ColorAdapter(config.getColorSensor());
	}
	
	/**
	 * <b>This <code>takeControl()</code> method returns true whenever the color sensor sees the border color.</b>
	 * <p>
	 * When the color sensor sees the border color, it will stop the scans in the <code>CenterlineDetector</code> 
	 * class and then return <code>true</code>.
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return boolean
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.0
	 */
	@Override
	public boolean takeControl() 
	{		
		if(colorAdapter.getColorID() == config.getBorderColor())
		{
			det.stop();
			return true;
		}
		return false;
	}

	/**
	 * <b>When this <code>action()</code> method gets called, it will make the robot reverse slightly, and 
	 * manually send the direction <code>Direction.DeadEnd</code> in a report.</b>
	 * 
	 * @author Caroline, Nick
	 * @param None
	 * @return Nothing
	 * @since 1.0.0 </br>
	 *        Last modified: 2.1.0
	 */
	@Override
	public void action() 
	{
		debugger.printToScreen("EmergencyBehavior: Boarder recovery initialized.");
		config.getMovePilotInstance().stop();
		config.getMovePilotInstance().travel(-3);
		det.makeReport(Direction.DeadEnd);
	}

	@Override
	public void suppress() 
	{
		//The suppress() method in this class should never have an effect as recovering has priority.
		debugger.printToScreen("EmergencyBehavior: Boarder recovery suppressed.");
	}
}