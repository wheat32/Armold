package arbitrator;

import java.util.Timer;
import java.util.TimerTask;

import utils.Debugger;

public class Arbitrator
{
	private final Behavior[] behaviors;
	/**
	 * If inAction is in -1, then no action is being ran
	 */
	private short inAction = -1;
	private Timer controlTimer;
	
	/**
	 * Priority is as follows: highest priority <-----> lowest priority
	 * @param behaviors
	 */
	public Arbitrator(Behavior[] behaviors)
	{
		if(behaviors == null || behaviors.length == 0)
		{
			throw new RuntimeException("Arbitrator: Behavior passed to the arbitrator are empty");
		}
		
		this.behaviors = behaviors;
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
					//(i < inAction) checks priority
					if(behaviors[i].control() == true && (inAction == -1 || i < inAction))
					{
						behaviors[i].action();
						inAction = i;
						return;
					}
				}
				
				//Check if no actions are being ran
				if(inAction == -1)
				{
					throw new RuntimeException("No behavior needs an action");
				}
				
				stopArbitrator();
				Debugger.printToScreen("Arbitrator: No behaviors requested control.");
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
