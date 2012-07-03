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
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.terasology.math.TeraMath;

import javax.vecmath.Vector3d;

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

    public void loadProjectionMatrix(float fov) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        float fovy = (float) (2 * Math.atan2(Math.tan(0.5 * fov * TeraMath.DEG_TO_RAD), aspectRatio)) * TeraMath.RAD_TO_DEG;
        gluPerspective(fovy, aspectRatio, 0.1f, 512f);
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadViewMatrix() {
        loadMatrix(calcViewMatrix());
    }

    public void loadNormalizedViewMatrix() {
        loadMatrix(calcNormalizedViewMatrix());
    }

    public void loadMatrix(Matrix4f m) {
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        m.store(buffer);
        buffer.flip();

        glLoadMatrix(buffer);
        _viewFrustum.updateFrustum();
    }

    public Matrix4f calcViewMatrix() {
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);

        Matrix4f vm = TeraMath.createViewMatrix(0f, (float) _bobbingVerticalOffsetFactor * 2.0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y + (float) _bobbingVerticalOffsetFactor * 2.0f, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);

        if (_reflected) {
            vm.translate(new Vector3f(0.0f, 2f * ((float) -_position.y + 32f), 0.0f));
            vm.scale(new Vector3f(1.0f, -1.0f, 1.0f));
        }

        return vm;
    }

    public Matrix4f calcNormalizedViewMatrix() {
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);

        Matrix4f vm = TeraMath.createViewMatrix(0f, 0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);

        if (_reflected) {
            vm.scale(new Vector3f(1.0f, -1.0f, 1.0f));
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
