# LHU Robotics Team

This is a respository for code dumping intended for use only by the LHU Robotics Team.

<b>Armold physical build version:</b>
- Build: 3
  - Revision: 1

<b>Any notes to better the cause or guidelines should go down here:</b>

- Speed should NOT surpass 15
  - Reason: Robot floors it and plants itself into a wall.
- Traveling distance for the robot must be a negative number.
  - Reason: Robot is technically moving backwards.
- Input IDs:
  - 1 = up
  - 2 = down
  - 4 = center
  - 8 = right
  - 16 = left
  - 32 = back
- End all programs with System.exit(0);
  - Reason: To prevent Armold from hard crashing
