package utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.TimerListener;

public class UserInput
{
	private Timer service;

	public UserInput(int delay, final Debugger debugger)
	{
		//Every [delay] milliseconds, it will check if a button has been pressed on the EV3 unit.
		service = new Timer(delay, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				switch(Button.getButtons())
				{
					case 32:
						debugger.printToScreen("UserInput: Died from back button press.");
						service.stop();
						Sound.beep();
						debugger.exit(true);//Program ends on this
				}
			}
			
		});
		service.setInitialDelay(64);
		service.start();
	}
}