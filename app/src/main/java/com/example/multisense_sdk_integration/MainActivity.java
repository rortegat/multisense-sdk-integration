package com.example.multisense_sdk_integration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.multisense_sdk_integration.services.MonitoringService;

public class MainActivity extends AppCompatActivity {

    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermissions();

        Button scanButton = findViewById(R.id.scan);

        scanButton.setOnClickListener(view -> {
            if (isScanning) {
                scanButton.setText(R.string.start);
                stopService(new Intent(getApplicationContext(), MonitoringService.class));
            } else {
                scanButton.setText(R.string.stop);

                Intent startIntent = new Intent(getApplicationContext(), MonitoringService.class);
                startService(startIntent);
            }
            isScanning = !isScanning;
        });

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

}