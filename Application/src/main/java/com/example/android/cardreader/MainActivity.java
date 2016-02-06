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
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.example.android.common.logger.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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
    public static ArrayList<PersonListFrag> frags = new ArrayList<PersonListFrag>();

    public static void processId(String id){
        Random random = new Random();
        User u = Globals.pending.remove(random.nextInt(Globals.pending.size()));
        u.checkinTime = new Date();
        Globals.checkedIn.add(u);
        instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(PersonListFrag f : MainActivity.frags){
                    f.mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //TODO
    void getPeople(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alternate_main_fragment);

        Globals.executeEden();

        instance = this;
        final ActionBar actionBar = getActionBar();
        actionBar.setTitle("                     TartanHacks");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        actionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.drawable.tab_bg));

        frags.add(new PersonListFrag(Globals.pending));
        frags.add(new PersonListFrag(Globals.allUsers));
        frags.add(new PersonListFrag(Globals.checkedIn));

        mAdapter = new FragmentAdapter(getFragmentManager());

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
