package com.mobileproto.lab5;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.test.AndroidTestRunner;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_USERNAME = "username";
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        username = pref.getString(PREF_USERNAME, null);

        if (username == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Username");
            alert.setMessage("Please enter a username.");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", null);
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            final AlertDialog dialog = alert.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean wantToCloseDialog = false;
                    Editable value = input.getText();
                    if (!value.toString().matches("")) {
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(PREF_USERNAME, value.toString())
                        .commit();
                        wantToCloseDialog = true;
                    }
                    if(wantToCloseDialog)
                        dialog.dismiss();
                }
            });
        }

        // Define view fragments
        FeedFragment feedFragment = new FeedFragment();
        ConnectionFragment connectionFragment = new ConnectionFragment();
        SearchFragment searchFragment = new SearchFragment();

        /*
         *  The following code is used to set up the tabs used for navigation.
         *  You shouldn't need to touch the following code.
         */
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        ActionBar.Tab feedTab = actionBar.newTab().setText(R.string.tab1);
        feedTab.setTabListener(new NavTabListener(feedFragment));

        ActionBar.Tab connectionTab = actionBar.newTab().setText(R.string.tab2);
        connectionTab.setTabListener(new NavTabListener(connectionFragment));

        ActionBar.Tab searchTab = actionBar.newTab().setText(R.string.tab3);
        searchTab.setTabListener(new NavTabListener(searchFragment));

        actionBar.addTab(feedTab);
        actionBar.addTab(connectionTab);
        actionBar.addTab(searchTab);

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.android_dark_blue)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tweet:

                AlertDialog.Builder tweet_alert = new AlertDialog.Builder(this);

                tweet_alert.setTitle("Compose Tweet");
                tweet_alert.setMessage("What's on your mind?");

                // Set an EditText view to get user input
                final EditText tweet_input = new EditText(this);
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(140);
                tweet_input.setFilters(FilterArray);
                tweet_alert.setView(tweet_input);
                tweet_alert.setPositiveButton("Ok", null);
                tweet_alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                final AlertDialog tweet_dialog = tweet_alert.create();
                tweet_dialog.show();
                tweet_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Thread postTweet = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                HttpClient client = new DefaultHttpClient();
                                HttpResponse response;
                                Boolean result = false;
                                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                                Editable value = tweet_input.getText();
                                String website = "http://twitterproto.herokuapp.com/"+username+"/tweets";
                                HttpPost post_tweet = new HttpPost(website);
                                JSONObject json = new JSONObject();
                                try {
                                json.put("status",value.toString());
                                StringEntity se = new StringEntity(json.toString());
                                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                                post_tweet.setEntity(se);
                                result = true;
                                response = client.execute(post_tweet); // fails here
                                tweet_dialog.dismiss();
                                }catch(Exception e){e.printStackTrace();}

                            }
                        });
                        postTweet.start();
                       /* new AsyncTask<Void, Void, Void>(){
                            HttpClient client = new DefaultHttpClient();
                            HttpResponse response;
                            Boolean result = false;
                            @Override
                            protected void onPreExecute() {
                                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
                           }
                            protected Void doInBackground(Void... voids){
                                try{
                                Editable value = tweet_input.getText();

                                String website = "http://twitterproto.herokuapp.com/"+username+"/tweets";
                                HttpPost post_tweet = new HttpPost(website);
                                JSONObject json = new JSONObject();
                                json.put("status",value.toString());
                                StringEntity se = new StringEntity(json.toString());
                                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
                                post_tweet.setEntity(se);
                                    result = true;
                                response = client.execute(post_tweet); // fails here

                                } catch (Exception e){e.printStackTrace();Log.v("Something Went wRong","somethingWentWrong");}
                                tweet_dialog.dismiss();

                               return null;
                           }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                if (result)
                                Toast.makeText(getApplicationContext(),"You just tweeted",Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(),"You just failed",Toast.LENGTH_LONG).show();
                            }
                        }.execute();*/
                    }
                });


                break;

            case R.id.action_username:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Username");
                alert.setMessage("Please enter a username.");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setPositiveButton("Ok", null);
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                final AlertDialog dialog = alert.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Boolean wantToCloseDialog = false;
                        Editable value = input.getText();
                        if (!value.toString().matches("")) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putString(PREF_USERNAME, value.toString())
                                    .commit();
                            wantToCloseDialog = true;
                        }
                        if(wantToCloseDialog)
                            dialog.dismiss();
                    }
                });
                break;

            default:
                break;
        }

        return true;
    }
}
