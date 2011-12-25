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
	/** visual items */
	private ImageView icon, up, down, flag;
	private TextView commentView, sourceView;
	
	/** private vars for using */
	private int commentId;
	
	private boolean showIcons, loadingItem;
	private String text, source;
	
	private Context context;
	
	public CommentItem(Context context) {
		this(context, 0);
	}
	
	
	public CommentItem(Context context, int commentId) {
        super(context);
		this.context = context;
		this.commentId = commentId;
		inflate();
		
		// using this constructor requires invoking init manually with the data
	}
	
	public CommentItem(Context context, AttributeSet attr) {
		super(context, attr);
		this.context = context;
		inflate();
		
		// init with data from the styled attrs
		// note that this constructor is used by the XML inflator
		// but since we're not going to use it, much...
		// we'll keep it anywya ;teehee
		final TypedArray a = context.obtainStyledAttributes(attr, R.styleable.CommentItem);
		
		init(a.getString(R.styleable.CommentItem_text), a.getString(R.styleable.CommentItem_source),
			a.getBoolean(R.styleable.CommentItem_show_icons, true), a.getBoolean(R.styleable.CommentItem_loading, false));
	}
	
	// inflate and set our instance vars
	public void inflate() {
        setOrientation(HORIZONTAL);
		setLayoutParams(new LinearLayout.LayoutParams(250, LayoutParams.WRAP_CONTENT));
		
        final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comment_item, this);

		this.commentView = (TextView) findViewById(R.id.comment);		
		this.sourceView = (TextView) findViewById(R.id.source);
		this.icon = (ImageView) findViewById(R.id.icon);
		this.up = (ImageView) findViewById(R.id.up);
		this.down = (ImageView) findViewById(R.id.down);
		this.flag = (ImageView) findViewById(R.id.flag);
	
	}
	
	// initialize and handle business logic
	public void init(String text, String source, boolean showIcons, boolean loading) {	
		this.commentView.setText(text);
		this.sourceView.setText(source);
		this.showIcons = showIcons;
		this.loadingItem = loading;
		
		/*
		If we're showing the icons and we're not a loading item (loading overrides show icons)
		Add listeners to the icons
		*/
		if (showIcons && ! loadingItem) {
			this.up.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					CommentItem.this.setLoading(CommentItem.this.up);
					CommentItem.this.onClick(1);
				}
			});
		
			this.down.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					CommentItem.this.setLoading(CommentItem.this.down);
					CommentItem.this.onClick(2);
				}
			});
		
			this.flag.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					CommentItem.this.setLoading(CommentItem.this.flag);
					CommentItem.this.onClick(3);
				}
			});
		} else {
			/*
			hide if we specified we're loading OR we set show_icons = false
			*/
			this.up.setVisibility(GONE);
			this.down.setVisibility(GONE);
			this.flag.setVisibility(GONE);
			
			/* loading text for when we're still loading ;) */
			if (loadingItem) {
				this.commentView.setText("Loading...");
			}
		}
	}
	
	/* sets the imageicon for this image to loading
	Used when we do click an image to show progress of update */
	public void setLoading(ImageView image) {
		image.setImageResource(R.drawable.loading);
	}
	
	public void onClick(final int rate) {
		new RequestTask(Cons.SERVER_URL + "rate_comment/" + Installation.id(this.context) + "/" + commentId + "/" + rate) {
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				// // restore the three images
				// // we could switch, but we can live without the overhead
				// CommentItem.this.up.setImageResource(R.drawable.up);
				// CommentItem.this.down.setImageResource(R.drawable.down);
				// CommentItem.this.flag.setImageResource(R.drawable.flag);
				
				// instead, reload comments to show them in order ;)
				((KharamlyActivity) CommentItem.this.context).loadComments(null);
			}
		};
	}
}