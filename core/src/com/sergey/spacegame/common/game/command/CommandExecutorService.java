package com.sergey.spacegame.common.game.command;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.event.CommandIssuedEvent;
import com.sergey.spacegame.common.game.Level;

public class CommandExecutorService {
    
    private CommandIssuedEvent.Builder commandIssuedEvent;
    
    public CommandExecutorService() {
        commandIssuedEvent = new CommandIssuedEvent.Builder();
    }
    
    public void executeCommand(Command command, Iterable<Entity> entitySource, int numEntities,
                               Vector2 start, Vector2 end, Level level) {
        if (numEntities == 0) return;
        command.getExecutable().issue(entitySource, numEntities, start, end, level);
        SpaceGame.getInstance()
                .getEventBus()
                .post(commandIssuedEvent.get(entitySource.iterator(), command.getId(), numEntities));
        //If replay is wanted it can be implemented by remembering the times that this was called
    }
}
