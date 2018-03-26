package behaviors;

import arbitrator.Behavior;
import utils.RobotConfig;

public class IntersectionDetector implements Behavior
{
	private final RobotConfig config;
	
	public IntersectionDetector(RobotConfig config)
	{
		this.config = config;
	}

	@Override
	public boolean control()
	{
		
		return false;
	}

	@Override
	public void action()
	{

	}
}
