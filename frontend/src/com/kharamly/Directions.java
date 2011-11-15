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

public class Directions {
	
	public static ArrayList<GeoPoint> getDirectionData(String srcPlace,
			String destPlace,Context c, TextView txtMsg) {

		// String urlString =
		// "http://10.0.2.2:8000/getdirections/El Basaten El Gharbaia, El-basatin, Cairo/Ezbet Fahmy, El-basatin, Cairo/false/false/";
		// String
		// urlString="http://maps.googleapis.com/maps/api/directions/json?origin=Boston,MA&destination=Concord,MA&waypoints=Charlestown,MA&#124;Lexington,MA&sensor=false";
		// Log.d("URL", urlString);

		//String urlString = "http://10.0.2.2:8000/getdirections/"+srcPlace+"/"+destPlace+"/false/false/";

		String urlString = "http://maps.googleapis.com/maps/api/directions/json?origin="
				+ srcPlace + "&destination=" + destPlace + "&sensor=false";
		Log.d("URL", urlString);
		ArrayList<GeoPoint> nodes = new ArrayList<GeoPoint>();
		try {

			DefaultHttpClient CLIENT = new DefaultHttpClient();

			HttpResponse resp = CLIENT.execute(new HttpGet(urlString));
			final String respStr = EntityUtils.toString(resp.getEntity());
		
			JSONObject jsonObject = new JSONObject(respStr);
		
			// routesArray contains ALL routes
			JSONArray routesArray = jsonObject.getJSONArray("routes");
			// Grab the first route

			JSONObject route = routesArray.getJSONObject(0);
			// Take all legs from the route
			JSONArray legs = route.getJSONArray("legs");
			// Grab first leg
			JSONObject leg = legs.getJSONObject(0);
			
			// JSONObject durationObject = leg.getJSONObject("duration");
			// String duration = durationObject.getString("text");
			// JSONArray ar=routeObject.getJSONArray("legs");
			// for(int i=0;i<legs.length();i++){
			// txtMsg.append(legs.getString(i)+"\n");
			// }

			/*
			 * txtMsg.append("\n"+leg.getJSONObject("duration").getString("value"
			 * )+"\n");
			 * txtMsg.append("\n"+leg.getJSONObject("distance").getString
			 * ("value")+"\n");
			 * txtMsg.append("\n"+leg.getJSONObject("end_location"
			 * ).getString("lng")+", "+
			 * leg.getJSONObject("end_location").getString("lat")+"\n");
			 * txtMsg.append("\n"+leg.getString("start_address")+"\n");
			 * txtMsg.append("\n"+leg.getString("end_address")+"\n");
			 * txtMsg.append
			 * ("\n"+leg.getJSONObject("start_location").getString("lng")+", "+
			 * leg.getJSONObject("start_location").getString("lat")+"\n");
			 * //txtMsg
			 * .append("\n"+leg.getJSONObject("distance").getString("value"
			 * )+"\n");
			 */JSONArray steps = leg.getJSONArray("steps");
			// txtMsg.append("\n" + steps.length() + "\n\n");
			for (int i = 0; i < steps.length(); i++) {
				JSONObject currentStep = steps.getJSONObject(i);
				/*
				 * txtMsg.append("\n" +
				 * currentStep.getJSONObject("start_location") .getString("lng")
				 * + ", " + currentStep.getJSONObject("start_location")
				 * .getString("lat") + "\n");
				 */
				double[] step = {
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lat")) };

				nodes.add(new GeoPoint((int) (step[1] * 1E6),
						(int) (step[0] * 1E6)));
				/*
				 * JSONArray polylines = currentStep.getJSONObject("polyline");
				 * for (int j = 0; j < polylines.length(); j++) { double[]
				 * polyline = { Double.parseDouble(polylines.getJSONObject(j)
				 * .getJSONObject("start_location").getString( "lng")),
				 * Double.parseDouble(polylines.getJSONObject(j)
				 * .getJSONObject("start_location").getString( "lat")) };
				 * nodes.add(new GeoPoint((int) (polyline[1] * 1E6), (int)
				 * (polyline[0] * 1E6)));
				 * 
				 * }
				 */
			}
			// txtMsg.append(leg.toString()+"\n");

		} catch (Exception e) {

		Toast.makeText(c, "exception" + e.toString(),
		 Toast.LENGTH_SHORT).show();
		 txtMsg.append(e.toString() + "\n");
		}

		/*
		 * NodeList nl = doc.getElementsByTagName("LineString"); for (int s = 0;
		 * s < nl.getLength(); s++) { Node rootNode = nl.item(s); NodeList
		 * configItems = rootNode.getChildNodes(); for (int x = 0; x <
		 * configItems.getLength(); x++) { Node lineStringNode =
		 * configItems.item(x); NodeList path = lineStringNode.getChildNodes();
		 * pathConent = path.item(0).getNodeValue(); } } String[] tempContent =
		 * pathConent.split(" "); return tempContent;
		 */
		return nodes;
	}

}
