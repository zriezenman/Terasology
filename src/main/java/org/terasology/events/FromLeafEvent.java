package org.terasology.events;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.world.block.Block;

import javax.vecmath.Vector3f;
/**
 * Created with IntelliJ IDEA.
 * User: zriezenman
 * Date: 5/18/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class FromLeafEvent extends AbstractEvent {
    private Block Leaf;
    private Vector3f pos;

    public FromLeafEvent(Block leafBlock, Vector3f position){
        Leaf = leafBlock;
        pos= position;
    }

    /**
     * @return the liquid
     */
    public Block getLiquid() {
        return Leaf;
    }

    /**
     * @return the Position
     */
    public Vector3f getPosition() {
        return pos;
    }

}
