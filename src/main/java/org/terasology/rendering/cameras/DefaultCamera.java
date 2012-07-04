/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
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
 */
package org.terasology.rendering.cameras;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.math.TeraMath;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * Simple default camera.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultCamera extends Camera {

    private double _bobbingRotationOffsetFactor, _bobbingVerticalOffsetFactor = 0.0;

    @Override
    protected Matrix4f calcProjectionMatrix(float fov) {
        return TeraMath.createProjectionMatrix(fov, 0.1f, 512f);
    }

    protected Matrix4f calcViewMatrix(boolean reflected) {
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);

        Matrix4f vm = TeraMath.createViewMatrix(0f, (float) _bobbingVerticalOffsetFactor * 2.0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y + (float) _bobbingVerticalOffsetFactor * 2.0f, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);

        if (reflected) {
            Matrix4f reflectionMatrix = TeraMath.calcReflectionMatrix(32f, (float) _position.y);
            vm.mul(reflectionMatrix);
        }

        return vm;
    }

    protected Matrix4f calcNormalizedViewMatrix(boolean reflected) {
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);

        Matrix4f vm = TeraMath.createViewMatrix(0f, 0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);

        if (reflected) {
            Matrix4f reflectionMatrix = TeraMath.calcReflectionMatrix(0.0f, 0.0f);
            vm.mul(reflectionMatrix);
        }

        return vm;
    }

    public void setBobbingRotationOffsetFactor(double f) {
        _bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(double f) {
        _bobbingVerticalOffsetFactor = f;
    }
}
