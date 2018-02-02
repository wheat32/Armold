package behaviors;

import centerLineDetection.CenterLineDetector;
import centerLineDetection.IntersectionType;
import lejos.hardware.Sound;
import lejos.robotics.ColorAdapter;
import lejos.robotics.subsumption.Behavior;
import utils.Debugger;
import utils.RobotConfig;

public class EmergencyBehavior implements Behavior, IntersectionType
{
	private RobotConfig config;
	private Debugger debugger;
	private ColorAdapter colorAdapter;
	private CenterLineDetector det;
	
	public EmergencyBehavior(RobotConfig config, CenterLineDetector det)
	{
		this.config = config;
		this.det = det;
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
		return (config.getSensorUtils().checkColorRange(colorAdapter.getColor(), config.borderColor) == true) 
				? true : false;
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
		Sound.beep();
		debugger.printToScreen("EmergencyBehavior: Boarder recovery activated.");
		config.getMovePilotInstance().stop();
		config.getMovePilotInstance().travel(-3);
		det.makeReport(Direction.DeadEnd);
		return;
	}

	@Override
	public void suppress() 
	{
		//The suppress() method in this class should never have an effect as recovering has priority.
		debugger.printToScreen("EmergencyBehavior: Boarder recovery suppressed.");
	}
}