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
package org.terasology.model.structures;

import org.lwjgl.BufferUtils;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;

/**
 * View frustum usable for frustum culling.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ViewFrustum {

    private final FrustumPlane[] _planes = new FrustumPlane[6];

    private final FloatBuffer _projectionMatrix = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer _viewMatrix = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer _clip = BufferUtils.createFloatBuffer(16);

    /**
     * Init. a new view frustum.
     */
    public ViewFrustum() {
        for (int i = 0; i < 6; i++)
            _planes[i] = new FrustumPlane();
    }

    /**
     * Updates the view frustum using the currently active modelview and projection matrices.
     */
    public void updateFrustum(Matrix4f vm, Matrix4f pm) {
        TeraMath.matrixToFloatBuffer(vm, _viewMatrix);
        TeraMath.matrixToFloatBuffer(pm, _projectionMatrix);

        _clip.put(0, _viewMatrix.get(0) * _projectionMatrix.get(0) + _viewMatrix.get(1) * _projectionMatrix.get(4) + _viewMatrix.get(2) * _projectionMatrix.get(8) + _viewMatrix.get(3) * _projectionMatrix.get(12));
        _clip.put(1, _viewMatrix.get(0) * _projectionMatrix.get(1) + _viewMatrix.get(1) * _projectionMatrix.get(5) + _viewMatrix.get(2) * _projectionMatrix.get(9) + _viewMatrix.get(3) * _projectionMatrix.get(13));
        _clip.put(2, _viewMatrix.get(0) * _projectionMatrix.get(2) + _viewMatrix.get(1) * _projectionMatrix.get(6) + _viewMatrix.get(2) * _projectionMatrix.get(10) + _viewMatrix.get(3) * _projectionMatrix.get(14));
        _clip.put(3, _viewMatrix.get(0) * _projectionMatrix.get(3) + _viewMatrix.get(1) * _projectionMatrix.get(7) + _viewMatrix.get(2) * _projectionMatrix.get(11) + _viewMatrix.get(3) * _projectionMatrix.get(15));

        _clip.put(4, _viewMatrix.get(4) * _projectionMatrix.get(0) + _viewMatrix.get(5) * _projectionMatrix.get(4) + _viewMatrix.get(6) * _projectionMatrix.get(8) + _viewMatrix.get(7) * _projectionMatrix.get(12));
        _clip.put(5, _viewMatrix.get(4) * _projectionMatrix.get(1) + _viewMatrix.get(5) * _projectionMatrix.get(5) + _viewMatrix.get(6) * _projectionMatrix.get(9) + _viewMatrix.get(7) * _projectionMatrix.get(13));
        _clip.put(6, _viewMatrix.get(4) * _projectionMatrix.get(2) + _viewMatrix.get(5) * _projectionMatrix.get(6) + _viewMatrix.get(6) * _projectionMatrix.get(10) + _viewMatrix.get(7) * _projectionMatrix.get(14));
        _clip.put(7, _viewMatrix.get(4) * _projectionMatrix.get(3) + _viewMatrix.get(5) * _projectionMatrix.get(7) + _viewMatrix.get(6) * _projectionMatrix.get(11) + _viewMatrix.get(7) * _projectionMatrix.get(15));

        _clip.put(8, _viewMatrix.get(8) * _projectionMatrix.get(0) + _viewMatrix.get(9) * _projectionMatrix.get(4) + _viewMatrix.get(10) * _projectionMatrix.get(8) + _viewMatrix.get(11) * _projectionMatrix.get(12));
        _clip.put(9, _viewMatrix.get(8) * _projectionMatrix.get(1) + _viewMatrix.get(9) * _projectionMatrix.get(5) + _viewMatrix.get(10) * _projectionMatrix.get(9) + _viewMatrix.get(11) * _projectionMatrix.get(13));
        _clip.put(10, _viewMatrix.get(8) * _projectionMatrix.get(2) + _viewMatrix.get(9) * _projectionMatrix.get(6) + _viewMatrix.get(10) * _projectionMatrix.get(10) + _viewMatrix.get(11) * _projectionMatrix.get(14));
        _clip.put(11, _viewMatrix.get(8) * _projectionMatrix.get(3) + _viewMatrix.get(9) * _projectionMatrix.get(7) + _viewMatrix.get(10) * _projectionMatrix.get(11) + _viewMatrix.get(11) * _projectionMatrix.get(15));

        _clip.put(12, _viewMatrix.get(12) * _projectionMatrix.get(0) + _viewMatrix.get(13) * _projectionMatrix.get(4) + _viewMatrix.get(14) * _projectionMatrix.get(8) + _viewMatrix.get(15) * _projectionMatrix.get(12));
        _clip.put(13, _viewMatrix.get(12) * _projectionMatrix.get(1) + _viewMatrix.get(13) * _projectionMatrix.get(5) + _viewMatrix.get(14) * _projectionMatrix.get(9) + _viewMatrix.get(15) * _projectionMatrix.get(13));
        _clip.put(14, _viewMatrix.get(12) * _projectionMatrix.get(2) + _viewMatrix.get(13) * _projectionMatrix.get(6) + _viewMatrix.get(14) * _projectionMatrix.get(10) + _viewMatrix.get(15) * _projectionMatrix.get(14));
        _clip.put(15, _viewMatrix.get(12) * _projectionMatrix.get(3) + _viewMatrix.get(13) * _projectionMatrix.get(7) + _viewMatrix.get(14) * _projectionMatrix.get(11) + _viewMatrix.get(15) * _projectionMatrix.get(15));

        // RIGHT
        _planes[0].setA(_clip.get(3) - _clip.get(0));
        _planes[0].setB(_clip.get(7) - _clip.get(4));
        _planes[0].setC(_clip.get(11) - _clip.get(8));
        _planes[0].setD(_clip.get(15) - _clip.get(12));
        _planes[0].normalize();

        // LEFT
        _planes[1].setA(_clip.get(3) + _clip.get(0));
        _planes[1].setB(_clip.get(7) + _clip.get(4));
        _planes[1].setC(_clip.get(11) + _clip.get(8));
        _planes[1].setD(_clip.get(15) + _clip.get(12));
        _planes[1].normalize();

        // BOTTOM
        _planes[2].setA(_clip.get(3) + _clip.get(1));
        _planes[2].setB(_clip.get(7) + _clip.get(5));
        _planes[2].setC(_clip.get(11) + _clip.get(9));
        _planes[2].setD(_clip.get(15) + _clip.get(13));
        _planes[2].normalize();

        // TOP
        _planes[3].setA(_clip.get(3) - _clip.get(1));
        _planes[3].setB(_clip.get(7) - _clip.get(5));
        _planes[3].setC(_clip.get(11) - _clip.get(9));
        _planes[3].setD(_clip.get(15) - _clip.get(13));
        _planes[3].normalize();

        // FAR
        _planes[4].setA(_clip.get(3) - _clip.get(2));
        _planes[4].setB(_clip.get(7) - _clip.get(6));
        _planes[4].setC(_clip.get(11) - _clip.get(10));
        _planes[4].setD(_clip.get(15) - _clip.get(14));
        _planes[4].normalize();

        // NEAR
        _planes[5].setA(_clip.get(3) + _clip.get(2));
        _planes[5].setB(_clip.get(7) + _clip.get(6));
        _planes[5].setC(_clip.get(11) + _clip.get(10));
        _planes[5].setD(_clip.get(15) + _clip.get(14));
        _planes[5].normalize();
    }

    /**
     * Returns true if the given point intersects the view frustum.
     */
    public boolean intersects(double x, double y, double z) {
        for (int i = 0; i < 6; i++) {
            if (_planes[i].getA() * x + _planes[i].getB() * y + _planes[i].getC() * z + _planes[i].getD() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this view frustum intersects the given AABB.
     */
    public boolean intersects(AABB aabb) {

        Vector3d[] aabbVertices = aabb.getVertices();

        Vector3d cp = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        for (int i = 0; i < 6; i++) {
            if (_planes[i].getA() * (aabbVertices[0].x - cp.x) + _planes[i].getB() * (aabbVertices[0].y - cp.y) + _planes[i].getC() * (aabbVertices[0].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[1].x - cp.x) + _planes[i].getB() * (aabbVertices[1].y - cp.y) + _planes[i].getC() * (aabbVertices[1].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[2].x - cp.x) + _planes[i].getB() * (aabbVertices[2].y - cp.y) + _planes[i].getC() * (aabbVertices[2].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[3].x - cp.x) + _planes[i].getB() * (aabbVertices[3].y - cp.y) + _planes[i].getC() * (aabbVertices[3].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[4].x - cp.x) + _planes[i].getB() * (aabbVertices[4].y - cp.y) + _planes[i].getC() * (aabbVertices[4].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[5].x - cp.x) + _planes[i].getB() * (aabbVertices[5].y - cp.y) + _planes[i].getC() * (aabbVertices[5].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[6].x - cp.x) + _planes[i].getB() * (aabbVertices[6].y - cp.y) + _planes[i].getC() * (aabbVertices[6].z - cp.z) + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * (aabbVertices[7].x - cp.x) + _planes[i].getB() * (aabbVertices[7].y - cp.y) + _planes[i].getC() * (aabbVertices[7].z - cp.z) + _planes[i].getD() > 0)
                continue;
            return false;
        }

        return true;
    }
}
