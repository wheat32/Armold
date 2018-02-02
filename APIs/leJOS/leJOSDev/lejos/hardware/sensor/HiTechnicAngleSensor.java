package lejos.hardware.sensor;

import lejos.hardware.port.I2CPort;
import lejos.hardware.port.Port;
import lejos.utility.Delay;
import lejos.utility.EndianTools;

/**
 * <b>Hitechnic Angle sensor</b><br>
 * The Angle sensor measures axle rotation position and rotation speed.
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
 * <td>Absolute angle</td>
 * <td>Measures the rotation position of a rotating axle</td>
 * <td>Degrees (0-359)</td>
 * <td> {@link #getAngleMode() }</td>
 * </tr>
 * <tr>
 * <td>Accumulated angle</td>
 * <td>Measures the accumulated number of degrees an axle has rotated</td>
 * <td>Degrees</td>
 * <td> {@link #getAccumulatedAngleMode() }</td>
 * </tr>
 * <tr>
 * <td>Angular velocity</td>
 * <td>the speed of the axle rotation</td>
 * <td>rotations / second</td>
 * <td> {@link #getAngularVelocityMode() }</td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * <b>Sensor configuration</b><br>
 * The zero position of the sensor can be set and stored in the sensors memory
 * using the calibrateAngle method. The accumulated angle can be set to zero
 * using the resetAccumulatedAngle method. The accumulated angle is not stored
 * in memory.
 * 
 * <p>
 * 
 * See <a
 *      href="http://www.hitechnic.com/cgi-bin/commerce.cgi?preadd=action&key=NAA1030">
 *      Sensor Product page </a>
 * See <a href="http://sourceforge.net/p/lejos/wiki/Sensor%20Framework/"> The
 *      leJOS sensor framework</a>
 * See {@link lejos.robotics.SampleProvider leJOS conventions for
 *      SampleProviders}
 * 
 *      <p>
 * 
 * 
 * 
 */
public class HiTechnicAngleSensor extends I2CSensor {
  protected static final int REG_ANGLE              = 0x42;
  protected static final int REG_ACCUMULATED_ANGLE  = 0x44;
  protected static final int REG_SPEED              = 0x48;
  protected static final int HTANGLE_MODE_CALIBRATE = 0x43;
  protected static final int HTANGLE_MODE_RESET     = 0x52;

  private byte               buf[]                  = new byte[4];

  public HiTechnicAngleSensor(I2CPort port) {
    super(port, DEFAULT_I2C_ADDRESS);
    init();
  }

  public HiTechnicAngleSensor(Port port) {
    super(port);
    init();
  }

  protected void init() {
    setModes(new SensorMode[] { new AngleMode(), new AccumulatedAngleMode(),
        new AngularVelocityMode() });
  }

  /**
   * Reset the rotation count of accumulated angle to zero. Not saved in EEPORM.
   */
  public void resetAccumulatedAngle() {
    sendData(0x41, (byte) HTANGLE_MODE_RESET);
  }

  /**
   * Calibrate the zero position of angle. Zero position is saved in EEPROM on
   * sensor. Thread sleeps for 50ms while that is done.
   */
  public void calibrateAngle() {
    sendData(0x41, (byte) HTANGLE_MODE_CALIBRATE);
    Delay.msDelay(50);
  }

  /**
   * <b>HiTechnic angle sensor, Angle mode</b><br>
   * Measures the rotation position of a rotating axle.
   * 
   * <p>
   * <b>Size and content of the sample</b><br>
   * The sample contains one elements containing the angle of the sensor in the range of 0 to 359 degrees.
   * 

   */  
  public SensorMode getAngleMode() {
    return getMode(0);
  }

  private class AngleMode implements SensorMode {

    @Override
    public int sampleSize() {
      return 1;
    }

    @Override
    public void fetchSample(float[] sample, int offset) {
      getData(REG_ANGLE, buf, 2);
      int bits9to2 = buf[0] & 0xFF;
      int bit1 = buf[1] & 0x01;

      sample[offset] = (bits9to2 << 1) | bit1;
    }

    @Override
    public String getName() {
      return "Angle";
    }
  }

  /**
   * <b>HiTechnic angle sensor, Accumulated angle mode</b><br>
   * Measures the accumulated number of degrees an axle has rotated.
   * 
   * <p>
   * <b>Size and content of the sample</b><br>
   * The sample contains one elements containing the accumulated rotation (in degrees) of the axle.
   */  
  public SensorMode getAccumulatedAngleMode() {
    return getMode(1);
  }

  private class AccumulatedAngleMode implements SensorMode {
    @Override
    public int sampleSize() {
      return 1;
    }

    @Override
    public void fetchSample(float[] sample, int offset) {
      getData(REG_ACCUMULATED_ANGLE, buf, 4);

      sample[offset] = -EndianTools.decodeIntBE(buf, 0);
    }

    @Override
    public String getName() {
      return "AccumulatedAngle";
    }
  }

  /**
   * <b>HiTechnic angle sensor, Angular velocity mode</b><br>
   * Measures the rotational speed of an axle.
   * 
   * <p>
   * <b>Size and content of the sample</b><br>
   * The sample contains one elements containing the angular velocity (in degrees) of the axle.
   */  
  public SensorMode getAngularVelocityMode() {
    return getMode(2);
  }

  private class AngularVelocityMode implements SensorMode {
    @Override
    public int sampleSize() {
      return 1;
    }

    @Override
    public void fetchSample(float[] sample, int offset) {
      getData(REG_SPEED, buf, 2);
      // 1 rpm = 360Â°/60sec = 6Â°/sec
      //FIXME shouldn't we multiply by 6 instead of dividing by 60?
      sample[offset] = -EndianTools.decodeShortBE(buf, 0) / 60f;
    }

    @Override
    public String getName() {
      return "AngularVelocity";
    }
  }
}
