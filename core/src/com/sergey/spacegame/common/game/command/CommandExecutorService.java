package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.event.CommandIssuedEvent;
import com.sergey.spacegame.common.game.Level;

/**
 * Represents a serivce for executing commands
 *
 * @author sergeys
 */
public class CommandExecutorService {
    
    private CommandIssuedEvent.Builder commandIssuedEvent;
    
    /**
     * Create a new CommandExecutorService
     */
    public CommandExecutorService() {
        commandIssuedEvent = new CommandIssuedEvent.Builder();
    }
    
    /**
     * Execute a given command
     *
     * @param command      - the command to execute
     * @param entitySource - the entities that the command should be run on
     * @param numEntities  - the number of entities
     * @param start        - the first input point (or 0,0)
     * @param end          - the second input point (or 0,0)
     * @param level        - the level that the entities are in
     */
    public void executeCommand(Command command, Iterable<Entity> entitySource, int numEntities,
                               Vector2 start, Vector2 end, Level level) {
        //If a command is issued but no entities are around to hear it was the command issued
        if (numEntities == 0) return;
        command.getExecutable().issue(entitySource, numEntities, start, end, level);
        SpaceGame.getInstance()
                .getEventBus()
                .post(commandIssuedEvent.get(entitySource.iterator(), command.getId(), numEntities));
        //If replay is wanted it can be implemented by remembering the times that this was called
    }
}
