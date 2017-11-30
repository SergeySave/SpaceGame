package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.HealthComponent;
import com.sergey.spacegame.common.ecs.component.InContructionComponent;
import com.sergey.spacegame.common.ecs.component.PlanetComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.Team1Component;
import com.sergey.spacegame.common.ecs.component.Team2Component;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.ecs.system.BuildingSystem;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.ecs.system.PlanetSystem;
import com.sergey.spacegame.common.event.BuildingConstructedEvent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.LevelLimits;
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
public class BuildBuildingOrder implements IOrder, MovingOrder {
    
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
        //Find the closest planet to the given coordinates
        Optional<Entity> closestPlanet = StreamSupport.stream(level.getPlanets().spliterator(), false)
                .filter(PositionComponent.MAPPER::has)
                .map((p) -> new Object[]{p, PositionComponent.MAPPER.get(p)})
                .map((p) -> new Object[]{
                        p[0], (((PositionComponent) p[1]).getX() - x) * (((PositionComponent) p[1]).getX() - x) +
                              (((PositionComponent) p[1]).getY() - y) * (((PositionComponent) p[1]).getY() - y)
                })
                .min((l, r) -> Float.compare((Float) l[1], (Float) r[1]))
                .map((c) -> (Entity) c[0]);
    
        //If the planet exists
        if (closestPlanet.isPresent()) {
            Entity planet = closestPlanet.get();
    
            //Create the building entity
            Entity                 building               = level.getEntities().get(entity).createEntity(level); //Copy of building
            InContructionComponent inContructionComponent = new InContructionComponent(entity, price);
            HealthComponent        healthComponent        = HealthComponent.MAPPER.get(building);
            inContructionComponent.finalHealth = healthComponent != null ? healthComponent.getHealth() : -1;
            inContructionComponent.originalTimeRemaining = time;
            inContructionComponent.timeRemaining = time;
            building.add(inContructionComponent);
    
            //If it has health it should start at 0 and increase as it is built
            if (healthComponent != null) {
                healthComponent.setHealth(0);
            }
    
            planetPos = PositionComponent.MAPPER.get(planet);
            RotationComponent planetR   = RotationComponent.MAPPER.get(planet);
            float             planetRot = planetR != null ? planetR.r : 0f;
    
            float buildingPos = planetPos.createVector().sub(x, y).scl(-1).angle() - planetRot;
            
            float[] minMax = PlanetSystem.getMinMax(building, planet, buildingPos);
    
            //Check if the spot on the planet is not free
            if (!PlanetComponent.MAPPER.get(planet).isFree(minMax[0], minMax[1], planet)) {
    
                //Registered construting buildings will get checked as well as existing buildings
                //We want a building in the same range that is under construction
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
    
                //If we found said building
                if (sameSpotBuilding.isPresent()) {
                    //Set that to our building
                    this.building = sameSpotBuilding.get();
    
                    ++InContructionComponent.MAPPER.get(this.building).building;
                } else {
                    //If we didnt find a building
                    isDone = true;
                    //Fail to build
                }
            } else { //The spot on the planet is free so we should build a new building
                BuildingComponent buildingC = new BuildingComponent();
        
                buildingC.init(planet, buildingPos + planetRot);
    
                building.add(buildingC);
                if (!PositionComponent.MAPPER.has(building)) building.add(new PositionComponent());
                BuildingSystem.doSetBuildingPosition(building, planet, buildingPos);
    
                PositionComponent posVar = PositionComponent.MAPPER.get(building);
                LevelLimits       limits = level.getLimits();
    
                if (posVar.getX() < limits.getMinX() || posVar.getX() > limits.getMaxX() ||
                    posVar.getY() < limits.getMinY() || posVar.getY() > limits.getMaxY()) {
                    //Invalid placement
                    isDone = true;
                    return;
                }
    
                level.getECS().addEntity(building);
                orderSystem.registerNewInConstruction(building);
    
                if (Team1Component.MAPPER.has(building)) {
                    level.getPlayer1().setMoney(level.getPlayer1().getMoney() - price);
                } else if (Team2Component.MAPPER.has(building)) {
                    level.getPlayer2().setMoney(level.getPlayer2().getMoney() - price);
                }
    
                this.building = building;
                ++inContructionComponent.building;
            }
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
    
        InContructionComponent inContructionComponent = InContructionComponent.MAPPER.get(building);
        HealthComponent        healthComponent        = HealthComponent.MAPPER.get(building);
    
        //If the constructing entity is a ship and has a position
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
                    .sub(planetPos.getX(), planetPos.getY())
                    .scl(1.5f)
                    .add(planetPos.getX(), planetPos.getY())
                    .add(20f * e.hashCode() / 2147483647f, 20f * ship.hashCode() / 2147483647f);
    
            double dx              = desired.x - pos.getX();
            double dy              = desired.y - pos.getY();
            double dist            = Math.hypot(dx, dy);
            double timePerUnitDist = dist / speed;
            //Check if it can move into position in the amount of time that has elapsed
            if (timePerUnitDist < deltaTime) {
                //Set it's position and clear its velocity
                pos.setX(desired.x);
                pos.setY(desired.y);
                e.remove(VelocityComponent.class);
                //Decrement the amount of time remaining
                inContructionComponent.timeRemaining -= deltaTime;
                //Update the health if needed
                if (healthComponent != null) {
                    healthComponent.setHealth(
                            inContructionComponent.finalHealth * (1 - inContructionComponent.timeRemaining /
                                                                      inContructionComponent.originalTimeRemaining));
                }
            } else {
                //If we cant move into position in the given time
                //Basically do a move order
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
                            vel.vx = 0;
                            vel.vy = 0;
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
            //If we are in position
            //Build the building
            inContructionComponent.timeRemaining -= deltaTime;
            if (healthComponent != null) {
                healthComponent.setHealth(
                        inContructionComponent.finalHealth * (1 - inContructionComponent.timeRemaining /
                                                                  inContructionComponent.originalTimeRemaining));
            }
        }
    
        //If we have finished building the building
        //This will only run for the first entity that finished constructing
        if (inContructionComponent.timeRemaining < 0) {
            SpaceGame.getInstance()
                    .getEventBus()
                    .post(buildingConstructedEvent.get(building, inContructionComponent.entityID));
    
            //And remove it from under construction
            building.remove(InContructionComponent.class);
    
            //Also update its health
            if (healthComponent != null) {
                healthComponent.setHealth(inContructionComponent.finalHealth);
            }

            isDone = true;
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return true;
    }
    
    @Override
    public boolean completed(Entity e) {
        return isDone;
    }
    
    @Override
    public void onCancel(Entity e, Level level) {
        if (building != null) {
            InContructionComponent icc = InContructionComponent.MAPPER.get(building);
            if (icc != null) {
                --icc.building;
                if (icc.building == 0) {
                    level.getECS().removeEntity(building);
                    if (Team1Component.MAPPER.has(building)) {
                        level.getPlayer1().setMoney(level.getPlayer1().getMoney() + price);
                    } else if (Team2Component.MAPPER.has(building)) {
                        level.getPlayer2().setMoney(level.getPlayer2().getMoney() + price);
                    }
                }
            }
        }
    }
    
    /**
     * Get the position that the order is trying to move the ship to
     *
     * @return an position if it currently exists
     */
    public Optional<Vector2> getPosition() {
        if (building == null || desired == null) return Optional.empty();
        return Optional.of(desired);
    }
    
    @Override
    public float getPositionX() {
        return desired.x;
    }
    
    @Override
    public float getPositionY() {
        return desired.y;
    }
    
    @Override
    public boolean doDraw() {
        return building != null && desired != null;
    }
}
