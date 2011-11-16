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
	static String respStr;
	public static ArrayList<Step> getDirectionData(String srcPlace,
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
		ArrayList<Step> steps = new ArrayList<Step>();
		try {

			DefaultHttpClient CLIENT = new DefaultHttpClient();

			HttpResponse resp = CLIENT.execute(new HttpGet(urlString));
			respStr  = EntityUtils.toString(resp.getEntity());
		
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
			 */JSONArray JSONsteps = leg.getJSONArray("steps");
			// txtMsg.append("\n" + steps.length() + "\n\n");
			for (int i = 0; i < JSONsteps.length(); i++) {
				JSONObject currentStep = JSONsteps.getJSONObject(i);
				/*
				 * txtMsg.append("\n" +
				 * currentStep.getJSONObject("start_location") .getString("lng")
				 * + ", " + currentStep.getJSONObject("start_location")
				 * .getString("lat") + "\n");
				 */
				double[] start = {
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lat")) };
				
				double[] end = {
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lat")) };
				
				Step cS= new Step();
				cS.source=new GeoPoint((int) (start[1] * 1E6),
						(int) (start[0] * 1E6));
				cS.destination=new GeoPoint((int) (end[1] * 1E6),
						(int) (end[0] * 1E6));
				cS.duration=Double.parseDouble(currentStep.getJSONObject(
				"duration").getString("value"));
				cS.distance=Double.parseDouble(currentStep.getJSONObject(
				"distance").getString("value"));
				steps.add(cS);
		
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
		return steps;
	}
	public static ArrayList<Step> getDirectionData2(String srcPlace,
			String destPlace,Context c, TextView txtMsg) {

	
		String urlString = "http://maps.googleapis.com/maps/api/directions/json?origin="
				+ srcPlace + "&destination=" + destPlace + "&sensor=false";
		Log.d("URL", urlString);
		ArrayList<Step> steps = new ArrayList<Step>();
		try {

			DefaultHttpClient CLIENT = new DefaultHttpClient();

			HttpResponse resp = CLIENT.execute(new HttpGet(urlString));
			respStr  = EntityUtils.toString(resp.getEntity());
		
			steps=getSteps2(c,txtMsg);
		
			
			

		} catch (Exception e) {

		Toast.makeText(c, "exception" + e.toString(),
		 Toast.LENGTH_SHORT).show();
		 txtMsg.append(e.toString() + "\n");
		}

		return steps;
	}
	public static ArrayList<Step>  getSteps(Context c, TextView txtMsg){
		ArrayList<Step> steps = new ArrayList<Step>();
		try {

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
			 */JSONArray JSONsteps = leg.getJSONArray("steps");
			// txtMsg.append("\n" + steps.length() + "\n\n");
			for (int i = 0; i < JSONsteps.length(); i++) {
				JSONObject currentStep = JSONsteps.getJSONObject(i);
				/*
				 * txtMsg.append("\n" +
				 * currentStep.getJSONObject("start_location") .getString("lng")
				 * + ", " + currentStep.getJSONObject("start_location")
				 * .getString("lat") + "\n");
				 */
				double[] start = {
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lat")) };
				
				double[] end = {
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lat")) };
				
				Step cS= new Step();
				cS.source=new GeoPoint((int) (start[1] * 1E6),
						(int) (start[0] * 1E6));
				cS.destination=new GeoPoint((int) (end[1] * 1E6),
						(int) (end[0] * 1E6));
				cS.duration=Double.parseDouble(currentStep.getJSONObject(
				"duration").getString("text"));
				cS.distance=Double.parseDouble(currentStep.getJSONObject(
				"distance").getString("text"));
				steps.add(cS);
				
				 ArrayList<GeoPoint> polylines=decodePoly(currentStep.getString("polyline"));
				 ArrayList<Step> polySteps=new ArrayList<Step>();
				 for(int j=0;j<polylines.size()-1;j++){
						Step c1= new Step();
						c1.source=polylines.get(j);
						c1.destination=polylines.get(j+1);
						
						c1.distance=10;
						polySteps.add(c1);
				 }
				 for(int j=0;j<polySteps.size()+1;j++){
					 if(j==0)
					 {
					Step c1= new Step();
					c1.source=cS.destination;
					c1.destination=polySteps.get(0).source;
					
					c1.distance=10;
					steps.add(c1);
					 }
					 else if (j<polySteps.size()){
						 steps.add(polySteps.get(j-1));
					 }
					 else if(i<JSONsteps.length()-1)
					 {
							JSONObject finalPolyStep = JSONsteps.getJSONObject(i+1);
							/*
							 * txtMsg.append("\n" +
							 * currentStep.getJSONObject("start_location") .getString("lng")
							 * + ", " + currentStep.getJSONObject("start_location")
							 * .getString("lat") + "\n");
							 */
							double[] endPoint = {
									Double.parseDouble(finalPolyStep.getJSONObject(
											"start_location").getString("lng")),
									Double.parseDouble(finalPolyStep.getJSONObject(
											"start_location").getString("lat")) };
							
							Step c1= new Step();
							c1.source=polySteps.get(j-1).destination;
							c1.destination=new GeoPoint((int) (endPoint[1] * 1E6),
									(int) (endPoint[0] * 1E6));
							
							c1.distance=10;
							steps.add(c1);
					 }
					
				 }
				/* for (int j = 0; j < polylines.length(); j++) { double[]
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
		return steps;
	}
	public static ArrayList<Step>  getSteps2(Context c,TextView txtMsg){
		ArrayList<Step> steps = new ArrayList<Step>();
		try {
			//int j=0;
			// txtMsg.append( j+++ "\n");
			JSONObject jsonObject = new JSONObject(respStr);
			// txtMsg.append( j+++ "\n");

			// routesArray contains ALL routes
			JSONArray routesArray = jsonObject.getJSONArray("routes");
			// Grab the first route

			JSONObject route = routesArray.getJSONObject(0);
			// Take all legs from the route
			JSONArray legs = route.getJSONArray("legs");
			// Grab first leg
			JSONObject leg = legs.getJSONObject(0);
		//	 txtMsg.append( j+++ "\n");

			
			 JSONArray JSONsteps = leg.getJSONArray("steps");
			 //txtMsg.append( j+++ "\n");

			for (int i = 0; i < JSONsteps.length(); i++) {
				JSONObject currentStep = JSONsteps.getJSONObject(i);

				double[] start = {
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"start_location").getString("lat")) };
				
				double[] end = {
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lng")),
						Double.parseDouble(currentStep.getJSONObject(
								"end_location").getString("lat")) };
				
				Step cS= new Step();
				cS.source=new GeoPoint((int) (start[1] * 1E6),
						(int) (start[0] * 1E6));
				cS.destination=new GeoPoint((int) (end[1] * 1E6),
						(int) (end[0] * 1E6));
				cS.duration=Double.parseDouble(currentStep.getJSONObject(
				"duration").getString("text"));
				cS.distance=Double.parseDouble(currentStep.getJSONObject(
				"distance").getString("text"));
				steps.add(cS);
			//	 txtMsg.append( j+ "\n");

				/*
				 ArrayList<GeoPoint> polylines=decodePoly(currentStep.getString("polyline"));
				 ArrayList<Step> polySteps=new ArrayList<Step>();
				 for(int j=0;j<polylines.size()-1;j++){
						Step c= new Step();
						c.source=polylines.get(j);
						c.destination=polylines.get(j+1);
						
						c.distance=10;
						polySteps.add(c);
				 }
				 for(int j=0;j<polySteps.size()+1;j++){
					 if(j==0)
					 {
					Step c= new Step();
					c.source=cS.destination;
					c.destination=polySteps.get(0).source;
					
					c.distance=10;
					steps.add(c);
					 }
					 else if (j<polySteps.size()){
						 steps.add(polySteps.get(j-1));
					 }
					 else if(i<JSONsteps.length()-1)
					 {
							JSONObject finalPolyStep = JSONsteps.getJSONObject(i+1);
						
							double[] endPoint = {
									Double.parseDouble(finalPolyStep.getJSONObject(
											"start_location").getString("lng")),
									Double.parseDouble(finalPolyStep.getJSONObject(
											"start_location").getString("lat")) };
							
							Step c= new Step();
							c.source=polySteps.get(j-1).destination;
							c.destination=new GeoPoint((int) (endPoint[1] * 1E6),
									(int) (endPoint[0] * 1E6));
							
							c.distance=10;
							steps.add(c);
					 }
					
				 }
				*/
			}


		} catch (Exception e) {

		Toast.makeText(c, "exception" + e.toString(),
		 Toast.LENGTH_SHORT).show();
		 txtMsg.append("step"+e.toString() + "\n");
		}

		
		return steps;
	}

	public static ArrayList<GeoPoint> decodePoly(String encoded) {
if(encoded.length()==0)
	return new ArrayList<GeoPoint>();
	    ArrayList<GeoPoint> poly = new ArrayList<GeoPoint>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
	             (int) (((double) lng / 1E5) * 1E6));
	        poly.add(p);
	    }

	    return poly;
	}
}
