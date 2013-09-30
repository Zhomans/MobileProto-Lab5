package com.mobileproto.lab5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chris on 9/17/13.
 */

public class DatabaseModel extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "tweets";
    public static final String TWEETER = "user";
    public static final String TWEETEE = "receiver";
    public static final String STATUS = "status";
    public static final String TWEET_TYPE = "type";
    public static final String TWEET_DATE = "date";

    public static final String DATABASE_NAME = "tweets";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + TWEETER
            + " text not null, " + TWEETEE
            + " text not null, " + STATUS
            + " text not null, " + TWEET_TYPE
            + " text not null, " + TWEET_DATE
            + " text not null);";

    public DatabaseModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseModel.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}