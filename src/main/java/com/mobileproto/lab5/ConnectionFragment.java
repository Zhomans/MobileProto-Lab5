package com.mobileproto.lab5;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionFragment extends Fragment {
    Timer timer;
    DBHandler db;

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_USERNAME = "username";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences pref = getActivity().getSharedPreferences(PREFS_NAME,getActivity().MODE_PRIVATE);
        final String username = pref.getString(PREF_USERNAME, null);

        final View v = inflater.inflate(R.layout.connections_fragment, null);
        List<FeedNotification> notifications = new ArrayList<FeedNotification>();

        ConnectionListAdapter connectionListAdapter = new ConnectionListAdapter(getActivity(), notifications);
        ListView connectionList = (ListView) v.findViewById(R.id.connectionListView);
        connectionList.setAdapter(connectionListAdapter);


        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                new AsyncTask<Void, Void, ArrayList<FeedNotification>>() {
                    HttpClient client = new DefaultHttpClient();
                    HttpResponse response;
                    InputStream inputStream = null;
                    String result = "";

                    @Override
                    protected void onPreExecute() {
                        HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                    }

                    protected ArrayList<FeedNotification> doInBackground(Void... voids) {
                        ArrayList<FeedNotification> notes = new ArrayList<FeedNotification>();
                        //db.open();
                        try {
                            String website = "http://twitterproto.herokuapp.com/tweets?q=" + username; //change to be variable
                            HttpGet all_mentions = new HttpGet(website);
                            all_mentions.setHeader("Content-type", "application/json");

                            response = client.execute(all_mentions);
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
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Server", "Cannot Establish Connection");
                            db = new DBHandler(getActivity());
                            db.open();
                            notes.addAll(db.getMentions(username));
                        }
                        finally{
                            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                        try {
                            if (!result.equals("")) {
                            JSONObject jObject = new JSONObject(result);
                            JSONArray jArray =  jObject.getJSONArray("tweets");
                            db = new DBHandler(getActivity());
                            db.open();
                            db.deleteMentions(username);
                            for (int i = 0; i < jArray.length(); i++){
                                JSONObject single_notification = jArray.getJSONObject(i);
                                MentionNotification mention = new MentionNotification(single_notification.getString("username"), "@" + username,single_notification.getString("tweet"));
                                db.createEntry(single_notification.getString("username"),"@" + username, single_notification.getString("tweet"),"tweet",single_notification.getString("date"));
                                notes.add(mention);}
                            }
                        }catch (JSONException e){e.printStackTrace();}


                        try {
                            String website = "http://twitterproto.herokuapp.com/" + username + "/followers";
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
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Server", "Cannot Establish Connection");
                            db = new DBHandler(getActivity());
                            db.open();
                            notes.addAll(db.getFollowers(username));

                        }
                        finally{
                            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                        try {
                            if (!result.equals("")) {
                            JSONObject jObject = new JSONObject(result);
                            JSONArray jArray =  jObject.getJSONArray("followers");
                            db = new DBHandler(getActivity());
                            db.open();
                            db.deleteFollowers(username);
                            for (int i = 0; i < jArray.length(); i++){
                                FollowNotification follow = new FollowNotification("@" + jArray.getString(i), "@"+username); //change to variable
                                notes.add(follow);}
                            }
                        }catch (JSONException e){e.printStackTrace();}
                        return notes;
                    }

                    protected void onPostExecute(ArrayList<FeedNotification> notifications){
                        ConnectionListAdapter connectionListAdapter = new ConnectionListAdapter(getActivity(), notifications);
                        ListView connectionList = (ListView) v.findViewById(R.id.connectionListView);
                        connectionList.setAdapter(connectionListAdapter);
                    }
                }.execute();
            }
        }, 0, 10000);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }
}
