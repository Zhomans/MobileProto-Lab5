package com.mobileproto.lab5;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import java.util.ArrayList;

/**
 * Created by chris on 10/2/13.
 */
public class ProfileActivity extends Activity {
    DBHandler db;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
                new AsyncTask<Void, Void, ArrayList<FeedItem>>() {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    InputStream inputStream = null;
                    String result = "";


                    @Override
                    protected void onPreExecute() {
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    }

                    protected ArrayList<FeedItem> doInBackground(Void... voids) {
                        ArrayList<FeedItem> feeds = new ArrayList<FeedItem>();

                        try {
                            String website = "http://twitterproto.herokuapp.com/"+ db.getUser() + "/follow";
                            HttpPost all_tweets = new HttpPost(website);
                            all_tweets.setHeader("Content-type","application/json");

                            response = client.execute(all_tweets);
                            response.getStatusLine().getStatusCode();
                            HttpEntity entity = response.getEntity();

                            inputStream = entity.getContent();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                            StringBuilder sb = new StringBuilder();

                            String line;
                            String nl = System.getProperty("line.separator");
                            while ((line = reader.readLine())!= null){
                                sb.append(line + nl);
                            }
                            result = sb.toString();
                        }
                        catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");
                            DBHandler db = new DBHandler(getActivity());
                            db.open();
                            feeds = db.getSearch(value);
                        }
                        finally{
                            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                        try {
                            if (!result.equals("")) {
                                JSONObject jObject = new JSONObject(result);
                                JSONArray jArray =  jObject.getJSONArray("tweets");
                                for (int i = 0; i < jArray.length(); i++){
                                    JSONObject single_feed = jArray.getJSONObject(i);
                                    FeedItem feeditem = new FeedItem(single_feed.getString("username"),single_feed.getString("tweet"));
                                    feeds.add(feeditem);}
                            }
                        }catch (JSONException e){e.printStackTrace();}
                        return feeds;
                    }

                    protected void onPostExecute(ArrayList<FeedItem> all_feeds){
                        FeedListAdapter feedListAdapter = new FeedListAdapter(getActivity(), all_feeds);
                        ListView feedList = (ListView) v.findViewById(R.id.searchResults);
                        feedList.setAdapter(feedListAdapter);
                    }
                }.execute();

            }
         });
    }
}
