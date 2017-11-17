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
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.game.command.ClientCommand;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.LevelLimits;
import com.sergey.spacegame.common.game.command.Command;
import com.sergey.spacegame.common.lua.LuaPredicate;
import com.sergey.spacegame.common.util.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommandUISystem extends EntitySystem {
    
    private static final float LINE_COLOR = Color.WHITE.toFloatBits();
    
    private OrthographicCamera camera;
    private DrawingBatch       batch;
    
    private Vector2 orderCenter;
    
    private ClientCommand command;
    
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
            if (!(level.getCommands().get("default") instanceof ClientCommand)) {
                System.err.println("ERROR: Command not a ClientCommand on Client side");
                return;
            }
            command = (ClientCommand) level.getCommands().get("default");
            if (command == null) {
                return;
            }
        }
        if (!command.getRequiresInput()) {
            List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                    .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                    .collect(Collectors.toList());
            boolean allEnabled = true;
            if (command.getReq() != null) {
                for (LuaPredicate predicate : command.getReq().values()) {
                    if (!predicate.test()) {
                        allEnabled = false;
                        break;
                    }
                }
            }
            if (allEnabled) {
                SpaceGame.getInstance()
                        .getCommandExecutor()
                        .executeCommand(command, entities, entities.size(), Vector2.Zero, Vector2.Zero, level);
            }
            command = null;
            return;
        }
    
        LevelLimits limits = level.getLimits();
    
        if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(Buttons.RIGHT)) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    
            if (vec.x < limits.getMinX() || vec.x > limits.getMaxX() || vec.y < limits.getMinY() ||
                vec.y > limits.getMaxY()) {
                return;
            }
            
            if (!command.getRequiresTwoInput()) {
                List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                        .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                        .collect(Collectors.toList());
                boolean allEnabled = true;
                if (command.getReq() != null) {
                    for (LuaPredicate predicate : command.getReq().values()) {
                        if (!predicate.test()) {
                            allEnabled = false;
                            break;
                        }
                    }
                }
                if (allEnabled) {
                    SpaceGame.getInstance()
                            .getCommandExecutor()
                            .executeCommand(command, entities, entities.size(), new Vector2(vec.x, vec.y), Vector2.Zero, level);
                }
                return;
            }
            orderCenter = new Vector2(vec.x, vec.y);
        }
        
        if (orderCenter != null) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    
            vec.x = Utils.clamp(vec.x, limits.getMinX(), limits.getMaxX());
            vec.y = Utils.clamp(vec.y, limits.getMinY(), limits.getMaxY());
            
            batch.setForceColor(LINE_COLOR);
            batch.line(orderCenter.x, orderCenter.y, vec.x, vec.y);
            if (!Gdx.input.isButtonPressed(Buttons.RIGHT)) {
                List<Entity> entities = StreamSupport.stream(selectedEntities.spliterator(), true)
                        .filter((e) -> ControllableComponent.MAPPER.get(e).commands.contains(command))
                        .collect(Collectors.toList());
                boolean allEnabled = true;
                if (command.getReq() != null) {
                    for (LuaPredicate predicate : command.getReq().values()) {
                        if (!predicate.test()) {
                            allEnabled = false;
                            break;
                        }
                    }
                }
                if (allEnabled) {
                    SpaceGame.getInstance()
                            .getCommandExecutor()
                            .executeCommand(command, entities, entities.size(), orderCenter, new Vector2(vec.x, vec.y), level);
                }
                orderCenter = null;
                return;
            }
        }
        
        if (command != null && command.getCursor() != null) {
            if (command.getCursor().needsInitialization()) command.getCursor().init();
            boolean allEnabled = true;
            if (command.getReq() != null) {
                for (LuaPredicate predicate : command.getReq().values()) {
                    if (!predicate.test()) {
                        allEnabled = false;
                        break;
                    }
                }
            }
            command.getCursor().drawExtra(level, batch, camera, allEnabled);
        }
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(ClientCommand command) {
        this.command = command;
    }
}
