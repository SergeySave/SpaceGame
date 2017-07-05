package com.sergey.spacegame.common.math;

@SuppressWarnings("ConstantConditions")
public final class AngleRange {
    
    private double min;
    private double max;
    
    public AngleRange(Angle min, Angle max) {
        this(min.getAngle(), max.getAngle());
    }
    
    public AngleRange(double min, double max) {
        this.min = min;
        this.max = max;
        
        //Fix the angles so that min is always less than max
        if (min > max) this.max += (Angle.USE_DEGREES ? 360 : Angle.PI2);
    }
    
    public void setMax(double max) {
        this.max = max;
        if (min > this.max) this.max += (Angle.USE_DEGREES ? 360 : Angle.PI2);
    }
    
    public void setMin(double min) {
        this.min = min;
    }
    
    public double getMaxD() {
        return max;
    }
    
    public Angle getMax() {
        return new Angle(max);
    }
    
    public void setMax(Angle max) {
        this.max = max.getAngle();
    }
    
    public double getMinD() {
        return min;
    }
    
    public Angle getMin() {
        return new Angle(min);
    }
    
    public void setMin(Angle min) {
        this.min = min.getAngle();
    }
    
    public boolean isInRange(Angle angle) {
        return this.isInRange(angle.getAngle());
    }
    
    public boolean isInRange(double angle) {
        //Fixes angle in case its wrong
        if (angle < min) angle += (Angle.USE_DEGREES ? 360 : Angle.PI2);
        if (angle > max) angle -= (Angle.USE_DEGREES ? 360 : Angle.PI2);
        
        return angle >= min && angle <= max;
    }
}
