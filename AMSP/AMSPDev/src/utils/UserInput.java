package utils;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class UserInput
{
	private Timer service;

	public UserInput(int delay, final Debugger debugger)
	{
		//Every [delay] milliseconds, it will check if a button has been pressed on the EV3 unit.
		service = new Timer(delay, new TimerListener()
		{
			@Override
			public void timedOut() 
			{
				
				switch(Button.getButtons())
				{
					case 32:
						debugger.printToScreen("UserInput: Died from back button press.");
						service.stop();
						Sound.beep();
						debugger.exit(true);
						return;
				}
			}
		});
	}
	
	public void start()
	{
		service.start();
	}
	
	public void stop()
	{
		service.stop();
	}
}