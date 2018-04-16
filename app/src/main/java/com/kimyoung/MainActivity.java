package com.kimyoung;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    private static final int MENU_LOGGER = 1;
    private static final int MENU_TRACKER = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startRSSLogger() {
        Intent intent = new Intent(MainActivity.this, RSSLogger.class);
        startActivity(intent); // start Logger mode
    }

    public void startTracker() {
        Intent intent = new Intent(MainActivity.this, FindMe.class);
        startActivity(intent); // start Tracker mode
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add menu items
        super.onCreateOptionsMenu(menu); // items for changing map
        menu.add(Menu.NONE, MENU_LOGGER, Menu.NONE, "Logger Mode");
        menu.add(Menu.NONE, MENU_TRACKER, Menu.NONE, "Tracker Mode");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case MENU_LOGGER: // logger mode
                item.setTitle("Logger Mode");
                startRSSLogger();
                return true;
            case MENU_TRACKER: // tracker mode
                item.setTitle("Tracker Mode");
                startTracker();
                return true;
            default: // change map
                return super.onOptionsItemSelected(item);
        }
    }

}
