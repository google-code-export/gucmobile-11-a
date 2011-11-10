package com.kharamly;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

// import 

public class KharamlyActivity extends MapActivity {
	// LinearLayout linearLayout; // Not used?
	MapView mapView;
	
	List<Overlay> mapOverlays;
	Drawable drawable;
	HelloItemizedOverlay itemizedOverlay;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mapOverlays = mapView.getOverlays();
		drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		itemizedOverlay = new HelloItemizedOverlay(drawable);
		
		GeoPoint point = new GeoPoint(19240000, -99120000);
		OverlayItem overlayitem = new OverlayItem(point, "", "");
		
		GeoPoint point2 = new GeoPoint(35410000, 139460000);
		OverlayItem overlayitem2 = new OverlayItem(point2, "", "");
		
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);
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
	    return false;
	}
	
	private static class HelloItemizedOverlay extends ItemizedOverlay {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		
		public HelloItemizedOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}
		
		public void addOverlay(OverlayItem overlay) {
		    mOverlays.add(overlay);
		    populate();
		}
		
		@Override
		protected OverlayItem createItem(int i) {
		  return mOverlays.get(i);
		}
		
		public int size() {
			return mOverlays.size();
		}
	}
}
