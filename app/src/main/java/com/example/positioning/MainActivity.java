package com.example.positioning;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;

import java.io.FileOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainActivity extends AppCompatActivity {
    Button stBtn, stopBtn, uptBtn, exitBtn;
    TextView laTxt, loTxt, disTxt, spTxt;
    Intent intent;
    BroadcastListener broadcastListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);

        laTxt = findViewById(R.id.laTxt);
        loTxt = findViewById(R.id.loTxt);
        disTxt = findViewById(R.id.disTxt);
        spTxt = findViewById(R.id.spTxt);
        stBtn = findViewById(R.id.stBtn);
        stopBtn = findViewById(R.id.stopBtn);
        uptBtn = findViewById(R.id.uptBtn);
        exitBtn = findViewById(R.id.exitBtn);

        intent = new Intent(this, LocationService.class);

        if (shouldAskPermissions()) {
            askPermissions();
        }
        broadcastListener = new BroadcastListener();
        this.registerReceiver(
                broadcastListener, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );

        uptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                laTxt.setText("Latitude: " + BroadcastListener.LA);
                loTxt.setText("Longitude: " + BroadcastListener.LO);
                disTxt.setText("Distance: " + BroadcastListener.DI + "km");
                spTxt.setText("Speed: " + BroadcastListener.SP + "km/h");
            }
        });

        stBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(intent);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Cancel", String.valueOf(isMyServiceRunning(LocationService.class)));
                if (isMyServiceRunning(LocationService.class)){
                    stopService(intent);
                }

            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterReceiver(broadcastListener);
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }




    @SuppressLint("NewApi")
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }

    protected boolean shouldAskPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}