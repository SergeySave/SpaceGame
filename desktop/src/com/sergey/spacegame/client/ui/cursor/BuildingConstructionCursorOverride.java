package com.sergey.spacegame.client.ui.cursor;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.sergey.spacegame.client.data.ClientVisualData;
import com.sergey.spacegame.client.gl.DrawingBatch;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.SizeComponent;
import com.sergey.spacegame.common.ecs.component.VisualComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.LevelLimits;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Represents a cursor override for building construction
 *
 * @author sergeys
 */
public final class BuildingConstructionCursorOverride implements CursorOverride {
    
    private static final float MULT  = Color.toFloatBits(0.5f, 0.5f, 0.5f, 0.5f);
    private static final float RED   = Color.toFloatBits(0.5f, 0f, 0f, 0f);
    private static final float GREEN = Color.toFloatBits(0f, 0.5f, 0f, 0f);
    
    //The entity that the cursor should be drawing
    private String entity;
    
    private transient boolean entityDirty;
    private transient Entity  building;
    
    public BuildingConstructionCursorOverride() {}
    
    @Override
    public void load(FileSystem fileSystem) throws IOException {
    }
    
    @Override
    public Optional<Cursor> getRequestedCursor() {
        return Optional.empty();
    }
    
    @Override
    public void drawExtra(Level level, DrawingBatch batch, OrthographicCamera camera, boolean enabled) {
        //Get our mouse coordinates in terms of game coordinates
        Vector3     vec    = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        LevelLimits limits = level.getLimits();
        
        //Stream all of the planets
        StreamSupport.stream(level.getPlanets().spliterator(), false)
                //That have positions
                .filter(PositionComponent.MAPPER::has)
                //Get their positions
                .map((p) -> new Object[]{p, PositionComponent.MAPPER.get(p)})
                //Calculate their distances squared to the mouse position
                .map((p) -> {
                    PositionComponent positionComponent = (PositionComponent) p[1];
                    float             dx                = positionComponent.getX() - vec.x;
                    float             dy                = positionComponent.getY() - vec.y;
                    p[1] = dx * dx + dy * dy;
                    return p;
                })
                //Find the closest planet
                .min((l, r) -> Float.compare((Float) l[1], (Float) r[1]))
                //Get the planet entity
                .map((c) -> (Entity) c[0])
                //And if we found one (which means one existed in the first place)
                .ifPresent((planet) -> {
                    //If we need to update our building entity then do that
                    if (entityDirty || building == null) {
                        building = level.getEntities().get(entity).createEntity(level);
                        entityDirty = false;
                        if (building == null) return;
                    }
                    
                    //If the building has no size then we can't do anything about it
                    if (!SizeComponent.MAPPER.has(building)) return;
                    //If it doesnt have a position we can give it one
                    if (!PositionComponent.MAPPER.has(building)) building.add(new PositionComponent());
                    
                    PositionComponent planetPos = PositionComponent.MAPPER.get(planet);
                    
                    //Get the angle relative to the planet
                    RotationComponent planetR = RotationComponent.MAPPER.get(planet);
                    float angle = planetPos.createVector().sub(vec.x, vec.y).scl(-1).angle()
                                  - (planetR != null ? planetR.r : 0f);
                    
                    //Calculate the building's position
                    BuildingSystem.doSetBuildingPosition(building, planet, angle);
                    
                    PositionComponent posVar;
                    SizeComponent     sizeVar;
                    VisualComponent   visVar;
                    ClientVisualData  visualData;
                    
                    RotationComponent rotVar;
                    
                    //Get the extent of the building's position around the planet
                    float[] minMax = PlanetSystem.getMinMax(building, planet, angle);
                    
                    //Determine if it is a valid placement
                    boolean validPlacement = PlanetComponent.MAPPER.get(planet).isFree(minMax[0], minMax[1], planet) &&
                                             enabled;
                    
                    posVar = PositionComponent.MAPPER.get(building);
                    sizeVar = SizeComponent.MAPPER.get(building);
                    visVar = VisualComponent.MAPPER.get(building);
                    
                    //Getting the visual data for the visual component
                    if (visVar.getVisualData() instanceof ClientVisualData) {
                        visualData = (ClientVisualData) visVar.getVisualData();
                        
                        if (validPlacement && posVar.getX() < limits.getMinX() || posVar.getX() > limits.getMaxX() ||
                            posVar.getY() < limits.getMinY() || posVar.getY() > limits.getMaxY()) {
                            validPlacement = false;
                        }
                        
                        batch.setMultTint(MULT);
                        //If it is valid tint it green and if not then red
                        batch.setAddTint(validPlacement ? GREEN : RED);
                        
                        //Draw the building
                        if (RotationComponent.MAPPER.has(building)) {
                            rotVar = RotationComponent.MAPPER.get(building);
                            float oX = rotVar.originX * sizeVar.w;
                            float oY = rotVar.originY * sizeVar.h;
                            batch.draw(visualData.getRegion(),
                                       posVar.getX() - oX,
                                       posVar.getY() - oY, oX, oY, sizeVar.w, sizeVar.h, 1, 1, rotVar.r);
                        } else {
                            batch.draw(visualData.getRegion(),
                                       posVar.getX() - sizeVar.w / 2,
                                       posVar.getY() - sizeVar.h / 2, sizeVar.w, sizeVar.h);
                        }
                    }
                });
    }
    
    public String getEntity() {
        return entity;
    }
    
    public void setEntity(String entity) {
        this.entity = entity;
        if (!entity.equals(this.entity)) entityDirty = true;
    }
    
    @Override
    public boolean needsInitialization() {
        return true;
    }
    
    @Override
    public void dispose() {
    }
}
