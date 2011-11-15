package com.kharamly;

import java.util.ArrayList;



import android.content.*;
import android.location.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.*;

public class KharamlyActivity extends MapActivity {
	// LinearLayout linearLayout; // Not used?
	MapView mapView;
	MyCustomizedLocationOverlay myLocationOverlay;

	Button btnStopService;
	TextView txtMsg;
	Intent intentMyService;
	ComponentName service;
	BroadcastReceiver receiver;
	String GPS_FILTER = "guc.action.GPS_LOCATION";

	MapController mc;
	ArrayList<GeoPoint> points;

	// Drawable drawable;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		myLocationOverlay = new MyCustomizedLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(
						myLocationOverlay.getMyLocation());
			}
		});

		//mapView.getOverlays().add(myLocationOverlay);
		mapView.postInvalidate();
		
		txtMsg = (TextView) findViewById(R.id.txtMsg);
		// initiate the service
		intentMyService = new Intent(this, MyGpsService.class);
		service = startService(intentMyService);
		txtMsg.setText("MyGpsService started - (see DDMS Log)");
		// register & define filter for local listener
		IntentFilter mainFilter = new IntentFilter(GPS_FILTER);
		
		registerReceiver(receiver, mainFilter);
		btnStopService = (Button) findViewById(R.id.btnStopService);
		btnStopService.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {

					stopService(new Intent(intentMyService));
					txtMsg.setText("After stoping Service: \n"
							+ service.getClassName());
					btnStopService.setText("Finished");
					btnStopService.setClickable(false);
				} catch (Exception e) {
					Log.e("MYGPS", e.getMessage());
				}
			}

		});
		mc = mapView.getController();
		receiver = new LocalReceiver(mapView,mc,getBaseContext());
		String coordinates[] = { "29.97480761668519", "31.2746619390382" };
		double lat = Double.parseDouble(coordinates[0]);
		double lng = Double.parseDouble(coordinates[1]);
		
		points = Directions.getDirectionData("maadi", "zamalek",getBaseContext(),txtMsg);
		mc.animateTo(points.get(0));
		mc.setZoom(17);
		mapView.invalidate();
	
		PathOverlay.drawPath(points, mapView);
		
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
		case R.id.block:
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
	
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			stopService(intentMyService);
			unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.e("MAIN-DESTROY>>>", e.getMessage());
		}
		Log.e("MAIN-DESTROY>>>", "Adios");

	}
	public static class MyCustomizedLocationOverlay extends MyLocationOverlay {
		public MyCustomizedLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
		}

		@Override
		public void onLocationChanged(Location location) {
			// let's pretend for now we'll update the routes everytime the
			// location is changed
			// this might drain the battery, but we'll see!

			// todo's
			// ping server
			// get response
			// render response on map
		}
	}
}
