package ru.codedevice.mqttbroadcastreceiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import java.util.concurrent.atomic.AtomicInteger;

import me.drakeet.materialdialog.MaterialDialog;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    String TAG = "AppActivity";
    int width;
    MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_main, null);

        ConstraintLayout sv = v.findViewById(R.id.constraint_layout);


        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        Log.d(TAG, "width : " +width);


//        LinearLayout ll = new LinearLayout(this);
//        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        ll.setOrientation(LinearLayout.VERTICAL);
//
//        ll.addView(addSwitch(this));

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if(android.os.Build.VERSION.SDK_INT < 16) {
            scroll.setBackgroundDrawable(getResources().getDrawable(R.drawable.side_nav_bar));
        }else {
            scroll.setBackground(getResources().getDrawable(R.drawable.side_nav_bar));
        }

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        ll.addView(addSwitch(this));
        ll.addView(addSwitch(this));


        scroll.addView(ll);
        sv.addView(scroll);

        setContentView(v);

//        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mMaterialDialog = new MaterialDialog(this)
                .setTitle("MaterialDialog")
                .setMessage("Hello world!")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });

        mMaterialDialog.show();

        mMaterialDialog.setTitle("Ghjdthrf");
        mMaterialDialog.show();
// You can change the message anytime. after show
        mMaterialDialog.setMessage("Текст сообщения");

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
    public boolean onNavigationItemSelected(MenuItem item) {
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


    private LinearLayout addSwitch(Context context) {
        LinearLayout ll = new LinearLayout(this);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        param.setMargins(20,30,20,10);
        ll.setLayoutParams(param);
        ll.setOrientation(LinearLayout.HORIZONTAL);


        Switch aswitch = new Switch(context);
        aswitch.setLayoutParams(new ViewGroup.LayoutParams
                (width/2, ViewGroup.LayoutParams.WRAP_CONTENT));
        aswitch.setText("Name");
        aswitch.setId(generateViewId());
        aswitch.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        aswitch.setBackgroundColor(Color.parseColor("#DCDCDC"));
        aswitch.setPadding(50,50,50,50);

        ll.addView(aswitch);

        Switch aswitch2 = new Switch(context);
        aswitch2.setLayoutParams(new ViewGroup.LayoutParams
                (width/2, ViewGroup.LayoutParams.WRAP_CONTENT));
        aswitch2.setText("Name");
        aswitch2.setId(generateViewId());
        aswitch2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        aswitch2.setBackgroundColor(Color.parseColor("#DCDCDC"));
        aswitch2.setPadding(50,50,50,50);

        ll.addView(aswitch2);

        return ll;
    }

    @SuppressLint("NewApi")
    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }

    }

}
