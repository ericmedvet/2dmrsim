/*-
 * ========================LICENSE_START=================================
 * mrsim2d-core
 * %%
 * Copyright (C) 2020 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.mrsim2d.core;

import io.github.ericmedvet.mrsim2d.core.actions.*;
import io.github.ericmedvet.mrsim2d.core.bodies.Body;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;

import java.util.function.UnaryOperator;

import io.github.ericmedvet.mrsim2d.core.bodies.Voxel;
import io.github.ericmedvet.mrsim2d.core.bodies.RotationalJoint;

import java.util.EnumMap;
import java.util.Map;

public class XMirrorer<A extends Action<O>, O> implements UnaryOperator<A> {

    @Override
    public A apply(A action) {
        if (action instanceof TranslateBody translateAction) {
            Body body = translateAction.body();
            Point translation = translateAction.translation();
            Point modifiedTranslation = new Point(-translation.x(), translation.y());
            TranslateBody modifiedAction = new TranslateBody(body, modifiedTranslation);
            //noinspection unchecked
            return (A) modifiedAction;
        }
        if (action instanceof SenseDistanceToBody senseAction) {
            double direction = senseAction.direction();
            double distanceRange = senseAction.distanceRange();
            Body body = senseAction.body();
            double modifiedDirection = direction > 0 ? -direction - Math.PI : -direction + Math.PI;
            SenseDistanceToBody modifiedAction = new SenseDistanceToBody(modifiedDirection, distanceRange, body);
            //noinspection unchecked
            return (A) modifiedAction;
        }
        if (action instanceof SenseVelocity senseAction) {
            double direction = senseAction.direction();
            Body body = senseAction.body();
            double modifiedDirection = direction > 0 ? -direction - Math.PI : -direction + Math.PI;
            SenseVelocity modifiedAction = new SenseVelocity(modifiedDirection, body);
            //noinspection unchecked
            return (A) modifiedAction;
        }
        if (action instanceof SenseRotatedVelocity senseAction) {
            double direction = senseAction.direction();
            Body body = senseAction.body();
            double modifiedDirection = direction > 0 ? -direction - Math.PI : -direction + Math.PI;
            SenseRotatedVelocity modifiedAction = new SenseRotatedVelocity(modifiedDirection, body);
            //noinspection unchecked
            return (A) modifiedAction;
        }
        if (action instanceof RotateBody rotateAction) {
            Body body = rotateAction.body();
            Point point = rotateAction.point();
            double angle = rotateAction.angle();
            double modifiedAngle = angle > 0 ? -angle + Math.PI : -angle - Math.PI;
            RotateBody modifiedAction = new RotateBody(body, point, modifiedAngle);
            //noinspection unchecked
            return (A) modifiedAction;
        }

        //TODO mirroring missing actions
//        if (action instanceof SenseNFC) {
//            SenseNFC senseAction = (SenseNFC) action;
//            Point displacement = senseAction.displacement();
//            Point modifiedDisplacement = new Point(-displacement.x(), displacement.y());
//            double direction = senseAction.direction();
//            double modifiedDirection = direction > 0 ? -direction - Math.PI : -direction + Math.PI;
//            SenseNFC modifiedAction = new SenseNFC(senseAction.body(), modifiedDisplacement, modifiedDirection, senseAction.channel());
//            //noinspection unchecked
//            return (A) modifiedAction;
//        }
//        if (action instanceof EmitNFCMessage) {
//            EmitNFCMessage emitAction = (EmitNFCMessage) action;
//            Body body = emitAction.body();
//            Point displacement = emitAction.displacement();
//            double direction = emitAction.direction();
//            short channel = emitAction.channel();
//            double value = emitAction.value();
//            Point modifiedDisplacement = new Point(-displacement.x(), displacement.y());
//            double modifiedDirection = direction > 0 ? -direction + Math.PI : -direction - Math.PI;
//            EmitNFCMessage modifiedAction = new EmitNFCMessage(body, modifiedDisplacement, modifiedDirection, channel, value);
//            //noinspection unchecked
//            return (A) modifiedAction;
//        }
//        if (action instanceof ActuateVoxel) {
//            ActuateVoxel actuateAction = (ActuateVoxel) action;
//            Voxel body = actuateAction.body();
//            EnumMap<Voxel.Side, Double> values = actuateAction.values();
//            double nValue = values.get(Voxel.Side.N);
//            double eValue = values.get(Voxel.Side.E);
//            double sValue = values.get(Voxel.Side.S);
//            double wValue = values.get(Voxel.Side.W);
//            EnumMap<Voxel.Side, Double> modifiedValues = new EnumMap<>(Map.of(
//                    Voxel.Side.N, nValue,
//                    Voxel.Side.E, wValue,
//                    Voxel.Side.S, sValue,
//                    Voxel.Side.W, eValue
//            ));
//            ActuateVoxel modifiedAction = new ActuateVoxel(body, modifiedValues);
//            //noinspection unchecked
//            return (A) modifiedAction;
//        }
//        if (action instanceof ActuateRotationalJoint) {
//            ActuateRotationalJoint actuateAction = (ActuateRotationalJoint) action;
//            RotationalJoint body = actuateAction.body();
//            double value = actuateAction.value();
//            double modifiedValue = value > 0 ? -value + Math.PI : -value - Math.PI;
//            ActuateRotationalJoint modifiedAction = new ActuateRotationalJoint(body, modifiedValue);
//            //noinspection unchecked
//            return (A) modifiedAction;
//        }
        return action;
    }
}
