/*
 * Copyright 2022 eric
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

package it.units.erallab.mrsim2d.core.agents.legged;

import it.units.erallab.mrsim2d.core.Sensor;
import it.units.erallab.mrsim2d.core.bodies.RotationalJoint;

import java.util.List;

public record LegChunk(
    double length,
    double width,
    double mass,
    RotationalJoint.Motor motor,
    ConnectorType upConnector,
    List<Sensor<?>> jointSensors
) {}
