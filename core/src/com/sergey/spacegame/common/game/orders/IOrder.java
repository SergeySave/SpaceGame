package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.game.Level;

/**
 * Represents an entity's order
 *
 * @author sergeys
 */
public interface IOrder {
    
    /**
     * Perform an entity update for this order
     *
     * @param e         - the entity to update
     * @param deltaTime - the amount of time in seconds since the last update
     * @param level     - the level that the entity is in
     */
    void update(Entity e, float deltaTime, Level level);
    
    /**
     * Is this order valid for the given entity
     *
     * @param e - the entity to check
     *
     * @return whether this order is valid for the entity
     */
    boolean isValidFor(Entity e);
    
    /**
     * Is this order completed for the given entity
     *
     * @param e - the entity to check
     *
     * @return whether this order is already completed
     */
    boolean completed(Entity e);
    
    /**
     * Initialize this order
     *
     * @param e - the entity to initialize this order for
     * @param level - the level the entity is in
     * @param orderSystem - the OrderSystem in charge of this order
     */
    default void init(Entity e, Level level, OrderSystem orderSystem) {}
    
    /**
     * Called when this order is cancelled
     *
     * @param e     - the entity that this order is being cancelled from
     * @param level - the level that the entity is in
     */
    default void onCancel(Entity e, Level level) {}
    
    /**
     * Get the percent completed of the order or -1 if it doesn't have a completion percent
     *
     * @return the percent completed
     */
    default float getEstimatedPercentComplete()                       { return -1; }
    
    /**
     * Get the order tag or null
     *
     * @return the order tag
     */
    default String getTag()                                           { return null; }
}
