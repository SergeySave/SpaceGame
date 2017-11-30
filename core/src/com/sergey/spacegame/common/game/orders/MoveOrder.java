package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.Level;

/**
 * Represents a move order that is supposed to travel at a given speed
 *
 * @author sergeys
 */
public class MoveOrder implements MovingOrder {
    
    private double  x;
    private double  y;
    private float   speed;
    private boolean done;
    
    /**
     * Create a move order
     *
     * @param x - the destination x coordinate
     * @param y - the destination y coordinate
     */
    public MoveOrder(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = -1;
    }
    
    /**
     * Create a MoveOrder with a given speed
     *
     * @param x     - the destination x coordinate
     * @param y     - the destination y coordinate
     * @param speed - the speed that should be moved at
     */
    public MoveOrder(double x, double y, float speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (done) return;
        
        PositionComponent pos = PositionComponent.MAPPER.get(e);
        VelocityComponent vel = VelocityComponent.MAPPER.get(e);
        if (vel == null) {
            vel = new VelocityComponent();
            e.add(vel);
        }
        ShipComponent ship = ShipComponent.MAPPER.get(e);
        if (speed < 0) speed = ship.moveSpeed;
    
        double dx   = x - pos.getX();
        double dy   = y - pos.getY();
        double dist = Math.hypot(dx, dy);
        if (dist < speed * deltaTime) {
            done = true;
            pos.setX((float) x);
            pos.setY((float) y);
            e.remove(VelocityComponent.class);
        } else {
            vel.vx = (float) (speed * dx / dist);
            vel.vy = (float) (speed * dy / dist);
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return PositionComponent.MAPPER.has(e) && ShipComponent.MAPPER.has(e);
    }
    
    @Override
    public boolean completed(Entity e) {
        return done;
    }
    
    @Override
    public float getPositionX() {
        return ((float) getX());
    }
    
    /**
     * Get the destination x coordinate
     *
     * @return the destination x coordinate
     */
    public double getX() {
        return x;
    }
    
    @Override
    public float getPositionY() {
        return ((float) getY());
    }
    
    /**
     * Get the destination y coordinate
     *
     * @return the destination y coordinate
     */
    public double getY() {
        return y;
    }
}
