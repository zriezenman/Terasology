package org.terasology.events;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.world.block.Block;
/**
 * Created with IntelliJ IDEA.
 * User: zriezenman
 * Date: 5/16/13
 * Time: 7:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class LeafEvent extends AbstractEvent{
    private Block Leaf;
    private Vector3f pos;

    public LeafEvent(Block liquidBlock, Vector3f position){
        Leaf = liquidBlock;
        pos= position;
    }
}
