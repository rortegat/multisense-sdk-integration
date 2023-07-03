package com.example.multisense_sdk_integration.services;

import static com.cellocator.nano.android.sdk.MultiSenseManager.ErrorCode.ERROR_BLUETOOTH_DISABLED;
import static com.cellocator.nano.android.sdk.MultiSenseManager.ErrorCode.ERROR_PERMISSION;
import static com.cellocator.nano.android.sdk.MultiSenseManager.ErrorCode.ERROR_READ_DATA;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.cellocator.nano.android.sdk.MultiSenseDeviceCallback;
import com.cellocator.nano.android.sdk.MultiSenseManager;
import com.cellocator.nano.android.sdk.MultiSenseObserver;
import com.cellocator.nano.android.sdk.MultiSenseObserverCallback;
import com.cellocator.nano.android.sdk.MultiSenseReadingLoggerStatus;
import com.cellocator.nano.android.sdk.MultiSenseScanner;
import com.cellocator.nano.android.sdk.model.MultiSenseDevice;
import com.cellocator.nano.android.sdk.model.MultiSenseSensors;
import com.example.multisense_sdk_integration.NotificationActivity;
import com.example.multisense_sdk_integration.R;

public class MonitoringService extends Service {

    private static final String TAG = "MULTISENSE";

    private MultiSenseScanner multiSenseScanner;
    private MultiSenseObserver multiSenseObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CREATING SERVICE");

        MultiSenseManager multiSenseManager = new MultiSenseManager(this);
        multiSenseScanner = multiSenseManager.createScanner();
        multiSenseObserver = multiSenseManager.createObserver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startScanning();
        startNotification();

        Toast.makeText(this, "Scan service started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopScanning();

        Toast.makeText(this, "Scan service stopped", Toast.LENGTH_LONG).show();

        super.onDestroy();
    }


    private void startNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker("BeaconScanner")
                .setContentTitle("BeaconScanner")
                .setContentText("Scanning in the foreground")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);

        NotificationChannel notificationChannel = new NotificationChannel("007", "MS Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        notificationManager.createNotificationChannel(notificationChannel);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setChannelId("007");

        startForeground(1, notificationBuilder.build());
    }

    private void startScanning() {
        Log.i(TAG, "STARTING SCAN");
        multiSenseScanner.scan(new MultiSenseDeviceCallback() {
            @Override
            public void onError(int errorType, String message) {
                String error;
                switch (errorType) {
                    case ERROR_BLUETOOTH_DISABLED:
                        error = "Bluetooth disabled: ";
                        break;
                    case ERROR_PERMISSION:
                        error = "Location permissions denied: ";
                        break;
                    case ERROR_READ_DATA:
                        error = "Data transfer interrupted: ";
                        break;
                    default:
                        error = "Unknown";
                }
                message = message != null ? error + message : error + "empty description";
                Log.e(TAG, message);
            }

            @Override
            public void onChange(MultiSenseDevice multiSenseDevice) {
                Log.i(TAG, "ADDING " + multiSenseDevice.getAddress());
                //Adding new tag to the observer
                multiSenseObserver.addTag(multiSenseDevice.getAddress());
            }
        });

        //Setting Foreground Mode
        multiSenseObserver.startForegroundMode();

        //Start observer service
        multiSenseObserver.startObserveTags(new MultiSenseObserverCallback() {
            @Override
            public void onReadingLoggerStatusChange(String s, MultiSenseReadingLoggerStatus multiSenseReadingLoggerStatus) {
                String status = s + " - Loading -> " + multiSenseReadingLoggerStatus.getPercent() + "%";
                if (multiSenseReadingLoggerStatus.getStatus().equals(MultiSenseReadingLoggerStatus.Status.SUCCESS))
                    status = s + " - Load completed.";
                Log.i(TAG, status);
            }

            @Override
            public void onError(int errorType, String message) {
                String error;
                switch (errorType) {
                    case ERROR_BLUETOOTH_DISABLED:
                        error = "Bluetooth disabled: ";
                        break;
                    case ERROR_PERMISSION:
                        error = "Location permissions denied: ";
                        break;
                    case ERROR_READ_DATA:
                        error = "Data transfer interrupted: ";
                        break;
                    default:
                        error = "Unknown error: ";
                }
                message = message != null ? error + message : error + "empty description";
                Log.e(TAG, message);
            }

            @Override
            public void onChange(MultiSenseDevice multiSenseDevice) {
                Log.i(TAG, multiSenseDevice.getAddress() + ": Advertisement received");

                if (multiSenseDevice.getAdvertisement().getTxReason() == MultiSenseSensors.TxReason.TXREASON_POWER_UP) {
                    Log.i(TAG, "Configuration applied to beacon: " + multiSenseDevice.getAddress());
                }

                Log.i(TAG, "Measurements retrieved from advertisement: " + multiSenseDevice.getSensors().size());
            }
        });
    }

    private void stopScanning() {
        Log.i(TAG, "STOPPING SCAN");
        multiSenseObserver.stopObserveTags();
        multiSenseScanner.stopScan();
    }


}
