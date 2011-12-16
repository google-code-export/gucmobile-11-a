package com.kharamly;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterConnection {
	Twitter twitter;
	RequestToken requestToken;
	public final static String consumerKey = "q6nuqNLZYBNLU9iefmCPQ"; // "your key here";
	public final static String consumerSecret = "08LnWAUljEoQL8k8YIAQ9FUYBeU1BbAH8aIKGBUKxS0"; // "your secret key here";
	private final String CALLBACKURL = "T4JOAuth://Main";
	Context c = null;
	TextView txtMsg;
	String token, secret;//if the user is signing in for the first time, the 
	//login page is displayed and the token and the secret are sent to the database
	//if the token and the secret are available for the current user, then no need
	//to display the login page again, the token and the secret should be retrieved
	//from the database directly
	
	public TwitterConnection(Context con, TextView t) {
		c = con;
		txtMsg = t;
	}

	/*
	 * - Creates object of Twitter and sets consumerKey and consumerSecret -
	 * Prepares the URL accordingly and opens the WebView for the user to
	 * provide sign-in details - When user finishes signing-in, WebView opens
	 * your activity back
	 */
	void OAuthLogin() {
		try {
			twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(consumerKey, consumerSecret);

			requestToken = twitter.getOAuthRequestToken(CALLBACKURL);

			String authUrl = requestToken.getAuthenticationURL();
			Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(in);
		} catch (TwitterException ex) {
			Log.e("in Main.OAuthLogin", ex.getMessage());
			// txtMsg.append(ex.toString());
		}
	}

	void retrieveData(Intent intent) {
		Uri uri = intent.getData();
		try {
			String verifier = uri.getQueryParameter("oauth_verifier");
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
					verifier);
			String token = accessToken.getToken(), secret = accessToken
					.getTokenSecret();
			for (Status st : twitter.getMentions()) {
				// txtMsg.append(st.getText() + "\n");
			}

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
			// txtMsg.append(ex.toString());
		}
	}

}
