package com.kharamly;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BadgeItem extends LinearLayout {

	private TextView name;
	private ImageView badgeIcon;

	public BadgeItem(Context context) {
		super(context);
		setOrientation(HORIZONTAL);

		LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 64);
		this.setPadding(10, 10, 10, 10);
		setLayoutParams(params);

		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.badge_item, this);

		setAttributes("Checkin: 1", R.drawable.checkin_1);
	}

	public void setAttributes(String name, int drawableId) {
		this.name = (TextView) findViewById(R.id.badge_name);
		this.name.setText(name);
		this.badgeIcon = (ImageView) findViewById(R.id.badge_icon);
		this.badgeIcon.setImageResource(drawableId);
	}

}
