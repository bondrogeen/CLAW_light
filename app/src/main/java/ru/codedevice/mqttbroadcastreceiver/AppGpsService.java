package ru.codedevice.mqttbroadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gms.location.DetectedActivity;
import java.util.Date;
import java.util.Timer;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;

public class AppGpsService extends Service implements LocationBasedOnActivityProvider.LocationBasedOnActivityListener {

    Timer timer;
    String TAG = "AppGpsService";
    SharedPreferences settings;
    String general_gps_time;

    PendingIntent pi;
    AlarmManager am;
    Intent intent;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        timer = new Timer();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        general_gps_time = settings.getString("general_gps_time", "60");
        setAlarm(Integer.parseInt(general_gps_time));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        cancelAlarm();
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        Log.d(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void getLocation(){
        SmartLocation.with(getApplicationContext())
                .location(new LocationManagerProvider())
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

    @Override
    public LocationParams locationParamsForActivity(DetectedActivity detectedActivity) {

        return null;
    }

    public void setAlarm(int time){
        am =(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(this, AppReceiver.class);
        intent.setAction("getLocation");
        pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * time, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(){
        Log.e(TAG, "cancelAlarm 1111");
        am.cancel(pi);
        Log.e(TAG, "cancelAlarm  22222");
    }
}
