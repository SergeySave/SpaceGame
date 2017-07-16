package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.Level;

public class MoveOrder implements IOrder {
    
    private double  x;
    private double  y;
    private float   speed;
    private boolean done;
    
    public MoveOrder(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = -1;
    }
    
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
    public boolean completed() {
        return done;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}
