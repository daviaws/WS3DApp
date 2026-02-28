# WS3DApp

This is a bundled setup inspired in [WS3DApp](https://github.com/rgudwin/WS3DApp)

It contains the development kit for Gudwin discipline IA941.

Run with:

```
docker compose up
```

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