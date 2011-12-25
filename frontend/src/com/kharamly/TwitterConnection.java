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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.SharedPreferences;
public class TwitterConnection {
	public static final String PREFS_NAME = "MyPrefsFile";	
	Twitter twitter;
	RequestToken requestToken;
	public final static String consumerKey = "q6nuqNLZYBNLU9iefmCPQ"; // "your key here";
	public final static String consumerSecret = "08LnWAUljEoQL8k8YIAQ9FUYBeU1BbAH8aIKGBUKxS0"; // "your secret key here";
	private final String CALLBACKURL = "kharamly://KharamlyActivity";
	Context c = null;
	TextView txtMsg;
	String token, secret;
	SharedPreferences settings;
	boolean flag=false;
	//if the user is signing in for the first time, the 
	//login page is displayed and the token and the secret are sent to the database
	//if the token and the secret are available for the current user, then no need
	//to display the login page again,= the token and the secret should be retrieved
	//from the database directly
	public TwitterConnection(Context con, TextView t) {
		c = con;
		txtMsg = t;
		 // Restore preferences
	       settings = con.getSharedPreferences(PREFS_NAME, 0);
	      // boolean silent = settings.getBoolean("silentMode", false);
	       // setSilent(silent);
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
			//re();
			//if(true)
			//	return;
			    
			boolean flag=settings.getString("token", "false")=="false"||settings.getString("secret", "false")=="false";	
			if(!flag){
				AccessToken a = new AccessToken(settings.getString("token", "false"),settings.getString("secret", "false") );
				twitter.setOAuthAccessToken(a); 
				for (Status st : twitter.getMentions()) {
				//	 txtMsg.append("from" +st.getText() + "\n");
				}
			}
			else{
			
			
		    requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
            
		
			String authUrl = requestToken.getAuthenticationURL();
			Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
			in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(in);
			}
		} catch (TwitterException ex) {
			Log.e("in Main.OAuthLogin", ex.getMessage());
			 txtMsg.append(ex.toString());
		}
	}
	void re(){
		String urlString = "http://10.0.2.2:8000/twitter/atabouraya";
	Log.d("URL", urlString);
	try {

		DefaultHttpClient CLIENT = new DefaultHttpClient();

		HttpResponse resp = CLIENT.execute(new HttpGet(urlString));
		String respStr  = EntityUtils.toString(resp.getEntity());
		txtMsg.append(respStr+"\n\n");
		
		JSONObject jsonObject = new JSONObject(respStr);
		String token =  jsonObject.getString("token");
		String secret= jsonObject.getString("secret");
		txtMsg.append(token);
		txtMsg.append(secret);
		AccessToken a = new AccessToken(token
				, secret);
		twitter.setOAuthAccessToken(a); 

		for (Status st : twitter.getMentions()) {
			txtMsg.append("InDevice\n");

			txtMsg.append(st.getText() + "\n");
		}


	}
	catch(Exception e){
		txtMsg.append(e.getMessage());
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

		    SharedPreferences.Editor editor = settings.edit();
		   
			editor.putString("token", token);
			editor.putString("secret", secret);
			editor.commit();
			txtMsg.append(settings.getString("token", "notfoundtoken"));
			txtMsg.append(settings.getString("secret", "notfoundsecret"));

			for (Status st : twitter.getMentions()) {

				//txtMsg.append(st.getText() + "\n");
			}

		} catch (TwitterException ex) {
			Log.e("Main.onNewIntent", "" + ex.getMessage());
			// txtMsg.append(ex.toString());
		}
	}

}
