package com.sergey.spacegame.client.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.client.ecs.component.SelectedComponent;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.ControllableComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.event.SelectionChangeEvent;
import com.sergey.spacegame.common.math.SpatialQuadtree;

import java.util.LinkedList;
import java.util.List;

public class SelectionSystem extends EntitySystem implements InputProcessor {
    
    private static final float SELECTION_COLOR = Color.toFloatBits(0.4f, 0.4f, 1f, 0.5f);
    
    private OrthographicCamera camera;
    private DrawingBatch       batch;
    private Vector2            selectionBegin;
    private Vector2            selectionEnd;
    
    private ImmutableArray<Entity>  selectedEntities;
    private SpatialQuadtree<Entity> quadtree;
    //private ImmutableArray<Entity> controllableEntities;
    
    private CommandUISystem cmdUI;
    
    private SelectionChangeEvent.Builder eventBuilder = new SelectionChangeEvent.Builder();
    
    public SelectionSystem(OrthographicCamera camera, DrawingBatch batch, CommandUISystem commandUI,
                           SpatialQuadtree<Entity> quadtree) {
        super(4);
        this.camera = camera;
        this.batch = batch;
        this.cmdUI = commandUI;
        this.quadtree = quadtree;
    }
    
    @Override
    public void addedToEngine(Engine engine) {
        selectedEntities = engine.getEntitiesFor(Family.all(SelectedComponent.class).get());
        //controllableEntities = engine.getEntitiesFor(Family.all(ControllableComponent.class, PositionComponent.class)
        //                                                     .exclude(InContructionComponent.class)
        //                                                     .get());
        
        SpaceGame.getInstance().getInputMultiplexer().addProcessor(this);
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        selectedEntities = null;
        //controllableEntities = null;
        
        SpaceGame.getInstance().getInputMultiplexer().removeProcessor(this);
    }
    
    @Override
    public void update(float deltaTime) {
        if (selectionBegin != null) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            batch.enableBlending();
            batch.setForceColor(SELECTION_COLOR);
            batch.rect(selectionBegin.x, selectionBegin.y, vec.x, vec.y);
            if (selectionEnd != null) {
                selectedEntities.forEach((e) -> e.remove(SelectedComponent.class));
    
                List<Entity> changedSelected = new LinkedList<>();
    
                quadtree.queryArea(selectionBegin, selectionEnd).forEachRemaining((e) -> {
                    Entity entity = e.getKey();
                    if (ControllableComponent.MAPPER.has(entity)) {
                        changedSelected.add(entity);
                        entity.add(new SelectedComponent());
                    }
                });
    
                if (changedSelected.size() == 0) {
                    Entity nearest = quadtree.getSingleNearest(selectionEnd);
        
                    if (nearest != null) {
                        Rectangle rect = new Rectangle(Math.min(selectionEnd.x, selectionBegin.x), Math.min(selectionEnd.y, selectionBegin.y), Math
                                .abs(selectionEnd.x - selectionBegin.x), Math.abs(selectionEnd.y - selectionBegin.y));
            
                        PositionComponent pos = PositionComponent.MAPPER.get(nearest);
                        if (SizeComponent.MAPPER.has(nearest)) {
                            SizeComponent size = SizeComponent.MAPPER.get(nearest);
                            if (RotationComponent.MAPPER.has(nearest)) {
                                RotationComponent rot = RotationComponent.MAPPER.get(nearest);
                                float             oX  = rot.originX * size.w;
                                float             oY  = rot.originY * size.h;
                                if (rect.overlaps(new Rectangle(pos.getX() - oX, pos.getY() - oY, size.w, size.h))) {
                                    nearest.add(new SelectedComponent());
                                    changedSelected.add(nearest);
                                }
                            } else {
                                if (rect.overlaps(new Rectangle(
                                        pos.getX() - size.w / 2, pos.getY() - size.h, size.w, size.h))) {
                                    nearest.add(new SelectedComponent());
                                    changedSelected.add(nearest);
                                }
                            }
                        } else {
                            if (rect.contains(pos.createVector())) {
                                nearest.add(new SelectedComponent());
                                changedSelected.add(nearest);
                            }
                        }
                    }
                }
                
                SpaceGame.getInstance().getEventBus().post(eventBuilder.get(changedSelected));
                
                cmdUI.setCommand(null);
                selectionBegin = null;
                selectionEnd = null;
            }
        }
    }
    
    @Override
    public boolean keyDown(int keycode) {return false;}
    
    @Override
    public boolean keyUp(int keycode) {return false;}
    
    @Override
    public boolean keyTyped(char character) {return false;}
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.LEFT) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            selectionBegin = new Vector2(vec.x, vec.y);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == Buttons.LEFT) {
            Vector3 vec = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            selectionEnd = new Vector2(vec.x, vec.y);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {return false;}
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {return false;}
    
    @Override
    public boolean scrolled(int amount) {return false;}
}
