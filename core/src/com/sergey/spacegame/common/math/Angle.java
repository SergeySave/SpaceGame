package com.sergey.spacegame.common.math;

public final class Angle {
    
    public static final boolean USE_DEGREES = true;
    public static final double  PI2         = Math.PI * 2;
    
    private double angle;
    
    public Angle(double angle) {
        this.angle = angle;
    }
    
    public static double getThroughRotateDistance(double angle1, double angle2) {
        double dr = Math.abs(angle1 - angle2) % (Angle.USE_DEGREES ? 360 : Angle.PI2);
        if (dr > (Angle.USE_DEGREES ? 180 : Math.PI)) return (Angle.USE_DEGREES ? 360 : Angle.PI2) - dr;
        return dr;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public void setAngle(double angle) {
        this.angle = angle;
    }
    
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
