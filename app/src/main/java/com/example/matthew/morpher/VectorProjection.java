package com.example.matthew.morpher;

import android.graphics.Point;

/**
 * VectorProjection class
 * Helper class for functions used to calculate vector projection
 */
public class VectorProjection {
    /**
     * Treat the line as a vector and return a point as a pseudo vector
     * @return Point as a pseudo vector
     */
    public psVector lineVector(Point p, Point q){
        return new psVector(p,q);
    }

    /**
     * Get the vector
     * @return Point (vector) that represents the normal
     */
    public psVector normalVector(psVector v){
        double normX = v.getY() * -1;
        double normY = v.getX();
        return new psVector(normX, normY);
    }

    /**
     * Calculate the fraction percent of the projection onto the vector
     * @param fraction Portion of the vector that is projected onto
     * @return percent along the line
     */
    public double getFractionPercent(psVector v, double fraction){
        double vectorLength = v.getLength();
        return fraction / vectorLength;
    }

    /**
     * Calculating the dot product of 2 vectors
     * @param a Vector 1
     * @param b Vector 2
     * @return result of the dot prodcut
     */
    public double dotProduct(psVector a, psVector b){
        return (a.getX() * b.getX()) + (a.getY() * b.getY());
    }

    /**
     * Determine projection length of a onto b
     * @param a Point (vector) to project onto the other Point (vector)
     * @param b Point (vector) being projected on
     * @return length of projection of one vector projected onto another vector
     */
    public double projectionLength(psVector a, psVector b){
        double num = dotProduct(a,b);
        double denom = b.getLength();
        return num / denom;
    }

    /**
     * Calculate the weights of the line
     * @param lineLength Length of the line being weighted
     * @param a Smooths warp
     * @param dist Distance from point to
     * @param p Influence of line length
     * @param b Influence of distance
     * @return Weight of the line
     */
    public double calculateWeight(double lineLength, double a, double dist, double p, double b){
        return Math.pow((Math.pow(lineLength,p) / (a + dist)), b);
    }
}
