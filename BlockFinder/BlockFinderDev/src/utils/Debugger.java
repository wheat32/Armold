package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import lejos.hardware.Button;
import lejos.hardware.Sound;

public class Debugger implements Thread.UncaughtExceptionHandler
{
	//Change this variable to get live debug feed to console
	private static boolean debuggingToConsole = false;
	
	private static boolean debugToScreenEnabled = true;
	private static RobotConfig config;
	private static StringBuilder outputColl = new StringBuilder();
	
	public static void setConfig(RobotConfig config)
	{
		Debugger.config = config;
	}
	
	public static void debugPrompt()
	{
		if(debuggingToConsole == true)
		{
			return;
		}
		
		boolean buttonPressed = false;
		boolean onYes = true;
		
		System.out.println("\n\n\n\n");
		System.out.println("Do you want debug\nto screen\nenabled?");
		System.out.print(">Yes     No");
		
		loop: while(true)
		{
			if(buttonPressed == true)
			{
				System.out.println("\n\n\n\n");
				System.out.println("Do you want debug\nto screen\nenabled?");
				
				if(onYes == true)
				{
					System.out.print(">Yes     No");
				}
				else
				{
					System.out.print(" Yes    >No");
				}
				buttonPressed = false;
			}
			
			switch(Button.getButtons())
			{
				case 2://Center/Confirm button
					System.out.println("\n\n\n\n\n\n\n");
					Sound.twoBeeps();
					break loop;
				case 8://Right button
					if(onYes == true)
					{
						buttonPressed = true;
						onYes = false;
						Sound.beep();
					}
					break;
				case 16://Left button
					if(onYes == false)
					{
						buttonPressed = true;
						onYes = true;
						Sound.beep();
					}
					break;
			}
			
			//Thread.sleep to prevent burning up the CPU
			try 
			{
				Thread.sleep(50);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		debugToScreenEnabled = onYes;
	}
	
	public static void printToScreen(String str)
	{
		outputColl.append("[" + config.getTime() + "] " + str + "\n");
		
		if(debugToScreenEnabled == true)
		{
			System.out.println("[" + config.getTime() + "] " + str);
		}
	}
	
	public static void exit(boolean immediateExit)
	{
		File file = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		try 
		{
			file = new File("output.txt");
			
			if(file.exists() == true && file != null)
			{
				file.delete();
			}
			
			file.createNewFile();
			
			fw = new FileWriter(file.getPath());
			bw = new BufferedWriter(fw);
			
			if(bw != null)
			{
				bw.write(outputColl.toString()); 
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
				fw.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		if(immediateExit == true)
		{
			System.exit(0);
		}
		else
		{
			while(Button.getButtons() != 32)
			{
				try 
				{
					Thread.sleep(50);
				} 
				catch (InterruptedException e)
				{
					System.exit(0);
				}
			}
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) 
	{
		Sound.buzz();
		// Get rid of invocation exception
	    if (e.getCause() != null)
	    {
	    	e = e.getCause();
	    }
	    // Send stack trace to the menu so it goes to EV3Console etc.
		
		System.out.println(e.getClass().getName());
		if (e.getMessage() != null) 
		{
			System.out.println(e.getMessage());
			outputColl.append(e.getMessage() + "\n");
		}
		
		if (e.getCause() != null)
		{
			System.out.println("Caused by:");
			System.out.println(e.getCause().toString());
			outputColl.append("Caused by:\n");
			outputColl.append(e.getCause().toString() + "\n");
		}
		
		StackTraceElement[] trace = e.getStackTrace();
		for(int i = 0; i < trace.length ;i++) 
		{
			System.out.println(trace[i].toString());
			outputColl.append(trace[i].toString() + "\n");
		}
	    
		exit(false);
	}
}