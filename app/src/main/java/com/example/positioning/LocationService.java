package com.example.positioning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class LocationService extends Service {
    public static boolean isRunning = false;
    private Context mContext;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private FileOutputStream out;
    private Location lastLocation;
    private float travelDistance = 0;
    private long travelTime = 0;
    private Date date1, date2;
    private Waypoint waypoint;
    private GPX gpx;
    private GPXParser gpxParser;
    public static final String
            ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude",
            EXTRA_DISTANCE = "extra_distance",
            EXTRA_AVG_SPEED = "extra_avg_speed";

    private static final int
            MIN_TIME = 2000,
            MIN_DISTANCE = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        isRunning = true;
        Log.e("Start", "onCreate");
        Log.e("Start", String.valueOf(isRunning));
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        mContext = this;
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.e("Start", "onStartCommand");
        Log.e("Start", "onStartCommand " + String.valueOf(isRunning));
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        date1 = new Date(lastLocation.getTime());
        travelTime = 0;
        travelDistance = 0;
        sendBroadcastMessage(lastLocation, 0, 0);
        final ArrayList<Waypoint> waypointHashSet = new ArrayList<>();
        final HashSet<Track> tracks = new HashSet<>();
        waypoint = new Waypoint();
        gpx = new GPX();
        gpxParser = new GPXParser();
        waypoint.setLatitude(lastLocation.getLatitude());
        waypoint.setLongitude(lastLocation.getLongitude());
        waypoint.setTime(date1);

        waypointHashSet.add(waypoint);
        final Track track = new Track();
        track.setTrackPoints(waypointHashSet);
        tracks.add(track);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                date2 = new Date(location.getTime());
                travelTime += Math.abs(date2.getTime() - date1.getTime());
                travelDistance += distance(lastLocation, location);
                sendBroadcastMessage(location, travelDistance, avgSpeed(travelDistance, travelTime));



                waypoint = new Waypoint();
                waypoint.setLatitude(location.getLatitude());
                waypoint.setLongitude(location.getLongitude());
                waypoint.setTime(date2);
                waypointHashSet.add(waypoint);
                track.setTrackPoints(waypointHashSet);
                tracks.clear();
                tracks.add(track);
                gpx.setTracks(tracks);
                try {
                    out = new FileOutputStream("/sdcard/Download/map.gpx");
                    gpxParser.writeGPX(gpx, out);

                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (TransformerException | IOException e) {
                    e.printStackTrace();
                }
                date1 = date2;
                lastLocation = location;
                Log.e("Location", String.valueOf(location.getLatitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy(){
        Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
        Log.e("Cancel", "onDestroy");
        stopSelf();
        isRunning = false;
        locationManager.removeUpdates(locationListener);
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void sendBroadcastMessage(Location location, float distance, float speed) {
        if (location != null) {
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
            intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
            intent.putExtra(EXTRA_DISTANCE, distance);
            intent.putExtra(EXTRA_AVG_SPEED, speed);
            this.sendBroadcast(intent);
        }
    }



    private static float distance(Location location1, Location location2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(location2.getLatitude() - location1.getLatitude());
        double dLng = Math.toRadians(location2.getLongitude() - location1.getLongitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(location1.getLatitude())) * Math.cos(Math.toRadians(location2.getLatitude())) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist/1000;
    }

    private static float avgSpeed(float distance, long time){
        return (float) 3600000*distance/time;
    }


}

