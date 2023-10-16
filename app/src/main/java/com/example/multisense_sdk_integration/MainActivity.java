package com.example.multisense_sdk_integration;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.multisense_sdk_integration.services.MonitoringService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean isScanning = false;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isScanning = isServiceRunning(MonitoringService.class);
        checkLocationPermissions();
        scanButton = findViewById(R.id.scan);
        SetButtonText();

        scanButton.setOnClickListener(view -> {
            isScanning = isServiceRunning(MonitoringService.class);
            if (isScanning) {
                scanButton.setText(R.string.start);
                stopService(new Intent(getApplicationContext(), MonitoringService.class));
                StartOrStopWorkManger(false);
            } else {
                scanButton.setText(R.string.stop);

                Intent startIntent = new Intent(getApplicationContext(), MonitoringService.class);
                startService(startIntent);
                StartOrStopWorkManger(true);
            }
           // isScanning = !isScanning;
        });

    }
    
    private void SetButtonText()
    {
        if (!isScanning) {
            scanButton.setText(R.string.start);
        } else {
            scanButton.setText(R.string.stop);
        }
    }

    private void StartOrStopWorkManger(boolean start)
    {
        if(start) {
            PeriodicWorkRequest timerRequest = new PeriodicWorkRequest.Builder(
                    TimerWorker.class,
                    15, // Interval in minutes
                    TimeUnit.MINUTES
            ).build();

            // Enqueue the work request
            WorkManager.getInstance(this).enqueue(timerRequest);
        }
        else
        {
            WorkManager.getInstance(getApplicationContext()).cancelAllWork();

        }
    }

    public void checkLocationPermissions() {
        //if the user hasn't granted access location permissions yet
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request for required permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}