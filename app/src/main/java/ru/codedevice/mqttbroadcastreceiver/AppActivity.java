package ru.codedevice.mqttbroadcastreceiver;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.*;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String TAG = "AppActivity";
    private ExpandingList mExpandingList;
    MaterialDialog dialog;

    JSONObject mainObject = new JSONObject();
    JSONObject subObject =new JSONObject();
    JSONArray subArray = new JSONArray();
    JSONArray allArray = new JSONArray();

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
                createElementItem();
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
            mainObject.put("color",R.color.blue);
            mainObject.put("icon",R.drawable.ic_ghost);
            mainObject.put("subArray",subArray);
            allArray.put(mainObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        }

        createItems();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart()");
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent = new Intent(AppActivity.this, AppService.class);
        intent.putExtra("statusInit","key");
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                intent.putExtra("key","up");
                startService(intent);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                intent.putExtra("key","down");
                startService(intent);
                return true;
        }
        return super.onKeyDown(keyCode, event);
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
                                item.removeSubItem(view);

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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {

                            }
                        })
                        .show();//
                return true;
            }
        });
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
                try {
                    JSONObject newObj = new JSONObject();
                    newObj.put("name",name.getText());
                    newObj.put("topic",topic.getText());
                    newObj.put("color",R.color.blue);
                    newObj.put("icon",R.drawable.ic_ghost);
                    newObj.put("subArray",new JSONArray());
                    allArray.put(newObj);
                    Storage.putArr("allArray",allArray);
                    addItemObject(newObj);
                    newObj = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
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
                Log.d(TAG, "allArray : "+ allArray);
                JSONObject obj = findArr(allArray,itemTopic);
                Log.d(TAG, "find obj : " + obj);
                JSONArray subArr;
                try {
                    if(obj.has("subArray")){
                        Log.d(TAG, "find : ");
                        subArr = obj.getJSONArray("subArray");
                    }else{
                        Log.d(TAG, "Not find : ");
                        subArr = new JSONArray();
                    }
                    JSONObject newObj = new JSONObject();
                    newObj.put("name",name.getText());
                    newObj.put("topic",topic.getText());
                    Log.d(TAG, "newObj : "+ newObj);
                    subArr.put(newObj);
                    Log.d(TAG, "subArray : "+ subArr);
                    obj.put("subArray",subArr);
                    Log.d(TAG, "obj : "+ obj);
                    Log.d(TAG, "allArray : "+ allArray);
                    allArray = removeArr(allArray,itemTopic);
                    Log.d(TAG, "allArray : "+ allArray);
                    allArray.put(obj);
                    Log.d(TAG, "allArray : "+ allArray);
                    Storage.putArr("allArray",allArray);
                    Log.d(TAG, "allArray : "+ allArray);
                    View newSubItem = item.createSubItem();
                    configureSubItem(item, newSubItem, itemName, itemTopic , String.valueOf(name.getText()),String.valueOf(topic.getText()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
