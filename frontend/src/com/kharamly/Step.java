package com.kharamly;

import com.google.android.maps.GeoPoint;
/**
 * @author Ahmed Abouraya
 * 
 *         this class represents the step which is the shortest path between two nodes
 */
public class Step {
GeoPoint source, destination;
double distance, duration, speed;
double startTime;
public void calculateSpeed(){
	speed=distance/duration;
}
}
