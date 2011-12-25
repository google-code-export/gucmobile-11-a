package com.kharamly;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class LocalReceiver extends BroadcastReceiver {
	MapController mc;
	MapView mapView;
	boolean flag = true;
	Context context;
	ArrayList<Step> steps;
	int currentIndex;
	double threshold = 0.0002;
	boolean start = false;
	long timerStart;
	TextView txtMsgq;
	boolean textViewAvailable;
	boolean destinationReached;
	public LocalReceiver(MapView mv, MapController mc, Context c,
			ArrayList<Step> steps, TextView txV) {
		mapView = mv;
		this.mc = mc;
		context = c;
		this.steps = steps;
		currentIndex = 0;
		txtMsgq = txV;
		if(txV==null)
			textViewAvailable=false;
		else textViewAvailable=true;
		destinationReached=false;
		
	}

	@Override
	public void onReceive(Context localContext, Intent callerIntent) {
		double latitude = callerIntent.getDoubleExtra("latitude", -1);
		double longitude = callerIntent.getDoubleExtra("longitude", -1);
		Log.e("MAIN>>>", Double.toString(latitude));
		Log.e("MAIN>>>", Double.toString(longitude));
		try {
			String msg = " lat: " + Double.toString(latitude) + " " + " lon: "
					+ Double.toString(longitude);
			
			appendToTextView("current location:" + msg + "\n");
			// testing the SMS-texting feature

			mc = mapView.getController();
			double coordinates[] = { latitude, longitude };
			double lat = coordinates[0];
			double lng = coordinates[1];

			GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			navigateToPosition(p);
			updateRoute(p);
			// texting(msg, p);
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG);
		}
	}

	public void updateRoute(GeoPoint p) {
		
		if (currentIndex == steps.size() && currentIndex != 0&&!destinationReached) {
			appendToTextView("destination reached");
			destinationReached=true;
			Toast.makeText(context, "destination reached", Toast.LENGTH_LONG);
			return;
		}
		if (!start) {

			if (getDistance(steps.get(currentIndex).source, p) < threshold) {
				start = true;
				timerStart = System.currentTimeMillis();
				steps.get(currentIndex).startTime = timerStart;
				// Toast.makeText(context, "start",
				// Toast.LENGTH_SHORT).show();
				appendToTextView("start");
				appendToTextView("\nnext node"
						+ steps.get(currentIndex).destination.getLatitudeE6()
						/ 1E6 + ", "
						+ steps.get(currentIndex).destination.getLongitudeE6()
						/ 1E6);

			} else {
				appendToTextView("start node"
						+ steps.get(currentIndex).source.getLatitudeE6() / 1E6
						+ ", "
						+ steps.get(currentIndex).source.getLongitudeE6() / 1E6
						+ "\n");

				return;
			}
			;
		}

		else if (getDistance(steps.get(currentIndex).destination, p) < threshold) {
			steps.get(currentIndex).duration = (System.currentTimeMillis() - timerStart) / 1000;
			appendToTextView("duration: (" + currentIndex + ","
					+ (currentIndex + 1) + ")-->"
					+ steps.get(currentIndex).duration);
			timerStart = System.currentTimeMillis();
			updateServer(steps.get(currentIndex));

			steps.get(currentIndex++).calculateSpeed();
			steps.get(currentIndex).startTime = timerStart;
			appendToTextView("\n" + currentIndex + " next node"
					+ steps.get(currentIndex).destination.getLatitudeE6() / 1E6
					+ ", "
					+ steps.get(currentIndex).destination.getLongitudeE6()
					/ 1E6 + "\n");

		} else
			appendToTextView("\n"
					+ getDistance(steps.get(currentIndex).destination, p)
					+ "\n");
	}

	public double getDistance(GeoPoint a, GeoPoint b) {
		double lat = a.getLatitudeE6() / 1E6 - b.getLatitudeE6() / 1E6;
		double lng = a.getLongitudeE6() / 1E6 - b.getLongitudeE6() / 1E6;
		return Math.sqrt(lat * lat + lng * lng);
	}

	public void resetPath(ArrayList<Step> s) {
		steps = s;
		currentIndex = 0;
		destinationReached=false;
	}

	public void navigateToPosition(GeoPoint p) {

		mc.animateTo(p);
		mc.setZoom(16);
		mapView.invalidate();
	}

/*	private void texting(String msg, GeoPoint p) {
		try {
			SmsManager smsMgr = SmsManager.getDefault();
			// Parameter of sendTextMessage are:
			// destinationAddress, senderAddress,
			// text, sentIntent, deliveryIntent)
			// ----------------------------------------------------------
			if (flag) {

				Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
				try {
					List<Address> addresses = geoCoder
							.getFromLocation(p.getLatitudeE6() / 1E6, p
									.getLongitudeE6() / 1E6, 1);

					String add = "";
					if (addresses.size() > 0) {
						for (int i = 0; i < addresses.get(0)
								.getMaxAddressLineIndex(); i++)
							add += addresses.get(0).getAddressLine(i) + "\n";
					}
					// txtMsg.append("\n" + add);
					smsMgr.sendTextMessage("+20124350608", null, "I am at "
							+ add, null, null);

				} catch (Exception e) {
					e.printStackTrace();
				}

				flag = false;
			}

		} catch (Exception e) {
		}
	}// texting
*/
	private void appendToTextView(String text){
		if(textViewAvailable)
			txtMsgq.append(text);	
	}
	
	private void updateServer(Step s) {
		
	
	}
}
