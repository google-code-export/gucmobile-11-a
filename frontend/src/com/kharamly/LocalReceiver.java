package com.kharamly;


import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.telephony.SmsManager;
import android.util.Log;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class LocalReceiver extends BroadcastReceiver {
	MapController mc;
	MapView mapView;
	boolean flag = true;
	Context context;
	public LocalReceiver(MapView mv,MapController mc,Context c){
		mapView=mv;
		this.mc=mc;
		context=c;
	}
	@Override
	public void onReceive(Context localContext, Intent callerIntent) {
		double latitude = callerIntent.getDoubleExtra("latitude", -1);
		double longitude = callerIntent.getDoubleExtra("longitude", -1);
		Log.e("MAIN>>>", Double.toString(latitude));
		Log.e("MAIN>>>", Double.toString(longitude));
		//String msg = " lat: " + Double.toString(latitude) + " " + " lon: "
		//		+ Double.toString(longitude);

		// testing the SMS-texting feature

		mc = mapView.getController();
		double coordinates[] = { latitude, longitude };
		double lat = coordinates[0];
		double lng = coordinates[1];

		GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

		mc.animateTo(p);
		mc.setZoom(17);
		mapView.invalidate();
		//texting(msg, p);
	}

	private void texting(String msg, GeoPoint p) {
		try {
			SmsManager smsMgr = SmsManager.getDefault();
			// Parameter of sendTextMessage are:
			// destinationAddress, senderAddress,
			// text, sentIntent, deliveryIntent)
			// ----------------------------------------------------------
			if (flag) {

				Geocoder geoCoder = new Geocoder(context, Locale
						.getDefault());
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
					//txtMsg.append("\n" + add);
					smsMgr.sendTextMessage("+20124350608", null, "I am at "
							+ add, null, null);

				} catch (Exception e) {
					// TODO: handle exception
				}

				flag = false;
			}

		} catch (Exception e) {
			}
	}// texting

}

