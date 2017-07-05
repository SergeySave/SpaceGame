package com.sergey.spacegame.common.math;

public final class Angle {
    
    public static final boolean USE_DEGREES = true;
    public static final double  PI2         = Math.PI * 2;
    
    private double angle;
    
    public Angle(double angle) {
        this.angle = angle;
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
