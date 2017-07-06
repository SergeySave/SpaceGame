package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.command.Command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommandUISystem extends EntitySystem {
    
    private static final float LINE_COLOR = Color.WHITE.toFloatBits();
    
    private OrthographicCamera camera;
    private DrawingBatch       batch;
    
    private Vector2 orderCenter;
    
    private Command command;
    
    private ImmutableArray<Entity> selectedEntities;
    
    private Level level;
    
    public CommandUISystem(OrthographicCamera camera, DrawingBatch batch, Level level) {
        super(5);
        this.camera = camera;
        this.level = level;
        this.batch = batch;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class, ControllableComponent.class)
                                                         .get());
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        selectedEntities = null;
    }
    
    @Override
    public void update(float deltaTime) {
        if (command == null) {
            command = level.getCommands().get("default");
            if (command == null) {
                return;
            }
        }
        if (!command.isRequiresInput()) {
            List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                    .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                    .collect(Collectors.toList());
            SpaceGame.getInstance()
                    .getCommandExecutor()
                    .executeCommand(command.getExecutable(), entities, entities.size(), Vector2.Zero, Vector2.Zero, level);
            return;
        }
        
        if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (!command.isRequiresTwoInput()) {
                List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                        .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                        .collect(Collectors.toList());
                SpaceGame.getInstance()
                        .getCommandExecutor()
                        .executeCommand(command.getExecutable(), entities, entities.size(), new Vector2(vec.x, vec.y), Vector2.Zero, level);
                return;
            }
            orderCenter = new Vector2(vec.x, vec.y);
        }
        
        if (orderCenter != null) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            batch.setForceColor(LINE_COLOR);
            batch.line(orderCenter.x, orderCenter.y, vec.x, vec.y);
            if (!Gdx.input.isButtonPressed(Buttons.RIGHT)) {
                List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                        .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                        .collect(Collectors.toList());
                SpaceGame.getInstance()
                        .getCommandExecutor()
                        .executeCommand(command.getExecutable(), entities, entities.size(), orderCenter, new Vector2(vec.x, vec.y), level);
                orderCenter = null;
                return;
            }
        }
        
        if (command != null && command.getCursor() != null) {
            if (command.getCursor().needsInitialization()) command.getCursor().init();
            command.getCursor().drawExtra(level, batch);
        }
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
}
