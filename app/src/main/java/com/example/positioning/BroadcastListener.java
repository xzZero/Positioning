package com.example.positioning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastListener extends BroadcastReceiver {
    public static String LA, LO, DI, SP;
    @Override
    public void onReceive(Context context, Intent intent) {
        double latitude = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0);
        double longitude = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0);
        float distance = intent.getFloatExtra(LocationService.EXTRA_DISTANCE, 0);
        float avgSpeed = intent.getFloatExtra(LocationService.EXTRA_AVG_SPEED, 0);
        Log.e("Location", "Lat: " + latitude + ", Lng: " + longitude + ". Distance: " + distance + ", AvgSpeec: " + avgSpeed);
        LA = String.valueOf(latitude);
        LO = String.valueOf(longitude);
        DI = String.valueOf(distance);
        SP = String.valueOf(avgSpeed);
    }
}
