package ru.codedevice.mqttbroadcastreceiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

import static android.view.View.*;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String TAG = "AppActivity";
    private ExpandingList mExpandingList;

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
                new MaterialDialog.Builder(AppActivity.this)
                        .title("Enter title")
//                        .content(R.string.input_content)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT)
                        .input("Holl", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                addItem(String.valueOf(input), new String[]{"First"}, R.color.purple, R.drawable.ic_ghost);
                            }
                        }).show();

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
        createItems();
//        startActivity(new Intent(AppActivity.this, MainActivity.class));
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


    private void createItems() {
        addItem("John", new String[]{"House", "Boat", "Candy", "Collection", "Sport", "Ball", "Head"}, R.color.pink, R.drawable.ic_ghost);
        addItem("Mary", new String[]{"Dog", "Horse", "Boat"}, R.color.blue, R.drawable.ic_ghost);
//        addItem("Ana", new String[]{"Cat"}, R.color.purple, R.drawable.ic_ghost);
//        addItem("Peter", new String[]{"Parrot", "Elephant", "Coffee"}, R.color.yellow, R.drawable.ic_ghost);
//        addItem("Joseph", new String[]{}, R.color.orange, R.drawable.ic_ghost);
//        addItem("Paul", new String[]{"Golf", "Football"}, R.color.green, R.drawable.ic_ghost);
//        addItem("Larry", new String[]{"Ferrari", "Mazda", "Honda", "Toyota", "Fiat"}, R.color.blue, R.drawable.ic_ghost);
//        addItem("Moe", new String[]{"Beans", "Rice", "Meat"}, R.color.yellow, R.drawable.ic_ghost);
//        addItem("Bart", new String[]{"Hamburger", "Ice cream", "Candy"}, R.color.purple, R.drawable.ic_ghost);
    }

    private void addItem(String title, String[] subItems, int colorRes, int iconRes) {
        //Let's create an item with R.layout.expanding_layout
        final ExpandingItem item = mExpandingList.createNewItem(R.layout.expanding_layout);


        //If item creation is successful, let's configure it

        if (item != null) {
//            item.setStateChangedListener(new ExpandingItem.OnItemStateChanged() {
//                @Override
//                public void itemCollapseStateChanged(boolean expanded) {
//                    Log.e(TAG, "itemCollapseStateChanged : "+expanded);
//                }
//
//            });
            item.setIndicatorColorRes(colorRes);
            item.setIndicatorIconRes(iconRes);
            //It is possible to get any view inside the inflated layout. Let's set the text in the item
            TextView tit = item.findViewById(R.id.title);

            tit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.toggleExpanded();
                }
            });

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
                                }
                            })
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    showInsertDialog(new MainActivity.OnItemCreated() {
                                        @Override
                                        public void itemCreated(String title) {
                                            View newSubItem = item.createSubItem();
                                            configureSubItem(item, newSubItem, title,"");
                                        }
                                    });
                                }
                            })
                            .show();

//
                    return true;
                }
            });

            tit.setText(title);

            //We can create items in batch.
            item.createSubItems(subItems.length);
            for (int i = 0; i < item.getSubItemsCount(); i++) {
                //Let's get the created sub item by its index
                final View view = item.getSubItemView(i);
                //Let's set some values in
                configureSubItem(item, view, subItems[i], String.valueOf(tit.getText()));
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

                final PrettyDialog dialog = new PrettyDialog(AppActivity.this);
                dialog
                        .addButton(
                                "OK",     // button text
                                R.color.pdlg_color_white,
                                R.color.pdlg_color_green,
                                new PrettyDialogCallback() {  // button OnClick listener
                                    @Override
                                    public void onClick() {
                                        Log.d(TAG, "item.findViewById(R.id.title) : "+title);
                                    }
                                }
                        )
                        .addButton(
                                "Cancel",
                                R.color.pdlg_color_white,
                                R.color.pdlg_color_red,
                                new PrettyDialogCallback() {
                                    @Override
                                    public void onClick() {
                                        // Dismiss
                                    }
                                }
                        )
                        .addButton(
                                "Option 3",
                                R.color.pdlg_color_black,
                                R.color.pdlg_color_gray,
                                null
                        )
                        .show();

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

    private void showInsertDialog(final MainActivity.OnItemCreated positive) {
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

}
