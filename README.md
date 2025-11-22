#BLACKHOLE SIMULATOR

A Java Swing application simulating the physics of a black hole, including an accretion disk, event horizon, and particle physics.

![Project Demo](assest/blackhole_simulation.gif)



this Blackhole has three features:

feature 1: The movement of particle
feature 2: The particles have three colors - a.Cyan (far) b.Yellow(inner) c.Red(innermost)
feature 3: The accertion disc that form near the blackhole 

Visuals:
Distance-based coloring (Red = Close, Cyan = Far).
Motion trails for high-speed inner regions.

| Key / Action | Function |
| :--- | :--- |
| **🖱️ Left Click** | Spawn a dense cluster of particles at the cursor location. |
| **🖱️ Right Click** | **Reset Galaxy:** Clears the screen and re-spawns the stable accretion disk. |
| **⌨️ Spacebar** | **Clear:** Instantly removes all particles from the screen. |
| **⌨️ R Key** | **Reset:** Triggers a full simulation reset. |


!Compile the code

```bash
javac BlackHoleSimulation.java
```