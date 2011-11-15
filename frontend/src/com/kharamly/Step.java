package com.kharamly;

import com.google.android.maps.GeoPoint;

public class Step {
GeoPoint source, destination;
double distance, duration, speed;
double startTime;
public void calculateSpeed(){
	speed=distance/duration;
}
}
