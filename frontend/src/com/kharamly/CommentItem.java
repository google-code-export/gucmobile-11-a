package com.kharamly;
/**
		Comment layout example (drawn manually ;tehe)
		 ---------------------------
 		| --- | --------------- | - |
 		||icn|||  comment      |||1||
 		| --- ||               || - |
 		|     | --------------- ||2||
 		|     | --------------- | - |
 		|     ||  source       |||3||
 		|     | --------------- | - |
		 ---------------------------		

	CommentItem is a custom component that holds a single comment row
	A comment row includes:
		- An icon /icn
		- up, down, flag icons for voting on comments /1, /2, /3 respectively
		- comment and source textview /comment, /source
		
	This will be accomplished as follows:
	- CommentItem is a horizontal linearlayout, includes three items:
	--- an icon /icn
	--- a vertical linearlayout including 2 items
	----- a comment textview /comment
	----- a source textview /source
	--- a vertical linearlayout including 3 items
	----- an up icon /1
	----- a down icon /2
	----- a flag icon /3
	
	@author	kamasheto
*/

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;		
	
public class CommentItem extends LinearLayout {
	
	private ImageView icon, up, down, flag;
	
	private TextView comment, source;
	
	public CommentItem(Context context, AttributeSet attr) {
        super(context, attr);
        setOrientation(HORIZONTAL);
		setLayoutParams(new LinearLayout.LayoutParams(250, LayoutParams.WRAP_CONTENT));
		
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_item, this);
		

        final TypedArray a = context.obtainStyledAttributes(attr, R.styleable.CommentItem);

		this.comment = (TextView) findViewById(R.id.comment);
        this.comment.setText(a.getString(R.styleable.CommentItem_text));
		
		
		this.source = (TextView) findViewById(R.id.source);
		this.source.setText(a.getString(R.styleable.CommentItem_source));
		
		this.icon = (ImageView) findViewById(R.id.icon);
		this.up = (ImageView) findViewById(R.id.up);
		this.down = (ImageView) findViewById(R.id.down);
		this.flag = (ImageView) findViewById(R.id.flag);
	}
}