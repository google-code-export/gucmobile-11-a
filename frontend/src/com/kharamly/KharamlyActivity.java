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
	List<MapRouteOverlay> routeOverlay = new ArrayList<MapRouteOverlay>();

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
                        	toast("No destination entered, Please write something !!!");
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
				toast("Sorry, Destination not found !");
				newDestination();
			} else {
				ProgressDialog dialog = ProgressDialog.show(
						KharamlyActivity.this, "", "Loading. Please wait...",
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
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent, 5);
							}

						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
				"No Network Available, do you want to enable the WiFi or your Mobile Network?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									@SuppressWarnings("unused") final DialogInterface dialog,
									@SuppressWarnings("unused") final int id) {
								Intent intent = new Intent(
										Settings.ACTION_WIRELESS_SETTINGS);
								startActivityForResult(intent, 4);
							}

						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
		builder.setMessage("Are you sure you want to exit?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
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
		MapView mapView;
		MapController mapController;
		MapRouteOverlay markerOverlay;
		Point markerPos;
		ArrayList<MapRouteOverlay> route1Overlays = new ArrayList<KharamlyActivity.MapRouteOverlay>();
		ArrayList<MapRouteOverlay> route2Overlays = new ArrayList<KharamlyActivity.MapRouteOverlay>();
		ArrayList<MapRouteOverlay> route3Overlays = new ArrayList<KharamlyActivity.MapRouteOverlay>();
		MapRouteOverlay route1markerOverlay;
		Point route1marker;
		MapRouteOverlay route2markerOverlay;
		Point route2marker;
		MapRouteOverlay route3markerOverlay;
		Point route3marker;
		public MyCustomizedLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
			this.mapView = mapView;
			this.mapController = mapView.getController();
		}

		@Override
		public void onLocationChanged(Location location) {

			float speed = location.getSpeed();
			mapController.setZoom(speed <= 5 ? 21 : speed >= 28 ? 15
					: (int) ((speed * -6 + 513) / 23));
//			/**
//			 * Remove any old route info
//			 */
//			for (MapRouteOverlay o : KharamlyActivity.this.routeOverlay) {
//				mapView.getOverlays().remove(o);
//			}
//			routeOverlay.clear();

			// Let's pretend for now we'll update the routes everytime the
			// location is changed
			// this might drain the battery, but we'll see!

			// Source: http://www.codeproject.com/KB/android/jsonandroidphp.aspx
			if(flag){
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(Cons.API_URL + location.getLatitude()
						+ "," + location.getLongitude() + "/" + destination + "/"
						+ location.getSpeed() + "/"
						+ Installation.id(KharamlyActivity.this));
				String x = Cons.API_URL + location.getLatitude()
				+ "," + location.getLongitude() + "/" + destination + "/"
				+ location.getSpeed() + "/"
				+ Installation.id(KharamlyActivity.this);
				Log.e("test", x);
				try {
					ArrayList<HashMap<String, Integer>> mylist = new ArrayList<HashMap<String, Integer>>();
					HttpResponse httpresponse = httpclient.execute(httpget);
					String responseBody = convertStreamToString(httpresponse
							.getEntity().getContent());
					Log.e("test", responseBody);
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
					JSONArray jArray = json.getJSONArray("routes");
					List<Overlay> overlays = mapView.getOverlays();
					Log.e("R number", jArray.length()+"");
					for (int i = 0; i < jArray.length(); i++) {
						JSONObject route = jArray.getJSONObject(i);
						JSONArray steps = route.getJSONArray("steps");
						Log.e("S number", ""+steps.length());
						for(int j = 0 ; j <steps.length();j++){
							JSONObject step = steps.getJSONObject(j);
							String polyline = step.getString("polyline");
							
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
							int color = step.getInt("col");
							int marker = step.getInt("marker");
							if (i ==0 ){
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color, marker==1, 255, i );
								route1Overlays.add(mro);
								overlays.add(mro);
							}
							else if (i ==1 ){
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color, marker==1, 50, i );
								route2Overlays.add(mro);
								overlays.add(mro);
							}
							else if (i ==2 ){
								MapRouteOverlay mro = new MapRouteOverlay(geopoints, color, marker==1, 50, i );
								route3Overlays.add(mro);
								overlays.add(mro);
							}

						}
					
					

					
				}

				GeoPoint loc = new GeoPoint(
						(int) (location.getLatitude() * 1000000),
						(int) (location.getLongitude() * 1000000));
				MapRouteOverlay marker = new MapRouteOverlay(loc);
				markerOverlay = marker;
				overlays.add(marker);
				mapView.invalidate();
				mapController.animateTo(loc);

			} catch (Exception e) {
				e.printStackTrace();
			}
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapview) {

			if (event.getAction() == 1) {
				List<Overlay> overlays = mapView.getOverlays();
				overlays.remove(markerOverlay);
			}
			return false;
		}

	}

	public class MapRouteOverlay extends Overlay {
		private GeoPoint gp1;
		private int color;
		private boolean marker;
		private boolean route;
		private Bitmap bmp;
		private int alpha = 180;
		private int routeNo;
		private ArrayList<GeoPoint> pointList;

		public MapRouteOverlay(GeoPoint gp1) {
			this.marker = true;
			this.gp1 = gp1;
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.car);
		}

		public MapRouteOverlay(ArrayList<GeoPoint> pointList, int color, boolean marker, int alpha, int routeNo) {
			this.pointList = pointList;
			this.color = color;
			this.route = marker;
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
				pathPaint.setColor(Color.GREEN);
				pathPaint.setAlpha(alpha);
				canvas.drawPath(path, pathPaint);
			}

		}
	}

}
