package com.kharamly;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PathOverlay extends Overlay {

	private GeoPoint gp1;
	private GeoPoint gp2;
	private int color;
	public PathOverlay(GeoPoint gp1, GeoPoint gp2,int color) {
		this.color=color;
		this.gp1 = gp1;
		this.gp2 = gp2;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		Projection projection = mapView.getProjection();
		if (shadow == false) {

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Point point = new Point();
			projection.toPixels(gp1, point);
			
			paint.setColor(color);
			Point point2 = new Point();
			projection.toPixels(gp2, point2);
			paint.setStrokeWidth(2);
			canvas.drawLine((float) point.x, (float) point.y, (float) point2.x,
					(float) point2.y, paint);
		}
		return super.draw(canvas, mapView, shadow, when);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
	}

	protected static void drawPath(ArrayList<Step> steps, MapView mv) {
		for (int i = 0; i < steps.size(); i++) {
			int color;
			   if (steps.get(i).speed == -1)
			        color=Color.BLUE;
			   else if (steps.get(i).speed <= 10)
				   color= 0xffff0000;
			   else if (steps.get(i).speed <= 15)
			        color= 0xffff8000;
			   else if (steps.get(i).speed <= 20)
			        color=0xffffff00;
			    else
			        color= 0xff00ff00;
			   
			mv.getOverlays().add(
					new PathOverlay(steps.get(i).source,steps.get(i).destination,color));
			mv.invalidate();
		}
	}
}
