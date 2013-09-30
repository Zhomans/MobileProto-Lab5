package com.mobileproto.lab5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by chris on 9/17/13.
 */
public class DBHandler {

    private SQLiteDatabase database;
    private DatabaseModel model;
    String user;
    private String[] allColumns = {DatabaseModel.TWEETER, DatabaseModel.TWEETEE, DatabaseModel.STATUS, DatabaseModel.TWEET_TYPE, DatabaseModel.TWEET_DATE};

    public DBHandler(Context context){
        model = new DatabaseModel(context);
    }

    public void open(){
        database = model.getWritableDatabase();
    }

    public void close() {
        database.close();
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getUser(){
        return this.user;
    }

    public long createEntry(String fromUser, String toUser, String status, String type, String date){
        ContentValues content = new ContentValues();
        content.put(DatabaseModel.TWEETER, fromUser);
        content.put(DatabaseModel.TWEETEE, toUser);
        content.put(DatabaseModel.STATUS, status);
        content.put(DatabaseModel.TWEET_TYPE, type);
        content.put(DatabaseModel.TWEET_DATE, date);

        long id = database.insert(DatabaseModel.TABLE_NAME, null, content);
        return id;
    }

    public ArrayList<FeedItem> getallFeeds() {
        ArrayList<FeedItem> allFeeds = new ArrayList<FeedItem>();

        Cursor cursor = database.query(DatabaseModel.TABLE_NAME,
                allColumns, DatabaseModel.TWEET_TYPE + " like '%feed%'", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FeedItem tweet = new FeedItem(cursor.getString(0), cursor.getString(2));
            allFeeds.add(tweet);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return allFeeds;
    }
    public void deleteFeeds(){
        database.delete(DatabaseModel.TABLE_NAME,
                DatabaseModel.TWEET_TYPE + " like '%feed%'",null);
    }
    public void deleteFollowers(){
        database.delete(DatabaseModel.TABLE_NAME,
                DatabaseModel.TWEET_TYPE + " like '%follower%'",null);
    }
    public void deleteFollowing(){
        database.delete(DatabaseModel.TABLE_NAME,
                DatabaseModel.TWEET_TYPE + " like '%following%'",null);
    }

    public ArrayList<FeedItem> getSearch(String value) {
        ArrayList<FeedItem> feeds = new ArrayList<FeedItem>();
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME,
                allColumns, DatabaseModel.STATUS + " like '%"+value+"%'" +
                " AND " + DatabaseModel.TWEET_TYPE + " like '%feed%'"
                ,null, null, null, null);

        cursor.moveToFirst();
        FeedItem tweet;
        while (!cursor.isAfterLast()) {
            tweet = new FeedItem(cursor.getString(0),cursor.getString(2));
            cursor.moveToNext();
            feeds.add(tweet);
        }
        // Make sure to close the cursor
        cursor.close();
        return feeds;
    }
    public ArrayList<MentionNotification> getMentions(String user) {
        ArrayList<MentionNotification> mentions = new ArrayList<MentionNotification>();
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME,
                allColumns, DatabaseModel.TWEET_TYPE + " like '%feed%'" +" AND " + DatabaseModel.TWEETEE + " like '%"+user+"%'" ,null, null, null, null);

        cursor.moveToFirst();
        MentionNotification tweet;
        while (!cursor.isAfterLast()) {
            tweet = new MentionNotification(cursor.getString(0),cursor.getString(1),cursor.getString(2));
            cursor.moveToNext();
            mentions.add(0,tweet);
        }
        // Make sure to close the cursor
        cursor.close();
        return mentions;
    }
    public ArrayList<FollowNotification> getFollowers(String user) {
        ArrayList<FollowNotification> follows = new ArrayList<FollowNotification>();
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME,
                allColumns, DatabaseModel.TWEET_TYPE + " like " + "'%follow%'" +" AND " + DatabaseModel.TWEETEE + " like " + "'%" + user + "%'",null,null,null,null);

        cursor.moveToFirst();
        FollowNotification tweet;
        while (!cursor.isAfterLast()) {
            tweet = new FollowNotification(cursor.getString(0),cursor.getString(1));
            follows.add(0, tweet);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return follows;
    }
    public ArrayList<FollowingNotification> getFollowing(String user) {
        ArrayList<FollowingNotification> follows = new ArrayList<FollowingNotification>();
        Cursor cursor = database.query(DatabaseModel.TABLE_NAME,
                allColumns, DatabaseModel.TWEET_TYPE + " like " + "'%follow%'" +" AND " + DatabaseModel.TWEETER + " like '%" + user + "%'" ,null, null, null, null);

        cursor.moveToFirst();
        FollowingNotification tweet;
        while (!cursor.isAfterLast()) {
            tweet = new FollowingNotification(cursor.getString(0),cursor.getString(1));
            follows.add(0, tweet);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return follows;
    }
}