package com.kharamly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import twitter4j.GeoLocation;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

	private final static int TIMEOUT_MILLISEC = 0;
	private final static String TAG_NAME = "Kharamly";
	private String destination = "29.985067,31.43873"; 
	private LocationManager manager;
	private boolean flag = true;
	
	final int[] BADGES = new int[]{
			R.drawable.checkin_1,
			R.drawable.checkin_50,
			R.drawable.checkin_100,
			R.drawable.checkin_500,
			R.drawable.checkin_1000,
			R.drawable.adventurer,
			R.drawable.addict,
			R.drawable.fanboy,
			R.drawable.super_user,
			R.drawable.warrior,
			R.drawable.junkie,
			R.drawable.speedster_100,
			R.drawable.speedster_140,
			R.drawable.speedster_180,
			R.drawable.turtle,
			R.drawable.granny,
			R.drawable.snail,
			R.drawable.lunatic,
			R.drawable.wacko,
			R.drawable.badger
		};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		panel = (SlidingPanel) findViewById(R.id.panel);

		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
		else
		{
			newDestination();
		}
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		myLocationOverlay = new MyCustomizedLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(
						myLocationOverlay.getMyLocation());
			}
		});

		mapView.postInvalidate();
	}
	
	private void newDestination()
	{
		PromptDialog dest =  new PromptDialog(KharamlyActivity.this, R.string.title, R.string.enter_comment){
            public boolean onOkClicked(String input, DialogInterface dialog) {
                        if(input.length()==0)
                        {
                        	newDestination();
                        	toast(getResources().getString(R.string.empty_dest));
                        }
                        else
                        {
                        	destination = URLEncoder.encode(input);
                        	if(haveNetworkConnection())
                        		geocoding();
                        	else
                        	{
                        		dialog.dismiss();
                        		buildAlertMessageNoInternet();
                        	}
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
		try {
			HttpResponse httpresponse = httpclient.execute(httpget);
			String responseBody = convertStreamToString(httpresponse
					.getEntity().getContent());
			JSONObject json = new JSONObject(responseBody);
			String status = json.get("status").toString();
			if (status.equalsIgnoreCase("ZERO_RESULTS")) {
				toast(getResources().getString(R.string.dest_not_found));
				newDestination();
			} else {
				ProgressDialog dialog = ProgressDialog.show(
						KharamlyActivity.this, "", getResources().getString(R.string.loading),
						true);
				JSONArray results = (JSONArray) json.get("results");
				JSONObject result = results.getJSONObject(0);
				JSONObject geo = (JSONObject) result.get("geometry");
				JSONObject location = (JSONObject) geo.get("location");
				String lat = location.get("lat").toString();
				String lng = location.get("lng").toString();
				Log.i(TAG_NAME, lat + " , " + lng);
				dialog.cancel();
				destination = lat + "," + lng;
				flag = true;
			}
		} catch (Exception e) {

		}
	}
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.nogps))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent, 5);
							}

						})
				.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							@SuppressWarnings("unused") final int id) {
						dialog.cancel();
						finish();
					}
				});
		builder.setIcon(R.drawable.nogps);
		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void buildAlertMessageNoInternet() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				getResources().getString(R.string.no_net))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								Intent intent = new Intent(
										Settings.ACTION_WIRELESS_SETTINGS);
								startActivityForResult(intent, 4);
							}

						})
				.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							@SuppressWarnings("unused") final int id) {
						dialog.cancel();
//						finish();
					}
				});
		builder.setIcon(R.drawable.nowireless);
		final AlertDialog alert2 = builder.create();
		alert2.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 5 && resultCode == 0) {
			// String provider = Settings.Secure.getString(getContentResolver(),
			// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				buildAlertMessageNoGps();
			}
			else
				newDestination();
		}
		if (requestCode == 4 && resultCode == 0) {
			if(!haveNetworkConnection())
			{
				buildAlertMessageNoInternet();
			}
			else
				geocoding();
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
		builder.setMessage(getResources().getString(R.string.exit))
				.setCancelable(false)
				.setPositiveButton(getResources().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						})
				.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
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
	
	/**
	 * Displays a toast message with the badge that the user has acquired
	 * Along with some text congratulating him
	 * @param drawableId image id of the badge
	 * @param name name of the badge
	 * @author Shanab
	 */
	public void badgeNotification(int drawableId, String name, String value) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(drawableId);
		TextView text = (TextView) layout.findViewById(R.id.text);
		
		if (value.trim().length() > 0) {
			name += ":" + value;
		}
		text.setText("Congratulations! You just won the " + name + " badge");

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
		toast.show();
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
	public static String convertStreamToString(InputStream is)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null)
			sb.append(line + "\n");
		is.close();
		return sb.toString();
	}

	public class MyCustomizedLocationOverlay extends MyLocationOverlay {
		
		// Variables Added by Monayri
		MapView mapView;
		MapController mapController;
		
		// This variable contains the overlay of the user position marker
		MapRouteOverlay markerOverlay;
		
		// This variable contains the steps of the first route
		ArrayList<StepInfo> route1Overlays = new ArrayList<StepInfo>();
		// This variable contains the steps of the second route
		ArrayList<StepInfo> route2Overlays = new ArrayList<StepInfo>();
		// This variable contains the steps of the third route
		ArrayList<StepInfo> route3Overlays = new ArrayList<StepInfo>();
		
		// This variable containts the overlay of the marker of the first route
		MapRouteOverlay route1markerOverlay;
		//This variable contains the position of the marker of the first route
		GeoPoint route1marker;
		// This variable containts the overlay of the marker of the second route
		MapRouteOverlay route2markerOverlay;
		//This variable contains the position of the marker of the second route
		GeoPoint route2marker;
		// This variable containts the overlay of the marker of the third route
		MapRouteOverlay route3markerOverlay;
		//This variable contains the position of the marker of the first route
		GeoPoint route3marker;
		// The number of routes returned by the server
		int routes = 0;
		// Chosen Route
		int routeChosen = 1;
		// The step user currently moving at 
		StepInfo chosenStep = null;
		// The location of the user
		GeoPoint loc;
		public MyCustomizedLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
			this.mapView = mapView;
			this.mapController = mapView.getController();
		}
		
		
		/**
		 * The onlocation Changed Method first checks if the routes are not queries from the server yet. 
		 * if its not, it calls the server using httpGet request and gets the routes, the routes is after that
		 * saved locally and drawn on the map.
		 * 
		 * @author Monayri
		 */
		@Override
		public void onLocationChanged(Location location) {

			// Setting the Zoom level
			mapController.setZoom(15);
			// if we dont have the routes yet
			if(flag){
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(Cons.API_URL + location.getLatitude()
						+ "," + location.getLongitude() + "/" + destination + "/"
						+ location.getSpeed() + "/"
						+ Installation.id(KharamlyActivity.this));
				
				try {
					// Getting the response from the server
					HttpResponse httpresponse = httpclient.execute(httpget);
					String responseBody = convertStreamToString(httpresponse
							.getEntity().getContent());
					JSONObject json = new JSONObject(responseBody);
					/* START OF BADGES */
					JSONArray badgeArray = json.getJSONArray("badges");
					for (int i = 0; i < badgeArray.length(); i++) {
						JSONObject badge = badgeArray.getJSONObject(i);
						int id = badge.getInt("id");
						String name = badge.getString("name");
						String value = badge.getString("value");
						badgeNotification(BADGES[id-1], name, value);
					}
					/* END OF BADGES */
					// No we'll iterate over the routes to draw and save the stepInfos
					JSONArray jArray = json.getJSONArray("routes");
					List<Overlay> overlays = mapView.getOverlays();
					routes = jArray.length();
					for (int i = 0; i < jArray.length(); i++) {
						JSONObject route = jArray.getJSONObject(i);
						JSONArray steps = route.getJSONArray("steps");
						Log.e("S number", ""+steps.length());
						for(int j = 0 ; j <steps.length();j++){
							JSONObject step = steps.getJSONObject(j);
							String polyline = step.getString("polyline");
							// polylines are decoded to get the points to be drawn
							//########## Start of Decoder #############
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
								int dlat = ((result & 1) != 0 ? ~(result >> 1)
										: (result >> 1));
								lat += dlat;
								shift = 0;
								result = 0;
								do {
									b = polyline.charAt(index++) - 63;
									result |= (b & 0x1f) << shift;
									shift += 5;
								} while (b >= 0x20);
								int dlng = ((result & 1) != 0 ? ~(result >> 1)
										: (result >> 1));
								lng += dlng;
								GeoPoint p = new GeoPoint(
										(int) (((double) lat / 1E5) * 1E6),
										(int) (((double) lng / 1E5) * 1E6));
								geopoints.add(p);
								}
							//############### End of Decoder ###################
							
							// getting the color of the step from the json
							int color = step.getInt("col");
							// getting whether this step includes the marker of the route
							int marker = step.getInt("marker");
							// getting the id of the step
							int id = step.getInt("loc");
							// if its the first route dont make the route transparent and make it opaque
							if (i ==0 ){
								// Drawing the step using the points created and the color extracted, and 255 is the value of the alpha that will make the step opaque
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color,255, i );
								StepInfo info = new StepInfo(geopoints, color, id, step.getDouble("s_lng"),step.getDouble("s_lat"));
								route1Overlays.add(info);
								overlays.add(mro);
								// if the step includes the marker Draw the marker on the step
								if (marker ==1 ){
									route1marker = new GeoPoint(
											(int) (((double) step.getDouble("s_lat")) * 1E6),
											(int) (((double) step.getDouble("s_lng")) * 1E6));
									route1markerOverlay = new MapRouteOverlay(route1marker, 3);
									overlays.add(route1markerOverlay);
								}
							}
							else if (i ==1 ){
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color,50, i );
								StepInfo info = new StepInfo(geopoints, color, id, step.getDouble("s_lng"),step.getDouble("s_lat"));
								route2Overlays.add(info);
								overlays.add(mro);
								if (marker ==1 ){
									route2marker = new GeoPoint(
											(int) (((double) step.getDouble("s_lat")) * 1E6),
											(int) (((double) step.getDouble("s_lng")) * 1E6));
									route2markerOverlay = new MapRouteOverlay(route2marker, 2);
									overlays.add(route2markerOverlay);
								}
							}
							else if (i ==2 ){
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color, 50, i );
								StepInfo info = new StepInfo(geopoints, color, id, step.getDouble("s_lng"),step.getDouble("s_lat"));
								route3Overlays.add(info);
								overlays.add(mro);
								if (marker ==1 ){
									route3marker = new GeoPoint(
											(int) (((double) step.getDouble("s_lat")) * 1E6),
											(int) (((double) step.getDouble("s_lng")) * 1E6));
									route3markerOverlay = new MapRouteOverlay(route3marker, 2);
									overlays.add(route3markerOverlay);
								}
							}

						}
					
					

					
				}
				
				// Drawing the marker representing the user position
				loc = new GeoPoint(
						(int) (location.getLatitude() * 1000000),
						(int) (location.getLongitude() * 1000000));
				MapRouteOverlay marker = new MapRouteOverlay(loc, 1);
				markerOverlay = marker;
				overlays.add(marker);
				mapView.invalidate();
				mapController.animateTo(loc);
				chosenStep = route1Overlays.get(0);
				// setting flag to false indicating that the routes are gotten
				flag = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			}else{
				// In the else part meaning that we already have the routes, 
				// we handle the pings that send the updates to the server 
				// about the congestion of the current step represented by the speed of the user
				float speed = location.getSpeed();
				double lng = location.getLongitude();
				double lat = location.getLatitude();
				// We remove the old overlay of the user position and update the user's position on the map
				mapView.getOverlays().remove(markerOverlay);
				GeoPoint loc = new GeoPoint(
						(int) (lat * 1000000),
						(int) (lng * 1000000));
				MapRouteOverlay marker = new MapRouteOverlay(loc, 1);
				markerOverlay = marker;
				mapView.getOverlays().add(marker);
				mapView.invalidate();
				mapController.animateTo(loc);
				if(chosenStep != null){
					// checking ig the end of the current step is reached or not, <0.00009 in lat and long is approximately 10 meters in reality
					if(Math.abs(lng-chosenStep.lng) < 0.00009 && Math.abs(lat-chosenStep.lat)<0.00009){
						// Sending the ping to the server
						HttpClient httpclient = new DefaultHttpClient();
						HttpGet httpget = new HttpGet(Cons.SERVER_URL + "update/" + chosenStep.id
								+ speed + "/"
								+ Installation.id(KharamlyActivity.this));
						try{
							HttpResponse response = httpclient.execute(httpget);
						}catch(Exception e){
							
						}
						// Getting the next step in the current route taken
						getNextStep();
						
					}
				}
			}
		}
		
		/**
		 * This method gets the next step in the current route taken
		 * @author Monayri
		 */
		public void getNextStep(){
			if(routeChosen == 1){
				int index = route1Overlays.indexOf(chosenStep) + 1;
				if(index == route1Overlays.size() || index == -1)
					chosenStep = null;
				else
					chosenStep = route1Overlays.get(index);
			}
			else if(routeChosen == 2){
				int index = route2Overlays.indexOf(chosenStep) + 1;
				if(index == route2Overlays.size()|| index == -1)
					chosenStep = null;
				else
					chosenStep = route2Overlays.get(index);
			}
			else if(routeChosen == 3){
				int index = route3Overlays.indexOf(chosenStep) + 1;
				if(index == route3Overlays.size()|| index == -1)
					chosenStep = null;
				else
					chosenStep = route3Overlays.get(index);
			}
				
		}
		/**
		 * This method is used to draw the map again using the info saved when the routes were
		 * retrieved from the server
		 * @param mapview
		 * @author Monayri
		 */
		public  void drawMap(MapView mapview){
			List<Overlay> overlays = mapView.getOverlays();
			MapRouteOverlay mro;
			
			// First Drawing each Route
			for (StepInfo info : route1Overlays){
				if (routeChosen == 1 ){
					mro = new MapRouteOverlay(info.points, info.color, 255, 1 );
					route1markerOverlay = new MapRouteOverlay(route1marker, 3);
					overlays.add(route1markerOverlay);
				}else{
					mro = new MapRouteOverlay(info.points, info.color, 50, 1 );
					route1markerOverlay = new MapRouteOverlay(route1marker, 2);
					overlays.add(route1markerOverlay);
				}
				overlays.add(mro);
				}
			for (StepInfo info : route2Overlays){
				if (routeChosen == 2 ){
					mro = new MapRouteOverlay(info.points, info.color, 255, 2);
					route2markerOverlay = new MapRouteOverlay(route2marker, 3);
					overlays.add(route2markerOverlay);
				}else{
					route2markerOverlay = new MapRouteOverlay(route2marker, 2);
					overlays.add(route2markerOverlay);
					mro = new MapRouteOverlay(info.points, info.color, 50, 2);
				}
				overlays.add(mro);
				}
			for (StepInfo info : route3Overlays){
				if (routeChosen == 3 ){
					mro = new MapRouteOverlay(info.points, info.color, 255, 1 );
					route3markerOverlay = new MapRouteOverlay(route3marker, 3);
					overlays.add(route3markerOverlay);
				}else{
					mro = new MapRouteOverlay(info.points, info.color, 50, 1 );
					route3markerOverlay = new MapRouteOverlay(route3marker, 2);
					overlays.add(route3markerOverlay);
				}
				overlays.add(mro);
				}
			// Then Drawing the user's position
			MapRouteOverlay marker = new MapRouteOverlay(loc, 1);
			markerOverlay = marker;
			mapView.getOverlays().add(marker);
		}
		
		/**
		 * This method handles the touchevents in case the user wants to switch between routes on the map
		 * the user clicks on the route's marker to choose that route
		 * @author Monayri
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapview) {

			if (event.getAction() == 1) {
				 	// Getting the position of the touch
				    int x = (int) event.getX();  
				    int y = (int) event.getY();
				    // Getting the position of the first marker
				    Point point = new Point();
					mapview.getProjection().toPixels(route1marker, point);
					
					// if the marker is touched and its not the chosenRoute then the map wll be drawn again
					if (Math.abs(x-point.x) < 40 && Math.abs(y-point.y)<40) {
				        if ( routeChosen != 1){
				        	mapview.getOverlays().clear();
				    		mapview.invalidate();
				    		mapview.getOverlays().add(this);
				        	routeChosen = 1;
				        	drawMap(mapview);
				        }
				    }
					
					// Same steps as route1
					if (route2marker !=null){
						mapview.getProjection().toPixels(route2marker, point);
						if (Math.abs(x-point.x) < 40 && Math.abs(y-point.y)<40) {
					        if ( routeChosen != 2){
					        	mapview.getOverlays().clear();
					    		mapview.invalidate();
					    		mapview.getOverlays().add(this);
					        	routeChosen = 2;
					        	drawMap(mapview);
					        }
					    }
					}
					
					// Same steps as routes 1 & 2
					if(route3marker != null){
						mapView.getProjection().toPixels(route3marker, point);
						if (Math.abs(x-point.x) < 40 && Math.abs(y-point.y)<40) {
					        if ( routeChosen != 3){
					        	mapview.getOverlays().clear();
					    		mapview.invalidate();
					    		mapview.getOverlays().add(this);
					        	routeChosen = 3;
					        	drawMap(mapview);
					        }
					    }
					}
				    
			}
			return false;
		}

	}
	/**
	 * This class draws anything needs to be drawn on the mapview (Routes and markers)
	 * @author Monayri
	 *
	 */
	public class MapRouteOverlay extends Overlay {
		private GeoPoint gp1; // The point of the marker to be drawn in case of markers
		private int color; // The color of the step to be drawn
		private boolean marker; // a boolean indicating if we're drawing markers or steps
		private Bitmap bmp; // The bitmap of the marker image
		private int alpha = 180; // Transparency level
		private int routeNo;
		private ArrayList<GeoPoint> pointList; // The pointList of the step to be drawn

		/**
		 * This constructor is for drawing the markers
		 * @param gp1
		 * @param index
		 * @author Monayri
		 */
		public MapRouteOverlay(GeoPoint gp1, int index) {
			this.marker = true;
			this.gp1 = gp1;
			if(index ==1 )
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.car);
			else if (index ==2)
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin1);
			else if (index ==3)
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pin2);
		}
		/**
		 * This constructor is for drawing a step
		 * @param pointList
		 * @param color
		 * @param alpha
		 * @param routeNo
		 * @author Monayri
		 */
		public MapRouteOverlay(ArrayList<GeoPoint> pointList, int color, int alpha, int routeNo) {
			this.pointList = pointList;
			this.color = color;
			this.alpha = alpha;
			this.routeNo = routeNo;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			if (marker) {
				Point point = new Point();
				mapView.getProjection().toPixels(gp1, point);
				int x = point.x - bmp.getWidth() / 2;
				int y = point.y - bmp.getHeight();
				canvas.drawBitmap(bmp, x, y, null);
			} else {
				Point current = new Point();
				Path path = new Path();
				Projection projection = mapView.getProjection();

				Iterator<GeoPoint> iterator = pointList.iterator();
				if (iterator.hasNext()) {
					projection.toPixels(iterator.next(), current);
					path.moveTo((float) current.x, (float) current.y);
				} else
					return;
				while (iterator.hasNext()) {
					projection.toPixels(iterator.next(), current);
					path.lineTo((float) current.x, (float) current.y);
				}

				Paint pathPaint = new Paint();
				pathPaint.setAntiAlias(true);
				pathPaint.setStrokeWidth(8.0f);
				pathPaint.setStyle(Paint.Style.STROKE);
				pathPaint.setColor(color);
				pathPaint.setAlpha(alpha);
				canvas.drawPath(path, pathPaint);
				
			}

		}
	}
	
	public class StepInfo {
		ArrayList<GeoPoint> points ;
		int color ;
		int id ;
		double lng;
		double lat;
		public StepInfo ( ArrayList <GeoPoint> points, int color, int id, double lng, double lat){
			this.points = points;
			this.color = color;
			this.id = id;
			this.lng = lng;
			this.lat = lat;
		}
	}

}
