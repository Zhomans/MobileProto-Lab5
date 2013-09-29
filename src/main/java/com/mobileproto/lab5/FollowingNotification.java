package com.mobileproto.lab5;

/**
 * Created by chris on 9/27/13.
 */
public class FollowingNotification extends FeedNotification {

    String userFrom;
    String userTo;

    public FollowingNotification(String userFrom, String userTo){
        super(userFrom, userTo, "You are now following" + userTo + "!", "follow");
        this.userFrom = "You are following" + userTo;
        this.userTo = userTo;
    }
}
