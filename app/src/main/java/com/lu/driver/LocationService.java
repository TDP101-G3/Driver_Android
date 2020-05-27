package com.lu.driver;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class LocationService extends Service {
    private static final String TAG = "TAG_MusicService";
    private PowerManager.WakeLock wakeLock;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private LocationTread locationTread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        acquireWakeLock();
        updateLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdateLocation();
        releaseWakeLock();
        Log.d(TAG, "onDestroy");
    }

    // 取得wake lock
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null && wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ServiceDemo:MyWakeLock");
            // 提供timeout時間以避免事情做完了還佔用著wakelock，一般設10分鐘
            wakeLock.acquire(10 * 60 * 1000);
            Log.d(TAG, "acquireWakeLock");
        }
    }

    // 釋放wake lock
    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
            Log.d(TAG, "releaseWakeLock");
        }
    }

    private void updateLocation(){
        if (locationTread == null) {
            locationTread = new LocationTread();
            locationTread.start();
        }
    }

    private void stopUpdateLocation(){
        if (locationTread != null) {
            locationTread = null;
        }
    }

    class LocationTread extends Thread{
        @Override
        public void run(){
            DriveFragment.showLastLocation();
        }
    }
}
