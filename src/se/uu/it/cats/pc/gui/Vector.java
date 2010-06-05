package se.uu.it.cats.pc.gui;

import java.util.*;
/**
 * Klassen Vektor.
 *
 * Christian Ålander
 * 2006-04-24
 */
public class Vector
{
    // instance variables - replace the example below with your own
    private double x;
    private double y;

    /**
     * Standard constructor for objects of class Vektor
     */
    public Vector() {
        // initialize instance variables
        x = y = 0;
    }

    // Överlagrad konstruktor
    public Vector(double xs, double ys) {
        x = xs;
        y = ys;
    }

    // getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Add vectors - Receives a vector and adds it with the local one
    public Vector add(Vector other) {
        return new Vector(x+other.getX(), y + other.getY());
    }

    // Subtract vectors
    public Vector subtract(Vector other) {
        return new Vector(x-other.getX(), y-other.getY());
    }

    // Rescale the vector
    public void skala(double lambda) {
        x *= lambda;
        y *= lambda;
    }

    // Returns a vector multiplicated with a number
    public Vector multiplicate(double lambda) {
      return new Vector(x*lambda, y*lambda);
    }

    // Scalar product
    public double scalar(Vector other) {
        return x*other.getX()+y*other.getY();
    }

    // Returns vector as string value.
    public String toString() {
        return "{"+x+"x "+y+"y}";
    }

    // Returns the length of the vector by pythagoras.
    public double vectorLength() {
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }
}
