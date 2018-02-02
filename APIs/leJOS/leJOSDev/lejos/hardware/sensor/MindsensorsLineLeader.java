package lejos.hardware.sensor;

import lejos.hardware.port.I2CPort;
import lejos.hardware.port.Port;

/**
 * <b>MindSensors Line Follower Sensor </b><br>
 * This sensor an array of 8 sensors with controlled light source, returning you values of the sensor readings.
 * 
 * <p style="color:red;">
 * The code for this sensor has not been tested. Please report test results to
 * the <A href="http://www.lejos.org/forum/"> leJOS forum</a>.
 * </p>
 * 
 * <p>
 * <table border=1>
 * <tr>
 * <th colspan=4>Supported modes</th>
 * </tr>
 * <tr>
 * <th>Mode name</th>
 * <th>Description</th>
 * <th>unit(s)</th>
 * <th>Getter</th>
 * </tr>
 * <tr>
 * <td>Red</td>
 * <td>Measures the light value when illuminated with a red light source.</td>
 * <td>N/A, normalized</td>
 * <td> {@link #getRedMode() }</td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * <b>Sensor configuration</b><br>
 * The sensor can be calibrated for black and white using the calibrateWhite and calibrateBlack methods. <p>
 * The sensor can be put in and out of sleep mode (lights off) using the sleep method and wakeUp methods.
 * 
 * <p>
 * 
 * See <a href="http://www.mindsensors.com/index.php?module=documents&JAS_DocumentManager_op=downloadFile&JAS_File_id=1370"> Sensor datasheet </a>
 * See <a href="http://www.mindsensors.com/index.php?module=pagemaster&PAGE_user_op=view_page&PAGE_id=111"> Sensor Product page </a>
 * See <a href="http://sourceforge.net/p/lejos/wiki/Sensor%20Framework/"> The
 *      leJOS sensor framework</a>
 * See {@link lejos.robotics.SampleProvider leJOS conventions for
 *      SampleProviders}
 * 
 *      <p>
 * 
 * 
 * @author Juan Antonio Brenha Moral, Eric Pascual (EP)
 * 
 */
public class MindsensorsLineLeader extends I2CSensor {
	private byte[] buf = new byte[8];
	private final static byte COMMAND = 0x41;

    /**
     * Constructor
     *
     * @param port
     * @param address I2C address for the device
     */
    public MindsensorsLineLeader(I2CPort port, int address) {
        super(port, address);
        init();
    }

    /**
     * Constructor
     *
     * @param port
     */
    public MindsensorsLineLeader(I2CPort port) {
        this(port, DEFAULT_I2C_ADDRESS);
        init();
    }
    
    /**
     * Constructor
     *
     * @param port
     * @param address I2C address for the device
     */
    public MindsensorsLineLeader(Port port, int address) {
        super(port, address, TYPE_LOWSPEED_9V);
        init();
    }

    /**
     * Constructor
     *
     * @param port
     */
    public MindsensorsLineLeader(Port port) {
        this(port, DEFAULT_I2C_ADDRESS);
        init();
    }
    
    protected void init() {
    	setModes(new SensorMode[]{ new RedMode() });
    }
    
	/**
	 * Send a single byte command represented by a letter
	 * 
	 * @param cmd the command to be sent
	 */
	public void sendCommand(char cmd) {
		sendData(COMMAND, (byte) cmd);
	}

	/**
	 * Sleep the sensor
	 */
	public void sleep() {
		sendCommand('D');
	}

	/**
	 * Wake up the sensor
	 * 
	 */
	public void wakeUp() {
		sendCommand('P');
	}
	
	public void calibrateWhite() {
		sendCommand('W');
	}
	
	public void calibrateBlack() {
		sendCommand('B');
	}
	
  /**
   * Return a sample provider in that measures the light reflection of a surface illuminated with a red led light. The sensor houses 8 light sensors. Each element in the sample represents one light sensor.
   */
	public SensorMode getRedMode() {
		return getMode(0);
	}

	private class RedMode implements SensorMode {
	@Override
	public int sampleSize() {
		return 8;
	}

	@Override
	public void fetchSample(float[] sample, int offset) {
		getData(0x49,buf,8);
		
		for(int i=0;i<8;i++) {
			sample[offset+i] = buf[i]/100;
		}	
	}

	@Override
	public String getName() {
		return "Red";
	}
	}
	
}
