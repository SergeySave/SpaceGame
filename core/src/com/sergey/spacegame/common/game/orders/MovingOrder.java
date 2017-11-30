package com.sergey.spacegame.common.game.orders;

/**
 * Represents an order that involves motion
 * Used for rendering motions
 *
 * @author sergeys
 */
public interface MovingOrder extends IOrder {
    
    /**
     * The x coordinate of the position this motion is going to
     *
     * @return the x coordinate of the position this motion is going to
     */
    float getPositionX();
    
    /**
     * The y coordinate of the position this motion is going to
     *
     * @return the y coordinate of the position this motion is going to
     */
    float getPositionY();
    
    /**
     * Should this order be drawn
     *
     * @return should this order be drawn
     */
    default boolean doDraw() {
        return true;
    }
}