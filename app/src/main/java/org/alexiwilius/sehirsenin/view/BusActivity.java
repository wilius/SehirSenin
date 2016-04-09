/*
 * Copyright 2012 The Android Open Source Project
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

package org.alexiwilius.sehirsenin.view;

import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import org.alexiwilius.ranti_app.location.LocationDetector;
import org.alexiwilius.ranti_app.location.NoActiveLocationSupplier;
import org.alexiwilius.ranti_app.location.NoLocationSupplierException;
import org.alexiwilius.ranti_app.util.Cache;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.ranti_app.view.layout.SlidingTabLayout;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Edwin on 15/02/2015.
 */
public class BusActivity extends ActionBarActivity {
    public static final String BUS_ID = "bus_id";

    // Declaring Your View and Variables

    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    String Titles[];
    int Numboftabs = 2;

    {
        UIThread.setActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Titles = new String[]{getString(R.string.departure_times), getString(R.string.line_path)};
        setContentView(R.layout.bus_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(getColor(R.color.claret_red_window));
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.claret_red_window));
        try {
            JSONObject line = Database.getLine(getIntent().getStringExtra(BUS_ID));
            setTitle(String.format("%s - %s", line.getString("id"), line.getString("name")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            LocationDetector.setLocationManager((LocationManager) getSystemService(LOCATION_SERVICE));
            // Creating The Toolbar and setting it as the Toolbar for the activity
            toolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);


            // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
            adapter = new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(adapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // Setting Custom Color for the Scroll bar indicator of the Tab View
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.tabsScrollColor);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);
        } catch (NoLocationSupplierException | NoActiveLocationSupplier e) {
            console.notifyAndClose(this, e.getMessage());
        }
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        String Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
        int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


        // Build a Constructor and assign the passed Values to appropriate values in the class
        public ViewPagerAdapter(FragmentManager fm, String mTitles[], int mNumbOfTabsumb) {
            super(fm);

            this.Titles = mTitles;
            this.NumbOfTabs = mNumbOfTabsumb;

        }

        //This method return the fragment for the every position in the View Pager
        @Override
        public Fragment getItem(int position) {

            if (position == 0) return new DepartureTimes();
            if (position == 1) return new LinePath();
            return null;
        }

        // This method return the titles for the Tabs in the Tab Strip

        @Override
        public String getPageTitle(int position) {
            return Titles[position];
        }

        // This method return the Number of tabs for the tabs Strip

        @Override
        public int getCount() {
            return NumbOfTabs;
        }
    }
}