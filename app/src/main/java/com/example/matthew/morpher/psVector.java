package com.example.matthew.morpher;

import android.graphics.Point;

/**
 * psVector class
 * Represents a vector
 */
public class psVector {
    //
    private double x;
    private double y;
    private double length;

    /**
     * Constructor for a vector
     * @param head Head point for the vector
     * @param tail Tail point for the vector
     */
    public psVector(Point head, Point tail){
        x = head.x - tail.x;
        y = head.y - tail.y;
        length = Math.sqrt((x * x) + (y * y));
    }

    /**
     * Constructor for a vector
     * @param newX X value for the vector
     * @param newY Y value for the vector
     */
    public psVector(double newX, double newY){
        x = newX;
        y = newY;
        length = Math.sqrt((x * x) + (y * y));
    }

    /**
     * Getter for the length of the vector
     * @return Length of the vector
     */
    public double getLength(){
        return length;
    }

    /**
     * Getter for the X value of the vector
     * @return X value
     */
    public double getX(){
        return x;
    }

    /**
     * Getter for the Y value of the vector
     * @return Y value
     */
    public double getY(){
        return y;
    }
}