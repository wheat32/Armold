package behaviors;

import arbitrator.Behavior;
import utils.RobotConfig;

public class MoveForward implements Behavior
{
	private final RobotConfig config;
	
	public MoveForward(RobotConfig config)
	{
		this.config = config;
	}
	
	@Override
	public boolean control()
	{
		return true;
	}

	@Override
	public void action()
	{
		config.getMovePilotInstance().forward();
	}
}
