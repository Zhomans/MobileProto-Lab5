package com.mobileproto.lab5;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by evan on 9/26/13.
 */
public class SearchFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.search_fragment, null);
        List<FeedItem> results = new ArrayList<FeedItem>();

        FeedListAdapter feedListAdapter = new FeedListAdapter(getActivity(), results);
        ListView feedList = (ListView) v.findViewById(R.id.searchResults);
        feedList.setAdapter(feedListAdapter);

        Button search = (Button) v.findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) v.findViewById(R.id.searchField);
                final String value = text.getText().toString();

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
                            String website = "http://twitterproto.herokuapp.com/tweets?q="+value;
                            HttpGet all_tweets = new HttpGet(website);
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
                        }
                        finally{
                            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                        try {JSONObject jObject = new JSONObject(result);
                            JSONArray jArray =  jObject.getJSONArray("tweets");
                            for (int i = 0; i < jArray.length(); i++){
                                JSONObject single_feed = jArray.getJSONObject(i);
                                FeedItem feeditem = new FeedItem(single_feed.getString("username"),single_feed.getString("tweet"));
                                feeds.add(feeditem);}
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

        return v;
    }

}
