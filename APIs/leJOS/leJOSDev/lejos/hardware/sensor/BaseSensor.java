package lejos.hardware.sensor;

import java.util.ArrayList;

import lejos.hardware.Device;


public class BaseSensor extends Device implements SensorModes
{
    protected int currentMode = 0;
    protected SensorMode[] modes;
    ArrayList<String> modeList;

    /**
     * Define the set of modes to be made available for this sensor.
     * @param m An array containing a list of modes
     */
    protected void setModes(SensorMode[] m)
    {
        modes = m;
        // force the list to be rebuilt
        modeList = null;
        currentMode = 0;
    }

    @Override
    public ArrayList<String> getAvailableModes()
    {
        if (modeList == null)
        {
            modeList = new ArrayList<String>(modes.length);
            if (modes != null)
                for(SensorMode m : modes)
                    modeList.add(m.getName());
        }
        return modeList;
    }

    @Override
    public SensorMode getMode(int mode)
    {
        if (mode < 0)
            throw new IllegalArgumentException("Invalid mode " + mode);
        if (modes == null || mode >= modes.length)
            return null;
        return modes[mode];
    }

    @Override
    public SensorMode getMode(String modeName)
    {
        // TODO: I'm sure there is a better way to do this, but it is late!
        int i = 0;
        for(String s : getAvailableModes())
        {
            if (s.equals(modeName))
                return modes[i];
            i++;
        }
        throw new IllegalArgumentException("No such mode " + modeName);
    }
    
    private boolean isValid(int mode) {
      if (mode < 0 || modes == null || mode >= modes.length) return false;
      return true;
    }
    
    private int getIndex(String modeName) {
      int i = 0;
      for(String s : getAvailableModes())
      {
          if (s.equals(modeName))
              return i;
          i++;
      }
      return -1;
    }
    
    @Override
    public String getName() {
      return modes[currentMode].getName();
    }

    @Override
    public int sampleSize() {
      return modes[currentMode].sampleSize();
    }

    @Override
    public void fetchSample(float[] sample, int offset) {
      modes[currentMode].fetchSample(sample, offset);
    }

    @Override
    public void setCurrentMode(int mode) {
      if (!isValid(mode)) {
        throw new IllegalArgumentException("Invalid mode " + mode);
      }
      else {
        currentMode = mode;
      }
    }

    @Override
    public void setCurrentMode(String modeName) {
      int mode = getIndex(modeName);
      if (mode==-1) { 
        throw new IllegalArgumentException("Invalid mode " + modeName);
      }
      else {
        currentMode = mode;
      }
    }

    @Override
    public int getCurrentMode() {
      return currentMode;
    }

    @Override
    public int getModeCount() {
      return modes.length;
    }

}
