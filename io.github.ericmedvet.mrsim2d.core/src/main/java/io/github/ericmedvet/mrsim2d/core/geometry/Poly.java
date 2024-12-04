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

package io.github.ericmedvet.mrsim2d.core.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record Poly(Point... vertexes) implements Shape {

    public static Poly rectangle(double w, double h) {
        return new Poly(Point.ORIGIN, new Point(w, 0), new Point(w, h), new Point(0, h));
    }

    public static Poly regular(double radius, int n) {
        return new Poly(IntStream.range(0, n)
                .mapToObj(i -> new Point(
                        radius * Math.cos(Math.PI * 2d * (double) i / (double) n),
                        radius * Math.sin(Math.PI * 2d * (double) i / (double) n)))
                .toArray(Point[]::new));
    }

    public static Poly square(double l) {
        return rectangle(l, l);
    }

    @Override
    public BoundingBox boundingBox() {
        return new BoundingBox(Point.min(vertexes), Point.max(vertexes));
    }

    @Override
    public double area() {
        double a = 0d;
        int l = vertexes.length;
        for (int i = 0; i < l; i++) {
            a = a + vertexes[i].x() * (vertexes[(l + i + 1) % l].y() - vertexes[(l + i - 1) % l].y());
        }
        a = 0.5d * Math.abs(a);
        return a;
    }

    @Override
    public Point center() {
        return Point.average(vertexes);
    }

    public List<Segment> sides() {
        List<Segment> sides = new ArrayList<>(vertexes.length);
        for (int i = 0; i < vertexes.length - 1; i++) {
            sides.add(new Segment(vertexes[i], vertexes[i + 1]));
        }
        sides.add(new Segment(vertexes[vertexes.length - 1], vertexes[0]));
        return Collections.unmodifiableList(sides);
    }

    @Override
    public String toString() {
        return "[" + Arrays.stream(vertexes).map(Point::toString).collect(Collectors.joining("->")) + "]";
    }
}
