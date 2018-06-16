package ru.codedevice.mqttbroadcastreceiver;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AppService extends Service implements MqttCallback {

    MqttAndroidClient MQTTclient;
    MqttConnectOptions options;
    SharedPreferences settings;
    Context context;
    BroadcastReceiver br;
    String serverUri;

    Boolean noDestroy = false;
    String clientId = "";
    String TAG = "AppService";

    String mqtt_server;
    String mqtt_port;
    String mqtt_username;
    String mqtt_password;
    String mqtt_device;
    String mqtt_firs_topic;
    Boolean mqtt_run;
    Boolean only_one = true;
    Boolean test = false;
    String list_general_full_battery;
    Map<String, String> map;

    JSONObject wedgetNameJSON;
    JSONObject allWedgetJSON;

    String widgetName;
    String widgetId;
    String widgetType;
    String widgetValue;
    String widgetText;
    String widgetTitle;
    SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        initSettings();
        initMQTT();
        map = new HashMap<String, String>();
        sp = getSharedPreferences(ConfigWidget.WIDGET_PREF, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null && intent.getExtras() != null) {
            String status = intent.getStringExtra("statusInit");
            Log.d(TAG, "statusInit :" + status);
            switch (status) {
                case "battery":
                    String battery = intent.getStringExtra("status");
                    map.put("info/battery/status", battery);
                    map.put("info/battery/level", String.valueOf(getBatteryLevel()));
                    break;
                case "boot":
                    map.put("info/boot/status", "start");
                    break;
                case "call":
                    String number = intent.getStringExtra("number");
                    String callStatus = intent.getStringExtra("type");
                    Log.e(TAG, "callStatus   :  " + callStatus);

                    if(callStatus.equals("disconnection")){
                        callList();
                    }
                    map.put("info/call/status", callStatus);
                    if (number != null) {
                        String name = uploadContactPhoto(this, number);
                        map.put("info/call/number", number);
                        map.put("info/call/name", name);
                    }
                    map.put("info/battery/level", String.valueOf(getBatteryLevel()));
                    break;
                case "sms":
                    String numberSms = intent.getStringExtra("number");
                    String textSms = intent.getStringExtra("text");
                    Log.e(TAG, "numberSms   :  " + numberSms);
                    Log.e(TAG, "textSms   :  " + textSms);
                    String name = uploadContactPhoto(this, numberSms);
                    map.put("info/sms/number", numberSms);
                    map.put("info/sms/text", textSms);
                    map.put("info/sms/name", name);
                    map.put("info/battery/level", String.valueOf(getBatteryLevel()));
                    break;
                case "buttons":
                    String button = intent.getStringExtra("button");
                    map.put("info/buttons/" + button, "true");
                    break;
                case "item":
                    String val = intent.getStringExtra("value");
                    String topic = intent.getStringExtra("topic");
                    map.put("info/item/" + topic, val);
                    break;
                case "seekbar":
                    int value = intent.getIntExtra("value", 0);
                    map.put("info/buttons/seekbar", String.valueOf(value));
                    break;
                case "key":
                    String key = intent.getStringExtra("key");
                    String key_value = intent.getStringExtra("value");
                    map.put("info/key/" + key, key_value);
                    break;
                case "googleNow":
                    String text = intent.getStringExtra("Text");
                    map.put("info/googleNow/text", text.toLowerCase());
                    break;
                case "test":
                    map.put("info/buttons/check", "true");
                    test = true;
                    break;
                case "wifi":
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = wifiManager.getConnectionInfo();
                    String ssid = info.getSSID();
                    Log.d(TAG, "ssid :" + ssid);
                    map.put("info/wifi/ssid", ssid);
                    map.put("info/battery/level", String.valueOf(getBatteryLevel()));
                    break;
                case "power":
                    String power = intent.getStringExtra("power");
                    map.put("info/battery/charging", power);
                    if (power.equals("connected")) {
                        initBroadReceiver();
                        noDestroy = true;
                    } else {
                        noDestroy = false;
                    }
                    break;
                case "batteryInfo":
                    int level = intent.getIntExtra("level", -1);
                    list_general_full_battery = settings.getString("list_general_full_battery", "100");
                    int setting_full_battery = Integer.parseInt(list_general_full_battery);
                    if (level >= setting_full_battery && only_one) {
                        map.put("info/battery/status", "ok");
                        only_one = false;
                    }
                    map.put("info/battery/level", String.valueOf(level));
                    int voltage = intent.getIntExtra("voltage", -1);
                    map.put("info/battery/voltage", String.valueOf((float) voltage / 1000));
                    int plugtype = intent.getIntExtra("plugged", -1);
                    String type = "";
                    if (plugtype == 0) {
                        type = "none";
                    } else if (plugtype == 1) {
                        type = "ac";
                    } else if (plugtype == 2) {
                        type = "usb";
                    } else {
                        type = String.valueOf(plugtype);
                    }
                    map.put("info/battery/plugtype", type);
                    int health = intent.getIntExtra("health", -1);
                    map.put("info/battery/health", String.valueOf(Variable.BATTERY_HEALTH[health]));
                    int temperature = intent.getIntExtra("temperature", -1);
                    map.put("info/battery/temperature", String.valueOf((float) temperature / 10));
                    break;
                case "widget":
                    widgetName = intent.getStringExtra("widgetName");
                    widgetId = intent.getStringExtra("widgetId");
                    widgetType = intent.getStringExtra("widgetType");
                    widgetValue = sp.getString(ConfigWidget.WIDGET_KEY_VALUE + widgetId, "false");
                    map.put("info/widget/" + widgetName, widgetValue);

                    SharedPreferences.Editor editor = sp.edit();
                    widgetValue = (widgetValue.equals("false") ? "true" : "false");
                    Log.e(TAG, "AppMqttService widgetValue revirs = " + widgetValue);
                    editor.putString(ConfigWidget.WIDGET_KEY_VALUE + widgetId, widgetValue);
                    editor.apply();

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                    AppWidgetOne.updateAppWidget(this, appWidgetManager, Integer.parseInt(widgetId), sp);

                    break;
                case "widget_create":
                    widgetName = intent.getStringExtra("widgetName");

                    try {
//                        wedgetNameJSON = AppWidgetOne.allWidget.getJSONObject(widgetName);
                        wedgetNameJSON = Storage.get("allWidget").getJSONObject(widgetName);
                        widgetId = wedgetNameJSON.getString("ID");
                        widgetType = wedgetNameJSON.getString("TYPE");
                        widgetText = wedgetNameJSON.getString("TEXT");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, "AppMqttService widgetId = " + widgetId);
                    Log.e(TAG, "AppMqttService widgetName = " + widgetName);
                    Log.e(TAG, "AppMqttService widgetType = " + widgetType);
                    Log.e(TAG, "AppMqttService widgetText = " + widgetText);

                    widgetValue = sp.getString(ConfigWidget.WIDGET_KEY_VALUE + widgetId, "false");
                    widgetTitle = sp.getString(ConfigWidget.WIDGET_KEY_TITLE + widgetId, "false");
                    if (MQTTclient.isConnected()) {
                        if (widgetType.equals(ConfigWidget.WIDGET_TYPE_TEXT_AND_TITLE)) {
//                             publish("comm/widget/" + widgetName + "/text", widgetText);
//                             publish("comm/widget/" + widgetName + "/title", widgetTitle);
                        }
                        if (widgetType.equals(ConfigWidget.WIDGET_TYPE_BUTTON)) {
//                             publish("comm/widget/" + widgetName + "/button", widgetValue);
//                             publish("comm/widget/" + widgetName + "/title", widgetTitle);
                        }
                    }
                    break;
            }
        }
        if (mqtt_run) {
            if (!MQTTclient.isConnected()) {
                connectMQTT();
            } else {
                sendData();
            }
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        return level;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void initBroadReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        br = new AppReceiver();
        registerReceiver(br, filter);
    }

    public void callList() {
        String[] projection = new String[]{CallLog.Calls._ID,CallLog.Calls.DATE,CallLog.Calls.NUMBER,CallLog.Calls.NEW, CallLog.Calls.TYPE};
        String where = "";

        int missed = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Cursor cursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI,projection,where,null,null
        );

        if (cursor.moveToFirst()) {
            do {
                long _id = cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID));
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                long type = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.TYPE));
                long newCall = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.NEW));
                if (type == CallLog.Calls.MISSED_TYPE && newCall != 0){
                    missed = missed + 1;
                    Log.d(TAG, "number : " + number + "  type : " + type);
                    Log.d(TAG, "newCall : " + newCall);
                }
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }

        map.put("info/call/missed", String.valueOf(missed));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(mqtt_run){
            disconnect();
        }
        if(br!=null){
            unregisterReceiver(br);
        }
    }

    public void initSettings() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        mqtt_server = settings.getString("mqtt_server", "");
        mqtt_port = settings.getString("mqtt_port", "");
        serverUri = "tcp://" + mqtt_server + ":" + mqtt_port;
        mqtt_username = settings.getString("mqtt_login", "");
        mqtt_password = settings.getString("mqtt_pass", "");
        mqtt_device = settings.getString("mqtt_device", "");
        mqtt_run = settings.getBoolean("mqtt_run", false);
        mqtt_firs_topic = settings.getString("mqtt_first_topic", "");
        if (mqtt_device==null || mqtt_device.equals("")) {
            mqtt_device = Build.MODEL;
        }
        mqtt_device = mqtt_device.replaceAll("\\s+","_");
        mqtt_device = mqtt_device.replaceAll("/","_");
        if (!mqtt_firs_topic.equals("")){
            mqtt_device = mqtt_firs_topic+"/"+mqtt_device;
        }

    }

    public void initMQTT() {
        Log.i(TAG, "Start initMQTT");
        clientId = clientId + System.currentTimeMillis();

        MQTTclient = new MqttAndroidClient(this.getApplicationContext(), serverUri, clientId);
        MQTTclient.setCallback(this);
        options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);
        if (mqtt_username!=null && !mqtt_username.equals("")){
            Log.i(TAG, "mqtt_username true");
            options.setUserName(mqtt_username);
        }
        if (mqtt_password!=null && !mqtt_password.equals("")){
            Log.i(TAG, "mqtt_password true");
            options.setPassword(mqtt_password.toCharArray());
        }
    }

    public void connectMQTT() {
        Log.d(TAG, "connectMQTT");
        try {
            IMqttToken token = MQTTclient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connection");
                    sendData();
                    if(test){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Connection", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Connection Failure");
                    if(test){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Connection Failure", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    stopSelf();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        Log.d(TAG, "Disconnect start");
        if (MQTTclient != null) {
            try {
                MQTTclient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            MQTTclient = null;
            Log.d(TAG, "Disconnect success");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void sendData(){
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            publish(String.valueOf(pair.getKey()), String.valueOf(pair.getValue()));
            Log.i(TAG,pair.getKey() + " = " + pair.getValue());
            it.remove();
        }
        if(!noDestroy){
            stopSelf();
        }
    }

    public void publish(String topic, String payload) {
        if(MQTTclient.isConnected()) {
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                MQTTclient.publish(mqtt_device + "/" + topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }


        private String uploadContactPhoto(Context context, String number) {
            String name = null;
            String contactId = null;
            InputStream input = null;
            String[] projection = new String[] {
                    ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};
            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor.moveToFirst()) {
                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
                Log.v("ffnet", "Started uploadcontactphoto: Contact Found @ " + number);
                Log.v("ffnet", "Started uploadcontactphoto: Contact name = " + name);
            } else {
                Log.v("ffnet", "Started uploadcontactphoto: Contact Not Found @ " + number);
            }
            return name;
    }
}
