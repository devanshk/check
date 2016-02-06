package com.example.android.cardreader;

import java.util.Date;

/**
 * Created by dkukreja on 2/5/16.
 */
public class User {
    public String name = "";
    public Date checkinTime = null;
    public enum Status {CheckedIn, Pending}

    public User(String name) {
        this.name = name;
    }
}
