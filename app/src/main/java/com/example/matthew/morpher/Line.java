package com.example.matthew.morpher;

import android.graphics.Point;

/**
 * Line class
 * Represents a line
 */
public class Line {
    //Class members
    private Point startPt;
    private Point endPt;
    public boolean isSelected = false;
    public boolean startSelected = false;
    public boolean endSelected = false;
    public boolean pairSelection = false;

    /**
     * Line constructor
     * @param start Start point of the line
     * @param end End point of the line
     */
    public Line(Point start, Point end){
        startPt = start;
        endPt = end;
    }

    /**
     * Getter for the start of the line
     * @return Start point
     */
    public Point getStart(){
        return startPt;
    }

    /**
     * Getter for the end of the line
     * @return End point
     */
    public Point getEnd(){
        return endPt;
    }

    /**
     * Setter for the start of the line
     * @param newStart Start point to set
     */
    public void setStart(Point newStart){
        startPt = newStart;
    }

    /**
     * Setter for the end of the line
     * @param newEnd End point to set
     */
    public void setEnd(Point newEnd){
        endPt = newEnd;
    }
}
