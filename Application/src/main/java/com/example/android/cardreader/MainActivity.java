/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.cardreader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.android.common.logger.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.nineoldandroids.animation.Animator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;

import cz.msebera.android.httpclient.Header;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends Activity implements ActionBar.TabListener, LoyaltyCardReader.AccountCallback{

    public static final String TAG = "MainActivity";
    public static MainActivity instance;
    static final int REQUEST_OK = 1;
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public LoyaltyCardReader mLoyaltyCardReader;
    ViewPager mViewPager;
    FragmentAdapter mAdapter;
    View fab;
    public static ArrayList<PersonListFrag> frags = new ArrayList<PersonListFrag>();
    public static String data;
    public static User usr;

    public static EditText idField;
    public static ProgressBar pb;
    public static TextView nameField;
    public static TextView scanView;
    public static AlertDialog signupDialog;
    public static AsyncHttpClient refreshClient;
    public static String curID;

    public static void register(User u){
        usr = u;
        usr.checkinTime = new Date();
        new MultiThread(new Runnable() {
            @Override
            public void run() {
                SyncHttpClient client = new SyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("andrewid", usr.andrewID);
                System.out.println("Posting " + usr.andrewID + " " + usr.rfid);
                params.setUseJsonStreamer(true);
                client.post(Globals.start + "/user/getid", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        new MultiThread(new Runnable() {
                            @Override
                            public void run() {
                                SyncHttpClient client = new SyncHttpClient();
                                RequestParams params = new RequestParams();
                                params.put("event_id",1);
                                params.put("nfctag", usr.rfid);
                                params.setUseJsonStreamer(true);
                                client.post(Globals.start + "/event/register", params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String result = new String(responseBody);
                                        System.out.println("Posted res: "+result);
                                        new MultiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                SyncHttpClient client = new SyncHttpClient();
                                                RequestParams params = new RequestParams();
                                                params.put("nfctag", usr.rfid);
                                                params.put("event_id", "1");
                                                params.setUseJsonStreamer(true);
                                                System.out.println("rfid = "+usr.rfid);
                                                client.post(Globals.start + "/event/checkin", params, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                        String result = new String(responseBody);
                                                        System.out.println("Posted res: "+result);
                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        System.out.println("Posted failed: "+error.toString());
                                                    }
                                                });
                                            }
                                        }).executeOnExecutor(Executors.newSingleThreadExecutor());
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        System.out.println("Posted failed: "+error.toString());
                                    }
                                });
                            }
                        }).executeOnExecutor(Executors.newSingleThreadExecutor());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        new MultiThread(new Runnable() {
                            @Override
                            public void run() {
                                SyncHttpClient client = new SyncHttpClient();
                                RequestParams params = new RequestParams();
                                params.put("password", "asbjdbajhscbjabshd");
                                params.put("andrewid", usr.andrewID);
                                params.put("nfctag", usr.rfid);
                                System.out.println("Posted " + usr.andrewID + " " + usr.rfid);
                                params.setUseJsonStreamer(true);
                                client.post(Globals.start + "/user/signup", params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        String result = new String(responseBody);
                                        System.out.println("Posted res1: "+result);

                                        new MultiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                SyncHttpClient client = new SyncHttpClient();
                                                RequestParams params = new RequestParams();
                                                params.put("event_id",1);
                                                params.put("nfctag", usr.rfid);
                                                params.setUseJsonStreamer(true);
                                                client.post(Globals.start + "/event/register", params, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                        String result = new String(responseBody);
                                                        System.out.println("Posted res: "+result);
                                                        new MultiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                SyncHttpClient client = new SyncHttpClient();
                                                                RequestParams params = new RequestParams();
                                                                params.put("nfctag", usr.rfid);
                                                                params.put("event_id", "1");
                                                                params.setUseJsonStreamer(true);
                                                                System.out.println("rfid = "+usr.rfid);
                                                                client.post(Globals.start + "/event/checkin", params, new AsyncHttpResponseHandler() {
                                                                    @Override
                                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                                        String result = new String(responseBody);
                                                                        System.out.println("Posted res: "+result);
                                                                    }

                                                                    @Override
                                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                                        System.out.println("Posted failed: "+error.toString());
                                                                    }
                                                                });
                                                            }
                                                        }).executeOnExecutor(Executors.newSingleThreadExecutor());
                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        System.out.println("Posted failed: "+error.toString());
                                                    }
                                                });
                                            }
                                        }).executeOnExecutor(Executors.newSingleThreadExecutor());
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        System.out.println("Posted failed1: "+error.toString());
                                        System.out.println("Posted code = "+statusCode);
                                        System.out.println("Posted 1 headers = "+headers[0]+" "+headers[1]+" "+headers[2]);
                                    }
                                });
                            }
                        }).executeOnExecutor(Executors.newSingleThreadExecutor());
                    }
                });
            }
        }).executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public static void checkin(User u){
        usr = u;
        new MultiThread(new Runnable() {
            @Override
            public void run() {
                SyncHttpClient client = new SyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("nfctag", usr.rfid);
                params.put("event_id", "1");
                params.setUseJsonStreamer(true);
                System.out.println("rfid = "+usr.rfid);
                client.post(Globals.start + "/event/checkin", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String result = new String(responseBody);
                        System.out.println("Posted res: "+result);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println("Posted failed: "+error.toString());
                    }
                });
            }
        }).executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public static void getUsers(int eventId){
        System.out.println("Getting Users.");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Globals.start + "/event/get-registered/" + eventId, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                String response = new String(bytes);
                System.out.println("Response=" + response);
                parseRegistered(response);
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("Failed with " + throwable.toString());
            }
        });

        client.get(Globals.start + "/event/get-checked-in/" + eventId, new AsyncHttpResponseHandler() {
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
    }

    public static void refreshUsers(){
        System.out.println("Refreshing Users.");
        refreshClient = new AsyncHttpClient();
        refreshClient.get(Globals.start + "/event/get-registered/" + 1, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                String response = new String(bytes);
                System.out.println("Response=" + response);
                Globals.allUsers.clear();
                Globals.pending.clear();
                parseRegistered(response);

                refreshClient.get(Globals.start + "/event/get-checked-in/" + 1, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                        String response = new String(bytes);
                        System.out.println("Response=" + response);
                        Globals.checkedIn.clear();
                        parseCheckedIn(response);
                    }

                    @Override
                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                        System.out.println("Failed with " + throwable.toString());
                    }
                });
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("Failed with " + throwable.toString());
            }
        });
    }

    public static void parseRegistered(String d){
        data = d;
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    System.out.println("jsonArray=" + jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        System.out.println("jo=" + jo);

                        String name = jo.getString("firstname") + " " + jo.getString("lastname");
                        String nfc = jo.getString("nfctag");
                        User u = new User(name);
                        u.rfid = nfc;

                        Globals.pending.add(u);
                        Globals.allUsers.add(u);
                    }
                    refreshLists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void parseCheckedIn(String d){
        data = d;
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    System.out.println("jsonArray=" + jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        System.out.println("jo=" + jo);

                        String name = jo.getString("firstname") + " " + jo.getString("lastname");
                        String nfc = jo.getString("nfctag");
                        User u = new User(name);
                        u.rfid = nfc;
                        u.checkinTime = new Date();

                        Globals.checkedIn.add(u);
                        Globals.allUsers.add(u);
                    }
                    refreshLists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void parseRegisteredRefresh(String d){
        data = d;
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<User> newPend = new ArrayList<User>();
                    JSONArray jsonArray = new JSONArray(data);
                    System.out.println("jsonArray=" + jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        System.out.println("jo=" + jo);

                        String name = jo.getString("firstname") + " " + jo.getString("lastname");
                        String nfc = jo.getString("nfctag");
                        User u = new User(name);
                        u.rfid = nfc;

                        newPend.add(u);
                    }
                    if (newPend.size()!=Globals.pending.size()) {
                        Globals.pending.clear();
                        for (User u : newPend)
                            Globals.pending.add(u);
                        refreshLists();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void parseCheckedInRefresh(String d){
        data = d;
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<User> newCheck = new ArrayList<User>();
                    JSONArray jsonArray = new JSONArray(data);
                    System.out.println("jsonArray=" + jsonArray);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        System.out.println("jo=" + jo);

                        String name = jo.getString("firstname") + " " + jo.getString("lastname");
                        String nfc = jo.getString("nfctag");
                        User u = new User(name);
                        u.rfid = nfc;
                        u.checkinTime = new Date();

                        newCheck.add(u);
                    }

                    if (newCheck.size()!=Globals.checkedIn.size()) {
                        Globals.checkedIn.clear();
                        for (User u : newCheck)
                            Globals.checkedIn.add(u);
                        refreshLists();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void refreshLists(){

        System.out.println("newPend. refreshing lists...");

        try {
            for (PersonListFrag f : frags) {
                f.mAdapter.notifyDataSetChanged();
            }

            Collections.sort(Globals.pending, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    String a = lhs.name;
                    String b = rhs.name;
                    return a.compareTo(b);
                }
            });
            Collections.sort(Globals.checkedIn, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    String a = lhs.name;
                    String b = rhs.name;
                    return a.compareTo(b);
                }
            });
            Collections.sort(Globals.allUsers, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    String a = lhs.name;
                    String b = rhs.name;
                    return a.compareTo(b);
                }
            });

            System.out.println("newPend. finished refreshing");
        } catch(Exception e){e.printStackTrace();}
    }

    public static void processId(String id){
        curID = id;
        if(Globals.adding){
            Globals.adding = false;
            signupDialog.dismiss();
            User u = new User(""+nameField.getText());
            u.rfid = id;
            u.andrewID = "" + idField.getText();
            u.checkinTime = new Date();
            Globals.allUsers.add(u);
            Globals.checkedIn.add(u);
            refreshLists();
            register(u);
        }
        else {
            instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean found = false;
                    for (User u : Globals.allUsers) {
                        if (u.rfid.equals(curID)) {
                            found = true;
                            if (u.checkinTime == null) {
                                u.checkinTime = new Date();
                                Globals.pending.remove(u);
                                Globals.checkedIn.add(u);
                                for (PersonListFrag f : MainActivity.frags)
                                    f.mAdapter.notifyDataSetChanged();
                                checkin(u);
                            } else {
                                Toast.makeText(instance, "" + u.name + " already checked in.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                    if (!found)
                        Toast.makeText(instance, "User not registered.", Toast.LENGTH_SHORT).show();
                }
            });
        }

//        if (Globals.adding){
//            Globals.adding = false;
//            signupDialog.dismiss();
//            User u = new User(""+nameField.getText());
//            u.rfid = id;
//            u.andrewID = "" + idField.getText();
//            u.checkinTime = new Date();
//            Globals.allUsers.add(u);
//            Globals.checkedIn.add(u);
//            refreshLists();
//        }
//        else {
//            Random random = new Random();
//            User u = Globals.pending.remove(random.nextInt(Globals.pending.size()));
//            u.checkinTime = new Date();
//            Globals.checkedIn.add(u);
//            checkin(u);
//            instance.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    for (PersonListFrag f : MainActivity.frags) {
//                        f.mAdapter.notifyDataSetChanged();
//                    }
//                }
//            });
//        }
    }

    public static void queryId(String id){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://apis.scottylabs.org/directory/v1/andrewID/" + id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                String response = new String(bytes);
                System.out.println("Response=" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String fullName = jsonObject.getString("first_name") + " " + jsonObject.getString("last_name");
                    nameField.setText(fullName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                YoYo.with(Techniques.FadeIn).duration(300).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        nameField.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(nameField);
                YoYo.with(Techniques.FadeIn).duration(300).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        scanView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(scanView);
                YoYo.with(Techniques.FadeOut).duration(300).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pb.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(pb);
                Globals.adding = true;
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                System.out.println("Failed with " + throwable.toString());
                if (pb.getVisibility() == View.VISIBLE) {
                    YoYo.with(Techniques.FadeOut).duration(1).withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pb.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).playOn(pb);
                    YoYo.with(Techniques.FadeOut).duration(1).withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            nameField.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).playOn(nameField);
                    YoYo.with(Techniques.FadeOut).duration(1).withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            scanView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).playOn(scanView);
                }
                Globals.adding=false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alternate_main_fragment);

        //TODO Stop Executing Eden
        //Globals.executeEden();
        getUsers(1);

        instance = this;
        final ActionBar actionBar = getActionBar();
        actionBar.setTitle("           TartanHacks");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        actionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));
        actionBar.setDisplayShowHomeEnabled(true);

        frags.add(new PersonListFrag(Globals.pending));
        frags.add(new PersonListFrag(Globals.allUsers));
        frags.add(new PersonListFrag(Globals.checkedIn));

        mAdapter = new FragmentAdapter(getFragmentManager());

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = View.inflate(instance, R.layout.dialog_signup, null);

                idField = (EditText)dialogView.findViewById(R.id.andrewIdField);
                pb = (ProgressBar)dialogView.findViewById(R.id.progress);
                nameField = (TextView)dialogView.findViewById(R.id.name);
                scanView = (TextView)dialogView.findViewById(R.id.scan_view);

                idField.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String id = s.toString();
                        pb.setVisibility(View.VISIBLE);
                        queryId(id);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(instance);
                builder.setView(dialogView);
                builder.setCancelable(true);
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Globals.adding=false;
                    }
                });

                signupDialog = builder.show();
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(0);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        actionBar.addTab(
                actionBar.newTab()
                        .setText("Pending")
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("All")
                        .setTabListener(this));
        actionBar.addTab(
                actionBar.newTab()
                        .setText("Checked In")
                        .setTabListener(this));

        mLoyaltyCardReader = new LoyaltyCardReader(this);

        // Disable Android Beam and register our card reader callback
        enableReaderMode();

        new UpdateThread().executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            System.out.println("Going into results");
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String result = thingsYouSaid.get(0);
            System.out.println("Android Heard: "+result);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        Activity activity = this;
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.enableReaderMode(activity, mLoyaltyCardReader, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        Activity activity = this;
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
        }
    }

    @Override
    public void onAccountReceived(final String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mAccountField.setText(account);
//            }
//        });
    }

    private class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            System.out.println("position = "+position);
            System.out.println("Sizes = "+Globals.pending.size()+" "+Globals.allUsers.size()+" "+Globals.checkedIn.size());
            return frags.get(position);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
