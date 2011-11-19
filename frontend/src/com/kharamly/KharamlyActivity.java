package com.kharamly;

import java.io.*;
import java.util.*;

import android.content.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.google.android.maps.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.json.*;

public class KharamlyActivity extends MapActivity {
	MapView mapView;
	MyCustomizedLocationOverlay myLocationOverlay;
	List<MapRouteOverlay> routeOverlay = new ArrayList<MapRouteOverlay>();
	
	private final static String API_URL = "http://10.0.2.2:8000/api/";
	private final static int TIMEOUT_MILLISEC = 0;
	private final static String TAG_NAME = "Kharamly";
	private String destination = "29.994192,31.444588"; // GUC ;)
	
	/** 
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
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
