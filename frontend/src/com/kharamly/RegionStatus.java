package com.kharamly;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public class RegionStatus {
	static String respStr;
	//this method takes longitude and latitude of a certain place and the radius of the targeted region
	//and returns list of steps with their speeds in that region
	public static ArrayList<Step> getRegionStatus(double longitude,
			double latitude,double radius, Context c, TextView txtMsg) {

		String urlString = "http://10.0.2.2:8000/inRadius/" + longitude + "/"
				+ latitude + "/"+radius+"/";

		Log.d("URL", urlString);
		ArrayList<Step> steps = new ArrayList<Step>();
		try {

			DefaultHttpClient CLIENT = new DefaultHttpClient();

			HttpResponse resp = CLIENT.execute(new HttpGet(urlString));
			respStr = EntityUtils.toString(resp.getEntity());
			JSONArray jsonArray = new JSONArray(respStr);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject currentStep = jsonArray.getJSONObject(i);
				
				double[] start = {
						Double.parseDouble(currentStep.getString("start_location_longitude")),
						Double.parseDouble(currentStep.getString("start_location_latitude")) };

				double[] end = {
						Double.parseDouble(currentStep.getString("end_location_longitude")),
						Double.parseDouble(currentStep.getString("end_location_latitude")) };

				Step cS = new Step();
				cS.source = new GeoPoint((int) (start[1] * 1E6),
						(int) (start[0] * 1E6));
				cS.destination = new GeoPoint((int) (end[1] * 1E6),
						(int) (end[0] * 1E6));
				cS.speed = currentStep.getDouble("avg_speed");
				steps.add(cS);

			}

		} catch (Exception e) {

		
		}
		return steps;
	}
}
