package ru.codedevice.mqttbroadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class AppActivity extends AppCompatActivity {

    final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.activity_app, null);

        ConstraintLayout sv = v.findViewById(R.id.main_constra);
        // Create a LinearLayout element
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);

//        ll.addView(addSwitch(this));
//
        sv.addView(ll);
//        setContentView(R.layout.activity_app);
        setContentView(v);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btn = findViewById(R.id.button_test);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        Intent intent = new Intent(AppActivity.this, AppService.class);
        intent.putExtra("statusInit","test");
        startService(intent);
            }
        });

//        startService(new Intent(this, AppService.class));

    }

//
//    @Override
//    public void onClick(View v) {
//        Intent intent;
//
//        intent = new Intent(this, SecondActivity.class);
//        startActivityForResult(intent, REQUEST_CODE);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    int number = data.getIntExtra("number", 1);
            }
        }
    }

    private Switch addSwitch(Context context) {
        Switch aswitch = new Switch(context);
        aswitch.setLayoutParams(new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        aswitch.setText("Name");
//        aswitch.setId(Integer.parseInt("id"));
        aswitch.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        aswitch.setBackgroundColor(Color.parseColor("#DCDCDC"));
        aswitch.setPadding(50,50,50,50);
        return aswitch;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(AppActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
