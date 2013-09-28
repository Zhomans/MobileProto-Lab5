package com.mobileproto.lab5;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        String username = pref.getString(PREF_USERNAME, null);

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


}
