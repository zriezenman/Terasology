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
import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.Config;
import org.terasology.math.TeraMath;
import org.terasology.model.structures.ViewFrustum;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Provides global access to fonts.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Camera {

    /* CAMERA PARAMETERS */
    protected final Vector3d _position = new Vector3d(0, 0, 0);
    protected final Vector3d _up = new Vector3d(0, 1, 0);
    protected final Vector3d _viewingDirection = new Vector3d(1, 0, 0);

    protected float _targetFov = Config.getInstance().getFov();
    protected float _activeFov = Config.getInstance().getFov() / 4f;

    protected Matrix4f _viewMatrix = new Matrix4f();
    protected Matrix4f _projectionMatrix = new Matrix4f();
    protected Matrix4f _viewProjectionMatrix = new Matrix4f();
    protected Matrix4f _prevViewProjectionMatrix = new Matrix4f();

    protected boolean _dirty = true;

    protected boolean _reflected = false, _normalized = false, _local = false;

    /* VIEW FRUSTUM */
    protected final ViewFrustum _viewFrustum = new ViewFrustum();

    public Matrix4f getViewMatrix() {
        if (_dirty) updateMatrices();
        return _viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        if (_dirty) updateMatrices();
        return _projectionMatrix;
    }

    public Matrix4f getViewProjectionMatrix() {
        if (_dirty) updateMatrices();
        return _viewProjectionMatrix;
    }

    public Matrix4f getPrevViewProjectionMatrix() {
        if (_dirty) updateMatrices();
        return _prevViewProjectionMatrix;
    }

    protected abstract Matrix4f calcViewMatrix();

    protected abstract Matrix4f calcProjectionMatrix(float fov);

    public void updateProjectionMatrix(float fov) {
        _projectionMatrix = calcProjectionMatrix(fov);
    }

//    public void loadModelViewMatrix(Matrix4f m) {
//        glLoadMatrix(TeraMath.matrixToFloatBuffer(m));
//        _viewFrustum.updateFrustum(m, _projectionMatrix);
//    }
//
//    public void loadProjectionMatrix(Matrix4f m) {
//        glMatrixMode(GL11.GL_PROJECTION);
//        glLoadMatrix(TeraMath.matrixToFloatBuffer(m));
//        glMatrixMode(GL11.GL_MODELVIEW);
//    }

    public Vector3d getPosition() {
        return _position;
    }

    public void setPosition(double x, double y, double z) {
        _position.set(x,y,z);
        _dirty = true;
    }

    public Vector3d getViewingDirection() {
        return _viewingDirection;
    }

    public void setViewDirection(double x, double y, double z) {
        _viewingDirection.set(x,y,z);
        _dirty = true;
    }

    public ViewFrustum getViewFrustum() {
        return _viewFrustum;
    }

    public void update(float delta) {
        double diff = Math.abs(_activeFov - _targetFov);
        if (diff < 1.0) {
            _activeFov = _targetFov;
        } else if (_activeFov < _targetFov) {
            _activeFov += 50.0 * delta;
            if (_activeFov >= _targetFov) {
                _activeFov = _targetFov;
            }
        } else if (_activeFov > _targetFov) {
            _activeFov -= 50.0 * delta;
            if (_activeFov <= _targetFov) {
                _activeFov = _targetFov;
            }
        }

        _prevViewProjectionMatrix = _viewProjectionMatrix;
    }

    protected void updateMatrices() {
        _viewMatrix = calcViewMatrix();
        updateProjectionMatrix(_activeFov);
        _viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(_viewMatrix, _projectionMatrix);
        _viewFrustum.updateFrustum(_viewMatrix, _projectionMatrix);
    }

    public void extendFov(float fov) {
        _targetFov = Config.getInstance().getFov() + fov;
        _dirty = true;
    }

    public void resetFov() {
        _targetFov = Config.getInstance().getFov();
        _dirty = true;
    }

    public void setReflected(boolean r) {
        _reflected = r;
        _dirty = true;
    }

    public void setNormalized(boolean r) {
        _normalized = r;
        _dirty = true;
    }

    public void setLocal(boolean r) {
        _local = r;
        _dirty = true;
    }
}
