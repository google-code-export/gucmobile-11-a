package com.kharamly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class KharamlyActivity extends MapActivity {
	MapView mapView;
	MyCustomizedLocationOverlay myLocationOverlay;
	List<MapRouteOverlay> routeOverlay = new ArrayList<MapRouteOverlay>();
	
	private final static String API_URL = "http://kharamly.alwaysdata.net/api/";
	private final static int TIMEOUT_MILLISEC = 0;
	private final static String TAG_NAME = "Kharamly";
	private String destination = "29.985067,31.43873"; // GUC ;)
	private LocationManager manager;
	
	/** 
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    }
		
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		
		myLocationOverlay = new MyCustomizedLocationOverlay(this, mapView);
		
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableMyLocation();
        //         new PromptDialog(KharamlyActivity.this, R.string.title, R.string.enter_comment){
        //     public boolean onOkClicked(String input) {
        //                 toast(input);
        //                 return true;
        //     }
        // };
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
			}
		});
		
		mapView.postInvalidate();
	}
	
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	            	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            	   startActivityForResult(intent, 5); 
	               }
	               
	               
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
	                    dialog.cancel();
	                    finish();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 5 && resultCode == 0){
            //String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
    	        buildAlertMessageNoGps();
    	    }
        }
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

	
	public void toast(String message) {
	    Log.e(TAG_NAME, message);
	    Toast.makeText(this, message, Toast.LENGTH_LONG);
	}
	
	public static void background(final Runnable r) {
		new Thread() {
			public void run() {
				r.run();
			}
		}.start();
	}
	
	/**
	 * Source: http://www.codeproject.com/KB/android/jsonandroidphp.aspx
	 */
    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
            sb.append(line + "\n");                
        is.close();
        return sb.toString();
    }
	
	public class MyCustomizedLocationOverlay extends MyLocationOverlay {
	    MapView mapView;
	    MapController mapController;
	    
		public MyCustomizedLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
			this.mapView = mapView;
			this.mapController = mapView.getController();
		}
		
		@Override
		public void onLocationChanged(Location location) {
    		
		    float speed = location.getSpeed();
            mapController.setZoom(speed <= 5 ? 21 : 
                                                speed >= 28 ? 15 : 
                                                                (int) ((speed * -6 + 513) / 23));
		    /**
		     * Remove any old route info
		     */
		    for (MapRouteOverlay o : KharamlyActivity.this.routeOverlay) {
		        mapView.getOverlays().remove(o);
		    }
		    routeOverlay.clear();
		    
		    // Let's pretend for now we'll update the routes everytime the location is changed 
		    // this might drain the battery, but we'll see!
		    
            // Source: http://www.codeproject.com/KB/android/jsonandroidphp.aspx
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(API_URL + 
                                        location.getLatitude() + "," + location.getLongitude() + "/" + 
                                        destination + "/" +
                                        location.getSpeed() + "/" +
                                        Installation.id(KharamlyActivity.this));
            try {
                HttpResponse httpresponse = httpclient.execute(httpget);
                String responseBody = convertStreamToString(httpresponse.getEntity().getContent());
                
                JSONObject json = new JSONObject(responseBody);
                JSONArray jArray = json.getJSONArray("steps");
                
                ArrayList<HashMap<String, Integer>> mylist = new ArrayList<HashMap<String, Integer>>();

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject e = jArray.getJSONObject(i);

                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                    map.put("s_lng", (int) (e.getDouble("s_lng") * 1E6));
                    map.put("s_lat", (int) (e.getDouble("s_lat") * 1E6));
                    map.put("e_lng", (int) (e.getDouble("e_lng") * 1E6));
                    map.put("e_lat", (int) (e.getDouble("e_lat") * 1E6));
                    map.put("col", e.getInt("col"));

                    mylist.add(map);
                }
                
                // Process mylist (list of hashmaps) to show on map
                List<Overlay> overlays = mapView.getOverlays();
                GeoPoint center = null;
                for (HashMap<String, Integer> step : mylist) {
                    GeoPoint start = new GeoPoint(step.get("s_lat"), step.get("s_lng"));
                    GeoPoint end = new GeoPoint(step.get("e_lat"), step.get("e_lng"));
                    
                    if (center == null) {
                        center = start;
                    }
                    
                    overlays.add(new MapRouteOverlay(start, end, step.get("col")));
                }
                
                mapView.invalidate();                
    		    mapController.animateTo(center);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
	
	public class MapRouteOverlay extends Overlay {
        private GeoPoint gp1;
        private GeoPoint gp2;
        private int color;

        public MapRouteOverlay(GeoPoint gp1, GeoPoint gp2, int color) {
            this.gp1 = gp1;
            this.gp2 = gp2;
            this.color = color;
        }
        
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            Projection projection = mapView.getProjection();
            Paint paint = new Paint();
            Point point = new Point();
            projection.toPixels(gp1, point);
            paint.setColor(color);
            Point point2 = new Point();
            projection.toPixels(gp2, point2);
            paint.setStrokeWidth(5);
            paint.setAlpha(120);
            canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
            super.draw(canvas, mapView, shadow);
        }
	}
}
