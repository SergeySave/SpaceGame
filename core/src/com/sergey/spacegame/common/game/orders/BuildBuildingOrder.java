package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.SpaceGame;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.event.BuildingConstructedEvent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.math.Angle;
import com.sergey.spacegame.common.math.AngleRange;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An order used to build a new building. Uses a finite state machine to control the process of the building.
 *
 * @author sergeys
 */
public class BuildBuildingOrder implements IOrder {
    
    private static final float                            EPSILON                  = 1e-2f;
    private static final Vector2                          TMP                      = new Vector2();
    private static       BuildingConstructedEvent.Builder buildingConstructedEvent = new BuildingConstructedEvent.Builder();
    
    private boolean           isDone;
    private Entity            building;
    private PositionComponent planetPos;
    private Vector2           desired;
    //Only transfer from constructor to init
    private float             time;
    private double            price;
    private float             x;
    private float             y;
    private String            entity;
    
    public BuildBuildingOrder(String entityName, float time, float x, float y, double price) {
        this.entity = entityName;
        this.time = time;
        this.isDone = false;
        this.x = x;
        this.y = y;
        this.price = price;
    }
    
    @Override
    public void init(Entity e, Level level, OrderSystem orderSystem) {
        Optional<Entity> closestPlanet = StreamSupport.stream(level.getPlanets().spliterator(), false)
                .filter(PositionComponent.MAPPER::has)
                .map((p) -> new Object[]{p, PositionComponent.MAPPER.get(p)})
                .map((p) -> new Object[]{
                        p[0], (((PositionComponent) p[1]).x - x) * (((PositionComponent) p[1]).x - x) +
                              (((PositionComponent) p[1]).y - y) * (((PositionComponent) p[1]).y - y)
                })
                .min((l, r) -> Float.compare((Float) l[1], (Float) r[1]))
                .map((c) -> (Entity) c[0]);
        
        if (closestPlanet.isPresent()) {
            Entity planet = closestPlanet.get();
            
            Entity                 building               = level.getEntities().get(entity).createEntity(level); //Copy of building
            InContructionComponent inContructionComponent = new InContructionComponent(entity);
            inContructionComponent.timeRemaining = time;
            building.add(inContructionComponent);
    
            planetPos = PositionComponent.MAPPER.get(planet);
            
            float buildingPos = planetPos.createVector().sub(x, y).scl(-1).angle();
            
            float[] minMax = PlanetSystem.getMinMax(building, planet, buildingPos);
            
            if (!PlanetComponent.MAPPER.get(planet).isFree(minMax[0], minMax[1])) {
    
                //Registered construting buildings will get checked as well as existing buildings
                Optional<Entity> sameSpotBuilding = Stream.concat(orderSystem.getConstructingBuildings()
                                                                          .stream(), StreamSupport.stream(level.getBuildingsInConstruction()
                                                                                                                  .spliterator(), false))
                        .filter(b -> BuildingComponent.MAPPER.get(b).getPlanet() == planet)
                        .filter(b -> {
                            float[] mm = PlanetSystem.getMinMax(b, planet, BuildingComponent.MAPPER.get(b)
                                    .getPosition());
                            return new AngleRange(mm[0], mm[1]).isInRange(buildingPos);
                        })
                        .findAny();
    
                if (sameSpotBuilding.isPresent()) {
                    this.building = sameSpotBuilding.get();
                    return;
                    //time = InContructionComponent.MAPPER.get(building).timeRemaining;
                } else {
                    //If placement is invalid
                    isDone = true;
                    //Fail to build
                    return;
                }
            }
            
            BuildingComponent buildingC = new BuildingComponent();
    
            buildingC.init(planet, buildingPos, building);
            
            building.add(buildingC);
            
            level.getECS().addEntity(building);
            orderSystem.registerNewInConstruction(building);
    
            level.setMoney(level.getMoney() - price);
            
            this.building = building;
        } else {
            //No planet fail to build
            isDone = true;
        }
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (isDone) return;
        if (!InContructionComponent.MAPPER.has(building)) {
            isDone = true;
            return;
        }
        
        if (ShipComponent.MAPPER.has(e) && PositionComponent.MAPPER.has(e)) {
            PositionComponent pos = PositionComponent.MAPPER.get(e);
            VelocityComponent vel = VelocityComponent.MAPPER.get(e);
            if (vel == null) {
                vel = new VelocityComponent();
                e.add(vel);
            }
            ShipComponent ship  = ShipComponent.MAPPER.get(e);
            float         speed = ship.moveSpeed;
            desired = PositionComponent.MAPPER.get(building)
                    .createVector()
                    .sub(planetPos.x, planetPos.y)
                    .scl(1.5f)
                    .add(planetPos.x, planetPos.y)
                    .add(20f * e.hashCode() / 2147483647f, 20f * ship.hashCode() / 2147483647f);
    
            double dx              = desired.x - pos.x;
            double dy              = desired.y - pos.y;
            double dist            = Math.hypot(dx, dy);
            double timePerUnitDist = dist / speed;
            if (timePerUnitDist < deltaTime) {
                pos.x = desired.x;
                pos.y = desired.y;
                e.remove(VelocityComponent.class);
                InContructionComponent.MAPPER.get(building).timeRemaining -= deltaTime;
            } else {
                if (RotationComponent.MAPPER.has(e)) {
                    RotationComponent rotationComponent = RotationComponent.MAPPER.get(e);
                    float             desiredAngle      = TMP.set((float) dx, (float) dy).angle();
                    if (Math.abs(rotationComponent.r - desiredAngle) > EPSILON) {
                        float timeToRotate = (float) (
                                Angle.getThroughRotateDistance(rotationComponent.r, desiredAngle) / ship.rotateSpeed);
                        if (timeToRotate > deltaTime) {
                            RotationVelocityComponent rotationVelocityComponent;
                            if (RotationVelocityComponent.MAPPER.has(e)) {
                                rotationVelocityComponent = RotationVelocityComponent.MAPPER.get(e);
                            } else {
                                rotationVelocityComponent = new RotationVelocityComponent();
                                e.add(rotationVelocityComponent);
                            }
                    
                            float dr = desiredAngle - rotationComponent.r;
                            //Fix the angle
                            while (dr < -180) {
                                dr += 360;
                            }
                            //Fix the angle
                            while (dr > 180) {
                                dr -= 360;
                            }
                    
                            rotationVelocityComponent.vr = Math.signum(dr) * ship.rotateSpeed;
                        } else {
                            if (RotationVelocityComponent.MAPPER.has(e)) e.remove(RotationVelocityComponent.class);
                            rotationComponent.r = desiredAngle;
                            vel.vx = (float) (dx / timePerUnitDist);
                            vel.vy = (float) (dy / timePerUnitDist);
                        }
                    }
                } else {
                    vel.vx = (float) (dx / timePerUnitDist);
                    vel.vy = (float) (dy / timePerUnitDist);
                }
    
                
            }
        } else {
            InContructionComponent.MAPPER.get(building).timeRemaining -= deltaTime;
        }
    
        if (InContructionComponent.MAPPER.get(building).timeRemaining < 0) {
            SpaceGame.getInstance()
                    .getEventBus()
                    .post(buildingConstructedEvent.get(building, InContructionComponent.MAPPER.get(building).entityID));
            building.remove(InContructionComponent.class);
            isDone = true;
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return true;
    }
    
    @Override
    public boolean completed() {
        return isDone;
    }
    
    @Override
    public void onCancel(Entity e, Level level) {
        if (building != null) {
            level.getECS().removeEntity(building);
        }
    }
    
    public Optional<Vector2> getPosition() {
        if (building == null || desired == null) return Optional.empty();
        return Optional.of(desired);
    }
}
