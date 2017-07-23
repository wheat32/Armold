package utils;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class UserInput
{
	private Timer service;

	public UserInput(int delay)
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
						System.out.println("UserInput: Died from back button press.");
						Sound.beep();
						System.exit(0);
					break;
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
