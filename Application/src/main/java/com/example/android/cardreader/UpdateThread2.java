package com.example.android.cardreader;

import android.os.AsyncTask;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dkukreja on 11/14/15.
 */
public class UpdateThread2 extends AsyncTask<Void,Integer,Void> {
    private final String TAG = "Async_MultiThread";

    public UpdateThread2(){
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (true) {
                System.out.println("Sending Get Request.");
                SyncHttpClient client = new SyncHttpClient();

                client.get(Globals.start + "/event/get-checked-in/" + 1, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                        String response = new String(bytes);
                        System.out.println("Response=" + response);
                        parseCheckedIn(response);
                    }

                    @Override
                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                        System.out.println("Failed with " + throwable.toString());
                    }
                });
                Thread.sleep(2000);
            }
        } catch(Exception e){e.printStackTrace();}
        return null;
    }

    protected void onPostExecute(Void v){
    }

    public static void parseRegistered(String d){
        ArrayList<User> newPend = new ArrayList<User>();
        ArrayList<User> newAll = new ArrayList<User>();
        try {
            JSONArray jsonArray = new JSONArray(d);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                System.out.println("jo=" + jo);

                String name = jo.getString("firstname") + " " + jo.getString("lastname");
                String nfc = jo.getString("nfctag");
                User u = new User(name);
                u.rfid = nfc;

                newPend.add(u);
                newAll.add(u);
            }
            if (newPend!=Globals.pending || newAll!=Globals.allUsers) {
                Globals.pending = newPend;
                Globals.allUsers = newAll;
                MainActivity.refreshLists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseCheckedIn(String d){
        ArrayList<User> newChecked = new ArrayList<User>();
        ArrayList<User> newAll = new ArrayList<User>();
        try {
            JSONArray jsonArray = new JSONArray(d);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                System.out.println("jo=" + jo);

                String name = jo.getString("firstname") + " " + jo.getString("lastname");
                String nfc = jo.getString("nfctag");
                User u = new User(name);
                u.rfid = nfc;

                newChecked.add(u);
                newAll.add(u);
            }
            if (newChecked!=Globals.pending || newAll!=Globals.allUsers) {
                Globals.pending = newChecked;
                Globals.allUsers = newAll;
                MainActivity.refreshLists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
