# WS3DApp

This is a very first program to use with the new WS3D environment that can be run with:

```bash
#! /bin/bash
xhost +

XAUTH=`xauth info | grep file | awk '{print $3}'`

docker run --rm -it --name coppelia-sim \
    -e DISPLAY \
    --net=host \
    --device /dev/snd \
    --privileged \
    -v $XAUTH:/root/.Xauthority \
    -p 4011:4011 \
    brgsil/ws3d-coppelia
```

Save this code in a file with the name ws3d.sh and call

```bash
chmod ugo+x ws3d.sh
```
to make it executable.
Run
```bash
./ws3d.sh
```
to run it. 
Then, in a second shell, run
```bash
./gradlew build
```
to build the WS3DApp and
```bash
./gradlew run
```
to run it. 

## Features

- Real-time control of the creature using the keyboard.
- Use of the **arrow keys** for movement:
  - **↑ (Up Arrow)**: moves the creature forward.
  - **↓ (Down Arrow)**: moves the creature backward.
  - **← (Left Arrow)**: rotates the creature to the left.
  - **→ (Right Arrow)**: rotates the creature to the right.
- Use of the **space bar** to completely stop the creature’s movement.
- Direct integration with **WS3DProxy**, using differential wheel control commands (`move(vr, vl, w)`).
- Keyboard event handling implemented with **Swing Key Bindings (InputMap / ActionMap)**, ensuring proper behavior regardless of component focus.
- Simple Swing-based graphical interface for manual creature control.
- Informative tooltips on buttons, indicating the associated action and keyboard shortcut.
- Visualization of objects in the creature’s field of view.
- Use of the `getThingsInVision()` method from the `Creature` class to retrieve, in real time, all `Thing` objects currently visible to the creature.
- Display of the visible objects in the graphical interface as a dynamic list, updated periodically during execution.
- Each visible object is presented with its **name** and **spatial position (x, y)**, based on the object’s center of mass.
- The visualization is integrated into the control interface, allowing the user to monitor the creature’s perception of the environment while manually controlling its movement.