/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.locationtester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.location.Geocoder;
import android.location.Address;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import android.util.Log;

/**
 * So you thought sync used up your battery life.
 */
public class LocationTester extends Activity {
    TextView mLog;
    DateFormat mDateFormat;
    PowerManager.WakeLock mPartialWakeLock;

    LocationManager locationManager;
    LocationProvider gpsProvider;
    LocationProvider netProvider;

    Context context;

    private long minTime; // min time between location updates, in milliseconds
    private float minDistance; // min distance between location updates, in meters

    private static final int SECONDS_TO_MILLISECONDS = 1000;
    private static final String TAG = "LocationTester";

    boolean mWasting, mWaking;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity.  You can find it
        // in res/layout/hello_activity.xml
        setContentView(R.layout.location_tester);

        findViewById(R.id.checkbox_gps).setOnClickListener(mGpsClickListener);
        findViewById(R.id.checkbox_nlp).setOnClickListener(mNlpClickListener);
        mLog = (TextView)findViewById(R.id.log);

        mDateFormat = DateFormat.getInstance();

        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryWaster");
        mPartialWakeLock.setReferenceCounted(false);

	// Prepare to request location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

	gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
	netProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);

        double tempMinTime = 1;
	minTime = (long) (tempMinTime * SECONDS_TO_MILLISECONDS);
	minDistance = 0;
	
	context = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (((CheckBox)findViewById(R.id.checkbox_gps)).isChecked()) {
            //
        }
        if (((CheckBox)findViewById(R.id.checkbox_nlp)).isChecked()) {
            //
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPartialWakeLock.isHeld()) {
            mPartialWakeLock.release();
        }
    }

    View.OnClickListener mGpsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            CheckBox checkbox = (CheckBox)v;
            if (checkbox.isChecked()) {
                //
		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			locationManager.requestLocationUpdates(gpsProvider.getName(), minTime, minDistance, mLocationListener);
                	mWaking = true;
                	mWasting = true;
                	updateWakeLock();
	        } else {
			Toast.makeText(context, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
			Intent i = new Intent();
			i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(i);
		}
            } else {
                //
		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			locationManager.removeUpdates(mLocationListener);
                	mWaking = false;
                	mWasting = false;
                	updateWakeLock();
                }
            }
        }
    };

    View.OnClickListener mNlpClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            CheckBox checkbox = (CheckBox)v;
            if (checkbox.isChecked()) {
		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			locationManager.requestLocationUpdates(netProvider.getName(), minTime, minDistance, mLocationListener);
                	mWaking = true;
                	mWasting = true;
                	updateWakeLock();
	   	} else {
			Toast.makeText(context, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
			Intent i = new Intent();
			i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(i);
		}
            } else {
		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			locationManager.removeUpdates(mLocationListener);
                	mWaking = false;
                	mWasting = false;
                	updateWakeLock();
                }
            }
        }
    };

    LocationListener mLocationListener = new LocationListener() {
	    @Override
	    public void onLocationChanged(Location location) {
		    //
		    double latitude = location.getLatitude();
		    double longitude = location.getLongitude();
		   
		    if (location.hasAccuracy()) {
			   float accuracy = location.getAccuracy();
			   Log.i(TAG, "accuracy = " + accuracy);
		    } else {
			   Log.i(TAG, "location has not accuracy");
		    }
		    // decode the location 
		    Geocoder gc = new Geocoder(context, Locale.getDefault());
		    List<Address> locationList = null;
		    try {
			    locationList = gc.getFromLocation(latitude, longitude, 1);
		    } catch (IOException e) {
			    e.printStackTrace();
		    }
		    
		    Address address = locationList.get(0); // get instance of address
		    Log.i(TAG, "address = " + address);
		    String countryName = address.getCountryName();
		    Log.i(TAG, "countryName = " + countryName);
		    String locality = address.getLocality(); // get the city name
		    Log.i(TAG, "locality = " + locality);
		    for (int i = 0; address.getAddressLine(i) != null; i++) {
			    String addressLine = address.getAddressLine(i); // get the street
			    Log.i(TAG, "addressLine = " + addressLine);
			    log(addressLine);
		    }
	    }

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {
		    //
	    }

	    @Override
	    public void onProviderEnabled(String provider) {
		    //
	    }
	
	    @Override
	    public void onProviderDisabled(String provider) {
		    //
	    }
    };

    void updateWakeLock() {
        if (mWasting) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mWaking) {
            if (!mPartialWakeLock.isHeld()) {
                mPartialWakeLock.acquire();
            }
        } else {
            if (mPartialWakeLock.isHeld()) {
                mPartialWakeLock.release();
            }
        }
    }

    void log(String s) {
        mLog.setText(mLog.getText() + "\n" + mDateFormat.format(new Date()) + ": " + s);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String title = action;
            int index = title.lastIndexOf('.');
            if (index >= 0) {
                title = title.substring(index + 1);
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                log(title + ": level=" + level);
            } else {
                log(title);
            }
        }
    };

}


