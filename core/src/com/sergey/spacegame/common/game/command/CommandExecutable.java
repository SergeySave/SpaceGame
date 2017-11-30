package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.game.Level;

/**
 * All CommandExecutables MUST have a no argument constructor
 *
 * @author sergeys
 */
@FunctionalInterface
public interface CommandExecutable {
    
    /**
     * Issue the command for which this is the executable
     *
     * This method should only be called from a CommandExecutorService because the service may need to do other things
     * that simply calling this method will not achieve
     *
     * @param entitySource - the entities that the command should be run on
     * @param numEntities  - the number of entities
     * @param start        - the first input point (or 0,0)
     * @param end          - the second input point (or 0,0)
     * @param level        - the level that the entities are in
     */
    void issue(Iterable<Entity> entitySource, int numEntities, Vector2 start, Vector2 end, Level level);
}
