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
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject Object = array.getJSONObject(i);
                if (Object.get("name").equals(name)){
                    return Object;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public JSONArray removeArr(JSONArray array,String topic){
        JSONArray newArr = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject Object = array.getJSONObject(i);
                if (!Object.get("topic").equals(topic)){
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

        int color = R.color.purple;
        int icon = R.drawable.ic_ghost;
        String name = "Room";
        String topic = "room";
        JSONArray subArray = null;

        if (item != null) {
            try {
                color = obj.getInt("color");
                icon = obj.getInt("icon");
                name = obj.getString("name");
                topic = obj.getString("topic");
                if(obj.has("subArray")){
                    subArray = obj.getJSONArray("subArray");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            item.setIndicatorColorRes(color);
            item.setIndicatorIconRes(icon);
            TextView tit = item.findViewById(R.id.title);
            tit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggleExpanded();
                }
            });

            String finalTopic = topic;
            tit.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View arg0) {
                    Log.d(TAG, "setOnClickListener : ");
                    new MaterialDialog.Builder(AppActivity.this)
                            .title(tit.getText())
                            .positiveText("Delete")
                            .negativeText("Create")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    mExpandingList.removeItem(item);
                                    Log.d(TAG, "finalTopic : " + finalTopic);
                                    Storage.putArr("allArray",removeArr(allArray,finalTopic));
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    createElementSub(item, finalTopic);

                                }
                            })
                            .show();

//
                    return true;
                }
            });
            tit.setText(name);
            Log.d(TAG, "subArray : " + subArray);
            if(subArray!=null && subArray.length()>0){
                Log.d(TAG, "subArray!=null : ");
                item.createSubItems(subArray.length());
                for (int i = 0; i < subArray.length(); i++) {
                    try {
                        JSONObject subObject = subArray.getJSONObject(i);
                        String subname = subObject.getString("name");
                        String subtopic = subObject.getString("topic");
                        final View view = item.getSubItemView(i);

                        configureSubItem(item, view, subname, String.valueOf(tit.getText()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void configureSubItem(final ExpandingItem item, final View view, String subTitle,String title) {
        TextView sub_title = view.findViewById(R.id.sub_title);
        sub_title.setText(subTitle);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "view.setOnClickListener : ");

            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                Log.d(TAG, "setOnClickListener : ");
                new MaterialDialog.Builder(AppActivity.this)
                        .title(sub_title.getText())
                        .positiveText("Delete")
                        .negativeText("Edit")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                item.removeSubItem(view);

                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {

                            }
                        })
                        .show();

//
                return true;
            }
        });
    }

    private void showInsertDialog(final AppActivity.OnItemCreated positive) {
        final EditText text = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(text);
        builder.setTitle(R.string.enter_title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positive.itemCreated(text.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    public void createElementItem(){
        View customView;
        EditText name;
        EditText topic;
        Button button;
        MaterialDialog dialog = new MaterialDialog.Builder(AppActivity.this)
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
//                Boolean has = findArr(allArray, String.valueOf(name.getText()));
//                Log.d(TAG, "has : " + has);
                try {

                    JSONObject newObj = new JSONObject();
                    newObj.put("name",name.getText());
                    newObj.put("topic",topic.getText());
                    newObj.put("color",R.color.blue);
                    newObj.put("icon",R.drawable.ic_ghost);
                    allArray.put(newObj);

                    Storage.putArr("allArray",allArray);
                    addItemObject(newObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void createElementSub(ExpandingItem item,String top){

        View customView;
        EditText name;
        EditText topic;
        Button button;
        MaterialDialog dialog = new MaterialDialog.Builder(AppActivity.this)
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
                JSONObject obj = findArr(allArray,top);
                JSONArray subArr;
                try {
                    if(obj.has("subArray")){
                        subArr = obj.getJSONArray("subArray");
                    }else{
                        subArr = new JSONArray();
                    }
                    JSONObject newObj = new JSONObject();
                    newObj.put("name",name.getText());
                    newObj.put("topic",topic.getText());
                    subArr.put(newObj);

                    View newSubItem = item.createSubItem();
                    configureSubItem(item, newSubItem, String.valueOf(name.getText()),"");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
