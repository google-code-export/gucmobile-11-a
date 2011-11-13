package com.kharamly;

import android.content.*;
import android.location.*;
import android.os.*;
import android.view.*;
import com.google.android.maps.*;

public class KharamlyActivity extends MapActivity {
	// LinearLayout linearLayout; // Not used?
	MapView mapView;
	MyCustomizedLocationOverlay myLocationOverlay;
	
	// Drawable drawable;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		myLocationOverlay = new MyCustomizedLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
			}
		});
		
		mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();
		// drawable = this.getResources().getDrawable(R.drawable.androidmarker);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.block :
			Intent i = new Intent(KharamlyActivity.this, ReportBlocked.class);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return true;
	}
	
	public static class MyCustomizedLocationOverlay extends MyLocationOverlay {
		public MyCustomizedLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
		}
		
		@Override
		public void onLocationChanged(Location location) {
		    // let's pretend for now we'll update the routes everytime the location is changed
		    // this might drain the battery, but we'll see!
		    
		    // todo's
		    // ping server
		    // get response
		    // render response on map
		}
	}
}
