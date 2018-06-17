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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class AppGpsService extends Service{

    LocationManager locationManager;
    Timer timer;
    String TAG = "AppGpsService";

    public AppGpsService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        timer = new Timer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        timer.cancel();
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "startId :" + startId);
        if (intent != null && intent.getExtras() != null && startId == 1) {
            String status = intent.getStringExtra("status");
            Log.d(TAG, "status :" + status);
            switch (status) {
                case "init":
                    String time = intent.getStringExtra("time");
                    timer.schedule(new UpdateTimeTask(), 0, Long.parseLong(time)*1000);
                    break;
            }
        }
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void getLocation(){
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        if (location != null) {
                            Log.d(TAG, "latitude : " + location.getLatitude());
                            Log.d(TAG, "longitude : " + location.getLongitude());
                            Log.d(TAG, "time : " + new Date(location.getTime()));
                            Intent intent = new Intent();
                            intent.setAction("ru.codedevice.mqttbroadcastreceiver.AppGpsService");
                            intent.putExtra("latitude", String.valueOf(location.getLatitude()));
                            intent.putExtra("longitude", String.valueOf(location.getLongitude()));
                            intent.putExtra("speed", String.valueOf(location.getSpeed()));
                            intent.putExtra("unixTime", String.valueOf(location.getTime()));
                            intent.putExtra("date", String.format("%tF", new Date(location.getTime())));
                            intent.putExtra("time", String.format("%tT", new Date(location.getTime())));
                            sendBroadcast(intent);
                        }
                    }
                });
    }

    class UpdateTimeTask extends TimerTask {
        public void run() {
//            Log.d(TAG, "UpdateTimeTask : run");
            getLocation();
        }
    }
}
