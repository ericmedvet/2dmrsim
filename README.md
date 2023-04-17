# 2-D Multi Robot Simulator (2D-MR-Sim)
This project is an extension of [2D-VSR-Sim](https://github.com/ericmedvet/2dhmsr) [1,2].

With respect to 2d-VSR-Sim, 2D-MR-Sim:
- allows to simulate more kinds of robots (not only Voxel-based Soft Robots);
- better models the concept of modularity;
- is decoupled from the inner physics simulator.

## Usage

Add (at least) this to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.ericmedvet</groupId>
    <artifactId>mrsim2d.core</artifactId>
    <version>0.8.3-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>io.github.ericmedvet</groupId>
    <artifactId>mrsim2d.engine.dyn4j</artifactId>
    <version>0.8.3-SNAPSHOT</version>
</dependency>
```

See [2D-robot-evolution](https://github.com/ericmedvet/2d-robot-evolution), that uses 2d-MR-Sim, for a more detailed documentation.

## References
1. Medvet, Bartoli, De Lorenzo, Seriani; [2D-VSR-Sim: a Simulation Tool for the Optimization of 2-D Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2020-j-mbds-vsr/); SoftwareX; 2020
2. Medvet, Bartoli, De Lorenzo, Seriani; [Design, Validation, and Case Studies of 2D-VSR-Sim, an Optimization-friendly Simulator of 2-D Voxel-based Soft Robots](https://medvet.inginf.units.it/publications/2020-p-mbds-design/); arXiv; 2020
