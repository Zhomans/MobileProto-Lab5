package com.mobileproto.lab5;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chris on 10/2/13.
 */
public class ProfileActivity extends Activity {
    DBHandler db;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_USERNAME = "username";
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        final String primeUser = pref.getString(PREF_USERNAME, null);

        Intent intent = getIntent();

        final String user = intent.getStringExtra("user");

        AutoResizeTextView userText= (AutoResizeTextView) findViewById(R.id.profile_name);
        userText.setText(user);

        db = new DBHandler(this);
        db.open();

        ArrayList<FeedItem> userFeeds = db.getUserFeeds(user);

        FeedListAdapter feedListAdapter = new FeedListAdapter(this, userFeeds);
        ListView feedList = (ListView) this.findViewById(R.id.feedList);
        feedList.setAdapter(feedListAdapter);

        Button follow = (Button) this.findViewById(R.id.follow_button);
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;

                    @Override
                    protected void onPreExecute() {
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    }

                    protected Void doInBackground(Void... voids) {
                        ArrayList<FeedItem> feeds = new ArrayList<FeedItem>();

                        try {
                            String website = "http://twitterproto.herokuapp.com/"+ primeUser + "/follow";
                            HttpPost all_tweets = new HttpPost(website);
                            //all_tweets.setHeader("Content-type","application/json");

                            JSONObject json = new JSONObject();
                            json.put("username",user);
                            StringEntity se = new StringEntity(json.toString());
                            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                            all_tweets.setEntity(se);

                            response = client.execute(all_tweets);
                        }
                        catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");}
                        return null;
                    }

                    protected void onPostExecute(ArrayList<FeedItem> all_feeds){
                        Toast.makeText(getApplicationContext(),"You are now following " + user,Toast.LENGTH_LONG);
                    }
                }.execute();
            }
         });
    }
}
