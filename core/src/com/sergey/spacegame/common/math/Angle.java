package com.sergey.spacegame.common.math;

/**
 * This class represents an angle
 *
 * @author sergeys
 */
public final class Angle {
    
    /**
     * Should the angle and angle range class use degrees or radians
     */
    public static final boolean USE_DEGREES = true;
    /**
     * The value of 2π (used to reduce runtime computations)
     */
    public static final double  PI2         = Math.PI * 2;
    
    private double angle;
    
    /**
     * Create a new angle
     *
     * @param angle - the angle as represented by a double
     */
    public Angle(double angle) {
        this.angle = angle;
    }
    
    /**
     * Get the minimum angle that you would need to rotate an angle at one of the angles to the other angle
     *
     * @param angle1 - the first angle
     * @param angle2 - the second angle
     *
     * @return a double representing the angle through which you would need to rotate
     */
    public static double getThroughRotateDistance(double angle1, double angle2) {
        double dr = Math.abs(angle1 - angle2) % (Angle.USE_DEGREES ? 360 : Angle.PI2);
        if (dr > (Angle.USE_DEGREES ? 180 : Math.PI)) return (Angle.USE_DEGREES ? 360 : Angle.PI2) - dr;
        return dr;
    }
    
    /**
     * Get the value of the angle that this angle represents
     *
     * @return the value of the angle
     */
    public double getAngle() {
        return angle;
    }
    
    /**
     * Set the value that the angle represents
     *
     * @param angle - the new value of the angle
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
    /**
     * Get the value of the angle as a float
     *
     * @return the value of the angle as a float
     */
    public float getAngleF() {
        return (float) angle;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Angle)) return false;
        Angle other = (Angle) obj;
        return this.angle == other.angle;
    }
    
    @Override
    public int hashCode() {
        //Return the sign bit exponent bits and first 20 bits of the mantissa in an integer
        //This makes comparing hashcodes a pretty good way to check if the doubles are "close enough"
        return (int) (Double.doubleToRawLongBits(angle) >> 32);
    }
    
    @Override
    public String toString() {
        return "Angle@" + angle;
    }
}
