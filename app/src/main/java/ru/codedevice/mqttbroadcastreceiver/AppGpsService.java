package ru.codedevice.mqttbroadcastreceiver;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class AppGpsService extends Service {

    LocationManager locationManager;
    Timer timer;
    String TAG = "AppGpsService";

    public AppGpsService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        timer = new Timer();
        timer.schedule(new UpdateTimeTask(), 0, 60000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        gps(false);
        timer.cancel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gps(true);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void gps(Boolean state) {
        if (state){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
            checkEnabled();
        }else{
            locationManager.removeUpdates(locationListener);
        }
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged ");
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled ");
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled ");
            checkEnabled();
            if (ActivityCompat.checkSelfPermission(AppGpsService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AppGpsService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
            gps(false);
            close();
            timer.cancel();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "onStatusChanged GPS_PROVIDER : " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                Log.d(TAG, "onStatusChanged NETWORK_PROVIDER : " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "location GPS_PROVIDER : " + formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "location NETWORK_PROVIDER : " + formatLocation(location));
        }
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format("Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",location.getLatitude(), location.getLongitude(), new Date(location.getTime()));
    }

    private void checkEnabled() {
        Log.d(TAG, "checkEnabled NETWORK_PROVIDER : " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        Log.d(TAG, "checkEnabled GPS_PROVIDER : " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    public void close(){
        stopSelf();
    }
    class UpdateTimeTask extends TimerTask {
        public void run() {
            Log.d(TAG, "UpdateTimeTask : run");
//            gps(true);
        }
    }
}
