package arbitrator;

import java.util.Timer;
import java.util.TimerTask;

import utils.RobotConfig;

public class Arbitrator
{
	private final RobotConfig config;
	private final Behavior[] behaviors;
	private short inAction = -1;
	private Timer controlTimer;
	
	/**
	 * Priority is as follows: highest priority <-----> lowest priority
	 * @param behaviors
	 */
	public Arbitrator(RobotConfig config, Behavior[] behaviors)
	{
		if(behaviors == null || behaviors.length == 0)
		{
			throw new RuntimeException("Arbitrator: Behavior passed to the arbitrator are empty");
		}
		
		if(config == null)
		{
			throw new RuntimeException("Arbitrator: RobotConfig passed to Arbitator is null");
		}
		
		this.behaviors = behaviors;
		this.config = config;
	}
	
	public void startArbitrator(short delay)
	{
		TimerTask checkControls = new TimerTask()
		{
			@Override
			public void run()
			{
				for(short i = 0; i < behaviors.length; i++)
				{
					if(behaviors[i].control() == true && (inAction == -1 || i < inAction))
					{
						behaviors[i].action();
						inAction = i;
						return;
					}
				}
				
				stopArbitrator();
				config.getDebugger().printToScreen("Arbitrator: No behaviors requested control.");
			}
		};
		controlTimer = new Timer();
		controlTimer.schedule(checkControls, delay, delay);
	}
	
	public void stopArbitrator()
	{
		controlTimer.cancel();
	}
}
