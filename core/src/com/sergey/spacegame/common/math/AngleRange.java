package com.sergey.spacegame.common.math;

/**
 * This represents a range of angles
 *
 * @author sergeys
 */
@SuppressWarnings("ConstantConditions")
public final class AngleRange {
    
    private double min;
    private double max;
    
    /**
     * Create an angle range from two angle objects
     *
     * @param min - the minimum angle
     * @param max - the maximum angle
     */
    public AngleRange(Angle min, Angle max) {
        this(min.getAngle(), max.getAngle());
    }
    
    /**
     * Create an angle range from two doubles
     *
     * @param min - the minimum angle
     * @param max - the maximum angle
     */
    public AngleRange(double min, double max) {
        this.min = min;
        this.max = max;
        
        //Fix the angles so that min is always less than max
        if (min > max) this.max += (Angle.USE_DEGREES ? 360 : Angle.PI2);
    }
    
    /**
     * Set the maximum angle
     *
     * @param max - the new maximum angle
     */
    public void setMax(double max) {
        this.max = max;
        if (min > this.max) this.max += (Angle.USE_DEGREES ? 360 : Angle.PI2);
    }
    
    /**
     * Set the minimum angle
     *
     * @param min - the new minimum angle
     */
    public void setMin(double min) {
        this.min = min;
    }
    
    /**
     * Get the maximum angle as a double
     *
     * @return the maximum angle as a double
     */
    public double getMaxD() {
        return max;
    }
    
    /**
     * Get the maximum angle
     *
     * @return the maximum angle
     */
    public Angle getMax() {
        return new Angle(max);
    }
    
    /**
     * Set the maxium angle
     *
     * @param max - the maximum angle
     */
    public void setMax(Angle max) {
        this.max = max.getAngle();
    }
    
    /**
     * Get the minimum angle as a double
     *
     * @return the minimum angle as a double
     */
    public double getMinD() {
        return min;
    }
    
    /**
     * Get the minimum angle
     *
     * @return the minimum angle
     */
    public Angle getMin() {
        return new Angle(min);
    }
    
    /**
     * Set the minimum angle
     *
     * @param min - the minimum angle
     */
    public void setMin(Angle min) {
        this.min = min.getAngle();
    }
    
    /**
     * Check if an angle is in this range
     *
     * @param angle - the angle to check
     *
     * @return is the angle contained within this range
     */
    public boolean isInRange(Angle angle) {
        return this.isInRange(angle.getAngle());
    }
    
    /**
     * Check if an angle is in this range
     *
     * @param angle - a double representing the angle
     *
     * @return is the angle contained within this range
     */
    public boolean isInRange(double angle) {
        //Fixes angle in case its wrong
        if (angle < min) angle += (Angle.USE_DEGREES ? 360 : Angle.PI2);
        if (angle > max) angle -= (Angle.USE_DEGREES ? 360 : Angle.PI2);
        
        return angle > min && angle < max;
    }
}
