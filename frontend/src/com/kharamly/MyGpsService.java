package com.kharamly;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class MyGpsService extends Service {
	Thread triggerService;
	LocationManager lm;
	GPSListener myLocationListener;
	boolean isRunning = true;

	public MyGpsService() {

	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);

		Log.e("<<MyGpsService-onStart>>", "I am alive-GPS!");
		// we place the slow work of the service in its own thread so the
		// response we send our caller who run a "startService(...)" method
		// gets a quick OK from us.
		triggerService = new Thread(new Runnable() {
			public void run() {
				try {
					Looper.prepare();
					// try to get your GPS location using the LOCATION.SERVIVE
					// provider
					lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					// This listener will catch and disseminate location updates
					myLocationListener = new GPSListener();
					long minTime = 10000; // frequency update: 10 seconds
					float minDistance = 50; // frequency update: 50 meter
					lm.requestLocationUpdates(
							// request GPS updates
							LocationManager.GPS_PROVIDER, minTime, minDistance,
							myLocationListener);

					Looper.loop();
				} catch (Exception e) {
					Log.e("MYGPS", e.getMessage());
				}
			}// run
		});
		triggerService.start();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	String GPS_FILTER = "guc.action.GPS_LOCATION";

	
	private class GPSListener implements LocationListener {
		public void onLocationChanged(Location location) {
			// capture location data sent by current provider
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			// assemble data bundle to be broadcasted
			Intent myFilteredResponse = new Intent(GPS_FILTER);
			myFilteredResponse.putExtra("latitude", latitude);
			myFilteredResponse.putExtra("longitude", longitude);
			Log.e(">>GPS_Service<<", "Lat:" + latitude + " lon:" + longitude);
			// send the location data out
			sendBroadcast(myFilteredResponse);
		}

	
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	}
}
