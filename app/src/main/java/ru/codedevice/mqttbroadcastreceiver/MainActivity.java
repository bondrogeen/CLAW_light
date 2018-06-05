package ru.codedevice.mqttbroadcastreceiver;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.diegodobelo.expandingview.ExpandingItem;
import com.diegodobelo.expandingview.ExpandingList;


public class MainActivity extends AppCompatActivity {
    private ExpandingList mExpandingList;
    String TAG = "MainActivity";


    private Toast toast;

    interface OnItemCreated {
        void itemCreated(String title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        mExpandingList = findViewById(R.id.expanding_list_main);
        createItems();

    }

    private void createItems() {
        addItem("John", new String[]{"House", "Boat", "Candy", "Collection", "Sport", "Ball", "Head"}, R.color.pink, R.drawable.ic_ghost);
        addItem("Mary", new String[]{"Dog", "Horse", "Boat"}, R.color.blue, R.drawable.ic_ghost);
        addItem("Ana", new String[]{"Cat"}, R.color.purple, R.drawable.ic_ghost);
        addItem("Peter", new String[]{"Parrot", "Elephant", "Coffee"}, R.color.yellow, R.drawable.ic_ghost);
        addItem("Joseph", new String[]{}, R.color.orange, R.drawable.ic_ghost);
        addItem("Paul", new String[]{"Golf", "Football"}, R.color.green, R.drawable.ic_ghost);
        addItem("Larry", new String[]{"Ferrari", "Mazda", "Honda", "Toyota", "Fiat"}, R.color.blue, R.drawable.ic_ghost);
        addItem("Moe", new String[]{"Beans", "Rice", "Meat"}, R.color.yellow, R.drawable.ic_ghost);
        addItem("Bart", new String[]{"Hamburger", "Ice cream", "Candy"}, R.color.purple, R.drawable.ic_ghost);
    }

    private void addItem(String title, String[] subItems, int colorRes, int iconRes) {
        //Let's create an item with R.layout.expanding_layout
        final ExpandingItem item = mExpandingList.createNewItem(R.layout.expanding_layout);

        //If item creation is successful, let's configure it
        if (item != null) {
            item.setIndicatorColorRes(colorRes);
            item.setIndicatorIconRes(iconRes);
            //It is possible to get any view inside the inflated layout. Let's set the text in the item
            ((TextView) item.findViewById(R.id.title)).setText(title);
//            ((TextView) item.findViewById(R.id.title)).setOnLongClickListener(new View.OnLongClickListener() {
//                public boolean onLongClick(View v) {
//                    Log.d(TAG, "setOnLongClickListener : ");
//                    return true;
//                }
//            });



            //We can create items in batch.
            item.createSubItems(subItems.length);
            for (int i = 0; i < item.getSubItemsCount(); i++) {
                //Let's get the created sub item by its index
                final View view = item.getSubItemView(i);
                //Let's set some values in
                configureSubItem(item, view, subItems[i]);
            }

//            item.findViewById(R.id.add_more_sub_items).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showInsertDialog(new OnItemCreated() {
//                        @Override
//                        public void itemCreated(String title) {
//                            View newSubItem = item.createSubItem();
//                            configureSubItem(item, newSubItem, title);
//                        }
//                    });
//                }
//            });
//
//            item.findViewById(R.id.remove_item).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mExpandingList.removeItem(item);
//                }
//            });
        }
    }

    private void showToast(String message) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void configureSubItem(final ExpandingItem item, final View view, String subTitle) {
        ((TextView) view.findViewById(R.id.sub_title)).setText(subTitle);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "view.setOnClickListener : ");

            }
        });


//        final ImageView remove_sub_item = view.findViewById(R.id.remove_sub_item);
//        remove_sub_item.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "setOnClickListener : ");
//                item.removeSubItem(view);
//
//            }
//        });
//
//        remove_sub_item.setOnLongClickListener(new View.OnLongClickListener() {
//            public boolean onLongClick(View v) {
//                Log.d(TAG, "setOnLongClickListener : ");
//                remove_sub_item.setVisibility(View.GONE);
//                return true;
//            }
//        });
    }

    private void showInsertDialog(final OnItemCreated positive) {
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
