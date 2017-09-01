# LHU Robotics Team

This is a respository for code dumping, issue tracking, and various documentation storing intended for use by the LHU Robotics Team.

<b><i>Armold</i> physical build version:</b>
- Build: 3
  - Revision: 1

<b>Any notes to better the cause or guidelines should go down here:</b>

- Speed should NOT surpass 15
  - Reason: Robot floors it and plants itself into a wall.
- Traveling distance for the robot must be a negative number.
  - Reason: Robot is technically moving backwards.
- Input IDs:
  - 1 = up
  - 2 = center
  - 4 = down
  - 8 = right
  - 16 = left
  - 32 = back
- End all programs with System.exit(0);
  - Reason: To prevent Armold from hard crashing
- A Netgear WNA1100 WiFi dongle is currently the only WiFi dongle that is supported by the EV3’s firmware.
  - Source: https://github.com/mindboards/ev3sources/blob/master/README.md
