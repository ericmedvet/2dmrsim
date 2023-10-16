
package io.github.ericmedvet.mrsim2d.buildable;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.mrsim2d.buildable.builders.*;

import java.util.List;

public class PreparedNamedBuilder {
  private final static NamedBuilder<Object> NB = NamedBuilder.empty()
      .and(io.github.ericmedvet.jsdynsym.buildable.PreparedNamedBuilder.get())
      .and(List.of("sim", "s"), NamedBuilder.empty()
          .and(NamedBuilder.fromUtilityClass(Misc.class))
          .and(List.of("sensor", "s"), NamedBuilder.fromUtilityClass(Sensors.class))
          .and(List.of("terrain", "t"), NamedBuilder.fromUtilityClass(Terrains.class))
          .and(List.of("agent", "a"), NamedBuilder.fromUtilityClass(Agents.class)
              .and(List.of("vsr"), NamedBuilder.empty()
                  .and(NamedBuilder.fromUtilityClass(VSRMisc.class))
                  .and(List.of("shape", "s"), NamedBuilder.fromUtilityClass(GridShapes.class))
                  .and(
                      List.of("sensorizingFunction", "sf"),
                      NamedBuilder.fromUtilityClass(VSRSensorizingFunctions.class)
                  )
                  .and(List.of("reactiveVoxels", "rv"), NamedBuilder.fromUtilityClass(ReactiveVoxels.class))
              )
              .and(List.of("legged", "l"), NamedBuilder.empty()
                  .and(NamedBuilder.fromUtilityClass(LeggedMisc.class))
              )
          )
          .and(List.of("task"), NamedBuilder.fromUtilityClass(Tasks.class)
              .and(List.of("locomotion", "l"), NamedBuilder.fromUtilityClass(LocomotionOutcomeFunctions.class))
              .and(List.of("jumping", "j"), NamedBuilder.fromUtilityClass(JumpingOutcomeFunctions.class))
              .and(List.of("piling", "p"), NamedBuilder.fromUtilityClass(PilingOutcomeFunctions.class))
              .and(List.of("balancing", "b"), NamedBuilder.fromUtilityClass(BalancingOutcomeFunctions.class))
          )
      );

  private PreparedNamedBuilder() {
  }

  public static NamedBuilder<Object> get() {
    return NB;
  }

}
