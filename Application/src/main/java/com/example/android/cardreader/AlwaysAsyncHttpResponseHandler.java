package com.example.android.cardreader;

import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * Created by dkukreja on 2/6/16.
 */
public abstract class AlwaysAsyncHttpResponseHandler extends AsyncHttpResponseHandler {
    @Override
    public boolean getUseSynchronousMode() {
        return false;
    }
}