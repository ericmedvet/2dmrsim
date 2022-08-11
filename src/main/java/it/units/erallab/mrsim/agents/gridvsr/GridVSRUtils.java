/*
 * Copyright 2022 Eric Medvet <eric.medvet@gmail.com> (as eric)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.units.erallab.mrsim.agents.gridvsr;

import it.units.erallab.mrsim.core.actions.*;
import it.units.erallab.mrsim.core.bodies.Voxel;
import it.units.erallab.mrsim.util.Grid;
import it.units.erallab.mrsim.util.StringUtils;

import java.util.*;
import java.util.function.Function;

/**
 * @author "Eric Medvet" on 2022/07/09 for 2dmrsim
 */
public class GridVSRUtils {

  private final static Map<String, Function<Voxel, Sense<? super Voxel>>> SENSORS = Map.ofEntries(
      Map.entry("v0", v -> new SenseRotatedVelocity(0, v)),
      Map.entry("v90", v -> new SenseRotatedVelocity(Math.PI / 2d, v)),
      Map.entry("sd0", v -> new SenseDistanceToBody(0, 1, v)),
      Map.entry("sd90", v -> new SenseDistanceToBody(Math.PI / 2d, 1, v)),
      Map.entry("sd180", v -> new SenseDistanceToBody(Math.PI, 1, v)),
      Map.entry("sd270", v -> new SenseDistanceToBody(Math.PI / 2d * 3d, 1, v)),
      Map.entry("ld0", v -> new SenseDistanceToBody(0, 8, v)),
      Map.entry("ld90", v -> new SenseDistanceToBody(Math.PI / 2d, 8, v)),
      Map.entry("ld180", v -> new SenseDistanceToBody(Math.PI, 8, v)),
      Map.entry("ld270", v -> new SenseDistanceToBody(Math.PI / 2d * 3d, 8, v)),
      Map.entry("ar", SenseAreaRatio::new),
      Map.entry("a", SenseAngle::new),
      Map.entry("ln", v -> new SenseSideCompression(Voxel.Side.N, v)),
      Map.entry("le", v -> new SenseSideCompression(Voxel.Side.E, v)),
      Map.entry("ls", v -> new SenseSideCompression(Voxel.Side.S, v)),
      Map.entry("lw", v -> new SenseSideCompression(Voxel.Side.W, v)),
      Map.entry("attn", v -> new SenseSideAttachment(Voxel.Side.N, v)),
      Map.entry("atte", v -> new SenseSideAttachment(Voxel.Side.E, v)),
      Map.entry("atts", v -> new SenseSideAttachment(Voxel.Side.S, v)),
      Map.entry("attw", v -> new SenseSideAttachment(Voxel.Side.W, v)),
      Map.entry("t", SenseContact::new),
      Map.entry("sin1", v -> new SenseSinusoidal(1, 0, v))
  );

  private GridVSRUtils() {
  }

  public static Grid<List<Function<Voxel, Sense<? super Voxel>>>> buildSensors(String name, Grid<Boolean> shape) {
    String sensorsRegex = "("
        + String.join("|", SENSORS.keySet()) + ")(\\+("
        + String.join("|", SENSORS.keySet())
        + "))*";
    Function<String, Optional<Grid<List<Function<Voxel, Sense<? super Voxel>>>>>> provider =
        StringUtils.formattedProvider(
            Map.ofEntries(
                Map.entry("empty", p -> {
                  List<Function<Voxel, Sense<? super Voxel>>> sensors = List.of();
                  return shape.map(b -> b == null ? null : sensors);
                }),
                Map.entry("uniform-(?<sensors>" + sensorsRegex + ")", p -> {
                  List<Function<Voxel, Sense<? super Voxel>>> sensors = Arrays.stream(p.s().get("sensors").split("\\+"))
                      .map(SENSORS::get)
                      .toList();
                  return shape.map(b -> b == null ? null : sensors);
                }),
                Map.entry("top-(?<topSensors>" + sensorsRegex + ")-bottom-(?<bottomSensors>" + sensorsRegex + ")" +
                    "-front-" +
                    "(?<frontSensors" +
                    ">" + sensorsRegex + ")", p -> {
                  List<Function<Voxel, Sense<? super Voxel>>> topSensors = Arrays.stream(p.s()
                          .get("topSensors")
                          .split("\\+"))
                      .map(SENSORS::get)
                      .toList();
                  List<Function<Voxel, Sense<? super Voxel>>> bottomSensors = Arrays.stream(p.s()
                          .get("bottomSensors")
                          .split("\\+"))
                      .map(SENSORS::get)
                      .toList();
                  List<Function<Voxel, Sense<? super Voxel>>> frontSensors = Arrays.stream(p.s()
                          .get("frontSensors")
                          .split("\\+"))
                      .map(SENSORS::get)
                      .toList();
                  return Grid.create(shape.w(), shape.h(), (Integer x, Integer y) -> {
                    if (!shape.get(x, y)) {
                      return null;
                    }
                    int maxX = shape.entries()
                        .stream()
                        .filter(e -> e.key().y() == y && e.value())
                        .mapToInt(e -> e.key().x())
                        .max()
                        .orElse(0);
                    List<Function<Voxel, Sense<? super Voxel>>> localSensors = new ArrayList<>();
                    if (x == maxX) {
                      localSensors.addAll(frontSensors);
                    }
                    if (y == 0) {
                      localSensors.addAll(bottomSensors);
                    }
                    if (y == shape.h() - 1) {
                      localSensors.addAll(topSensors);
                    }
                    return localSensors;
                  });
                })
            ));
    return provider.apply(name).orElseThrow();
  }

}
