package ru.codedevice.mqttbroadcastreceiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.util.Log;

import java.util.HashMap;

public class AppOreoService extends Service {

    String TAG = "AppOreoService";

    BroadcastReceiver br;
    SharedPreferences settings;
    Boolean general_startNet;
    Boolean general_wifi;
    Boolean general_call;
    Boolean general_sms;
    Boolean general_battery;

    public AppOreoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        initBroadReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unregisterReceiver(br);
    }

    public void initBroadReceiver() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        general_startNet = settings.getBoolean("general_startNet", false);
        general_wifi = settings.getBoolean("general_wifi", false);
        general_call = settings.getBoolean("general_call", false);
        general_sms = settings.getBoolean("general_sms", false);
        general_battery = settings.getBoolean("general_battery", false);

        IntentFilter filter = new IntentFilter();

        if(general_wifi){
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        }
        if(general_call){
            filter.addAction("android.intent.action.PHONE_STATE");
        }
        if(general_sms){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                filter.addAction(Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION);
            }
        }
        if(general_battery){
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        }
        filter.addAction("ru.codedevice.mqttbroadcastreceiver.AppReceiver");
        filter.addAction("ru.codedevice.mqttbroadcastreceiver.AppGpsService");

        br = new AppReceiver();
        registerReceiver(br, filter);
    }
}
