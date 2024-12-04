/*-
 * ========================LICENSE_START=================================
 * mrsim2d-viewer
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

package io.github.ericmedvet.mrsim2d.viewer.drawers;

import io.github.ericmedvet.mrsim2d.viewer.ComponentDrawer;
import java.awt.*;

public abstract class AbstractComponentDrawer<K> implements ComponentDrawer {
    private final Class<K> bodyClass;

    public AbstractComponentDrawer(Class<K> bodyClass) {
        this.bodyClass = bodyClass;
    }

    protected abstract boolean innerDraw(double t, K k, Graphics2D g);

    @SuppressWarnings("unchecked")
    @Override
    public boolean draw(double t, Object o, Graphics2D g) {
        if (bodyClass.isAssignableFrom(o.getClass())) {
            return innerDraw(t, (K) o, g);
        }
        return false;
    }
}
