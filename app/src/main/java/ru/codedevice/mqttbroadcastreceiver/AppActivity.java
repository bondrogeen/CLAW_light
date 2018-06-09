package ru.codedevice.mqttbroadcastreceiver;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.*;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String TAG = "AppActivity";
    private ExpandingList mExpandingList;
    MaterialDialog dialog;
    SharedPreferences settings;

    JSONObject mainObject = new JSONObject();
    JSONObject subObject =new JSONObject();
    JSONArray subArray = new JSONArray();
    JSONArray allArray = new JSONArray();

    Boolean general_key = false;
    Boolean general_sms = false;
    Boolean general_call = false;
    Boolean permission_two = false;

    String[] PERMISSION_CALL = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
    };
    String[] PERMISSION_SMS = new String[]{
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS
    };
    String[] PERMISSION_STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    final int PERMISSION_REQUEST_CODE_WRITE_STORAGE = 1;
    final int PERMISSION_REQUEST_CODE_READ_STORAGE = 2;

    interface OnItemCreated {
        void itemCreated(String title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions(PERMISSION_STORAGE)){
                    createElementItem();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mExpandingList = findViewById(R.id.expanding_list_main);

        initSettings();

        if(checkPermissions(PERMISSION_STORAGE)){
            createItems();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart()");
        initSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume()");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity: onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity: onDestroy()");
        if(dialog!=null){
            dialog.dismiss();
        }

    }

    public void initSettings(){
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        general_key = settings.getBoolean("general_key", false);
        general_sms = settings.getBoolean("general_sms", false);
        general_call = settings.getBoolean("general_call", false);

        if (general_sms){
            if(!checkPermissions(PERMISSION_SMS)){
                general_sms = false;
                editor.putBoolean("general_sms",general_sms);
                editor.apply();
            }
        }
        if (general_call){
            if(!checkPermissions(PERMISSION_CALL)){
                general_call = false;
                editor.putBoolean("general_call",general_call);
                editor.apply();
            }
        }

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(general_key) {
            Intent intent = new Intent(AppActivity.this, AppService.class);
            intent.putExtra("statusInit", "key");
            Log.d(TAG, "event : " + event.getAction());
            int key = event.getKeyCode();
            String value = String.valueOf(event.getAction());
            if (event.getRepeatCount() == 0) {
                intent.putExtra("key", Variable.KEY[key]);
                intent.putExtra("value", value);
                startService(intent);
            }
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                    || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(AppActivity.this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_settings:
                startActivity(new Intent(AppActivity.this, SettingsActivity.class));
                break;
            case R.id.nav_share:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"bondrogeen@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "MQTT BroadcastReceiver");
                email.putExtra(Intent.EXTRA_TEXT, "");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Select email client :"));
                break;
            case R.id.nav_donate:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HYHDHXVH6UE3C"));
                startActivity(browserIntent);
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public JSONObject findArr(JSONArray array,String name){
        Log.d(TAG, "findArr array : " + array);
        Log.d(TAG, "findArr name : " + name);
        Log.d(TAG, "findArr array.length() : " + array.length());
        JSONObject Object;
        String topic;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object = array.getJSONObject(i);
                Log.d(TAG, "findArr Object : " + Object);
                topic = String.valueOf(Object.get("topic"));
                Log.d(TAG, name + ".equals("+topic+") : " + name.equals(topic));
                if (name.equals(topic)){
                    return Object;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public void sendIntent(String topic,String value){
        Intent i = new Intent(this, AppService.class);
        i.putExtra("statusInit","item");
        i.putExtra("topic",topic);
        i.putExtra("value",value);
        startService(i);
    }
    public Boolean findTopic(JSONArray array,String name){
        JSONObject Object;
        String topic;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object = array.getJSONObject(i);
                topic = String.valueOf(Object.get("topic"));
                if (name.equals(topic)){
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public JSONArray removeArr(JSONArray array,String name){
        Log.d(TAG, "removeArr array : " + array);
        Log.d(TAG, "removeArr name : " + name);
        Log.d(TAG, "removeArr array.length() : " + array.length());
        JSONArray newArr = new JSONArray();
        JSONObject Object;
        String topic;
        for (int i = 0; i < array.length(); i++) {
            try {
                Object = array.getJSONObject(i);
                Log.d(TAG, "removeArr Object : " + Object);
                topic = String.valueOf(Object.get("topic"));
                Log.d(TAG, "removeArr name.equals(topic) : " + name.equals(topic));
                if (!name.equals(topic)){
                    newArr.put(Object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return newArr;
    }

    private void createItems() {
        allArray = Storage.getArr("allArray");

        if(allArray == null){
            Log.d(TAG, "allArray == null : ");
            allArray = new JSONArray();
            try {
                subObject.put("name","First");
                subObject.put("topic","first");
                subArray.put(subObject);
                mainObject.put("name","RoomNew");
                mainObject.put("topic","room");
                mainObject.put("color", R.color.blue);
                mainObject.put("icon",R.drawable.ic_ghost);
                mainObject.put("subArray",subArray);
                allArray.put(mainObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < allArray.length(); i++) {
            try {
                JSONObject Object = allArray.getJSONObject(i);
                addItemObject(Object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addItemObject(JSONObject obj) {
        final ExpandingItem item = mExpandingList.createNewItem(R.layout.expanding_layout);
        int itemColor = R.color.purple;
        int itemIcon = R.drawable.ic_ghost;
        String itemName = "Room";
        String itemTopic = "room";
        JSONArray subArray = null;
        Log.d(TAG, "obj : " + obj);
        if (item != null && obj != null) {
            try {
                itemColor = obj.getInt("color");
                itemIcon = obj.getInt("icon");
                itemName = obj.getString("name");
                itemTopic = obj.getString("topic");
                if(obj.has("subArray")){
                    subArray = obj.getJSONArray("subArray");
                }else{
                    subArray = new JSONArray();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            item.setIndicatorColorRes(itemColor);
            item.setIndicatorIconRes(itemIcon);
            TextView tit = item.findViewById(R.id.title);
            tit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggleExpanded();
                }
            });

            String finalTopic = itemTopic;
            String finalItemName = itemName;
            tit.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View arg0) {
                    Log.d(TAG, "setOnClickListener : ");
                    dialog = new MaterialDialog.Builder(AppActivity.this)
                            .title(tit.getText())
                            .positiveText("Delete")
                            .negativeText("Create")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    mExpandingList.removeItem(item);
                                    Log.d(TAG, "finalTopic : " + finalTopic);
                                    Log.d(TAG, "allArrayallArray : " + allArray);
                                    allArray = removeArr(allArray,finalTopic);
                                    Log.d(TAG, "allArrayallArray : " + allArray);
                                    Storage.putArr("allArray",allArray);
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    createElementSub(item, finalItemName,finalTopic);

                                }
                            })
                            .show();

//
                    return true;
                }
            });
            tit.setText(itemName);
            Log.d(TAG, "subArray : " + subArray);
            if(subArray!=null && subArray.length()>0){
                Log.d(TAG, "subArray!=null : ");
                item.createSubItems(subArray.length());
                for (int i = 0; i < subArray.length(); i++) {
                    try {
                        JSONObject subObject = subArray.getJSONObject(i);
                        String subName = subObject.getString("name");
                        String subTopic = subObject.getString("topic");
                        final View view = item.getSubItemView(i);

                        configureSubItem(item, view, itemName,itemTopic,subName,subTopic);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void configureSubItem(final ExpandingItem item, final View view, String itemName,String itemTopic,String subName,String subTopic) {
        TextView sub_title = view.findViewById(R.id.sub_title);
        sub_title.setText(subName);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "view.setOnClickListener itemName: " + itemName);
                Log.d(TAG, "view.setOnClickListener itemTopic: " + itemTopic);
                Log.d(TAG, "view.setOnClickListener subName: " + subName);
                Log.d(TAG, "view.setOnClickListener subTopic: " + subTopic);
                View customView;
                Button button_on;
                Button button_off;
//                Button button_true;
//                Button button_false;
                MaterialDialog dialog = new MaterialDialog.Builder(AppActivity.this)
                        .title(subName)
                        .customView(R.layout.material_dialog_button, true)
                        .build();
                customView = dialog.getCustomView();
                assert customView != null;
                button_on = customView.findViewById(R.id.button_on);
                button_off = customView.findViewById(R.id.button_off);
//                button_on.setBackgroundColor(Color.GREEN);
//                button_on.setTextColor(Color.WHITE);
//                button_off.setBackgroundColor(Color.RED);
//                button_off.setTextColor(Color.WHITE);
                button_off.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendIntent(itemTopic + "/" + subTopic, "false");
                        dialog.dismiss();
                    }
                });
                button_on.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendIntent(itemTopic + "/" + subTopic, "true");
                        dialog.dismiss();
                    }
                });

//                button_true = customView.findViewById(R.id.button_true);
//                button_false = customView.findViewById(R.id.button_false);
                dialog.show();



            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                Log.d(TAG, "setOnClickListener : ");
                dialog = new MaterialDialog.Builder(AppActivity.this)
                        .title(sub_title.getText())
                        .positiveText("Delete")
                        .negativeText("Edit")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                JSONObject obj = findArr(allArray,itemTopic);
                                JSONArray subArr;
                                try {
                                    if(obj.has("subArray")){
                                        Log.d(TAG, "find : ");
                                        subArr = obj.getJSONArray("subArray");
                                    }else{
                                        subArr  = new JSONArray();
                                    }
                                    subArr = removeArr(subArr,subTopic);
                                    obj.put("subArray",subArr);
                                    Log.d(TAG, "allArray : " + allArray);
                                    allArray = removeArr(allArray,itemTopic);
                                    Log.d(TAG, "allArray removeArr: " + allArray);
                                    allArray.put(obj);
                                    Log.d(TAG, "allArray allArray.put: " + allArray);
                                    Storage.putArr("allArray",allArray);
                                    item.removeSubItem(view);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                editElementSub(item,view,itemName,itemTopic,subName,subTopic);
                            }
                        })
                        .show();//
                return true;
            }
        });
    }

    public boolean containsWhiteSpace(String str){
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(str);
        return (matcher.find() || str.length() == 0);
    }

    public void createElementItem(){
        View customView;
        EditText name;
        EditText topic;
        Button button;
        dialog = new MaterialDialog.Builder(AppActivity.this)
                .title("Add a new category")
                .customView(R.layout.material_dialog_custom_view, true)
                .build();

        customView = dialog.getCustomView();
        assert customView != null;
        name = customView.findViewById(R.id.textName);
        topic = customView.findViewById(R.id.textTopic);


        button = customView.findViewById(R.id.buttonOk);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicText = String.valueOf(topic.getText());
                Log.d(TAG, "topicText : true" + topicText);
                Log.d(TAG, "findTopic : " + findTopic(allArray, topicText));
                Log.d(TAG, "containsWhiteSpace("+topicText+") : " + containsWhiteSpace(topicText));
                if(findTopic(allArray, topicText) || containsWhiteSpace(topicText)){
                    topic.setTextColor(Color.RED);
                    if(findTopic(allArray, topicText)){
                        showToast("This topic already exists");
                    }
                    if(containsWhiteSpace(topicText)) {
                        showToast("Topic must be without spaces and have at least one character");
                    }
                }else {
                    try {
                        JSONObject newObj = new JSONObject();
                        newObj.put("name", name.getText());
                        newObj.put("topic", topic.getText());
                        newObj.put("color", R.color.blue);
                        newObj.put("icon", R.drawable.ic_ghost);
                        newObj.put("subArray", new JSONArray());
                        allArray.put(newObj);
                        Storage.putArr("allArray", allArray);
                        addItemObject(newObj);
                        newObj = null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    public void showToast(String test) {
        Toast toast = Toast.makeText(getApplicationContext(),test,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void createElementSub(ExpandingItem item,String itemName , String itemTopic){
        Log.d(TAG, "createElementSub : ");
        Log.d(TAG, "top : "+ itemTopic);
        View customView;
        EditText name;
        EditText topic;
        Button button;
        MaterialDialog dialog = new MaterialDialog.Builder(AppActivity.this)
                .title("Add a new sub item")
                .customView(R.layout.material_dialog_custom_view, true)
                .build();
        customView = dialog.getCustomView();
        assert customView != null;
        name = customView.findViewById(R.id.textName);
        topic = customView.findViewById(R.id.textTopic);
        button = customView.findViewById(R.id.buttonOk);


        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicText = String.valueOf(topic.getText());
                JSONObject obj = findArr(allArray,itemTopic);
                JSONArray subArr;
                try {
                    if (obj.has("subArray")) {
                        Log.d(TAG, "find : ");
                        subArr = obj.getJSONArray("subArray");
                    } else {
                        Log.d(TAG, "Not find : ");
                        subArr = new JSONArray();
                    }
                    if (findTopic(subArr, topicText) || containsWhiteSpace(topicText)) {
                        topic.setTextColor(Color.RED);
                        if (findTopic(subArr, topicText)) {
                            showToast("This topic already exists");
                        }
                        if (containsWhiteSpace(topicText)) {
                            showToast("Topic must be without spaces and have at least one character");
                        }
                    } else {
                        JSONObject newObj = new JSONObject();
                        newObj.put("name", name.getText());
                        newObj.put("topic", topic.getText());
                        Log.d(TAG, "newObj : " + newObj);
                        subArr.put(newObj);
                        Log.d(TAG, "subArray : " + subArr);
                        obj.put("subArray", subArr);
                        Log.d(TAG, "obj : " + obj);
                        Log.d(TAG, "allArray : " + allArray);
                        allArray = removeArr(allArray, itemTopic);
                        Log.d(TAG, "allArray : " + allArray);
                        allArray.put(obj);
                        Log.d(TAG, "allArray : " + allArray);
                        Storage.putArr("allArray", allArray);
                        Log.d(TAG, "allArray : " + allArray);
                        View newSubItem = item.createSubItem();
                        configureSubItem(item, newSubItem, itemName, itemTopic, String.valueOf(name.getText()), String.valueOf(topic.getText()));
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });

        dialog.show();
    }

    public void editElementSub(ExpandingItem item, final View view,String itemName,String itemTopic,String subName,String subTopic){
        Log.d(TAG, "editElementSub : ");
        Log.d(TAG, "subTopic : "+ subTopic);
        View customView;
        EditText name;
        EditText topic;
        Button button;
        MaterialDialog dialog = new MaterialDialog.Builder(AppActivity.this)
                .title("Edit a sub item")
                .customView(R.layout.material_dialog_custom_view, true)
                .build();
        customView = dialog.getCustomView();
        assert customView != null;
        name = customView.findViewById(R.id.textName);
        topic = customView.findViewById(R.id.textTopic);
        button = customView.findViewById(R.id.buttonOk);
        name.setText(subName);
        topic.setText(subTopic);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String topicText = String.valueOf(topic.getText());
                JSONObject obj = findArr(allArray,itemTopic);
                JSONArray subArr;
                try {
                    if (obj.has("subArray")) {
                        Log.d(TAG, "find : ");
                        subArr = obj.getJSONArray("subArray");
                    } else {
                        Log.d(TAG, "Not find : ");
                        subArr = new JSONArray();
                    }
                    if (containsWhiteSpace(topicText)) {
                        topic.setTextColor(Color.RED);
                        if (containsWhiteSpace(topicText)) {
                            showToast("Topic must be without spaces and have at least one character");
                        }
                    } else {
                        JSONObject newObj = new JSONObject();
                        newObj.put("name", name.getText());
                        newObj.put("topic", topic.getText());
                        Log.d(TAG, "newObj : " + newObj);
                        subArr = removeArr(subArr, itemTopic);
                        subArr.put(newObj);
                        Log.d(TAG, "subArray : " + subArr);
                        obj.put("subArray", subArr);
                        Log.d(TAG, "obj : " + obj);
                        Log.d(TAG, "allArray : " + allArray);
                        allArray = removeArr(allArray, itemTopic);
                        Log.d(TAG, "allArray : " + allArray);
                        allArray.put(obj);
                        Log.d(TAG, "allArray : " + allArray);
                        Storage.putArr("allArray", allArray);
                        Log.d(TAG, "allArray : " + allArray);
                        View newSubItem = item.createSubItem();
                        item.removeSubItem(view);
                        configureSubItem(item, newSubItem, itemName, itemTopic, String.valueOf(name.getText()), String.valueOf(topic.getText()));
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });

        dialog.show();
    }

    private boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(this, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
                return false;
            }
        }
        return true;
    }

    private void requestPermission() {
        if(permission_two) {
            dialog = new MaterialDialog.Builder(this)
                    .title("Permission")
                    .content("In order to use this function, you must provide a permission.")
                    .positiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(appSettingsIntent, 100);
                        }
                    })
                    .show();
        }else{
            permission_two = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.e(TAG, "grantResults : " + grantResults);
        Log.e(TAG, "permissions : " + permissions);
        Log.e(TAG, "requestCode : " + requestCode);
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            return;
        }
    }


}
