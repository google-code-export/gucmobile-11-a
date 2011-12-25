package com.kharamly;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
	SlidingPanel panel;
	MapView mapView;
	MyCustomizedLocationOverlay myLocationOverlay;
	List<MapRouteOverlay> routeOverlay = new ArrayList<MapRouteOverlay>();
	
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

    
	    panel = (SlidingPanel) findViewById(R.id.panel);
		
		manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	    }
		
	    HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://maps.googleapis.com/maps/api/directions/json?origin=30.060421,31.498468&destination=29.985104,31.43888&sensor=true&alternatives=true");
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		
		myLocationOverlay = new MyCustomizedLocationOverlay(this, mapView);
		
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableMyLocation();
		newDestination();
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
			}
		});
		
		mapView.postInvalidate();
	}
	
	private void newDestination()
	{
		PromptDialog dest =  new PromptDialog(KharamlyActivity.this, R.string.title, R.string.enter_comment){
            public boolean onOkClicked(String input) {
                        if(input.length()==0)
                        {
                        	newDestination();
                        	toast("No destination entered, Please write something !!!");
                        }
                        else
                        {
                        	destination = input;
                        	geocoding();
                        }
                        return true;
            }
        };
        dest.show();
	}
	
	private void geocoding()
	{
		String geoUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="+destination+"&sensor=true";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(geoUrl);
		try
		{
			HttpResponse httpresponse = httpclient.execute(httpget);
        	String responseBody = convertStreamToString(httpresponse.getEntity().getContent());
        	JSONObject json = new JSONObject(responseBody);
        	String status = json.get("status").toString();
        	if(status.equalsIgnoreCase("ZERO_RESULTS"))
        	{
        		toast("Sorry, Destination not found !");
        		newDestination();
        	}
        	else
        	{
        		ProgressDialog dialog = ProgressDialog.show(KharamlyActivity.this, "", 
                        "Loading. Please wait...", true);
        		JSONArray results = (JSONArray) json.get("results");
        		JSONObject result = results.getJSONObject(0);
        		JSONObject geo = (JSONObject)result.get("geometry");
        		JSONObject location = (JSONObject)geo.get("location");
        		String lat = location.get("lat").toString();
        		String lng = location.get("lng").toString();
        		Log.i(TAG_NAME, lat+" , "+lng);        		
        		dialog.cancel();
        		toast(lat+" , "+lng);
        	}
		}
		catch(Exception e)
		{
			
		}
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
				break;
			
			case R.id.comments:
				panel.toggle();
				break;
    			
			case R.id.dest:
				newDestination();
				break;
				
			case R.id.close:
				closeKharamly();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void closeKharamly() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to exit?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                finish();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		builder.show();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
	    return true;
	}

	
	public void toast(String message) {
	    Log.e(TAG_NAME, message);
	    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
            HttpGet httpget = new HttpGet(Cons.API_URL + 
                                        location.getLatitude() + "," + location.getLongitude() + "/" + 
                                        destination + "/" +
                                        location.getSpeed() + "/" +
                                        Installation.id(KharamlyActivity.this));
            
            try {
            	ArrayList<HashMap<String, Integer>> mylist = new ArrayList<HashMap<String, Integer>>();
            	HttpResponse httpresponse = httpclient.execute(httpget);
            	String responseBody = convertStreamToString(httpresponse.getEntity().getContent());
            	JSONObject json = new JSONObject(responseBody);
            	JSONArray jArray = json.getJSONArray("steps");
            	List<Overlay> overlays = mapView.getOverlays();
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject e = jArray.getJSONObject(i);
                    StringBuffer urlString = new StringBuffer();
                    urlString.append("http://maps.google.com/maps?f=d&hl=en");
                    urlString.append("&saddr=");// from
                    urlString.append(e.getDouble("s_lat")+","+e.getDouble("s_lng"));
                    urlString.append("&daddr=");// to
                    urlString.append(e.getDouble("e_lat")+","+e.getDouble("e_lng"));
                    urlString.append("&ie=UTF8&0&om=0&output=dragdir"); //DRAGDIR RETURNS JSON
                    String url = urlString.toString();
                    URL inUrl = new URL(url);
                    URLConnection yc = inUrl.openConnection();
                    BufferedReader in = new BufferedReader( new InputStreamReader(yc.getInputStream()));
                    String inputLine;
                    String encoded = "";
                    while ((inputLine = in.readLine()) != null)
                        encoded = encoded.concat(inputLine);
                    in.close();
                    String polyline = encoded.split("points:")[1].split(",")[0];
                    polyline = polyline.replace("\"", "");
                    polyline = polyline.replace("\\\\", "\\");
                    
                    ArrayList<GeoPoint> geopoints = new ArrayList<GeoPoint>();
                    int index = 0, len = polyline.length();
                    int lat = 0, lng = 0;
                    while (index < len) {
                        int b, shift = 0, result = 0;
                        do {
                            b = polyline.charAt(index++) - 63;
                            result |= (b & 0x1f) << shift;
                            shift += 5;
                        } while (b >= 0x20);
                        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                        lat += dlat;
                        shift = 0;
                        result = 0;
                        do {
                            b = polyline.charAt(index++) - 63;
                            result |= (b & 0x1f) << shift;
                            shift += 5;
                        } while (b >= 0x20);
                        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                        lng += dlng;
                        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
                        geopoints.add(p);
                    }
                    int color = e.getInt("col");
                    overlays.add(new MapRouteOverlay(geopoints, color));
                }
                
                GeoPoint loc = new GeoPoint((int)(location.getLatitude()*1000000), (int)(location.getLongitude()*1000000));
                overlays.add(new MapRouteOverlay(loc));
                mapView.invalidate();       
    		    mapController.animateTo(loc);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
	
	public class MapRouteOverlay extends Overlay {
        private GeoPoint gp1;
        private int color;
        private boolean marker;
        private Bitmap bmp;
        private ArrayList<GeoPoint> pointList;
        
        
        public MapRouteOverlay(GeoPoint gp1){
        	this.marker = true;
        	this.gp1 = gp1;
        	bmp = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        }
        
        public MapRouteOverlay(ArrayList<GeoPoint> pointList, int color){
        	this.pointList= pointList;
        	this.color = color;
        }
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        	super.draw(canvas, mapView, shadow);
        	
        	if(marker){
        		Point point = new Point();
                mapView.getProjection().toPixels(gp1, point);                                      
                int x = point.x - bmp.getWidth() / 2;                                              
                int y = point.y - bmp.getHeight();                                                 
                canvas.drawBitmap(bmp, x, y, null);  
        	}else{
                Point current = new Point();
                Path path = new Path();
                Projection projection = mapView.getProjection();
                
                Iterator<GeoPoint> iterator = pointList.iterator();
                if (iterator.hasNext()) {
                    projection.toPixels(iterator.next(), current);
                    path.moveTo((float) current.x, (float) current.y);
                } else return;
                while(iterator.hasNext()) {
                    projection.toPixels(iterator.next(), current);
                    path.lineTo((float) current.x, (float) current.y);
                }

                Paint pathPaint = new Paint();
                pathPaint.setAntiAlias(true);
                pathPaint.setStrokeWidth(4.0f);
                pathPaint.setColor(Color.RED);
                pathPaint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, pathPaint);
        	}
            
        }
	}
	
}
