package ru.codedevice.mqttbroadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AppReceiver extends BroadcastReceiver {

    String TAG = "AppReceiver";
    Context context;
    Intent i;
    SharedPreferences settings;
    Boolean general_startBoot;
    Boolean general_startNet;

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        general_startNet = settings.getBoolean("general_startNet", false);
        String action = intent.getAction();
        Log.i(TAG, "Action : " + action);
        i = new Intent(context, AppService.class);

        Log.e(TAG, "Network Connection is [" + hasConnection(context) + "]");

        if (action.equals("android.intent.action.BOOT_COMPLETED")
            || action.equals("android.intent.action.QUICKBOOT_POWERON")
            || action.equals("com.htc.intent.action.QUICKBOOT_POWERON") ){
            i.putExtra("statusInit","boot");
            context.startService(i);
        }

        if (action.equals("android.intent.action.SCREEN_ON")
                ||action.equals("android.intent.action.SCREEN_OFF")){
            i.putExtra("statusInit","screen");
            context.startService(i);
        }

        if (action.equals("android.net.wifi.STATE_CHANGE")){
            if(hasConnection(context)){
                i.putExtra("statusInit","wifi");
                context.startService(i);
            }

        }

        if (action.equals("android.intent.action.BATTERY_CHANGED")){
            i.putExtras(intent);
            i.putExtra("statusInit","batteryInfo");
            context.startService(i);
        }

        if (action.equals("android.intent.action.BATTERY_LOW")){
            i.putExtra("statusInit","battery");
            if (action.equals("android.intent.action.BATTERY_LOW")) {
                i.putExtra("status","low");
            }
            context.startService(i);
        }
        if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")
                ||action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")){
            i.putExtra("statusInit","power");
            if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                i.putExtra("power","connected");
            }else{
                i.putExtra("power","disconnected");
            }
            context.startService(i);
        }
    }

    public boolean hasConnection(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()){
            Log.e(TAG, "Network Connection is TYPE_WIFI");
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()){
            Log.e(TAG, "Network Connection is TYPE_MOBILE");
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()){
            Log.e(TAG, "Network Connection is WTF");
            return true;
        }
        return false;
    }
}
