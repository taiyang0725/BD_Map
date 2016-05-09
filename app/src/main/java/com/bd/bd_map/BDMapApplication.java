package com.bd.bd_map;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by An on 2016/5/4.
 */

public class BDMapApplication extends Application {
    public LocationService locationService;
    @Override
    public void onCreate() {
        super.onCreate();


        locationService = new LocationService(getApplicationContext());

        SDKInitializer.initialize(getApplicationContext());
    }
}
